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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import lombok.extern.slf4j.Slf4j;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MarkupContainer;
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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

import org.sakaiproject.delegatedaccess.model.HierarchyNodeSerialized;
import org.sakaiproject.delegatedaccess.model.ListOptionSerialized;
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.model.SelectOption;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;
import org.sakaiproject.delegatedaccess.utils.PropertyEditableColumnAdvancedUserOptions;
import org.sakaiproject.delegatedaccess.utils.PropertyEditableColumnCheckbox;
import org.sakaiproject.delegatedaccess.utils.PropertyEditableColumnDropdown;
import org.sakaiproject.delegatedaccess.utils.PropertyEditableColumnList;

/**
 * Creates the UserEdit page to edit a user's access
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
@Slf4j
public class UserEditPage  extends BaseTreePage{

	private TreeTable tree;
	private String[] defaultRole = null;
	List<String> accessAdminNodeIds = null;
	private SelectOption filterHierarchy;
	private String filterSearch = "";
	private List<ListOptionSerialized> blankRestrictedTools;
	private boolean modifiedAlert = false;
	
	@Override
	protected DefaultAbstractTree getTree() {
		return  tree;
	}

	public UserEditPage(final String userId, final String displayName){
		blankRestrictedTools = projectLogic.getEntireToolsList();
		
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
		add(new Label("userName", displayName));
		//FORM:
		Form form = new Form("form");
		add(form);
		//Filter Forum
		Form filterForm = new Form("filterform");
		add(filterForm);

		//Expand Collapse Link:
		filterForm.add(getExpandCollapseLink());

		//Filter Search:
		
		//Dropdown
		final ChoiceRenderer choiceRenderer = new ChoiceRenderer("label", "value");
		final PropertyModel<SelectOption> filterHierarchydModel = new PropertyModel<SelectOption>(this, "filterHierarchy");
		List<SelectOption> hierarchyOptions = new ArrayList<SelectOption>();
		String[] hierarchy = sakaiProxy.getServerConfigurationStrings(DelegatedAccessConstants.HIERARCHY_SITE_PROPERTIES);
		if(hierarchy == null || hierarchy.length == 0){
			hierarchy = DelegatedAccessConstants.DEFAULT_HIERARCHY;
		}
		for(int i = 0; i < hierarchy.length; i++){
			hierarchyOptions.add(new SelectOption(hierarchy[i], "" + i));
		}
		final DropDownChoice filterHierarchyDropDown = new DropDownChoice("filterHierarchyLevel", filterHierarchydModel, hierarchyOptions, choiceRenderer);
		filterHierarchyDropDown.setOutputMarkupPlaceholderTag(true);
		filterForm.add(filterHierarchyDropDown);
		//Filter Search field
		final PropertyModel<String> filterSearchModel = new PropertyModel<String>(this, "filterSearch");
		final TextField<String> filterSearchTextField = new TextField<String>("filterSearch", filterSearchModel);
		filterSearchTextField.setOutputMarkupPlaceholderTag(true);
		filterForm.add(filterSearchTextField);
		//submit button:
		filterForm.add(new AjaxButton("filterButton", new StringResourceModel("filter", null)){

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> arg1) {
				DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) getTree().getModelObject().getRoot();
				//check that no nodes have been modified
				if(!modifiedAlert && anyNodesModified(rootNode)){
					formFeedback.setDefaultModel(new ResourceModel("modificationsPending"));
					formFeedback.add(new AttributeModifier("class", true, new Model("alertMessage")));
					target.add(formFeedback);
					formFeedback2.setDefaultModel(new ResourceModel("modificationsPending"));
					formFeedback2.add(new AttributeModifier("class", true, new Model("alertMessage")));
					target.add(formFeedback2);
					modifiedAlert = true;
					//call a js function to hide the message in 5 seconds
					target.appendJavaScript("hideFeedbackTimer('" + formFeedbackId + "');");
					target.appendJavaScript("hideFeedbackTimer('" + formFeedback2Id + "');");
				}else{
					//now go through the tree and make sure its been loaded at every level:
					Integer depth = null;
					if(filterHierarchy != null && filterHierarchy.getValue() != null && !"".equals(filterHierarchy.getValue().trim())){
						try{
							depth = Integer.parseInt(filterHierarchy.getValue());
						}catch(Exception e){
							//number format exception, ignore
						}
					}
					if(depth != null && filterSearch != null && !"".equals(filterSearch.trim())){
						expandTreeToDepth(rootNode, depth, userId, blankRestrictedTools, accessAdminNodeIds, false, false, false, filterSearch);
						getTree().updateTree(target);
					}
					modifiedAlert = false;
				}
			}			
		});
		filterForm.add(new AjaxButton("filterClearButton", new StringResourceModel("clear", null)){

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> arg1) {
				DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) getTree().getModelObject().getRoot();
				//check that no nodes have been modified
				if(!modifiedAlert && anyNodesModified(rootNode)){
					formFeedback.setDefaultModel(new ResourceModel("modificationsPending"));
					formFeedback.add(new AttributeModifier("class", true, new Model("alertMessage")));
					target.add(formFeedback);
					formFeedback2.setDefaultModel(new ResourceModel("modificationsPending"));
					formFeedback2.add(new AttributeModifier("class", true, new Model("alertMessage")));
					target.add(formFeedback2);
					modifiedAlert = true;
					//call a js function to hide the message in 5 seconds
					target.appendJavaScript("hideFeedbackTimer('" + formFeedbackId + "');");
					target.appendJavaScript("hideFeedbackTimer('" + formFeedback2Id + "');");
				}else{
					filterSearch = "";
					filterHierarchy = null;
					target.add(filterSearchTextField);
					target.add(filterHierarchyDropDown);

					((NodeModel) rootNode.getUserObject()).setAddedDirectChildrenFlag(false);
					rootNode.removeAllChildren();				
					getTree().getTreeState().collapseAll();
					getTree().updateTree(target);
					modifiedAlert = false;
				}
			}
		});
		
		//tree:

		//create a map of the realms and their roles for the Role column
		final Map<String, String> roleMap = projectLogic.getRealmRoleDisplay(false);
		String largestRole = "";
		for(String role : roleMap.values()){
			if(role.length() > largestRole.length()){
				largestRole = role;
			}
		}
		//set the size of the role Column (shopper becomes)
		int roleColumnSize = 80 + largestRole.length() * 6;
		if(roleColumnSize < 155){
			//for "Choose One" default option
			roleColumnSize = 155;
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
		List<IColumn> columnsList = new ArrayList<IColumn>();
		columnsList.add(new PropertyTreeColumn(new ColumnLocation(Alignment.MIDDLE, 100, Unit.PROPORTIONAL),	"", "userObject.node.description"));
		if(sakaiProxy.isSuperUser()){
			columnsList.add(new PropertyEditableColumnCheckbox(new ColumnLocation(Alignment.RIGHT, 70, Unit.PX), new StringResourceModel("accessAdmin", null).getString(), "userObject.accessAdmin", DelegatedAccessConstants.TYPE_ACCESS_ADMIN));
			columnsList.add(new PropertyEditableColumnCheckbox(new ColumnLocation(Alignment.RIGHT, 90, Unit.PX), new StringResourceModel("shoppingPeriodAdmin", null).getString(), "userObject.shoppingPeriodAdmin", DelegatedAccessConstants.TYPE_SHOPPING_PERIOD_ADMIN));
		}
		columnsList.add(new PropertyEditableColumnCheckbox(new ColumnLocation(Alignment.RIGHT, 68, Unit.PX), new StringResourceModel("siteAccess", null).getString(), "userObject.directAccess", DelegatedAccessConstants.TYPE_ACCESS));
		if(!singleRoleOptions){
			columnsList.add(new PropertyEditableColumnDropdown(new ColumnLocation(Alignment.RIGHT, roleColumnSize, Unit.PX), new StringResourceModel("userBecomes", null).getString(),
					"userObject.roleOption", roleMap, DelegatedAccessConstants.TYPE_ACCESS, sakaiProxy.isSuperUser() ? null : sakaiProxy.getSubAdminOrderedRealmRoles()));
		}
		columnsList.add(new PropertyEditableColumnList(new ColumnLocation(Alignment.RIGHT, 134, Unit.PX), new StringResourceModel("restrictedToolsHeader", null).getString(),
				"userObject.restrictedAuthTools", DelegatedAccessConstants.TYPE_ACCESS, DelegatedAccessConstants.TYPE_LISTFIELD_TOOLS));
		//setup advanced options settings:
		Map<String, Object> advSettings = new HashMap<String, Object>();
		advSettings.put(PropertyEditableColumnAdvancedUserOptions.SETTINGS_ALLOW_SET_BECOME_USER, sakaiProxy.isSuperUser() || sakaiProxy.allowAccessAdminsSetBecomeUserPerm());
		columnsList.add(new PropertyEditableColumnAdvancedUserOptions(new ColumnLocation(Alignment.RIGHT, 92, Unit.PX), new StringResourceModel("advanced", null).getString(), "", advSettings));
		IColumn columns[] = columnsList.toArray(new IColumn[columnsList.size()]);

		//if the user isn't a super user, they should only be able to edit the nodes they 
		//have been granted accessAdmin privileges
		if(!sakaiProxy.isSuperUser()){
			Set<HierarchyNodeSerialized> accessAdminNodes = projectLogic.getAccessAdminNodesForUser(sakaiProxy.getCurrentUserId());
			accessAdminNodeIds = new ArrayList<String>();
			if(accessAdminNodes != null){
				for(HierarchyNodeSerialized node : accessAdminNodes){
					accessAdminNodeIds.add(node.id);
				}
			}
		}
		
		final TreeModel treeModel = projectLogic.createEntireTreeModelForUser(userId, true, false);		
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
					anyAdded = projectLogic.addChildrenNodes(node, userId, blankRestrictedTools, false, accessAdminNodeIds, false, false);
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
					boolean anyAdded = projectLogic.addChildrenNodes(node, userId, blankRestrictedTools, false, accessAdminNodeIds, false, false);
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
			
			@Override
			protected MarkupContainer newNodeLink(MarkupContainer parent, String id, TreeNode node) {
				try{
					parent.add(new AttributeAppender("title", new Model(((NodeModel) ((DefaultMutableTreeNode) node).getUserObject()).getNode().description), " "));
				}catch(Exception e){
					log.error(e.getMessage(), e);
				}
				return super.newNodeLink(parent, id, node);
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
					updateNodeAccess(userId, defaultRole);

					//display a "saved" message
					formFeedback.setDefaultModel(new ResourceModel("success.save"));
					formFeedback.add(new AttributeModifier("class", true, new Model("success")));
					target.add(formFeedback);
					formFeedback2.setDefaultModel(new ResourceModel("success.save"));
					formFeedback2.add(new AttributeModifier("class", true, new Model("success")));
					target.add(formFeedback2);
				}catch (Exception e) {
					log.error(e.getMessage(), e);
					formFeedback.setDefaultModel(new ResourceModel("failed.save"));
					formFeedback.add(new AttributeModifier("class", true, new Model("alertMessage")));
					target.add(formFeedback);
					formFeedback2.setDefaultModel(new ResourceModel("failed.save"));
					formFeedback2.add(new AttributeModifier("class", true, new Model("alertMessage")));
					target.add(formFeedback2);
				}
				//call a js function to hide the message in 5 seconds
				target.appendJavaScript("hideFeedbackTimer('" + formFeedbackId + "');");
				target.appendJavaScript("hideFeedbackTimer('" + formFeedback2Id + "');");
				modifiedAlert = false;
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
