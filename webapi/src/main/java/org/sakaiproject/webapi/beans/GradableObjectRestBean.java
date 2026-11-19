package org.sakaiproject.webapi.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// toolId is the gradebook item's EXTERNAL_APP_NAME
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GradableObjectRestBean {

    private String siteId;
    private String entityId;
    private String toolId;
    private String title;
    private Double pointsPossible;
    private String dueDate;
    private String openDate;

}
