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

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.IOException;
import java.util.UUID;

final class LocationTypeAdapter extends TypeAdapter<Location> {
    private final static String WORLD_KEY = "w", X_KEY = "x", Y_KEY = "y", Z_KEY = "z", PITCH = "p", YAW = "ya";

    @Override
    public void write(JsonWriter out, Location value) throws IOException {
        out.beginObject();
        out.name(WORLD_KEY);
        out.value(value.getWorld().getUID().toString());
        out.name(X_KEY);
        out.value(value.getX());
        out.name(Y_KEY);
        out.value(value.getY());
        out.name(Z_KEY);
        out.value(value.getZ());
        out.name(PITCH);
        out.value(value.getPitch());
        out.name(YAW);
        out.value(value.getYaw());
        out.endObject();
    }

    @Override
    public Location read(JsonReader in) throws IOException {
        in.beginObject();
        Double x = null, y = null, z = null, pitch = null, yaw = null;
        World world = null;
        while (in.hasNext()) {
            String s = in.nextName();
            if (s.equals(WORLD_KEY)) {
                UUID uuid = UUID.fromString(in.nextString());
                world = Bukkit.getWorld(uuid);
                if (world == null)
                    throw new JsonParseException("Could not find the world by the UUID: " + uuid.toString());
                continue;
            }
            double v = in.nextDouble();
            switch (s) {
                case X_KEY:
                    x = v;
                    break;
                case Y_KEY:
                    y = v;
                    break;
                case Z_KEY:
                    z = v;
                    break;
                case PITCH:
                    pitch = v;
                    break;
                case YAW:
                    yaw = v;
                    break;
            }
        }
        in.endObject();

        if (world == null || x == null || y == null || z == null || pitch == null || yaw == null)
            throw new JsonParseException("Could not read Location object, missing a critical value (expecting world, x, y, z, p, ya)");

        return new Location(world, x, y, z, yaw.floatValue(), pitch.floatValue());
    }
}
