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
import org.emau.icmvc.ganimed.ttp.psn.enums.AnonymisationResult;
import org.emau.icmvc.ganimed.ttp.psn.enums.DeletionResult;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DBException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DeletionForbiddenException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DomainIsFullException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InsertPairException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownValueException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.ValueIsAnonymisedException;

@WebService(name = "gpasServiceWithNotification")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@Stateless
@Remote(PSNManagerWithNotification.class)
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class PSNManagerWithNotificationBean extends PSNManagerBase implements PSNManagerWithNotification
{
	@Override
	public String getOrCreatePseudonymFor(String notificationClientID, String value, String domainName)
			throws DBException, DomainIsFullException, InvalidParameterException, UnknownDomainException
	{
		return super.getOrCreatePseudonymFor(notificationClientID, value, domainName);
	}

	@Override
	public Map<String, String> getOrCreatePseudonymForList(String notificationClientID, Set<String> values, String domainName)
			throws DBException, DomainIsFullException, InvalidParameterException, UnknownDomainException
	{
		return super.getOrCreatePseudonymForList(notificationClientID, values, domainName);
	}

	@Override
	public void anonymiseEntry(String notificationClientID, String value, String domainName)
			throws DBException, InvalidParameterException, UnknownDomainException, UnknownValueException, ValueIsAnonymisedException
	{
		super.anonymiseEntry(notificationClientID, value, domainName);
	}

	@Override
	public Map<String, AnonymisationResult> anonymiseEntries(String notificationClientID, Set<String> values, String domainName)
			throws DBException, InvalidParameterException, UnknownDomainException
	{
		return super.anonymiseEntries(notificationClientID, values, domainName);
	}

	@Override
	public void deleteEntry(String notificationClientID, String value, String domainName)
			throws DeletionForbiddenException, InvalidParameterException, UnknownDomainException, UnknownValueException
	{
		super.deleteEntry(notificationClientID, value, domainName);
	}

	@Override
	public Map<String, DeletionResult> deleteEntries(String notificationClientID, Set<String> values, String domainName)
			throws DeletionForbiddenException, InvalidParameterException, UnknownDomainException
	{
		return super.deleteEntries(notificationClientID, values, domainName);
	}

	@Override
	public void insertValuePseudonymPair(String notificationClientID, String value, String pseudonym, String domainName)
			throws InsertPairException, InvalidParameterException, UnknownDomainException
	{
		super.insertValuePseudonymPair(notificationClientID, value, pseudonym, domainName);
	}

	@Override
	public List<InsertPairExceptionDTO> insertValuePseudonymPairs(String notificationClientID, Map<String, String> pairs, String domainName)
			throws InvalidParameterException, UnknownDomainException
	{
		return super.insertValuePseudonymPairs(notificationClientID, pairs, domainName);
	}
}
