package org.sakaiproject.site.tool.helper.managegroup.rsf;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * 
 * @author
 *
 */
public class GroupEditViewParameters extends SimpleViewParameters {

    public String groupId;
    public String newTitle;
    public String url;
    public String newConfig;
    public String visible;
    
    public String getParseSpec() {
        String togo = super.getParseSpec().concat(",groupId,newTitle,visible,url,newConfig");
        return togo;
    }
    
    public ViewParameters copyBase() {
        GroupEditViewParameters togo = (GroupEditViewParameters) super.copyBase();
        togo.groupId = groupId;
        togo.newTitle = newTitle;
        togo.visible = visible;
        togo.url = url;
        togo.newConfig = newConfig;
        return togo;
    }
    
}
