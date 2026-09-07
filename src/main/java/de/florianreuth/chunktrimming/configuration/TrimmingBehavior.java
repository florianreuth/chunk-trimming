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

package de.florianreuth.chunktrimming.configuration;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class TrimmingBehavior {

    private final int saveRadius;
    private final Set<String> excludedWorlds;

    public TrimmingBehavior(final FileConfiguration configuration) {
        this.saveRadius = Math.clamp(configuration.getInt("save-radius", 2), 1, 32);
        final List<String> worlds = configuration.getStringList("excluded-worlds");
        this.excludedWorlds = worlds.stream().map(world -> world.toLowerCase(Locale.ROOT)).collect(Collectors.toUnmodifiableSet());
    }

    public int saveRadius() {
        return this.saveRadius;
    }

    public boolean isExcluded(final World world) {
        return this.excludedWorlds.contains(world.getName().toLowerCase(Locale.ROOT));
    }

}
