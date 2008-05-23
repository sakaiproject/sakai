package org.sakaiproject.site.tool.helper.managegroup.rsf;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.tool.helper.managegroup.impl.SiteManageGroupHandler;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * 
 * @author
 *
 */
public class GroupDelProducer implements ViewComponentProducer, ViewParamsReporter {
    
    public static final String VIEW_ID = "GroupDel";
    public MessageLocator messageLocator;
    public SiteManageGroupHandler handler;      
    
    public String getViewID() {    
        return VIEW_ID;
    }

    public void fillComponents(UIContainer arg0, ViewParameters arg1, ComponentChecker arg2) {
        GroupEditViewParameters params = null;

        String groupId = null;
        
        UIBranchContainer mode = null;
        
        try {
            params = (GroupEditViewParameters) arg1;
            groupId = params.groupId;
            
        }
        catch (Exception e) {
            e.printStackTrace();
            mode = UIBranchContainer.make(arg0, "mode-failed:");
            UIOutput.make(mode, "message", e.getLocalizedMessage());
            return;
        }    
        
        if ("nil".equals(groupId)) {
            mode = UIBranchContainer.make(arg0, "mode-failed:");
            //UIOutput.make(mode, "message", messageLocator.getMessage("error_groupid"));
        }
        else {
            try {
                String title = handler.removeGroup( groupId );

                mode = UIBranchContainer.make(arg0, "mode-pass:");
                UIOutput.make(mode, "groupId", groupId);
                UIOutput.make(mode, "message", title + " " + messageLocator
                        .getMessage("success_removed"));

            } catch (IdUnusedException e) {
                mode = UIBranchContainer.make(arg0, "mode-failed:");
                UIOutput.make(mode, "message", e.getLocalizedMessage());
                e.printStackTrace();
            } catch (PermissionException e) {
                mode = UIBranchContainer.make(arg0, "mode-failed:");
                UIOutput.make(mode, "message", e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }
    
    public ViewParameters getViewParameters() {
        GroupEditViewParameters params = new GroupEditViewParameters();
        params.groupId = "nil";
        return params;
    }
}
