package org.sakaiproject.site.tool.helper.managegroupsectionrole.rsf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.tool.helper.managegroupsectionrole.impl.SiteManageGroupSectionRoleHandler;
import org.sakaiproject.site.tool.helper.managegroupsectionrole.rsf.GroupEditViewParameters;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.user.api.User;

import uk.ac.cam.caret.sakai.rsf.producers.FrameAdjustingProducer;
import uk.ac.cam.caret.sakai.rsf.util.SakaiURLUtil;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.flow.jsfnav.DynamicNavigationCaseReporter;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.RawViewParameters;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.stringutil.StringList;

/**
 * 
 * @author
 *
 */
public class GroupDelProducer 
implements ViewComponentProducer, DynamicNavigationCaseReporter{
    
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(GroupDelProducer.class);
	
    public static final String VIEW_ID = "GroupDel";
    public MessageLocator messageLocator;
    public SiteManageGroupSectionRoleHandler handler;
    public FrameAdjustingProducer frameAdjustingProducer;
    
    public String getViewID() {    
        return VIEW_ID;
    }

    private TargettedMessageList tml;
	public void setTargettedMessageList(TargettedMessageList tml) {
		this.tml = tml;
	}
	
    public void fillComponents(UIContainer tofill, ViewParameters arg1, ComponentChecker arg2) {

    	UIOutput.make(tofill, "page-title", messageLocator.getMessage("editgroup.removegroups"));
		
		UIForm deleteForm = UIForm.make(tofill, "delete-confirm-form");
		 
		boolean renderDelete = false;
		
		// Create a multiple selection control for the tasks to be deleted.
		// We will fill in the options at the loop end once we have collected them.
		UISelect deleteselect = UISelect.makeMultiple(deleteForm, "delete-group",
				null, "#{SiteManageGroupSectionRoleHandler.deleteGroupIds}", new String[] {});

		//get the headers for the table
		UIMessage.make(deleteForm, "group-title-title","group.title");
		UIMessage.make(deleteForm, "group-size-title", "group.number");
		UIMessage.make(deleteForm, "group-remove-title", "editgroup.remove");
		
		List<Group> groups = handler.getSelectedGroups();
		
		StringList deletable = new StringList();
		M_log.debug(this + "fillComponents: got a list of " + groups.size() + " groups");
      
		if (groups != null && groups.size() > 0)
        {
            for (Iterator<Group> it=groups.iterator(); it.hasNext(); ) {
            	Group group = it.next();
            	String groupId = group.getId();
                UIBranchContainer grouprow = UIBranchContainer.make(deleteForm, "group-row:", group.getId());

    			UIOutput.make(grouprow,"group-title",group.getTitle());
    			
    			int size = 0;
    			try
    			{
    				size=AuthzGroupService.getAuthzGroup(group.getReference()).getMembers().size();
    			}
    			catch (GroupNotDefinedException e)
    			{
    				M_log.debug(this + "fillComponent: cannot find group " + group.getReference());
    			}
    			UIOutput.make(grouprow,"group-size",String.valueOf(size));

    			deletable.add(group.getId());
				UISelectChoice delete =  UISelectChoice.make(grouprow, "group-select", deleteselect.getFullID(), (deletable.size()-1));
				delete.decorators = new DecoratorList(new UITooltipDecorator(UIMessage.make("delete_group_tooltip", new String[] {group.getTitle()})));
				UIMessage message = UIMessage.make(grouprow,"delete-label","delete_group_tooltip", new String[] {group.getTitle()});
				UILabelTargetDecorator.targetLabel(message,delete);
				M_log.debug(this + ".fillComponent: this group can be deleted");
				renderDelete = true;
            }
		}

		deleteselect.optionlist.setValue(deletable.toStringArray());
		UICommand.make(deleteForm, "delete-groups",  UIMessage.make("editgroup.removegroups"), "#{SiteManageGroupSectionRoleHandler.processDeleteGroups}");
		UICommand.make(deleteForm, "cancel", UIMessage.make("editgroup.cancel"), "#{SiteManageGroupSectionRoleHandler.processCancelDelete}");
   
		//process any messages
        UIBranchContainer errorRow = UIBranchContainer.make(tofill,"error-row:", "0");
		UIMessage.make(errorRow,"error","editgroup.groupdel.alert", new String[]{});
    }
    
    public ViewParameters getViewParameters() {
        GroupEditViewParameters params = new GroupEditViewParameters();
        params.id = null;
        return params;
    }
    
    public List reportNavigationCases() {
        List togo = new ArrayList();
        togo.add(new NavigationCase("success", new SimpleViewParameters(GroupListProducer.VIEW_ID)));
    	togo.add(new NavigationCase("cancel", new SimpleViewParameters(GroupListProducer.VIEW_ID)));
        return togo;
    }
}
