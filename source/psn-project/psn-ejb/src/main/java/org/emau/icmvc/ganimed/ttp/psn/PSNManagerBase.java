package org.emau.icmvc.ganimed.ttp.psn;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.psn.dto.InsertPairExceptionDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.PSNNetDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.PSNTreeDTO;
import org.emau.icmvc.ganimed.ttp.psn.enums.AnonymisationResult;
import org.emau.icmvc.ganimed.ttp.psn.enums.DeletionResult;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DBException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DeletionForbiddenException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DomainIsFullException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InsertPairException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidPSNException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.PSNNotFoundException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownValueException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.ValueIsAnonymisedException;
import org.emau.icmvc.ganimed.ttp.psn.internal.AnonymDomain;
import org.emau.icmvc.ganimed.ttp.psn.utils.PSNNotificationSender;

/**
 * Superclass for PSNManager implementations. No methods are public to enable sublasses to decide
 * on their own which methods to expose by publicly overwriting them.
 * All methods which can change data have an overloaded method variant to additionally pass
 * a notification client ID for sending notifications.
 */
public class PSNManagerBase extends GPASServiceBase
{
	private final Logger LOGGER = LogManager.getLogger(getClass());
	private final PSNNotificationSender ns = PSNNotificationSender.getInstance();

	private String notificationInfo(String notificationClientID)
	{
		return StringUtils.isNotBlank(notificationClientID) ? "with notification for client " + notificationClientID + " ": "";
	}

	protected String getOrCreatePseudonymFor(String value, String domainName)
			throws DBException, DomainIsFullException, InvalidParameterException, UnknownDomainException
	{
		return getOrCreatePseudonymFor(null, value, domainName);
	}
	protected String getOrCreatePseudonymFor(String notificationClientID, String value, String domainName)
			throws DBException, DomainIsFullException, InvalidParameterException, UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("getOrCreatePseudonymFor {}for value {} within domain {}", notificationInfo(notificationClientID), value, domainName);
		}
		checkParameter(value, "value");
		checkParameter(domainName, "domainName");
		String result = cache.getOrCreatePseudonymFor(value, domainName);
		ns.sendNotificationForGetOrCreatePseudonym(notificationClientID, domainName, value, result);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("getOrCreatePseudonymFor {}for value {} within domain {} succeed", notificationInfo(notificationClientID), value, domainName);
		}
		return result;
	}

	protected String getPseudonymFor(String value, String domainName)
			throws InvalidParameterException, UnknownDomainException, UnknownValueException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("getPseudonymFor for value {} within domain {}", value, domainName);
		}
		checkParameter(value, "value");
		checkParameter(domainName, "domainName");
		String result = cache.getPseudonymFor(value, domainName);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("getPseudonymFor for value {} within domain {} succeed", value, domainName);
		}
		return result;
	}

	protected void anonymiseEntry(String value, String domainName)
			throws DBException, InvalidParameterException, UnknownDomainException, UnknownValueException, ValueIsAnonymisedException
	{
		anonymiseEntry(null, value, domainName);
	}
	protected void anonymiseEntry(String notificationClientID, String value, String domainName)
			throws DBException, InvalidParameterException, UnknownDomainException, UnknownValueException, ValueIsAnonymisedException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("anonymiseEntry {}for domain {}", notificationInfo(notificationClientID), domainName);
		}
		checkParameter(value, "value");
		checkParameter(domainName, "domainName");
		if (AnonymDomain.NAME.equals(domainName))
		{
			String message = "it's not possible to anonymise values for the intern domain " + AnonymDomain.NAME;
			LOGGER.error(message);
			throw new InvalidParameterException(message);
		}
		cache.anonymiseEntry(value, domainName);
		ns.sendNotificationForAnonymiseEntry(notificationClientID, domainName, value);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("entry for domain {} anonymised", domainName);
		}
	}

	protected Map<String, AnonymisationResult> anonymiseEntries(Set<String> values, String domainName)
			throws DBException, InvalidParameterException, UnknownDomainException
	{
		return anonymiseEntries(null, values, domainName);
	}
	protected Map<String, AnonymisationResult> anonymiseEntries(String notificationClientID, Set<String> values, String domainName)
			throws DBException, InvalidParameterException, UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("anonymiseEntries {}({}) for domain {}", notificationInfo(notificationClientID), values.size(), domainName);
		}
		checkParameter(values, "values");
		checkParameter(domainName, "domainName");
		if (AnonymDomain.NAME.equals(domainName))
		{
			String message = "it's not possible to anonymise values for the intern domain " + AnonymDomain.NAME;
			LOGGER.error(message);
			throw new InvalidParameterException(message);
		}
		Map<String, AnonymisationResult> result = cache.anonymiseEntries(values, domainName);
		ns.sendNotificationForAnonymiseEntries(notificationClientID, domainName, values, result);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug( "{} entries for domain {} anonymised", values.size(), domainName);
		}
		return result;
	}

	protected boolean isAnonym(String value)
			throws InvalidParameterException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("isAnonym for value {}", value);
		}
		checkParameter(value, "value");
		boolean result = cache.isAnonym(value);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug(value + " is " + (result ? "an" : "no") + " anonym");
		}
		return result;
	}

	protected boolean isAnonymised(String psn, String domainName)
			throws InvalidParameterException, InvalidPSNException, UnknownDomainException, PSNNotFoundException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("isAnonymised for psn {} within domain {}", psn, domainName);
		}
		checkParameter(psn, "psn");
		checkParameter(domainName, "domainName");
		boolean result = cache.isAnonymised(psn, domainName);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("{} is {}anonymised within {}", psn, result ? "" : "not ", domainName);
		}
		return result;
	}

	protected void deleteEntry(String value, String domainName)
			throws DeletionForbiddenException, InvalidParameterException, UnknownDomainException, UnknownValueException
	{
		deleteEntry(null, value, domainName);
	}
	protected void deleteEntry(String notificationClientID, String value, String domainName)
			throws DeletionForbiddenException, InvalidParameterException, UnknownDomainException, UnknownValueException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("deleteEntry {}for value {} from domain {}", notificationInfo(notificationClientID), value, domainName);
		}
		checkParameter(value, "value");
		checkParameter(domainName, "domainName");
		cache.deleteEntry(value, domainName);
		ns.sendNotificationForDeleteEntry(notificationClientID, domainName, value);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("value-pseudonym-pair for value {} removed from domain {}", value, domainName);
		}
	}

	protected Map<String, DeletionResult> deleteEntries(Set<String> values, String domainName)
			throws DeletionForbiddenException, InvalidParameterException, UnknownDomainException
	{
		return deleteEntries(null, values, domainName);
	}
	protected Map<String, DeletionResult> deleteEntries(String notificationClientID, Set<String> values, String domainName)
			throws DeletionForbiddenException, InvalidParameterException, UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("deleteEntries {}for {} values from domain {}", notificationInfo(notificationClientID), values.size(), domainName);
		}
		Map<String, DeletionResult> result = cache.deleteEntries(values, domainName);
		ns.sendNotificationForDeleteEntries(notificationClientID, domainName, values, result);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("deleted {} entries from domain {}", values.size(), domainName);
		}
		return result;
	}

	protected void validatePSN(String psn, String domainName)
			throws InvalidParameterException, InvalidPSNException, UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("validatePSN {} within domain {}", psn, domainName);
		}
		checkParameter(psn, "psn");
		checkParameter(domainName, "domainName");
		cache.validatePSN(psn, domainName);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("{} within domain {} is valid", psn, domainName);
		}
	}

	protected String getValueFor(String psn, String domainName)
			throws InvalidPSNException, PSNNotFoundException, InvalidParameterException, UnknownDomainException, ValueIsAnonymisedException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("getValueFor for pseudonym {} within domain {}", psn, domainName);
		}
		checkParameter(psn, "psn");
		checkParameter(domainName, "domainName");
		String result = cache.getValueFor(psn, domainName);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("found value for pseudonym {} within domain {}",psn, domainName);
		}
		return result;
	}

	protected Map<String, String> getOrCreatePseudonymForList(Set<String> values, String domainName)
			throws DBException, DomainIsFullException, InvalidParameterException, UnknownDomainException
	{
		return getOrCreatePseudonymForList(null, values, domainName);
	}
	protected Map<String, String> getOrCreatePseudonymForList(String notificationClientID, Set<String> values, String domainName)
			throws DBException, DomainIsFullException, InvalidParameterException, UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("getOrCreatePseudonymForList {}for {} values within domain {}", notificationInfo(notificationClientID), values.size(), domainName);
		}
		checkParameter(values, "values");
		checkParameter(domainName, "domainName");
		Map<String, String> result = cache.getOrCreatePseudonymForList(values, domainName);
		ns.sendNotificationForGetOrCreatePseudonyms(notificationClientID, domainName, values, result);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("proceeded {} values", result.size());
		}
		return result;
	}

	protected Map<String, String> getValueForList(Set<String> psnList, String domainName)
			throws InvalidParameterException, UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("getValueForList for {} pseudonyms within domain {}", psnList.size(), domainName);
		}
		checkParameter(psnList, "psnList");
		checkParameter(domainName, "domainName");
		Map<String, String> result = cache.getValueForList(psnList, domainName);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("proceeded {} pseudonyms", result.size());
		}
		return result;
	}

	protected Map<String, String> getPseudonymForList(Set<String> values, String domainName)
			throws InvalidParameterException, UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("getPseudonymForList for {} values within domain {}", values.size(), domainName);
		}
		checkParameter(values, "values");
		checkParameter(domainName, "domainName");
		Map<String, String> result = cache.getPseudonymForList(values, domainName);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("proceeded {} values", result.size());
		}
		return result;
	}

	protected Map<String, String> getPseudonymForValuePrefix(String valuePrefix, String domainName)
			throws InvalidParameterException, UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("getPseudonymForValuePrefix for {} within domain {}", valuePrefix, domainName);
		}
		checkParameter(valuePrefix, "valuePrefix");
		checkParameter(domainName, "domainName");
		Map<String, String> result = cache.getPseudonymForValuePrefix(valuePrefix, domainName);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("found {} pseudonyms", result.size());
		}
		return result;
	}

	protected void insertValuePseudonymPair(String value, String pseudonym, String domainName)
			throws InsertPairException, InvalidParameterException, UnknownDomainException
	{
		insertValuePseudonymPair(null, value, pseudonym, domainName);
	}
	protected void insertValuePseudonymPair(String notificationClientID, String value, String pseudonym, String domainName)
			throws InsertPairException, InvalidParameterException, UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("insertValuePseudonymPair {}for value {} in domain {}", notificationInfo(notificationClientID), value, domainName);
		}
		checkParameter(value, "value");
		checkParameter(pseudonym, "pseudonym");
		checkParameter(domainName, "domainName");
		cache.insertValuePseudonymPair(value, pseudonym, domainName);
		ns.sendNotificationForInsertValuePseudonymPair(notificationClientID, domainName, value, pseudonym);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("persisted pseudonym for {} in domain {}", value, domainName);
		}
	}

	protected List<InsertPairExceptionDTO> insertValuePseudonymPairs(Map<String, String> pairs, String domainName)
			throws InvalidParameterException, UnknownDomainException
	{
		return insertValuePseudonymPairs(null, pairs, domainName);
	}
	protected List<InsertPairExceptionDTO> insertValuePseudonymPairs(String notificationClientID, Map<String, String> pairs, String domainName)
			throws InvalidParameterException, UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("insertValuePseudonymPairs {}for {} value-pseudonym pairs in domain {}", notificationInfo(notificationClientID), pairs.size(), domainName);
		}
		checkParameter(pairs, "pairs");
		checkParameter(domainName, "domainName");
		List<InsertPairExceptionDTO> result = cache.insertValuePseudonymPairs(pairs, domainName);
		ns.sendNotificationForInsertValuePseudonymPairs(notificationClientID, domainName, pairs, result);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("inserted {} value-pseudonym pairs in domain {}. {} errors occurred", pairs.size() - result.size(), domainName, result.size());
		}
		return result;
	}

	protected PSNTreeDTO getPSNTreeForPSN(String psn, String domainName)
			throws InvalidParameterException, InvalidPSNException, PSNNotFoundException, UnknownDomainException, ValueIsAnonymisedException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("getPSNTreeForPSN for {} within domain {}", psn, domainName);
		}
		checkParameter(psn, "psn");
		checkParameter(domainName, "domainName");
		PSNTreeDTO result = cache.getPSNTreeForPSN(psn, domainName);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("psn tree created");
		}
		return result;
	}

	protected PSNNetDTO getPSNNetFor(String valueOrPSN) throws InvalidParameterException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("getPSNNetFor for value or psn {}", valueOrPSN);
		}
		checkParameter(valueOrPSN, "valueOrPSN");
		PSNNetDTO result = cache.getPSNNetFor(valueOrPSN);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("psn net created");
		}
		return result;
	}
}
