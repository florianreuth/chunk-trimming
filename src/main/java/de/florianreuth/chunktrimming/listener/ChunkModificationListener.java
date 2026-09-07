/*
 * This file is part of chunk-trimming - https://github.com/florianreuth/chunk-trimming
 * Copyright (C) 2026 Florian Reuth <git@florianreuth.de>
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

package de.florianreuth.chunktrimming.listener;

import de.florianreuth.chunktrimming.service.ChunkTrackingService;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

public final class ChunkModificationListener implements Listener {

    private final ChunkTrackingService service;

    public ChunkModificationListener(final ChunkTrackingService service) {
        this.service = service;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDispense(final BlockDispenseEvent event) {
        this.service.markModified(event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonExtend(final BlockPistonExtendEvent event) {
        this.markPushedBlocks(event.getBlocks(), event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonRetract(final BlockPistonRetractEvent event) {
        this.markPushedBlocks(event.getBlocks(), event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(final EntityExplodeEvent event) {
        this.service.markModified(event.getLocation());
        this.service.markModifiedBlocks(event.blockList());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityChangeBlock(final EntityChangeBlockEvent event) {
        this.service.markModified(event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntitySpawn(final EntitySpawnEvent event) {
        final Entity entity = event.getEntity();
        if (!(entity instanceof Mob)) {
            this.service.markModified(entity.getLocation());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onProjectileHit(final ProjectileHitEvent event) {
        final Block hitBlock = event.getHitBlock();
        this.service.markModified(hitBlock != null ? hitBlock.getLocation() : event.getEntity().getLocation());
    }

    private void markPushedBlocks(final List<Block> blocks, final BlockPistonEvent event) {
        this.service.markModified(event.getBlock());
        for (final Block block : blocks) {
            this.service.markModified(block);
            this.service.markModified(block.getRelative(event.getDirection()));
        }
    }

}
