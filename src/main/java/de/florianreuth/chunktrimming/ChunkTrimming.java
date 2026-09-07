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

package de.florianreuth.chunktrimming;

import de.florianreuth.chunktrimming.command.ChunkTrimmingCommand;
import de.florianreuth.chunktrimming.configuration.TrimmingBehavior;
import de.florianreuth.chunktrimming.listener.ChunkModificationListener;
import de.florianreuth.chunktrimming.listener.PlayerActivityListener;
import de.florianreuth.chunktrimming.service.ChunkTrackingService;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChunkTrimming extends JavaPlugin {

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        final TrimmingBehavior behavior = new TrimmingBehavior(this.getConfig());

        final ChunkTrackingService service = new ChunkTrackingService(behavior);
        this.getServer().getPluginManager().registerEvents(service, this);
        this.getServer().getPluginManager().registerEvents(new PlayerActivityListener(behavior, service), this);
        this.getServer().getPluginManager().registerEvents(new ChunkModificationListener(service), this);

        this.getCommand("chunktrimming").setExecutor(new ChunkTrimmingCommand(behavior, service));
    }

}
