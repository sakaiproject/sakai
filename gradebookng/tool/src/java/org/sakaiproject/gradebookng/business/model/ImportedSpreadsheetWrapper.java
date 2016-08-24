package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;
import java.util.Collection;
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
	private Collection<ImportedColumn> columns;
}
