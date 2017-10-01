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

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.mycila.guice.ext.closeable.CloseableModule;
import com.mycila.guice.ext.jsr250.Jsr250Module;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;
import sh.joey.pl.JPl;
import sh.joey.pl.parse.ParserModule;
import sh.joey.pl.rx.accessor.RxAccessorModule;
import sh.joey.pl.rx.event.EventSource;
import sh.joey.pl.rx.scheduler.RxBukkitSchedulerModule;

@RequiredArgsConstructor
final class BukkitModule extends AbstractModule {
    private final JPl plugin;

    @SuppressWarnings("unchecked")
    @Override
    protected void configure() {
        install(new CloseableModule());
        install(new Jsr250Module());
        install(new ParserModule());
        install(new RxAccessorModule());
        install(new RxBukkitSchedulerModule());
        install(new ParserModule());

        bind(JPl.class).toInstance(plugin);
        bind(Plugin.class).toInstance(plugin);
        ((AnnotatedBindingBuilder) bind(plugin.getClass())).toInstance(plugin);

        install(binder -> plugin.configure(binder::install));
    }
}
