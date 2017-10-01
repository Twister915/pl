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

public interface SqlConfiguration {
    String getJdbcUrl();
    String getUsername();
    String getPassword();

    default int getMinimumIdle() {
        return 1;
    }

    default int getMaximumPoolSize() {
        return 8;
    }

    default int getIdleTimeout() {
        return 30000;
    }

    default int getConnectionTimeout() {
        return 5000;
    }

    default String getConnectionTestQuery() {
        return "SELECT 1";
    }
}