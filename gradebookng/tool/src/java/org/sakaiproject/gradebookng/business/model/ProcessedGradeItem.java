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

    private String itemTitle;
    private Long itemId;
    private String itemPointValue;
    private ProcessedGradeItemStatus status = new ProcessedGradeItemStatus(ProcessedGradeItemStatus.STATUS_UNKNOWN);
    private List<ProcessedGradeItemDetail> processedGradeItemDetails = new ArrayList<ProcessedGradeItemDetail>();

    private String commentLabel;
    private ProcessedGradeItemStatus commentStatus = new ProcessedGradeItemStatus(ProcessedGradeItemStatus.STATUS_UNKNOWN);
}
