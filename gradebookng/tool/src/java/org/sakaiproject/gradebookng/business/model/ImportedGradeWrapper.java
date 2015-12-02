package org.sakaiproject.gradebookng.business.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Created by chmaurer on 1/23/15.
 */
@Data
public class ImportedGradeWrapper implements Serializable {

//    private List<ProcessedGradeItem> processedGradeItems;
    private List<ImportedGrade> importedGrades;
    private Collection<ImportColumn> columns;
}
