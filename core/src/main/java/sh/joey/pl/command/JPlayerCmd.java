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

package sh.joey.pl.command;

import org.bukkit.entity.Player;

import javax.inject.Inject;

public abstract class JPlayerCmd extends JCmd {
    private Player sender;
    private String[] args;

    @Inject private void setup(Player sender, @Args String[] args) {
        this.sender = sender;
        this.args = args;
    }

    protected final Player sender() {
        return sender;
    }

    protected final String[] args() {
        return args;
    }
}
