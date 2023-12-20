package org.emau.icmvc.ttp.psn.frontend.controller;

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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainOutDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.PSNDTO;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;
import org.emau.icmvc.ttp.psn.frontend.controller.common.AbstractGPASBean;
import org.icmvc.ttp.web.model.WebFile;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DualListModel;

/**
 * Export psn value pairs of selected domain(s)
 *
 * @author Arne Blumentritt
 */
@ViewScoped
@ManagedBean(name = "exportController")
public class ExportController extends AbstractGPASBean
{
	public static final int MAX_FILE_NAME_LENGTH = 100;
	private DualListModel<DomainOutDTO> domainLists = new DualListModel<>();
	private WebFile webFile;

	@PostConstruct
	public void init()
	{
		domainLists.setSource(domainService.listDomains().stream().sorted().collect(Collectors.toList()));
	}

	public void onExport() throws UnknownDomainException, InvalidParameterException
	{
		webFile = new WebFile("gPAS");
		List<String> columns = new ArrayList<>(Arrays.asList(getBundle().getString("psn.originalValue"), getBundle().getString("psn.pseudonym")));
		if (domainLists.getTarget().size() > 1)
		{
			columns.add(getBundle().getString("psn.domain"));
		}
		webFile.setColumns(columns);
		
		List<PSNDTO> psns = new ArrayList<>();
		for (DomainOutDTO domain : domainLists.getTarget())
		{
			psns.addAll(domainService.listPSNs(domain.getName()));
		}

		// Content
		for (PSNDTO psn : psns)
		{
			if (domainLists.getTarget().size() > 1)
			{
				webFile.getElements().add(Arrays.asList(psn.getOriginalValue(), psn.getPseudonym(), psn.getDomainName()));
			}
			else
			{
				webFile.getElements().add(Arrays.asList(psn.getOriginalValue(), psn.getPseudonym()));
			}
		}
		
		// Filename
		StringBuilder sb = new StringBuilder();
		sb.append(getBundle().getString("export.fileName"));
		for (DomainOutDTO domain : domainLists.getTarget())
		{
			sb.append(" ").append(domain.getName());
		}
		String fileName = StringUtils.abbreviate(sb.toString(), "...", MAX_FILE_NAME_LENGTH);
		webFile.onDownload(fileName);
	}

	public DefaultStreamedContent getDownloadFile()
	{
		return webFile.getDownloadFile();
	}

	public DualListModel<DomainOutDTO> getDomainLists()
	{
		return domainLists;
	}

	public void setDomainLists(DualListModel<DomainOutDTO> domainLists)
	{
		this.domainLists = domainLists;
	}
}
