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

package sh.joey.pl.apt;

import com.google.common.collect.ImmutableMap;
import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;
import java.util.Map;

public final class CommandMapProducer {
    public static String commandMapProducer(Yaml yaml, Map<String, Command> commandMap) {
        Map<String, Object> out = new HashMap<>();
        commandMap.forEach((name, data) ->
            out.put(name.toLowerCase(), ImmutableMap.builder()
                                                    .put("handler", data.getHandlerClass())
                                                    .build()));

        return yaml.dump(ImmutableMap.builder().put("commands", out).build());
    }
}
