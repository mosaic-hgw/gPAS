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

import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.ejb.ConcurrencyManagement;
import jakarta.ejb.ConcurrencyManagementType;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
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
import org.emau.icmvc.ganimed.ttp.psn.utils.StringPair;

@WebService(name = "gpasService")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@Stateless
@Remote(PSNManager.class)
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class PSNManagerBean extends PSNManagerBase implements PSNManager
{
	@Override
	public String createPseudonymFor(String value, String domainName)
			throws DomainIsFullException, InvalidParameterException, UnknownDomainException
	{
		return super.createPseudonymFor(null, value, domainName);
	}

	@Override
	public List<String> createPseudonymsFor(String value, String domainName, int number)
			throws DomainIsFullException, InvalidParameterException, UnknownDomainException
	{
		return super.createPseudonymsFor(null, value, domainName, number);
	}

	@Override
	public Map<String, String> createPseudonymForList(Set<String> values, String domainName)
			throws DomainIsFullException, InvalidParameterException, UnknownDomainException
	{
		return super.createPseudonymForList(null, values, domainName);
	}

	@Override
	public Map<String, List<String>> createPseudonymsForList(Set<String> values, String domainName, int number)
			throws DomainIsFullException, InvalidParameterException, UnknownDomainException
	{
		return super.createPseudonymsForList(null, values, domainName, number);
	}

	@Override
	public String getOrCreatePseudonymFor(String value, String domainName)
			throws DomainIsFullException, InvalidParameterException, UnknownDomainException
	{
		return super.getOrCreatePseudonymFor(null, value, domainName);
	}

	@Override
	public List<String> getOrCreatePseudonymsFor(String value, String domainName, int minNumber)
			throws DomainIsFullException, InvalidParameterException, UnknownDomainException
	{
		return super.getOrCreatePseudonymsFor(null, value, domainName, minNumber);
	}

	@Override
	public Map<String, String> getOrCreatePseudonymForList(Set<String> values, String domainName)
			throws DomainIsFullException, InvalidParameterException, UnknownDomainException
	{
		return super.getOrCreatePseudonymForList(null, values, domainName);
	}

	@Override
	public Map<String, List<String>> getOrCreatePseudonymsForList(Set<String> values, String domainName, int minNumber)
			throws DomainIsFullException, InvalidParameterException, UnknownDomainException
	{
		return super.getOrCreatePseudonymsForList(null, values, domainName, minNumber);
	}

	@Override
	public String getPseudonymFor(String value, String domainName)
			throws InvalidParameterException, UnknownDomainException, UnknownValueException
	{
		return super.getPseudonymFor(value, domainName);
	}

	@Override
	public List<String> getPseudonymsFor(String value, String domainName)
			throws InvalidParameterException, UnknownDomainException, UnknownValueException
	{
		return super.getPseudonymsFor(value, domainName);
	}

	@Override
	public Map<String, String> getPseudonymForList(Set<String> values, String domainName)
			throws InvalidParameterException, UnknownDomainException
	{
		return super.getPseudonymForList(values, domainName);
	}

	@Override
	public Map<String, List<String>> getPseudonymsForList(Set<String> values, String domainName)
			throws InvalidParameterException, UnknownDomainException
	{
		return super.getPseudonymsForList(values, domainName);
	}

	@Override
	public Map<String, String> getPseudonymForValuePrefix(String valuePrefix, String domainName)
			throws InvalidParameterException, UnknownDomainException
	{
		return super.getPseudonymForValuePrefix(valuePrefix, domainName);
	}

	@Override
	public Map<String, List<String>> getPseudonymsForValuePrefix(String valuePrefix, String domainName)
			throws InvalidParameterException, UnknownDomainException
	{
		return super.getPseudonymsForValuePrefix(valuePrefix, domainName);
	}

	@Override
	public void anonymiseEntry(String value, String domainName)
			throws DBException, InvalidParameterException, UnknownDomainException, UnknownValueException, ValueIsAnonymisedException
	{
		super.anonymiseEntry(null, value, domainName);
	}

	@Override
	public Map<String, AnonymisationResult> anonymiseEntries(Set<String> values, String domainName)
			throws DBException, InvalidParameterException, UnknownDomainException
	{
		return super.anonymiseEntries(null, values, domainName);
	}

	@Override
	public void anonymiseAllEntriesForValue(String value, String domainName)
			throws DBException, InvalidParameterException, UnknownDomainException, UnknownValueException, ValueIsAnonymisedException
	{
		super.anonymiseAllEntriesForValue(null, value, domainName);
	}

	@Override
	public Map<String, AnonymisationResult> anonymiseAllEntriesForValues(Set<String> values, String domainName)
			throws DBException, InvalidParameterException, UnknownDomainException
	{
		return super.anonymiseAllEntriesForValues(null, values, domainName);
	}

	@Override
	public void anonymisePseudonym(String psn, String domainName)
			throws DBException, InvalidParameterException, UnknownDomainException, InvalidPSNException, PSNNotFoundException
	{
		super.anonymisePseudonym(null, psn, domainName);
	}

	@Override
	public Map<String, AnonymisationResult> anonymisePseudonyms(Set<String> psns, String domainName)
			throws DBException, InvalidParameterException, UnknownDomainException
	{
		return super.anonymisePseudonyms(null, psns, domainName);
	}

	@Override
	public boolean isAnonym(String value)
			throws InvalidParameterException
	{
		return super.isAnonym(value);
	}

	@Override
	public boolean isAnonymised(String psn, String domainName)
			throws InvalidParameterException, InvalidPSNException, UnknownDomainException, PSNNotFoundException
	{
		return super.isAnonymised(psn, domainName);
	}

	@Override
	public void deleteEntry(String value, String domainName)
			throws DeletionForbiddenException, InvalidParameterException, UnknownDomainException, UnknownValueException
	{
		super.deleteEntry(null, value, domainName);
	}

	@Override
	public Map<String, DeletionResult> deleteEntries(Set<String> values, String domainName)
			throws DeletionForbiddenException, InvalidParameterException, UnknownDomainException
	{
		return super.deleteEntries(null, values, domainName);
	}

	@Override
	public void deleteAllEntriesForValue(String value, String domainName)
			throws DeletionForbiddenException, InvalidParameterException, UnknownDomainException, UnknownValueException
	{
		super.deleteAllEntriesForValue(null, value, domainName);
	}

	@Override
	public Map<String, DeletionResult> deleteAllEntriesForValues(Set<String> values, String domainName)
			throws DeletionForbiddenException, InvalidParameterException, UnknownDomainException
	{
		return super.deleteAllEntriesForValues(null, values, domainName);
	}

	@Override
	public void deletePseudonym(String psn, String domainName)
			throws DeletionForbiddenException, InvalidParameterException, UnknownDomainException, UnknownValueException, InvalidPSNException, PSNNotFoundException
	{
		super.deletePseudonym(null, psn, domainName);
	}

	@Override
	public Map<String, DeletionResult> deletePseudonyms(Set<String> psns, String domainName)
			throws DeletionForbiddenException, InvalidParameterException, UnknownDomainException
	{
		return super.deletePseudonyms(null, psns, domainName);
	}

	@Override
	public void validatePSN(String psn, String domainName)
			throws InvalidParameterException, InvalidPSNException, UnknownDomainException
	{
		super.validatePSN(psn, domainName);
	}

	@Override
	public String getValueFor(String psn, String domainName)
			throws InvalidParameterException, InvalidPSNException, PSNNotFoundException, UnknownDomainException, ValueIsAnonymisedException
	{
		return super.getValueFor(psn, domainName);
	}

	@Override
	public Map<String, String> getValueForList(Set<String> psnList, String domainName)
			throws InvalidParameterException, UnknownDomainException
	{
		return super.getValueForList(psnList, domainName);
	}

	@Override
	public void insertValuePseudonymPair(String value, String pseudonym, String domainName)
			throws InsertPairException, InvalidParameterException, UnknownDomainException
	{
		super.insertValuePseudonymPair(null, value, pseudonym, domainName);
	}

	@Override
	public List<InsertPairExceptionDTO> insertValuePseudonymPairs(List<StringPair> pairs, String domainName)
			throws InvalidParameterException, UnknownDomainException
	{
		return super.insertValuePseudonymPairs(null, pairs, domainName);
	}

	@Override
	public PSNTreeDTO getPSNTreeForPSN(String psn, String domainName)
			throws InvalidParameterException, InvalidPSNException, PSNNotFoundException, UnknownDomainException, ValueIsAnonymisedException
	{
		return super.getPSNTreeForPSN(psn, domainName);
	}

	@Override
	public PSNNetDTO getPSNNetFor(String valueOrPSN)
			throws InvalidParameterException
	{
		return super.getPSNNetFor(valueOrPSN);
	}
}
