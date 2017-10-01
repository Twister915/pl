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

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public final class RxAccessorModule extends AbstractModule {
    @Override
    protected void configure() {
        bindAccessor(Kind.IO, IOAccessorFactory.class);
        bindAccessor(Kind.COMPUTATION, ComputationAccessorFactory.class);
        bindAccessor(Kind.SERVER_ASYNC, AsyncAccessorFactory.class);
    }

    private void bindAccessor(Kind kind, Class<? extends AsyncAccessors> accessor) {
        bind(AsyncAccessors.class).annotatedWith(AccessorKinds.kind(kind)).to(accessor).in(Singleton.class);
    }
}
