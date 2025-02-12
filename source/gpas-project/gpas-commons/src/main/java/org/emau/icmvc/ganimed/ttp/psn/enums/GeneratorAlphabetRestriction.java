package org.emau.icmvc.ganimed.ttp.psn.enums;

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
import org.emau.icmvc.ganimed.ttp.psn.utils.MathUtil;

public enum GeneratorAlphabetRestriction
{
	NONE(0, "there is no restriction on valid alphabets"),
	CONST_10(10, "the length of the alphabet must be 10"),
	CONST_32(32, "the length of the alphabet must be 32"),
	PRIME(0, "the length of the alphabet must be a prime number"),
	PRIME_POWER(0, "the length of the alphabet must be a power of a prime number");

	private final int validLength;
	private final String description;

	GeneratorAlphabetRestriction(int validLength, String description)
	{
		this.validLength = validLength;
		this.description = description;
	}

	/**
	 * {@return true if the specified alphabet fulfils this restriction}
	 * @param alphabet the alphabet to test
	 */
	public boolean isValid(Alphabet alphabet)
	{
		if (alphabet == null)
		{
			return false;
		}
		int length = alphabet.length();
		return switch (this)
		{
			case NONE -> true;
			case CONST_10, CONST_32 -> length == validLength;
			case PRIME -> MathUtil.isPrime(length);
			case PRIME_POWER -> MathUtil.isPrimePower(length);
		};
	}

	public void validateAlphabet(Alphabet alphabet) throws InvalidAlphabetException
	{
		if (!isValid(alphabet))
		{
			throw new InvalidAlphabetException(getDescription() + " (alphabet: " + alphabet + ")");
		}
	}

	/**
	 * {@return the length for valid alphabets if this restriction describes a constant length, otherwise 0]
	 */
	public int getValidLength()
	{
		return validLength;
	}

	/**
	 * {@return a description for this restriction}
	 */
	public String getDescription()
	{
		return description;
	}
}