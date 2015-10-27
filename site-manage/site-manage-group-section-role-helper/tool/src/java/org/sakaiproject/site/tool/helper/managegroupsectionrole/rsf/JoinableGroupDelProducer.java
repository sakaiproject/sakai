package org.sakaiproject.site.tool.helper.managegroupsectionrole.rsf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.tool.helper.managegroupsectionrole.impl.SiteManageGroupSectionRoleHandler;
import uk.ac.cam.caret.sakai.rsf.producers.FrameAdjustingProducer;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.*;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * 
 * @author
 *
 */
public class JoinableGroupDelProducer
implements ViewComponentProducer, ActionResultInterceptor, ViewParamsReporter {
    
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(JoinableGroupDelProducer.class);
	
    public static final String VIEW_ID = "JoinableGroupDel";
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
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker arg2) {

    	UIOutput.make(tofill, "page-title", messageLocator.getMessage("editgroup.removegroups"));
		
		UIForm deleteForm = UIForm.make(tofill, "delete-jg-confirm-form");
		 
		boolean renderDelete = false;

		String joinableSetId = ((JoinableGroupDelViewParameters) viewparams).id;
		boolean delete = joinableSetId != null && !"".equals(joinableSetId);
		if(delete){
			handler.joinableSetName = joinableSetId;
			handler.joinableSetNameOrig = joinableSetId;
		}
		UIOutput.make(deleteForm, "prompt", messageLocator.getMessage("editgroup.removegroups"));
		UILabelTargetDecorator.targetLabel(UIMessage.make(deleteForm, "group-title-group", "group.joinable.setname"), UIOutput.make(deleteForm, "groupTitle-group", joinableSetId));

        if (delete) {
            UIOutput.make(deleteForm, "current-groups-title", messageLocator.getMessage("group.joinable.currentgroups"));
			int i = 0;
			for(Group group : handler.site.getGroups()){
				String joinableSet = group.getProperties().getProperty(group.GROUP_PROP_JOINABLE_SET);
				if(joinableSet != null && joinableSet.equals(joinableSetId)){
					UIBranchContainer currentGroupsRow = UIBranchContainer.make(tofill,"current-groups-row:", Integer.valueOf(i).toString());
					UIOutput.make(currentGroupsRow, "current-group", group.getTitle());
					i++;
				}
			}

		}
		UICommand.make(deleteForm, "delete-groups",  UIMessage.make("editgroup.removegroups"), "#{SiteManageGroupSectionRoleHandler.processDeleteJoinableSet}");
        //Set the cancel button to redirect to EditJoinableSet form
        UIForm deleteJoinableSetForm = UIForm.make(tofill, "delete-jg-form",new CreateJoinableGroupViewParameters(CreateJoinableGroupsProducer.VIEW_ID, joinableSetId));
		UICommand cancel = UICommand.make(deleteJoinableSetForm, "cancel", UIMessage.make("editgroup.cancel"));
		cancel.parameters.add(new UIDeletionBinding("#{destroyScope.resultScope}"));
   
		//process any messages
        UIBranchContainer errorRow = UIBranchContainer.make(tofill,"error-row:", "0");
		UIMessage.make(errorRow,"error","editgroup.groupdel.alert", new String[]{});
    }

    @Override
    public ViewParameters getViewParameters() {
        JoinableGroupDelViewParameters params = new JoinableGroupDelViewParameters();
        params.id = null;
        return params;
    }
    
    // new hotness
    public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
        if ("success".equals(actionReturn) || "cancel".equals(actionReturn)) {
            result.resultingView = new SimpleViewParameters(GroupListProducer.VIEW_ID);
        }
    }
}
