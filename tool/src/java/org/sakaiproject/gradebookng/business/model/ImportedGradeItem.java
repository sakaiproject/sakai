package org.sakaiproject.gradebookng.business.model;

import lombok.Data;

/**
 * Created by chmaurer on 1/21/15.
 */
@Data
public class ImportedGradeItem {

    private String gradeItemName;
    private String gradeItemComment;
    private String gradeItemScore;

}
