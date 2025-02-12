package org.emau.icmvc.ganimed.ttp.psn;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.emau.icmvc.ganimed.ttp.psn.exceptions.PSNErrorStrings;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.PSNNotFoundException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownValueException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.ValueIsAnonymisedException;
import org.emau.icmvc.ganimed.ttp.psn.utils.PSNNotificationSender;
import org.emau.icmvc.ganimed.ttp.psn.utils.StringPair;
import org.emau.icmvc.ttp.util.Pair;

/**
 * Superclass for PSNManager implementations. No methods are public to enable subclasses to decide
 * on their own which methods to expose by publicly overwriting them.
 * All methods which can change data take an additionally a notification client ID as parameter
 * for sending notifications.
 */
public class PSNManagerBase extends GPASServiceBase
{
	public static final String SYSPROP_KEY_MPSN_ALLOW_ENTRY_METHODS = "ttp.gpas.mpsn.allowEntryMethods";
	public static final String ENVVAR_KEY_MPSN_ALLOW_ENTRY_METHODS = "TTP_GPAS_MPSN_ALLOW_ENTRY_METHODS";
	private static final String VALUE = "value";
	private static final String VALUES = "values";
	private static final String DOMAIN_NAME = "domainName";
	private static final String FOR_VALUE_WITHIN_DOMAIN = "{} {}for value {} within domain {}";
	private static final String FOR_VALUES_WITHIN_DOMAIN = "{} {}for {} values within domain {}";
	private static final String PROCEEDED_VALUES = "proceeded {} values";
	private static final String SEP = System.getProperty("org.emau.icmvc.ganimed.ttp.psn.separator", ",");

	private final PSNNotificationSender ns = PSNNotificationSender.getInstance();

	private static boolean allowEntryMethodsForMPSNDomains()
	{
		return Boolean.getBoolean(SYSPROP_KEY_MPSN_ALLOW_ENTRY_METHODS) ||
				Boolean.parseBoolean(System.getenv().getOrDefault(ENVVAR_KEY_MPSN_ALLOW_ENTRY_METHODS, "false"));
	}

	private static String notificationInfo(String notificationClientID)
	{
		return StringUtils.isNotBlank(notificationClientID) ? "with notification for client " + notificationClientID + " " : "";
	}

	private static Map<String, List<String>> toArrayListValuedHashMap(MultiValuedMap<String, String> multiMap)
	{
		return multiMap.asMap().entrySet().stream().collect(Collectors.toMap(
						Entry::getKey,
						e -> e.getValue() instanceof ArrayList ? (ArrayList<String>) e.getValue() : new ArrayList<>(e.getValue()),
						(oldValue, newValue) -> oldValue,
						HashMap::new
				));
	}

	private static Map<String, String> toJoinedFlatMap(Map<String, List<String>> multiMap)
	{
		Map<String, String> joinedFlatMap = HashMap.newHashMap(multiMap.size());
		multiMap.forEach((key, value) -> joinedFlatMap.put(key, String.join(SEP, value)));
		return joinedFlatMap;
	}

	private static Map<String, String> toFlatMap(MultiValuedMap<String, String> multiMap)
	{
		return toFlatMap(multiMap, null);
	}

	private static Map<String, String> toFlatMap(MultiValuedMap<String, String> multiMap, String multiValueMarker)
	{
		Map<String, String> flatMap = HashMap.newHashMap(multiMap.size());
		for (Entry<String, String> e : multiMap.entries())
		{
			String key = e.getKey();
			String oldValue = flatMap.putIfAbsent(key, e.getValue());
			if (oldValue != null)
			{
				if (multiValueMarker == null)
				{
					throwIllegalArgumentExceptionForMultiplePsnContext(key);
				}
				else if (!oldValue.equals(multiValueMarker))
				{
					flatMap.put(key, multiValueMarker);
				}
			}
		}
		return flatMap;
	}

	private void debug(String msg, String method, String notificationClientID, Object... args)
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug(msg, ArrayUtils.insert(0, args, method, notificationInfo(notificationClientID)));
		}
	}

	/**
	 * @see PSNManager#createPseudonymFor(String, String)
	 * @see PSNManagerWithNotification#createPseudonymFor(String, String, String)
	 */
	protected String createPseudonymFor(String notificationClientID, String value, String domainName)
			throws DomainIsFullException, InvalidParameterException, UnknownDomainException
	{
		debug(FOR_VALUE_WITHIN_DOMAIN, "createPseudonymFor", notificationClientID, value, domainName);
		checkParameter(value, VALUE);
		checkParameter(domainName, DOMAIN_NAME);
		List<String> psns = cache.createPseudonymsFor(value, domainName, 1);
		// For both spsn and mpsn domains, the result contains only a single PSN.
		String result = psns.getFirst();
		ns.sendNotificationForGetOrCreatePseudonym(notificationClientID, domainName, value, result);
		LOGGER.debug("createPseudonymFor for value {} within domain {} succeeded", value, domainName);
		return result;
	}

	/**
	 * @see PSNManager#createPseudonymsFor(String, String, int)
	 * @see PSNManagerWithNotification#createPseudonymsFor(String, String, String, int)
	 */
	protected List<String> createPseudonymsFor(String notificationClientID, String value, String domainName, int number)
			throws DomainIsFullException, InvalidParameterException, UnknownDomainException
	{
		debug(FOR_VALUE_WITHIN_DOMAIN, "createPseudonymsFor", notificationClientID, value, domainName);
		checkParameter(value, VALUE);
		checkParameter(domainName, DOMAIN_NAME);
		// For spsn domains, an exception will be triggered at cache.createPseudonymsFor,
		// if the operation would result in more than one PSN for the value.
		List<String> psns = cache.createPseudonymsFor(value, domainName, number);
		ns.sendNotificationForGetOrCreatePseudonym(notificationClientID, domainName, value, String.join(SEP, psns));
		LOGGER.debug("createPseudonymsFor for value {} within domain {} succeeded", value, domainName);
		return psns;
	}

	/**
	 * @see PSNManager#createPseudonymForList(Set, String)
	 * @see PSNManagerWithNotification#createPseudonymForList(String, Set, String)
	 */
	protected Map<String, String> createPseudonymForList(String notificationClientID, Set<String> values, String domainName)
			throws DomainIsFullException, InvalidParameterException, UnknownDomainException
	{
		debug(FOR_VALUES_WITHIN_DOMAIN, "createPseudonymForList", notificationClientID, values.size(), domainName);
		checkParameter(values, VALUES);
		checkParameter(domainName, DOMAIN_NAME);
		ListValuedMap<String, String> psns = cache.createPseudonymsForList(values, domainName, 1);
		// For both spsn and mpsn domains, the map's list values each contain exactly one element, so that flattening is not a problem here.
		Map<String, String> result = toFlatMap(psns);
		ns.sendNotificationForGetOrCreatePseudonyms(notificationClientID, domainName, values, result);
		LOGGER.debug(PROCEEDED_VALUES, result.size());
		return result;
	}

	/**
	 * @see PSNManager#createPseudonymsForList(Set, String, int)
	 * @see PSNManagerWithNotification#createPseudonymsForList(String, Set, String, int)
	 */
	protected Map<String, List<String>> createPseudonymsForList(String notificationClientID, Set<String> values, String domainName, int number)
			throws DomainIsFullException, InvalidParameterException, UnknownDomainException
	{
		debug(FOR_VALUES_WITHIN_DOMAIN, "createPseudonymsForList", notificationClientID, values.size(), domainName);
		checkParameter(values, VALUES);
		checkParameter(domainName, DOMAIN_NAME);
		Map<String, List<String>> result = toArrayListValuedHashMap(cache.createPseudonymsForList(values, domainName, number));
		// For spsn domains, the final existence of a single PSN per value has already been asserted at cache.getOrCreatePseudonymsFor,
		// so that in this case the map's list values each contain exactly one element.
		ns.sendNotificationForGetOrCreatePseudonyms(notificationClientID, domainName, values, toJoinedFlatMap(result));
		LOGGER.debug(PROCEEDED_VALUES, result.size());
		return result;
	}

	/**
	 * @see PSNManager#getOrCreatePseudonymFor(String, String)
	 * @see PSNManagerWithNotification#getOrCreatePseudonymFor(String, String, String)
	 */
	protected String getOrCreatePseudonymFor(String notificationClientID, String value, String domainName)
			throws DomainIsFullException, InvalidParameterException, UnknownDomainException
	{
		debug(FOR_VALUE_WITHIN_DOMAIN, "getOrCreatePseudonymFor", notificationClientID, value, domainName);
		checkParameter(value, VALUE);
		checkParameter(domainName, DOMAIN_NAME);
		List<String> psns = cache.getOrCreatePseudonymsFor(value, domainName, 1);
		// For spsn domains, the final existence of a single PSN for the given value has already been asserted at cache.getOrCreatePseudonymsFor,
		// so that in this case the list contains exactly one element.
		if (psns.size() > 1)
		{
			// For mpsn domains everything was fine so far and no new PSN was created, but we can't return multiple PSNs as a single string.
			throwIllegalArgumentExceptionForMultiplePsnContext(value);
		}
		String result = psns.getFirst();
		ns.sendNotificationForGetOrCreatePseudonym(notificationClientID, domainName, value, result);
		LOGGER.debug("getOrCreatePseudonymFor for value {} within domain {} succeeded", value, domainName);
		return result;
	}

	/**
	 * @see PSNManager#getOrCreatePseudonymsFor(String, String, int)
	 * @see PSNManagerWithNotification#getOrCreatePseudonymsFor(String, String, String, int)
	 */
	protected List<String> getOrCreatePseudonymsFor(String notificationClientID, String value, String domainName, int minNumber)
			throws InvalidParameterException, DomainIsFullException, UnknownDomainException
	{
		debug(FOR_VALUE_WITHIN_DOMAIN, "getOrCreatePseudonymsFor", notificationClientID, value, domainName);
		checkParameter(value, VALUE);
		checkParameter(domainName, DOMAIN_NAME);
		List<String> psns = cache.getOrCreatePseudonymsFor(value, domainName, minNumber);
		// For spsn domains, the final existence of a single PSN for the given value has already been asserted at cache.getOrCreatePseudonymsFor,
		// so that in this case the list contains exactly one element.
		ns.sendNotificationForGetOrCreatePseudonym(notificationClientID, domainName, value, String.join(SEP, psns));
		LOGGER.debug("getOrCreatePseudonymsFor for value {} within domain {} succeeded", value, domainName);
		return psns;
	}

	/**
	 * @see PSNManager#getOrCreatePseudonymForList(Set, String)
	 * @see PSNManagerWithNotification#getOrCreatePseudonymForList(String, Set, String)
	 */
	protected Map<String, String> getOrCreatePseudonymForList(String notificationClientID, Set<String> values, String domainName)
			throws DomainIsFullException, InvalidParameterException, UnknownDomainException
	{
		debug(FOR_VALUES_WITHIN_DOMAIN, "getOrCreatePseudonymForList", notificationClientID, values.size(), domainName);
		checkParameter(values, VALUES);
		checkParameter(domainName, DOMAIN_NAME);
		ListValuedMap<String, String> psns = cache.getOrCreatePseudonymsForList(values, domainName, 1);
		// For spsn domains, the final existence of a single PSN for the given value has already been asserted at cache.getOrCreatePseudonymsFor,
		// so that in this case the list contains exactly one element.
		// For mpsn domains everything was fine so far, but we can't return multiple PSNs as a single string.
		// No additional PSN has been created (when requesting one but finding multiple PSNs),
		// so we replace multiple PSNs with an error marker when flattening the multi-valued list.
		Map<String, String> result = toFlatMap(psns, PSNErrorStrings.MULTIPLE_PSNS);
		ns.sendNotificationForGetOrCreatePseudonyms(notificationClientID, domainName, values, result);
		LOGGER.debug(PROCEEDED_VALUES, result.size());
		return result;
	}

	/**
	 * @see PSNManager#getOrCreatePseudonymsForList(Set, String, int)
	 * @see PSNManagerWithNotification#getOrCreatePseudonymsForList(String, Set, String, int)
	 */
	protected Map<String, List<String>> getOrCreatePseudonymsForList(String notificationClientID, Set<String> values, String domainName, int minNumber)
			throws DomainIsFullException, InvalidParameterException, UnknownDomainException
	{
		debug(FOR_VALUES_WITHIN_DOMAIN, "getOrCreatePseudonymsForList", notificationClientID, values.size(), domainName);
		checkParameter(values, VALUES);
		checkParameter(domainName, DOMAIN_NAME);
		Map<String, List<String>> result = toArrayListValuedHashMap(cache.getOrCreatePseudonymsForList(values, domainName, minNumber)); // throws an exception for multi-PSNs
		// For spsn domains, the final existence of a single PSN per value has already been asserted at cache.getOrCreatePseudonymsForList,
		// so that in this case the map's list values each contain exactly one element.
		ns.sendNotificationForGetOrCreatePseudonyms(notificationClientID, domainName, values, toJoinedFlatMap(result));
		LOGGER.debug(PROCEEDED_VALUES, result.size());
		return result;
	}

	/**
	 * @see PSNManager#getPseudonymFor(String, String)
	 */
	protected String getPseudonymFor(String value, String domainName)
			throws InvalidParameterException, UnknownDomainException, UnknownValueException
	{
		LOGGER.debug("getPseudonymFor for value {} within domain {}", value, domainName);
		checkParameter(value, VALUE);
		checkParameter(domainName, DOMAIN_NAME);
		List<String> psns = cache.getPseudonymsFor(value, domainName);
		// For spsn domains, the final existence of a single PSN for the given value has already been asserted at cache.getPseudonymsFor,
		// so that in this case the list contains exactly one element.
		if (psns.size() > 1)
		{
			// For mpsn domains everything was fine so far and no new PSN was created, but we can't return multiple PSNs as a single string.
			throwIllegalArgumentExceptionForMultiplePsnContext(value);
		}
		LOGGER.debug("getPseudonymFor for value {} within domain {} succeeded", value, domainName);
		return psns.getFirst();
	}

	/**
	 * @see PSNManager#getPseudonymsFor(String, String)
	 */
	protected List<String> getPseudonymsFor(String value, String domainName)
			throws InvalidParameterException, UnknownDomainException, UnknownValueException
	{
		LOGGER.debug("getPseudonymsFor for value {} within domain {}", value, domainName);
		checkParameter(value, VALUE);
		checkParameter(domainName, DOMAIN_NAME);
		List<String> psns = cache.getPseudonymsFor(value, domainName);
		// For spsn domains, the final existence of a single PSN for the given value has already been asserted at cache.getOrCreatePseudonymsFor,
		// so that in this case the list contains exactly one element.
		LOGGER.debug("getPseudonymsFor for value {} within domain {} succeeded", value, domainName);
		return psns;
	}

	/**
	 * @see PSNManager#getPseudonymForList(Set, String)
	 */
	protected Map<String, String> getPseudonymForList(Set<String> values, String domainName)
			throws InvalidParameterException, UnknownDomainException
	{
		LOGGER.debug("getPseudonymForList for {} values within domain {}", values.size(), domainName);
		checkParameter(values, VALUES);
		checkParameter(domainName, DOMAIN_NAME);
		ListValuedMap<String, String> psns = cache.getPseudonymsForList(values, domainName);
		// For spsn domains, the final existence of a single PSN per value has already been asserted at cache.getPseudonymsForList,
		// so that in this case the map's list values each contain exactly one element.
		// For mpsn domains everything was fine so far, but we can't return multiple PSNs as a single string.
		// so we replace multiple PSNs with an error marker when flattening the multi-valued list.
		Map<String, String> result = toFlatMap(psns, PSNErrorStrings.MULTIPLE_PSNS);
		LOGGER.debug(PROCEEDED_VALUES, result.size());
		return result;
	}

	/**
	 * @see PSNManager#getPseudonymsForList(Set, String)
	 */
	protected Map<String, List<String>> getPseudonymsForList(Set<String> values, String domainName)
			throws InvalidParameterException, UnknownDomainException
	{
		LOGGER.debug("getPseudonymsForList for {} values within domain {}", values.size(), domainName);
		checkParameter(values, VALUES);
		checkParameter(domainName, DOMAIN_NAME);
		Map<String, List<String>> result = toArrayListValuedHashMap(cache.getPseudonymsForList(values, domainName)); // throws an exception for multi-PSNs
		// For spsn domains, the final existence of a single PSN per value has already been asserted at cache.getPseudonymsForList,
		// so that in this case the map's list values each contain exactly one element.
		LOGGER.debug(PROCEEDED_VALUES, result.size());
		return result;
	}

	/**
	 * @see PSNManager#getPseudonymForValuePrefix(String, String)
	 */
	protected Map<String, String> getPseudonymForValuePrefix(String valuePrefix, String domainName)
			throws InvalidParameterException, UnknownDomainException
	{
		LOGGER.debug("getPseudonymForValuePrefix for {} within domain {}", valuePrefix, domainName);
		checkParameter(valuePrefix, "valuePrefix");
		checkParameter(domainName, DOMAIN_NAME);
		ListValuedMap<String, String> psns = cache.getPseudonymsForValuePrefix(valuePrefix, domainName);
		// For spsn domains, the final existence of a single PSN per value has already been asserted at cache.getPseudonymsForList,
		// so that in this case the map's list values each contain exactly one element.
		// For mpsn domains everything was fine so far, but we can't return multiple PSNs as a single string.
		// so we replace multiple PSNs with an error marker when flattening the multi-valued list.
		Map<String, String> result = toFlatMap(psns, PSNErrorStrings.MULTIPLE_PSNS);
		LOGGER.debug("found {} pseudonyms", result.size());
		return result;
	}

	/**
	 * @see PSNManager#getPseudonymsForValuePrefix(String, String)
	 */
	protected Map<String, List<String>> getPseudonymsForValuePrefix(String valuePrefix, String domainName)
			throws InvalidParameterException, UnknownDomainException
	{
		LOGGER.debug("getPseudonymsForValuePrefix for {} within domain {}", valuePrefix, domainName);
		checkParameter(valuePrefix, "valuePrefix");
		checkParameter(domainName, DOMAIN_NAME);
		ListValuedMap<String, String> result = cache.getPseudonymsForValuePrefix(valuePrefix, domainName);
		// For spsn domains, the final existence of a single PSN per value has already been asserted at cache.getPseudonymsForList,
		// so that in this case the map's list values each contain exactly one element.
		LOGGER.debug("found {} pseudonyms", result.values().size());
		return toArrayListValuedHashMap(result);
	}

	/**
	 * @see PSNManager#getValueFor(String, String)
	 */
	protected String getValueFor(String psn, String domainName)
			throws InvalidPSNException, PSNNotFoundException, InvalidParameterException, UnknownDomainException, ValueIsAnonymisedException
	{
		LOGGER.debug("getValueFor for pseudonym {} within domain {}", psn, domainName);
		checkParameter(psn, "psn");
		checkParameter(domainName, DOMAIN_NAME);
		String result = cache.getValueFor(psn, domainName);
		LOGGER.debug("found value for pseudonym {} within domain {}",psn, domainName);
		return result;
	}

	/**
	 * @see PSNManager#getValueForList(Set, String)
	 */
	protected Map<String, String> getValueForList(Set<String> psnList, String domainName)
			throws InvalidParameterException, UnknownDomainException
	{
		LOGGER.debug("getValueForList for {} pseudonyms within domain {}", psnList.size(), domainName);
		checkParameter(psnList, "psnList");
		checkParameter(domainName, DOMAIN_NAME);
		Map<String, String> result = cache.getValueForList(psnList, domainName);
		LOGGER.debug("proceeded {} pseudonyms", result.size());
		return result;
	}

	/**
	 * @see PSNManager#anonymiseEntry(String, String)
	 * @see PSNManagerWithNotification#anonymiseEntry(String, String, String)
	 */
	protected void anonymiseEntry(String notificationClientID, String value, String domainName)
			throws DBException, InvalidParameterException, UnknownDomainException, UnknownValueException, ValueIsAnonymisedException
	{
		anonymiseValue("anonymiseEntry", notificationClientID, value, domainName, allowEntryMethodsForMPSNDomains());
	}


	/**
	 * @see PSNManager#anonymiseEntries(Set, String)
	 * @see PSNManagerWithNotification#anonymiseEntries(String, Set, String)
	 */
	protected Map<String, AnonymisationResult> anonymiseEntries(String notificationClientID, Set<String> values, String domainName)
			throws DBException, InvalidParameterException, UnknownDomainException
	{
		return anonymiseValues("anonymiseEntries", notificationClientID, values, domainName, allowEntryMethodsForMPSNDomains());
	}

	/**
	 * @see PSNManager#anonymiseAllEntriesForValue(String, String)
	 * @see PSNManagerWithNotification#anonymiseAllEntriesForValue(String, String, String)
	 */
	protected void anonymiseAllEntriesForValue(String notificationClientID, String value, String domainName)
			throws DBException, InvalidParameterException, UnknownDomainException, UnknownValueException, ValueIsAnonymisedException
	{
		anonymiseValue("anonymiseAllEntriesForValue", notificationClientID, value, domainName, true);
	}

	/**
	 * @see PSNManager#anonymiseAllEntriesForValues(Set, String)
	 * @see PSNManagerWithNotification#anonymiseAllEntriesForValues(String, Set, String)
	 */
	protected Map<String, AnonymisationResult> anonymiseAllEntriesForValues(String notificationClientID, Set<String> values, String domainName)
			throws DBException, InvalidParameterException, UnknownDomainException
	{
		return anonymiseValues("anonymiseAllEntriesForValues", notificationClientID, values, domainName, true);
	}

	/**
	 * @see #anonymiseEntry(String, String, String)
	 * @see #anonymiseAllEntriesForValue(String, String, String)
	 */
	private void anonymiseValue(String method, String notificationClientID, String value, String domainName, boolean allowMultiPSN)
			throws DBException, InvalidParameterException, UnknownDomainException, UnknownValueException, ValueIsAnonymisedException
	{
		debug("{} {}for domain {}", method, notificationClientID, domainName);
		checkParameter(value, VALUE);
		checkParameter(domainName, DOMAIN_NAME);
		checkThatNotAnonymDomain(domainName);
		if (!allowMultiPSN)
		{
			checkSinglePsnDomain(domainName);
		}
		cache.anonymiseValue(value, domainName);
		ns.sendNotificationForAnonymiseEntry(notificationClientID, domainName, value);
		LOGGER.debug("entry for domain {} anonymised", domainName);
	}

	/**
	 * @see #anonymiseEntries(String, Set, String)
	 * @see #anonymiseAllEntriesForValues(String, Set, String)
	 */
	private Map<String, AnonymisationResult> anonymiseValues(String method, String notificationClientID, Set<String> values, String domainName, boolean allowMultiPSN)
			throws DBException, InvalidParameterException, UnknownDomainException
	{
		debug("{} {}({}) for domain {}", method, notificationClientID, values.size(), domainName);
		checkParameter(values, VALUES);
		checkParameter(domainName, DOMAIN_NAME);
		checkThatNotAnonymDomain(domainName);
		if (!allowMultiPSN)
		{
			checkSinglePsnDomain(domainName);
		}
		Map<String, AnonymisationResult> result = cache.anonymiseValues(values, domainName);
		ns.sendNotificationForAnonymiseEntries(notificationClientID, domainName, values, result);
		LOGGER.debug( "{} entries for domain {} anonymised", values.size(), domainName);
		return result;
	}

	/**
	 * @see PSNManager#anonymisePseudonym(String, String)
	 * @see PSNManagerWithNotification#anonymisePseudonym(String, String, String)
	 */
	protected void anonymisePseudonym(String notificationClientID, String psn, String domainName)
			throws InvalidPSNException, PSNNotFoundException, DBException, UnknownDomainException, InvalidParameterException
	{
		debug("{} {}for domain {}","anonymisePseudonym", notificationClientID, domainName);
		checkParameter(psn, "psn");
		checkParameter(domainName, DOMAIN_NAME);
		checkThatNotAnonymDomain(domainName);
		cache.anonymisePseudonym(psn, domainName);
		ns.sendNotificationForAnonymiseEntry(notificationClientID, domainName, psn);
		LOGGER.debug("psn for domain {} anonymised", domainName);
	}

	/**
	 * @see PSNManager#anonymisePseudonyms(Set, String)
	 * @see PSNManagerWithNotification#anonymisePseudonyms(String, Set, String)
	 */
	protected Map<String, AnonymisationResult> anonymisePseudonyms(String notificationClientID, Set<String> psns, String domainName)
			throws DBException, InvalidParameterException, UnknownDomainException
	{
		debug("{} {}({}) for domain {}", "anonymisePseudonyms", notificationClientID, psns.size(), domainName);
		checkParameter(psns, VALUES);
		checkParameter(domainName, DOMAIN_NAME);
		checkThatNotAnonymDomain(domainName);
		Map<String, AnonymisationResult> result = cache.anonymisePseudonyms(psns, domainName);
		ns.sendNotificationForAnonymiseEntries(notificationClientID, domainName, psns, result);
		LOGGER.debug( "{} psns for domain {} anonymised", psns.size(), domainName);
		return result;
	}

	/**
	 * @see PSNManager#isAnonym(String)
	 */
	protected boolean isAnonym(String value)
			throws InvalidParameterException
	{
		LOGGER.debug("isAnonym for value {}", value);
		checkParameter(value, VALUE);
		boolean result = cache.isAnonym(value);
		LOGGER.debug("{} is {} anonym", value, result ? "an" : "no");
		return result;
	}

	/**
	 * @see PSNManager#isAnonymised(String, String)
	 */
	protected boolean isAnonymised(String psn, String domainName)
			throws InvalidParameterException, InvalidPSNException, UnknownDomainException, PSNNotFoundException
	{
		LOGGER.debug("isAnonymised for psn {} within domain {}", psn, domainName);
		checkParameter(psn, "psn");
		checkParameter(domainName, DOMAIN_NAME);
		boolean result = cache.isAnonymised(psn, domainName);
		LOGGER.debug("{} is {}anonymised within {}", psn, result ? "" : "not ", domainName);
		return result;
	}

	/**
	 * @see PSNManager#deleteEntry(String, String)
	 * @see PSNManagerWithNotification#deleteEntry(String, String, String)
	 */
	protected void deleteEntry(String notificationClientID, String value, String domainName)
			throws DeletionForbiddenException, InvalidParameterException, UnknownDomainException, UnknownValueException
	{
		deleteValue("deleteEntry", notificationClientID, value, domainName, allowEntryMethodsForMPSNDomains());
	}

	/**
	 * @see PSNManager#deleteEntries(Set, String)
	 * @see PSNManagerWithNotification#deleteEntries(String, Set, String)
	 */
	protected Map<String, DeletionResult> deleteEntries(String notificationClientID, Set<String> values, String domainName)
			throws DeletionForbiddenException, InvalidParameterException, UnknownDomainException
	{
		return deleteValues("deleteEntries", notificationClientID, values, domainName, allowEntryMethodsForMPSNDomains());
	}

	/**
	 * @see PSNManager#deleteAllEntriesForValue(String, String)
	 * @see PSNManagerWithNotification#deleteAllEntriesForValue(String, String, String)
	 */
	protected void deleteAllEntriesForValue(String notificationClientID, String value, String domainName)
			throws DeletionForbiddenException, InvalidParameterException, UnknownDomainException, UnknownValueException
	{
		deleteValue("deleteAllEntriesForValue", notificationClientID, value, domainName, true);
	}

	/**
	 * @see PSNManager#deleteAllEntriesForValues(Set, String)
	 * @see PSNManagerWithNotification#deleteAllEntriesForValues(String, Set, String)
	 */
	protected Map<String, DeletionResult> deleteAllEntriesForValues(String notificationClientID, Set<String> values, String domainName)
			throws DeletionForbiddenException, InvalidParameterException, UnknownDomainException
	{
		return deleteValues("deleteAllEntriesForValues", notificationClientID, values, domainName, true);
	}

	/**
	 * @see #deleteEntry(String, String, String)
	 * @see #deleteAllEntriesForValue(String, String, String)
	 */
	private void deleteValue(String method, String notificationClientID, String value, String domainName, boolean allowMultiPSN)
			throws DeletionForbiddenException, InvalidParameterException, UnknownDomainException, UnknownValueException
	{
		debug("{} {}for value {} from domain {}", method, notificationClientID, value, domainName);
		checkParameter(value, VALUE);
		checkParameter(domainName, DOMAIN_NAME);
		if (!allowMultiPSN)
		{
			checkSinglePsnDomain(domainName);
		}
		cache.deleteValue(value, domainName);
		ns.sendNotificationForDeleteEntry(notificationClientID, domainName, value);
		LOGGER.debug("value-pseudonym-pair(s) for value {} removed from domain {}", value, domainName);
	}

	/**
	 * @see #deleteEntries(String, Set, String)
	 * @see #deleteAllEntriesForValues(String, Set, String)
	 */
	private Map<String, DeletionResult> deleteValues(String method, String notificationClientID, Set<String> values, String domainName, boolean allowMultiPSN)
			throws DeletionForbiddenException, InvalidParameterException, UnknownDomainException
	{
		debug("{} {}for {} values from domain {}", method, notificationClientID, values.size(), domainName);
		if (!allowMultiPSN)
		{
			cache.assertThatPSNsAreDeletable(domainName);
			checkSinglePsnDomain(domainName);
		}
		Map<String, DeletionResult> result = cache.deleteValues(values, domainName);
		ns.sendNotificationForDeleteEntries(notificationClientID, domainName, values, result);
		LOGGER.debug("deleted {} entries from domain {}", values.size(), domainName);
		return result;
	}

	/**
	 * @see PSNManager#deletePseudonym(String, String)
	 * @see PSNManagerWithNotification#deletePseudonym(String, String, String)
	 */
	protected void deletePseudonym(String notificationClientID, String psn, String domainName)
			throws DeletionForbiddenException, InvalidParameterException, UnknownDomainException, UnknownValueException, InvalidPSNException, PSNNotFoundException
	{
		debug("{} {}for psn {} from domain {}", "deletePseudonym", notificationClientID, psn, domainName);
		checkParameter(psn, VALUE);
		checkParameter(domainName, DOMAIN_NAME);
		cache.deletePseudonym(psn, domainName);
		ns.sendNotificationForDeleteEntry(notificationClientID, domainName, psn);
		LOGGER.debug("value-pseudonym-pair(s) for psn {} removed from domain {}", psn, domainName);
	}

	/**
	 * @see PSNManager#deletePseudonyms(Set, String)
	 * @see PSNManagerWithNotification#deletePseudonyms(String, Set, String)
	 */
	protected Map<String, DeletionResult> deletePseudonyms(String notificationClientID, Set<String> psns, String domainName)
			throws DeletionForbiddenException, InvalidParameterException, UnknownDomainException
	{
		debug("{} {}for {} psns from domain {}", "deletePseudonyms", notificationClientID, psns.size(), domainName);
		Map<String, DeletionResult> result = cache.deletePseudonyms(psns, domainName);
		ns.sendNotificationForDeleteEntries(notificationClientID, domainName, psns, result);
		LOGGER.debug("deleted {} psns from domain {}", psns.size(), domainName);
		return result;
	}

	/**
	 * @see PSNManager#validatePSN(String, String)
	 */
	protected void validatePSN(String psn, String domainName)
			throws InvalidParameterException, InvalidPSNException, UnknownDomainException
	{
		LOGGER.debug("validatePSN {} within domain {}", psn, domainName);
		checkParameter(psn, "psn");
		checkParameter(domainName, DOMAIN_NAME);
		cache.validatePSN(psn, domainName);
		LOGGER.debug("{} within domain {} is valid", psn, domainName);
	}

	/**
	 * @see PSNManager#insertValuePseudonymPair(String, String, String)
	 * @see PSNManagerWithNotification#insertValuePseudonymPair(String, String, String, String)
	 */
	protected void insertValuePseudonymPair(String notificationClientID, String value, String pseudonym, String domainName)
			throws InsertPairException, InvalidParameterException, UnknownDomainException
	{
		debug("{} {}for value {} in domain {}", "insertValuePseudonymPair", notificationClientID, value, domainName);
		checkParameter(value, VALUE);
		checkParameter(pseudonym, "pseudonym");
		checkParameter(domainName, DOMAIN_NAME);
		cache.insertValuePseudonymPair(value, pseudonym, domainName);
		ns.sendNotificationForInsertValuePseudonymPair(notificationClientID, domainName, value, pseudonym);
		LOGGER.debug("persisted pseudonym for {} in domain {}", value, domainName);
	}

	/**
	 * @see PSNManager#insertValuePseudonymPairs(List, String)
	 * @see PSNManagerWithNotification#insertValuePseudonymPairs(String, List, String)
	 */
	protected List<InsertPairExceptionDTO> insertValuePseudonymPairs(String notificationClientID, List<StringPair> pairs, String domainName)
			throws InvalidParameterException, UnknownDomainException
	{
		debug("{} {}for {} value-pseudonym pairs in domain {}", "insertValuePseudonymPairs", notificationClientID, pairs.size(), domainName);
		checkParameter(pairs, "pairs");
		checkParameter(domainName, DOMAIN_NAME);
		List<Entry<String, String>> entries = Pair.toMapEntries(pairs);
		List<InsertPairExceptionDTO> result = cache.insertValuePseudonymPairs(entries, domainName);
		ns.sendNotificationForInsertValuePseudonymPairs(notificationClientID, domainName, entries, result);
		LOGGER.debug("inserted {} value-pseudonym pairs in domain {}. {} errors occurred", pairs.size() - result.size(), domainName, result.size());
		return result;
	}

	/**
	 * @see PSNManager#getPSNTreeForPSN(String, String)
	 */
	protected PSNTreeDTO getPSNTreeForPSN(String psn, String domainName)
			throws InvalidParameterException, InvalidPSNException, PSNNotFoundException, UnknownDomainException, ValueIsAnonymisedException
	{
		LOGGER.debug("getPSNTreeForPSN for {} within domain {}", psn, domainName);
		checkParameter(psn, "psn");
		checkParameter(domainName, DOMAIN_NAME);
		PSNTreeDTO result = cache.getPSNTreeForPSN(psn, domainName);
		LOGGER.debug("psn tree created");
		return result;
	}

	/**
	 * @see PSNManager#getPSNNetFor(String)
	 */
	protected PSNNetDTO getPSNNetFor(String valueOrPSN) throws InvalidParameterException
	{
		LOGGER.debug("getPSNNetFor for value or psn {}", valueOrPSN);
		checkParameter(valueOrPSN, "valueOrPSN");
		PSNNetDTO result = cache.getPSNNetFor(valueOrPSN);
		LOGGER.debug("psn net created");
		return result;
	}
}
