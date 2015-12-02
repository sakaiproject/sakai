package org.sakaiproject.gradebookng.business.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by chmaurer on 1/21/15.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ImportedGradeItem implements Serializable {

    private String gradeItemName;
    private String gradeItemComment;
    private String gradeItemScore;

}
