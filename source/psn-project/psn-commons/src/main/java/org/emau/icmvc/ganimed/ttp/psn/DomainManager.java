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

import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;

import org.emau.icmvc.ganimed.ttp.psn.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainLightDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.PSNDTO;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DomainInUseException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidCheckDigitClassException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidDomainNameException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidGeneratorException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;

@WebService
public interface DomainManager {

	/**
	 * creates a new domain
	 * 
	 * @param domainDTO
	 *            see {@link DomainDTO}
	 * @throws InvalidDomainNameException
	 *             if the given domain name is invalid (null or empty)
	 * @throws InvalidAlphabetException
	 *             if the given alphabet name (entry for the given domain within the psn_projects table) is not a valid alphabet or the length of the alphabet is not valid for the given check digit
	 *             class
	 * @throws InvalidCheckDigitClassException
	 *             if the given check digit class name (entry for the given domain within the psn_projects table) is not a valid check digit class
	 * @throws InvalidGeneratorException
	 *             if the generator can't be instantiated
	 * @throws DomainInUseException
	 *             if a domain with that name already exists
	 * @throws UnknownDomainException
	 *             if the given parentDomain doesn't exists
	 */
	public void addDomain(@XmlElement(required = true) @WebParam(name = "domainDTO") DomainLightDTO domainDTO) throws InvalidDomainNameException,
			InvalidAlphabetException, InvalidCheckDigitClassException, InvalidGeneratorException, DomainInUseException, UnknownDomainException;

	/**
	 * deletes the given domain
	 * 
	 * @param domain
	 *            identifier
	 * @throws DomainInUseException
	 *             if there's at least one pseudonym within that domain
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	public void deleteDomain(@XmlElement(required = true) @WebParam(name = "domain") String domain)
			throws DomainInUseException, UnknownDomainException;

	/**
	 * returns all information for the given domain
	 * 
	 * @param domain
	 * @return see {@link DomainDTO}
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	public @XmlElement(required = true) DomainDTO getDomainObject(@XmlElement(required = true) @WebParam(name = "domain") String domain)
			throws UnknownDomainException;

	/**
	 * returns all information for the given domain besides the number of pseudonyms
	 * 
	 * @param domain
	 * @return see {@link DomainLightDTO}
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	public @XmlElement(required = true) DomainLightDTO getDomainLightObject(@XmlElement(required = true) @WebParam(name = "domain") String domain)
			throws UnknownDomainException;

	/**
	 * @return list of all domains within the database; see {@link DomainDTO}
	 */
	public @XmlElement(required = true) List<DomainDTO> listDomains();

	/**
	 * @return list of all domains within the database; see {@link DomainLightDTO}
	 */
	public @XmlElement(required = true) List<DomainLightDTO> listDomainsLight();

	/**
	 * @param prefix
	 * @return list of all domains with the given prefix; see {@link DomainLightDTO}
	 */
	public @XmlElement(required = true) List<DomainLightDTO> getDomainsForPrefix(
			@XmlElement(required = true) @WebParam(name = "prefix") String prefix);

	/**
	 * @param suffix
	 * @return list of all domains with the given suffix; see {@link DomainLightDTO}
	 */
	public @XmlElement(required = true) List<DomainLightDTO> getDomainsForSuffix(
			@XmlElement(required = true) @WebParam(name = "suffix") String suffix);

	/**
	 * @return list of all possible properties; is used by the web client
	 */
	public @XmlElement(required = true) List<String> listPossibleProperties();

	/**
	 * @param domain
	 *            domain for which all pseudonyms should be retrieved
	 * @return all pseudonyms for that domain
	 * @throws UnknownDomainException
	 *             if the given domain is not found
	 */
	public @XmlElement(required = true) List<PSNDTO> listPseudonymsFor(@XmlElement(required = true) @WebParam(name = "domain") String domain)
			throws UnknownDomainException;
}
