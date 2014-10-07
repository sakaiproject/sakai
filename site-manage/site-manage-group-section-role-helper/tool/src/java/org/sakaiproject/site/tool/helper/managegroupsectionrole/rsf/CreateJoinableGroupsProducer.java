package org.sakaiproject.site.tool.helper.managegroupsectionrole.rsf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.tool.helper.managegroupsectionrole.impl.SiteManageGroupSectionRoleHandler;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIDeletionBinding;
import uk.org.ponder.rsf.components.decorators.UICSSDecorator;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class CreateJoinableGroupsProducer implements ViewComponentProducer, ActionResultInterceptor, ViewParamsReporter{

	public static final String VIEW_ID = "CreateJoinableGroups";
	private static Log M_log = LogFactory.getLog(CreateJoinableGroupsProducer.class);
	public SiteManageGroupSectionRoleHandler handler;
	public MessageLocator messageLocator;
		
	private TargettedMessageList tml;
	public void setTargettedMessageList(TargettedMessageList tml) {
		this.tml = tml;
	}
	
	@Override
	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {
		String joinableSetId = ((CreateJoinableGroupViewParameters) viewparams).id;
		boolean edit = joinableSetId != null && !"".equals(joinableSetId);
		if(edit){
			handler.joinableSetName = joinableSetId;
			handler.joinableSetNameOrig = joinableSetId;
		}
		UIForm groupForm = UIForm.make(tofill, "groups-form");
		String title = null;
		if(edit){
			title = messageLocator.getMessage("group.joinable.title.edit");
		}else{
			title = messageLocator.getMessage("group.joinable.title");
		}
		UIOutput.make(groupForm, "prompt", title);
        UIOutput.make(groupForm, "emptyGroupTitleAlert", messageLocator.getMessage("editgroup.titlemissing"));
        UIOutput.make(groupForm, "instructions", messageLocator.getMessage("group.joinable.desc"));
        UILabelTargetDecorator.targetLabel(UIMessage.make(groupForm, "group-title-group", "group.joinable.setname"), UIInput.make(groupForm, "groupTitle-group", "SiteManageGroupSectionRoleHandler.joinableSetName"));
        UIInput.make(groupForm, "groupTitle-group-orig", "SiteManageGroupSectionRoleHandler.joinableSetNameOrig");
        
        if(edit){
        	UIOutput.make(groupForm, "current-groups-title", messageLocator.getMessage("group.joinable.currentgroups"));
        	
        	List<String> setGroupNames = new ArrayList<String>();
        	for(Group group : handler.site.getGroups()){
        		String joinableSet = group.getProperties().getProperty(group.GROUP_PROP_JOINABLE_SET);
        		if(joinableSet != null && joinableSet.equals(joinableSetId)){
        			setGroupNames.add(group.getTitle());
        		}
        	}
        	Collections.sort(setGroupNames);
        	int i = 0;
        	for(String name : setGroupNames){
        		UIBranchContainer currentGroupsRow = UIBranchContainer.make(tofill,"current-groups-row:", Integer.valueOf(i).toString());
    			UIOutput.make(currentGroupsRow, "current-group", name);
    			i++;
        	}
        }
        Map<String,String> cssMap = new HashMap<String,String>();
		cssMap.put("background","#FFFFCC");
		
		//unjoin (edit page)
		//allow unjoin
		 UIBranchContainer allowUnjoinEdit = UIBranchContainer.make(groupForm,"allowunjoinEdit-row:");
		 if(edit){
			 //first set the option:
			 for(Group group : handler.site.getGroups()){
				 String joinableSetName = group.getProperties().getProperty(group.GROUP_PROP_JOINABLE_SET);
				 if(joinableSetName != null && joinableSetName.equals(handler.joinableSetName)){
					 //we only need to find the first one since all are the same
					 handler.unjoinable = Boolean.valueOf(group.getProperties().getProperty(group.GROUP_PROP_JOINABLE_UNJOINABLE));
					 handler.unjoinableOrig = handler.unjoinable;
					 break;
				 }
			 }
			 
			 UIBoundBoolean allowUnjoinCheckboxEdit = UIBoundBoolean.make(allowUnjoinEdit, "allowUnjoinEdit", "#{SiteManageGroupSectionRoleHandler.unjoinable}");
			 UILabelTargetDecorator.targetLabel(UIMessage.make(allowUnjoinEdit, "allowUnjoinEdit-label", "group.allow.unjoinable"), allowUnjoinCheckboxEdit);
		 }
		
		
		if(edit){
			//Additional Row
			UIBranchContainer additionalRow = UIBranchContainer.make(groupForm,"additional-title-row:");
			additionalRow.decorate(new UICSSDecorator(cssMap));
			UIOutput.make(additionalRow, "additional-title", messageLocator.getMessage("group.joinable.additionalGroups"));
		}
		//Num of Groups Row
        UIBranchContainer groupsRow = UIBranchContainer.make(groupForm,"num-groups-row:");
        if(edit){
        	groupsRow.decorate(new UICSSDecorator(cssMap));
        }
        UILabelTargetDecorator.targetLabel(UIMessage.make(groupsRow, "group-unit", "group.joinable.numOfGroups"), UIInput.make(groupsRow, "num-groups", "SiteManageGroupSectionRoleHandler.joinableSetNumOfGroups"));
        //Max members Row:
        UIBranchContainer maxRow = UIBranchContainer.make(groupForm,"max-members-row:");
        if(edit){
        	maxRow.decorate(new UICSSDecorator(cssMap));
        }
        UILabelTargetDecorator.targetLabel(UIMessage.make(maxRow, "group-max-members", "group.joinable.maxMembers"), UIInput.make(maxRow, "num-max-members", "SiteManageGroupSectionRoleHandler.joinableSetNumOfMembers"));
        //allow preview row:
        UIBranchContainer allowPreviewRow = UIBranchContainer.make(groupForm,"allowpreview-row:");
        if(edit){
        	allowPreviewRow.decorate(new UICSSDecorator(cssMap));
        }
        UIBoundBoolean checkbox = UIBoundBoolean.make(allowPreviewRow, "allowPreviewMembership", "#{SiteManageGroupSectionRoleHandler.allowPreviewMembership}");
		UILabelTargetDecorator.targetLabel(UIMessage.make(allowPreviewRow, "allowPreviewMembership-label", "group.joinable.allowPreview"), checkbox);
		
		//allow view members row:
		 UIBranchContainer allowViewRow = UIBranchContainer.make(groupForm,"allowview-row:");
		 if(edit){
			 allowViewRow.decorate(new UICSSDecorator(cssMap));
		 }
		UIBoundBoolean viewMembersCheckbox = UIBoundBoolean.make(allowViewRow, "allowViewMembership", "#{SiteManageGroupSectionRoleHandler.allowViewMembership}");
		UILabelTargetDecorator.targetLabel(UIMessage.make(allowViewRow, "allowViewMembership-label", "group.allow.view.membership2"), viewMembersCheckbox);
		
		//allow unjoin
		 UIBranchContainer allowUnjoin = UIBranchContainer.make(groupForm,"allowunjoin-row:");
		 if(edit){
			 //we don't want to show this field if it's in edit mode
			 allowUnjoin.decorate(new UICSSDecorator(cssMap));
		 }else{
			 UIBoundBoolean allowUnjoinCheckbox = UIBoundBoolean.make(allowUnjoin, "allowUnjoin", "#{SiteManageGroupSectionRoleHandler.unjoinable}");
			 UILabelTargetDecorator.targetLabel(UIMessage.make(allowUnjoin, "allowUnjoin-label", "group.allow.unjoinable"), allowUnjoinCheckbox);
		 }
		
		if(edit){
			//Generate Button
			UIBranchContainer generateRow = UIBranchContainer.make(groupForm,"generate-row:");
			generateRow.decorate(new UICSSDecorator(cssMap));
			UICommand.make(generateRow, "gererate", messageLocator.getMessage("group.joinable.generate"), "#{SiteManageGroupSectionRoleHandler.processGenerateJoinableSet}");
		}
		//Save/Cancel
		String saveMethodBinding = null;
		String buttonTitle;
		if(edit){
			saveMethodBinding = "#{SiteManageGroupSectionRoleHandler.processChangeJoinableSetName}";
			buttonTitle = messageLocator.getMessage("editgroup.update");
		}else{
			saveMethodBinding = "#{SiteManageGroupSectionRoleHandler.processCreateJoinableSet}";
			buttonTitle = messageLocator.getMessage( "editgroup.new" );
		}
		UICommand.make(groupForm, "save", buttonTitle, saveMethodBinding);
        UICommand cancel = UICommand.make(groupForm, "cancel", messageLocator.getMessage("cancel"), "#{SiteManageGroupSectionRoleHandler.processBack}");
        cancel.parameters.add(new UIDeletionBinding("#{destroyScope.resultScope}"));
        
        if(edit){
        	//Delete Set button:
        	UICommand.make(groupForm, "delete", messageLocator.getMessage("group.joinable.delete"), "#{SiteManageGroupSectionRoleHandler.processDeleteJoinableSet}");
        }
      //process any messages
        tml = handler.messages;
        if (tml.size() > 0) {
			for (int i = 0; i < tml.size(); i ++ ) {
				UIBranchContainer errorRow = UIBranchContainer.make(groupForm,"error-row:", Integer.valueOf(i).toString());
				String outString = "";
				if (tml.messageAt(i).args != null ) {
					outString = messageLocator.getMessage(tml.messageAt(i).acquireMessageCode(),tml.messageAt(i).args[0]);
				} else {
					outString = messageLocator.getMessage(tml.messageAt(i).acquireMessageCode());
				}
				UIOutput.make(errorRow,"error",outString);
		    		
			}
        }
	}

	@Override
	public String getViewID() {
		return VIEW_ID;
	}

	@Override
	public ViewParameters getViewParameters() {
		CreateJoinableGroupViewParameters params = new CreateJoinableGroupViewParameters();
		params.id = null;
		return params;
	}

	@Override
	public void interceptActionResult(ARIResult result,
			ViewParameters incoming, Object actionReturn) {
		if ("success".equals(actionReturn) || "cancel".equals(actionReturn)) {
            result.resultingView = new SimpleViewParameters(GroupListProducer.VIEW_ID);
        }
	}

}
