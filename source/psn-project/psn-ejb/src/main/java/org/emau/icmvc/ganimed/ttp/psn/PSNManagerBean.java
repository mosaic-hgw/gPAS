package org.emau.icmvc.ganimed.ttp.psn;

/*-
 * ###license-information-start###
 * gPAS - a Generic Pseudonym Administration Service
 * __
 * Copyright (C) 2013 - 2022 Independent Trusted Third Party of the University Medicine Greifswald
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

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

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

@WebService(name = "gpasService")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@Stateless
@Remote(PSNManager.class)
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class PSNManagerBean extends GPASServiceBase implements PSNManager
{
	private static final Logger LOGGER = LogManager.getLogger(PSNManagerBean.class);

	@Override
	public String getOrCreatePseudonymFor(String value, String domainName) throws DBException, DomainIsFullException, InvalidParameterException, UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("getOrCreatePseudonymFor for value " + value + " within domain " + domainName);
		}
		checkParameter(value, "value");
		checkParameter(domainName, "domainName");
		String result = cache.getOrCreatePseudonymFor(value, domainName);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("getOrCreatePseudonymFor for value " + value + " within domain " + domainName + " succeed");
		}
		return result;
	}

	@Override
	public String getPseudonymFor(String value, String domainName) throws InvalidParameterException, UnknownDomainException, UnknownValueException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("getPseudonymFor for value " + value + " within domain " + domainName);
		}
		checkParameter(value, "value");
		checkParameter(domainName, "domainName");
		String result = cache.getPseudonymFor(value, domainName);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("getPseudonymFor for value " + value + " within domain " + domainName + " succeed");
		}
		return result;
	}

	@Override
	public void anonymiseEntry(String value, String domainName) throws DBException, InvalidParameterException, UnknownDomainException, UnknownValueException, ValueIsAnonymisedException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("anonymiseEntry for domain " + domainName);
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
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("entry for domain " + domainName + " anonymised");
		}
	}

	@Override
	public Map<String, AnonymisationResult> anonymiseEntries(Set<String> values, String domainName) throws DBException, InvalidParameterException, UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("anonymiseEntries for " + values.size() + " for domain " + domainName);
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
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug(values.size() + " entries for domain " + domainName + " anonymised");
		}
		return result;
	}

	@Override
	public boolean isAnonym(String value) throws InvalidParameterException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("isAnonym for value " + value);
		}
		checkParameter(value, "value");
		boolean result = cache.isAnonym(value);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug(value + " is " + (result ? "an" : "no") + " anonym");
		}
		return result;
	}

	@Override
	public boolean isAnonymised(String psn, String domainName) throws InvalidParameterException, InvalidPSNException, UnknownDomainException, PSNNotFoundException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("isAnonymised for psn " + psn + " within domain " + domainName);
		}
		checkParameter(psn, "psn");
		checkParameter(domainName, "domainName");
		boolean result = cache.isAnonymised(psn, domainName);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug(psn + " is " + (result ? "" : "not") + " anonymised within " + domainName);
		}
		return result;
	}

	@Override
	public void deleteEntry(String value, String domainName) throws DeletionForbiddenException, InvalidParameterException, UnknownDomainException, UnknownValueException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("deleteEntry for value " + value + " from domain " + domainName);
		}
		checkParameter(value, "value");
		checkParameter(domainName, "domainName");
		cache.deleteEntry(value, domainName);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("value-pseudonym-pair for value " + value + " removed from domain " + domainName);
		}
	}

	@Override
	public Map<String, DeletionResult> deleteEntries(Set<String> values, String domainName) throws DeletionForbiddenException, InvalidParameterException, UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("deleteEntries for " + values.size() + " values from domain " + domainName);
		}
		Map<String, DeletionResult> result = cache.deleteEntries(values, domainName);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("deleted " + values.size() + " entries from domain " + domainName);
		}
		return result;
	}

	@Override
	public void validatePSN(String psn, String domainName) throws InvalidParameterException, InvalidPSNException, UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("validatePSN " + psn + " within domain " + domainName);
		}
		checkParameter(psn, "psn");
		checkParameter(domainName, "domainName");
		cache.validatePSN(psn, domainName);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug(psn + " within domain " + domainName + " is valid");
		}
	}

	@Override
	public String getValueFor(String psn, String domainName)
			throws InvalidPSNException, PSNNotFoundException, InvalidParameterException, UnknownDomainException, ValueIsAnonymisedException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("getValueFor for pseudonym " + psn + " within domain " + domainName);
		}
		checkParameter(psn, "psn");
		checkParameter(domainName, "domainName");
		String result = cache.getValueFor(psn, domainName);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("found value for pseudonym " + psn + " within domain " + domainName);
		}
		return result;
	}

	@Override
	public Map<String, String> getOrCreatePseudonymForList(Set<String> values, String domainName)
			throws DBException, DomainIsFullException, InvalidParameterException, UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("getOrCreatePseudonymForList for " + values.size() + " values within domain " + domainName);
		}
		checkParameter(values, "values");
		checkParameter(domainName, "domainName");
		Map<String, String> result = cache.getOrCreatePseudonymForList(values, domainName);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("proceeded " + result.size() + " values");
		}
		return result;
	}

	@Override
	public Map<String, String> getValueForList(Set<String> psnList, String domainName) throws InvalidParameterException, UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("getValueForList for " + psnList.size() + " pseudonyms within domain " + domainName);
		}
		checkParameter(psnList, "psnList");
		checkParameter(domainName, "domainName");
		Map<String, String> result = cache.getValueForList(psnList, domainName);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("proceeded " + result.size() + " pseudonyms");
		}
		return result;
	}

	@Override
	public Map<String, String> getPseudonymForList(Set<String> values, String domainName) throws InvalidParameterException, UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("getPseudonymForList for " + values.size() + " values within domain " + domainName);
		}
		checkParameter(values, "values");
		checkParameter(domainName, "domainName");
		Map<String, String> result = cache.getPseudonymForList(values, domainName);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("proceeded " + result.size() + " values");
		}
		return result;
	}

	@Override
	public Map<String, String> getPseudonymForValuePrefix(String valuePrefix, String domainName) throws InvalidParameterException, UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("getPseudonymForValuePrefix for " + valuePrefix + " within domain " + domainName);
		}
		checkParameter(valuePrefix, "valuePrefix");
		checkParameter(domainName, "domainName");
		Map<String, String> result = cache.getPseudonymForValuePrefix(valuePrefix, domainName);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("found " + result.size() + " pseudonyms");
		}
		return result;
	}

	@Override
	public void insertValuePseudonymPair(String value, String pseudonym, String domainName) throws InsertPairException, InvalidParameterException, UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("insertValuePseudonymPair for value " + value + " in domain " + domainName);
		}
		checkParameter(value, "value");
		checkParameter(pseudonym, "pseudonym");
		checkParameter(domainName, "domainName");
		cache.insertValuePseudonymPair(value, pseudonym, domainName);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("persisted pseudonym for " + value + " in domain " + domainName);
		}
	}

	@Override
	public List<InsertPairExceptionDTO> insertValuePseudonymPairs(Map<String, String> pairs, String domainName) throws InvalidParameterException, UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("insertValuePseudonymPairs for " + pairs.size() + " value-pseudonym pairs in domain " + domainName);
		}
		checkParameter(pairs, "pairs");
		checkParameter(domainName, "domainName");
		List<InsertPairExceptionDTO> result = cache.insertValuePseudonymPairs(pairs, domainName);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("inserted " + (pairs.size() - result.size()) + " value-pseudonym pairs in domain " + domainName + ". " + result.size() + " errors occurred");
		}
		return result;
	}

	@Override
	public PSNTreeDTO getPSNTreeForPSN(String psn, String domainName)
			throws InvalidParameterException, InvalidPSNException, PSNNotFoundException, UnknownDomainException, ValueIsAnonymisedException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("getPSNTreeForPSN for " + psn + " within domain " + domainName);
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

	@Override
	public PSNNetDTO getPSNNetFor(String valueOrPSN) throws InvalidParameterException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("getPSNNetFor for value or psn " + valueOrPSN);
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
