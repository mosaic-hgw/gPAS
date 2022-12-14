package org.emau.icmvc.ganimed.ttp.psn.generator;

/*-
 * ###license-information-start###
 * gPAS - a Generic Pseudonym Administration Service
 * __
 * Copyright (C) 2013 - 2022 Independent Trusted Third Party of the University Medicine Greifswald
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.psn.config.DomainConfig;
import org.emau.icmvc.ganimed.ttp.psn.enums.GeneratorAlphabetRestriction;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.CharNotInAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidPSNException;

public class NoCheckDigits extends CheckDigits
{
	private static final Logger logger = LogManager.getLogger(NoCheckDigits.class);

	public NoCheckDigits()
	{
		super();
	}

	public NoCheckDigits(Alphabet alphabet, DomainConfig config) throws InvalidAlphabetException
	{
		super(alphabet, config);
		logger.info("initialised NoCheckDigits generator with the alphabet: " + alphabet);
	}

	@Override
	public String generateCheckDigits(String message) throws CharNotInAlphabetException
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("add check digigts for message '" + message + "'");
		}
		// test fuer CharNotInAlphabetException
		for (int i = 0; i < message.length(); i++)
		{
			getAlphabet().getPosForSymbol(message.charAt(i));
		}
		return "";
	}

	@Override
	public void check(String value, int messageLength) throws CharNotInAlphabetException, InvalidPSNException
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("check message '" + value + "'");
		}
		if (value.length() != messageLength)
		{
			String message = "invalid value '" + value + "' - it should have a length of " + messageLength;
			logger.info(message);
			throw new InvalidPSNException(message);
		}
		for (int i = 0; i < value.length(); i++)
		{
			getAlphabet().getPosForSymbol(value.charAt(i));
		}
	}

	@Override
	public GeneratorAlphabetRestriction getAlphabetRestriction()
	{
		return GeneratorAlphabetRestriction.NONE;
	}
}
