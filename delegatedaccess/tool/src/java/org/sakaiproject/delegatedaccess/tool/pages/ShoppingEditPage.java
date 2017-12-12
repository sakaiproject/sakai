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

import lombok.extern.slf4j.Slf4j;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
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
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

import org.sakaiproject.delegatedaccess.model.ListOptionSerialized;
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.model.SelectOption;
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
@Slf4j
public class ShoppingEditPage extends BaseTreePage{
	private TreeTable tree;
	private String[] defaultRole = null;
	private SelectOption filterHierarchy;
	private String filterSearch = "";
	private List<ListOptionSerialized> blankRestrictedTools;
	private boolean modifiedAlert = false;

	public static final String SCRIPT_DATEPICKER = "javascript/init-datepicker.js";

	@Override
	protected DefaultAbstractTree getTree() {
		return  tree;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forUrl(SCRIPT_DATEPICKER));
	}

	public ShoppingEditPage(){
		disableLink(shoppingAdminLink);
		
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

		//FORM:
		Form form = new Form("form");
		add(form);

		//Filter Forum
		Form filterForm = new Form("filterform");
		add(filterForm);
		
		//bulk add, edit, delete link:
		filterForm.add(new Link("bulkEditLink"){

			@Override
			public void onClick() {
				setResponsePage(new ShoppingEditBulkPage());
			}			
		});
		


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
						expandTreeToDepth(rootNode, depth, DelegatedAccessConstants.SHOPPING_PERIOD_USER, blankRestrictedTools, null, false, true, false, filterSearch);
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
		final Map<String, String> roleMap = projectLogic.getRealmRoleDisplay(true);
		String largestRole = "";
		for(String role : roleMap.values()){
			if(role.length() > largestRole.length()){
				largestRole = role;
			}
		}
		//set the size of the role Column (shopper becomes)
		int roleColumnSize = 80 + largestRole.length() * 6;
		if(roleColumnSize < 155){
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
		final TreeModel treeModel = projectLogic.createTreeModelForShoppingPeriod(sakaiProxy.getCurrentUserId());

		List<IColumn> columnsList = new ArrayList<IColumn>();
		columnsList.add(new PropertyEditableColumnCheckbox(new ColumnLocation(Alignment.LEFT, 35, Unit.PX), "",	"userObject.directAccess", DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER));
		columnsList.add(new PropertyTreeColumn(new ColumnLocation(Alignment.MIDDLE, 100, Unit.PROPORTIONAL),	"", "userObject.node.description"));
		if(!singleRoleOptions){
			columnsList.add(new PropertyEditableColumnDropdown(new ColumnLocation(Alignment.RIGHT, roleColumnSize, Unit.PX), new StringResourceModel("shoppersBecome", null).getString(),
					"userObject.roleOption", roleMap, DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER, null));
		}
		columnsList.add(new PropertyEditableColumnDate(new ColumnLocation(Alignment.RIGHT, 104, Unit.PX), new StringResourceModel("startDate", null).getString(), "userObject.shoppingPeriodStartDate", true));
		columnsList.add(new PropertyEditableColumnDate(new ColumnLocation(Alignment.RIGHT, 104, Unit.PX), new StringResourceModel("endDate", null).getString(), "userObject.shoppingPeriodEndDate", false));
		columnsList.add(new PropertyEditableColumnList(new ColumnLocation(Alignment.RIGHT, 120, Unit.PX), new StringResourceModel("showToolsHeader", null).getString(),
				"userObject.restrictedAuthTools", DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER, DelegatedAccessConstants.TYPE_LISTFIELD_TOOLS));
		columnsList.add(new PropertyEditableColumnAdvancedOptions(new ColumnLocation(Alignment.RIGHT, 92, Unit.PX), new StringResourceModel("advanced", null).getString(), "userObject.shoppingPeriodRevokeInstructorEditable", DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER));
		IColumn columns[] = columnsList.toArray(new IColumn[columnsList.size()]);

		final boolean activeSiteFlagEnabled = sakaiProxy.isActiveSiteFlagEnabled();
		final ResourceReference inactiveWarningIcon = new PackageResourceReference(ShoppingEditPage.class, "images/bullet_error.png");
		final ResourceReference instructorEditedIcon = new PackageResourceReference(ShoppingEditPage.class, "images/bullet_red.png");
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
			
			protected ResourceReference getNodeIcon(TreeNode node) {
				if(activeSiteFlagEnabled && !((NodeModel) ((DefaultMutableTreeNode) node).getUserObject()).isActive()){
					return inactiveWarningIcon;
				}else if(((NodeModel) ((DefaultMutableTreeNode) node).getUserObject()).isInstructorEdited()){
					return instructorEditedIcon;
				}else{
					return super.getNodeIcon(node);
				}
			}
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
					updateNodeAccess(DelegatedAccessConstants.SHOPPING_PERIOD_USER, defaultRole);


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
		final ResourceReference nodeIcon = new PackageResourceReference(DefaultAbstractTree.class, "res/folder-closed.gif");
		final ResourceReference siteIcon = new PackageResourceReference(DefaultAbstractTree.class, "res/item.gif");
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
