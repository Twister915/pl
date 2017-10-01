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

package sh.joey.example;

import sh.joey.pl.Cmd;
import sh.joey.pl.command.JPlayerCmd;
import sh.joey.pl.command.exception.CommandException;
import sh.joey.pl.format.Formatter;

import javax.inject.Inject;

@Cmd(value = "test",
     permission = "example.test",
     aliases = {"t", "sup"},
     description = "Used to test the command system")
public final class TestCmd extends JPlayerCmd {
    @Inject private Formatter formatter;

    @Override
    protected void run() throws CommandException {
        formatter.at("test").with("player", sender().getName()).sendTo(sender());
    }
}
