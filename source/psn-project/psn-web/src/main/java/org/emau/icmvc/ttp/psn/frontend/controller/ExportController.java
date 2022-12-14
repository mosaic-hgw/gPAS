package org.emau.icmvc.ttp.psn.frontend.controller;

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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import java.text.SimpleDateFormat;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainOutDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.PSNDTO;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;
import org.emau.icmvc.ttp.psn.frontend.controller.common.AbstractGPASBean;
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
	private DualListModel<DomainOutDTO> domainLists = new DualListModel<>();
	private DefaultStreamedContent downloadFile;

	@PostConstruct
	public void init()
	{
		domainLists.setSource(domainService.listDomains().stream().sorted().collect(Collectors.toList()));
	}

	public void onExport() throws UnknownDomainException, InvalidParameterException
	{
		List<PSNDTO> psns = new ArrayList<>();

		for (DomainOutDTO domain : domainLists.getTarget())
		{
			psns.addAll(domainService.listPSNs(domain.getName()));
		}

		StringBuilder result = new StringBuilder();

		// Header
		result.append("sep=;");
		result.append("\r\n");

		result.append(getBundle().getString("psn.originalValue")).append(";");
		result.append(getBundle().getString("psn.pseudonym")).append(";");
		if (domainLists.getTarget().size() > 1)
		{
			result.append(getBundle().getString("psn.domain")).append(";");
		}
		result.append("\r\n");

		// Content
		for (PSNDTO psn : psns)
		{
			result.append(psn.getOriginalValue()).append(";");
			result.append(psn.getPseudonym()).append(";");
			if (domainLists.getTarget().size() > 1)
			{
				result.append(psn.getDomainName()).append(";");
			}
			result.append("\r\n");
		}

		// File name
		SimpleDateFormat sdf = new SimpleDateFormat(getCommonBundle("en").getString("ui.date.pattern.date"));

		StringBuilder fileName = new StringBuilder(sdf.format(new Date()));
		fileName.append(" ").append(getBundle().getString("export.fileName"));
		for (DomainOutDTO domain : domainLists.getTarget())
		{
			fileName.append(" ").append(domain.getName());
		}
		fileName.append(" gPAS");
		fileName.append(".csv");

		// Create download stream
		InputStream stream = new ByteArrayInputStream(result.toString().getBytes(StandardCharsets.UTF_16LE));

		downloadFile = DefaultStreamedContent.builder()
				.stream(() -> stream)
				.contentType("text/csv")
				.name(fileName.toString())
				.contentEncoding(StandardCharsets.UTF_16LE.name())
				.build();
	}

	public DefaultStreamedContent getDownloadFile()
	{
		return downloadFile;
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
