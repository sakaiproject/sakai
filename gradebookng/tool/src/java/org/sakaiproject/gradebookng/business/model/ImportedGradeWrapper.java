package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import lombok.Data;

/**
 * Created by chmaurer on 1/23/15.
 */
@Data
public class ImportedGradeWrapper implements Serializable {

	// private List<ProcessedGradeItem> processedGradeItems;
	private List<ImportedGrade> importedGrades;
	private Collection<ImportColumn> columns;
}
