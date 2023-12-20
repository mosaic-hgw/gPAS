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

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

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

@WebService(name = "gpasService")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@Stateless
@Remote(PSNManager.class)
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class PSNManagerBean extends PSNManagerBase implements PSNManager
{
	@Override
	public String getOrCreatePseudonymFor(String value, String domainName)
			throws DBException, DomainIsFullException, InvalidParameterException, UnknownDomainException
	{
		return super.getOrCreatePseudonymFor(value, domainName);
	}

	@Override
	public Map<String, String> getOrCreatePseudonymForList(Set<String> values, String domainName)
			throws DBException, DomainIsFullException, InvalidParameterException, UnknownDomainException
	{
		return super.getOrCreatePseudonymForList(values, domainName);
	}

	@Override
	public String getPseudonymFor(String value, String domainName)
			throws InvalidParameterException, UnknownDomainException, UnknownValueException
	{
		return super.getPseudonymFor(value, domainName);
	}

	@Override
	public Map<String, String> getPseudonymForList(Set<String> values, String domainName)
			throws InvalidParameterException, UnknownDomainException
	{
		return super.getPseudonymForList(values, domainName);
	}

	@Override
	public Map<String, String> getPseudonymForValuePrefix(String valuePrefix, String domainName)
			throws InvalidParameterException, UnknownDomainException
	{
		return super.getPseudonymForValuePrefix(valuePrefix, domainName);
	}

	@Override
	public void anonymiseEntry(String value, String domainName)
			throws DBException, InvalidParameterException, UnknownDomainException, UnknownValueException, ValueIsAnonymisedException
	{
		super.anonymiseEntry(value, domainName);
	}

	@Override
	public Map<String, AnonymisationResult> anonymiseEntries(Set<String> values, String domainName)
			throws DBException, InvalidParameterException, UnknownDomainException
	{
		return super.anonymiseEntries(values, domainName);
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
		super.deleteEntry(value, domainName);
	}

	@Override
	public Map<String, DeletionResult> deleteEntries(Set<String> values, String domainName)
			throws DeletionForbiddenException, InvalidParameterException, UnknownDomainException
	{
		return super.deleteEntries(values, domainName);
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
		super.insertValuePseudonymPair(value, pseudonym, domainName);
	}

	@Override
	public List<InsertPairExceptionDTO> insertValuePseudonymPairs(Map<String, String> pairs, String domainName)
			throws InvalidParameterException, UnknownDomainException
	{
		return super.insertValuePseudonymPairs(pairs, domainName);
	}

	@Override
	public PSNTreeDTO getPSNTreeForPSN(String psn, String domainName)
			throws InvalidParameterException, InvalidPSNException, PSNNotFoundException, UnknownDomainException, ValueIsAnonymisedException
	{
		return super.getPSNTreeForPSN(psn, domainName);
	}

	@Override
	public PSNNetDTO getPSNNetFor(String valueOrPSN) throws InvalidParameterException
	{
		return super.getPSNNetFor(valueOrPSN);
	}
}
