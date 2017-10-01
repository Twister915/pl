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

import com.google.inject.Injector;
import com.google.inject.Provider;
import io.reactivex.Scheduler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
abstract class AccessorFactory implements AsyncAccessors {
    private final Scheduler syncScheduler, asyncScheduler;
    private final Provider<Injector> injector;

    @Override
    public <T> AsyncAccessor<T> access(T instance) {
        return new AsyncAccessor<>(instance, syncScheduler, asyncScheduler);
    }

    @Override
    public <T> AsyncAccessor<T> access(Class<? extends T> type) {
        return new AsyncAccessor<>(injector.get().getInstance(type), syncScheduler, asyncScheduler);
    }
}
