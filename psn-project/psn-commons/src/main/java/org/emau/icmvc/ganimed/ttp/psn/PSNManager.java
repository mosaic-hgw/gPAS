package org.emau.icmvc.ganimed.ttp.psn;

/*
 * ###license-information-start###
 * gPAS - a Generic Pseudonym Administration Service
 * __
 * Copyright (C) 2013 - 2017 The MOSAIC Project - Institut fuer Community Medicine der
 * 							Universitaetsmedizin Greifswald - mosaic-projekt@uni-greifswald.de
 * 							concept and implementation
 * 							l. geidel
 * 							web client
 * 							g. weiher
 * 							a. blumentritt
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

import java.util.Set;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;

import org.emau.icmvc.ganimed.ttp.psn.dto.HashMapWrapper;
import org.emau.icmvc.ganimed.ttp.psn.dto.PSNTreeDTO;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DBException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DeletionForbiddenException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidGeneratorException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidPSNException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.PSNNotFoundException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownValueException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.ValueIsAnonymisedException;
import org.emau.icmvc.ganimed.ttp.psn.generator.GeneratorProperties;

@WebService
public interface PSNManager {

	/**
	 * returns the pseudonym to the given value
	 * <p>
	 * creates a new one, if no pseudonym for the given value exists within the db
	 * 
	 * @param value
	 *            the string for which a pseudonym should be retrieved / generated
	 * @param domain
	 *            for / in which domain should the pair value-pseudonym be retrieved / stored
	 * @return the pseudonym (existing or new generated)
	 * @throws DBException
	 *             if an error occurred while persisting a new entry
	 * @throws InvalidGeneratorException
	 *             if the generator can't be instantiated
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	public @XmlElement(required = true) String getOrCreatePseudonymFor(@XmlElement(required = true) @WebParam(name = "value") String value,
			@XmlElement(required = true) @WebParam(name = "domain") String domain)
			throws DBException, InvalidGeneratorException, UnknownDomainException;

	/**
	 * list version of {@link PSNManager#getOrCreatePseudonymFor(String, String)}
	 * 
	 * @param values
	 *            list of strings for which pseudonyms should be retrieved / generated
	 * @param domain
	 *            for / in which domain should the pair value-pseudonym be retrieved / stored
	 * @return a map with pairs original value -> pseudonym
	 * @throws DBException
	 *             if an error occurred while persisting a new entry
	 * @throws InvalidGeneratorException
	 *             if the generator can't be instantiated
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	public @XmlElement(required = true) HashMapWrapper<String, String> getOrCreatePseudonymForList(
			@XmlElement(required = true) @WebParam(name = "values") Set<String> values,
			@XmlElement(required = true) @WebParam(name = "domain") String domain)
			throws DBException, InvalidGeneratorException, UnknownDomainException;

	/**
	 * returns the pseudonym to the given value
	 * 
	 * @param value
	 *            the string for which a pseudonym should be retrieved
	 * @param domain
	 *            for which domain should the pseudonym be retrieved
	 * @return the pseudonym
	 * @throws UnknownValueException
	 *             if the given value is not found for the given domain
	 */
	public @XmlElement(required = true) String getPseudonymFor(@XmlElement(required = true) @WebParam(name = "value") String value,
			@XmlElement(required = true) @WebParam(name = "domain") String domain) throws UnknownValueException;

	/**
	 * list version of {@link PSNManager#getPseudonymFor(String, String)} returns a map&lt;value, psn&gt; (with psn="*** VALUE NOT FOUND ***" for each given value which is unknown for the given
	 * domain)
	 * 
	 * @param values
	 *            a list of values for which the pseudonyms should be retrieved
	 * @param domain
	 *            for which domain should the pseudonyms be retrieved
	 * @return map of value-pseudonym pairs
	 */
	public @XmlElement(required = true) HashMapWrapper<String, String> getPseudonymForList(
			@XmlElement(required = true) @WebParam(name = "values") Set<String> values,
			@XmlElement(required = true) @WebParam(name = "domain") String domain);

	/**
	 * list version of {@link PSNManager#getPseudonymFor(String, String)} returns a map&lt;value, psn&gt; for all values which starts with the given string
	 * 
	 * @param valuePrefix
	 *            the prefix of all values for which the pseudonyms should be retrieved
	 * @param domain
	 *            for which domain should the pseudonyms be retrieved
	 * @return map of value-pseudonym pairs
	 */
	public @XmlElement(required = true) HashMapWrapper<String, String> getPseudonymForValuePrefix(
			@XmlElement(required = true) @WebParam(name = "valuePrefix") String valuePrefix,
			@XmlElement(required = true) @WebParam(name = "domain") String domain);

	/**
	 * deletes the assignment value - pseudonym from the db (replaces "value" with "###_anonym_###_randString_###_anonym_###")
	 * 
	 * @param value
	 *            the value which should be anonymised
	 * @param domain
	 *            for which domain should the given value be anonymised
	 * @throws DBException
	 *             if an error occurred while anonymising the entry
	 * @throws UnknownValueException
	 *             if the value is not found within the given domain
	 * @throws ValueIsAnonymisedException
	 *             if the value is already anonymised
	 */
	public void anonymiseEntry(@XmlElement(required = true) @WebParam(name = "value") String value,
			@XmlElement(required = true) @WebParam(name = "domain") String domain)
			throws DBException, UnknownValueException, ValueIsAnonymisedException;

	/**
	 * deletes the assignment value - pseudonym from the db
	 *
	 * @author schuldtr
	 * @param value
	 *            the value for which the assignment should be delete
	 * @param domain
	 *            within which domain should the assignment be deleted
	 * @throws DeletionForbiddenException
	 *             when it's not allowed to delete value-pseudonym-assignments within a domain<br>
	 *             see {@link GeneratorProperties#PSNS_DELETABLE}
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 * @throws UnknownValueException
	 *             if the given value is not found within the db for that domain
	 */
	public void deleteEntry(@XmlElement(required = true) @WebParam(name = "value") String value,
			@XmlElement(required = true) @WebParam(name = "domain") String domain)
			throws DeletionForbiddenException, UnknownDomainException, UnknownValueException;

	/**
	 * checks if the given string is a valid pseudonym
	 * 
	 * @param psn
	 *            the pseudonym which should be checked
	 * @param domain
	 *            for which domain should the given pseudonym be checked
	 * @throws InvalidGeneratorException
	 *             if the generator can't be instantiated
	 * @throws InvalidPSNException
	 *             if the given PSN is not valid
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	public void validatePSN(@XmlElement(required = true) @WebParam(name = "psn") String psn,
			@XmlElement(required = true) @WebParam(name = "domain") String domain)
			throws DBException, InvalidGeneratorException, InvalidPSNException, UnknownDomainException;

	/**
	 * searches the value for the given psn and domain; calls checkPSN(psn, domain) before searching the value
	 * 
	 * @param psn
	 *            for which pseudonym should the original value be retrieved
	 * @param domain
	 *            for which domain should the original value to the given pseudonym be retrieved
	 * @return the original value
	 * @throws InvalidGeneratorException
	 *             if the generator can't be instantiated
	 * @throws InvalidPSNException
	 *             if the given PSN is not valid
	 * @throws PSNNotFoundException
	 *             if the given PSN is not found in the db for that domain
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 * @throws ValueIsAnonymisedException
	 *             if pseudonym is anonymised
	 */
	public @XmlElement(required = true) String getValueFor(@XmlElement(required = true) @WebParam(name = "psn") String psn,
			@XmlElement(required = true) @WebParam(name = "domain") String domain)
			throws InvalidGeneratorException, InvalidPSNException, PSNNotFoundException, UnknownDomainException, ValueIsAnonymisedException;

	/**
	 * list version of {@link PSNManager#getValueFor(String, String)}<br>
	 * returns a map&lt;psn, value&gt; (with value="*** PSN NOT FOUND ***" for each given psn which is unknown for the given domain)
	 * 
	 * @param psnList
	 *            list of pseudonyms for which the original values should be retrieved
	 * @param domain
	 *            for which domain should the original value to the given pseudonym be retrieved
	 * @return a map with pairs pseudonym -> original value
	 * @throws InvalidGeneratorException
	 *             if the generator can't be instantiated
	 * @throws InvalidPSNException
	 *             if the given PSN is not valid
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	public @XmlElement(required = true) HashMapWrapper<String, String> getValueForList(
			@XmlElement(required = true) @WebParam(name = "psnList") Set<String> psnList,
			@XmlElement(required = true) @WebParam(name = "domain") String domain)
			throws InvalidGeneratorException, InvalidPSNException, UnknownDomainException;

	/**
	 * insert a value - pseudonym pair<br>
	 * value is expected to be a valid pseudonym
	 * 
	 * @param value
	 *            the value for which the pseudonym should be inserted
	 * @param pseudonym
	 *            the pseudonym which should be inserted for the given value
	 * @param domain
	 *            for which domain should the given pairs be stored
	 * @throws DBException
	 *             if an error occurred while persisting a new entry (e.g. duplicate value)
	 * @throws InvalidGeneratorException
	 *             if the generator can't be instantiated
	 * @throws InvalidPSNException
	 *             if the given PSN is not valid
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	public void insertValuePseudonymPair(@XmlElement(required = true) @WebParam(name = "value") String value,
			@XmlElement(required = true) @WebParam(name = "pseudonym") String pseudonym,
			@XmlElement(required = true) @WebParam(name = "domain") String domain)
			throws DBException, InvalidGeneratorException, InvalidPSNException, UnknownDomainException;

	/**
	 * insert value - pseudonym pairs<br>
	 * values within the map are expected to be valid pseudonyms
	 * 
	 * @param pairs
	 *            map of pairs which should be stored
	 * @param domain
	 *            for which domain should the given pairs be stored
	 * @throws DBException
	 *             if an error occurred while persisting a new entry (e.g. duplicate value)
	 * @throws InvalidGeneratorException
	 *             if the generator can't be instantiated
	 * @throws InvalidPSNException
	 *             if the given PSN is not valid
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	public void insertValuePseudonymPairs(@XmlElement(required = true) @WebParam(name = "pairs") HashMapWrapper<String, String> pairs,
			@XmlElement(required = true) @WebParam(name = "domain") String domain)
			throws DBException, InvalidGeneratorException, InvalidPSNException, UnknownDomainException;

	/**
	 * Create psn tree<br>
	 * with all values that are somehow linked to the given psn
	 * 
	 * @param psn
	 *            pseudonym that should be used to create the psn tree
	 * @param domain
	 *            domain of the given pseudonym
	 * @throws DBException
	 *             if an error occurred while persisting a new entry (e.g. duplicate value)
	 * @throws InvalidGeneratorException
	 *             if the generator can't be instantiated
	 * @throws InvalidPSNException
	 *             if the given PSN is not valid
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 * @throws ValueIsAnonymisedException
	 *             if the value is already anonymised
	 * @throws PSNNotFoundException
	 *             if the given PSN is not found in the db for that domain
	 */
	public PSNTreeDTO getPSNTreeForPSN(@XmlElement(required = true) @WebParam(name = "psn") String psn,
			@XmlElement(required = true) @WebParam(name = "domain") String domain) throws DBException, InvalidGeneratorException, InvalidPSNException,
			UnknownDomainException, PSNNotFoundException, ValueIsAnonymisedException;
}
