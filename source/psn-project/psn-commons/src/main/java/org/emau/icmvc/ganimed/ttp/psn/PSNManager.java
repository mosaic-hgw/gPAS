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
import org.emau.icmvc.ganimed.ttp.psn.dto.PSNNetDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.PSNTreeDTO;
import org.emau.icmvc.ganimed.ttp.psn.enums.AnonymisationResult;
import org.emau.icmvc.ganimed.ttp.psn.enums.DeletionResult;
import org.emau.icmvc.ganimed.ttp.psn.enums.InsertPairError;
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

@WebService
public interface PSNManager
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
	String getOrCreatePseudonymFor(@XmlElement(required = true) @WebParam(name = "value") String value,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws DBException, DomainIsFullException, InvalidParameterException, UnknownDomainException;

	/**
	 * list version of {@link PSNManager#getOrCreatePseudonymFor(String, String)}
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
			@XmlElement(required = true) @WebParam(name = "values") Set<String> values,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws DBException, DomainIsFullException, InvalidParameterException, UnknownDomainException;

	/**
	 * returns the pseudonym to the given value
	 *
	 * @param value
	 *            the string for which a pseudonym should be retrieved
	 * @param domainName
	 *            for which domain should the pseudonym be retrieved
	 * @return the pseudonym
	 * @throws InvalidParameterException
	 *             if one of the required parameters is not set
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 * @throws UnknownValueException
	 *             if the given value is not found for the given domain
	 */
	@XmlElement(name = "psn")
	String getPseudonymFor(@XmlElement(required = true) @WebParam(name = "value") String value,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws InvalidParameterException, UnknownDomainException, UnknownValueException;

	/**
	 * list version of {@link PSNManager#getPseudonymFor(String, String)}<br>
	 * returns a map&lt;value, psn&gt; (with psn="*** VALUE NOT FOUND ***" for each given value
	 * which is unknown for the given domain)
	 *
	 * @param values
	 *            a list of values for which the pseudonyms should be retrieved
	 * @param domainName
	 *            for which domain should the pseudonyms be retrieved
	 * @return map of value-pseudonym pairs
	 * @throws InvalidParameterException
	 *             if one of the required parameters is not set
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "valuePsnMap")
	Map<String, String> getPseudonymForList(
			@XmlElement(required = true) @WebParam(name = "values") Set<String> values,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName) throws InvalidParameterException, UnknownDomainException;

	/**
	 * list version of {@link PSNManager#getPseudonymFor(String, String)}<br>
	 * returns a map&lt;value, psn&gt; for all values which starts with the given string
	 *
	 * @param valuePrefix
	 *            the prefix of all values for which the pseudonyms should be retrieved
	 * @param domainName
	 *            for which domain should the pseudonyms be retrieved
	 * @return map of value-pseudonym pairs
	 * @throws InvalidParameterException
	 *             if one of the required parameters is not set
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "valuePsnMap")
	Map<String, String> getPseudonymForValuePrefix(
			@XmlElement(required = true) @WebParam(name = "valuePrefix") String valuePrefix,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName) throws InvalidParameterException, UnknownDomainException;

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
	void anonymiseEntry(@XmlElement(required = true) @WebParam(name = "value") String value,
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
			@XmlElement(required = true) @WebParam(name = "values") Set<String> values,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws DBException, InvalidParameterException, UnknownDomainException;

	/**
	 * is the given value an anonym?
	 *
	 * @param value
	 *            the value which should be tested
	 * @return true if the given value is an anonym, false else
	 * @throws InvalidParameterException
	 *             if one of the required parameters is not set
	 */
	boolean isAnonym(@XmlElement(required = true) @WebParam(name = "value") String value)
			throws InvalidParameterException;

	/**
	 * is the given psn anonymised?
	 *
	 * @param psn
	 *            the psn which should be tested
	 * @param domainName
	 *            for which domain the given psn should be tested
	 * @return true if the given psn is anonymised, false else
	 * @throws InvalidParameterException
	 *             if one of the required parameters is not set
	 * @throws InvalidPSNException
	 *             if the given PSN is not valid
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 * @throws PSNNotFoundException
	 *             if the psn is not found within the given domain
	 */
	boolean isAnonymised(@XmlElement(required = true) @WebParam(name = "psn") String psn,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws InvalidParameterException, InvalidPSNException, UnknownDomainException, PSNNotFoundException;

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
	void deleteEntry(@XmlElement(required = true) @WebParam(name = "value") String value,
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
			@XmlElement(required = true) @WebParam(name = "values") Set<String> values,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws DeletionForbiddenException, InvalidParameterException, UnknownDomainException;

	/**
	 * checks if the given string is a valid pseudonym
	 *
	 * @param psn
	 *            the pseudonym which should be checked
	 * @param domainName
	 *            for which domain should the given pseudonym be checked
	 * @throws InvalidParameterException
	 *             if one of the required parameters is not set
	 * @throws InvalidPSNException
	 *             if the given PSN is not valid
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	void validatePSN(@XmlElement(required = true) @WebParam(name = "psn") String psn,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws InvalidParameterException, InvalidPSNException, UnknownDomainException;

	/**
	 * searches the value for the given psn and domain; calls checkPSN(psn, domainName) before
	 * searching the value
	 *
	 * @param psn
	 *            for which pseudonym should the original value be retrieved
	 * @param domainName
	 *            for which domain should the original value to the given pseudonym be retrieved
	 * @return the original value
	 * @throws InvalidParameterException
	 *             if one of the required parameters is not set
	 * @throws InvalidPSNException
	 *             if the given PSN is not valid
	 * @throws PSNNotFoundException
	 *             if the given PSN is not found within the db for that domain
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 * @throws ValueIsAnonymisedException
	 *             if pseudonym is anonymised
	 */
	@XmlElement(name = "value")
	String getValueFor(@XmlElement(required = true) @WebParam(name = "psn") String psn,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws InvalidParameterException, InvalidPSNException, PSNNotFoundException, UnknownDomainException, ValueIsAnonymisedException;

	/**
	 * list version of {@link PSNManager#getValueFor(String, String)}<br>
	 * returns a map&lt;psn, value&gt; (with value="*** PSN NOT FOUND ***" for each given psn which
	 * is unknown for the given domain)
	 *
	 * @param psnList
	 *            list of pseudonyms for which the original values should be retrieved
	 * @param domainName
	 *            for which domain should the original value to the given pseudonym be retrieved
	 * @return a map with pairs pseudonym -> original value
	 * @throws InvalidParameterException
	 *             if one of the required parameters is not set
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "psnValueMap")
	Map<String, String> getValueForList(
			@XmlElement(required = true) @WebParam(name = "psnList") Set<String> psnList,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws InvalidParameterException, UnknownDomainException;

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
	void insertValuePseudonymPair(@XmlElement(required = true) @WebParam(name = "value") String value, @XmlElement(required = true) @WebParam(name = "pseudonym") String pseudonym,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName) throws InsertPairException, InvalidParameterException, UnknownDomainException;

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
	List<InsertPairExceptionDTO> insertValuePseudonymPairs(@XmlElement(required = true) @WebParam(name = "pairs") Map<String, String> pairs,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName) throws InvalidParameterException, UnknownDomainException;

	/**
	 * create psn tree<br>
	 * with all values that are somehow linked to the given psn
	 *
	 * @param psn
	 *            pseudonym that should be used to create the psn tree
	 * @param domainName
	 *            name of the domain for the given pseudonym
	 * @return a psn tree object with all psn connected to the given psn
	 * @throws InvalidParameterException
	 *             if one of the required parameters is not set
	 * @throws InvalidPSNException
	 *             if the given PSN is not valid
	 * @throws PSNNotFoundException
	 *             if the given PSN is not found within the db for that domain
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 * @throws ValueIsAnonymisedException
	 *             if the value is already anonymised
	 */
	@XmlElement(name = "psnTree")
	PSNTreeDTO getPSNTreeForPSN(@XmlElement(required = true) @WebParam(name = "psn") String psn,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws InvalidParameterException, InvalidPSNException, PSNNotFoundException, UnknownDomainException, ValueIsAnonymisedException;

	/**
	 * create psn net<br>
	 * with all values that are somehow linked to the given psn
	 *
	 * @param valueOrPSN
	 *            value or pseudonym that should be used to create the psn net
	 * @return a psn net object with all psn connected to the given psn / value
	 * @throws InvalidParameterException
	 *             if one of the required parameters is not set
	 */
	@XmlElement(name = "psnNet")
	PSNNetDTO getPSNNetFor(@XmlElement(required = true) @WebParam(name = "valueOrPSN") String valueOrPSN)
			throws InvalidParameterException;
}
