package org.emau.icmvc.ganimed.ttp.psn.alphabets;

/*-
 * ###license-information-start###
 * gPAS - a Generic Pseudonym Administration Service
 * __
 * Copyright (C) 2013 - 2024 Independent Trusted Third Party of the University Medicine Greifswald
 * 							kontakt-ths@uni-greifswald.de
 * 							concept and implementation
 * 							l.geidel
 * 							web client
 * 							a.blumentritt
 * 							docker
 * 							r.schuldt
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
import org.emau.icmvc.ganimed.ttp.psn.generator.Alphabet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Alphabets
{
	public static final String CUSTOM = "CUSTOM";
	public static final String HEX = "org.emau.icmvc.ganimed.ttp.psn.alphabets.Hex";
	public static final String NUMBERS = "org.emau.icmvc.ganimed.ttp.psn.alphabets.Numbers";
	public static final String NUMBERS_WITHOUT_ZERO = "org.emau.icmvc.ganimed.ttp.psn.alphabets.NumbersWithoutZero";
	public static final String NUMBERS_X = "org.emau.icmvc.ganimed.ttp.psn.alphabets.NumbersX";
	public static final String SYMBOL_31 = "org.emau.icmvc.ganimed.ttp.psn.alphabets.Symbol31";
	public static final String SYMBOL_32 = "org.emau.icmvc.ganimed.ttp.psn.alphabets.Symbol32";

	private static final Logger LOGGER = LoggerFactory.getLogger(Alphabets.class);

	private Alphabets()
	{
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Creates an alphabet from an alphabet string, which must be either the name of a subclass of  {@link Alphabet},
	 * a comma-separated list of characters, or a single character.
	 *
	 * @param alphabetString the alphabet string
	 * @return an appropriate alphabet instance for the alphabet string
	 * @throws InvalidAlphabetException for an invalid alphabet string
	 */
	public static Alphabet createAlphabet(String alphabetString) throws InvalidAlphabetException
	{
		Alphabet result;
		if (alphabetString == null)
		{
			throw new InvalidAlphabetException("alphabet is null");
		}
		if (alphabetString.contains(",") || alphabetString.length() == 1)
		{
			try
			{
				LOGGER.debug("creating generic alphabet with following chars: {}", alphabetString);
				result = new GenericAlphabet(alphabetString);
			}
			catch (Exception e)
			{
				String message = "exception while creating alphabet '" + alphabetString + "' - " + e
						+ (e.getCause() != null ? "(" + e.getCause() + ")" : "");
				throw new InvalidAlphabetException(message, e);
			}
		}
		else
		{
			Class<? extends Alphabet> alphabetClass;
			try
			{
				LOGGER.debug("creating alphabet {}", alphabetString);
				Class<?> temp = Class.forName(alphabetString);
				alphabetClass = temp.asSubclass(Alphabet.class);
				result = alphabetClass.getDeclaredConstructor().newInstance();
			}
			catch (Exception e)
			{
				String message = "exception while creating alphabet class '" + alphabetString + "' - " + e
						+ (e.getCause() != null ? "(" + e.getCause() + ")" : "");
				throw new InvalidAlphabetException(message, e);
			}
		}
		return result;
	}

	public static boolean isCustomAlphabet(String alphabetString)
	{
		return alphabetString.contains(",") || alphabetString.length() == 1;

	}

	public static int utf8mb4ByteCount(String s)
	{
		int count = 0;
		if (s != null)
		{
			for (char c : s.toCharArray())
			{
				count += utf8mb4ByteCount(c);
			}
		}
		return count;
	}

	public static int utf8mb4ByteCount(char c) {
		if (Character.isHighSurrogate(c) || Character.isLowSurrogate(c))
		{
			// Surrogat-Paare benötigen 4 Bytes in UTF-8
			return 4;
		}
		else if (c <= 0x7F) {
			// Standard-ASCII-Zeichen benötigen 1 Byte
			return 1;
		}
		else if (c <= 0x7FF) {
			// Zeichen im Bereich von 0x80 bis 0x7FF benötigen 2 Bytes
			return 2;
		}
		else {
			// Zeichen im Bereich von 0x800 bis 0xFFFF benötigen 3 Bytes
			return 3;
		}
	}
}
