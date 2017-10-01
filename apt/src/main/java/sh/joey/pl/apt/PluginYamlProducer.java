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

import lombok.SneakyThrows;
import org.yaml.snakeyaml.Yaml;
import sh.joey.pl.Dep;
import sh.joey.pl.Pl;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class PluginYamlProducer {
    private PluginYamlProducer() {}

    static String generate(Yaml yaml, PluginSpec from) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("main", from.getPluginClass());
        data.put("name", validateName(getName(from)));
        data.put("version", validateVersion(getVersion(from)));

        Putter<Pl, String, Object> putter = ifNotEmpty(from.getPl(), putter(data));
        putter.put("description", Pl::description);
        putter.put("authors", Pl::authors, a -> a.length != 0);
        putter.put("loadOn", y -> y.loadOn().name(), loadOn -> !loadOn.equals("POSTWORLD"));

        putter.put("depend", y -> Stream.of(y.depend())
                                        .filter(d -> !d.soft())
                                        .map(Dep::value)
                                        .collect(Collectors.toList()), list -> list.size() != 0);

        putter.put("softdepend", y -> Stream.of(y.depend())
                                            .filter(d -> d.soft())
                                            .map(Dep::value)
                                            .collect(Collectors.toList()), list -> list.size() != 0);
        putter.put("loadbefore", Pl::loadbefore, lb -> lb.length != 0);
        putter.put("prefix", Pl::prefix);
        putter.put("website", Pl::website);

        Map<String, Map<String, Object>> commandMap = new HashMap<>();
        putCommands(commandMap, from.getCommands());
        if (!commandMap.isEmpty())
            data.put("commands", commandMap);

        return yaml.dump(data);
    }

    private static String validateName(String name) {
        if (!name.matches("[A-Za-z0-9_\\-]+"))
            throw new ProcessingException(null, "Invalid name '%s' for plugin!", name);

        return name;
    }

    private static String validateVersion(String version) {
        if (!version.matches("[0-9a-zA-Z._+\\-]+"))
            throw new ProcessingException(null, "Invalid version '%s' for plugin!", version);

        return version;
    }

    private static void putCommands(Map<String, Map<String, Object>> commandMap, Map<String, Command> commandData) {
        commandData.forEach((name, data) -> {
            Map<String, Object> cmdSpec = new HashMap<>();
            Putter<Command, String, Object> putter = ifNotEmpty(data, putter(cmdSpec));

            putter.put("description", d -> d.getAnnotation().description());
            putter.put("usage", d -> d.getAnnotation().usage());
            putter.put("permission", d -> d.getAnnotation().permission());
            putter.put("aliases", d -> d.getAnnotation().aliases(), a -> a.length > 0);

            commandMap.put(name, cmdSpec);
        });
    }

    private static <K, T> Function<K, Consumer<T>> putter(Map<? super K, ? super T> data) {
        return key -> value -> data.put(key, value);
    }

    private static <S, K, V> Putter<S, K, V> ifNotEmpty(S spec, Function<K, Consumer<V>> consumer) {
        return new Putter<S, K, V>() {
            @Override
            @SneakyThrows
            public <V1 extends V> void put(K key, Function<S, ? extends V1> getter, Predicate<V1> checker) {
                V1 value = getter.apply(spec);
                if (value == null || !checker.apply(value))
                    return;

                consumer.apply(key).accept(value);
            }
        };
    }

    static String getName(PluginSpec from) {
        String name = from.getPl().name();
        if (name.length() > 0)
            return name;

        String[] classParts = from.getPluginClass().split("\\.");
        return classParts[classParts.length - 1];
    }

    static String getVersion(PluginSpec from) {
        switch (from.getPl().version()) {
            case "__GIT__":
                throw new UnsupportedOperationException("no support for git version!");
            case "":
                throw new IllegalArgumentException("need to supply version number");
            default:
                return from.getPl().version();
        }
    }

    private interface Putter<S, K, V> {
        <V1 extends V> void put(K key, Function<S, ? extends V1> getter, Predicate<V1> checker);
        default void put(K key, Function<S, ? extends V> getter) {
            put(key, getter, v -> v.toString().length() > 0);
        }
    }

    private interface Function<T, R> {
        R apply(T v1) throws Exception;
    }

    private interface Predicate<V> extends Function<V, Boolean> {}

    private interface Consumer<T> {
        void accept(T value) throws Exception;
    }
}
