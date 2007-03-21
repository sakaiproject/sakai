package org.sakaiproject.site.tool.helper.order.rsf;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * 
 * @author Joshua Ryan joshua.ryan@asu.edu
 *
 */
public class PageAddViewParameters extends SimpleViewParameters {

    public String toolId;
    public String newTitle;
    public String mode;
    
    public String getParseSpec() {
        String togo = super.getParseSpec().concat(",mode,toolId,newTitle");
        return togo;
    }
    
    public ViewParameters copyBase() {
        PageAddViewParameters togo = (PageAddViewParameters) super.copyBase();
        togo.toolId = toolId;
        togo.mode = mode;
        togo.newTitle = newTitle;
        return togo;
    }
    
}
