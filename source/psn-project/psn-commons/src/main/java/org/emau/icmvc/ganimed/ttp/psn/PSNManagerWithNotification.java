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

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import org.emau.icmvc.ganimed.ttp.psn.config.DomainProperties;
import org.emau.icmvc.ganimed.ttp.psn.dto.InsertPairExceptionDTO;
import org.emau.icmvc.ganimed.ttp.psn.enums.AnonymisationResult;
import org.emau.icmvc.ganimed.ttp.psn.enums.DeletionResult;
import org.emau.icmvc.ganimed.ttp.psn.enums.InsertPairError;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DBException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DeletionForbiddenException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DomainIsFullException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InsertPairException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownValueException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.ValueIsAnonymisedException;

@WebService
public interface PSNManagerWithNotification
{
	/**
	 * returns a pseudonym for the given value
	 *
	 * @param value
	 *            string for which the pseudonym should be retrieved / generated
	 * @param domainName
	 *            for / in which domain should the pair value-pseudonym be retrieved / stored
	 * @return a map with pairs original value -> pseudonym
	 * @throws DBException
	 *             if an error occurred while persisting a new entry
	 * @throws DomainIsFullException
	 *             if a pseudonym for the given value couldn't be found, has therefor to be created but no more pseudonyms could be created for the given domain
	 * @throws InvalidParameterException
	 *             if one of the required parameters is not set
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	@XmlElement(name = "psn")
	String getOrCreatePseudonymFor(
			@XmlElement(required = false) @WebParam(name = "notificationClientID") String notificationClientID,
			@XmlElement(required = true) @WebParam(name = "value") String value,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws DBException, DomainIsFullException, InvalidParameterException, UnknownDomainException;

	/**
	 * list version of {@link PSNManagerWithNotification#getOrCreatePseudonymFor(String, String, String)}
	 * returns a map&lt;value, psn&gt; for all values
	 *
	 * @param values
	 *            list of strings for which pseudonyms should be retrieved / generated
	 * @param domainName
	 *            for / in which domain should the pair value-pseudonym be retrieved / stored
	 * @return a map with pairs original value -> pseudonym
	 * @throws DBException
	 *             if an error occurred while persisting a new entry
	 * @throws DomainIsFullException
	 *             if a pseudonym for the given value couldn't be found, has therefor to be created but no more pseudonyms could be created for the given domain
	 * @throws InvalidParameterException
	 *             if one of the required parameters is not set
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "valuePsnMap")
	Map<String, String> getOrCreatePseudonymForList(
			@XmlElement(required = false) @WebParam(name = "notificationClientID") String notificationClientID,
			@XmlElement(required = true) @WebParam(name = "values") Set<String> values,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws DBException, DomainIsFullException, InvalidParameterException, UnknownDomainException;

	/**
	 * deletes the assignment value - pseudonym from the db (replaces "value" with
	 * "###_anonym_###_randString_###_anonym_###")
	 *
	 * @param value
	 *            the value which should be anonymised
	 * @param domainName
	 *            for which domain should the given value be anonymised
	 * @throws DBException
	 *             if an error occurred while anonymising the entry
	 * @throws InvalidParameterException
	 *             if one of the required parameters is not set
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 * @throws UnknownValueException
	 *             if the value is not found within the given domain
	 * @throws ValueIsAnonymisedException
	 *             if the value is already anonymised
	 */
	void anonymiseEntry(
			@XmlElement(required = false) @WebParam(name = "notificationClientID") String notificationClientID,
			@XmlElement(required = true) @WebParam(name = "value") String value,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws DBException, InvalidParameterException, UnknownDomainException, UnknownValueException, ValueIsAnonymisedException;

	/**
	 * deletes the assignments value - pseudonym from the db (replaces "value" with
	 * "###_anonym_###_randString_###_anonym_###")
	 *
	 * @param values
	 *            the values which should be anonymised
	 * @param domainName
	 *            for which domain should the given value be anonymised
	 * @return the success status
	 * @throws DBException
	 *             if an error occurred while anonymising the entry
	 * @throws InvalidParameterException
	 *             if one of the required parameters is not set
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "anonymisationResult")
	Map<String, AnonymisationResult> anonymiseEntries(
			@XmlElement(required = false) @WebParam(name = "notificationClientID") String notificationClientID,
			@XmlElement(required = true) @WebParam(name = "values") Set<String> values,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws DBException, InvalidParameterException, UnknownDomainException;

	/**
	 * deletes the given value and corresponding pseudonym from the db
	 *
	 * @param value
	 *            the value which should be delete
	 * @param domainName
	 *            within which domain should the assignment be deleted
	 * @throws DeletionForbiddenException
	 *             when it's not allowed to delete value-pseudonym-assignments within a domain<br>
	 *             see {@link DomainProperties#PSNS_DELETABLE}
	 * @throws InvalidParameterException
	 *             if one of the required parameters is not set
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 * @throws UnknownValueException
	 *             if the given value is not found within the db for that domain
	 * @author schuldtr
	 */
	void deleteEntry(
			@XmlElement(required = false) @WebParam(name = "notificationClientID") String notificationClientID,
			@XmlElement(required = true) @WebParam(name = "value") String value,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws DeletionForbiddenException, InvalidParameterException, UnknownDomainException, UnknownValueException;

	/**
	 * deletes the given values and corresponding pseudonyms from the db
	 *
	 * @param values
	 *            the values which should be delete
	 * @param domainName
	 *            within which domain should the assignments be deleted
	 * @return a map with all values and the corresponding success status
	 * @throws DeletionForbiddenException
	 *             when it's not allowed to delete value-pseudonym-assignments within a domain<br>
	 *             see {@link DomainProperties#PSNS_DELETABLE}
	 * @throws InvalidParameterException
	 *             if one of the required parameters is not set
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "deletionResult")
	Map<String, DeletionResult> deleteEntries(
			@XmlElement(required = false) @WebParam(name = "notificationClientID") String notificationClientID,
			@XmlElement(required = true) @WebParam(name = "values") Set<String> values,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws DeletionForbiddenException, InvalidParameterException, UnknownDomainException;


	/**
	 * insert a value - pseudonym pair<br>
	 * value is expected to be a valid pseudonym
	 *
	 * @param value
	 *            the value for which the pseudonym should be inserted
	 * @param pseudonym
	 *            the pseudonym which should be inserted for the given value
	 * @param domainName
	 *            for which domain should the given pairs be stored
	 * @throws InsertPairException
	 *             if an error occurred while persisting the given pair (see {@link InsertPairError})
	 * @throws InvalidParameterException
	 *             if one of the required parameters is not set
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	void insertValuePseudonymPair(
			@XmlElement(required = false) @WebParam(name = "notificationClientID") String notificationClientID,
			@XmlElement(required = true) @WebParam(name = "value") String value,
			@XmlElement(required = true) @WebParam(name = "pseudonym") String pseudonym,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws InsertPairException, InvalidParameterException, UnknownDomainException;

	/**
	 * insert value - pseudonym pairs<br>
	 * values within the map are expected to be valid pseudonyms
	 *
	 * @param pairs
	 *            map of pairs which should be stored
	 * @param domainName
	 *            for which domain should the given pairs be stored
	 * @return a list with all occurred exceptions, see {@link InsertPairExceptionDTO}
	 * @throws InvalidParameterException
	 *             if one of the required parameters is not set
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "exceptionList")
	List<InsertPairExceptionDTO> insertValuePseudonymPairs(
			@XmlElement(required = false) @WebParam(name = "notificationClientID") String notificationClientID,
			@XmlElement(required = true) @WebParam(name = "pairs") Map<String, String> pairs,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws InvalidParameterException, UnknownDomainException;
}
