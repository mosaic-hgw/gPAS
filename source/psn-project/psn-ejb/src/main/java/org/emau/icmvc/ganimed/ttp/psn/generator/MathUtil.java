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

public class MathUtil
{
	/**
	 * returns an array with all prime factors of the given number
	 * <p>
	 * if factors appear more then once, they are returned ordered in their multiplicity, e.g. 180
	 * -> [2,2,3,3,5]
	 *
	 * @param number
	 * @return
	 */
	public static int[] getPrimeFactors(int number)
	{
		if (number < 2)
		{
			return new int[0];
		}
		int maxFactors = getMaxPrimeFactorsFor(number);
		int highestPossibleFactor = (int) Math.floor(Math.sqrt(number));
		int[] result = new int[maxFactors];
		int count = 0;
		while (number % 2 == 0)
		{
			result[count++] = 2;
			number >>= 1;
		}
		for (int i = 3; i <= highestPossibleFactor;)
		{
			if (number % i == 0)
			{
				result[count++] = i;
				number /= i;
			}
			else
			{
				i += 2;
			}
		}
		if (number != 1)
		{
			result[count] = number;
		}
		return result;
	}

	private static int getMaxPrimeFactorsFor(int number)
	{
		/*
		 * da 2 der kleinstmoegliche primfaktor ist, ist die anzahl der primfaktoren immer kleiner
		 * gleich dem exponenten der naechst hoeheren zweierpotenz. also (x = anzahl primfaktoren):
		 * number <=
		 * 2^x genauer: 2^(x-1) < number <= 2^x daraus folgt: log(2^(x-1)) < log(number) x-1 <
		 * log(number)/log(2) x < log(number)/log(2) + 1 da x ganzzahlig sein muss, reicht math.ceil
		 * statt "+1"
		 * (ueberschaetzt um 1, wenn log(2) teiler von log(number) ist)
		 */
		return (int) Math.ceil(Math.log10(number) / Math.log10(2));
	}

	/**
	 * returns true, if the given number is a prime power
	 *
	 * @param number
	 * @return
	 */
	public static boolean isPrimePower(int number)
	{
		boolean result = false;
		int[] primeFactors = getPrimeFactors(number);
		if (primeFactors.length > 0)
		{
			int factor = primeFactors[0];
			result = true;
			for (int i = 1; i < primeFactors.length; i++)
			{
				if (primeFactors[i] == 0)
				{
					break;
				}
				else if (primeFactors[i] != factor)
				{
					result = false;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * returns true, if the given number is a prime
	 *
	 * @param number
	 * @return
	 */
	public static boolean isPrime(int number)
	{
		if (number <= 1)
		{
			return false;
		}
		if (number == 2)
		{
			return true;
		}
		if (number % 2 == 0)
		{
			return false;
		}
		for (int i = 3; i <= Math.sqrt(number) + 1; i += 2)
		{
			if (number % i == 0)
			{
				return false;
			}
		}
		return true;
	}
}
