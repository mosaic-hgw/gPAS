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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.psn.config.DomainConfig;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainOutDTO;
import org.emau.icmvc.ganimed.ttp.psn.enums.ForceCache;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DomainInUseException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidCheckDigitClassException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidGeneratorException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParentDomainException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;
import org.emau.icmvc.ttp.psn.frontend.beans.PsnClassPathProvider;
import org.emau.icmvc.ttp.psn.frontend.controller.common.AbstractGPASBean;
import org.emau.icmvc.ttp.psn.frontend.util.Alphabets;
import org.emau.icmvc.ttp.psn.frontend.util.Generators;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import static org.emau.icmvc.ttp.psn.frontend.util.SessionMapKeys.SELECTED_DOMAIN;

@ViewScoped
@ManagedBean(name = "domainController")
public class DomainController extends AbstractGPASBean
{
	private TreeNode selectedDomainNode;
	private DomainOutDTO selectedDomain;
	private boolean deleteConfirmation;
	private String customAlphabet;
	private Mode mode = Mode.READ;
	private String psnPrefix;
	private String psnSuffix;
	private int psnLength;

	@ManagedProperty(value = "#{ClassPathProvider}")
	private PsnClassPathProvider provider;

	public void onShowDetails()
	{
		setSelectedDomainFromNode();
		// Alphabet is a custom alphabet
		if (selectedDomain.getAlphabet() != null && !provider.getAlphabetMap().containsValue(selectedDomain.getAlphabet()))
		{
			customAlphabet = selectedDomain.getAlphabet().replace(",", "");
		}
		mode = Mode.READ;
	}

	public String onShowPsns()
	{
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		Map<String, Object> sessionMap = externalContext.getSessionMap();
		setSelectedDomainFromNode();
		sessionMap.put(SELECTED_DOMAIN, selectedDomain.getName());

		return "/html/internal/edit.xhml?faces-redirect=true";
	}

	public void onNew()
	{
		selectedDomain = new DomainOutDTO();
		selectedDomain.setConfig(new DomainConfig());
		selectedDomain.setAlphabet("org.emau.icmvc.ganimed.ttp.psn.alphabets.Numbers");
		selectedDomain.setCheckDigitClass("org.emau.icmvc.ganimed.ttp.psn.generator.Verhoeff");
		mode = Mode.NEW;
		psnPrefix = null;
		psnSuffix = null;
		psnLength = DomainConfig.DEFAULT_PSN_LENGTH;

	}

	public void onNewChild()
	{
		List<String> parents = Collections.singletonList(((DomainOutDTO) selectedDomainNode.getData()).getName());
		onNew();
		selectedDomain.setParentDomainNames(parents);
	}

	public void onNewBrother()
	{
		List<String> parents = ((DomainOutDTO) selectedDomainNode.getData()).getParentDomainNames();
		onNew();
		selectedDomain.setParentDomainNames(parents);
	}

	public void onEdit()
	{
		onShowDetails();
		mode = Mode.EDIT;
	}

	public void onSave()
	{
		if (customAlphabet != null)
		{
			Object[] args = null;
			if (customAlphabet.contains(" "))
			{
				args = new Object[] { " " };
			}
			if (customAlphabet.contains(","))
			{
				args = new Object[] { "," };
			}

			if (args != null)
			{
				logMessage(new MessageFormat(getBundle().getString("domains.message.warn.alphabet.illegalCharacter")).format(args), Severity.WARN);
				return;
			}

			for (String letter : customAlphabet.split(""))
			{
				if (customAlphabet.length() - customAlphabet.replace(letter, "").length() > 1)
				{
					args = new Object[] { letter };
					logMessage(new MessageFormat(getBundle().getString("domains.message.warn.alphabet.characterTwice")).format(args), Severity.WARN);
					return;
				}
			}
		}

		try
		{
			logger.debug("prefix '{}, suffix '{}', length '{}'", psnPrefix, psnSuffix, psnLength);
			// validate psnPrefix, psnSuffix, and psnLength and throw an InvalidArgumentException,
			// if (psnLength + psnPrefix.length + psnSuffix.length) > DomainConfig.getMaxNumberOfCharactersInFullyQualifiedPsn()
			DomainConfig.checkLength(psnPrefix, psnSuffix, psnLength);
			// reset PSN config to avoid InvalidArgumentException triggered by parts which have not yet been set to smaller values
			selectedDomain.getConfig().setPsnPrefix("");
			selectedDomain.getConfig().setPsnSuffix("");
			selectedDomain.getConfig().setPsnLength(0);
			// now set PSN config to current values
			selectedDomain.getConfig().setPsnPrefix(getPsnPrefix());
			selectedDomain.getConfig().setPsnSuffix(getPsnSuffix());
			selectedDomain.getConfig().setPsnLength(getPsnLength());
			selectedDomain.setAlphabet(customAlphabet == null ? selectedDomain.getAlphabet() : String.join(",", Arrays.asList(customAlphabet.split(""))));
			Object[] args = { getDomainLabel(selectedDomain) };
			if (mode == Mode.EDIT)
			{
				if (selectedDomain.getNumberOfPseudonyms() == 0)
				{
					domainService.updateDomain(selectedDomain);
				}
				else
				{
					domainService.updateDomainInUse(selectedDomain.getName(), selectedDomain.getLabel(), selectedDomain.getComment(), selectedDomain.getConfig()
							.isSendNotificationsWeb(), selectedDomain.getConfig().isPsnsDeletable());
				}
				logMessage(new MessageFormat(getBundle().getString("domains.message.info.updated")).format(args), Severity.INFO);
			}
			else
			{
				if (StringUtils.isBlank(selectedDomain.getName()))
				{
					selectedDomain.setName(selectedDomain.getLabel());
				}
				domainService.addDomain(selectedDomain);
				logMessage(new MessageFormat(getBundle().getString("domains.message.info.saved")).format(args), Severity.INFO);
			}
			customAlphabet = null;
			loadDomains();
		}
		catch (InvalidParameterException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.WARN);
		}
		catch (InvalidAlphabetException | InvalidCheckDigitClassException | InvalidGeneratorException | DomainInUseException | UnknownDomainException | InvalidParentDomainException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	public void onDelete()
	{
		setSelectedDomainFromNode();
		try
		{
			Object[] args = { getDomainLabel(selectedDomain.getName()) };
			domainService.deleteDomainWithPSNs(selectedDomain.getName());
			logMessage(new MessageFormat(getBundle().getString("domains.message.info.deleted")).format(args), Severity.INFO);
			loadDomains();
		}
		catch (DomainInUseException | UnknownDomainException | InvalidParameterException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	public void onCancel()
	{
		loadDomains();
		selectedDomainNode = null;
		customAlphabet = null;
	}

	public TreeNode getDomainTree()
	{
		// Create fake root for all domains
		TreeNode gPASRoot = new DefaultTreeNode(new DomainOutDTO());
		((DomainOutDTO) gPASRoot.getData()).setName(ROOT_DOMAIN);
		gPASRoot.setExpanded(true);

		// Get all root domains that have no parent
		List<TreeNode> rootTrees = new ArrayList<>();
		for (DomainOutDTO domainDTO : getDomains())
		{
			if (domainDTO.getParentDomainNames() == null || domainDTO.getParentDomainNames().isEmpty())
			{
				rootTrees.add(new DefaultTreeNode(domainDTO));
			}
		}

		// Sort root trees alphabetically
		rootTrees = rootTrees.stream().sorted(Comparator.comparing(c -> (DomainOutDTO) c.getData())).collect(Collectors.toList());

		// Add all to fake gPAS root and find children recursively
		for (TreeNode root : rootTrees)
		{
			gPASRoot.getChildren().add(root);
			searchChildren(root);
		}

		return gPASRoot;
	}

	private void searchChildren(TreeNode parent)
	{
		// Open parent in frontend
		parent.setExpanded(true);

		// Search in all domains for childs of this parent
		// Create temp list to sort alphabetically
		List<TreeNode> children = new ArrayList<>();
		for (DomainOutDTO domainDTO : getDomains())
		{
			if (domainDTO.getParentDomainNames().contains(((DomainOutDTO) parent.getData()).getName()))
			{
				children.add(new DefaultTreeNode(domainDTO));
			}
		}
		parent.getChildren().addAll(children.stream().sorted(Comparator.comparing(c -> (DomainOutDTO) c.getData())).collect(Collectors.toList()));

		// Get children for each child
		parent.getChildren().forEach(c -> searchChildren((TreeNode) c));
	}

	public Boolean getEditable()
	{
		return selectedDomainNode == null || (selectedDomainNode.getData() != null && ((DomainOutDTO) selectedDomainNode.getData()).getNumberOfPseudonyms() == 0);
	}

	public Boolean getDeletable()
	{
		return selectedDomainNode != null && getEditable()
				&& selectedDomainNode.getChildCount() == 0;
	}

	public List<String> getAlphabets()
	{
		return provider.getAlphabetMap().values().stream().map(String.class::cast).sorted().collect(Collectors.toList());
	}

	public List<String> getCheckDigitGenerators()
	{
		return provider.getGeneratorMap().values().stream().map(String.class::cast).sorted().collect(Collectors.toList());
	}

	public List<String> getCheckDigitGeneratorsForAlphabet(String alphabet)
	{
		List<String> generators = getCheckDigitGenerators();
		List<String> filteredGenerators = new ArrayList<>();

		List<String> generatorLength10 = Arrays.asList(Generators.VERHOEFF, Generators.VERHOEFF_GUMM, Generators.DAMM);
		List<String> generatorPrime = Collections.singletonList(Generators.REED_SOLOMON_LAGRANGE);

		filteredGenerators.add(Generators.NO_CHECK_DIGITS);
		if (Alphabets.NUMBERS.equals(alphabet))
		{
			filteredGenerators.addAll(generators.stream().filter(generatorLength10::contains).collect(Collectors.toList()));
		}
		else if (Alphabets.NUMBERS_X.equals(alphabet)
				|| Alphabets.SYMBOL_31.equals(alphabet))
		{
			filteredGenerators.addAll(generators.stream().filter(generatorPrime::contains).collect(Collectors.toList()));
		}
		else if (Alphabets.SYMBOL_32.equals(alphabet))
		{
			filteredGenerators.addAll(generators.stream().filter(Generators.HAMMING_CODE::equals).collect(Collectors.toList()));
		}
		else if (Alphabets.CUSTOM.equals(alphabet))
		{
			return generators;
		}

		return filteredGenerators;
	}

	public List<String> completeAvailableParentDomains(String s)
	{
		// match by label ignoring case, filter out current domain, filter out existing parents, sort by label
		return super.getDomains().stream()
				.filter(d -> getDomainLabel(d).toLowerCase().contains(s.toLowerCase()) && (selectedDomain == null || !d.getName().equals(selectedDomain.getName())))
				.filter(d -> selectedDomain == null || !selectedDomain.getParentDomainNames().contains(d.getName()))
				.sorted().map(DomainOutDTO::getName).collect(Collectors.toList());
	}

	/**
	 * Change selected generator if selected alphabet does not allow the current generator
	 */
	public void updateSelectedGenerator()
	{
		if (!getCheckDigitGeneratorsForAlphabet(selectedDomain.getAlphabet()).contains(selectedDomain.getCheckDigitClass()))
		{
			selectedDomain.setCheckDigitClass(getCheckDigitGeneratorsForAlphabet(selectedDomain.getAlphabet()).get(0));
		}
	}

	public String getGeneratorShort(String generator)
	{
		return generator.split("[.]")[generator.split("[.]").length - 1];
	}

	public ForceCache[] getCacheOptions()
	{
		return ForceCache.values();
	}

	public DomainOutDTO getSelectedDomain()
	{
		return selectedDomain;
	}

	public void setSelectedDomain(DomainOutDTO selectedDomain)
	{
		this.selectedDomain = selectedDomain;
	}

	public TreeNode getSelectedDomainNode()
	{
		return selectedDomainNode;
	}

	public void setSelectedDomainNode(TreeNode selectedDomainNode)
	{
		this.selectedDomainNode = selectedDomainNode;
	}

	public void setSelectedDomainFromNode()
	{
		selectedDomain = (DomainOutDTO) selectedDomainNode.getData();
		psnPrefix = selectedDomain.getConfig().getPsnPrefix();
		psnSuffix = selectedDomain.getConfig().getPsnSuffix();
		psnLength = selectedDomain.getConfig().getPsnLength();
		deleteConfirmation = false;
	}

	public boolean isDeleteConfirmation()
	{
		return deleteConfirmation;
	}

	public void setDeleteConfirmation(boolean deleteConfirmation)
	{
		this.deleteConfirmation = deleteConfirmation;
	}

	public String getCustomAlphabet()
	{
		return customAlphabet;
	}

	public void setCustomAlphabet(String customAlphabet)
	{
		this.customAlphabet = customAlphabet;
	}

	public Boolean getReadOnly()
	{
		return mode == Mode.READ;
	}

	public void setProvider(PsnClassPathProvider provider)
	{
		this.provider = provider;
	}

	public Mode getMode()
	{
		return mode;
	}

	public String getPsnPrefix()
	{
		return psnPrefix;
	}

	public void setPsnPrefix(String psnPrefix)
	{
		logger.debug("set PSN prefix '{}'", psnPrefix);
		this.psnPrefix = psnPrefix;
	}

	public String getPsnSuffix()
	{
		return psnSuffix;
	}

	public void setPsnSuffix(String psnSuffix)
	{
		logger.debug("set PSN suffix '{}'", psnSuffix);
		this.psnSuffix = psnSuffix;
	}

	public int getPsnLength()
	{
		return psnLength;
	}

	public void setPsnLength(int psnLength)
	{
		logger.debug("set PSN length '{}'", psnLength);
		this.psnLength = psnLength;
	}

	public static final boolean DYNAMIC_MAX_PSN_LENGTH = true;

	public int getMaxPsnTotalLength()
	{
		return DomainConfig.getMaxNumberOfCharactersInFullyQualifiedPsn();
	}

	public int getMaxPsnPrefixLength()
	{
		if (DYNAMIC_MAX_PSN_LENGTH)
		{
			return Math.max(0, getMaxPsnTotalLength() -
					(psnLength + (psnSuffix != null ? psnSuffix.length() : 0)));
		}
		return getMaxPsnTotalLength();
	}

	public int getMaxPsnSuffixLength()
	{
		if (DYNAMIC_MAX_PSN_LENGTH)
		{
			return Math.max(0, getMaxPsnTotalLength() -
					(psnLength + (psnPrefix != null ? psnPrefix.length() : 0)));
		}
		return getMaxPsnTotalLength();
	}

	public int getMaxPsnValueLength()
	{
		if (DYNAMIC_MAX_PSN_LENGTH)
		{
			return Math.max(1, getMaxPsnTotalLength() -
					((psnPrefix != null ? psnPrefix.length() : 0) + (psnSuffix != null ? psnSuffix.length() : 0)));
		}
		return getMaxPsnTotalLength();
	}

	public int getRemainingPsnTotalLength()
	{
		return getMaxPsnTotalLength() - (psnLength +
				(psnPrefix != null ? psnPrefix.length() : 0) +
				(psnSuffix != null ? psnSuffix.length() : 0));
	}

	public int getPsnLengthSum()
	{
		return psnLength +
				(psnPrefix != null ? psnPrefix.length() : 0) +
				(psnSuffix != null ? psnSuffix.length() : 0);
	}

	public enum Mode
	{
		READ, EDIT, NEW
	}
}
