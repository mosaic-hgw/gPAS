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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 * holds the uploaded file with all modifications made as a two dimensional
 * ArrayList
 * 
 * @author weiherg
 * 
 */
@ManagedBean(name = "FileHolder")
@ViewScoped
public class FileHolderBean {
	private String uploadFileName;
	private int fileSize;
	private List<List<String>> rows;
	private List<List<String>> filteredRows;
	private List<String> columns;

	@PostConstruct
	public void reset() {
		uploadFileName = null;
		fileSize = -1;
		rows = null;
		filteredRows = null;
		columns = null;
	}

	/**
	 * get all the values in one column of the files data
	 * 
	 * @param index
	 *            index of the column
	 * @return List of values in he column
	 */
	public List<String> getColumn(int index) {
		List<String> result = new ArrayList<String>();
		for (List<String> row : rows) {
			result.add(row.get(index));
		}
		return result;
	}

	/**
	 * adds a column to the files data by applying the given mapping on the
	 * given source column
	 * 
	 * @param newColumnName
	 *            column header of the new column
	 * @param refIndex
	 *            index of source column
	 * @param rowMapping
	 *            mapping to apply on source column
	 */
	public void addColumn(String newColumnName, int refIndex, Map<String, String> rowMapping) {
		columns.add(newColumnName);
		// int refIndex = columns.indexOf(refColumn);
		for (List<String> row : rows) {
			row.add(rowMapping.get(row.get(refIndex)));
		}
	}

	/**
	 * replaces a column by applying the given mapping on the given source
	 * column
	 * 
	 * @param newColumnName
	 *            column header of the new column
	 * @param refIndex
	 *            index of source column
	 * @param rowMapping
	 *            mapping to apply on source column
	 */
	public void replaceColumn(String newColumnName, int refIndex, Map<String, String> rowMapping) {
		// int refIndex = columns.indexOf(refColumn);
		columns.set(refIndex, newColumnName);
		for (List<String> row : rows) {
			row.set(refIndex, rowMapping.get(row.get(refIndex)));
		}

	}

	/**
	 * returns a map of all column headers and there indexes
	 * 
	 * @return map of column headers
	 */
	public Map<String, Integer> getColumnsMap() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		if (columns != null) {
			for (int i = 0; i < columns.size(); i++) {
				map.put(columns.get(i), i);
			}
		}
		return map;
	}

	/**
	 * parses the file data into a csv string for streaming into a download file
	 * 
	 * @return csv string
	 */
	public String toCsvString() {

		StringBuilder output = new StringBuilder();
		output.append("sep=;");
		output.append("\r\n");

		if (columns != null) {
			for (int i = 0; i < columns.size(); i++) {
				output.append(columns.get(i));

				if (i < columns.size() - 1)
					output.append(';');
			}
			output.append("\r\n");
		}

		for (List<String> row : rows) {
			output.append(row.get(0));
			for (int i = 1; i < row.size(); i++) {
				output.append(';');
				output.append(row.get(i));
			}
			output.append("\r\n");
		}
		return output.toString();
	}

	public List<String> getColumns() {
		return columns;
	}

	public String getUploadFileName() {
		return uploadFileName;
	}

	public void setUploadFileName(String uploadFileName) {
		this.uploadFileName = uploadFileName;
	}

	public List<List<String>> getRows() {
		return rows;
	}

	/**
	 * sets the file data rows with the given data, creating new column
	 * headers(tableHeaderOption=false) or using the first row as header
	 * (tableHeaderOption=false)
	 * 
	 * @param rows
	 *            file data
	 * @param tableHeaderOption
	 */
	public void setRows(List<List<String>> rows, boolean tableHeaderOption) {
		this.rows = rows;
		int columnCount = rows.get(0).size();
		columns = new ArrayList<String>();
		if (tableHeaderOption) {
			columns = rows.get(0);
			this.rows.remove(0);
			columnCount--;
		} else {
			for (int i = 0; i < columnCount; i++) {
				columns.add("column" + i);
			}
		}
		fileSize = rows.size();
	}

	public int getFileSize() {
		return fileSize;
	}

	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}

	public List<List<String>> getFilteredRows() {
		return filteredRows;
	}

	public void setFilteredRows(List<List<String>> filteredRows) {
		this.filteredRows = filteredRows;
	}

	public void deleteColumn(String columnName, int sourceColumnIndex, Map<String, String> mapping) {
		
		if(columns!=null && columns.size() > sourceColumnIndex)
		{			
			columns.remove(sourceColumnIndex);
		}	

		//though cols and rows are handled seperately, row content needs to be updated as well
		for (List<String> row : rows) {
			row.remove(sourceColumnIndex);
		}
		
	}
}
