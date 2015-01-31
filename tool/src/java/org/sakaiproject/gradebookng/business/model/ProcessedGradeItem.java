package org.sakaiproject.gradebookng.business.model;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chmaurer on 1/27/15.
 */
@Data
public class ProcessedGradeItem implements Serializable {

    public static final int STATUS_UPDATE = 0;
    public static final int STATUS_NEW = 1;
    public static final int STATUS_NA = 2;
    public static final int STATUS_UNKNOWN = 3;

    private String itemTitle;
    private Long itemId;
    private String itemPointValue;
    private int status = STATUS_UNKNOWN;
    private List<ProcessedGradeItemDetail> processedGradeItemDetails = new ArrayList<ProcessedGradeItemDetail>();
}
