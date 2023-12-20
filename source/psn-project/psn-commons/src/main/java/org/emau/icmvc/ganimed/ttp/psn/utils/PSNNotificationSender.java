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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.psn.dto.InsertPairExceptionDTO;
import org.emau.icmvc.ganimed.ttp.psn.enums.AnonymisationResult;
import org.emau.icmvc.ganimed.ttp.psn.enums.DeletionResult;
import org.emau.icmvc.ttp.notification.NotificationSender;

public class PSNNotificationSender extends NotificationSender
{
	/**
	 * @see <a href="https://www.wikiwand.com/de/Initialization-on-demand_holder_idiom">Initialization-on-demand holder idiom</a>
	 */
	private static class InstanceHolder
	{
		private static final PSNNotificationSender INSTANCE = new PSNNotificationSender();
	}

	public static PSNNotificationSender getInstance() {
		return PSNNotificationSender.InstanceHolder.INSTANCE;
	}

	private PSNNotificationMessage createMessage(String clientId, String event, String domainName)
	{
		return new PSNNotificationMessage("GPAS." + event, clientId, domainName, null);
	}

	public boolean sendNotificationForGetOrCreatePseudonym(String clientId, String domainName, String value, String result)
	{
		return sendNotificationForGetOrCreatePseudonyms(clientId, domainName, Set.of(value), Map.of(value, result));
	}

	public boolean sendNotificationForGetOrCreatePseudonyms(String clientId, String domainName, Set<String> values, Map<String, String> result)
	{
		if (StringUtils.isBlank(clientId))
		{
			return false;
		}

		PSNNotificationMessage msg = createMessage(clientId, "GetOrCreatePseudonyms", domainName);

		msg.setRequest(values); // TODO(FMM) should we omit the redundant request values?
		msg.setPseudonymisationResult(result);

		return sendNotification(msg);
	}

	public boolean sendNotificationForAnonymiseEntry(String clientId, String domainName, String value)
	{
		return sendNotificationForAnonymiseEntries(clientId, domainName, Set.of(value), Map.of(value, AnonymisationResult.SUCCESS));
	}

	public boolean sendNotificationForAnonymiseEntries(String clientId, String domainName, Set<String> values, Map<String, AnonymisationResult> result)
	{
		if (StringUtils.isBlank(clientId))
		{
			return false;
		}

		PSNNotificationMessage msg = createMessage(clientId, "AnonymiseEntries", domainName);

		msg.setRequest(values); // TODO(FMM) should we omit the redundant request values?
		msg.setAnonymisationResult(result);

		return sendNotification(msg);
	}

	public boolean sendNotificationForDeleteEntry(String clientId, String domainName, String value)
	{
		return sendNotificationForDeleteEntries(clientId, domainName, Set.of(value), Map.of(value, DeletionResult.SUCCESS));
	}

	public boolean sendNotificationForDeleteEntries(String clientId, String domainName, Set<String> values, Map<String, DeletionResult> result)
	{
		if (StringUtils.isBlank(clientId))
		{
			return false;
		}

		PSNNotificationMessage msg = createMessage(clientId, "DeleteEntries", domainName);

		msg.setRequest(values); // TODO(FMM) should we omit the redundant request values?
		msg.setDeletionResult(result);

		return sendNotification(msg);
	}

	public boolean sendNotificationForInsertValuePseudonymPair(String clientId, String domainName, String value, String pseudonym)
	{
		return sendNotificationForInsertValuePseudonymPairs(clientId, domainName, Map.of(value, pseudonym), List.of());
	}

	public boolean sendNotificationForInsertValuePseudonymPairs(String clientId, String domainName, Map<String, String> pairs, List<InsertPairExceptionDTO> result)
	{
		if (StringUtils.isBlank(clientId))
		{
			return false;
		}

		PSNNotificationMessage msg = createMessage(clientId, "InsertValuePseudonymPairs", domainName);

		msg.setInsertionRequest(pairs); // TODO(FMM) should we omit the redundant request values (by adding the succeeded insertion pairs to the result with empty error field?
		msg.setInsertionResult(result);

		return sendNotification(msg);
	}
}
