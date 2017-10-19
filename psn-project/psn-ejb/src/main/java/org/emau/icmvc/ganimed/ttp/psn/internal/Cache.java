package org.emau.icmvc.ganimed.ttp.psn.internal;

/*
 * ###license-information-start###
 * gPAS - a Generic Pseudonym Administration Service
 * __
 * Copyright (C) 2013 - 2017 The MOSAIC Project - Institut fuer Community Medicine der
 * 							Universitaetsmedizin Greifswald - mosaic-projekt@uni-greifswald.de
 * 							concept and implementation
 * 							l. geidel
 * 							web client
 * 							g. weiher
 * 							a. blumentritt
 * 							please cite our publications
 * 							http://dx.doi.org/10.3414/ME14-01-0133
 * 							http://dx.doi.org/10.1186/s12967-015-0545-6
 * __
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ###license-information-end###
 */


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ejb.Singleton;

import org.emau.icmvc.ganimed.ttp.psn.generator.Generator;
import org.emau.icmvc.ganimed.ttp.psn.generator.GeneratorProperties;

/**
 * cache for expensive objects
 * 
 * @author geidell
 *
 */
@Singleton
public class Cache {

	private static final List<String> possibleGeneratorProperties = new ArrayList<String>();
	private static final HashMap<String, Generator> generatorCache = new HashMap<String, Generator>();

	static {
		for(GeneratorProperties property : GeneratorProperties.values()) {
			possibleGeneratorProperties.add(property.toString());
		}
	}

	public static List<String> getPossibleGeneratorProperties() {
		return possibleGeneratorProperties;
	}

	public static Generator getGenerator(String domain) {
		return generatorCache.get(domain);
	}

	public static Generator cacheGenerator(String domain, Generator generator) {
		return generatorCache.put(domain, generator);
	}

	public static Generator removeGenerator(String domain) {
		return generatorCache.remove(domain);
	}
}
