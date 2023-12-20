package org.emau.icmvc.ganimed.ttp.psn.generator;

/*-
 * ###license-information-start###
 * gPAS - a Generic Pseudonym Administration Service
 * __
 * Copyright (C) 2013 - 2023 Independent Trusted Third Party of the University Medicine Greifswald
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

import org.emau.icmvc.ganimed.ttp.psn.config.DomainConfig;
import org.emau.icmvc.ganimed.ttp.psn.enums.GeneratorAlphabetRestriction;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.CharNotInAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidPSNException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParameterException;

public abstract class CheckDigits
{
	private final Alphabet alphabet;

	public CheckDigits()
	{
		super();
		this.alphabet = null;
	}

	public CheckDigits(Alphabet alphabet, DomainConfig config)
	{
		super();
		this.alphabet = alphabet;
	}

	/**
	 * @param message
	 *            string for which check digits should be generated
	 * @return check digits
	 * @throws CharNotInAlphabetException
	 * @throws MessageTooLargeException
	 * @throws InvalidParameterException
	 */
	public abstract String generateCheckDigits(String message) throws CharNotInAlphabetException, InvalidParameterException;

	/**
	 * @param value
	 *            string (including check digits) to be checked
	 * @param messageLength
	 *            length of [value] without check digits
	 * @throws CharNotInAlphabetException
	 * @throws InvalidPSNException
	 */
	public abstract void check(String value, int messageLength) throws CharNotInAlphabetException, InvalidPSNException;

	/**
	 * @return the alphabet for this instance
	 */
	public Alphabet getAlphabet()
	{
		return alphabet;
	}

	/**
	 * gives the restriction for the number of chars within the alphabet
	 *
	 * @return
	 */
	public abstract GeneratorAlphabetRestriction getAlphabetRestriction();
}
