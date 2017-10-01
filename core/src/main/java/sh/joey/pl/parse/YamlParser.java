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

import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import org.yaml.snakeyaml.Yaml;
import sh.joey.pl.util.YamlUtil;

final class YamlParser extends Parser {
    private final Yaml yaml = YamlUtil.newYaml();
    private final Scheduler.Worker serializedWorker = Schedulers.io().createWorker();

    @Override
    public <T> Single<T> parse(String data, Class<T> type) {
        return Single.create(s ->
             s.setDisposable(serializedWorker.schedule(() ->
                s.onSuccess(yaml.loadAs(data, type)))));
    }

    @Override
    public Single<String> write(Object o) {
        return Single.create(s ->
            s.setDisposable(serializedWorker.schedule(() ->
                s.onSuccess(yaml.dump(o)))));
    }
}
