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

import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Synchronized;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

abstract class BukkitWorker extends Scheduler.Worker {
    private final long initTime = System.currentTimeMillis() / 50;
    private final Map<Long, WorkGroup> workGroups = new ConcurrentHashMap<>();
    private boolean disposed;

    abstract BukkitTask later(Runnable r, long tickDelay);
    abstract BukkitTask asap(Runnable r);
    abstract boolean canRunImmediately();

    @Override
    public Disposable schedule(@NonNull Runnable run, long delay, @NonNull TimeUnit unit) {
        if (delay <= 0) {
            if (canRunImmediately()) {
                run.run();
                return Disposables.disposed();
            }

            return new SingleDisposableTask(run);
        }

        return getGroup(delay, unit).add(new QueuedWork(run));
    }

    @Synchronized
    private WorkGroup getGroup(Long delay, TimeUnit unit) {
        return workGroups.computeIfAbsent(toAbsoluteTicks(delay, unit), key -> new WorkGroup(unit.toMillis(delay) / 50, key));
    }

    @Synchronized
    private void clearGroup(Long delay) {
        workGroups.remove(delay);
    }

    private long toAbsoluteTicks(long fromNow, TimeUnit unit) {
        return ((System.currentTimeMillis() + unit.toMillis(fromNow)) / 50) - initTime;
    }

    @Override
    @Synchronized
    public void dispose() {
        workGroups.forEach((ms, group) -> group.dispose());
        workGroups.clear();
        disposed = true;
    }

    @Override
    @Synchronized
    public boolean isDisposed() {
        return disposed;
    }

    protected final class SingleDisposableTask implements Runnable, Disposable {
        private final BukkitTask scheduled;
        private final Runnable runnable;
        private boolean finished;

        private SingleDisposableTask(Runnable runnable) {
            this.runnable = runnable;
            this.scheduled = asap(this);
        }

        @Override
        @Synchronized
        public void dispose() {
            if (isDisposed())
                return;

            scheduled.cancel();
        }

        @Override
        @Synchronized
        public boolean isDisposed() {
            return finished || scheduled.isCancelled();
        }

        @Override
        @Synchronized
        public void run() {
            runnable.run();
            finished = true;
        }
    }

    protected final class WorkGroup implements Runnable, Disposable {
        private final List<QueuedWork> work = new ArrayList<>();
        private final long key;
        private BukkitTask scheduled;
        private boolean finished;

        private WorkGroup(long delayTicks, long key) {
            scheduled = later(this, delayTicks);
            this.key = key;
        }

        @Synchronized
        private boolean remove(QueuedWork work) {
            boolean removed = !finished && this.work.remove(work);
            if (!removed)
                return false;

            if (this.work.size() == 0)
                clearGroup(key);

            return true;
        }

        @Synchronized
        private Disposable add(QueuedWork work) {
            if (finished)
                throw new IllegalStateException("cannot add more work for items executed in the past");

            this.work.add(work);
            work.setParent(this);
            return work;
        }

        @Override
        @Synchronized
        public void run() {
            if (finished)
                throw new IllegalStateException("worker run more than one time");

            finished = true;

            for (QueuedWork queuedWork : work)
                queuedWork.run();

            work.clear();

            clearGroup(key);
        }

        @Override
        @Synchronized
        public void dispose() {
            if (finished)
                return;

            if (scheduled == null)
                return;

            if (scheduled.isCancelled())
                return;

            scheduled.cancel();
            work.forEach(QueuedWork::dispose);
            work.clear();
            finished = true;

            clearGroup(key);
        }

        @Override
        public boolean isDisposed() {
            return scheduled == null || scheduled.isCancelled() || finished;
        }
    }

    @RequiredArgsConstructor
    protected final class QueuedWork implements Disposable, Runnable {
        private final Runnable r;
        @Setter private WorkGroup parent;
        private boolean disposed;

        @Override
        @Synchronized
        public void dispose() {
            if (!parent.remove(this))
                throw new IllegalStateException("could not cancel a unit of work");

            disposed = true;
        }

        @Override
        @Synchronized
        public boolean isDisposed() {
            return disposed;
        }

        @Override
        @Synchronized
        public void run() {
            if (disposed)
                return;

            r.run();
            disposed = true;
        }
    }
}
