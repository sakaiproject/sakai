package org.sakaiproject.gradebookng.business.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by chmaurer on 1/27/15.
 */
@Data
public class ProcessedGradeItemDetail implements Serializable {

    private String studentEid;
    private String studentUuid;
    private String grade;
    private String comment;
}
