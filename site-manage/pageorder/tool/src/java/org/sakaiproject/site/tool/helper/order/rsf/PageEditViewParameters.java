package org.sakaiproject.site.tool.helper.order.rsf;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * 
 * @author Joshua Ryan joshua.ryan@asu.edu
 *
 */
public class PageEditViewParameters extends SimpleViewParameters {

    public String pageId;
    public String newTitle;
    public String url;
    public String newConfig;
    public String visible;
    
    public String getParseSpec() {
        String togo = super.getParseSpec().concat(",pageId,newTitle,visible,url,newConfig");
        return togo;
    }
    
    public ViewParameters copyBase() {
        PageEditViewParameters togo = (PageEditViewParameters) super.copyBase();
        togo.pageId = pageId;
        togo.newTitle = newTitle;
        togo.visible = visible;
        togo.url = url;
        togo.newConfig = newConfig;
        return togo;
    }
    
}
