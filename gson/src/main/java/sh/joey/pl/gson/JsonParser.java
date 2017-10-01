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

package sh.joey.pl.gson;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.inject.Inject;
import sh.joey.pl.parse.Parser;

public final class JsonParser extends Parser {
    private final Gson gson;

    @Inject
    JsonParser(Gson gson) {
        this.gson = gson;
    }

    @Override
    public <T> T parseNow(String data, Class<T> type) {
        return gson.fromJson(data, type);
    }

    @Override
    public String writeNow(Object o) {
        return gson.toJson(o);
    }

    public JsonElement toJsonTree(Object o) {
        return gson.toJsonTree(o);
    }
}
