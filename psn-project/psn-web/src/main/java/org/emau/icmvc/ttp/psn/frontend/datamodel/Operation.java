package org.emau.icmvc.ttp.psn.frontend.datamodel;

import org.emau.icmvc.ttp.psn.frontend.beans.FileControllerV2.ColumnHandling;

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


import org.emau.icmvc.ttp.psn.frontend.beans.FileControllerV2.ProcessingMode;

public class Operation {
	/**
	 * selected domain
	 */
	private String selectedDomain;
	/**
	 * index of the source column in the file model(FileHolder)
	 */
	private int sourceColumnIndex;
	/**
	 * processing mode
	 */
	private ProcessingMode mode;
	
	private ColumnHandling columnhandling;

	

	/**
	 * Get-Method for selectedDomain.
	 * 
	 * @return selectedDomain
	 */
	public String getSelectedDomain() {
		return selectedDomain;
	}

	/**
	 * Set-Method for selectedDomain.
	 * 
	 * @param selectedDomain
	 */
	public void setSelectedDomain(String selectedDomain) {
		this.selectedDomain = selectedDomain;
	}

	/**
	 * Get-Method for valueColumnInput.
	 * 
	 * @return valueColumnInput
	 */
	public int getSourceColumnIndex() {
		return sourceColumnIndex;
	}

	/**
	 * Set-Method for valueColumnInput.
	 * 
	 * @param valueColumnInput
	 */
	public void setSourceColumnIndex(int sourceColumnIndex) {
		this.sourceColumnIndex = sourceColumnIndex;
	}

	/**
	 * Get-Method for mode.
	 * 
	 * @return mode
	 */
	public ProcessingMode getMode() {
		return mode;
	}

	/**
	 * Set-Method for mode.
	 * 
	 * @param mode
	 */
	public void setMode(ProcessingMode mode) {
		this.mode = mode;
	}

	/**
	 * Get-Method for column handling.
	 * 
	 * @return handling
	 */
	public ColumnHandling getColumnHandling() {
		return columnhandling;
	}

	/**
	 * Set-Method for Column handling
	 * 
	 * @param handling
	 */
	public void setColumnHandling(ColumnHandling handling) {
		this.columnhandling = handling;
		
	}
	
	
}
