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

import com.google.common.base.Joiner;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FormatBuilder {
    private final String prefix;
    private final List<String> formats;
    private final Map<String, String> inputs = new HashMap<>();
    private boolean linePrefix, coloredInputs;
    private FormatHeader header, trailer;

    FormatBuilder(String prefix, List<String> formats) {
        this.prefix = prefix;
        this.formats = formats;
    }

    public FormatBuilder with(String key, Object value) {
        inputs.put(key, value.toString());
        return this;
    }

    public FormatBuilder with(String key, String format, Object... args) {
        return with(key, String.format(format, args));
    }

    public FormatBuilder withLinePrefix(boolean prefix) {
        this.linePrefix = prefix;
        return this;
    }

    public FormatBuilder withHeader(FormatHeader header) {
        this.header = header;
        return this;
    }

    public FormatBuilder withTrailer(FormatHeader header) {
        this.trailer = header;
        return this;
    }

    public FormatBuilder decorate(FormatHeader header) {
        return withHeader(header).withTrailer(header);
    }

    public FormatBuilder withPrefix(boolean prefix) {
        return withLinePrefix(prefix);
    }

    public FormatBuilder withColoredInputs(boolean coloredInputs) {
        this.coloredInputs = coloredInputs;
        return this;
    }

    public String get() {
        if (formats.isEmpty())
            throw new IllegalArgumentException("could not find format...");

        String formatString = Joiner.on('\n').join(formats);
        String s = ChatColor.translateAlternateColorCodes('&', formatString);

        for (Map.Entry<String, String> stringStringEntry : inputs.entrySet()) {
            String value = stringStringEntry.getValue();
            if (coloredInputs)
                value = ChatColor.translateAlternateColorCodes('&', value);

            s = s.replaceAll(String.format("\\{\\{%s\\}\\}", stringStringEntry.getKey()), value);
        }

        if (linePrefix)
            return prefix;

        if (header != null)
            s = header.getLine() + "\n" + s;

        if (trailer != null)
            s += "\n" + trailer.getLine();

        return s;
    }

    @Override
    public String toString() {
        return get();
    }

    public void sendTo(CommandSender player) {
        player.sendMessage(get());
    }
}
