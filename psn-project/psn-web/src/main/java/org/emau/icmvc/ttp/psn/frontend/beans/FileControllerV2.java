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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.emau.icmvc.ganimed.ttp.psn.DomainManager;
import org.emau.icmvc.ganimed.ttp.psn.PSNManager;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DBException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DeletionForbiddenException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidGeneratorException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidPSNException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownValueException;
import org.emau.icmvc.ttp.psn.frontend.datamodel.Operation;
import org.emau.icmvc.ttp.psn.frontend.exceptions.InvalidUploadException;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * backing bean for file_controls.xhtml
 * 
 * @author weiherg
 * @author bialkem, changes for deletePSNS
 * 
 */
@ManagedBean(name = "FileControllerV2")
@ViewScoped
public class FileControllerV2 {
	/**
	 * mode for processing csv files
	 * 
	 * @author weiherg
	 * 
	 */
	public enum ProcessingMode {
		PSEUDONYMISE_CREATE(messages.getString("file.operations.value1"), "role.psn.getOrCreatePseudonymForList"), 
		DEPSEUDONYMISE(messages.getString("file.operations.value2"), "role.psn.getValueForList"), 
		SEARCH(messages.getString("file.operations.value3"), "role.psn.getPseudonymForList"),
		DELETE(messages.getString("file.operations.value4"), "role.psn.deleteEntry");
		
		
		private String label;
		private String role;

		private ProcessingMode(String label, String role) {
			this.label = label;
			this.role = role;
		}

		public String getLabel() {
			return label;
		}

		public String getRole() {
			return role;
		}
	}

	public enum ColumnHandling {
		CREATE_NEW(messages.getString("file.columnhandling.value1")), 
		REPLACE_SOURCE(messages.getString("file.columnhandling.value2")), 
		DELETE_SOURCE(messages.getString("file.columnhandling.value3"));
		
		private String label;
		
		private ColumnHandling(String label) {
			this.label = label;			
		}

		public String getLabel() {
			return label;
		}		
	}
	
	
	
	/**
	 * bundle of all info/error messages used in the application
	 */
	private static ResourceBundle messages;

	private final Logger logger = LoggerFactory.getLogger(FileControllerV2.class);

	@ManagedProperty(value = "#{FileHolder}")
	private FileHolderBean fileHolder;

	@EJB(lookup = "java:global/gpas/psn-ejb/DomainManagerBean!org.emau.icmvc.ganimed.ttp.psn.DomainManager")
	private DomainManager domainManager;

	@EJB(lookup = "java:global/gpas/psn-ejb/PSNManagerBean!org.emau.icmvc.ganimed.ttp.psn.PSNManager")
	private PSNManager psnManager;

	/**
	 * display name of the file
	 */
	private String fileName;
	/**
	 * uploaded File
	 */
	private UploadedFile uploadFile;
	// OPTIONS
	/**
	 * true if first column is table header
	 */
	private boolean tableHeaderOption = false;

	private List<Operation> operations;
	

	private Operation currentOperation;
	// END OPTIONS
	/**
	 * downloadable file
	 */
	private DefaultStreamedContent outputFile;

	@PostConstruct
	public void init() {
		messages = ResourceBundle.getBundle("messages");
		reset();
	}

	public void reset() {
		
		uploadFile = null;
		
		operations = new ArrayList<Operation>();
		currentOperation = new Operation();
				
		fileHolder.reset();
	}

	public ProcessingMode[] getProcessingModes() {
		return ProcessingMode.values();
	}
	
	public ColumnHandling[] getColumnHandling() {
		return ColumnHandling.values();
	}

	/**
	 * returns a map of all available domains for selection
	 * 
	 * @return
	 */
	public Map<String, Object> getDomainMap() {
		Map<String, Object> domainMap = new HashMap<String, Object>();
		for (DomainDTO domain : domainManager.listDomains()) {
			domainMap.put(domain.getDomain(), domain.getDomain());
		}
		return domainMap;
	}

	// ---------------------------actionListeners---------------------------------------
	public void onAddOperation() {
		operations.add(currentOperation);
	}

	/**
	 * handles upload of files saves the files data in the model (fileHolder)
	 * 
	 * @param event
	 */
	public void handleFileUpload(FileUploadEvent event) {
		FacesContext context = FacesContext.getCurrentInstance();
		uploadFile = event.getFile();
		BufferedReader rd = null;
		String tmp;
		List<List<String>> rows = new ArrayList<List<String>>();
		try {
			if (uploadFile == null) {
				throw new InvalidUploadException(messages.getString("file.error.noFileSelected"));
			} else {
				// save the file id for display
				fileHolder.setUploadFileName(uploadFile.getFileName());
				if (logger.isDebugEnabled()) {
					logger.debug("uploading file: " + uploadFile.getFileName());
				}
				// read the file and convert data into a list
				rd = new BufferedReader(new InputStreamReader(uploadFile.getInputstream()));
				
				String sep = null;
				Boolean ready = false;

				// Pattern, that identifies single records(even with "") and splits the values and delimiters in 2 groups
				Pattern pattern = null;
				Matcher matcher;
				while ((tmp = rd.readLine()) != null) {
					if (!ready) {
						// Get seperator
						if (tmp.contains("sep")) {
							sep = tmp.substring(4);
							tmp = rd.readLine();
						} else {
							sep = ",";
						}
						
						// Compile pattern
						pattern = Pattern.compile("((?:\"[^\"]*?\")*|[^\"][^" + sep + "]*?)([" + sep + "]|$)");
						
						// Ready for parsing CSV data
						ready = true;
					}
					
					if (!tmp.isEmpty()) {
						ArrayList<String> record = new ArrayList<String>();
						matcher = pattern.matcher(tmp);
						while (matcher.find()) {
							record.add(matcher.group(1).replace("\"", ""));
						}
						// remove empty match at the end of the record
						record.remove(record.size() - 1);
						rows.add(record);
					}

				}
				if (rows.isEmpty()) {
					throw new InvalidUploadException("No entries in the file");
				}
				// save he data in the file holder
				fileHolder.setRows(rows, tableHeaderOption);
				Object[] args = { fileHolder.getFileSize(), fileHolder.getUploadFileName() };
				context.addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, new MessageFormat(messages.getString("file.info.success_upload")).format(args),
								""));
				if (logger.isInfoEnabled()) {
					logger.info("file upload of '" + uploadFile.getFileName() + "' sucessful");
				}
			}
		} catch (IOException e) {
			context.addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("general.error.unexpectedError"), e.getMessage()));
			if (logger.isErrorEnabled()) {
				logger.error("", e);
			}
		} catch (InvalidUploadException e) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), e.getMessage()));
			if (logger.isErrorEnabled()) {
				logger.error("", e);
			}
		} finally {
			if (rd != null) {
				try {
					rd.close();
				} catch (IOException e) {
					if (logger.isErrorEnabled()) {
						logger.error("", e);
					}
				}
			}
		}

	}

	/**
	 * creates a downloadable file from the data
	 */
	public void handleFileDownload() {
		FacesContext context = FacesContext.getCurrentInstance();
		// create the File to download
		InputStream stream = new ByteArrayInputStream(fileHolder.toCsvString().getBytes());
		outputFile = new DefaultStreamedContent(stream, "text/csv", fileHolder.getUploadFileName());
		context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, messages.getString("file.info.download"), ""));
		if (logger.isInfoEnabled()) {
			logger.info("file was downloaded with the name:" + fileName);
		}
	}

	/**
	 * button listener, that shows a confirm dialog, should PSEUDONYMISE_CREATE mode be selected just calls process() on all other modes
	 */
	public void onStart() {
		
		//remember opertations, why not
		operations.add(currentOperation);
		
		//process only current started operation
		process(currentOperation);
	}

	/**
	 * determines, which operation to call, modifies the model(FileHolder), depending on the mapping returned by Backend
	 */
	private void process(Operation operation) {
		Map<String, String> mapping;
		String columnName;
		// depending on the selected mode, a map with an entry for every value
		// in the processed column is created(Pseudonym -> value/value->Pseudonym)
		switch (operation.getMode()) {
			case SEARCH:
				columnName = operation.getSelectedDomain() + "-SEARCH(" + fileHolder.getColumns().get(operation.getSourceColumnIndex()) + ")";
				mapping = searchForPseudonyms(operation);
			break;
			case DEPSEUDONYMISE:
				columnName = operation.getSelectedDomain() + "-DEPSEUDONYMISE(" + fileHolder.getColumns().get(operation.getSourceColumnIndex()) + ")";
				mapping = processPseudonymList(operation);
			break;
			case PSEUDONYMISE_CREATE:
				columnName = operation.getSelectedDomain() + "-PSEUDONYMIZE(" + fileHolder.getColumns().get(operation.getSourceColumnIndex()) + ")";
				mapping = pseudonymiseList(operation);
			break;
			
			case DELETE:
				columnName = operation.getSelectedDomain() + "-DELETE(" + fileHolder.getColumns().get(operation.getSourceColumnIndex()) + ")";
				mapping = processDeletePseudonymList(operation);
			break;
			
			default:
				columnName = "";
				mapping = new HashMap<String, String>();
		}
		// applies the returned mapping on the data model
		// depending on whether the selected Column should be replaced or a new one added, different methods are used
				
		if (!mapping.isEmpty()) {
			switch (operation.getColumnHandling()) {
			case CREATE_NEW:
				fileHolder.addColumn(columnName, operation.getSourceColumnIndex(), mapping);
				fileHolder.getFileSize();
				break;
				
			case REPLACE_SOURCE:
				fileHolder.replaceColumn(columnName, operation.getSourceColumnIndex(), mapping);
				fileHolder.getFileSize();
				break;
				
			case DELETE_SOURCE:
				fileHolder.deleteColumn(columnName, operation.getSourceColumnIndex(), mapping);
				
				break;

			default:
				break;
			}
		}
		
		

	}

	// ----------------internal methods---------------------------------------------
	/**
	 * logic for PSEUDONYMISe  mode
	 * 
	 * @return mapping(Original Value -> Pseudonym)
	 */
	private Map<String, String> pseudonymiseList(Operation operation) {
		FacesContext context = FacesContext.getCurrentInstance();
		Map<String, String> mapping = new HashMap<String, String>();
		if (logger.isDebugEnabled()) {
			logger.debug("PSEUDONYMIZE processValueList called for Value List: '" + "bla" + "' and domain '" + operation.getSelectedDomain() + "'");
		}
		try {
			// int columnIndex = fileHolder.getColumns().indexOf(ValueColumnInput);
			mapping = psnManager.getOrCreatePseudonymForList(new HashSet<String>(fileHolder.getColumn(operation.getSourceColumnIndex())),
					operation.getSelectedDomain()).getMap();
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, messages.getString("file.info.getPseudonyms"), ""));
			if (logger.isInfoEnabled()) {
				logger.info("processing ValueList success :");
			}
			// show download
		} catch (IllegalArgumentException e) {
			context.addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("general.error.unexpectedError"), e.getMessage()));
			if (logger.isErrorEnabled()) {
				logger.error("", e);
			}
		}catch (DBException e) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("general.error.database"), e.getMessage()));
			if (logger.isErrorEnabled()) {
				logger.error("", e);
			}
		} catch (InvalidGeneratorException e) {
			context.addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("general.error.unexpectedError"), e.getMessage()));
			if (logger.isErrorEnabled()) {
				logger.error("", e);
			}
		} catch (UnknownDomainException e) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("domain.error.unkownDomain"), e.getMessage()));
			if (logger.isErrorEnabled()) {
				logger.error("", e);
			}
		}

		return mapping;

	}

	/**
	 * logic for SEARCH mode
	 * 
	 * @return mapping(Original Value -> Pseudonym)
	 */
	private Map<String, String> searchForPseudonyms(Operation operation) {
		FacesContext context = FacesContext.getCurrentInstance();
		Map<String, String> mapping = new HashMap<String, String>();
		if (logger.isDebugEnabled()) {
			logger.debug("PSEUDONYMISE - processValueList called for Value List: '" + "bla" + "' and domain '" + operation.getSelectedDomain() + "'");
		}
		try {
			mapping = psnManager.getPseudonymForList(new HashSet<String>(fileHolder.getColumn(operation.getSourceColumnIndex())),
					operation.getSelectedDomain()).getMap();
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, messages.getString("file.info.getOrCreatePseudonyms"), ""));
			if (logger.isInfoEnabled()) {
				logger.info("processing ValueList success :");
			}
			// show download
		} catch (IllegalArgumentException e) {
			context.addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("general.error.unexpectedError"), e.getMessage()));
			if (logger.isErrorEnabled()) {
				logger.error("", e);
			}
		} 
		return mapping;

	}
	
	

	/**
	 * logic for DEPSEUDONYMISE mode
	 * 
	 * @return mapping(Pseudonym -> Original value)
	 */
	private Map<String, String> processPseudonymList(Operation operation) {
		FacesContext context = FacesContext.getCurrentInstance();
		Map<String, String> mapping = new HashMap<String, String>();
		if (logger.isDebugEnabled()) {
			logger.debug("processPseudonymList called for Pseudonym List with " + fileHolder.getFileSize() + " entries for domain '"
					+ operation.getSelectedDomain() + "'");
		}
		try {
			// retrieve original Values and saves them in a Map
//			System.out.println("Column:" + fileHolder.getColumn(operation.getSourceColumnIndex()));
			mapping = psnManager.getValueForList(new HashSet<String>(fileHolder.getColumn(operation.getSourceColumnIndex())),
					operation.getSelectedDomain()).getMap();
//			System.out.println("Inverted Mapping:" + fileHolder.getColumn(operation.getSourceColumnIndex()));
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, messages.getString("file.info.getOriginalValues"), ""));
			if (logger.isInfoEnabled()) {
				logger.info("processing ValueList success");
			}
		} catch (IllegalArgumentException e) {
			context.addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("general.error.unexpectedError"), e.getMessage()));
			if (logger.isErrorEnabled()) {
				logger.error("", e);
			}
		} catch (InvalidGeneratorException e) {
			context.addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("general.error.unexpectedError"), e.getMessage()));
			if (logger.isErrorEnabled()) {
				logger.error("", e);
			}
		} catch (UnknownDomainException e) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("domain.error.unkownDomain"), e.getMessage()));
			if (logger.isErrorEnabled()) {
				logger.error("", e);
			}
		} catch (InvalidPSNException e) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("file.error.wrongDomain"), e.getMessage()));
			if (logger.isErrorEnabled()) {
				logger.error("", e);
			}
		}
		return mapping;

	}
	
	/**
	 * logic for Delete mode
	 * 
	 * @return mapping(Pseudonym -> *** value deleted ***)
	 */
	private Map<String, String> processDeletePseudonymList(Operation operation) {
		FacesContext context = FacesContext.getCurrentInstance();
		Map<String, String> mapping = new HashMap<String, String>();
		if (logger.isDebugEnabled()) {
			logger.debug("processDeletePseudonymList called for Pseudonym List with " + fileHolder.getFileSize() + " entries for domain '"
					+ operation.getSelectedDomain() + "'");
		}
		
		Object[] args = {};
		
		try {
			
			// retrieve original Values and saves them in a Map
			List<String> toBeOrigValues = fileHolder.getColumn(operation.getSourceColumnIndex());
			String targetDomain = operation.getSelectedDomain();
									
			for (String value : toBeOrigValues) {
				logger.debug("Try to delete original value="+value+" and assigned psn in domain "+targetDomain);
				args = new Object[]{value, targetDomain};
				psnManager.deleteEntry(value, targetDomain);
				
				mapping.put(value, "*** original value and psn deleted ***");
			}
			
//			mapping = psnManager.getValueForList(new HashSet<String>(fileHolder.getColumn(operation.getSourceColumnIndex())),
//					operation.getSelectedDomain()).getMap();
//			System.out.println("Inverted Mapping:" + fileHolder.getColumn(operation.getSourceColumnIndex()));
			
			
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, messages.getString("file.info.deletePseudonyms"), ""));
			if (logger.isInfoEnabled()) {
				logger.info("processing ValueList success");
			}
		} catch (IllegalArgumentException e) {
			context.addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("general.error.unexpectedError"), e.getMessage()));
			if (logger.isErrorEnabled()) {
				logger.error("", e);
			}
		}
		
		catch (DeletionForbiddenException e) {
			context.addMessage(null, new 	FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("file.error.wrongDomain"), e.getMessage()));
			if (logger.isErrorEnabled()) {
				logger.error("", e);
			}
		}
		catch (UnknownDomainException e) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("domain.error.unkownDomain"), e.getMessage()));
			if (logger.isErrorEnabled()) {
				logger.error("", e);
			}
		} 
		 catch (UnknownValueException e) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, new MessageFormat(messages.getString("psn.error.NoPsnForValue")).format(args), ""));
			if (logger.isErrorEnabled()) {
				logger.error("", e);
			}
		}
		
		return mapping;

	}

	// ----------getters/setters----------------------------------------------
	public FileHolderBean getFileHolder() {
		return fileHolder;
	}

	public void setFileHolder(FileHolderBean fileHolder) {
		this.fileHolder = fileHolder;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public DefaultStreamedContent getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(DefaultStreamedContent outputFile) {
		this.outputFile = outputFile;
	}

	public boolean isTableHeaderOption() {
		return tableHeaderOption;
	}

	public void setTableHeaderOption(boolean tableHeaderOption) {
		this.tableHeaderOption = tableHeaderOption;
	}

	public UploadedFile getUploadFile() {
		return uploadFile;
	}

	public void setUploadFile(UploadedFile uploadFile) {
		this.uploadFile = uploadFile;
	}

	/**
	 * Get-Method for operations.
	 * 
	 * @return operations
	 */
	public List<Operation> getOperations() {
		return operations;
	}

	/**
	 * Set-Method for operations.
	 * 
	 * @param operations
	 */
	public void setOperations(List<Operation> operations) {
		this.operations = operations;
	}

	/**
	 * Get-Method for currentOperation.
	 * 
	 * @return currentOperation
	 */
	public Operation getCurrentOperation() {
		return currentOperation;
	}

	/**
	 * Set-Method for currentOperation.
	 * 
	 * @param currentOperation
	 */
	public void setCurrentOperation(Operation currentOperation) {
		this.currentOperation = currentOperation;
	}

}
