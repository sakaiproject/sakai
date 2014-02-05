/*
* The Trustees of Columbia University in the City of New York
* licenses this file to you under the Educational Community License,
* Version 2.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of the
* License at:
*
* http://opensource.org/licenses/ecl2.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.delegatedaccess.tool.pages;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.tree.DefaultAbstractTree;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation.Alignment;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation.Unit;
import org.apache.wicket.extensions.markup.html.tree.table.IColumn;
import org.apache.wicket.extensions.markup.html.tree.table.PropertyTreeColumn;
import org.apache.wicket.extensions.markup.html.tree.table.TreeTable;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.markup.html.tree.AbstractTree;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.delegatedaccess.model.ListOptionSerialized;
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;
import org.sakaiproject.delegatedaccess.utils.PropertyEditableColumnAdvancedOptions;
import org.sakaiproject.delegatedaccess.utils.PropertyEditableColumnCheckbox;
import org.sakaiproject.delegatedaccess.utils.PropertyEditableColumnDate;
import org.sakaiproject.delegatedaccess.utils.PropertyEditableColumnDropdown;
import org.sakaiproject.delegatedaccess.utils.PropertyEditableColumnList;

/**
 * This is the page to edit the shopping period information used by shopping period admins
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
public class ShoppingEditPage extends BaseTreePage{
	private TreeTable tree;
	private static final Logger log = Logger.getLogger(ShoppingEditPage.class);
	private String[] defaultRole = null;

	@Override
	protected AbstractTree getTree() {
		return  tree;
	}

	public ShoppingEditPage(){
		disableLink(shoppingAdminLink);

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


		//tree:

		//create a map of the realms and their roles for the Role column
		final Map<String, String> roleMap = projectLogic.getRealmRoleDisplay(true);
		String largestRole = "";
		for(String role : roleMap.values()){
			if(role.length() > largestRole.length()){
				largestRole = role;
			}
		}
		//set the size of the role Column (shopper becomes)
		int roleColumnSize = 40 + largestRole.length() * 6;
		if(roleColumnSize < 115){
			roleColumnSize = 115;
		}
		boolean singleRoleOptions = false;
		if(roleMap.size() == 1){
			String[] split = null;
			for(String key : roleMap.keySet()){
				split = key.split(":");
			}
			if(split != null && split.length == 2){
				//only one option for role, so don't bother showing it in the table
				singleRoleOptions = true;
				defaultRole = split;
			}
		}
		final TreeModel treeModel = projectLogic.createTreeModelForShoppingPeriod(sakaiProxy.getCurrentUserId());

		List<IColumn> columnsList = new ArrayList<IColumn>();
		columnsList.add(new PropertyEditableColumnCheckbox(new ColumnLocation(Alignment.LEFT, 35, Unit.PX), "",	"userObject.directAccess", DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER));
		columnsList.add(new PropertyTreeColumn(new ColumnLocation(Alignment.MIDDLE, 100, Unit.PROPORTIONAL),	"", "userObject.node.description"));
		if(!singleRoleOptions){
			columnsList.add(new PropertyEditableColumnDropdown(new ColumnLocation(Alignment.RIGHT, roleColumnSize, Unit.PX), new StringResourceModel("shoppersBecome", null).getString(),
					"userObject.roleOption", roleMap, DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER, null));
		}
		columnsList.add(new PropertyEditableColumnDate(new ColumnLocation(Alignment.RIGHT, 100, Unit.PX), new StringResourceModel("startDate", null).getString(), "userObject.shoppingPeriodStartDate", true));
		columnsList.add(new PropertyEditableColumnDate(new ColumnLocation(Alignment.RIGHT, 100, Unit.PX), new StringResourceModel("endDate", null).getString(), "userObject.shoppingPeriodEndDate", false));
		columnsList.add(new PropertyEditableColumnList(new ColumnLocation(Alignment.RIGHT, 96, Unit.PX), new StringResourceModel("showToolsHeader", null).getString(),
				"userObject.restrictedAuthTools", DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER, DelegatedAccessConstants.TYPE_LISTFIELD_TOOLS));
		columnsList.add(new PropertyEditableColumnAdvancedOptions(new ColumnLocation(Alignment.RIGHT, 75, Unit.PX), new StringResourceModel("advanced", null).getString(), "userObject.shoppingPeriodRevokeInstructorEditable", DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER));
		IColumn columns[] = columnsList.toArray(new IColumn[columnsList.size()]);

		final List<ListOptionSerialized> blankRestrictedTools = projectLogic.getEntireToolsList();
		final boolean activeSiteFlagEnabled = sakaiProxy.isActiveSiteFlagEnabled();
		final ResourceReference inactiveWarningIcon = new CompressedResourceReference(ShoppingEditPage.class, "images/bullet_error.png");
		final ResourceReference instructorEditedIcon = new CompressedResourceReference(ShoppingEditPage.class, "images/bullet_red.png");
		//a null model means the tree is empty
		tree = new TreeTable("treeTable", treeModel, columns){
			@Override
			public boolean isVisible() {
				return treeModel != null;
			}
			protected void onNodeLinkClicked(AjaxRequestTarget target, TreeNode node) {
				tree.getTreeState().selectNode(node, false);
				
				boolean anyAdded = false;
				if(!tree.getTreeState().isNodeExpanded(node) && !((NodeModel) ((DefaultMutableTreeNode) node).getUserObject()).isAddedDirectChildrenFlag()){
					anyAdded = projectLogic.addChildrenNodes(node, DelegatedAccessConstants.SHOPPING_PERIOD_USER, blankRestrictedTools, false, null, true, false);
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
					boolean anyAdded = projectLogic.addChildrenNodes(node, DelegatedAccessConstants.SHOPPING_PERIOD_USER, blankRestrictedTools, false, null, true, false);
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
			
			protected org.apache.wicket.ResourceReference getNodeIcon(TreeNode node) {
				if(activeSiteFlagEnabled && !((NodeModel) ((DefaultMutableTreeNode) node).getUserObject()).isActive()){
					return inactiveWarningIcon;
				}else if(((NodeModel) ((DefaultMutableTreeNode) node).getUserObject()).isInstructorEdited()){
					return instructorEditedIcon;
				}else{
					return super.getNodeIcon(node);
				}
			}
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
					updateNodeAccess(DelegatedAccessConstants.SHOPPING_PERIOD_USER, defaultRole);


					//display a "saved" message
					formFeedback.setDefaultModel(new ResourceModel("success.save"));
					formFeedback.add(new AttributeModifier("class", true, new Model("success")));
					target.addComponent(formFeedback);
					formFeedback2.setDefaultModel(new ResourceModel("success.save"));
					formFeedback2.add(new AttributeModifier("class", true, new Model("success")));
					target.addComponent(formFeedback2);
				}catch (Exception e) {
					log.error(e.getMessage(), e);
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
			@Override
			public boolean isVisible() {
				return treeModel != null;
			}
		};
		form.add(updateButton);

		//cancelButton button:
		Button cancelButton = new Button("cancel") {
			@Override
			public void onSubmit() {
				setResponsePage(new UserPage());
			}
			@Override
			public boolean isVisible() {
				return treeModel != null;
			}
		};
		form.add(cancelButton);


		//Access Warning:
		Label noAccessLabel = new Label("noAccess"){
			@Override
			public boolean isVisible() {
				return treeModel == null;
			}
		};

		noAccessLabel.setDefaultModel(new StringResourceModel("noShoppingAdminAccess", null));        
		add(noAccessLabel);

		add(new Image("inactiveLegend", inactiveWarningIcon){
			@Override
			public boolean isVisible() {
				return activeSiteFlagEnabled;
			}
		});
		
		//setup legend:
		final ResourceReference nodeIcon = new CompressedResourceReference(DefaultAbstractTree.class, "res/folder-closed.gif");
		final ResourceReference siteIcon = new CompressedResourceReference(DefaultAbstractTree.class, "res/item.gif");
		add(new Label("legend", new StringResourceModel("legend", null)));
		add(new Image("legendNode", nodeIcon));
		add(new Label("legendNodeDesc", new StringResourceModel("legendNodeDesc", null)));
		add(new Image("legendSite", siteIcon));
		add(new Label("legendSiteDesc", new StringResourceModel("legendSiteDesc", null)));
		add(new Image("legendInactive",inactiveWarningIcon));
		add(new Label("legendInactiveDesc", new StringResourceModel("legendInactiveDesc", null)));
		add(new Image("legendInstructorEdited", instructorEditedIcon));
		add(new Label("legendInstructorEditedDesc", new StringResourceModel("legendInstructorEditedDesc", null)));
		
	}


}
