package org.sakaiproject.delegatedaccess.tool.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
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
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.model.SearchResult;
import org.sakaiproject.delegatedaccess.model.ListOptionSerialized;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;
import org.sakaiproject.delegatedaccess.utils.PropertyEditableColumnCheckbox;
import org.sakaiproject.delegatedaccess.utils.PropertyEditableColumnDropdown;
import org.sakaiproject.delegatedaccess.utils.PropertyEditableColumnList;

/**
 * Creates the UserEdit page to edit a user's access
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */

public class UserEditPage  extends BaseTreePage{

	private TreeTable tree;
	private static final Logger log = Logger.getLogger(UserEditPage.class);
	private String[] defaultRole = null;
	
	@Override
	protected AbstractTree getTree() {
		return  tree;
	}

	public UserEditPage(final SearchResult searchResult){

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

		//USER NAME & IMAGE:
		add(new Label("userName", searchResult.getDisplayName()));
		//FORM:
		Form form = new Form("form");
		add(form);

		//Expand Collapse Link:
		form.add(getExpandCollapseLink());


		//tree:

		//create a map of the realms and their roles for the Role column
		final Map<String, List<String>> realmMap = sakaiProxy.getDelegatedAccessRealmOptions();
		boolean singleRoleOptions = false;
		if(realmMap.size() == 1 && ((List<String>) realmMap.values().toArray()[0]).size() == 1){
			//only one option for role, so don't bother showing it in the table
			singleRoleOptions = true;
			defaultRole = new String[]{(String) realmMap.keySet().toArray()[0], ((List<String>) realmMap.values().toArray()[0]).get(0)};
		}
		List<IColumn> columnsList = new ArrayList<IColumn>();
		columnsList.add(new PropertyTreeColumn(new ColumnLocation(Alignment.MIDDLE, 100, Unit.PROPORTIONAL),	"", "userObject.node.description"));
		columnsList.add(new PropertyEditableColumnCheckbox(new ColumnLocation(Alignment.RIGHT, 70, Unit.PX), new StringResourceModel("shoppingPeriodAdmin", null).getString(), "userObject.shoppingPeriodAdmin", DelegatedAccessConstants.TYPE_SHOPPING_PERIOD_ADMIN));
		columnsList.add(new PropertyEditableColumnCheckbox(new ColumnLocation(Alignment.RIGHT, 55, Unit.PX), new StringResourceModel("siteAccess", null).getString(), "userObject.directAccess", DelegatedAccessConstants.TYPE_ACCESS));
		if(!singleRoleOptions){
			columnsList.add(new PropertyEditableColumnDropdown(new ColumnLocation(Alignment.RIGHT, 360, Unit.PX), new StringResourceModel("userBecomes", null).getString(),
					"userObject.realmModel", realmMap, DelegatedAccessConstants.TYPE_ACCESS));
		}
		columnsList.add(new PropertyEditableColumnList(new ColumnLocation(Alignment.RIGHT, 96, Unit.PX), new StringResourceModel("restrictedToolsHeader", null).getString(),
				"userObject.restrictedTools", DelegatedAccessConstants.TYPE_ACCESS, DelegatedAccessConstants.TYPE_LISTFIELD_TOOLS));
		if(sakaiProxy.isShowTermColumnAccess() && projectLogic.getEntireTermsList().size() > 0){
			columnsList.add(new PropertyEditableColumnList(new ColumnLocation(Alignment.RIGHT, 65, Unit.PX), new StringResourceModel("termHeader", null).getString(),
					"userObject.terms", DelegatedAccessConstants.TYPE_ACCESS, DelegatedAccessConstants.TYPE_LISTFIELD_TERMS));
		}
		IColumn columns[] = columnsList.toArray(new IColumn[columnsList.size()]);

		final TreeModel treeModel = projectLogic.createEntireTreeModelForUser(searchResult.getId(), true, false);
		final List<ListOptionSerialized> blankRestrictedTools = projectLogic.getEntireToolsList();
		final List<ListOptionSerialized> blankTerms = projectLogic.getEntireTermsList();
		//a null model means the tree is empty
		tree = new TreeTable("treeTable", treeModel, columns){
			@Override
			public boolean isVisible() {
				return treeModel != null;
			}
			protected void onNodeLinkClicked(AjaxRequestTarget target, TreeNode node) {
				//the nodes are generated on the fly with ajax.  This will add any child nodes that 
				//are missing in the tree.  Expanding and collapsing will refresh the tree node
				
				tree.getTreeState().selectNode(node, false);
				
				boolean anyAdded = false;
				if(!tree.getTreeState().isNodeExpanded(node) && !((NodeModel) ((DefaultMutableTreeNode) node).getUserObject()).isAddedDirectChildrenFlag()){
					anyAdded = projectLogic.addChildrenNodes(node, searchResult.getId(), blankRestrictedTools, blankTerms);
					((NodeModel) ((DefaultMutableTreeNode) node).getUserObject()).setAddedDirectChildrenFlag(true);
				}
				if(anyAdded){
					collapseEmptyFoldersHelper((DefaultMutableTreeNode) node);
				}
				if(!tree.getTreeState().isNodeExpanded(node) || anyAdded){
					tree.getTreeState().expandNode(node);
				}else{
					tree.getTreeState().collapseNode(node);
				}
			}
			protected void onJunctionLinkClicked(AjaxRequestTarget target, TreeNode node) {
				//the nodes are generated on the fly with ajax.  This will add any child nodes that 
				//are missing in the tree.  Expanding and collapsing will refresh the tree node
				if(tree.getTreeState().isNodeExpanded(node) && !((NodeModel) ((DefaultMutableTreeNode) node).getUserObject()).isAddedDirectChildrenFlag()){
					boolean anyAdded = projectLogic.addChildrenNodes(node, searchResult.getId(), blankRestrictedTools, blankTerms);
					((NodeModel) ((DefaultMutableTreeNode) node).getUserObject()).setAddedDirectChildrenFlag(true);
					if(anyAdded){
						collapseEmptyFoldersHelper((DefaultMutableTreeNode) node);
					}
				}
			}
			@Override
			protected boolean isForceRebuildOnSelectionChange() {
				return true;
			};
		};
		if(singleRoleOptions){
			tree.add(new AttributeAppender("class", new Model("noRoles"), " "));
		}
		form.add(tree);

		//updateButton button:
		AjaxButton updateButton = new AjaxButton("update", form) {
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form arg1) {
				try{
					//save node access and roll information:
					updateNodeAccess(searchResult.getId(), defaultRole);

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
				setResponsePage(new SearchUsersPage());
			}
		};
		form.add(cancelButton);

	}
}
