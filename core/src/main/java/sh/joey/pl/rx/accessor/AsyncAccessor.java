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

import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.Callable;

@RequiredArgsConstructor
public final class AsyncAccessor<T> {
    private final T instance;
    private final Scheduler syncScheduler, asyncScheduler;

    public <V> Single<V> call(Function<T, V> func) {
        return Single.fromCallable(preapply(func, instance)).subscribeOn(asyncScheduler).observeOn(syncScheduler);
    }

    public Completable callVoid(Consumer<T> consumer) {
        return Completable.fromAction(preapply(consumer, instance)).subscribeOn(asyncScheduler).observeOn(syncScheduler);
    }

    private static <T> Action preapply(Consumer<T> consumer, T instance) {
        return () -> consumer.accept(instance);
    }

    private static <T, R> Callable<R> preapply(Function<T, R> func, T inst) {
        return () -> func.apply(inst);
    }
}
