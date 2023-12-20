package org.emau.icmvc.ganimed.ttp.psn.utils;

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

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class PSNNotificationMessageTest extends AbstractPSNNotificationMessageTest
{
	@Test
	void equals()
	{
		assertNotEquals(msg, new PSNNotificationMessage());

		assertNotEquals(msg, msgP);
		assertNotEquals(msg, msgA);
		assertNotEquals(msg, msgD);
		assertNotEquals(msg, msgI);

		assertNotEquals(msgP, msgA);
		assertNotEquals(msgP, msgD);
		assertNotEquals(msgP, msgI);
	}

	@Test
	void capture()
	{
		assertEquals(msgP, captured(msgP));
		assertEquals(msgA, captured(msgA));
		assertEquals(msgD, captured(msgD));
		assertEquals(msgI, captured(msgI));
	}

	@Test
	void toJson() throws JsonProcessingException
	{
		assertEquals(JSON_P, msgP.toJson());
		assertEquals(JSON_A, msgA.toJson());
		assertEquals(JSON_D, msgD.toJson());
		assertEquals(JSON_I, msgI.toJson());
	}

	@Test
	void fromJson() throws IOException
	{
		assertEquals(msgP, new PSNNotificationMessage(JSON_P));
		assertEquals(msgA, new PSNNotificationMessage(JSON_A));
		assertEquals(msgD, new PSNNotificationMessage(JSON_D));
		assertEquals(msgI, new PSNNotificationMessage(JSON_I));
	}
}
