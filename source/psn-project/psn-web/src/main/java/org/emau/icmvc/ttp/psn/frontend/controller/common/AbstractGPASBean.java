package org.emau.icmvc.ttp.psn.frontend.controller.common;

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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.psn.DomainManager;
import org.emau.icmvc.ganimed.ttp.psn.PSNManager;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainOutDTO;
import org.icmvc.ttp.web.controller.AbstractBean;

public abstract class AbstractGPASBean extends AbstractBean
{
	@EJB(lookup = "java:global/gpas/psn-ejb/PSNManagerBean!org.emau.icmvc.ganimed.ttp.psn.PSNManager")
	protected PSNManager service;

	@EJB(lookup = "java:global/gpas/psn-ejb/DomainManagerBean!org.emau.icmvc.ganimed.ttp.psn.DomainManager")
	protected DomainManager domainService;

	private Map<String, DomainOutDTO> domainMap;
	private List<DomainOutDTO> domains;
	protected static final String TOOL = "gPAS";
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
		return domainMap.get(domainName);
	}

	public String getDomainLabel(String domainName)
	{
		DomainOutDTO domain = getDomain(domainName);
		String label = domain != null ? domain.getLabel() : null;
		return StringUtils.isNotBlank(label) ? label : domainName;
	}

	protected void loadDomains()
	{
		domainMap = new LinkedHashMap<>();
		domains = new ArrayList<>();
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
}
