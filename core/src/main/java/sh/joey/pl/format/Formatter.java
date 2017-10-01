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

package sh.joey.pl.format;

import com.google.common.collect.ImmutableList;
import lombok.Synchronized;
import sh.joey.pl.JPl;
import sh.joey.pl.parse.Parser;
import sh.joey.pl.parse.ParserKind;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public final class Formatter {
    final static int SCREEN_WIDTH = 80;
    private final static String PREFIX_KEY = "prefix";

    private final Map<?, ?> data;
    private String cachedPrefix;

    @Inject
    Formatter(@ParserKind("yaml") Parser yamlParser, JPl plugin) {
        this.data = yamlParser.parse(plugin.getResourceOrDataFile("formats.yml")
                                            .orElseThrow(() -> new IllegalStateException("no formats.yml!")), Map.class)
                              .blockingGet();
    }

    public FormatBuilder at(String at) {
        return new FormatBuilder(getPrefix(), loadFormatLines(at));
    }

    @Synchronized
    private String getPrefix() {
        if (cachedPrefix == null) {
            List<String> strings = loadFormatLines(PREFIX_KEY);
            if (strings.isEmpty())
                cachedPrefix = "";
            else if (strings.size() == 1)
                cachedPrefix = strings.get(0);
            else
                throw new IllegalStateException("the prefix is too many lines");
        }

        return cachedPrefix;
    }

    @SuppressWarnings("unchecked")
    private List<String> loadFormatLines(String at) {
        Object value = valueAt(data, at).orElseThrow(() -> new IllegalArgumentException("could not find format '" + at + "'"));
        if (value instanceof List)
            return ImmutableList.copyOf((List<String>) value);
        else if (value instanceof String)
            return ImmutableList.of((String) value);
        else
            throw new IllegalArgumentException("value at '" + at + "' is instance of " + value.getClass().getName() + "!");
    }

    private static Optional<Map<?, ?>> sectionAt(Map<?, ?> root, String key) {
        key = key.trim();
        if (key.length() == 0)
            return Optional.empty();

        return sectionAt(root, key.split("\\."));
    }

    private static Optional<Map<?, ?>> sectionAt(Map<? , ?> root, String[] keys) {
        if (keys.length == 0)
            return Optional.empty();

        String topKey = keys[0].trim();
        if (topKey.length() == 0)
            return Optional.empty();

        Object rawTop = root.get(topKey);
        if (rawTop == null)
            return Optional.empty();

        if (!(rawTop instanceof Map))
            throw new IllegalArgumentException("Stumbled into a value which is supposed to be a section. Check " + topKey);

        Map<?, ?> top = (Map<?, ?>) rawTop;
        if (keys.length == 1) {
            return Optional.of(top);
        }

        String[] next = new String[keys.length - 1];
        System.arraycopy(keys, 1, next, 0, next.length);
        return sectionAt(top, next);
    }

    private static Optional<Object> valueAt(Map<?, ?> root, String key) {
        key = key.trim();
        if (key.length() == 0)
            return Optional.empty();

        String[] split = key.split("\\.");
        if (split.length == 1)
            root.get(split[0]);

        String[] head = new String[split.length - 1];
        System.arraycopy(split, 0, head, 0, head.length);
        return sectionAt(root, head).map(section -> valueAt(section, split[split.length - 1]));
    }
}
