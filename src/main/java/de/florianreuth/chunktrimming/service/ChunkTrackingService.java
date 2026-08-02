/*
 * This file is part of chunk-trimming - https://github.com/florianreuth/chunk-trimming
 * Copyright (C) 2026 Florian Reuth <git@florianreuth.de> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.florianreuth.chunktrimming.service;

import de.florianreuth.chunktrimming.configuration.TrimmingBehavior;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public final class ChunkTrackingService implements Listener {

    private final TrimmingBehavior behavior;
    private final Map<UUID, Map<Long, MarkReason>> markedChunks = new ConcurrentHashMap<>();
    private final Map<UUID, Set<Long>> generatedChunks = new ConcurrentHashMap<>();

    private final AtomicLong trimmedWrites = new AtomicLong();
    private final AtomicLong keptByVisit = new AtomicLong();
    private final AtomicLong keptByModification = new AtomicLong();
    private final AtomicLong keptByExistingData = new AtomicLong();

    public ChunkTrackingService(final TrimmingBehavior behavior) {
        this.behavior = behavior;
    }

    public void markVisited(final World world, final int chunkX, final int chunkZ) {
        this.mark(world, chunkX, chunkZ, MarkReason.VISITED);
    }

    public void markModified(final Block block) {
        this.mark(block.getWorld(), block.getX() >> 4, block.getZ() >> 4, MarkReason.MODIFIED);
    }

    public void markModified(final Location location) {
        final World world = location.getWorld();
        if (world == null) {
            return;
        }

        this.mark(world, location.getBlockX() >> 4, location.getBlockZ() >> 4, MarkReason.MODIFIED);
    }

    public void markModifiedBlocks(final Collection<Block> blocks) {
        for (final Block block : blocks) {
            this.markModified(block);
        }
    }

    @EventHandler
    public void onChunkLoad(final ChunkLoadEvent event) {
        final World world = event.getWorld();
        if (event.isNewChunk() && !this.behavior.isExcluded(world)) {
            final Chunk chunk = event.getChunk();
            final Set<Long> chunks = this.generatedChunks.computeIfAbsent(world.getUID(), key -> ConcurrentHashMap.newKeySet());
            chunks.add(chunkKey(chunk.getX(), chunk.getZ()));
        }
    }

    @EventHandler
    public void onChunkUnload(final ChunkUnloadEvent event) {
        final Chunk chunk = event.getChunk();
        final World world = event.getWorld();
        final MarkReason reason = this.unmark(world, chunk.getX(), chunk.getZ());
        final boolean generated = this.unmarkGenerated(world, chunk.getX(), chunk.getZ());

        if (this.behavior.isExcluded(world) || !event.isSaveChunk()) {
            return;
        }

        // A chunk that came from disk already holds state this plugin cannot see, so skipping its write would
        // roll it back to whatever the region file still contains. Only chunks generated since the last save
        // have nothing to lose, because they can be regenerated from the seed.
        if (!generated) {
            this.keptByExistingData.incrementAndGet();
            return;
        }

        if (reason == MarkReason.VISITED) {
            this.keptByVisit.incrementAndGet();
            return;
        }

        if (reason == MarkReason.MODIFIED) {
            this.keptByModification.incrementAndGet();
            return;
        }

        event.setSaveChunk(false);
        this.trimmedWrites.incrementAndGet();
    }

    @EventHandler
    public void onEntitiesUnload(final EntitiesUnloadEvent event) {
        final Chunk chunk = event.getChunk();

        // Entities are saved separately from the chunk, so they would outlive its skipped write and return into a
        // chunk that regenerates its own ones alongside them, leaving everything twice.
        if (this.isDiscarded(event.getWorld(), chunk.getX(), chunk.getZ())) {
            for (final Entity entity : event.getEntities()) {
                if (!(entity instanceof Player)) {
                    entity.remove();
                }
            }
        }
    }

    @EventHandler
    public void onWorldSave(final WorldSaveEvent event) {
        // Everything that was still loaded has been written to disk now, so those chunks are no longer new.
        this.generatedChunks.remove(event.getWorld().getUID());
    }

    @EventHandler
    public void onWorldUnload(final WorldUnloadEvent event) {
        this.markedChunks.remove(event.getWorld().getUID());
        this.generatedChunks.remove(event.getWorld().getUID());
    }

    public long trimmedWrites() {
        return this.trimmedWrites.get();
    }

    public long keptByVisit() {
        return this.keptByVisit.get();
    }

    public long keptByModification() {
        return this.keptByModification.get();
    }

    public long keptByExistingData() {
        return this.keptByExistingData.get();
    }

    public int trackedChunks() {
        int tracked = 0;
        for (final Map<Long, MarkReason> chunks : this.markedChunks.values()) {
            tracked += chunks.size();
        }

        return tracked;
    }

    private void mark(final World world, final int chunkX, final int chunkZ, final MarkReason reason) {
        if (!this.behavior.isExcluded(world)) {
            final Map<Long, MarkReason> chunks = this.markedChunks.computeIfAbsent(world.getUID(), key -> new ConcurrentHashMap<>());

            if (reason == MarkReason.VISITED) {
                chunks.put(chunkKey(chunkX, chunkZ), reason);
            } else {
                chunks.putIfAbsent(chunkKey(chunkX, chunkZ), reason);
            }
        }
    }

    private MarkReason unmark(final World world, final int chunkX, final int chunkZ) {
        final Map<Long, MarkReason> chunks = this.markedChunks.get(world.getUID());
        return chunks == null ? null : chunks.remove(chunkKey(chunkX, chunkZ));
    }

    private boolean unmarkGenerated(final World world, final int chunkX, final int chunkZ) {
        final Set<Long> chunks = this.generatedChunks.get(world.getUID());
        return chunks != null && chunks.remove(chunkKey(chunkX, chunkZ));
    }

    private boolean isDiscarded(final World world, final int chunkX, final int chunkZ) {
        if (this.behavior.isExcluded(world)) {
            return false;
        }

        final long key = chunkKey(chunkX, chunkZ);
        final Set<Long> generated = this.generatedChunks.get(world.getUID());
        if (generated == null || !generated.contains(key)) {
            return false;
        }

        final Map<Long, MarkReason> marked = this.markedChunks.get(world.getUID());
        return marked == null || !marked.containsKey(key);
    }

    private static long chunkKey(final int chunkX, final int chunkZ) {
        return ((long) chunkZ << 32) | (chunkX & 0xFFFFFFFFL);
    }

}
