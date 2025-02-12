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

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.faces.annotation.ManagedProperty;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.psn.alphabets.Alphabets;
import org.emau.icmvc.ganimed.ttp.psn.config.DomainConfig;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainInDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainOutDTO;
import org.emau.icmvc.ganimed.ttp.psn.enums.ForceCache;
import org.emau.icmvc.ganimed.ttp.psn.enums.ValidateViaParents;
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
import org.emau.icmvc.ttp.psn.frontend.beans.PsnClassPathProvider;
import org.emau.icmvc.ttp.psn.frontend.controller.common.AbstractGPASBean;
import org.emau.icmvc.ttp.psn.frontend.util.Generators;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import static org.emau.icmvc.ttp.psn.frontend.util.SessionMapKeys.SELECTED_DOMAIN;
import static org.emau.icmvc.ttp.util.SneakyThrowUtils.sneakyF;

@ViewScoped
@Named("domainController")
public class DomainController extends AbstractGPASBean implements Serializable
{
	@Serial
	private static final long serialVersionUID = 6711767325157398033L;
	private TreeNode<DomainOutDTO> selectedDomainNode;
	private DomainOutDTO selectedDomain;
	private boolean deleteConfirmation;
	private String customAlphabet;
	private Mode mode = Mode.READ;
	private String psnPrefix;
	private String psnSuffix;
	private int psnLength;
	private Alphabet tempAlphabet;
	private static final String CUSTOM_ALPHABET = "CUSTOM";

	@Inject
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

	public void onShowPsns() throws IOException
	{
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		Map<String, Object> sessionMap = externalContext.getSessionMap();
		setSelectedDomainFromNode();
		sessionMap.put(SELECTED_DOMAIN, selectedDomain.getName());
		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
		ec.redirect(ec.getRequestContextPath() + "/html/internal/edit.xhtml?faces-redirect=true");
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
		updateAlphabet();
	}

	public void updateAlphabet()
	{
		try
		{
			if (selectedDomain.getAlphabet().equals(CUSTOM_ALPHABET))
			{
				tempAlphabet = StringUtils.isNotEmpty(customAlphabet) ? Alphabets.createAlphabet(prepareCustomAlphabet(customAlphabet)) : null;
			}
			else
			{
				tempAlphabet = Alphabets.createAlphabet(selectedDomain.getAlphabet());
			}
		}
		catch (InvalidAlphabetException e)
		{
			logger.error(e.getLocalizedMessage());
		}
	}

	private String prepareCustomAlphabet(String customAlphabet)
	{
		boolean illegalCharacter = false;
		boolean doubleCharacter = false;
		if (customAlphabet.contains(" "))
		{
			logMessage(new MessageFormat(getBundle().getString("domains.message.warn.alphabet.illegalCharacter")).format(new Object[] { " " }), Severity.ERROR, Severity.WARN);
			illegalCharacter = true;
		}
		if (customAlphabet.contains(","))
		{
			logMessage(new MessageFormat(getBundle().getString("domains.message.warn.alphabet.illegalCharacter")).format(new Object[] { "," }), Severity.ERROR, Severity.WARN);
			illegalCharacter = true;
		}

		for (String letter : customAlphabet.split(""))
		{
			if (customAlphabet.length() - customAlphabet.replace(letter, "").length() > 1)
			{
				logMessage(new MessageFormat(getBundle().getString("domains.message.warn.alphabet.characterTwice")).format(new Object[] { letter }), Severity.ERROR, Severity.WARN);
				doubleCharacter = true;
			}
		}

		if (illegalCharacter || doubleCharacter)
		{
			return null;
		}

		return String.join(",", Arrays.asList(customAlphabet.split("")));
	}

	public void onNewChild()
	{
		List<String> parents = Collections.singletonList(selectedDomainNode.getData().getName());
		onNew();
		selectedDomain.setParentDomainNames(parents);
	}

	public void onNewBrother()
	{
		List<String> parents = selectedDomainNode.getData().getParentDomainNames();
		onNew();
		selectedDomain.setParentDomainNames(parents);
	}

	public void onEdit()
	{
		onShowDetails();
		updateAlphabet();
		mode = Mode.EDIT;
	}

	public void onSave()
	{
		if (selectedDomain.getAlphabet().equals(CUSTOM_ALPHABET))
		{
			selectedDomain.setAlphabet(prepareCustomAlphabet(customAlphabet));
		}

		try
		{
			logger.debug("prefix '{}, suffix '{}', length '{}'", psnPrefix, psnSuffix, psnLength);
			// validate psnPrefix, psnSuffix, and psnLength and throw an InvalidArgumentException,
			// if (psnLength + psnPrefix.length + psnSuffix.length + numCheckDigits) > DomainConfig.getMaxNumberOfCharactersInFullyQualifiedPsn()
			DomainConfig.checkPsnLength(psnPrefix, psnSuffix, psnLength, createCheckDigits());
			// reset PSN config to avoid InvalidArgumentException triggered by parts which have not yet been set to smaller values
			selectedDomain.getConfig().resetPsnPrefixSuffixAndLength();
			// now set PSN config to current values
			selectedDomain.getConfig().setPsnPrefixSuffixAndLength(getPsnPrefix(), getPsnSuffix(), getPsnLength());
			Object[] args = { getDomainLabel(selectedDomain) };
			if (mode == Mode.EDIT)
			{
				if (selectedDomain.getNumberOfPseudonyms() == 0)
				{
					getManager().updateDomain(selectedDomain);
				}
				else
				{
					getManager().updateDomainInUse(selectedDomain.getName(),
							selectedDomain.getLabel(),
							selectedDomain.getComment(),
							selectedDomain.getParentDomainNames(),
							selectedDomain.getConfig().isSendNotificationsWeb(),
							selectedDomain.getConfig().isPsnsDeletable());
				}
				logMessage(new MessageFormat(getBundle().getString("domains.message.info.updated")).format(args), Severity.INFO);
			}
			else
			{
				if (StringUtils.isBlank(selectedDomain.getName()))
				{
					selectedDomain.setName(selectedDomain.getLabel());
				}
				getManager().addDomain(selectedDomain);
				logMessage(new MessageFormat(getBundle().getString("domains.message.info.saved")).format(args), Severity.INFO);
			}
			customAlphabet = null;
			loadDomains();
		}
		catch (InvalidParameterException e)
		{
			if (e.getMessage().contains("isValidateValuesViaParents"))
			{
				logMessage(getBundle().getString("domains.message.InvalidParameterException.noParentsForValidation"), Severity.WARN);
				reloadParentDomains();
			}
			else
			{
				logMessage(e.getLocalizedMessage(), Severity.WARN);
			}
		}
		catch (InvalidAlphabetException | InvalidCheckDigitClassException | InvalidGeneratorException | DomainInUseException | UnknownDomainException | InvalidParentDomainException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
		catch (InvalidUpdateInUseOperationException e)
		{
			logMessage(getBundle().getString("domains.message.InvalidUpdateInUseOperationException"), Severity.WARN);
			reloadParentDomains();
		}
	}

	private void reloadParentDomains()
	{
		if (selectedDomain != null && StringUtils.isNotEmpty(selectedDomain.getName()))
		{
			try
			{
				selectedDomain.setParentDomainNames(getManager().getDomain(selectedDomain.getName()).getParentDomainNames());
			}
			catch (InvalidParameterException | UnknownDomainException e)
			{
				logger.error(e.getLocalizedMessage());
			}
		}
	}

	public void onDelete()
	{
		setSelectedDomainFromNode();
		try
		{
			Object[] args = { getDomainLabel(selectedDomain.getName()) };
			getManager().deleteDomainWithPSNs(selectedDomain.getName());
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

	public TreeNode<DomainOutDTO> getDomainTree()
	{
		// Create fake root for all domains
		TreeNode<DomainOutDTO> gPASRoot = new DefaultTreeNode<>(new DomainOutDTO());
		gPASRoot.getData().setName(ROOT_DOMAIN);
		gPASRoot.setExpanded(true);

		// Get all root domains that have no parent
		List<TreeNode<DomainOutDTO>> rootTrees = new ArrayList<>();
		for (DomainOutDTO domainDTO : getDomains())
		{
			if (domainDTO.getParentDomainNames() == null || domainDTO.getParentDomainNames().isEmpty())
			{
				rootTrees.add(new DefaultTreeNode<>(domainDTO));
			}
		}

		// Sort root trees alphabetically
		rootTrees = rootTrees.stream().sorted(Comparator.comparing(TreeNode::getData)).collect(Collectors.toList());

		// Add all to fake gPAS root and find children recursively
		for (TreeNode<DomainOutDTO> root : rootTrees)
		{
			gPASRoot.getChildren().add(root);
			searchChildren(root);
		}

		return gPASRoot;
	}

	private void searchChildren(TreeNode<DomainOutDTO> parent)
	{
		// Open parent in frontend
		parent.setExpanded(true);

		// Search in all domains for childs of this parent
		// Create temp list to sort alphabetically
		List<TreeNode<DomainOutDTO>> children = new ArrayList<>();
		for (DomainOutDTO domainDTO : getDomains())
		{
			if (domainDTO.getParentDomainNames().contains(parent.getData().getName()))
			{
				children.add(new DefaultTreeNode<>(domainDTO));
			}
		}
		parent.getChildren().addAll(children.stream().sorted(Comparator.comparing(TreeNode::getData)).toList());

		// Get children for each child
		parent.getChildren().forEach(this::searchChildren);
	}

	public boolean isEditable()
	{
		return !hasPSNs(selectedDomain);
	}

	public boolean isSelectedDomainNodeEditable()
	{
		return !isSelectedDomainNodeWithPSNs();
	}

	public boolean isSelectedDomainNodeWithPSNs()
	{
		return isNodeWithPSNs(selectedDomainNode);
	}

	public static boolean isNodeWithPSNs(TreeNode<DomainOutDTO> node)
	{
		if (node != null)
		{
			if (hasPSNs(node.getData()))
			{
				return true;
			}
			for (TreeNode<DomainOutDTO> child : node.getChildren())
			{
				if (isNodeWithPSNs(child))
				{
					return true;
				}
			}
		}
		return false;
	}

	public boolean isSelectedDomainNodeWithChildren()
	{
		return selectedDomainNode != null && selectedDomainNode.getChildCount() > 0;
	}

	private static boolean hasPSNs(DomainOutDTO domain)
	{
		return domain != null && domain.getNumberOfPseudonyms() > 0;
	}

	public List<String> getAlphabets()
	{
		return provider.getAlphabetMap().values().stream().map(String.class::cast).sorted().collect(Collectors.toList());
	}

	public List<String> getCheckDigitGenerators()
	{
		List<String> generators = provider.getGeneratorMap().values().stream().map(String.class::cast).sorted().collect(Collectors.toList());
		if (mode == Mode.NEW)
		{
			generators.removeIf(CheckDigits::isDeprecated);
		}
		return generators;
	}

	public List<String> getCheckDigitGeneratorsForAlphabet(Alphabet alphabet)
	{
		if (alphabet == null)
		{
			return List.of(Generators.NO_CHECK_DIGITS);
		}

		List<String> sortReference = Arrays.asList(
				Generators.VERHOEFF,
				Generators.NO_CHECK_DIGITS,
				Generators.GUMM,
				Generators.VERHOEFF_GUMM,
				Generators.HAMMING_CODE,
				Generators.DAMM,
				Generators.REED_SOLOMON_LAGRANGE);

		return getCheckDigitGenerators().stream().
				map(sneakyF(CheckDigits::createCheckDigits)).
				filter(cd -> cd.getAlphabetRestriction().isValid(alphabet)).
				map(CheckDigits::getClass).map(Class::getName).
				sorted(Comparator.comparingInt(s -> {
					int index = sortReference.indexOf(s);
					return index == -1 ? Integer.MAX_VALUE : index;
				})).toList();
	}

	public List<String> getCheckDigitGeneratorsForAlphabet(String alphabet)
	{
		selectedDomain.setAlphabet(alphabet);
		updateAlphabet();
		return getCheckDigitGeneratorsForAlphabet(tempAlphabet);
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
		updateAlphabet();
		List<String> checkDigitGeneratorsForAlphabet = getCheckDigitGeneratorsForAlphabet(tempAlphabet);
		if (!checkDigitGeneratorsForAlphabet.contains(selectedDomain.getCheckDigitClass()))
		{
			selectedDomain.setCheckDigitClass(checkDigitGeneratorsForAlphabet.getFirst());
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

	public ValidateViaParents[] getValidateViaParentsOptions()
	{
		return ValidateViaParents.values();
	}

	public DomainOutDTO getSelectedDomain()
	{
		return selectedDomain;
	}

	public void setSelectedDomain(DomainOutDTO selectedDomain)
	{
		this.selectedDomain = selectedDomain;
	}

	public TreeNode<DomainOutDTO> getSelectedDomainNode()
	{
		return selectedDomainNode;
	}

	public void setSelectedDomainNode(TreeNode<DomainOutDTO> selectedDomainNode)
	{
		this.selectedDomainNode = selectedDomainNode;
	}

	public void setSelectedDomainFromNode()
	{
		selectedDomain = selectedDomainNode.getData();
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

	public boolean isReadOnly()
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

	public int getPsnPrefixLength()
	{
		return psnPrefix != null ? psnPrefix.length() : 0;
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

	public int getPsnSuffixLength()
	{
		return psnSuffix != null ? psnSuffix.length() : 0;
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

	/**
	 * {@return the maximum length for the prefix such that after concatenation with the current psn, its check digits,
	 * and the current suffix the resulting string does not exceed the size of the PSN-column in the DB}
	 */
	public int getMaxPsnPrefixLength()
	{
		return Math.max(0, DomainConfig.getMaxNumberOfCharactersInFullyQualifiedPsn() - computeEffectivePsnLength(psnLength) - getPsnSuffixLength());
	}

	/**
	 * {@return the maximum length for the suffix such that after concatenation with the current psn, its check digits,
	 * and the current prefix the resulting string does not exceed the size of the PSN-column in the DB}
	 */
	public int getMaxPsnSuffixLength()
	{
		return Math.max(0, DomainConfig.getMaxNumberOfCharactersInFullyQualifiedPsn() - computeEffectivePsnLength(psnLength) - getPsnPrefixLength());
	}

	/**
	 * {@return the maximum length for the pure psn such that it can be handled by the check digits algorithm
	 * and does not exceed its DB-column size when concatenated with check digits, prefix, and suffix}
	 */
	public int getMaxPsnValueLength()
	{
		return Math.max(0, computeEffectiveMaxPsnLength());
	}

	/**
	 * {@return the number of remaining characters for the psn}
	 */
	public int getRemainingPsnTotalLength()
	{
		return Math.max(0, DomainConfig.getMaxNumberOfCharactersInFullyQualifiedPsn() - getPsnLengthSum());
	}

	/**
	 * {@return the length of the concatenation of psn, check digits, prefix, and suffix}
	 */
	public int getPsnLengthSum()
	{
		return computeEffectivePsnLength(psnLength) + getPsnPrefixLength() + getPsnSuffixLength();
	}
	
	public int getCheckDigitLength()
	{
		return computeEffectivePsnLength(psnLength) - psnLength;
	}

	public BigDecimal getDomainSize()
	{
		return new BigDecimal(tempAlphabet == null ? 0 : tempAlphabet.length()).pow(psnLength);
	}

	public enum Mode
	{
		READ, EDIT, NEW
	}

	public CheckDigits createCheckDigits() throws InvalidCheckDigitClassException, InvalidGeneratorException
	{
		DomainInDTO domain = selectedDomain;
		if (domain == null)
		{
			return null;
		}

		String clazz = domain.getCheckDigitClass();

		if (clazz == null)
		{
			return null;
		}

		if (tempAlphabet == null)
		{
			updateAlphabet();
		}

		return CheckDigits.createCheckDigits(clazz, tempAlphabet, domain.getConfig());
	}

	/**
	 * {@return the number of characters remaining for the psn including check digits yet leaving place for the current prefix and suffix}
	 */
	public int computeRemainingLengthForEffectivePsn()
	{
		return DomainConfig.getMaxNumberOfCharactersInFullyQualifiedPsn() - getPsnPrefixLength() - getPsnSuffixLength();
	}

	/**
	 * {@return the maximum length of PSNs in relation to the associated CheckDigits algorithm,
	 * so that the character string resulting from the concatenation of the PSNs, the check digits, the current prefix and the current suffix
	 * is no longer than {@link DomainConfig#getMaxNumberOfCharactersInFullyQualifiedPsn()}}
	 */
	public int computeEffectiveMaxPsnLength()
	{
		int remainingLengthForEffectivePsn = computeRemainingLengthForEffectivePsn();
		try
		{
			CheckDigits checkDigits = createCheckDigits();
			if (checkDigits != null)
			{
				return checkDigits.getEffectiveMaxMessageLength(remainingLengthForEffectivePsn);
			}
			return remainingLengthForEffectivePsn;
		}
		catch (InvalidCheckDigitClassException | InvalidGeneratorException e)
		{
			return remainingLengthForEffectivePsn;
		}
	}

	/**
	 * {@return the total length of PSNs wrt. the associated CheckDigits algorithm
	 * which is the length of the string resulting from the concatenation of the psn
	 * (if requested including prefix and suffix) and its check digits}
	 */
	public int computeEffectivePsnLength(int psnLength)
	{
		try
		{
			CheckDigits checkDigits = createCheckDigits();
			if (checkDigits != null)
			{
				int messageLength = psnLength;
				DomainInDTO domain = selectedDomain;
				if (domain != null)
				{
					messageLength += domain.getConfig().isIncludePrefixInCheckDigitCalculation() ? getPsnPrefixLength() : 0;
					messageLength += domain.getConfig().isIncludeSuffixInCheckDigitCalculation() ? getPsnSuffixLength() : 0;
				}
				psnLength += checkDigits.getNumberOfCheckDigits(messageLength);
			}
		}
		catch (InvalidCheckDigitClassException | InvalidGeneratorException ignored)
		{
			// return original length
		}
		return psnLength;
	}
}
