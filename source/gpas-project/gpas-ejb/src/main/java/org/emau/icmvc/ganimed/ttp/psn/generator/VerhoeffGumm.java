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

public class VerhoeffGumm extends CheckDigit
{
	@Serial
	private static final long serialVersionUID = 3015393396612287086L;

	protected static final int[][] OP = {
			{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }, //
			{ 1, 2, 3, 4, 0, 6, 7, 8, 9, 5 }, //
			{ 2, 3, 4, 0, 1, 7, 8, 9, 5, 6 }, //
			{ 3, 4, 0, 1, 2, 8, 9, 5, 6, 7 }, //
			{ 4, 0, 1, 2, 3, 9, 5, 6, 7, 8 }, //
			{ 5, 9, 8, 7, 6, 0, 4, 3, 2, 1 }, //
			{ 6, 5, 9, 8, 7, 1, 0, 4, 3, 2 }, //
			{ 7, 6, 5, 9, 8, 2, 1, 0, 4, 3 }, //
			{ 8, 7, 6, 5, 9, 3, 2, 1, 0, 4 }, //
			{ 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 }
	};

	protected static final int[][] F = new int[][] {
			{ 1, 0, 3, 2, 4, 5, 6, 7, 8, 9 }, //
			{ 0, 4, 3, 2, 1, 8, 9, 5, 6, 7 }, //
			{ 0, 1, 2, 3, 4, 6, 7, 8, 9, 5 }, //
			{ 0, 4, 3, 2, 1, 9, 5, 6, 7, 8 }, //
			{ 0, 1, 2, 3, 4, 7, 8, 9, 5, 6 }, //
			{ 0, 4, 3, 2, 1, 5, 6, 7, 8, 9 }, //
			{ 1, 0, 3, 2, 4, 8, 9, 5, 6, 7 }, //
			{ 4, 0, 2, 3, 1, 6, 7, 8, 9, 5 }, //
			{ 1, 0, 3, 2, 4, 9, 5, 6, 7, 8 }, //
			{ 4, 0, 2, 3, 1, 7, 8, 9, 5, 6 }
	};

	protected static final int[] INV = new int[] { 0, 4, 3, 2, 1, 5, 6, 7, 8, 9 };

	public VerhoeffGumm()
	{
	}

	public VerhoeffGumm(Alphabet alphabet, DomainConfig config) throws InvalidAlphabetException
	{
		super(alphabet);
		getAlphabetRestriction().validateAlphabet(alphabet);
		logger.info("initialised {}", this);
	}

	/**
	 * With this algorithm, all individual typing errors and all swaps of neighbouring characters should be detected as errors for any PSN.
	 * However, as can be seen in the JUnit test, all swaps are only recognised for approx. 77% of the PSNs.
	 * This is due to a faulty lookup table {@link #F} (the permutation function TAU original algorithm).
	 * It must therefore no longer be used, at least for new domains (creating new domains with deprecated check-digits-algorithms is not allowed).
	 * Instead, you can use the correctly implemented algorithm {@link Gumm}.
	 *
	 * @return true
	 */
	@Override
	public boolean isDeprecated()
	{
		return true;
	}

	@Override
	public GeneratorAlphabetRestriction getAlphabetRestriction()
	{
		return GeneratorAlphabetRestriction.CONST_10;
	}

	protected char calculateCheckDigit(String message) throws CharNotInAlphabetException
	{
		int check = 0;
		for (int k = 1; k <= message.length(); k++)
		{
			check = OP[F[k % 10][getAlphabet().getPosForSymbol(message.charAt(message.length() - k))]][check];
		}
		return getAlphabet().getSymbol(INV[check]);
	}
}
