package org.sakaiproject.tool.assessment.ui.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DataTableColumn {


    public static final String TYPE_HTML = "html";
    public static final String TYPE_HTML_NUM = "html-num";
    public static final String TYPE_NUM = "numeric";
    public static final String TYPE_ANY_NUM = "any-number";
    public static final String TYPE_IP_ADDRESS = "ip-address";


    private boolean orderable;
    private boolean searchable;
    private String type;
}
