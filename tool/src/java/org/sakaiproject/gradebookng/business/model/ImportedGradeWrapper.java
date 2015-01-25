package org.sakaiproject.gradebookng.business.model;

import lombok.Data;

import java.util.List;

/**
 * Created by chmaurer on 1/23/15.
 */
@Data
public class ImportedGradeWrapper {

    private List<String> assignmentNames;
    private List<ImportedGrade> importedGrades;
}
