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

package sh.joey.pl.sql;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.throwingproviders.ThrowingProviderBinder;

import javax.sql.DataSource;

public final class SqlModule extends AbstractModule {
    @Override
    protected void configure() {
        requireBinding(SqlConfiguration.class);

        ThrowingProviderBinder
            .create(binder())
            .bind(DataSourceProvider.class, DataSource.class)
            .to(ConfiguredDataSourceProvider.class)
            .in(Singleton.class);
    }
}
