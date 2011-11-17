package org.sakaiproject.delegatedaccess.tool.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation.Alignment;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation.Unit;
import org.apache.wicket.extensions.markup.html.tree.table.IColumn;
import org.apache.wicket.extensions.markup.html.tree.table.PropertyTreeColumn;
import org.apache.wicket.extensions.markup.html.tree.table.TreeTable;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.tree.AbstractTree;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;
import org.sakaiproject.delegatedaccess.utils.PropertyEditableColumnAuthDropdown;
import org.sakaiproject.delegatedaccess.utils.PropertyEditableColumnCheckbox;
import org.sakaiproject.delegatedaccess.utils.PropertyEditableColumnDate;
import org.sakaiproject.delegatedaccess.utils.PropertyEditableColumnPermList;

public class ShoppingPeriodPage extends BaseTreePage{
	private TreeTable tree;
	private static final Logger log = Logger.getLogger(ShoppingPeriodPage.class);

	@Override
	protected AbstractTree getTree() {
		return  tree;
	}

	public ShoppingPeriodPage(){

		//Form Feedback (Saved/Error)
		final Label formFeedback = new Label("formFeedback");
		formFeedback.setOutputMarkupPlaceholderTag(true);
		final String formFeedbackId = formFeedback.getMarkupId();
		add(formFeedback);
		//Form Feedback2 (Saved/Error)
		final Label formFeedback2 = new Label("formFeedback2");
		formFeedback2.setOutputMarkupPlaceholderTag(true);
		final String formFeedback2Id = formFeedback2.getMarkupId();
		add(formFeedback2);

		//FORM:
		Form form = new Form("form");
		add(form);

		//Expand Collapse Link:
		form.add(getExpandCollapseLink());


		//tree:

		//create a map of the realms and their roles for the Role column
		List<AuthzGroup> siteTemplates = sakaiProxy.getSiteTemplates();
		final Map<String, List<String>> realmMap = new HashMap<String, List<String>>();
		for(AuthzGroup group : siteTemplates){
			List<String> roles = new ArrayList<String>();
			for(Role role : group.getRoles()){
				roles.add(role.getId());
			}
			realmMap.put(group.getId(), roles);
		}
		IColumn columns[] = new IColumn[] {
				new PropertyEditableColumnCheckbox(new ColumnLocation(Alignment.LEFT, 55, Unit.PX), new StringResourceModel("access", null).getString(),
					"userObject.directAccess"),
				new PropertyTreeColumn(new ColumnLocation(Alignment.MIDDLE, 100, Unit.PROPORTIONAL),
					"", "userObject.node.title"),
				new PropertyEditableColumnDate(new ColumnLocation(Alignment.RIGHT, 200, Unit.PX), new StringResourceModel("startDate", null).getString(), "userObject.shoppingPeriodStartDate", true),
				new PropertyEditableColumnDate(new ColumnLocation(Alignment.RIGHT, 200, Unit.PX), new StringResourceModel("endDate", null).getString(), "userObject.shoppingPeriodEndDate", false),
				new PropertyEditableColumnAuthDropdown(new ColumnLocation(Alignment.RIGHT, 100, Unit.PX), new StringResourceModel("shoppingPeriodAuth", null).getString(), "userObject.shoppingPeriodAuth"),
				new PropertyEditableColumnPermList(new ColumnLocation(Alignment.RIGHT, 96, Unit.PX), new StringResourceModel("shoppingPeriodPerms", null).getString(),
				"userObject.shoppingPeriodPerms")
		};

		final TreeModel treeModel = projectLogic.createTreeModelForShoppingPeriod(sakaiProxy.getCurrentUserId());

		//a null model means the tree is empty
		tree = new TreeTable("treeTable", treeModel, columns){
			@Override
			public boolean isVisible() {
				return treeModel != null;
			}
			protected void onNodeLinkClicked(AjaxRequestTarget target, TreeNode node) {
				if(!tree.getTreeState().isNodeExpanded(node)){
					tree.getTreeState().expandNode(node);
				}else{
					tree.getTreeState().collapseNode(node);
				}
			}
			@Override
			protected boolean isForceRebuildOnSelectionChange() {
				return false;
			};
		};
		form.add(tree);

		//updateButton button:
		AjaxButton updateButton = new AjaxButton("update", form) {
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form arg1) {
				try{
					//save node access and roll information:
					updateNodeAccess(DelegatedAccessConstants.SHOPPING_PERIOD_USER);

					
					//display a "saved" message
					formFeedback.setDefaultModel(new ResourceModel("success.save"));
					formFeedback.add(new AttributeModifier("class", true, new Model("success")));
					target.addComponent(formFeedback);
					formFeedback2.setDefaultModel(new ResourceModel("success.save"));
					formFeedback2.add(new AttributeModifier("class", true, new Model("success")));
					target.addComponent(formFeedback2);
				}catch (Exception e) {
					log.error(e);
					formFeedback.setDefaultModel(new ResourceModel("failed.save"));
					formFeedback.add(new AttributeModifier("class", true, new Model("alertMessage")));
					target.addComponent(formFeedback);
					formFeedback2.setDefaultModel(new ResourceModel("failed.save"));
					formFeedback2.add(new AttributeModifier("class", true, new Model("alertMessage")));
					target.addComponent(formFeedback2);
				}
				//call a js function to hide the message in 5 seconds
				target.appendJavascript("hideFeedbackTimer('" + formFeedbackId + "');");
				target.appendJavascript("hideFeedbackTimer('" + formFeedback2Id + "');");
			}
		};
		form.add(updateButton);

		//cancelButton button:
		Button cancelButton = new Button("cancel") {
			@Override
			public void onSubmit() {
				setResponsePage(new UserPage());
			}
		};
		form.add(cancelButton);

	}


}
