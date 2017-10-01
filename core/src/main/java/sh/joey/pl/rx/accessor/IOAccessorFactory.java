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

package sh.joey.pl.rx.accessor;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import sh.joey.pl.rx.scheduler.ServerScheduler;

final class IOAccessorFactory extends AccessorFactory {
    @Inject IOAccessorFactory(@ServerScheduler Scheduler syncScheduler, Provider<Injector> injector) {
        super(syncScheduler, Schedulers.io(), injector);
    }
}
