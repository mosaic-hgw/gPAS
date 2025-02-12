package org.emau.icmvc.ttp.psn.frontend.controller.common;

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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.psn.DomainManager;
import org.emau.icmvc.ganimed.ttp.psn.PSNManager;
import org.emau.icmvc.ganimed.ttp.psn.StatisticManager;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainOutDTO;
import org.icmvc.ttp.web.controller.AbstractBean;
import org.icmvc.ttp.web.util.WebAuthContext;

public abstract class AbstractGPASBean extends AbstractBean
{
	protected static final String TOOL = ServiceHelper.TOOL;

	@Inject
	private ServiceHelper serviceHelper;

	private Map<String, DomainOutDTO> domainMap;
	private List<DomainOutDTO> domains;
	protected final String ROOT_DOMAIN = "ROOT_DOMAIN";

	public ServiceHelper getServiceHelper()
	{
		if (serviceHelper == null)
		{
			// https://github.com/eclipse-ee4j/mojarra/issues/4308
			// If the above does not work here, too: "@Inject private ServiceHelper serviceHelper;"
			// but "CDI.current().select(ServiceHelper.class).get();" helps

			serviceHelper = CDI.current().select(ServiceHelper.class).get();
		}

		return serviceHelper;
	}

	public PSNManager getService()
	{
		return getServiceHelper().getService();
	}

	public PSNManager getServiceWithAutomaticNotification(boolean notify)
	{
		return getServiceHelper().getServiceWithAutomaticNotification(notify);
	}

	public PSNManager getServiceWithAutomaticNotification(String domainName)
	{
		return getServiceHelper().getServiceWithAutomaticNotification(isSendingNotifications(domainName));
	}

	public DomainManager getManager()
	{
		return getServiceHelper().getManager();
	}

	public StatisticManager getStatisticService()
	{
		return getServiceHelper().getStatisticService();
	}

	@Override public WebAuthContext getWebAuthContext()
	{
		return getServiceHelper().getWebAuthContext();
	}

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
		for (DomainOutDTO domainDTO : getManager().listDomains().stream().sorted().toList())
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
}
