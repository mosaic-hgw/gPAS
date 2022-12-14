package org.emau.icmvc.ganimed.ttp.psn;

/*-
 * ###license-information-start###
 * gPAS - a Generic Pseudonym Administration Service
 * __
 * Copyright (C) 2013 - 2022 Independent Trusted Third Party of the University Medicine Greifswald
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

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

@WebService(name = "DomainService")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@Stateless
@Remote(DomainManager.class)
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class DomainManagerBean extends GPASServiceBase implements DomainManager
{
	private static final Logger LOGGER = LogManager.getLogger(DomainManagerBean.class);

	@Override
	public void addDomain(DomainInDTO domainDTO)
			throws DomainInUseException, InvalidAlphabetException, InvalidCheckDigitClassException, InvalidGeneratorException, InvalidParameterException, InvalidParentDomainException,
			UnknownDomainException
	{
		String domainName = domainDTO.getName();
		if (LOGGER.isInfoEnabled())
		{
			LOGGER.info("addDomain with name " + domainName);
		}
		checkParameter(domainName, "domainName");
		cache.addDomain(domainDTO);
		if (LOGGER.isInfoEnabled())
		{
			LOGGER.info("new domain " + domainName + " persisted");
		}
	}

	@Override
	public void updateDomain(DomainInDTO domainDTO)
			throws DomainInUseException, InvalidAlphabetException, InvalidCheckDigitClassException, InvalidGeneratorException, InvalidParameterException, UnknownDomainException,
			InvalidParentDomainException
	{
		String domainName = domainDTO.getName();
		if (LOGGER.isInfoEnabled())
		{
			LOGGER.info("updateDomain with name " + domainName);
		}
		checkParameter(domainName, "domainName");
		cache.updateDomain(domainDTO);
		if (LOGGER.isInfoEnabled())
		{
			LOGGER.info("domain " + domainName + " updated");
		}
	}

	@Override
	public void updateDomainInUse(String domainName, String label, String comment) throws InvalidParameterException, UnknownDomainException
	{
		if (LOGGER.isInfoEnabled())
		{
			LOGGER.info("updateDomainInUse with name " + domainName);
		}
		checkParameter(domainName, "domainName");
		cache.updateDomainInUse(domainName, label, comment);
		if (LOGGER.isInfoEnabled())
		{
			LOGGER.info("domain " + domainName + " updated");
		}
	}

	@Override
	public void deleteDomain(String domainName) throws DomainInUseException, InvalidParameterException, UnknownDomainException
	{
		if (LOGGER.isInfoEnabled())
		{
			LOGGER.info("deleteDomain with name " + domainName);
		}
		checkParameter(domainName, "domainName");
		cache.deleteDomain(domainName);
		if (LOGGER.isInfoEnabled())
		{
			LOGGER.info("domain " + domainName + " deleted");
		}
	}

	@Override
	public DomainOutDTO getDomain(String domainName) throws InvalidParameterException, UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("getDomainObject with name " + domainName);
		}
		checkParameter(domainName, "domainName");
		DomainOutDTO result = cache.getDomainDTO(domainName);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("domain found");
		}
		return result;
	}

	@Override
	public List<DomainOutDTO> listDomains()
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("listDomains");
		}
		List<DomainOutDTO> result = cache.listDomains();
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("listDomains found " + result.size() + " domains");
		}
		return result;
	}

	@Override
	public List<PSNDTO> listPSNs(String domainName) throws InvalidParameterException, UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("listPSNs for domain " + domainName);
		}
		checkParameter(domainName, "domainName");
		List<PSNDTO> result = cache.listPSNs(domainName);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("listPSNs found " + result.size() + " pseudonyms");
		}
		return result;
	}

	@Override
	public List<PSNDTO> listPSNsPaginated(String domainName, PaginationConfig config) throws InvalidParameterException, UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("listPSNsPaginated for domain " + domainName + " with " + config);
		}
		checkParameter(domainName, "domainName");
		checkParameter(config, "config");
		List<PSNDTO> result = cache.listPSNsForDomainsPaginated(Collections.singletonList(domainName), config);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("listPSNsPaginated found " + result.size() + " pseudonyms for given pagination config");
		}
		return result;
	}

	@Override
	public long countPSNs(String domainName, PaginationConfig config) throws InvalidParameterException, UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("countPSNs for domain " + domainName + " with " + config);
		}
		checkParameter(domainName, "domainName");
		checkParameter(config, "config");
		long count = cache.countPSNsForDomainsPaginated(Collections.singletonList(domainName), config);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("countPSNs found " + count + " pseudonyms for given pagination filter");
		}
		return count;
	}

	@Override
	public List<PSNDTO> listPSNsForDomainsPaginated(List<String> domainNames, PaginationConfig config) throws InvalidParameterException, UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("listPSNsForDomainsPaginated for domains " + domainNames + " with " + config);
		}
		checkParameter(domainNames, "domainName");
		checkParameter(config, "config");
		List<PSNDTO> result = cache.listPSNsForDomainsPaginated(domainNames, config);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("listPSNsForDomainsPaginated found " + result.size() + " pseudonyms for given pagination config");
		}
		return result;
	}

	@Override
	public long countPSNsForDomains(List<String> domainNames, PaginationConfig config) throws InvalidParameterException, UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("countPSNsForDomains for domain " + domainNames + " with " + config);
		}
		checkParameter(domainNames, "domainName");
		checkParameter(config, "config");
		long count = cache.countPSNsForDomainsPaginated(domainNames, config);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("countPSNsForDomains found " + count + " pseudonyms for given pagination filter");
		}
		return count;
	}

	@Override
	public List<DomainOutDTO> getDomainsForPrefix(String prefix) throws InvalidParameterException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("getDomainsForPrefix with prefix " + prefix);
		}
		checkParameter(prefix, "prefix");
		List<DomainOutDTO> result = cache.getDomainsForPrefix(prefix);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("getDomainsForPrefix found " + result.size() + " domains");
		}
		return result;
	}

	@Override
	public List<DomainOutDTO> getDomainsForSuffix(String suffix) throws InvalidParameterException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("getDomainsForSuffix with suffix " + suffix);
		}
		checkParameter(suffix, "suffix");
		List<DomainOutDTO> result = cache.getDomainsForSuffix(suffix);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("getDomainsForSuffix found " + result.size() + " domains");
		}
		return result;
	}

	@Override
	public GeneratorAlphabetRestriction getRestrictionForCheckDigitClass(String checkDigitClassName) throws InvalidCheckDigitClassException, InvalidParameterException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("getRestrictionForCheckDigitClass for class " + checkDigitClassName);
		}
		checkParameter(checkDigitClassName, "checkDigitClassName");
		GeneratorAlphabetRestriction result = cache.getRestrictionForCheckDigitClass(checkDigitClassName);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("alphabet restrition for class " + checkDigitClassName + " is " + result.toString());
		}
		return result;
	}

	@Override
	public boolean arePSNDeletable(String domainName) throws InvalidParameterException, UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("arePSNDeletable for domain " + domainName);
		}
		checkParameter(domainName, "domainName");
		boolean result = cache.arePSNDeletable(domainName);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("arePSNDeletable for domain " + domainName + " is " + result);
		}
		return result;
	}
}
