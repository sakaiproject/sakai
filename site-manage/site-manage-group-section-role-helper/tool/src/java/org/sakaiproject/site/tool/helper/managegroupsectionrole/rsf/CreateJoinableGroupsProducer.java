package org.sakaiproject.site.tool.helper.managegroupsectionrole.rsf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.StringUtils;

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
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Produces the new/edit joinable set UI.
 * 
 * @author Bryan Holiday, bjones86
 */
public class CreateJoinableGroupsProducer implements ViewComponentProducer, ActionResultInterceptor, ViewParamsReporter{

	public static final String VIEW_ID = "CreateJoinableGroups";
	private static final Log M_log = LogFactory.getLog(CreateJoinableGroupsProducer.class);
	public SiteManageGroupSectionRoleHandler handler;
	public MessageLocator messageLocator;

	private TargettedMessageList tml;
	public void setTargettedMessageList(TargettedMessageList tml) {
		this.tml = tml;
	}

	@Override
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

		String joinableSetId = StringUtils.trimToEmpty( ((CreateJoinableGroupViewParameters) viewparams).id );
		boolean edit = StringUtils.isNotEmpty( joinableSetId );
		boolean nameChange = !handler.joinableSetName.equals( handler.joinableSetNameOrig )
				|| (StringUtils.isNotEmpty( handler.joinableSetName ) && StringUtils.isNotEmpty( handler.joinableSetNameOrig )
							 && handler.joinableSetName.equals( handler.joinableSetNameOrig ) );

		if(edit && !nameChange){
			handler.joinableSetName = joinableSetId;
			handler.joinableSetNameOrig = joinableSetId;
		}
		else if( edit && nameChange )
		{
			handler.joinableSetNameOrig = handler.joinableSetName;
		}

		UIForm groupForm = UIForm.make(tofill, "groups-form");
		String title = messageLocator.getMessage( edit ? "group.joinable.title.edit" : "group.joinable.title" );
		UIOutput.make(groupForm, "prompt", title);
		UIOutput.make(groupForm, "emptyGroupTitleAlert", messageLocator.getMessage("editgroup.titlemissing"));
		UIOutput.make(groupForm, "instructions", messageLocator.getMessage("group.joinable.desc"));
		UILabelTargetDecorator.targetLabel(UIMessage.make(groupForm, "group-title-group", "group.joinable.setname"), UIInput.make(groupForm, "groupTitle-group", "SiteManageGroupSectionRoleHandler.joinableSetName"));
		UIInput.make(groupForm, "groupTitle-group-orig", "SiteManageGroupSectionRoleHandler.joinableSetNameOrig");

		if(edit){
			// Current groups
			UIOutput.make(groupForm, "current-groups-title", messageLocator.getMessage("group.joinable.currentgroups"));
			List<String> setGroupNames = new ArrayList<>();
			for(Group group : handler.site.getGroups()){
				String joinableSet = group.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET);
				if(joinableSet != null){
					if( (!nameChange && joinableSet.equals( joinableSetId )) || (nameChange && joinableSet.equals( handler.joinableSetName )) )
					{
						if( !handler.pendingGroupTitles.contains( group.getTitle() ) )
						{
							setGroupNames.add(group.getTitle());
						}
					}
				}
			}
			Collections.sort(setGroupNames);
			int i = 0;
			for(String name : setGroupNames){
				UIBranchContainer currentGroupsRow = UIBranchContainer.make(tofill,"current-groups-row:", Integer.toString(i));
				UIOutput.make(currentGroupsRow, "current-group", name);
				i++;
			}

			// Pending groups
			if( !handler.pendingGroupTitles.isEmpty() )
			{
				i = 0;
				Collections.sort( handler.pendingGroupTitles );
				UIOutput.make( groupForm, "pending-groups-title", messageLocator.getMessage( "group.joinable.pendingGroups" ) );
				UIOutput.make( groupForm, "pending-groups-disclaimer", messageLocator.getMessage( "group.joinable.pendingGroups.disclaimer" ) );
				for( String pendingGroupTitle : handler.pendingGroupTitles )
				{
					UIBranchContainer pendingGroupsRow = UIBranchContainer.make( tofill, "pending-groups-row:", Integer.toString( i ) );
					UIOutput.make( pendingGroupsRow, "pending-group", pendingGroupTitle );
					i++;
				}
			}
		}

		//unjoin (edit page)
		if(edit){
			//first set the option:
			for(Group group : handler.site.getGroups()){
				String joinableSetName = group.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_SET);
				if(joinableSetName != null && joinableSetName.equals(handler.joinableSetName)){
					//we only need to find the first one since all are the same
					handler.unjoinable = Boolean.valueOf(group.getProperties().getProperty(Group.GROUP_PROP_JOINABLE_UNJOINABLE));
					handler.unjoinableOrig = handler.unjoinable;
					break;
				}
			}

			UIBranchContainer allowUnjoinEdit = UIBranchContainer.make(groupForm,"allowunjoinEdit-row:");
			allowUnjoinEdit.decorate( new UIStyleDecorator( "edit" ) );
			UIBoundBoolean allowUnjoinCheckboxEdit = UIBoundBoolean.make(allowUnjoinEdit, "allowUnjoinEdit", "#{SiteManageGroupSectionRoleHandler.unjoinable}");
			UILabelTargetDecorator.targetLabel(UIMessage.make(allowUnjoinEdit, "allowUnjoinEdit-label", "group.allow.unjoinable"), allowUnjoinCheckboxEdit);
		}

		//Additional Row
		if(edit){
			UIBranchContainer additionalRow = UIBranchContainer.make(groupForm,"additional-title-row:");
			additionalRow.decorate( new UIStyleDecorator( "edit collapsed" ) );
			UIOutput.make(additionalRow, "additional-title", messageLocator.getMessage("group.joinable.additionalGroups"));
		}

		//Num of Groups Row
		UIBranchContainer groupsRow = UIBranchContainer.make(groupForm,"num-groups-row:");
		UILabelTargetDecorator.targetLabel(UIMessage.make(groupsRow, "group-unit", "group.joinable.numOfGroups"), UIInput.make(groupsRow, "num-groups", "SiteManageGroupSectionRoleHandler.joinableSetNumOfGroups"));
		if(edit){
			groupsRow.decorate( new UIStyleDecorator( "edit" ) );
		}

		//Max members Row:
		UIBranchContainer maxRow = UIBranchContainer.make(groupForm,"max-members-row:");
		UILabelTargetDecorator.targetLabel(UIMessage.make(maxRow, "group-max-members", "group.joinable.maxMembers"), UIInput.make(maxRow, "num-max-members", "SiteManageGroupSectionRoleHandler.joinableSetNumOfMembers"));
		if(edit){
			maxRow.decorate( new UIStyleDecorator( "edit" ) );
		}

		//allow preview row:
		UIBranchContainer allowPreviewRow = UIBranchContainer.make(groupForm,"allowpreview-row:");
		UIBoundBoolean checkbox = UIBoundBoolean.make(allowPreviewRow, "allowPreviewMembership", "#{SiteManageGroupSectionRoleHandler.allowPreviewMembership}");
		UILabelTargetDecorator.targetLabel(UIMessage.make(allowPreviewRow, "allowPreviewMembership-label", "group.joinable.allowPreview"), checkbox);
		if(edit){
			allowPreviewRow.decorate( new UIStyleDecorator( "edit" ) );
		}

		//allow view members row:
		UIBranchContainer allowViewRow = UIBranchContainer.make(groupForm,"allowview-row:");
		UIBoundBoolean viewMembersCheckbox = UIBoundBoolean.make(allowViewRow, "allowViewMembership", "#{SiteManageGroupSectionRoleHandler.allowViewMembership}");
		UILabelTargetDecorator.targetLabel(UIMessage.make(allowViewRow, "allowViewMembership-label", "group.allow.view.membership2"), viewMembersCheckbox);
		if(edit){
			allowViewRow.decorate( new UIStyleDecorator( "edit" ) );
		}

		//allow unjoin
		if(!edit){
			UIBranchContainer allowUnjoin = UIBranchContainer.make(groupForm,"allowunjoin-row:");
			UIBoundBoolean allowUnjoinCheckbox = UIBoundBoolean.make(allowUnjoin, "allowUnjoin", "#{SiteManageGroupSectionRoleHandler.unjoinable}");
			UILabelTargetDecorator.targetLabel(UIMessage.make(allowUnjoin, "allowUnjoin-label", "group.allow.unjoinable"), allowUnjoinCheckbox);
		}

		// Generate button
		if(edit){
			UIBranchContainer generateRow = UIBranchContainer.make(groupForm,"generate-row:");
			generateRow.decorate( new UIStyleDecorator( "edit" ) );
			UICommand.make(generateRow, "generate", messageLocator.getMessage("group.joinable.generate"), "#{SiteManageGroupSectionRoleHandler.processGenerateJoinableSet}");
		}

		//Save/Cancel
		String saveMethodBinding = "#{SiteManageGroupSectionRoleHandler." + (edit ? "processUpdateJoinableSet}" : "processCreateJoinableSet}");
		String buttonTitle = messageLocator.getMessage( (edit ? "group.joinable.edit.saveChanges" : "editgroup.new") );
		UICommand.make(groupForm, "save", buttonTitle, saveMethodBinding);
		UICommand cancel = UICommand.make(groupForm, "cancel", messageLocator.getMessage("cancel"), "#{SiteManageGroupSectionRoleHandler.processBack}");
		cancel.parameters.add(new UIDeletionBinding("#{destroyScope.resultScope}"));

		// Delete Set button:
		if(edit){
			//UICommand.make(groupForm, "delete", messageLocator.getMessage("group.joinable.delete"), "#{SiteManageGroupSectionRoleHandler.processDeleteJoinableSet}");
			//To add delete confirmation step before actually deleting joinable groups.
			UIForm deleteGroupsForm = UIForm.make(tofill, "delete-groups-form",new JoinableGroupDelViewParameters(JoinableGroupDelProducer.VIEW_ID, joinableSetId));
			UICommand.make(deleteGroupsForm, "delete", messageLocator.getMessage("group.joinable.delete"));
		}

		//process any messages
		tml = handler.messages;
		if (tml.size() > 0) {
			for (int i = 0; i < tml.size(); i ++ ) {
				UIBranchContainer errorRow = UIBranchContainer.make(groupForm,"error-row:", Integer.toString(i));
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
	public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
		if ("success".equals(actionReturn) || "cancel".equals(actionReturn)) {
			result.resultingView = new SimpleViewParameters(GroupListProducer.VIEW_ID);
		}
	}
}
