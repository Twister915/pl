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

import com.google.inject.Inject;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

final class ConfiguredDataSourceProvider implements DataSourceProvider<DataSource> {
    private final SqlConfiguration baseConfig;

    @Inject
    ConfiguredDataSourceProvider(SqlConfiguration baseConfig) {
        this.baseConfig = baseConfig;
    }

    @Override
    public DataSource get() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(baseConfig.getJdbcUrl());
        config.setUsername(baseConfig.getUsername());
        config.setPassword(baseConfig.getPassword());
        config.setMinimumIdle(baseConfig.getMinimumIdle());
        config.setMaximumPoolSize(baseConfig.getMaximumPoolSize());
        config.setConnectionTestQuery(baseConfig.getConnectionTestQuery());
        config.setIdleTimeout(baseConfig.getIdleTimeout());
        config.setConnectionTimeout(baseConfig.getConnectionTimeout());

        return new HikariDataSource(config);
    }
}
