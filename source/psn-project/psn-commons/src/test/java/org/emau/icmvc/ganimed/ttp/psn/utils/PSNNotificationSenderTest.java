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

import java.util.Map;
import java.util.Set;

import org.codehaus.jettison.json.JSONObject;
import org.emau.icmvc.ganimed.ttp.psn.enums.AnonymisationResult;
import org.emau.icmvc.ganimed.ttp.psn.enums.DeletionResult;
import org.emau.icmvc.ttp.notification.NotificationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PSNNotificationSenderTest extends AbstractPSNNotificationMessageTest
{
	private TestPSNNotificationSender sender;

	static class TestPSNNotificationSender extends PSNNotificationSender
	{
		PSNNotificationMessage lastMsg;

		@Override
		public boolean sendNotification(NotificationMessage msg)
		{
			assertEquals(PSNNotificationMessage.class, msg.getClass());
			if (msg instanceof PSNNotificationMessage pMsg)
			{
				lastMsg = pMsg;
			}
			else
			{
				lastMsg = null;
			}

			return super.sendNotification(msg);
		}

		@Override
		public boolean sendNotification(JSONObject msg, String clientId, String type)
		{
			return true;
		}
	}

	@Override
	@BeforeEach
	void setUp()
	{
		super.setUp();
		sender = new TestPSNNotificationSender();
	}

	@Test
	void getInstance()
	{
		assertSame(PSNNotificationSender.getInstance(), PSNNotificationSender.getInstance());
	}

	private void assertCommonAttrs(PSNNotificationMessage msg, String type)
	{
		assertNotNull(msg);
		assertEquals("cid", msg.getClientId());
		assertEquals("d", msg.getDomain());
		assertNull(msg.getComment());
		assertEquals("GPAS." + type, msg.getType());
	}

	@Test
	void sendNotificationForGetOrCreatePseudonym()
	{
		Map.Entry<String, String> e = pseudonymisationResult.firstEntry();
		sender.sendNotificationForGetOrCreatePseudonym("cid", "d", e.getKey(), e.getValue());
		assertCommonAttrs(sender.lastMsg, "GetOrCreatePseudonyms");
		Set<Map.Entry<String, String>> result = sender.lastMsg.getPseudonymisationResult().entrySet();
		assertEquals(1, result.size());
		assertTrue(result.contains(e));
		assertEquals(Set.of(e.getKey()), sender.lastMsg.getRequest());
		assertNull(sender.lastMsg.getAnonymisationResult());
		assertNull(sender.lastMsg.getDeletionResult());
		assertNull(sender.lastMsg.getInsertionResult());
		assertNull(sender.lastMsg.getInsertionRequest());
	}

	@Test
	void sendNotificationForGetOrCreatePseudonyms()
	{
		sender.sendNotificationForGetOrCreatePseudonyms("cid", "d", request, pseudonymisationResult);
		assertCommonAttrs(sender.lastMsg, "GetOrCreatePseudonyms");
		assertEquals(request, sender.lastMsg.getRequest());
		assertEquals(pseudonymisationResult, sender.lastMsg.getPseudonymisationResult());
		assertEquals(request, sender.lastMsg.getRequest());
		assertNull(sender.lastMsg.getAnonymisationResult());
		assertNull(sender.lastMsg.getDeletionResult());
		assertNull(sender.lastMsg.getInsertionResult());
		assertNull(sender.lastMsg.getInsertionRequest());
	}

	@Test
	void sendNotificationForAnonymiseEntry()
	{
		Map.Entry<String, AnonymisationResult> e = anonymisationResult.firstEntry();
		sender.sendNotificationForAnonymiseEntry("cid", "d", e.getKey());
		assertCommonAttrs(sender.lastMsg, "AnonymiseEntries");
		Map<String, AnonymisationResult> result = sender.lastMsg.getAnonymisationResult();
		assertEquals(1, result.size());
		assertTrue(result.containsKey(e.getKey()));
		assertTrue(result.containsValue(AnonymisationResult.SUCCESS));
		assertEquals(Set.of(e.getKey()), sender.lastMsg.getRequest());
		assertNull(sender.lastMsg.getPseudonymisationResult());
		assertNull(sender.lastMsg.getDeletionResult());
		assertNull(sender.lastMsg.getInsertionResult());
		assertNull(sender.lastMsg.getInsertionRequest());
	}

	@Test
	void sendNotificationForAnonymiseEntries()
	{
		sender.sendNotificationForAnonymiseEntries("cid", "d", request, anonymisationResult);
		assertCommonAttrs(sender.lastMsg, "AnonymiseEntries");
		assertEquals(request, sender.lastMsg.getRequest());
		assertEquals(anonymisationResult, sender.lastMsg.getAnonymisationResult());
		assertEquals(request, sender.lastMsg.getRequest());
		assertNull(sender.lastMsg.getPseudonymisationResult());
		assertNull(sender.lastMsg.getDeletionResult());
		assertNull(sender.lastMsg.getInsertionResult());
		assertNull(sender.lastMsg.getInsertionRequest());
	}

	@Test
	void sendNotificationForDeleteEntry()
	{
		Map.Entry<String, DeletionResult> e = deletionResult.firstEntry();
		sender.sendNotificationForDeleteEntry("cid", "d", e.getKey());
		assertCommonAttrs(sender.lastMsg, "DeleteEntries");
		Map<String, DeletionResult> result = sender.lastMsg.getDeletionResult();
		assertEquals(1, result.size());
		assertTrue(result.containsKey(e.getKey()));
		assertTrue(result.containsValue(DeletionResult.SUCCESS));
		assertEquals(Set.of(e.getKey()), sender.lastMsg.getRequest());
		assertNull(sender.lastMsg.getPseudonymisationResult());
		assertNull(sender.lastMsg.getAnonymisationResult());
		assertNull(sender.lastMsg.getInsertionResult());
		assertNull(sender.lastMsg.getInsertionRequest());
	}

	@Test
	void sendNotificationForDeleteEntries()
	{
		sender.sendNotificationForDeleteEntries("cid", "d", request, deletionResult);
		assertCommonAttrs(sender.lastMsg, "DeleteEntries");
		assertEquals(request, sender.lastMsg.getRequest());
		assertEquals(deletionResult, sender.lastMsg.getDeletionResult());
		assertEquals(request, sender.lastMsg.getRequest());
		assertNull(sender.lastMsg.getPseudonymisationResult());
		assertNull(sender.lastMsg.getAnonymisationResult());
		assertNull(sender.lastMsg.getInsertionResult());
		assertNull(sender.lastMsg.getInsertionRequest());
	}

	@Test
	void sendNotificationForInsertValuePseudonymPair()
	{
		Map.Entry<String, String> e = insertionRequest.firstEntry();
		sender.sendNotificationForInsertValuePseudonymPair("cid", "d", e.getKey(), e.getValue());
		assertCommonAttrs(sender.lastMsg, "InsertValuePseudonymPairs");
		Map<String, String> request = sender.lastMsg.getInsertionRequest();
		assertEquals(1, request.size());
		assertTrue(request.containsKey(e.getKey()));
		assertTrue(request.containsValue(e.getValue()));
		assertNull(sender.lastMsg.getRequest());
		assertNull(sender.lastMsg.getPseudonymisationResult());
		assertNull(sender.lastMsg.getAnonymisationResult());
		assertNull(sender.lastMsg.getDeletionResult());
	}

	@Test
	void sendNotificationForInsertValuePseudonymPairs()
	{
		sender.sendNotificationForInsertValuePseudonymPairs("cid", "d", insertionRequest, insertionResult);
		assertCommonAttrs(sender.lastMsg, "InsertValuePseudonymPairs");
		assertEquals(insertionRequest, sender.lastMsg.getInsertionRequest());
		assertEquals(insertionResult, sender.lastMsg.getInsertionResult());
		assertNull(sender.lastMsg.getRequest());
		assertNull(sender.lastMsg.getPseudonymisationResult());
		assertNull(sender.lastMsg.getAnonymisationResult());
		assertNull(sender.lastMsg.getDeletionResult());
	}
}
