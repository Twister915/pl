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

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Singleton;

public final class ParserModule extends AbstractModule {
    @Override
    protected void configure() {
        bindParser(binder(), "yaml", YamlParser.class);
    }

    public static void bindParser(Binder binder, String kind, Class<? extends Parser> parser) {
        binder.bind(Parser.class).annotatedWith(ParserKinds.parser(kind)).to(parser).in(Singleton.class);
    }
}
