package org.emau.icmvc.ganimed.ttp.psn.generator;
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

import java.io.Serial;

import org.emau.icmvc.ganimed.ttp.psn.config.DomainConfig;
import org.emau.icmvc.ganimed.ttp.psn.enums.GeneratorAlphabetRestriction;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.CharNotInAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidAlphabetException;

public class Damm extends CheckDigit
{
	@Serial
	private static final long serialVersionUID = -6390976252273971830L;

	private static final int[][] op = {
			{ 0, 3, 1, 7, 5, 9, 8, 6, 4, 2 }, //
			{ 7, 0, 9, 2, 1, 5, 4, 8, 6, 3 }, //
			{ 4, 2, 0, 6, 8, 7, 1, 3, 5, 9 }, //
			{ 1, 7, 5, 0, 9, 8, 3, 4, 2, 6 }, //
			{ 6, 1, 2, 3, 0, 4, 5, 9, 7, 8 }, //
			{ 3, 6, 7, 4, 2, 0, 9, 5, 8, 1 }, //
			{ 5, 8, 6, 9, 7, 2, 0, 1, 3, 4 }, //
			{ 8, 9, 4, 5, 3, 6, 2, 0, 1, 7 }, //
			{ 9, 4, 3, 8, 6, 1, 7, 2, 0, 5 }, //
			{ 2, 5, 8, 1, 4, 3, 6, 7, 9, 0 }
	};

	public Damm()
	{
	}

	public Damm(Alphabet alphabet, DomainConfig config) throws InvalidAlphabetException
	{
		super(alphabet);
		getAlphabetRestriction().validateAlphabet(alphabet);
		logger.info("initialised {}", this);
	}

	@Override
	public GeneratorAlphabetRestriction getAlphabetRestriction()
	{
		return GeneratorAlphabetRestriction.CONST_10;
	}

	protected char calculateCheckDigit(String message) throws CharNotInAlphabetException
	{
		int check = 0;
		for (int i = message.length(); i > 0; i--)
		{
			check = op[check][getAlphabet().getPosForSymbol(message.charAt(i - 1))];
		}
		return getAlphabet().getSymbol(check);
	}
}
