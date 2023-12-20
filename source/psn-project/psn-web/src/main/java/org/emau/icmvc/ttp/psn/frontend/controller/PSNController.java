package org.emau.icmvc.ttp.psn.frontend.controller;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainOutDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.PSNDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.PSNNetDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.PSNNetNodeDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.PSNTreeDTO;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DBException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DeletionForbiddenException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DomainIsFullException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InsertPairException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidGeneratorException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidPSNException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.PSNNotFoundException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownValueException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.ValueIsAnonymisedException;
import org.emau.icmvc.ttp.psn.frontend.controller.common.AbstractGPASBean;
import org.emau.icmvc.ttp.psn.frontend.model.PSNDTOLazyModel;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import static org.emau.icmvc.ttp.psn.frontend.util.SessionMapKeys.SELECTED_DOMAIN;

@ViewScoped
@ManagedBean(name = "psnController")
public class PSNController extends AbstractGPASBean
{
	private PSNDTOLazyModel psnLazyModel;

	private DomainOutDTO selectedDomain;
	private PSNDTO selectedRow;
	private TreeNode selectedNode;
	private PSNDTO selectedPseudonym;

	private DomainOutDTO domain;
	private String domainFilter;
	private String originalValue;
	private String pseudonym;
	private TreeNode pseudonymTree;
	private boolean treeOpen = false;
	private boolean autoShowTree = false;

	@PostConstruct
	public void init()
	{
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		Map<String, Object> sessionMap = externalContext.getSessionMap();
		if (sessionMap.containsKey(SELECTED_DOMAIN))
		{
			try
			{
				for (DomainOutDTO domainDTO : super.getDomains())
				{
					if (domainDTO.getName().equals(sessionMap.get(SELECTED_DOMAIN)))
					{
						setSelectedDomain(domainDTO);

						onSelectDomain();
					}
				}
			}
			catch (UnknownDomainException e)
			{
				logger.error(e.getLocalizedMessage());
			}
		}
	}

	public void load(String originalValue, String pseudonym)
	{
		if (FacesContext.getCurrentInstance().isPostback())
		{
			return;
		}

		if (StringUtils.isNotEmpty(originalValue) || StringUtils.isNotEmpty(pseudonym))
		{
			autoShowTree = true;
			PrimeFaces.current().executeScript("PF('blockPseudonyms').show()");
			selectedDomain = getDomainsWithAll().stream().filter(d -> d.getName().equals("ALL")).findFirst().orElse(null);
			PrimeFaces.current().executeScript("$('#main\\\\:pseudonyms\\\\:globalFilter').val('" + (StringUtils.isNotEmpty(originalValue) ? originalValue : pseudonym) + "')");
			PrimeFaces.current().executeScript("PF('pseudonyms').filter();");
		}
	}

	public void onSelectDomain() throws UnknownDomainException
	{
		if (selectedDomain != null)
		{
			domain = selectedDomain;
			setGlobalFilter(null);
			getPSNLazyModel().setDomains(getSelectedDomains());
			DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("main:pseudonyms");
			dataTable.reset();
		}
	}

	public void onNewPseudonym()
	{
		originalValue = null;
		pseudonym = null;
	}

	public void onSavePseudonym()
	{
		try
		{
			if (!originalValueAlreadyExists(originalValue))
			{
				pseudonym = getOrCreatePseudonymFor(originalValue, domain.getName());
				selectedDomain = domain;
				Object[] args = { originalValue, pseudonym, domain.getName() };
				logMessage(new MessageFormat(getBundle().getString("edit.message.info.pseudonymSaved")).format(args), Severity.INFO, true, false);
				loadDomains();
				loadTree(pseudonym);
				onNewPseudonym();
			}
		}
		catch (InvalidParameterException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.WARN);
		}
		catch (DBException | UnknownDomainException | DomainIsFullException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	public void onSavePseudonymPair()
	{
		try
		{
			if (!originalValueAlreadyExists(originalValue) && !pseudonymAlreadyExists(pseudonym))
			{
				insertValuePseudonymPair(originalValue, pseudonym, domain.getName());
				selectedDomain = domain;
				Object[] args = { originalValue, pseudonym, domain.getName() };
				logMessage(new MessageFormat(getBundle().getString("edit.message.info.pseudonymSaved")).format(args), Severity.INFO, true, false);
				loadDomains();
				onNewPseudonym();
			}
		}
		catch (InsertPairException | InvalidPSNException | InvalidParameterException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.WARN);
		}
		catch (InvalidGeneratorException | UnknownDomainException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	public void onDelete()
	{
		try
		{
			deleteEntry(selectedPseudonym.getOriginalValue(), selectedPseudonym.getDomainName());
			Object[] args = { selectedPseudonym.getPseudonym(), selectedPseudonym.getDomainName() };
			logMessage(new MessageFormat(getBundle().getString("edit.message.info.deleted")).format(args), Severity.INFO, true, false);
			loadDomains();
			loadTree(selectedPseudonym.getOriginalValue());
			selectedNode = null;
		}
		catch (DeletionForbiddenException | UnknownDomainException | UnknownValueException | InvalidParameterException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	public void onAnonymise()
	{
		try
		{
			anonymiseEntry(selectedPseudonym.getOriginalValue(), selectedPseudonym.getDomainName());
			Object[] args = { selectedPseudonym.getOriginalValue(), selectedPseudonym.getPseudonym(), selectedPseudonym.getDomainName() };
			logMessage(new MessageFormat(getBundle().getString("edit.message.info.anonymised")).format(args), Severity.INFO, true, false);
			loadDomains();
			loadTree(selectedPseudonym.getPseudonym());
		}
		catch (ValueIsAnonymisedException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.WARN);
		}
		catch (DBException | UnknownValueException | UnknownDomainException | InvalidParameterException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	public void showTree(PSNDTO psn)
	{
		treeOpen = true;
		selectedRow = psn;
		setSelectedPseudonymFromDataTable();
		if (psn != null)
		{
			loadTree(psn.getOriginalValue());
		}
		autoShowTree = false;
	}

	private void loadTree(String value)
	{
		PSNNetDTO net;
		try
		{
			net = service.getPSNNetFor(value);

			// Create TreeNode for UI
			TreeNode root = new DefaultTreeNode(net.getRoot());
			root.setExpanded(true);

			// Recursively add all Node elements
			searchNodes(root, net.getRoot());

			pseudonymTree = root;
		}
		catch (InvalidParameterException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	public void onCloseTree()
	{
		treeOpen = false;
	}

	public void onPseudonymiseOriginalValue()
	{
		onNewPseudonym();
		if (selectedPseudonym != null)
		{
			originalValue = selectedPseudonym.getOriginalValue();
			domain = getRelatedDomains(Relation.SIBLING, true).size() > 0 ? getRelatedDomains(Relation.SIBLING, true).get(0) : null;
		}
	}

	public void onPseudonymisePseudonym()
	{
		onNewPseudonym();
		if (selectedPseudonym != null)
		{
			originalValue = selectedPseudonym.getPseudonym();
			domain = getRelatedDomains(Relation.CHILD, true).size() > 0 ? getRelatedDomains(Relation.CHILD, true).get(0) : null;
		}
	}

	public void setSelectedPseudonymFromTree()
	{
		selectedPseudonym = selectedNode == null ? null : ((PSNNetNodeDTO) selectedNode.getData()).toPSNDTO();
	}

	public void setSelectedPseudonymFromTreeNode(PSNNetNodeDTO node)
	{
		selectedPseudonym = node == null ? null : node.toPSNDTO();
	}

	public void setSelectedPseudonymFromDataTable()
	{
		selectedPseudonym = selectedRow == null ? null : new PSNDTO(selectedRow.getDomainName(), selectedRow.getOriginalValue(), selectedRow.getPseudonym());
	}

	public boolean isAnonymisable(PSNDTO psn) throws InvalidParameterException
	{
		return psn != null && !isAnonym(psn.getOriginalValue());
	}

	public boolean isAnonym(String originalValue) throws InvalidParameterException
	{
		return service.isAnonym(originalValue);
	}

	public boolean isDeletable(PSNDTO psn) throws UnknownDomainException
	{
		if (psn != null && !ROOT_DOMAIN.equals(psn.getDomainName()))
		{
			try
			{
				return domainService.arePSNDeletable(psn.getDomainName());
			}
			catch (InvalidParameterException e)
			{
				logMessage(e.getLocalizedMessage(), Severity.ERROR);
			}
		}
		return false;
	}

	public List<DomainOutDTO> getRelatedDomains(Relation relation, boolean message)
	{
		if (selectedPseudonym != null && StringUtils.isNotEmpty(selectedPseudonym.getDomainName()))
		{
			try
			{
				List<DomainOutDTO> temp = new ArrayList<>();
				for (DomainOutDTO d : super.getDomains())
				{
					// Add child domains if domain parent is equal to selected pseudonym domain name
					if (Relation.CHILD.equals(relation) && d.getParentDomainNames().contains(selectedPseudonym.getDomainName()))
					{
						temp.add(d);
					}
					// Add brother domains if its not the current domain
					else if (Relation.SIBLING.equals(relation) && !d.getName().equals(selectedPseudonym.getDomainName()))
					{
						// And if list of iterating domain parents exactly matches list of selected pseudonym domain parents
						List<String> domainParents = domainService.getDomain(selectedPseudonym.getDomainName()).getParentDomainNames();
						if (d.getParentDomainNames().containsAll(domainParents) && domainParents.containsAll(d.getParentDomainNames()))
						{
							temp.add(d);
						}
					}
				}

				if (message)
				{
					// Display a message if no suitable domains were found
					clearFacesMessages();
					if (Relation.CHILD.equals(relation) && temp.isEmpty())
					{
						logMessage(getBundle().getString("page.edit.newPseudonym.message.noChildDomains"), Severity.WARN);
					}
					else if (Relation.SIBLING.equals(relation) && temp.isEmpty())
					{
						logMessage(getBundle().getString("page.edit.newPseudonym.message.noSiblingDomains"), Severity.WARN);
					}
				}

				return temp;
			}
			catch (InvalidParameterException | UnknownDomainException e)
			{
				logger.error(e.getLocalizedMessage());
			}
		}

		return super.getDomains();
	}

	public List<DomainOutDTO> getFilteredDomains()
	{
		List<DomainOutDTO> domains;

		// Create child pseudonym
		if (selectedPseudonym != null && Objects.equals(originalValue, selectedPseudonym.getPseudonym()))
		{
			domains = getRelatedDomains(Relation.CHILD, true);
		}
		// Create sibling pseudonym
		else if (selectedPseudonym != null && Objects.equals(originalValue, selectedPseudonym.getOriginalValue()))
		{
			domains = getRelatedDomains(Relation.SIBLING, true);
		}
		else
		{
			domains = super.getDomains();
		}
		return domains.stream().sorted().collect(Collectors.toList());
	}

	private boolean originalValueAlreadyExists(String originalValue) throws UnknownDomainException
	{
		try
		{
			String existingPseudonym = service.getPseudonymFor(originalValue, domain.getName());
			Object[] args = { originalValue, existingPseudonym };
			logMessage(new MessageFormat(getBundle().getString("edit.message.warn.originalValueExists")).format(args), Severity.WARN, true, false);
			return true;
		}
		catch (UnknownValueException expected)
		{
			return false;
		}
		catch (InvalidParameterException e)
		{
			throw new UnknownDomainException(e);
		}
	}

	private boolean pseudonymAlreadyExists(String pseudonym) throws InvalidGeneratorException, InvalidPSNException, UnknownDomainException
	{
		try
		{
			String existingOriginalValue = service.getValueFor(pseudonym, domain.getName());
			Object[] args = { pseudonym, existingOriginalValue };
			logMessage(new MessageFormat(getBundle().getString("edit.message.warn.pseudonymExists")).format(args), Severity.WARN, true, false);
			return true;
		}
		catch (ValueIsAnonymisedException expected)
		{
			Object[] args = { pseudonym };
			logMessage(new MessageFormat(getBundle().getString("edit.message.warn.pseudonymAnonymised")).format(args), Severity.WARN, true, false);
			return true;
		}
		catch (PSNNotFoundException expected)
		{
			return false;
		}
		catch (InvalidParameterException e)
		{
			throw new UnknownDomainException(e);
		}
	}

	private void searchNodes(TreeNode parentNodeUI, PSNTreeDTO parentNodeDTO)
	{
		for (PSNTreeDTO childNodeDTO : parentNodeDTO.getChildren())
		{
			TreeNode childNodeUI = new DefaultTreeNode(childNodeDTO, parentNodeUI);
			childNodeUI.setExpanded(true);
			if (selectedRow != null && childNodeDTO.getPseudonym().equals(selectedRow.getPseudonym()))
			{
				childNodeUI.setSelected(true);
			}
			parentNodeUI.getChildren().add(childNodeUI);

			if (parentNodeDTO.getLevel() == -1)
			{
				parentNodeDTO.setDomainName(ROOT_DOMAIN);
				parentNodeDTO.setPseudonym(childNodeDTO.getOriginalValue());
			}

			searchNodes(childNodeUI, childNodeDTO);
		}
	}

	@Override
	public void loadDomains()
	{
		super.loadDomains();
		if (selectedDomain != null)
		{
			setSelectedDomain(getDomains().stream().filter(d -> selectedDomain.getName().equals(d.getName())).findAny().orElse(null));
		}
	}

	/**
	 * Returns all domains sorted by label.
	 *
	 * @return all domains sorted by label
	 */
	public List<DomainOutDTO> getAllDomainsSortedByLabel()
	{
		return super.getDomains().stream().sorted().collect(Collectors.toList());
	}

	/**
	 * Returns all domains sorted by label if the name of the selected domain (as returned by
	 * {@link #getSelectedDomain()}) is "ALL", or otherwise a singleton list with the selected domain.
	 *
	 * @return all domains or the selected domain
	 */
	public List<DomainOutDTO> getSelectedDomains()
	{
		if (selectedDomain == null)
		{
			return Collections.emptyList();
		}
		else if ("ALL".equals(selectedDomain.getName()))
		{
			return getAllDomainsSortedByLabel();
		}

		return Collections.singletonList(selectedDomain);
	}

	public List<DomainOutDTO> getDomainsWithAll()
	{
		DomainOutDTO all = new DomainOutDTO();
		all.setName("ALL");
		all.setLabel(getBundle().getString("domain.all"));
		all.setNumberOfPseudonyms(
				(int) (long) getAllDomainsSortedByLabel().stream().collect(Collectors.summingLong(DomainOutDTO::getNumberOfPseudonyms)));

		List<DomainOutDTO> result = new ArrayList<>(Collections.singletonList(all));
		result.addAll(getAllDomainsSortedByLabel());

		return result;
	}

	public PSNDTOLazyModel getPSNLazyModel()
	{
		if (psnLazyModel == null)
		{
			psnLazyModel = new PSNDTOLazyModel(domainService, getSelectedDomains());
		}
		else
		{
			psnLazyModel.setDomains(getSelectedDomains());
		}
		return psnLazyModel;
	}

	public DomainOutDTO getSelectedDomain()
	{
		return selectedDomain;
	}

	public void setSelectedDomain(DomainOutDTO selectedDomain)
	{
		this.selectedDomain = selectedDomain;
	}

	public PSNDTO getSelectedRow()
	{
		return selectedRow;
	}

	public void setSelectedRow(PSNDTO selectedRow)
	{
		this.selectedRow = selectedRow;
	}

	public TreeNode getPseudonymTree()
	{
		return pseudonymTree;
	}

	public TreeNode getSelectedNode()
	{
		return selectedNode;
	}

	public void setSelectedNode(TreeNode selectedNode)
	{
		this.selectedNode = selectedNode;
	}

	public boolean isTreeOpen()
	{
		return treeOpen;
	}

	public DomainOutDTO getDomain()
	{
		return domain;
	}

	public void setDomain(DomainOutDTO domain)
	{
		this.domain = domain;
	}

	public String getDomainFilter()
	{
		return domainFilter;
	}

	public void setDomainFilter(String domainFilter)
	{
		this.domainFilter = domainFilter;
	}

	public String getOriginalValue()
	{
		return originalValue;
	}

	public void setOriginalValue(String originalValue)
	{
		this.originalValue = originalValue;
	}

	public String getPseudonym()
	{
		return pseudonym;
	}

	public void setPseudonym(String pseudonym)
	{
		this.pseudonym = pseudonym;
	}

	public PSNDTO getSelectedPseudonym()
	{
		return selectedPseudonym;
	}

	public enum Relation
	{
		CHILD, SIBLING
	}

	public boolean isAutoShowTree()
	{
		return autoShowTree;
	}
}
