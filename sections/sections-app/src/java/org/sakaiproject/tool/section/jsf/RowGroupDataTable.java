package org.sakaiproject.tool.section.jsf;

import org.apache.myfaces.component.html.ext.HtmlDataTable;

/**
 * Author:Louis Majanja <louis@media.berkeley.edu>
 * Date: Jan 18, 2007
 * Time: 1:07:09 PM
 */
public class RowGroupDataTable extends HtmlDataTable {

    public String category;
    public static final String COMPONENT_TYPE = "org.sakaiproject.tool.section.jsf.RowGroupDataTable";
    public static final String COMPONENT_FAMILY = "javax.faces.Data";
    public static final String DEFAULT_RENDERER_TYPE = "org.sakaiproject.tool.section.jsf.RowGroupDataTableRenderer";


    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }
}
