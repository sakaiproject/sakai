package org.sakaiproject.delegatedaccess.tool.pages;

import javax.swing.tree.TreeNode;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.delegatedaccess.model.NodeModel;

public class EditablePanelAdvancedOptionsText extends Panel{

	private boolean loadedFlag = false;
	
	public EditablePanelAdvancedOptionsText(String id, IModel inputModel, final NodeModel nodeModel, final TreeNode node, final int userType) {
		super(id);
		
		final WebMarkupContainer inheritedSpan = new WebMarkupContainer("inheritedSpan");
		inheritedSpan.setOutputMarkupId(true);
		final String inheritedSpanId = inheritedSpan.getMarkupId();
		add(inheritedSpan);
		
		
		AjaxLink<Void> inheritedToolsLink = new AjaxLink<Void>("inheritedToolsLink"){
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				target.appendJavascript("document.getElementById('" + inheritedSpanId + "').style.display='';");
			}
		};
		
		add(inheritedToolsLink);
		
		AjaxLink<Void> closeInheritedSpanLink = new AjaxLink<Void>("closeInheritedSpanLink") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				target.appendJavascript("document.getElementById('" + inheritedSpanId + "').style.display='none';");
			}
		};
		inheritedSpan.add(closeInheritedSpanLink);

		Label inheritedNodeTitle = new Label("inheritedNodeTitle", nodeModel.getNode().title);
		inheritedSpan.add(inheritedNodeTitle);
		
		boolean showInstructorGroupTitleTmp = false;
		boolean revokeInstructorEditable = nodeModel.getNodeShoppingPeriodRevokeInstructorEditable();
		boolean revokeInstructorPublicOpt = nodeModel.getNodeShoppingPeriodRevokeInstructorPublicOpt();
		if(revokeInstructorEditable || revokeInstructorPublicOpt){
			showInstructorGroupTitleTmp = true;
		}
		final boolean showInstructorGroupTitle = showInstructorGroupTitleTmp;
		
		Label instructorGroupTitleLabel = new Label("instructorGroupTitle", new StringResourceModel("advOptInstructorGroupTitle", null)){
			@Override
			public boolean isVisible() {
				return showInstructorGroupTitle;
			}
		};
		inheritedSpan.add(instructorGroupTitleLabel);
		
		Label revokeInstructorEditableLabel = new Label("revokeInstructorEditable", new StringResourceModel("shoppingPeriodRevokeInstructorEditable", null)){
			public boolean isVisible() {
				return nodeModel.getNodeShoppingPeriodRevokeInstructorEditable();
			}
		};
		inheritedSpan.add(revokeInstructorEditableLabel);
		
		Label revokeInstructorPublicOptLabel = new Label("revokeInstructorPublicOpt", new StringResourceModel("shoppingPeriodRevokePublicOptCheckbox", null)){
			public boolean isVisible() {
				return nodeModel.getNodeShoppingPeriodRevokeInstructorPublicOpt();
			}
		};
		inheritedSpan.add(revokeInstructorPublicOptLabel);
		
	}
	
	public boolean isLoadedFlag() {
		return loadedFlag;
	}
	
}
