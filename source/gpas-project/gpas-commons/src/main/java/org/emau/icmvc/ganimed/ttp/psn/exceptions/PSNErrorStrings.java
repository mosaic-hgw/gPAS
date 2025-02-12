package org.emau.icmvc.ganimed.ttp.psn.exceptions;

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

public class PSNErrorStrings
{
	public static final String INVALID_PSN = "*** INVALID PSEUDONYM ***";
	public static final String INVALID_VALUE = "*** INVALID VALUE ***";
	public static final String PSN_NOT_FOUND = "*** PSEUDONYM NOT FOUND ***";
	public static final String VALUE_NOT_FOUND = "*** VALUE NOT FOUND ***";
	public static final String VALUE_IS_ANONYMISED = "*** VALUE IS ANONYMISED ***";
	public static final String MULTIPLE_PSNS = "*** MULTIPLE_PSNS ***";
	public static final String PAIR_VALUE_INVALID = "VALUE_INVALID";
	public static final String PAIR_PSEUDONYM_INVALID = "PSEUDONYM_INVALID";
	public static final String PAIR_DIFFERENT_PSEUDONYM_FOR_VALUE_EXISTS = "DIFFERENT_PSEUDONYM_FOR_VALUE_EXISTS";
	public static final String PAIR_DIFFERENT_VALUE_FOR_PSEUDONYM_EXISTS = "DIFFERENT_VALUE_FOR_PSEUDONYM_EXISTS";

	/**
	 * Returns true if a given string is a PSNErrorString.
	 *
	 * @param text
	 * 		the string to test
	 * @return true if the given text is a PSNErrorString
	 */
	public static boolean isPSNErrorString(final String text)
	{
		return switch (text)
		{
			case INVALID_PSN,
				 INVALID_VALUE,
				 PSN_NOT_FOUND,
				 VALUE_NOT_FOUND,
				 VALUE_IS_ANONYMISED,
				 PAIR_VALUE_INVALID,
				 PAIR_PSEUDONYM_INVALID,
				 PAIR_DIFFERENT_PSEUDONYM_FOR_VALUE_EXISTS,
				 PAIR_DIFFERENT_VALUE_FOR_PSEUDONYM_EXISTS,
				 MULTIPLE_PSNS -> true;
			default -> false;
		};
	}

	/**
	 * Returns true if a given string is a PSNErrorString describing that a PSN or a value was not found.
	 *
	 * @param text
	 * 		the string to test
	 * @return true if a given string is a PSNErrorString describing that a PSN or a value was not found.
	 */
	public static boolean isNotFoundErrorString(final String text)
	{
		return text != null && text.endsWith("NOT FOUND ***");
	}

	/**
	 * Returns true if a given string is a PSNErrorString describing that a PSN or a value was invalid.
	 *
	 * @param text
	 * 		the string to test
	 * @return true if a given string is a PSNErrorString describing that a PSN or a value was invalid.
	 */
	public static boolean isInvalidErrorString(final String text)
	{
		return text != null && text.startsWith("*** INVALID");
	}
}
