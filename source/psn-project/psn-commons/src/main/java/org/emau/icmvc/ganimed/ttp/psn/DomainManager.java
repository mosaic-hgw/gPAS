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

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import org.emau.icmvc.ganimed.ttp.psn.config.DomainProperties;
import org.emau.icmvc.ganimed.ttp.psn.config.PaginationConfig;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainInDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainOutDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.PSNDTO;
import org.emau.icmvc.ganimed.ttp.psn.enums.GeneratorAlphabetRestriction;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DomainInUseException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidCheckDigitClassException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidGeneratorException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParentDomainException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;

@WebService
public interface DomainManager
{
	/**
	 * adds a new domain
	 *
	 * @param domainDTO
	 *            see {@link DomainInDTO}
	 * @throws DomainInUseException
	 *             if the given domain already exists
	 * @throws InvalidAlphabetException
	 *             if the given alphabet name (attribute of the given domain) is not a valid alphabet or the length of the alphabet is not valid for the
	 *             given check digit class
	 * @throws InvalidCheckDigitClassException
	 *             if the given check digit class name (attribute of the given domain) is not a valid check digit class
	 * @throws InvalidGeneratorException
	 *             if the generator can't be instantiated
	 * @throws InvalidParameterException
	 *             if domainName is null or empty
	 * @throws UnknownDomainException
	 *             if the given parentDomain can't be found
	 */
	void addDomain(@XmlElement(required = true) @WebParam(name = "domainDTO") DomainInDTO domainDTO) throws DomainInUseException,
			InvalidAlphabetException, InvalidCheckDigitClassException, InvalidGeneratorException, InvalidParameterException, InvalidParentDomainException, UnknownDomainException;

	/**
	 * updates a existing domain
	 *
	 * @param domainDTO
	 *            see {@link DomainInDTO}
	 * @throws DomainInUseException
	 *             if there's at least one pseudonym within that domain
	 * @throws InvalidAlphabetException
	 *             if the given alphabet name (attribute of the given domain) is not a valid alphabet or the length of the alphabet is not valid for the
	 *             given check digit class
	 * @throws InvalidCheckDigitClassException
	 *             if the given check digit class name (attribute of the given domain) is not a valid check digit class
	 * @throws InvalidGeneratorException
	 *             if the generator can't be instantiated
	 * @throws InvalidParameterException
	 *             if domainName is null or empty
	 * @throws UnknownDomainException
	 *             if the given domain or parentDomain doesn't exists
	 */
	void updateDomain(@XmlElement(required = true) @WebParam(name = "domainDTO") DomainInDTO domainDTO) throws DomainInUseException,
			InvalidAlphabetException, InvalidCheckDigitClassException, InvalidGeneratorException, InvalidParameterException, UnknownDomainException, InvalidParentDomainException;

	/**
	 * updates a existing domain which is in use (contains psns)
	 *
	 * @param domainName
	 *            the name of the domain
	 * @param label
	 *            the new label
	 * @param comment
	 *            the new comment
	 * @param sendNotificationsWeb
	 *            whether the web should send notifications to external systems
	 * @param psnsDeletable
	 *            whether psns should be deleteable
	 * @throws InvalidParameterException
	 *             if domainName is null or empty
	 * @throws UnknownDomainException
	 *             if the given domain or parentDomain doesn't exists
	 */
	void updateDomainInUse(@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "label") String label,
			@XmlElement(required = true) @WebParam(name = "comment") String comment,
			@XmlElement(required = true) @WebParam(name = "sendNotificationsWeb") boolean sendNotificationsWeb,
			@XmlElement(required = true) @WebParam(name = "psnsDeletable") boolean psnsDeletable)
			throws InvalidParameterException, UnknownDomainException, InvalidGeneratorException, InvalidAlphabetException, InvalidParentDomainException, InvalidCheckDigitClassException;

	/**
	 * deletes the given domain
	 *
	 * @param domainName
	 *            identifier
	 * @throws DomainInUseException
	 *             if there's at least one pseudonym within that domain or that domain is a parent domain
	 * @throws InvalidParameterException
	 *             if domainName is null or empty
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	void deleteDomain(@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws DomainInUseException, InvalidParameterException, UnknownDomainException;

	/**
	 * deletes the given domain with all pseudonyms - be sure you know, what you're doing
	 *
	 * @param domainName
	 *            identifier
	 * @throws DomainInUseException
	 *             if that domain is a parent domain
	 * @throws InvalidParameterException
	 *             if domainName is null or empty
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	void deleteDomainWithPSNs(@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws DomainInUseException, InvalidParameterException, UnknownDomainException;

	/**
	 * returns all information for the given domain
	 *
	 * @param domainName
	 *            the name of the domain
	 * @return see {@link DomainOutDTO}
	 * @throws InvalidParameterException
	 *             if domainName is null or empty
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	@XmlElement(name = "domain")
	DomainOutDTO getDomain(@XmlElement(required = true) @WebParam(name = "domainName") String domainName)
			throws InvalidParameterException, UnknownDomainException;

	/**
	 * @return list of all domains within the database; see {@link DomainOutDTO}
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "domainList")
	List<DomainOutDTO> listDomains();

	/**
	 * @param prefix
	 *            the domain prefix
	 * @return list of all domains with the given prefix; see {@link DomainOutDTO}
	 * @throws InvalidParameterException
	 *             if prefix is null or empty
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "domainList")
	List<DomainOutDTO> getDomainsForPrefix(
			@XmlElement(required = true) @WebParam(name = "prefix") String prefix) throws InvalidParameterException;

	/**
	 * @param suffix
	 *            the domain suffix
	 * @return list of all domains with the given suffix; see {@link DomainOutDTO}
	 * @throws InvalidParameterException
	 *             if suffix is null or empty
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "domainList")
	List<DomainOutDTO> getDomainsForSuffix(
			@XmlElement(required = true) @WebParam(name = "suffix") String suffix) throws InvalidParameterException;

	/**
	 * @param domainName
	 *            domain for which all pseudonyms should be retrieved
	 * @return all pseudonyms for that domain
	 * @throws InvalidParameterException
	 *             if domainName is null or empty
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "psnList")
	List<PSNDTO> listPSNs(
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName) throws InvalidParameterException, UnknownDomainException;

	/**
	 * lists all matching psns for the given domain paginated w.r.t. the filter in the pagination config
	 *
	 * @param domainName
	 *            domain for which all pseudonyms should be retrieved
	 * @param config
	 *            see {@link PaginationConfig}
	 * @return all pseudonyms for that domain
	 * @throws InvalidParameterException
	 *             if domainName is null or empty
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "psnList")
	List<PSNDTO> listPSNsPaginated(
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "config") PaginationConfig config) throws InvalidParameterException, UnknownDomainException;

	/**
	 * counts matching for the given domain w.r.t. the filter in the pagination config
	 *
	 * @param domainName
	 *            domain for which all pseudonyms should be retrieved
	 * @param config
	 *            see {@link PaginationConfig} (page size and first entry will be ignored)
	 * @return the number of all matching pseudonyms for that domain
	 * @throws InvalidParameterException
	 *             if domainName is null or empty
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	@XmlElement(name = "psnCount")
	long countPSNs(
			@XmlElement(required = true) @WebParam(name = "domainName") String domainName,
			@XmlElement(required = true) @WebParam(name = "config") PaginationConfig config) throws InvalidParameterException, UnknownDomainException;

	/**
	 * lists all matching psns for the given domains paginated w.r.t. the filter in the pagination config
	 *
	 * @param domainNames
	 *            domains for which all pseudonyms should be retrieved
	 * @param config
	 *            see {@link PaginationConfig}
	 * @return all pseudonyms for that domain
	 * @throws InvalidParameterException
	 *             if domainName is null or empty
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	@XmlElementWrapper(nillable = true, name = "return")
	@XmlElement(name = "psnListForDomains")
	List<PSNDTO> listPSNsForDomainsPaginated(
			@XmlElement(required = true) @WebParam(name = "domainNames") List<String> domainNames,
			@XmlElement(required = true) @WebParam(name = "config") PaginationConfig config) throws InvalidParameterException, UnknownDomainException;

	/**
	 * counts matching for the given domain w.r.t. the filter in the pagination config
	 *
	 * @param domainNames
	 *            domain for which all pseudonyms should be retrieved
	 * @param config
	 *            see {@link PaginationConfig} (page size and first entry will be ignored)
	 * @return the number of all matching pseudonyms for that domain
	 * @throws InvalidParameterException
	 *             if domainName is null or empty
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	@XmlElement(name = "psnCountForDomains")
	long countPSNsForDomains(
			@XmlElement(required = true) @WebParam(name = "domainNames") List<String> domainNames,
			@XmlElement(required = true) @WebParam(name = "config") PaginationConfig config) throws InvalidParameterException, UnknownDomainException;

	/**
	 * gives the restriction for the number of chars within the alphabet for the given check digit class
	 *
	 * @throws InvalidCheckDigitClassException
	 * @throws InvalidParameterException
	 */
	@XmlElement(name = "alphabetRestriction")
	GeneratorAlphabetRestriction getRestrictionForCheckDigitClass(
			@XmlElement(required = true) @WebParam(name = "checkDigitClass") String checkDigitClass)
			throws InvalidCheckDigitClassException, InvalidParameterException;

	/**
	 *
	 * @param domainName
	 *            returns true if the property {@link DomainProperties#PSNS_DELETABLE} is set
	 * @throws InvalidParameterException
	 *             if domainName is null or empty
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	boolean arePSNDeletable(String domainName) throws InvalidParameterException, UnknownDomainException;
}
