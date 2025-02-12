package org.emau.icmvc.ttp.psn.frontend.controller;

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

import java.io.Serial;
import java.io.Serializable;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainOutDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.InsertPairExceptionDTO;
import org.emau.icmvc.ganimed.ttp.psn.enums.AnonymisationResult;
import org.emau.icmvc.ganimed.ttp.psn.enums.DeletionResult;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DBException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DeletionForbiddenException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DomainIsFullException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.PSNErrorStrings;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.psn.utils.StringPair;
import org.emau.icmvc.ttp.psn.frontend.controller.common.AbstractGPASBean;
import org.emau.icmvc.ttp.psn.frontend.model.BatchInput;
import org.icmvc.ttp.web.model.WebFile;

@ViewScoped
@Named("batchController")
public class BatchController extends AbstractGPASBean implements Serializable
{
	@Serial
	private static final long serialVersionUID = 7854682797687251417L;
	// File
	private WebFile webFile;

	// Options
	private Action selectedAction;
	private List<String> domainNames;
	private String selectedDomain;
	private boolean fileContainsDomainName;
	private boolean generateNewPseudonyms;
	private boolean replaceSourceColumn;
	private String targetColumnName;
	public static final String VALUE = "VALUE";
	public static final String PSEUDONYM = "PSEUDONYM";
	public static final String DOMAIN = "DOMAIN";

	// Progress bar
	private int sum;
	private int progress;

	@PostConstruct
	public void init()
	{
		webFile = new WebFile("gPAS", VALUE);
		onNewUpload();
	}

	public void initImport()
	{
		if (FacesContext.getCurrentInstance().isPostback())
		{
			return;
		}
		setSelectedAction(Action.IMPORT);
		onChangeAction();
	}

	public void onNewUpload()
	{
		webFile.onNewUpload();

		selectedAction = null;
		domainNames = getDomains().stream().map(DomainOutDTO::getName).collect(Collectors.toList());
		selectedDomain = null;
		fileContainsDomainName = false;
		generateNewPseudonyms = false;
		replaceSourceColumn = false;
		targetColumnName = null;

		sum = 0;
		progress = 0;
	}

	public void onDoAction()
	{
		Instant start = Instant.now();

		Set<String> singleInputs = new HashSet<>();
		Set<BatchInput> importInputs = new HashSet<>();
		Map<String, String> resultMap = new HashMap<>();
		Set<String> errorKeys = new HashSet<>();
		boolean askForReplace;

		// Required to ask the user if he really wants to replace the elements if a
		// value was not found
		List<List<String>> resultElements = new ArrayList<>();

		int rowIndex = 0;
		for (List<String> row : webFile.getElements())
		{
			rowIndex++;
			if (row.size() > webFile.getSelectedColumn() && row.get(webFile.getSelectedColumn()) != null)
			{
				if (selectedAction.equals(Action.IMPORT))
				{
					importInputs.add(new BatchInput(row.get(webFile.getSelectedColumnForName(VALUE)),
							row.get(webFile.getSelectedColumnForName(PSEUDONYM)),
							fileContainsDomainName ? row.get(webFile.getSelectedColumnForName(DOMAIN)) : selectedDomain));
				}
				else
				{
					singleInputs.add(row.get(webFile.getSelectedColumn()));
				}
			}
			else
			{
				Object[] args = { rowIndex };
				logMessage(new MessageFormat(getBundle().getString("batch.message.warn.rowSkipped")).format(args), Severity.WARN);
			}
		}
		sum = selectedAction.equals(Action.IMPORT) ? importInputs.size() : singleInputs.size();
		progress = 0;

		switch (selectedAction)
		{
			case PSEUDONYMISE:
				if (generateNewPseudonyms)
				{
					try
					{
						for (List<String> split : splitSet(singleInputs, 10000))
						{
							resultMap.putAll(getServiceWithAutomaticNotification(selectedDomain).getOrCreatePseudonymForList(new HashSet<>(split), selectedDomain));
							progress += split.size();
						}
						Object[] args = { webFile.getElements().size(), time.getTimeInAutoFormat(Duration.between(start, Instant.now()).toMillis()) };
						logMessage(new MessageFormat(getBundle().getString("batch.message.info.done." + selectedAction)).format(args), Severity.INFO);

					}
					catch (UnknownDomainException | InvalidParameterException | DomainIsFullException e)
					{
						logMessage(e.getLocalizedMessage(), Severity.ERROR);
					}
				}
				else
				{
					try
					{
						for (List<String> split : splitSet(singleInputs, 10000))
						{
							resultMap.putAll(getService().getPseudonymForList(new HashSet<>(split), selectedDomain));
							progress += split.size();
						}
						Object[] args = { webFile.getElements().size(), time.getTimeInAutoFormat(Duration.between(start, Instant.now()).toMillis()) };
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
					for (List<String> split : splitSet(singleInputs, 10000))
					{
						resultMap.putAll(getService().getValueForList(new HashSet<>(split), selectedDomain));
						progress += split.size();
					}
					Object[] args = { webFile.getElements().size(), time.getTimeInAutoFormat(Duration.between(start, Instant.now()).toMillis()) };
					logMessage(new MessageFormat(getBundle().getString("batch.message.info.done." + selectedAction)).format(args), Severity.INFO);
				}
				catch (UnknownDomainException | InvalidParameterException e)
				{
					logMessage(e.getLocalizedMessage(), Severity.ERROR);
				}
				break;
			case IMPORT:
				try
				{
					long exceptionsCount = 0;

					Map<String, Set<BatchInput>> groupedImportInputs;
					if (fileContainsDomainName)
					{
						groupedImportInputs = importInputs.stream().collect(Collectors.groupingBy(BatchInput::getDomain, Collectors.toSet()));
					}
					else
					{
						groupedImportInputs = new HashMap<>();
						groupedImportInputs.put(selectedDomain, importInputs);
					}

					for (Map.Entry<String, Set<BatchInput>> domainImportInputs : groupedImportInputs.entrySet())
					{
						for (List<BatchInput> split : splitBatchInputSet(domainImportInputs.getValue(), 10000))
						{
							split.stream().map(BatchInput::getValue).collect(Collectors.toSet()).forEach(k -> resultMap.put(k, "OK"));
							List<StringPair> pairs = split.stream().map(b -> new StringPair(b.getValue(), b.getPseudonym())).toList();
							String domainName = domainImportInputs.getKey();
							List<InsertPairExceptionDTO> exceptions = getServiceWithAutomaticNotification(domainName).insertValuePseudonymPairs(pairs, domainName);
							for (InsertPairExceptionDTO exceptionDTO : exceptions)
							{
								resultMap.replace(exceptionDTO.getValue(), exceptionDTO.getErrorType().name());
							}
							exceptionsCount += exceptions.size();
							progress += split.size();
						}
					}

					Object[] args = { webFile.getElements().size() - exceptionsCount, time.getTimeInAutoFormat(Duration.between(start, Instant.now()).toMillis()) };
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
					for (List<String> split : splitSet(singleInputs, 10000))
					{
						Map<String, AnonymisationResult> anonymisationResult = getServiceWithAutomaticNotification(selectedDomain)
								.anonymiseAllEntriesForValues(new HashSet<>(split), selectedDomain);
						resultMap.putAll(anonymisationResult.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
										entry -> getBundle().getString("batch.action.ANONYMISE." + entry.getValue().toString()))));
						progress += split.size();
					}
					Object[] args = { webFile.getElements().size(), time.getTimeInAutoFormat(Duration.between(start, Instant.now()).toMillis()) };
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
					for (List<String> split : splitSet(singleInputs, 10000))
					{
						Map<String, DeletionResult> deletionResult = getServiceWithAutomaticNotification(selectedDomain)
								.deleteAllEntriesForValues(new HashSet<>(split), selectedDomain);
						resultMap.putAll(deletionResult.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
										entry -> getBundle().getString("batch.action.DELETE." + entry.getValue().toString()))));
						progress += split.size();
					}
					Object[] args = { webFile.getElements().size(), time.getTimeInAutoFormat(Duration.between(start, Instant.now()).toMillis()) };
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
			for (List<String> row : webFile.getElements())
			{
				String value = null;
				if (row.size() > webFile.getSelectedColumn())
				{
					value = resultMap.get(row.get(webFile.getSelectedColumn()));
				}

				if (value != null && PSNErrorStrings.isPSNErrorString(value))
				{
					errorKeys.add(value);
				}

				List<String> rowCopy = new ArrayList<>(row);
				if (rowCopy.size() <= webFile.getSelectedColumn())
				{
					rowCopy.add(webFile.getSelectedColumn(), null);
				}

				// Add column for result or error message
				if (replaceSourceColumn)
				{
					rowCopy.set(webFile.getSelectedColumn(), value);
				}
				else
				{
					rowCopy.add(webFile.getSelectedColumn() + resultColumn(), value);
				}
				resultElements.add(rowCopy);
			}

			// Ask for replace if a value was not found or is not valid for another reason
			askForReplace = !errorKeys.isEmpty();

			if (replaceSourceColumn)
			{
				webFile.getColumns().set(webFile.getSelectedColumn(), targetColumnName);
				calculateTargetColumnName();
			}
			else
			{
				webFile.getColumns().add(webFile.getSelectedColumn() + resultColumn(), targetColumnName);
				if (Action.IMPORT != selectedAction)
				{
					// Highlight added result column
					webFile.setSelectedColumnForName(VALUE, webFile.getSelectedColumn() + resultColumn());
				}
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
				if (errorKeys.stream().anyMatch(k -> k.equals(PSNErrorStrings.PAIR_PSEUDONYM_INVALID)))
				{
					logMessage(getBundle().getString("batch.message.warn.invalidPseudonym"), Severity.WARN);
				}
				if (errorKeys.stream().anyMatch(k -> k.equals(PSNErrorStrings.PAIR_VALUE_INVALID)))
				{
					logMessage(getBundle().getString("batch.message.warn.invalidValue"), Severity.WARN);
				}
				if (errorKeys.stream().anyMatch(k -> k.equals(PSNErrorStrings.PAIR_DIFFERENT_PSEUDONYM_FOR_VALUE_EXISTS)))
				{
					logMessage(getBundle().getString("batch.message.warn.differentPsnForValueExists"), Severity.WARN);
				}
				if (errorKeys.stream().anyMatch(k -> k.equals(PSNErrorStrings.PAIR_DIFFERENT_VALUE_FOR_PSEUDONYM_EXISTS)))
				{
					logMessage(getBundle().getString("batch.message.warn.differentValueForPseudonymExists"), Severity.WARN);
				}
			}
			webFile.setElements(resultElements);
			webFile.setProcessed(true);
		}
		progress = 0;
	}

	private int resultColumn()
	{
		if (Action.IMPORT.equals(selectedAction))
		{
			return fileContainsDomainName ? 3 : 2;
		}
		else
			return 1;
	}

	public void onDownload()
	{
		webFile.onDownload(getBundle().getString("batch.fileName." + selectedAction.name()));
	}

	public void onChangeAction()
	{
		if (selectedAction == Action.IMPORT)
		{
			webFile.setSelectedColumnForName(PSEUDONYM, 1);
		}
		else
		{
			webFile.setSelectedColumnForName(PSEUDONYM, null);
		}
		calculateTargetColumnName();
	}

	public void onChangeFileContainsDomain()
	{
		if (fileContainsDomainName)
		{
			webFile.setSelectedColumnForName(DOMAIN, 2);
		}
		else
		{
			webFile.setSelectedColumnForName(DOMAIN, null);
		}
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
		else if (selectedAction == Action.IMPORT)
		{
			targetColumnName = getBundle().getString("batch.option.targetColumnName.import");
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

	private Collection<List<BatchInput>> splitBatchInputSet(Set<BatchInput> set, int size)
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
			return 1;
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

	public boolean isFileContainsDomainName()
	{
		return fileContainsDomainName;
	}

	public void setFileContainsDomainName(boolean fileContainsDomainName)
	{
		this.fileContainsDomainName = fileContainsDomainName;
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

	public List<Action> getActionsWithoutImport()
	{
		return Arrays.stream(getActions()).filter(a -> !Action.IMPORT.equals(a)).toList();
	}

	public enum Action
	{
		PSEUDONYMISE, DEPSEUDONYMISE, IMPORT, ANONYMISE, DELETE
	}
}
