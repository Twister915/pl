/*
 * Copyright (c) 2017 Joseph Sacchini
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the 2nd version of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package sh.joey.pl.gson;

import com.google.gson.GsonBuilder;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Modifier;

public class GsonConfiguration {
    public GsonBuilder configure(GsonBuilder builder) {
        return baseConfiguration(builder)
                .registerTypeHierarchyAdapter(Location.class, new LocationTypeAdapter())
                .registerTypeHierarchyAdapter(OfflinePlayer.class, new OfflinePlayerTypeAdapter())
                .registerTypeHierarchyAdapter(World.class, new WorldTypeAdapter())
                .registerTypeHierarchyAdapter(ItemStack.class, new ItemStackTypeAdapter());
    }

    protected GsonBuilder baseConfiguration(GsonBuilder builder) {
        return builder
                .serializeNulls()
                .excludeFieldsWithModifiers(Modifier.TRANSIENT);
    }
}
