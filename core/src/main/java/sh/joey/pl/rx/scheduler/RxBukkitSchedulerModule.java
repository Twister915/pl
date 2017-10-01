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

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.reactivex.Scheduler;

public final class RxBukkitSchedulerModule extends AbstractModule {
    @Override protected void configure() {}

    @Provides
    @ServerScheduler(Kind.ASYNC)
    @Singleton
    Scheduler asyncScheduler(Provider<Injector> injector) {
        return new AbstractBukkitScheduler<>(injector, BukkitAsyncWorker.class);
    }

    @Provides
    @ServerScheduler(Kind.SYNC)
    @Singleton
    Scheduler syncScheduler(Provider<Injector> injector) {
        return new AbstractBukkitScheduler<>(injector, BukkitSyncWorker.class);
    }
}
