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

package sh.joey.pl;

import com.google.common.io.ByteStreams;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Stage;
import com.mycila.guice.ext.closeable.CloseableInjector;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Delegate;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import sh.joey.pl.command.JCmd;
import sh.joey.pl.parse.Parser;
import sh.joey.pl.parse.ParserKind;
import sh.joey.pl.rx.event.EventSource;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

public abstract class JPl extends JavaPlugin {
    @Inject private Provider<CloseableInjector> injector;
    @Delegate @Inject private EventSource eventSource;
    @Inject @ParserKind("yaml") private Parser yamlParser;

    private boolean enabled;

    @Override
    public final void onEnable() {
        try {
            Guice.createInjector(stage(), new BukkitModule(this)).injectMembers(this);
            loadCommands();
            up();
            enabled = true;
        } catch (Throwable t) {
            getLogger().warning("Failed to up plugin due to error");
            t.printStackTrace();
            setEnabled(false);
        }
    }

    @Override
    public final void onDisable() {
        if (enabled) {
            try {
                down();
            } catch (Throwable t) {
                getLogger().warning("Failed to down plugin due to error");
                t.printStackTrace();
            }
        }

        if (injector != null)
            injector.get().close();
    }

    protected void up() throws Throwable {}
    protected void down() throws Throwable {}
    protected void configure(Installer b) {
        getLogger().warning(getDescription().getName() + " does not configure guice");
    }

    protected Stage stage() {
        if (System.getenv("TEST") != null || System.getenv("DEV") != null)
            return Stage.DEVELOPMENT;

        return Stage.PRODUCTION;
    }

    protected final Pl getAnnotation() {
        Pl annotation = getClass().getAnnotation(Pl.class);
        if (annotation == null)
            throw new IllegalStateException("You must annotate the main class with @Pl");

        return annotation;
    }

    @SuppressWarnings("unchecked")
    private void loadCommands() {
        InputStream resource = getResource("META-INF/.pl.commands.yml");
        if (resource == null)
            return;

        yamlParser.parse(resource, CommandsFile.class).blockingGet().getCommands().forEach((command, handler) -> {
            PluginCommand bukkitCmd = getCommand(command);
            if (bukkitCmd == null)
                throw new IllegalStateException("could not find command '" + command + "' for plugin...");

            Class<?> handlerType;
            try {
                handlerType = Class.forName(handler.handler);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("could not find class " + handler + " for command " + command + "!", e);
            }

            if (!JCmd.class.isAssignableFrom(handlerType)) {
                if (!CommandExecutor.class.isAssignableFrom(handlerType))
                    throw new IllegalStateException(handlerType.getName() + " is not a valid handler class, does not extend JCmd");

                CommandExecutor instance = (CommandExecutor) injector.get().getInstance(handlerType);
                bukkitCmd.setExecutor(instance);
                if (instance instanceof TabCompleter)
                    bukkitCmd.setTabCompleter((TabCompleter) instance);

            } else {
                Class<? extends JCmd> commandType = (Class<? extends JCmd>) handlerType;

                JCommandExecutor executor = new JCommandExecutor(commandType, injector);

                bukkitCmd.setExecutor(executor);
                bukkitCmd.setTabCompleter(executor);
            }

            getLogger().info("loaded command /" + command + " => " + handlerType.getSimpleName());
        });
    }

    public Optional<InputStream> getResourceOrDataFile(String name) {
        File file = new File(getDataFolder(), name);
        if (!file.exists()) {
            InputStream resource = getResource(name);
            if (resource == null)
                return Optional.empty();

            if (!file.mkdirs())
                throw new IllegalArgumentException("could not create dirs for file");

            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                ByteStreams.copy(resource, outputStream);
                return getResourceOrDataFile(name);
            } catch (FileNotFoundException e) {
                return Optional.empty();
            } catch (IOException e) {
                throw new RuntimeException("could not copy resource default to file", e);
            }
        }

        try {
            return Optional.of(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("the file existed, but threw FileNotFoundException", e);
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    private static final class CommandsFile {
        private Map<String, CommandSpec> commands;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    private static final class CommandSpec {
        private String handler;
    }

    protected interface Installer {
        void install(Module m);
    }
}
