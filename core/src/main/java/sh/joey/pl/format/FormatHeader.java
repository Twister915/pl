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

package sh.joey.pl.format;

import lombok.AccessLevel;
import lombok.Getter;

import static sh.joey.pl.format.Formatter.SCREEN_WIDTH;

public enum FormatHeader {
    DASHES($("-", SCREEN_WIDTH)),
    DECORATED_DASHES("*" + $("-", SCREEN_WIDTH - 2) + "*");

    @Getter(AccessLevel.PACKAGE) private final String line;

    FormatHeader(String line) {
        this.line = line;
    }


    private static String $(String c, int count) {
        if (count == 0)
            return "";

        return c + $(c, count - 1);
    }
}
