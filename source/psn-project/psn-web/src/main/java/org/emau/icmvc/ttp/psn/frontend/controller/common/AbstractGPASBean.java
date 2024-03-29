package org.emau.icmvc.ttp.psn.frontend.controller.common;

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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.psn.DomainManager;
import org.emau.icmvc.ganimed.ttp.psn.PSNManager;
import org.emau.icmvc.ganimed.ttp.psn.PSNManagerWithNotification;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainOutDTO;
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
import org.icmvc.ttp.web.controller.AbstractBean;

public abstract class AbstractGPASBean extends AbstractBean
{
	@EJB(lookup = "java:global/gpas/psn-ejb/PSNManagerBean!org.emau.icmvc.ganimed.ttp.psn.PSNManager")
	protected PSNManager service;
	@EJB(lookup = "java:global/gpas/psn-ejb/PSNManagerWithNotificationBean!org.emau.icmvc.ganimed.ttp.psn.PSNManagerWithNotification")
	protected PSNManagerWithNotification serviceWithNotification;

	@EJB(lookup = "java:global/gpas/psn-ejb/DomainManagerBean!org.emau.icmvc.ganimed.ttp.psn.DomainManager")
	protected DomainManager domainService;

	private Map<String, DomainOutDTO> domainMap;
	private List<DomainOutDTO> domains;
	protected static final String TOOL = "gPAS";
	protected static final String NOTIFICATION_CLIENT_ID = TOOL + "_Web";
	protected final String ROOT_DOMAIN = "ROOT_DOMAIN";

	@Override
	protected ResourceBundle getBundle()
	{
		FacesContext facesContext = FacesContext.getCurrentInstance();
		return facesContext.getApplication().getResourceBundle(facesContext, "msg");
	}

	public List<DomainOutDTO> getDomains()
	{
		if (domains == null)
		{
			loadDomains();
		}
		return domains;
	}

	public DomainOutDTO getDomain(String domainName)
	{
		return domainMap != null ? domainMap.get(domainName) : null;
	}

	public String getDomainLabel(String domainName)
	{
		DomainOutDTO domain = getDomain(domainName);
		String label = domain != null ? domain.getLabel() : null;
		return StringUtils.isNotBlank(label) ? label : domainName;
	}

	public String getDomainLabel(DomainOutDTO domain)
	{
		String label = domain != null ? domain.getLabel() : null;
		return StringUtils.isNotBlank(label) ? label : domain != null ? domain.getName() : null;
	}

	protected void loadDomains()
	{
		domainMap = new LinkedHashMap<>();
		domains = new ArrayList<>();
		// we could use <Stream>.toList() (since Java 16) but that is criticized by SonarQube which might be not ready for Java 17
		for (DomainOutDTO domainDTO : domainService.listDomains().stream().sorted().collect(Collectors.toList()))
		{
			domainMap.put(domainDTO.getName(), domainDTO);
			domains.add(domainDTO);
		}
	}
	
	public String getTool()
	{
		return TOOL;
	}

	public String getROOT_DOMAIN()
	{
		return ROOT_DOMAIN;
	}

	public boolean isSendingNotifications(String domain)
	{
		DomainOutDTO d = getDomain(domain);
		return d != null && d.getConfig().isSendNotificationsWeb();
	}

	protected String getOrCreatePseudonymFor(String value, String domainName)
			throws DBException, DomainIsFullException, InvalidParameterException, UnknownDomainException
	{
		if (isSendingNotifications(domainName))
		{
			return serviceWithNotification.getOrCreatePseudonymFor(NOTIFICATION_CLIENT_ID, value, domainName);
		}
		else
		{
			return service.getOrCreatePseudonymFor(value, domainName);
		}
	}
	protected Map<String, String> getOrCreatePseudonymForList(Set<String> values, String domainName)
			throws DBException, InvalidParameterException, UnknownDomainException, DomainIsFullException
	{
		if (isSendingNotifications(domainName))
		{
			return serviceWithNotification.getOrCreatePseudonymForList(NOTIFICATION_CLIENT_ID, values, domainName);
		}
		else
		{
			return service.getOrCreatePseudonymForList(values, domainName);
		}
	}
	protected void anonymiseEntry(String value, String domainName)
			throws DBException, InvalidParameterException, UnknownDomainException, UnknownValueException, ValueIsAnonymisedException
	{
		if (isSendingNotifications(domainName))
		{
			serviceWithNotification.anonymiseEntry(NOTIFICATION_CLIENT_ID, value, domainName);
		}
		else
		{
			service.anonymiseEntry(value, domainName);
		}
	}

	protected Map<String, AnonymisationResult> anonymiseEntries(Set<String> values, String domainName)
			throws DBException, InvalidParameterException, UnknownDomainException
	{
		if (isSendingNotifications(domainName))
		{
			return serviceWithNotification.anonymiseEntries(NOTIFICATION_CLIENT_ID, values, domainName);
		}
		else
		{
			return service.anonymiseEntries(values, domainName);
		}
	}

	protected void deleteEntry(String value, String domainName)
			throws DeletionForbiddenException, InvalidParameterException, UnknownDomainException, UnknownValueException
	{
		if (isSendingNotifications(domainName))
		{
			serviceWithNotification.deleteEntry(NOTIFICATION_CLIENT_ID, value, domainName);
		}
		else
		{
			service.deleteEntry(value, domainName);
		}
	}

	protected Map<String, DeletionResult> deleteEntries(Set<String> values, String domainName)
			throws DeletionForbiddenException, InvalidParameterException, UnknownDomainException
	{
		if (isSendingNotifications(domainName))
		{
			return serviceWithNotification.deleteEntries(NOTIFICATION_CLIENT_ID, values, domainName);
		}
		else
		{
			return service.deleteEntries(values, domainName);
		}
	}

	protected void insertValuePseudonymPair(String value, String pseudonym, String domainName)
			throws InsertPairException, InvalidParameterException, UnknownDomainException
	{
		if (isSendingNotifications(domainName))
		{
			serviceWithNotification.insertValuePseudonymPair(NOTIFICATION_CLIENT_ID, value, pseudonym, domainName);
		}
		else
		{
			service.insertValuePseudonymPair(value, pseudonym, domainName);
		}
	}

	protected List<InsertPairExceptionDTO> insertValuePseudonymPairs(Map<String, String> pairs, String domainName)
			throws InvalidParameterException, UnknownDomainException
	{
		if (isSendingNotifications(domainName))
		{
			return serviceWithNotification.insertValuePseudonymPairs(NOTIFICATION_CLIENT_ID, pairs, domainName);
		}
		else
		{
			return service.insertValuePseudonymPairs(pairs, domainName);
		}
	}
}
