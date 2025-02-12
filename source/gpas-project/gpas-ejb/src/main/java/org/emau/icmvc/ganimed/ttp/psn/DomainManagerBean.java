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

import java.util.Collections;
import java.util.List;

import jakarta.ejb.ConcurrencyManagement;
import jakarta.ejb.ConcurrencyManagementType;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import org.emau.icmvc.ganimed.ttp.psn.alphabets.Alphabets;
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
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidUpdateInUseOperationException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.psn.generator.Alphabet;
import org.emau.icmvc.ganimed.ttp.psn.generator.CheckDigits;

@WebService(name = "DomainService")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@Stateless
@Remote(DomainManager.class)
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class DomainManagerBean extends GPASServiceBase implements DomainManager
{
	@Override
	public void addDomain(DomainInDTO domainDTO)
			throws DomainInUseException, InvalidAlphabetException, InvalidCheckDigitClassException, InvalidGeneratorException,
				InvalidParameterException, InvalidParentDomainException, UnknownDomainException
	{
		String domainName = domainDTO.getName();
		LOGGER.info("addDomain with name {}", domainName);
		checkParameter(domainName, "domainName");
		cache.addDomain(domainDTO);
		LOGGER.info("new domain {} persisted", domainName);
	}

	@Override
	public void updateDomain(DomainInDTO domainDTO)
			throws DomainInUseException, InvalidAlphabetException, InvalidCheckDigitClassException, InvalidGeneratorException,
				InvalidParameterException, UnknownDomainException, InvalidParentDomainException, InvalidUpdateInUseOperationException
	{
		String domainName = domainDTO.getName();
		LOGGER.info("updateDomain with name {}", domainName);
		checkParameter(domainName, "domainName");
		cache.updateDomain(domainDTO);
		LOGGER.info("domain {} updated", domainName);
	}

	@Override
	public void updateDomainInUse(String domainName, String label, String comment, List<String> parentDomainNames, boolean sendNotificationsWeb, boolean psnsDeletable)
			throws InvalidParameterException, UnknownDomainException, InvalidGeneratorException, InvalidAlphabetException,
				InvalidParentDomainException, InvalidCheckDigitClassException, InvalidUpdateInUseOperationException
	{
		LOGGER.info("updateDomainInUse with name {}", domainName);
		checkParameter(domainName, "domainName");
		cache.updateDomainInUse(domainName, label, comment, parentDomainNames, sendNotificationsWeb, psnsDeletable);
		LOGGER.info("domain (in use) {} updated", domainName);
	}

	@Override
	public void deleteDomain(String domainName)
			throws DomainInUseException, InvalidParameterException, UnknownDomainException
	{
		LOGGER.info("deleteDomain with name {}", domainName);
		checkParameter(domainName, "domainName");
		cache.deleteDomain(domainName, false);
		LOGGER.info("domain {} deleted", domainName);
	}

	@Override
	public void deleteDomainWithPSNs(String domainName)
			throws DomainInUseException, InvalidParameterException, UnknownDomainException
	{
		LOGGER.info("deleteDomainWithPSNs with name {}", domainName);
		checkParameter(domainName, "domainName");
		cache.deleteDomain(domainName, true);
		LOGGER.info("domain {} and all related PSNs deleted", domainName);
	}

	@Override
	public DomainOutDTO getDomain(String domainName)
			throws InvalidParameterException, UnknownDomainException
	{
		LOGGER.debug("getDomainObject with name {}", domainName);
		checkParameter(domainName, "domainName");
		DomainOutDTO result = cache.getDomainDTO(domainName);
		LOGGER.debug("domain found");
		return result;
	}

	@Override
	public List<DomainOutDTO> listDomains()
	{
		LOGGER.debug("listDomains");
		List<DomainOutDTO> result = cache.listDomains();
		LOGGER.debug("listDomains found {} domains", result.size());
		return result;
	}

	@Override
	public List<PSNDTO> listPSNs(String domainName)
			throws InvalidParameterException, UnknownDomainException
	{
		LOGGER.debug("listPSNs for domain {}", domainName);
		checkParameter(domainName, "domainName");
		List<PSNDTO> result = cache.listPSNs(domainName);
		LOGGER.debug("listPSNs found {} pseudonyms", result.size());
		return result;
	}

	@Override
	public List<PSNDTO> listPSNsPaginated(String domainName, PaginationConfig config)
			throws InvalidParameterException, UnknownDomainException
	{
		LOGGER.debug("listPSNsPaginated for domain {} with {}", domainName, config);
		checkParameter(domainName, "domainName");
		checkParameter(config, "config");
		List<PSNDTO> result = cache.listPSNsForDomainsPaginated(Collections.singletonList(domainName), config);
		LOGGER.debug("listPSNsPaginated found {} pseudonyms for given pagination config", result.size());
		return result;
	}

	@Override
	public long countPSNs(String domainName, PaginationConfig config)
			throws InvalidParameterException, UnknownDomainException
	{
		LOGGER.debug("countPSNs for domain {} with {}", domainName, config);
		checkParameter(domainName, "domainName");
		checkParameter(config, "config");
		long count = cache.countPSNsForDomainsPaginated(Collections.singletonList(domainName), config);
		LOGGER.debug("countPSNs found {} pseudonyms for given pagination filter", count);
		return count;
	}

	@Override
	public List<PSNDTO> listPSNsForDomainsPaginated(List<String> domainNames, PaginationConfig config)
			throws InvalidParameterException, UnknownDomainException
	{
		LOGGER.debug("listPSNsForDomainsPaginated for domains {} with {}", domainNames, config);
		checkParameter(domainNames, "domainName");
		checkParameter(config, "config");
		List<PSNDTO> result = cache.listPSNsForDomainsPaginated(domainNames, config);
		LOGGER.debug("listPSNsForDomainsPaginated found {} pseudonyms for given pagination config", result.size());
		return result;
	}

	@Override
	public long countPSNsForDomains(List<String> domainNames, PaginationConfig config)
			throws InvalidParameterException, UnknownDomainException
	{
		LOGGER.debug("countPSNsForDomains for domain {} with {}", domainNames, config);
		checkParameter(domainNames, "domainName");
		checkParameter(config, "config");
		long count = cache.countPSNsForDomainsPaginated(domainNames, config);
		LOGGER.debug("countPSNsForDomains found {} pseudonyms for given pagination filter", count);
		return count;
	}

	@Override
	public List<DomainOutDTO> getDomainsForPrefix(String prefix)
			throws InvalidParameterException
	{
		LOGGER.debug("getDomainsForPrefix with prefix {}", prefix);
		checkParameter(prefix, "prefix");
		List<DomainOutDTO> result = cache.getDomainsForPrefix(prefix);
		LOGGER.debug("getDomainsForPrefix found {} domains", result.size());
		return result;
	}

	@Override
	public List<DomainOutDTO> getDomainsForSuffix(String suffix)
			throws InvalidParameterException
	{
		LOGGER.debug("getDomainsForSuffix with suffix {}", suffix);
		checkParameter(suffix, "suffix");
		List<DomainOutDTO> result = cache.getDomainsForSuffix(suffix);
		LOGGER.debug("getDomainsForSuffix found {} domains", result.size());
		return result;
	}

	@Override
	public GeneratorAlphabetRestriction getRestrictionForCheckDigitClass(String checkDigitClassName)
			throws InvalidCheckDigitClassException, InvalidParameterException
	{
		LOGGER.debug("getRestrictionForCheckDigitClass for class {}", checkDigitClassName);
		checkParameter(checkDigitClassName, "checkDigitClassName");
		GeneratorAlphabetRestriction result = CheckDigits.createCheckDigits(checkDigitClassName).getAlphabetRestriction();
		LOGGER.debug("alphabet restriction for class {} is {}", checkDigitClassName, result);
		return result;
	}

	@Override
	public boolean arePSNDeletable(String domainName)
			throws InvalidParameterException, UnknownDomainException
	{
		LOGGER.debug("arePSNDeletable for domain {}", domainName);
		checkParameter(domainName, "domainName");
		boolean result = cache.arePSNDeletable(domainName);
		LOGGER.debug("arePSNDeletable for domain {} is {}", domainName, result);
		return result;
	}

	@Override
	public Alphabet getAlphabet(String alphabetName)
			throws InvalidParameterException, InvalidAlphabetException
	{
		LOGGER.debug("getAlphabet {}", alphabetName);
		checkParameter(alphabetName, "alphabetName");
		Alphabet result = Alphabets.createAlphabet(alphabetName);
		LOGGER.debug("getAlphabet {} result is {}", alphabetName, result);
		return result;
	}
}
