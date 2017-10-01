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

package sh.joey.example;

import io.reactivex.disposables.CompositeDisposable;
import org.bukkit.event.player.PlayerJoinEvent;
import sh.joey.pl.Dep;
import sh.joey.pl.JPl;
import sh.joey.pl.Pl;

@Pl(
    version = "1.0.0",
    depend = {
        @Dep("mucore"),
        @Dep(value = "factions", soft = true)
    }
)
public final class ExamplePl extends JPl {
    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void up() throws Throwable {
        disposable.add(observeEvent(PlayerJoinEvent.class)
                            .subscribe(event -> event.getPlayer().sendMessage("yo welcome to the server broski")));
        getLogger().info("started");
    }

    @Override
    protected void down() throws Throwable {
        disposable.dispose();
        getLogger().info("disabled");
    }
}
