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

package sh.joey.pl.rx.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import javax.inject.Inject;

final class BukkitSyncWorker extends BukkitWorker {
    private final Plugin plugin;

    @Inject BukkitSyncWorker(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    BukkitTask later(Runnable r, long tickDelay) {
        return Bukkit.getScheduler().runTaskLater(plugin, r, tickDelay);
    }

    @Override
    BukkitTask asap(Runnable r) {
        return Bukkit.getScheduler().runTaskAsynchronously(plugin, r);
    }

    @Override
    boolean canRunImmediately() {
        return Bukkit.isPrimaryThread();
    }
}
