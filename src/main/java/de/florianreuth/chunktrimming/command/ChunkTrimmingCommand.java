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

package de.florianreuth.chunktrimming.command;

import de.florianreuth.chunktrimming.configuration.TrimmingBehavior;
import de.florianreuth.chunktrimming.service.ChunkTrackingService;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import static net.kyori.adventure.text.Component.text;

public final class ChunkTrimmingCommand implements CommandExecutor {

    private final TrimmingBehavior behavior;
    private final ChunkTrackingService service;

    public ChunkTrimmingCommand(final TrimmingBehavior behavior, final ChunkTrackingService service) {
        this.behavior = behavior;
        this.service = service;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final long trimmed = this.service.trimmedWrites();
        final long keptByVisit = this.service.keptByVisit();
        final long keptByModification = this.service.keptByModification();
        final long keptByExistingData = this.service.keptByExistingData();
        final long total = trimmed + keptByVisit + keptByModification + keptByExistingData;
        final double ratio = total == 0L ? 0.0D : trimmed * 100.0D / total;

        sender.sendMessage(text("Chunk Trimming", NamedTextColor.AQUA));
        sender.sendMessage(entry("Skipped writes", String.format(Locale.ROOT, "%,d (%.1f%%)", trimmed, ratio)));
        sender.sendMessage(entry("Saved by existing data", String.format(Locale.ROOT, "%,d", keptByExistingData)));
        sender.sendMessage(entry("Saved by radius " + this.behavior.saveRadius(), String.format(Locale.ROOT, "%,d", keptByVisit)));
        sender.sendMessage(entry("Saved by modification", String.format(Locale.ROOT, "%,d", keptByModification)));
        sender.sendMessage(entry("Tracked chunks", String.format(Locale.ROOT, "%,d", this.service.trackedChunks())));
        return true;
    }

    private Component entry(final String label, final String value) {
        return text(label + ": ", NamedTextColor.GRAY).append(text(value, NamedTextColor.WHITE));
    }

}
