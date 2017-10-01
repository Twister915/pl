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

package sh.joey.pl.util;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.representer.Representer;

public class YamlUtil {
    private YamlUtil() {}

    public static Yaml newYaml() {
        Representer representer = new Representer();
        PropertyUtils propertyUtils = representer.getPropertyUtils();
        propertyUtils.setSkipMissingProperties(true);
        propertyUtils.setBeanAccess(BeanAccess.FIELD);
        propertyUtils.setAllowReadOnlyProperties(true);

        return new Yaml(representer);
    }
}
