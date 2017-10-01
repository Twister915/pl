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

package sh.joey.pl.parse;

import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import lombok.AccessLevel;
import lombok.Getter;
import sh.joey.pl.rx.scheduler.Kind;
import sh.joey.pl.rx.scheduler.ServerScheduler;

import javax.inject.Inject;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.Callable;

public abstract class Parser {
    @Inject @ServerScheduler(Kind.SYNC)  @Getter(AccessLevel.PROTECTED)
    private Scheduler returnScheduler;

    public <T> Single<T> parse(ByteSource source, Class<T> type) {
        return ioAsync(() -> source.read()).map(String::new).flatMap(data -> parse(data, type));
    }

    public Completable write(Object o, ByteSink sink) {
        return write(o).map(String::getBytes).flatMapCompletable(bytes -> ioAsync(() -> sink.write(bytes)));
    }

    public <T> Single<T> parse(InputStream stream, Class<T> type) {
        return ioAsync(() -> ByteStreams.toByteArray(stream)).map(String::new).flatMap(data -> parse(data, type));
    }

    public Completable write(Object o, OutputStream stream) {
        return write(o).map(String::getBytes).flatMapCompletable(bytes -> ioAsync(() -> stream.write(bytes)));
    }

    public <T> Single<T> parse(File file, Class<T> type) {
        return parse(Files.asByteSource(file), type);
    }

    public void write(Object o, File file, FileWriteMode... modes) {
        write(o, Files.asByteSink(file, modes));
    }

    protected final <T> Single<T> ioAsync(Callable<T> callable) {
        return Single.fromCallable(callable)
                     .subscribeOn(Schedulers.io())
                     .observeOn(returnScheduler);
    }

    protected final Completable ioAsync(Action action) {
        return Completable.fromAction(action)
                          .subscribeOn(Schedulers.io())
                          .observeOn(returnScheduler);
    }

    public <T> Single<T> parse(String data, Class<T> type)  {
        return Single.fromCallable(() -> parseNow(data, type));
    }

    public Single<String> write(Object o) {
        return Single.fromCallable(() -> writeNow(o));
    }

    protected <T> T parseNow(String data, Class<T> type) {
        throw new UnsupportedOperationException("unimplemented");
    }

    protected String writeNow(Object o) {
        throw new UnsupportedOperationException("unimplemented");
    }
}
