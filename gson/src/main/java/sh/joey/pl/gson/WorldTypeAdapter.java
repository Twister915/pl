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

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.IOException;
import java.util.UUID;

final class WorldTypeAdapter extends TypeAdapter<World> {
    @Override
    public void write(JsonWriter out, World value) throws IOException {
        out.value(value.getUID().toString());
    }

    @Override
    public World read(JsonReader in) throws IOException {
        return Bukkit.getWorld(UUID.fromString(in.nextString()));
    }
}
