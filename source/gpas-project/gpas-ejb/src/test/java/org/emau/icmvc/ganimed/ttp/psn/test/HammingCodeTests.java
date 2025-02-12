package org.emau.icmvc.ganimed.ttp.psn.test;

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

import org.emau.icmvc.ganimed.ttp.psn.alphabets.Symbol32;
import org.emau.icmvc.ganimed.ttp.psn.generator.HammingCode;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HammingCodeTests extends CheckDigitTests
{
	HammingCodeTests() throws Exception
	{
		alphabet = new Symbol32();
		checkDigits = new HammingCode(alphabet, config);
	}

	@Test
	void testOneExampleForEveryLength() throws Exception
	{
		checkOneExampleForEveryLength(false);
	}

	@Test
	@Disabled("only for manual testing, too slow!")
	void testOneExampleForEveryLengthWithAll2DigitErrors() throws Exception
	{
		checkOneExampleForEveryLength(true);
	}

	@Test
	void testEffectiveMaxMessageLength()
	{
		assertEquals(120, checkDigits.getEffectiveMaxMessageLength());

		assertEquals(120, checkDigits.getEffectiveMaxMessageLength(128));
		assertEquals(120, checkDigits.getEffectiveMaxMessageLength(127));
		assertEquals(119, checkDigits.getEffectiveMaxMessageLength(126));

		assertEquals(57, checkDigits.getEffectiveMaxMessageLength(64));
		assertEquals(57, checkDigits.getEffectiveMaxMessageLength(63));
		assertEquals(56, checkDigits.getEffectiveMaxMessageLength(62));

		assertEquals(26, checkDigits.getEffectiveMaxMessageLength(32));
		assertEquals(26, checkDigits.getEffectiveMaxMessageLength(31));
		assertEquals(25, checkDigits.getEffectiveMaxMessageLength(30));

		assertEquals(11, checkDigits.getEffectiveMaxMessageLength(16));
		assertEquals(11, checkDigits.getEffectiveMaxMessageLength(15));
		assertEquals(10, checkDigits.getEffectiveMaxMessageLength(14));

		assertEquals(4, checkDigits.getEffectiveMaxMessageLength(8));
		assertEquals(4, checkDigits.getEffectiveMaxMessageLength(7));
		assertEquals(3, checkDigits.getEffectiveMaxMessageLength(6));

		assertEquals(1, checkDigits.getEffectiveMaxMessageLength(4));
		assertEquals(1, checkDigits.getEffectiveMaxMessageLength(3));
		assertEquals(0, checkDigits.getEffectiveMaxMessageLength(2));
	}
}
