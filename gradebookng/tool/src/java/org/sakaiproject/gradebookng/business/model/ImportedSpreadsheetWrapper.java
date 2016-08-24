package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Wraps an imported file
 */
public class ImportedSpreadsheetWrapper implements Serializable {

	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private List<ImportedRow> rows;

	@Getter
	@Setter
	private List<ImportedColumn> columns;

	public ImportedSpreadsheetWrapper() {
		this.rows = new ArrayList<>();
		this.columns = new ArrayList<>();
	}
}
