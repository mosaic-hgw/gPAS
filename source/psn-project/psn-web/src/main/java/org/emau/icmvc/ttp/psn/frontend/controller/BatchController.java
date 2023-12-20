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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.emau.icmvc.ganimed.ttp.psn.dto.DomainOutDTO;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DBException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DeletionForbiddenException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DomainIsFullException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.PSNErrorStrings;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;
import org.emau.icmvc.ttp.psn.frontend.controller.common.AbstractGPASBean;
import org.icmvc.ttp.web.model.WebFile;

@ViewScoped
@ManagedBean(name = "batchController")
public class BatchController extends AbstractGPASBean
{
	// File
	private WebFile webFile;

	// Options
	private Action selectedAction;
	private List<String> domainNames;
	private String selectedDomain;
	private boolean generateNewPseudonyms;
	private boolean replaceSourceColumn;
	private String targetColumnName;

	// Progress bar
	private int sum;
	private int progress;

	@PostConstruct
	public void init()
	{
		webFile = new WebFile("gPAS");
		onNewUpload();
	}

	public void onNewUpload()
	{
		webFile.onNewUpload();

		selectedAction = null;
		domainNames = getDomains().stream().map(DomainOutDTO::getName).collect(Collectors.toList());
		selectedDomain = null;
		generateNewPseudonyms = false;
		replaceSourceColumn = false;
		targetColumnName = null;

		sum = 0;
		progress = 0;
	}

	public void onDoAction()
	{
		Set<String> inputs = new HashSet<>();
		Map<String, String> resultMap = new HashMap<>();
		boolean askForReplace = false;

		// Required to ask the user if he really wants to replace the elements if a
		// value was not found
		List<List<String>> resultElements = new ArrayList<>();

		int rowIndex = 0;
		for (List<String> row : webFile.getElements())
		{
			rowIndex++;
			if (row.size() > webFile.getSelectedColumn() && row.get(webFile.getSelectedColumn()) != null)
			{
				inputs.add(row.get(webFile.getSelectedColumn()));
			}
			else
			{
				Object[] args = { rowIndex };
				logMessage(new MessageFormat(getBundle().getString("batch.message.warn.rowSkipped")).format(args), Severity.WARN);
			}
		}
		Object[] args = { webFile.getElements().size() };
		sum = inputs.size();
		progress = 0;
		boolean withNotifications = isSendingNotifications(selectedDomain);

		switch (selectedAction)
		{
			case PSEUDONYMISE:
				if (generateNewPseudonyms)
				{
					try
					{
						for (List<String> split : splitSet(inputs, 100000))
						{
							resultMap.putAll(getOrCreatePseudonymForList(new HashSet<>(split), selectedDomain));
							progress += split.size();
						}
						logMessage(new MessageFormat(getBundle().getString("batch.message.info.done." + selectedAction)).format(args), Severity.INFO);

					}
					catch (DBException | UnknownDomainException | InvalidParameterException | DomainIsFullException e)
					{
						logMessage(e.getLocalizedMessage(), Severity.ERROR);
					}
				}
				else
				{
					try
					{
						for (List<String> split : splitSet(inputs, 100000))
						{
							resultMap.putAll(service.getPseudonymForList(new HashSet<>(split), selectedDomain));
							progress += split.size();
						}
						logMessage(new MessageFormat(getBundle().getString("batch.message.info.done." + selectedAction)).format(args), Severity.INFO);
					}
					catch (InvalidParameterException | UnknownDomainException e)
					{
						logMessage(e.getLocalizedMessage(), Severity.ERROR);
					}
				}
				break;
			case DEPSEUDONYMISE:
				try
				{
					for (List<String> split : splitSet(inputs, 1000))
					{
						resultMap.putAll(service.getValueForList(new HashSet<>(split), selectedDomain));
						progress += split.size();
					}
					logMessage(new MessageFormat(getBundle().getString("batch.message.info.done." + selectedAction)).format(args), Severity.INFO);
				}
				catch (UnknownDomainException | InvalidParameterException e)
				{
					logMessage(e.getLocalizedMessage(), Severity.ERROR);
				}
				break;
			case ANONYMISE:
				try
				{
					for (List<String> split : splitSet(inputs, 1000))
					{
						resultMap.putAll(anonymiseEntries(new HashSet<>(split), selectedDomain).entrySet().stream()
								.collect(Collectors.toMap(
										Map.Entry::getKey,
										entry -> getBundle().getString("batch.action.ANONYMISE." + entry.getValue().toString()))));
						progress += split.size();
					}
					logMessage(new MessageFormat(getBundle().getString("batch.message.info.done." + selectedAction)).format(args), Severity.INFO);
				}
				catch (UnknownDomainException | InvalidParameterException | DBException e)
				{
					logMessage(e.getLocalizedMessage(), Severity.ERROR);
				}
				break;
			case DELETE:
				try
				{
					for (List<String> split : splitSet(inputs, 1000))
					{
						resultMap.putAll(deleteEntries(new HashSet<>(split), selectedDomain).entrySet().stream()
								.collect(Collectors.toMap(
										Map.Entry::getKey,
										entry -> getBundle().getString("batch.action.DELETE." + entry.getValue().toString()))));
						progress += split.size();
					}
					logMessage(new MessageFormat(getBundle().getString("batch.message.info.done." + selectedAction)).format(args), Severity.INFO);
				}
				catch (UnknownDomainException | InvalidParameterException | DeletionForbiddenException e)
				{
					logMessage(e.getLocalizedMessage(), Severity.ERROR);
				}
				break;
			default:
				break;
		}

		if (!resultMap.isEmpty())
		{
			Set<String> errorKeys = new HashSet<>();

			for (List<String> row : webFile.getElements())
			{
				String value = null;
				if (row.size() > webFile.getSelectedColumn())
				{
					value = resultMap.get(row.get(webFile.getSelectedColumn()));
				}

				if (PSNErrorStrings.isPSNErrorString(value)) {
					errorKeys.add(value);
				}

				List<String> rowCopy = new ArrayList<>(row);
				if (rowCopy.size() <= webFile.getSelectedColumn())
				{
					rowCopy.add(webFile.getSelectedColumn(), null);
				}
				if (replaceSourceColumn)
				{
					rowCopy.set(webFile.getSelectedColumn(), value);
				}
				else
				{
					rowCopy.add(webFile.getSelectedColumn() + 1, value);
				}
				resultElements.add(rowCopy);
			}

			// Ask for replace if a value was not found or is not valid for an other reason
			askForReplace = !errorKeys.isEmpty();

			if (replaceSourceColumn)
			{
				webFile.getColumns().set(webFile.getSelectedColumn(), targetColumnName);
				calculateTargetColumnName();
			}
			else
			{
				webFile.getColumns().add(webFile.getSelectedColumn() + 1, targetColumnName);
				// Highlight added result column
				webFile.setSelectedColumn(webFile.getSelectedColumn() + 1);
				calculateTargetColumnName();
			}

			// TODO replace funktionalität einbauen
			if (askForReplace)
			{
				if (errorKeys.stream().anyMatch(PSNErrorStrings::isNotFoundErrorString))
				{
					logMessage(getBundle().getString("batch.message.warn.notFound." + selectedAction.name()), Severity.WARN);
				}
				if (errorKeys.stream().anyMatch(PSNErrorStrings::isInvalidErrorString))
				{
					logMessage(getBundle().getString("batch.message.warn.invalid." + selectedAction.name()), Severity.WARN);
				}
			}
			webFile.setElements(resultElements);
			webFile.setProcessed(true);
		}
		progress = 0;
	}

	public void onDownload()
	{
		webFile.onDownload(getBundle().getString("batch.fileName." + selectedAction.name()));
	}

	public void calculateTargetColumnName()
	{
		if (selectedAction == Action.PSEUDONYMISE)
		{
			targetColumnName = getBundle().getString("batch.option.targetColumnName.pseudonymOf") + " "
					+ webFile.getColumns().get(webFile.getSelectedColumn());
		}
		else if (selectedAction == Action.DEPSEUDONYMISE)
		{
			targetColumnName = getBundle().getString("batch.option.targetColumnName.valueOf") + " "
					+ webFile.getColumns().get(webFile.getSelectedColumn());
		}
		else if (selectedAction == Action.DELETE)
		{
			targetColumnName = getBundle().getString("batch.option.targetColumnName.deleteResult") + " "
					+ webFile.getColumns().get(webFile.getSelectedColumn());
		}
		else if (selectedAction == Action.ANONYMISE)
		{
			targetColumnName = getBundle().getString("batch.option.targetColumnName.anonymiseResult") + " "
					+ webFile.getColumns().get(webFile.getSelectedColumn());
		}
	}

	public WebFile getWebFile()
	{
		return webFile;
	}

	public void setWebFile(WebFile webFile)
	{
		this.webFile = webFile;
	}

	private Collection<List<String>> splitSet(Set<String> set, int size)
	{
		AtomicInteger counter = new AtomicInteger();
		return set.stream()
				.collect(Collectors.groupingBy(it -> counter.getAndIncrement() / size))
				.values();
	}

	public Integer getProgress()
	{
		if (sum == 0)
		{
			return 0;
		}
		else
		{
			int result = progress * 100 / sum;
			return result == 0 ? 1 : result;
		}
	}
	
	public List<String> getDomainNames()
	{
		return domainNames;
	}

	public String getSelectedDomain()
	{
		return selectedDomain;
	}

	public void setSelectedDomain(String selectedDomain)
	{
		this.selectedDomain = selectedDomain;
	}

	public boolean getGenerateNewPseudonyms()
	{
		return generateNewPseudonyms;
	}

	public void setGenerateNewPseudonyms(boolean generateNewPseudonyms)
	{
		this.generateNewPseudonyms = generateNewPseudonyms;
	}

	public boolean getReplaceSourceColumn()
	{
		return replaceSourceColumn;
	}

	public void setReplaceSourceColumn(boolean replaceSourceColumn)
	{
		this.replaceSourceColumn = replaceSourceColumn;
	}

	public String getTargetColumnName()
	{
		return targetColumnName;
	}

	public void setTargetColumnName(String targetColumnName)
	{
		this.targetColumnName = targetColumnName;
	}

	public Action getSelectedAction()
	{
		return selectedAction;
	}

	public void setSelectedAction(Action selectedAction)
	{
		this.selectedAction = selectedAction;
	}

	public Action[] getActions()
	{
		return Action.values();
	}

	public enum Action
	{
		PSEUDONYMISE, DEPSEUDONYMISE, ANONYMISE, DELETE
	}
}
