package org.emau.icmvc.ganimed.ttp.psn.internal;

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

public class PSNCacheObject
{
	public final static int BITS_PER_FIELD = 32;
	// jedes moegliche psn wird durch ein bit repraesentiert
	// pos des bits entspricht nummer des psn wenn alle moeglichen psn alphabetisch durchnummeriert wuerden
	// pre-, suffixe und pruefziffern werden ignoriert
	private final int[] cache;
	private long count = 0;

	public PSNCacheObject(long size)
	{
		// wert ist sicher int, siehe berechnung in generator.java
		int realSize = (int) (size / BITS_PER_FIELD);
		if (size % BITS_PER_FIELD != 0)
		{
			realSize++;
		}
		cache = new int[realSize];
	}

	public void setPos(long pos)
	{
		// wert ist sicher int, siehe berechnung in generator.java
		int fieldPos = (int) (pos / BITS_PER_FIELD);
		short posWithinInt = (short) (pos % BITS_PER_FIELD);
		cache[fieldPos] = cache[fieldPos] | 1 << posWithinInt;
		count++;
	}

	public void unsetPos(long pos)
	{
		// wert ist sicher int, siehe berechnung in generator.java
		int fieldPos = (int) (pos / BITS_PER_FIELD);
		short posWithinInt = (short) (pos % BITS_PER_FIELD);
		cache[fieldPos] = cache[fieldPos] & ~(1 << posWithinInt);
		count--;
	}

	public boolean isPosSet(long pos)
	{
		// wert ist sicher int, siehe berechnung in generator.java
		int fieldPos = (int) (pos / BITS_PER_FIELD);
		short posWithinInt = (short) (pos % BITS_PER_FIELD);
		return (cache[fieldPos] & 1 << posWithinInt) != 0;
	}

	public long getCount()
	{
		return count;
	}
}
