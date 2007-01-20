package org.sakaiproject.tool.section.jsf;



import org.apache.myfaces.taglib.html.ext.HtmlDataTableTag;

/**
 * Author:Louis Majanja <louis@media.berkeley.edu>
 * Date: Jan 18, 2007
 * Time: 1:07:44 PM
 */
public class RowGroupDataTableTag extends HtmlDataTableTag {

    public String getRendererType() {
        return org.sakaiproject.tool.section.jsf.RowGroupDataTable.DEFAULT_RENDERER_TYPE;
    }

    public String getComponentType() {
        return RowGroupDataTable.COMPONENT_TYPE;
    }
}

