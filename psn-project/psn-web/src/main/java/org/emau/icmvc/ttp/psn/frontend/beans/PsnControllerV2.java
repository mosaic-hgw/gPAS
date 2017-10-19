package org.emau.icmvc.ttp.psn.frontend.beans;

/*
 * ###license-information-start###
 * gPAS - a Generic Pseudonym Administration Service
 * __
 * Copyright (C) 2013 - 2017 The MOSAIC Project - Institut fuer Community Medicine der
 * 							Universitaetsmedizin Greifswald - mosaic-projekt@uni-greifswald.de
 * 							concept and implementation
 * 							l. geidel
 * 							web client
 * 							g. weiher
 * 							a. blumentritt
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
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.emau.icmvc.ganimed.ttp.psn.DomainManager;
import org.emau.icmvc.ganimed.ttp.psn.PSNManager;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.HashMapWrapper;
import org.emau.icmvc.ganimed.ttp.psn.dto.PSNDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.PSNTreeDTO;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DBException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DeletionForbiddenException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidGeneratorException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidPSNException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.PSNNotFoundException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownValueException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.ValueIsAnonymisedException;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ManagedBean(name = "PsnControllerV2")
@ViewScoped
public class PsnControllerV2 {

	private final Logger logger = LoggerFactory.getLogger(PsnControllerV2.class);

	@EJB(lookup = "java:global/gpas/psn-ejb/PSNManagerBean!org.emau.icmvc.ganimed.ttp.psn.PSNManager")
	private PSNManager psnManager;

	@EJB(lookup = "java:global/gpas/psn-ejb/DomainManagerBean!org.emau.icmvc.ganimed.ttp.psn.DomainManager")
	private DomainManager domainManager;

	/**
	 * bundle of all info/error messages used in the application
	 */
	private ResourceBundle messages;
	/**
	 * bean representation of pseudonym_input_text
	 */
	private String pseudonym;
	/**
	 * bean representation of search_input
	 */
	private String searchValue;
	/**
	 * bean representation of original_value_input_text
	 */
	private String originalValue;
	/**
	 * list for populating psn_table
	 */
	private List<PSNDTO> psnList;
	/**
	 * filter for psn_table
	 */
	private List<PSNDTO> filteredPsnList = new ArrayList<PSNDTO>();
	private List<DomainDTO> domains;
	/**
	 * bean representation of the selected domain
	 */
	private DomainDTO selectedDomain;
	/**
	 * bean representation of the selected psn for anonymisation
	 */
	private String origValueForAnonymisation;
	/**
	 * bean representation of the selected psn for deletion
	 */
	private String origValueForDeletion;
	/**
	 * bean representation of the selected psn for the psn-tree
	 */
	private String selectedPsnForTree;
	/**
	 * bean representation of the selected psnValuePairOriginalValue
	 */
	private String psnValuePairOriginalValue;
	/**
	 * bean representation of the selected psnValuePairPseudonym
	 */
	private String psnValuePairPseudonym;

	private TreeNode psnTree;

	@PostConstruct
	public void init() {
		messages = ResourceBundle.getBundle("messages");
		updateAll();
	}

	/**
	 * update the full bean
	 */
	public void updateAll() {
		domains = domainManager.listDomains();
		selectedDomain = null;
		updatePsnList();

	}

	/**
	 * updates the psn List and resets the inputs
	 */
	public void updatePsnList() {
		FacesContext context = FacesContext.getCurrentInstance();
		if (selectedDomain == null) {
			psnList = null;
			//filteredPsnList = null;
			pseudonym = null;
			originalValue = null;
			psnValuePairOriginalValue = null;
			psnValuePairPseudonym = null;
			logger.info("no domain selected");
		} else {
			try {
				psnList = domainManager.listPseudonymsFor(selectedDomain.getDomain());
				if (logger.isDebugEnabled()) {
					logger.debug("Psn List updated: " + psnList.size() + "entries");
				}
				if (logger.isInfoEnabled()) {
					logger.info("XXX Psn List updated: " + psnList.size() + "entries");
				}
			} catch (UnknownDomainException e) {
				context.addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("domain.error.unkownDomain"), e.getMessage()));
				if (logger.isErrorEnabled()) {
					logger.error("domain unkown", e);
				}
			}
			//filteredPsnList = psnList;
			origValueForAnonymisation = null;
			origValueForDeletion = null;
			selectedPsnForTree = null;
		}
	}

	public void onDomainSelect() {
		psnTree = null;
		psnValuePairOriginalValue = null;
		psnValuePairPseudonym = null;
		updatePsnList();
	}

	/**
	 * anonymises the Psn specified by selectedPsn
	 * 
	 * @param event
	 */
	public void anonymiseSelectedEntry() {
		FacesContext context = FacesContext.getCurrentInstance();
		Object[] args = { origValueForAnonymisation, selectedDomain.getDomain() };
		try {
			psnManager.anonymiseEntry(origValueForAnonymisation, selectedDomain.getDomain());
			domains = domainManager.listDomains();
			updatePsnList();
			context.addMessage("anonymisation",
					new FacesMessage(FacesMessage.SEVERITY_INFO, new MessageFormat(messages.getString("psn.info.valueAnonymised")).format(args), ""));
			origValueForAnonymisation = null;
			if (logger.isDebugEnabled()) {
				logger.debug("anonymised entry");
			}
		} catch (DBException | UnknownValueException e) {
			context.addMessage("anonymisation",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, new MessageFormat(messages.getString("psn.error.valueNotFound")).format(args), ""));
			if (logger.isErrorEnabled()) {
				logger.error("Database error", e);
			}
		} catch (ValueIsAnonymisedException e) {
			context.addMessage("anonymisation",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("psn.error.valueIsAnonymised"), e.getMessage()));
			if (logger.isErrorEnabled()) {
				logger.error("Database error", e);
			}
		}
	}
	
	/**
	 * deletes the Psn specified by selectedPsn
	 * 
	 * @param event
	 */
	public void deleteSelectedEntry() {
		
		
		FacesContext context = FacesContext.getCurrentInstance();
		Object[] args = { origValueForDeletion, selectedDomain.getDomain() };
		try {
			psnManager.deleteEntry(origValueForDeletion, selectedDomain.getDomain());
			domains = domainManager.listDomains();
			updatePsnList();
			context.addMessage("deletion",
					new FacesMessage(FacesMessage.SEVERITY_INFO, new MessageFormat(messages.getString("psn.info.valueDeleted")).format(args), ""));
			origValueForDeletion = null;
			if (logger.isDebugEnabled()) {
				logger.debug("deleted entry");
			}
			if (logger.isInfoEnabled()) {
				logger.info("deleted entry");
			}
		} catch (DeletionForbiddenException e) {
			context.addMessage("deletion",
					new FacesMessage(FacesMessage.SEVERITY_INFO, new MessageFormat(messages.getString("psn.error.deleteForbidden")).format(args), ""));
			if (logger.isErrorEnabled())
				logger.error("", e);
		} catch (UnknownDomainException e) {
			context.addMessage("deletion",
					new FacesMessage(FacesMessage.SEVERITY_INFO, new MessageFormat(messages.getString("domain.error.unkownDomain")).format(args), ""));
			if (logger.isErrorEnabled())
				logger.error("", e);
		} catch (UnknownValueException e) {
			
			context.addMessage("deletion",
					new FacesMessage(FacesMessage.SEVERITY_INFO, new MessageFormat(messages.getString("psn.error.valueNotFound")).format(args), ""));
			if (logger.isErrorEnabled())
				logger.error("", e);
		}
	}

	public void searchPseudonym() {
		FacesContext context = FacesContext.getCurrentInstance();
		if (logger.isDebugEnabled()) {
			logger.debug("searchPseudonym called for value '" + originalValue + "' and Domain '" + selectedDomain + "'");
		}
		try {
			if (originalValue == null || searchValue.isEmpty()) {
				context.addMessage("search", new FacesMessage(FacesMessage.SEVERITY_INFO, "Can't process empty value.", ""));
				if (logger.isErrorEnabled()) {
					logger.error("can't retrieve pseudonym for empty value");
				}
			} else {
				String pseudonym = psnManager.getPseudonymFor(searchValue, selectedDomain.getDomain());
				Object[] args2 = { searchValue, pseudonym };
				context.addMessage("search", new FacesMessage(new MessageFormat(messages.getString("psn.info.getPseudonym")).format(args2), ""));
				if (logger.isInfoEnabled()) {
					logger.info("pseudonym retrieved for Value'" + searchValue + "'");
				}
			}
			searchValue = null;
		} catch (UnknownValueException e) {
			String[] args = { searchValue };
			context.addMessage("search",
					new FacesMessage(FacesMessage.SEVERITY_INFO, new MessageFormat(messages.getString("psn.error.NoPsnForValue")).format(args), ""));
			if (logger.isErrorEnabled())
				logger.error("", e);
		}
	}

	/**
	 * retrieves existing originalValue(field) for the current pseudonym(field) and current selected domain
	 * 
	 * @return ""
	 */
	public void builtPseudonym() {
		FacesContext context = FacesContext.getCurrentInstance();
		if (logger.isDebugEnabled()) {
			logger.debug("builtPseudonym called for value '" + originalValue + "' and Domain '" + selectedDomain + "'");
		}
		try {
			if (originalValue == null || originalValue.isEmpty()) {
				context.addMessage("pseudonymisation", new FacesMessage(FacesMessage.SEVERITY_INFO, "can't process empty value", ""));
				if (logger.isErrorEnabled()) {
					logger.error("can't retrieve pseudonym for empty value");
				}
			} else {
				String pseudonym = psnManager.getOrCreatePseudonymFor(originalValue, selectedDomain.getDomain());
				Object[] args2 = { originalValue, pseudonym };
				context.addMessage("pseudonymisation",
						new FacesMessage(new MessageFormat(messages.getString("psn.info.getPseudonym")).format(args2), ""));
				if (logger.isInfoEnabled()) {
					logger.info("pseudonym retrieved for Value'" + originalValue + "'");
				}
			}
			originalValue = null;
			domains = domainManager.listDomains();
			updatePsnList();
		} catch (DBException e) {
			context.addMessage("pseudonymisation",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("general.error.database"), e.getMessage()));
			if (logger.isErrorEnabled())
				logger.error("", e);
		} catch (InvalidGeneratorException e) {
			context.addMessage("pseudonymisation",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("general.error.unexpectedError"), e.getMessage()));
			if (logger.isErrorEnabled())
				logger.error("", e);
		} catch (UnknownDomainException e) {
			context.addMessage("pseudonymisation",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("domain.error.unkownDomain"), e.getMessage()));
			if (logger.isErrorEnabled())
				logger.error("unexpected error", e);
		}
	}

	/**
	 * retrieves existing pseudonym(field) or creates non existing pseudonym(field) for the current originalValue(field) and current selected domain
	 * 
	 * @return ""
	 */
	public void valueOfPseudonym() {
		FacesContext context = FacesContext.getCurrentInstance();
		if (logger.isDebugEnabled()) {
			logger.debug("valueOfPseudonym called for pseudonym '" + pseudonym + "' and Domain '" + selectedDomain + "'");
		}
		try {
			String originalValue = psnManager.getValueFor(pseudonym, selectedDomain.getDomain());
			Object[] args2 = { originalValue, pseudonym };
			if (logger.isInfoEnabled()) {
				logger.info("original value retrieved for pseudonym'" + pseudonym + "'");
			}
			context.addMessage("depseudonymisation", new FacesMessage(new MessageFormat(messages.getString("psn.info.getValue")).format(args2), ""));
			pseudonym = null;
		} catch (InvalidGeneratorException e) {
			context.addMessage("depseudonymisation", new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), e.getMessage()));
			if (logger.isErrorEnabled())
				logger.error("", e);
		} catch (InvalidPSNException e) {
			context.addMessage("depseudonymisation", new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), e.getMessage()));
			if (logger.isErrorEnabled())
				logger.error("", e);
		} catch (PSNNotFoundException e) {
			context.addMessage("depseudonymisation",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("psn.error.psnNotFound"), e.getMessage()));
			if (logger.isErrorEnabled())
				logger.error("psn not found", e);
		} catch (UnknownDomainException e) {
			context.addMessage("depseudonymisation",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("domain.error.unkownDomain"), e.getMessage()));
			if (logger.isErrorEnabled())
				logger.error("", e);
		} catch (ValueIsAnonymisedException e) {
			context.addMessage("depseudonymisation", new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), e.getMessage()));
			if (logger.isErrorEnabled())
				logger.error("", e);
		}
	}

	public List<String> complete(String query) {

		List<String> result = new ArrayList<String>();
		for (PSNDTO psn : psnList) {

			if ((!Pattern.matches("###_anonym_###_.*_###_anonym_###", psn.getOriginalValue())) && psn.getOriginalValue().contains(query)) {
				result.add(psn.getOriginalValue());
			}
		}
		return result;
	}

	/**
	 * Display a psn tree
	 * 
	 * @param event
	 */
	public void displayPSNTree(ActionEvent event) {
		FacesContext context = FacesContext.getCurrentInstance();
		try {
			PSNTreeDTO list = psnManager.getPSNTreeForPSN(selectedPsnForTree, selectedDomain.getDomain());
			psnTree = new DefaultTreeNode(list.getDomain() + " : " + list.getPseudonym());
			psnTree.setExpanded(true);
			createNode(psnTree, list.getChildren());
			domains = domainManager.listDomains();
			updatePsnList();
			selectedPsnForTree = null;
		} catch (DBException e) {
			context.addMessage("psnTreeMessage", new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
			if (logger.isErrorEnabled())
				logger.error("", e);
		} catch (InvalidGeneratorException e) {
			context.addMessage("psnTreeMessage",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("general.error.unexpectedError"), e.getMessage()));
			if (logger.isErrorEnabled())
				logger.error("", e);
		} catch (UnknownDomainException e) {
			context.addMessage("psnTreeMessage",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("domain.error.unkownDomain"), e.getMessage()));
			if (logger.isErrorEnabled())
				logger.error("unexpected error", e);
		} catch (InvalidPSNException e) {
			context.addMessage("psnTreeMessage", new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), e.getMessage()));
			if (logger.isErrorEnabled())
				logger.error("", e);
		} catch (PSNNotFoundException e) {
			context.addMessage("psnTreeMessage", new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), e.getMessage()));
			if (logger.isErrorEnabled())
				logger.error("", e);
		} catch (ValueIsAnonymisedException e) {
			context.addMessage("psnTreeMessage", new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), e.getMessage()));
			if (logger.isErrorEnabled())
				logger.error("", e);
		}
	}

	private TreeNode createNode(TreeNode rootTree, List<PSNTreeDTO> list) {

		for (PSNTreeDTO dto : list) {
			TreeNode node = new DefaultTreeNode(dto.getDomain() + " : " + dto.getPseudonym());
			createNode(node, dto.getChildren());
			node.setExpanded(true);
			rootTree.getChildren().add(node);
		}
		return rootTree;
	}

	/**
	 * Insert a psn value pair
	 * 
	 * @param event
	 */
	public void insertPSNValuePair(ActionEvent event) {
		FacesContext context = FacesContext.getCurrentInstance();
		Object[] args = { psnValuePairPseudonym, selectedDomain.getDomain(), psnValuePairOriginalValue };
		try {
			HashMap<String, String> valuePairs = new HashMap<>();
			valuePairs.put(psnValuePairOriginalValue, psnValuePairPseudonym);
			HashMapWrapper<String, String> valuePairsWrapper = new HashMapWrapper<>(valuePairs);
			psnManager.insertValuePseudonymPairs(valuePairsWrapper, selectedDomain.getDomain());
			domains = domainManager.listDomains();
			updatePsnList();
			context.addMessage("psnValuePairsMessage", new FacesMessage(FacesMessage.SEVERITY_INFO,
					new MessageFormat(messages.getString("psn.info.psnValuePairInserted")).format(args), ""));
			origValueForAnonymisation = null;
			if (logger.isDebugEnabled()) {
				logger.debug("psn value pair inserted");
			}
		} catch (DBException e) {
			context.addMessage("psnValuePairsMessage", new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
			if (logger.isErrorEnabled())
				logger.error("", e);
		} catch (InvalidGeneratorException e) {
			context.addMessage("psnValuePairsMessage",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("general.error.unexpectedError"), e.getMessage()));
			if (logger.isErrorEnabled())
				logger.error("", e);
		} catch (UnknownDomainException e) {
			context.addMessage("psnValuePairsMessage",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("domain.error.unkownDomain"), e.getMessage()));
			if (logger.isErrorEnabled())
				logger.error("unexpected error", e);
		} catch (InvalidPSNException e) {
			context.addMessage("psnValuePairsMessage", new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), e.getMessage()));
			if (logger.isErrorEnabled())
				logger.error("", e);
		}
	}

	public PSNManager getPsnManager() {
		return psnManager;
	}

	public void setPsnManager(PSNManager psnManager) {
		this.psnManager = psnManager;
	}

	public DomainManager getDomainManager() {
		return domainManager;
	}

	public void setDomainManager(DomainManager domainManager) {
		this.domainManager = domainManager;
	}

	public String getPseudonym() {
		return pseudonym;
	}

	public void setPseudonym(String pseudonym) {
		this.pseudonym = pseudonym;
	}

	public String getOriginalValue() {
		return originalValue;
	}

	public void setOriginalValue(String originalValue) {
		this.originalValue = originalValue;
	}

	public List<PSNDTO> getPsnList() {
		return psnList;
	}

	public void setPsnList(List<PSNDTO> psnList) {
		this.psnList = psnList;
	}

	public List<PSNDTO> getFilteredPsnList() {
		return filteredPsnList;
	}

	//eigentlich ist in primefaces die filtered list unnötig
	public void setFilteredPsnList(List<PSNDTO> filteredPsnList) {
		this.filteredPsnList = filteredPsnList;
	}

	public DomainDTO getSelectedDomain() {
		return selectedDomain;
	}

	public void setSelectedDomain(DomainDTO selectedDomain) {
		this.selectedDomain = selectedDomain;
	}

	public String getOrigValueForAnonymisation() {
		return origValueForAnonymisation;
	}
	public void setOrigValueForAnonymisation(String origValueForAnonymisation) {
		this.origValueForAnonymisation = origValueForAnonymisation;
	}

	public void setOrigValueForDeletion(String origValueForDeletion) {
		this.origValueForDeletion = origValueForDeletion;
	}
	
	public String getOrigValueForDeletion() {
		return origValueForDeletion;
	}


	public String getSelectedPsnForTree() {
		return selectedPsnForTree;
	}

	public void setSelectedPsnForTree(String selectedPsnForTree) {
		this.selectedPsnForTree = selectedPsnForTree;
	}

	public List<DomainDTO> getDomains() {
		return domains;
	}

	public void setDomains(List<DomainDTO> domains) {
		this.domains = domains;
	}

	public String getSearchValue() {
		return searchValue;
	}

	public void setSearchValue(String searchValue) {
		this.searchValue = searchValue;
	}

	public TreeNode getPsnTree() {
		return psnTree;
	}

	public String getPsnValuePairOriginalValue() {
		return psnValuePairOriginalValue;
	}

	public void setPsnValuePairOriginalValue(String psnValuePairOriginalValue) {
		this.psnValuePairOriginalValue = psnValuePairOriginalValue;
	}

	public String getPsnValuePairPseudonym() {
		return psnValuePairPseudonym;
	}

	public void setPsnValuePairPseudonym(String psnValuePairPseudonym) {
		this.psnValuePairPseudonym = psnValuePairPseudonym;
	}
}
