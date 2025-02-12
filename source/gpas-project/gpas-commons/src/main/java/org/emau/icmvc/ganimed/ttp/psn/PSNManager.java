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

import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
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
import org.emau.icmvc.ganimed.ttp.psn.utils.MultiPsnListAdapter;
import org.emau.icmvc.ganimed.ttp.psn.utils.MultiPsnMapAdapter;
import org.emau.icmvc.ganimed.ttp.psn.utils.StringPair;

@WebService
public interface PSNManager
{
	/**
	 * {@return a new pseudonym for the given value in the specified domain}
	 * <p>
	 * Note that an exception will be thrown if there are already pseudonyms for the value in a single-PSN domain (inconsistent data).
	 * </p>
	 * Preferably to be used for multi-PSN domains.
	 *
	 * @param value the value to create a pseudonym for
	 * @param domainName the name of the domain
	 *
	 * @throws DomainIsFullException if the domain is full
	 * @throws InvalidParameterException if there are already pseudonyms for the value in a single-PSN domain
	 * @throws UnknownDomainException if the domain is unknown
	 */
	@XmlElement(name = "psn")
	String createPseudonymFor(
			@XmlElement(required = true) @WebParam(name = "value") String value,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws DomainIsFullException, InvalidParameterException, UnknownDomainException;

	/**
	 * {@return the specified number of new pseudonyms for the given value in the specified multi-PSN domain}
	 * <p>
	 * Note that an exception will be thrown if the number is greater than 1 or if there are already pseudonyms
	 * for the value in a single-PSN domain (inconsistent data).
	 * </p>
	 * Preferably to be used for multi-PSN domains.
	 *
	 * @param value the value to create a pseudonym for
	 * @param domainName the name of the domain
	 * @param number the number of new pseudonyms to create
	 *
	 * @throws DomainIsFullException if the domain is full
	 * @throws InvalidParameterException if there are already pseudonyms for the value in a single-PSN domain
	 * @throws UnknownDomainException if the domain is unknown
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "psn")
	List<String> createPseudonymsFor(
			@XmlElement(required = true) @WebParam(name = "value") String value,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "number") int number)
			throws DomainIsFullException, InvalidParameterException, UnknownDomainException;

	/**
	 * {@return a new pseudonym for each of the given values in the specified domain}
	 * <p>
	 * Note that an exception will be thrown if there are already pseudonyms for the value in a single-PSN domain.
	 * </p>
	 * Preferably to be used for multi-PSN domains.
	 *
	 * @param values the values to create a pseudonym for
	 * @param domainName the name of the domain
	 *
	 * @throws DomainIsFullException if the domain is full
	 * @throws InvalidParameterException if there are already pseudonyms for the value in a single-PSN domain
	 * @throws UnknownDomainException if the given domain is not found
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "valuePsnMap")
	Map<String, String> createPseudonymForList(
			@XmlElement(required = true) @WebParam(name = "values") Set<String> values,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws DomainIsFullException, InvalidParameterException, UnknownDomainException;

	/**
	 * {@return the specified number of new pseudonyms for each of the given values in the specified multi-PSN domain}
	 * <p>
	 * Note that an exception will be thrown if the number is greater than 1 or if there are already pseudonyms
	 * for the value in a single-PSN domain.
	 * </p>
	 * Preferably to be used for multi-PSN domains.
	 *
	 * @param values the values to create a pseudonym for
	 * @param domainName the name of the domain
	 * @param number the number of new pseudonyms to create
	 *
	 * @throws DomainIsFullException if the domain is full
	 * @throws InvalidParameterException if the number is greater than 1 or if there are already pseudonyms
	 * 		for the value in a single-PSN domain
	 * @throws UnknownDomainException if the given domain is not found
	 */
	@XmlJavaTypeAdapter(MultiPsnMapAdapter.class)
	@XmlElement(name = "return")
	Map<String, List<String>> createPseudonymsForList(
			@XmlElement(required = true) @WebParam(name = "values") Set<String> values,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "number") int number)
			throws DomainIsFullException, InvalidParameterException, UnknownDomainException;

	/**
	 * {@return the pseudonym for the given value in the specified domain, or create a new one if there is no such pseudonym, yet}
	 * <p>
	 * Note that an exception will be thrown if there are multiple pseudonyms for a value in a single-PSN domain (inconsistent data),
	 * but also for a multi-PSN domain, as multiple pseudonyms cannot be returned as a single string (invalid parameter).
	 * </p>
	 * Preferably to be used for single-PSN domains.
	 *
	 * @param value the value to get or create a pseudonym for
	 * @param domainName the name of the domain
	 *
	 * @throws DomainIsFullException if the domain is full
	 * @throws InvalidParameterException if there exist more than one pseudonym for the value in a multi-PSN domain
	 * @throws UnknownDomainException if the domain is unknown
	 */
	@XmlElement(name = "psn")
	String getOrCreatePseudonymFor(
			@XmlElement(required = true) @WebParam(name = "value") String value,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws DomainIsFullException, InvalidParameterException, UnknownDomainException;

	/**
	 * {@return all pseudonyms for the given value in the specified domain,
	 * and ensures that at least <code>minNumber</code> pseudonyms for the value, creating new ones as needed}
	 * <p>
	 * Note that an exception will be thrown if there are multiple pseudonyms for a value in a single-PSN domain (inconsistent data).
	 * </p>
	 * Preferably to be used for multi-PSN domains.
	 *
	 * @param value the value to get or create a pseudonym for
	 * @param domainName the name of the domain
	 * @param minNumber the minimum number of pseudonyms for the given value
	 *
	 * @throws InvalidParameterException if one of the required parameters is not set
	 * @throws DomainIsFullException if the domain is full
	 * @throws UnknownDomainException if the domain is unknown
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "psn")
	List<String> getOrCreatePseudonymsFor(
			@XmlElement(required = true) @WebParam(name = "value") String value,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "minNumber") int minNumber)
			throws DomainIsFullException, InvalidParameterException, UnknownDomainException;

	/**
	 * {@return pseudonyms for the given values in the specified domain, or create a new ones if there are no such pseudonyms, yet}
	 * <p>
	 *     Note that an exception will be thrown if there are multiple pseudonyms for a value in a single-PSN domain (inconsistent data),
	 * but also for a multi-PSN domain, as multiple pseudonyms cannot be returned as a single string (invalid parameter).
	 * </p>
	 * Preferably to be used for single-PSN domains.
	 *
	 * @param values the values to get or create a pseudonym for
	 * @param domainName the name of the domain
	 *
	 * @throws DomainIsFullException if the domain is full
	 * @throws InvalidParameterException if there exist more than one pseudonym for value in a multi-PSN domain
	 * @throws UnknownDomainException if the domain is unknown
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "valuePsnMap")
	Map<String, String> getOrCreatePseudonymForList(
			@XmlElement(required = true) @WebParam(name = "values") Set<String> values,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws DomainIsFullException, InvalidParameterException, UnknownDomainException;

	/**
	 * {@return all pseudonyms for each of the given values in the specified domain,
	 * and ensures that at least <code>minNumber</code> pseudonyms exist per value, creating new ones as needed}
	 * <p>
	 * Note that an exception will be thrown if there are multiple pseudonyms for a value in a single-PSN domain (inconsistent data).
	 * </p>
	 * Preferably to be used for multi-PSN domains.
	 *
	 * @param values the value to get or create a pseudonym for
	 * @param domainName the name of the domain
	 * @param minNumber the minimum number of pseudonyms for the given values
	 *
	 * @throws DomainIsFullException if the domain is full
	 * @throws InvalidParameterException if one of the required parameters is not set
	 * @throws UnknownDomainException if the domain is unknown
	 */
	@XmlJavaTypeAdapter(MultiPsnMapAdapter.class)
	@XmlElement(name = "return")
	Map<String, List<String>> getOrCreatePseudonymsForList(
			@XmlElement(required = true) @WebParam(name = "values") Set<String> values,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "minNumber") int minNumber)
			throws DomainIsFullException, InvalidParameterException, UnknownDomainException;

	/**
	 * {@return the pseudonym for the given value in the specified domain or throw an exception if there is no such pseudonym}
	 * <p>
	 * Note that also an exception will be thrown if there are multiple pseudonyms for the value in a single-PSN domain (inconsistent data),
	 * but also for a multi-PSN domain, as they all cannot be returned as a single string (invalid parameter).
	 * </p>
	 * Preferably to be used for single-PSN domains.
	 *
	 * @param value the value to get a pseudonym for
	 * @param domainName the name of the domain
	 *
	 * @throws InvalidParameterException if there exist more than one pseudonym for the value in a multi-PSN domain
	 * @throws UnknownDomainException if the domain is unknown
	 */
	@XmlElement(name = "psn")
	String getPseudonymFor(
			@XmlElement(required = true) @WebParam(name = "value") String value,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws InvalidParameterException, UnknownDomainException, UnknownValueException;

	/**
	 * {@return the pseudonyms for the given value in the specified domain}
	 * <p>
	 * Note that an exception will be thrown if there are multiple pseudonyms for a value in a single-PSN domain (inconsistent data).
	 * </p>
	 * Preferably to be used for multi-PSN domains.
	 *
	 * @param value the value to get pseudonyms for
	 * @param domainName the name of the domain
	 *
	 * @throws InvalidParameterException if one of the required parameters is not set
	 * @throws UnknownDomainException if the domain is unknown
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "psn")
	List<String> getPseudonymsFor(
			@XmlElement(required = true) @WebParam(name = "value") String value,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws InvalidParameterException, UnknownDomainException, UnknownValueException;

	/**
	 * {@return the pseudonyms for the given values in the specified domain or throw an exception if there is no such pseudonym}
	 * <p>
	 * Note that also an exception will be thrown if there are multiple pseudonyms for the value in a single-PSN domain (inconsistent data),
	 * but also for a multi-PSN domain, as they all cannot be returned as a single string (invalid parameter).
	 * </p>
	 * Preferably to be used for single-PSN domains.
	 *
	 * @param values the value to get pseudonyms for
	 * @param domainName the name of the domain
	 *
	 * @throws InvalidParameterException if there exist more than one pseudonym for the value in a multi-PSN domain
	 * @throws UnknownDomainException if the domain is unknown
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "valuePsnMap")
	Map<String, String> getPseudonymForList(
			@XmlElement(required = true) @WebParam(name = "values") Set<String> values,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws InvalidParameterException, UnknownDomainException;

	/**
	 * {@return the pseudonyms for the given values in the specified domain}
	 * <p>
	 * Note that an exception will be thrown if there are multiple pseudonyms for a value in a single-PSN domain (inconsistent data).
	 * </p>
	 * Preferably to be used for multi-PSN domains.
	 *
	 * @param values the value to get pseudonyms for
	 * @param domainName the name of the domain
	 *
	 * @throws InvalidParameterException if one of the required parameters is not set
	 * @throws UnknownDomainException if the domain is unknown
	 */
	@XmlJavaTypeAdapter(MultiPsnMapAdapter.class)
	@XmlElement(name = "return")
	Map<String, List<String>> getPseudonymsForList(
			@XmlElement(required = true) @WebParam(name = "values") Set<String> values,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws InvalidParameterException, UnknownDomainException;

	/**
	 * {@return the pseudonyms for all values which starts with the given prefix in the specified domain}
	 * <p>
	 * Note that also an exception will be thrown if there are multiple pseudonyms for the value in a single-PSN domain (inconsistent data),
	 * but also for a multi-PSN domain, as they all cannot be returned as a single string (invalid parameter).
	 * </p>
	 * Preferably to be used for single-PSN domains.
	 *
	 * @param valuePrefix the prefix of all values for which the pseudonyms should be retrieved
	 * @param domainName the name of the domain
	 *
	 * @throws InvalidParameterException if there exist more than one pseudonym for the value in a multi-PSN domain
	 * @throws UnknownDomainException if the domain is unknown
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "valuePsnMap")
	Map<String, String> getPseudonymForValuePrefix(
			@XmlElement(required = true) @WebParam(name = "valuePrefix") String valuePrefix,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws InvalidParameterException, UnknownDomainException;

	/**
	 * {@return all pseudonyms for each value which starts with the given prefix in the specified domain}
	 * <p>
	 * Note that an exception will be thrown if there are multiple pseudonyms for a value in a single-PSN domain (inconsistent data).
	 * </p>
	 * Preferably to be used for multi-PSN domains.
	 *
	 * @param valuePrefix the prefix of all values for which the pseudonyms should be retrieved
	 * @param domainName the name of the domain
	 *
	 * @throws InvalidParameterException if one of the required parameters is not set
	 * @throws UnknownDomainException if the domain is unknown
	 */
	@XmlJavaTypeAdapter(MultiPsnMapAdapter.class)
	@XmlElement(name = "return")
	Map<String, List<String>> getPseudonymsForValuePrefix(
			@XmlElement(required = true) @WebParam(name = "valuePrefix") String valuePrefix,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws InvalidParameterException, UnknownDomainException;

	/**
	 * Anonymises a single pseudonym for the given value in the specified domain,
	 * replacing "value" with "###_anonym_###_randString_###_anonym_###".
	 * Throws an exception if there is not exactly one pseudonym for the value.
	 *
	 * @param value the value which should be anonymised
	 * @param domainName the name of the domain
	 *
	 * @throws DBException e.g. if there exist more than one pseudonym for a value in a single-PSN domain
	 * @throws InvalidParameterException e.g. if there exist more than one pseudonym for a value in a multi-PSN domain
	 * @throws UnknownDomainException if the domain is unknown
	 * @throws UnknownValueException if there are no pseudonyms for the given value in the specified domain
	 * @throws ValueIsAnonymisedException if the value cannot be anonymised
	 */
	void anonymiseEntry(
			@XmlElement(required = true) @WebParam(name = "value") String value,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws DBException, InvalidParameterException, UnknownDomainException, UnknownValueException, ValueIsAnonymisedException;

	/**
	 * {@return SUCCESS, NOT_FOUND, ALREADY_ANONYMISED, or ERROR for the values depending on the result of anonymising them
	 * (replacing each "value" with "###_anonym_###_randString_###_anonym_###")}
	 * In particular, an {@link AnonymisationResult#ERROR} is returned for values with multiple pseudonyms associated.
	 *
	 * @param values the values which should be anonymised
	 * @param domainName the name of the domain
	 *
	 * @throws DBException e.g. if there exist more than one pseudonym for a value in a single-PSN domain
	 * @throws InvalidParameterException if one of the required parameters is not set
	 * @throws UnknownDomainException if the given domain is not found
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "anonymisationResult")
	Map<String, AnonymisationResult> anonymiseEntries(
			@XmlElement(required = true) @WebParam(name = "values") Set<String> values,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws DBException, InvalidParameterException, UnknownDomainException;

	/**
	 * Anonymises all pseudonyms for the given value in the specified domain
	 * (replacing each "value" with "###_anonym_###_randString_###_anonym_###").
	 * Throws an exception if there is no pseudonym for the value.
	 *
	 * @param value the value for which all entries should be anonymised
	 * @param domainName the name of the domain
	 *
	 * @throws DBException e.g. if there exist more than one pseudonym for a value in a single-PSN domain
	 * @throws InvalidParameterException e.g. if there exist more than one pseudonym for a value in a multi-PSN domain
	 * @throws UnknownDomainException if the given domain is not found
	 * @throws UnknownValueException if there are no pseudonyms for the given value in the specified domain
	 * @throws ValueIsAnonymisedException if the value cannot be anonymised
	 */
	void anonymiseAllEntriesForValue(
			@XmlElement(required = true) @WebParam(name = "value") String value,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws DBException, InvalidParameterException, UnknownDomainException, UnknownValueException, ValueIsAnonymisedException;

	/**
	 * {@return SUCCESS, NOT_FOUND, ALREADY_ANONYMISED for the values depending on the result of anonymising all pseudonyms for each value}
	 *
	 * @param values the values for which all entries should be anonymised
	 * @param domainName the name of the domain
	 *
	 * @throws DBException e.g. if there exist more than one pseudonym for a value in a single-PSN domain
	 * @throws InvalidParameterException if one of the required parameters is not set
	 * @throws UnknownDomainException if the given domain is not found
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "anonymisationResult")
	Map<String, AnonymisationResult> anonymiseAllEntriesForValues(
			@XmlElement(required = true) @WebParam(name = "values") Set<String> values,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws DBException, InvalidParameterException, UnknownDomainException;

	/**
	 * Anonymises the given pseudonym in the specified domain.
	 *
	 * @param psn the pseudonym to anonymise
	 * @param domainName the name of the domain
	 *
	 * @throws DBException e.g. if there exist more than one pseudonym for a value in a single-PSN domain
	 * @throws InvalidParameterException if one of the required parameters is not set
	 * @throws UnknownDomainException if the given domain is not found
	 */
	void anonymisePseudonym(
			@XmlElement(required = true) @WebParam(name = "psn") String psn,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws DBException, InvalidParameterException, UnknownDomainException, InvalidPSNException, PSNNotFoundException;

	/**
	 * {@return SUCCESS, NOT_FOUND, or ALREADY_ANONYMISED for the pseudonyms depending on the result of anonymising them}
	 *
	 * @param psns the pseudonyms to anonymise
	 * @param domainName the name of the domain
	 *
	 * @throws DBException e.g. if there exist more than one pseudonym for a value in a single-PSN domain
	 * @throws InvalidParameterException if one of the required parameters is not set
	 * @throws UnknownDomainException if the given domain is not found
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "anonymisationResult")
	Map<String, AnonymisationResult> anonymisePseudonyms(
			@XmlElement(required = true) @WebParam(name = "psns") Set<String> psns,
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
	boolean isAnonym(
			@XmlElement(required = true) @WebParam(name = "value") String value)
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
	boolean isAnonymised(
			@XmlElement(required = true) @WebParam(name = "psn") String psn,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws InvalidParameterException, InvalidPSNException, UnknownDomainException, PSNNotFoundException;

	/**
	 * Deletes a single pseudonym for the given value in the specified domain.
	 * Throws an exception if there is not exactly one pseudonym for the value.
	 *
	 * @param value the value which should be deleted
	 * @param domainName the name of the domain
	 *
	 * @throws InvalidParameterException if one of the required parameters is not set
	 * @throws UnknownDomainException if the given domain is not found
	 * @throws UnknownValueException if there are no pseudonyms for the given value in the specified domain
	 */
	void deleteEntry(
			@XmlElement(required = true) @WebParam(name = "value") String value,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws DeletionForbiddenException, InvalidParameterException, UnknownDomainException, UnknownValueException;

	/**
	 * {@return SUCCESS, NOT_FOUND, or ERROR for the given values depending on the result of deleting all their pseudonyms}
	 * In particular, an {@link DeletionResult#ERROR} is returned for values with multiple pseudonyms associated.
	 *
	 * @param values the values which should be deleted
	 * @param domainName the name of the domain
	 *
	 * @throws InvalidParameterException if one of the required parameters is not set
	 * @throws UnknownDomainException if the given domain is not found
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "deletionResult")
	Map<String, DeletionResult> deleteEntries(
			@XmlElement(required = true) @WebParam(name = "values") Set<String> values,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws DeletionForbiddenException, InvalidParameterException, UnknownDomainException;

	/**
	 * Deletes all pseudonyms for the given value in the specified domain.
	 * Throws an exception if there is no pseudonym for the value.
	 *
	 * @param value the value for which all entries should be deleted
	 * @param domainName the name of the domain
	 *
	 * @throws InvalidParameterException e.g. if there exist more than one pseudonym for a value in a multi-PSN domain
	 * @throws UnknownDomainException if the given domain is not found
	 * @throws UnknownValueException if there are no pseudonyms for the given value in the specified domain
	 */
	void deleteAllEntriesForValue(
			@XmlElement(required = true) @WebParam(name = "value") String value,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws DeletionForbiddenException, InvalidParameterException, UnknownDomainException, UnknownValueException;

	/**
	 * {@return SUCCESS, NOT_FOUND for the values depending on the result of deleting all pseudonyms for each value}
	 *
	 * @param values the values for which all entries should be deleted
	 * @param domainName the name of the domain
	 *
	 * @throws InvalidParameterException if one of the required parameters is not set
	 * @throws UnknownDomainException if the given domain is not found
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "deletionResult")
	Map<String, DeletionResult> deleteAllEntriesForValues(
			@XmlElement(required = true) @WebParam(name = "values") Set<String> values,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws DeletionForbiddenException, InvalidParameterException, UnknownDomainException;

	/**
	 * Deletes the pseudonym in the specified domain.
	 *
	 * @param psn the pseudonym to delete
	 * @param domainName the name of the domain
	 *
	 * @throws InvalidParameterException if one of the required parameters is not set
	 * @throws UnknownDomainException if the given domain is not found
	 * @throws UnknownValueException if there are no pseudonyms for the given value in the specified domain
	 */
	void deletePseudonym(
			@XmlElement(required = true) @WebParam(name = "psn") String psn,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws DeletionForbiddenException, InvalidParameterException, UnknownDomainException, UnknownValueException, InvalidPSNException, PSNNotFoundException;

	/**
	 * {@return SUCCESS or NOT_FOUND for the given pseudonyms depending on the result of deleting pseudonyms}
	 *
	 * @param psns the pseudonyms to delete
	 * @param domainName the name of the domain
	 *
	 * @throws InvalidParameterException if one of the required parameters is not set
	 * @throws UnknownDomainException if the given domain is not found
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "deletionResult")
	Map<String, DeletionResult> deletePseudonyms(
			@XmlElement(required = true) @WebParam(name = "psns") Set<String> psns,
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
	void validatePSN(
			@XmlElement(required = true) @WebParam(name = "psn") String psn,
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
	String getValueFor(
			@XmlElement(required = true) @WebParam(name = "psn") String psn,
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
	void insertValuePseudonymPair(
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
			@XmlElement(required = true) @WebParam(name = "pairs") @XmlJavaTypeAdapter(MultiPsnListAdapter.class) List<StringPair> pairs,
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws InvalidParameterException, UnknownDomainException;

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
	PSNTreeDTO getPSNTreeForPSN(
			@XmlElement(required = true) @WebParam(name = "psn") String psn,
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
	PSNNetDTO getPSNNetFor(
			@XmlElement(required = true) @WebParam(name = "valueOrPSN") String valueOrPSN)
			throws InvalidParameterException;
}
