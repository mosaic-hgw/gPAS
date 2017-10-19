package org.emau.icmvc.ganimed.ttp.psn.generator;

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


import java.util.Map;

import org.emau.icmvc.ganimed.ttp.psn.exceptions.CharNotInAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidPSNException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.MessageTooLargeException;

/**
 * stores the alphabet and provides methods to generate and validate check digits
 * 
 * @author geidell
 * 
 */
public abstract class CheckDigits {

	private final Alphabet alphabet;
	private final Map<GeneratorProperties, String> properties;

	public CheckDigits(Alphabet alphabet, Map<GeneratorProperties, String> properties) {
		super();
		this.alphabet = alphabet;
		this.properties = properties;
	}

	/**
	 * 
	 * @param message
	 *            string for which check digits should be generated
	 * @return check digits
	 * @throws CharNotInAlphabetException
	 * @throws MessageTooLargeException
	 */
	public abstract String generateCheckDigits(String message) throws CharNotInAlphabetException, MessageTooLargeException;

	/**
	 * 
	 * @param value
	 *            string (including check digits) to be checked
	 * @param messageLength
	 *            length of [value] without check digits
	 * @throws CharNotInAlphabetException
	 * @throws InvalidPSNException
	 */
	public abstract void check(String value, int messageLength) throws CharNotInAlphabetException, InvalidPSNException;

	/**
	 * 
	 * @return the alphabet for this instance
	 */
	public Alphabet getAlphabet() {
		return alphabet;
	}

	/**
	 * 
	 * @param key
	 * @return the property for this check digit instance with the given key
	 */
	public String getProperty(GeneratorProperties key) {
		if (properties == null) {
			return "";
		}
		String result = properties.get(key);
		return result != null ? result : "";
	}
}
