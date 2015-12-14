package org.sakaiproject.gradebookng.business.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by chmaurer on 3/16/15.
 */
@Data
public class ProcessedGradeItemStatus implements Serializable {

    public static final int STATUS_UPDATE = 0;
    public static final int STATUS_NEW = 1;
    public static final int STATUS_NA = 2;
    public static final int STATUS_UNKNOWN = 3;
    public static final int STATUS_EXTERNAL = 4;

    private int statusCode;
    private String statusValue;

    public ProcessedGradeItemStatus(int statusCode) {
        this.statusCode = statusCode;
    }

    public ProcessedGradeItemStatus(int statusCode, String statusValue) {
        this.statusCode = statusCode;
        this.statusValue = statusValue;
    }
}
