package org.emau.icmvc.ganimed.ttp.psn;

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

import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidCheckDigitClassException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidGeneratorException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.psn.generator.Generator;

public interface DomainManagerLocal extends DomainManager {

	/**
	 * returns the generator for the given domain; tries to instantiate it with properties from the psn_projects table if it's not found within cache
	 * 
	 * @param domain
	 * @throws InvalidAlphabetException
	 *             if the given alphabet name (entry for the given domain within the psn_projects table) is not a valid alphabet or the length of the alphabet is not valid for the given check digit
	 *             class
	 * @throws InvalidCheckDigitClassException
	 *             if the given check digit class name (entry for the given domain within the psn_projects table) is not a valid check digit class
	 * @throws InvalidGeneratorException
	 *             if the generator can't be instantiated
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	public Generator getGeneratorFor(String domain) throws InvalidAlphabetException, InvalidCheckDigitClassException, InvalidGeneratorException,
			UnknownDomainException;
}
