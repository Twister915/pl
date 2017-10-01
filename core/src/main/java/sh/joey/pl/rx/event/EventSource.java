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

package sh.joey.pl.rx.event;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import sh.joey.pl.rx.scheduler.Kind;
import sh.joey.pl.rx.scheduler.ServerScheduler;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public final class EventSource {
    private final Plugin plugin;
    private final Scheduler syncScheduler;

    @Inject
    EventSource(Plugin plugin,
                @ServerScheduler(Kind.SYNC) Scheduler scheduler) {
        this.plugin = plugin;
        this.syncScheduler = scheduler;
    }

    public <T extends Event> Observable<T> observeEvent(Class<T> type) {
        return observeEventRaw(type).observeOn(syncScheduler);
    }

    public <T extends Event> Observable<T> observeEvent(Class<T> type, EventPriority priority) {
        return observeEventRaw(type, priority).observeOn(syncScheduler);
    }

    public <T extends Event> Observable<T> observeEvent(Class<T> type, EventPriority priority, boolean ignoreCancelled) {
        return observeEventRaw(type, priority, ignoreCancelled).observeOn(syncScheduler);
    }

    @SafeVarargs
    public final <T extends Event> Observable<T> observeEvent(EventPriority priority, boolean ignoreCancelled, Class<? extends T>... types) {
        return observeEventRaw(priority, ignoreCancelled, types).observeOn(syncScheduler);
    }

    @SafeVarargs
    public final <T extends Event> Observable<T> observeEvent(boolean ignoreCancelled, Class<? extends T>... types) {
        return observeEventRaw(ignoreCancelled, types).observeOn(syncScheduler);
    }

    @SafeVarargs
    public final <T extends Event> Observable<T> observeEvent(EventPriority priority, Class<? extends T>... types) {
        return observeEventRaw(priority, types).observeOn(syncScheduler);
    }

    @SafeVarargs
    public final <T extends Event> Observable<T> observeEvent(Class<? extends T>... types) {
        return observeEventRaw(types).observeOn(syncScheduler);
    }

    public <T extends Event> Observable<T> observeEvent(Set<Class<? extends T>> kinds) {
        return observeEventRaw(kinds).observeOn(syncScheduler);
    }

    public <T extends Event> Observable<T> observeEvent(Set<Class<? extends T>> kinds, EventPriority priority) {
        return observeEventRaw(kinds, priority).observeOn(syncScheduler);
    }

    public <T extends Event> Observable<T> observeEvent(Set<Class<? extends T>> kinds, EventPriority priority, boolean ignoreCancelled) {
        return observeEventRaw(kinds, priority, ignoreCancelled).observeOn(syncScheduler);
    }

    public <T extends Event> Observable<T> observeEventRaw(Class<T> type) {
        return observeEventRaw(type, EventPriority.NORMAL);
    }

    public <T extends Event> Observable<T> observeEventRaw(Class<T> type, EventPriority priority) {
        return observeEventRaw(type, priority, false);
    }

    public <T extends Event> Observable<T> observeEventRaw(Class<T> type, EventPriority priority, boolean ignoreCancelled) {
        return observeEventRaw(Collections.singleton(type), priority, ignoreCancelled);
    }

    @SafeVarargs
    public final <T extends Event> Observable<T> observeEventRaw(boolean ignoreCancelled, Class<? extends T>... types) {
        return observeEventRaw(EventPriority.NORMAL, ignoreCancelled, types);
    }

    @SafeVarargs
    public final <T extends Event> Observable<T> observeEventRaw(EventPriority priority, Class<? extends T>... types) {
        return observeEventRaw(priority, false, types);
    }

    @SafeVarargs
    public final <T extends Event> Observable<T> observeEventRaw(Class<? extends T>... types) {
        return observeEventRaw(EventPriority.NORMAL, types);
    }

    @SafeVarargs
    public final <T extends Event> Observable<T> observeEventRaw(EventPriority priority, boolean ignoreCancelled, Class<? extends T>... types) {
        return observeEventRaw(ImmutableSet.copyOf(types), priority, ignoreCancelled);
    }

    public final <T extends Event> Observable<T> observeEventRaw(Set<Class<? extends T>> kinds) {
        return observeEventRaw(kinds, EventPriority.NORMAL);
    }

    public final <T extends Event> Observable<T> observeEventRaw(Set<Class<? extends T>> kinds, EventPriority priority) {
        return observeEventRaw(kinds, priority, false);
    }

    @SuppressWarnings("unchecked")
    public final <T extends Event> Observable<T> observeEventRaw(Set<Class<? extends T>> kinds, EventPriority priority, boolean ignoreCancelled) {
        return Observable.<T>create(subscriber -> {
            AtomicBoolean isDisposed = new AtomicBoolean(false);

            Listener listener = new Listener() {
                @EventHandler(priority = EventPriority.HIGHEST)
                void onDisable(PluginDisableEvent event) {
                    if (event.getPlugin() != plugin)
                        return;

                    if (!isDisposed.compareAndSet(false, true))
                        return;

                    subscriber.onComplete();
                }
            };

            PluginManager manager = Bukkit.getPluginManager();
            manager.registerEvents(listener, plugin);
            for (Class<? extends T> kind : kinds) {
                manager.registerEvent(kind, listener, priority, (l, event) -> {
                    if (isDisposed.get())
                        return;

                    if (!canDispatch(kinds, event))
                        return;

                    try {
                        subscriber.onNext((T) event);
                    } catch (Throwable t) {
                        subscriber.tryOnError(t);
                    }
                }, plugin, ignoreCancelled);
            }

            subscriber.setDisposable(new Disposable() {
                @Override
                public void dispose() {
                    if (!isDisposed.compareAndSet(false, true))
                        return;

                    HandlerList.unregisterAll(listener);
                    subscriber.onComplete();
                }

                @Override
                public boolean isDisposed() {
                    return isDisposed.get();
                }
            });
        }).subscribeOn(syncScheduler);
    }

    private static <T extends Event> boolean canDispatch(Set<Class<? extends T>> kinds, Event v) {
        Class<? extends Event> aClass = v.getClass();
        for (Class<? extends T> kind : kinds)
            if (kind == aClass || kind.isAssignableFrom(aClass))
                return true;

        return false;
    }

}
