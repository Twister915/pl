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

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.spi.InjectionPoint;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import sh.joey.pl.command.Args;
import sh.joey.pl.command.JCmd;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

final class JCommandExecutor implements CommandExecutor, TabCompleter {
    private final Class<? extends JCmd> commandExecutor;
    private final Provider<? extends Injector> parentInjector;
    private final boolean onlyAcceptsPlayer;

    public JCommandExecutor(Class<? extends JCmd> commandExecutor, Provider<? extends Injector> parentInjector) {
        this.commandExecutor = commandExecutor;
        this.parentInjector = parentInjector;
        this.onlyAcceptsPlayer = checkOnlyAcceptsPlayer(commandExecutor);
    }

    private static boolean checkOnlyAcceptsPlayer(Class<? extends JCmd> executor) {
        for (InjectionPoint point : InjectionPoint.forInstanceMethodsAndFields(executor)) {
            if (!(point.getMember() instanceof Field))
                continue;

            Key<?> key = point.getDependencies().get(0).getKey();
            if (key.getTypeLiteral().getRawType().equals(Player.class)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (onlyAcceptsPlayer && !(sender instanceof Player))
            return false;

        childInjector(sender, args).getInstance(commandExecutor).doCommand();
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (onlyAcceptsPlayer && !(sender instanceof Player))
            return Collections.emptyList();

        return childInjector(sender, args).getInstance(commandExecutor).doComplete();
    }

    private Injector childInjector(CommandSender sender, String[] args) {
        return parentInjector.get().createChildInjector(binder -> {
            binder.bind(CommandSender.class).toInstance(sender);
            if (sender instanceof Player) {
                binder.bind(Player.class).toInstance(((Player) sender));
            }

            binder.bind(String[].class).annotatedWith(Args.class).toInstance(args);
        });
    }
}
