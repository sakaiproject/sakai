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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.tree.AbstractTree;
import org.apache.wicket.markup.html.tree.BaseTree;
import org.apache.wicket.markup.html.tree.LinkTree;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.delegatedaccess.model.ListOptionSerialized;
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.model.SelectOption;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;
import org.sakaiproject.site.api.Site;

/**
 * Creates the landing page for a user to show them all their access and links to go to the sites
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
public class UserPage  extends BaseTreePage{

	private BaseTree tree;
	boolean expand = true;
	private String search = "";
	private String instructorField = "";
	private SelectOption termField;
	private TreeModel treeModel = null;
	private String userId;
	private String selectedInstructorOption = DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR_TYPE_INSTRUCTOR;
	
	protected AbstractTree getTree()
	{
		return tree;
	}

	public UserPage(){
		disableLink(accessPageLink);

		//this is the home page so set user as current user
		if(isShoppingPeriodTool()){
			userId = DelegatedAccessConstants.SHOPPING_PERIOD_USER;
		}else{
			//check if they should even have acces to this page:
			if(!hasDelegatedAccess){
				if(hasShoppingAdmin){
					setResponsePage(new ShoppingEditPage());
				}else if(hasAccessAdmin){
					setResponsePage(new SearchUsersPage());
				}
			}
			
			userId = sakaiProxy.getCurrentUserId();
		}

		//Title
		Label title = new Label("title");
		if(isShoppingPeriodTool()){
			title.setDefaultModel(new StringResourceModel("shoppingTitle", null));
		}else{
			title.setDefaultModel(new StringResourceModel("delegatedAccessTitle", null));
		}
		add(title);
		
		//Description
		Label description = new Label("description");
		if(isShoppingPeriodTool()){
			description.setDefaultModel(new StringResourceModel("shoppingInstruction", null));
		}else{
			description.setDefaultModel(new StringResourceModel("delegatedAccessInstructions", null));
		}
		add(description);
		
		setTreeModel(userId, false);
		
		final List<ListOptionSerialized> blankRestrictedTools = projectLogic.getEntireToolsList();
		if(treeModel != null){
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeModel.getRoot();
			if(((NodeModel) node.getUserObject()).isDirectAccess()){
				projectLogic.addChildrenNodes(node, userId, blankRestrictedTools, true, null, false, isShoppingPeriodTool());
			}
		}
		//a null model means the user doesn't have any associations
		tree = new LinkTree("tree", treeModel){
			@Override
			public boolean isVisible() {
				return treeModel != null
						&& ((!sakaiProxy.getDisableUserTreeView() && !isShoppingPeriodTool()) || 
								(!sakaiProxy.getDisableShoppingTreeView() && isShoppingPeriodTool()));
			}
			protected void onNodeLinkClicked(Object node, BaseTree tree, AjaxRequestTarget target) {
				if(tree.isLeaf(node)){
					//The user has clicked a leaf and chances are its a site.
					//all sites are leafs, but there may be non sites as leafs
					NodeModel nodeModel = (NodeModel) ((DefaultMutableTreeNode) node).getUserObject();
					if(nodeModel.isSiteNode()){
						Site site = sakaiProxy.getSiteByRef(nodeModel.getNode().title);
						if(site != null){
							//redirect the user to the site
							target.appendJavascript("popupWindow('" + site.getUrl() + "', '" + new StringResourceModel("popupBlockWarning", null).getObject() + "')");
						}
					}
				}else{
					boolean anyAdded = false;
					if(!tree.getTreeState().isNodeExpanded(node) && !((NodeModel) ((DefaultMutableTreeNode) node).getUserObject()).isAddedDirectChildrenFlag()){
						anyAdded = projectLogic.addChildrenNodes(node, userId, blankRestrictedTools, true, null, false, isShoppingPeriodTool());
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
			};
			
			protected void onJunctionLinkClicked(AjaxRequestTarget target, Object node) {
				//the nodes are generated on the fly with ajax.  This will add any child nodes that 
				//are missing in the tree.  Expanding and collapsing will refresh the tree node
				if(tree.getTreeState().isNodeExpanded(node) && !((NodeModel) ((DefaultMutableTreeNode) node).getUserObject()).isAddedDirectChildrenFlag()){
					boolean anyAdded = projectLogic.addChildrenNodes(node, userId, blankRestrictedTools, true, null, false, isShoppingPeriodTool());
					((NodeModel) ((DefaultMutableTreeNode) node).getUserObject()).setAddedDirectChildrenFlag(true);
					if(anyAdded){
						collapseEmptyFoldersHelper((DefaultMutableTreeNode) node);
					}
				}
			}
			
			@Override
			protected boolean isForceRebuildOnSelectionChange() {
				return false;
			};
		};
		tree.setRootLess(true);
		add(tree);
		tree.getTreeState().collapseAll();

		//Access Warning:
		Label noAccessLabel = new Label("noAccess"){
			@Override
			public boolean isVisible() {
				return treeModel == null && (!isShoppingPeriodTool() && !sakaiProxy.getDisableUserTreeView());
			}
		};
		if(isShoppingPeriodTool()){
			noAccessLabel.setDefaultModel(new StringResourceModel("noShoppingSites", null));
			
		}else{
			noAccessLabel.setDefaultModel(new StringResourceModel("noDelegatedAccess", null));
		}
		add(noAccessLabel);
		
		//no hierarchy setup:
		add(new Label("noHierarchy", new StringResourceModel("noHierarchy", null)){
			public boolean isVisible() {
				return treeModel == null && sakaiProxy.isSuperUser() && "".equals(projectLogic.getRootNodeId().id);
			}
		});
		


		//Create Search Form:
		final PropertyModel<String> messageModel = new PropertyModel<String>(this, "search");
		final PropertyModel<String> instructorFieldModel = new PropertyModel<String>(this, "instructorField");
		final PropertyModel<SelectOption> termFieldModel = new PropertyModel<SelectOption>(this, "termField");
		Form<?> form = new Form("form"){
			@Override
			protected void onSubmit() {	
				Map<String, String> advancedOptions = new HashMap<String,String>();
				if(termField != null && !"".equals(termField.getValue())){
					advancedOptions.put(DelegatedAccessConstants.ADVANCED_SEARCH_TERM, termField.getValue());
				}
				if(instructorField != null && !"".equals(instructorField)){
					advancedOptions.put(DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR, instructorField);
					advancedOptions.put(DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR_TYPE, selectedInstructorOption);
				}
				//need to set the tree model so that is is the full model
				setResponsePage(new UserPageSiteSearch(search, advancedOptions, false, false));
			}
			@Override
			public boolean isVisible() {
				return treeModel != null || isShoppingPeriodTool() || (!isShoppingPeriodTool() && sakaiProxy.getDisableUserTreeView());
			}
		};
		AbstractReadOnlyModel<String> instructorFieldLabelModel = new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if(isShoppingPeriodTool()){
					return new StringResourceModel("instructor", null).getObject() + ":";
				}else{
					return new StringResourceModel("user", null).getObject() + ":";
				}
			}
		};
		form.add(new Label("instructorFieldLabel", instructorFieldLabelModel));
		form.add(new TextField<String>("search", messageModel));
		form.add(new TextField<String>("instructorField", instructorFieldModel));
		//Instructor Options:
		RadioGroup group = new RadioGroup("instructorOptionsGroup", new PropertyModel<String>(this, "selectedInstructorOption")){
			@Override
			public boolean isVisible() {
				//only show if its not shopping period
				return !isShoppingPeriodTool();
			}
		};
		group.add(new Radio("instructorOption", Model.of(DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR_TYPE_INSTRUCTOR)));
		group.add(new Radio("memberOption", Model.of(DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR_TYPE_MEMBER)));
		form.add(group);
		List<SelectOption> termOptions = new ArrayList<SelectOption>();
		for(String[] entry : sakaiProxy.getTerms()){
			termOptions.add(new SelectOption(entry[1], entry[0]));
		}
		ChoiceRenderer choiceRenderer = new ChoiceRenderer("label", "value");
		DropDownChoice termFieldDropDown = new DropDownChoice("termField", termFieldModel, termOptions, choiceRenderer);
		//keeps the null option (choose one) after a user selects an option
		termFieldDropDown.setNullValid(true);
		form.add(termFieldDropDown);
		form.add(new WebMarkupContainer("searchHeader"));
		form.add(new Button("submitButton"));

		add(form);


	}
	
	private void setTreeModel(String userId, boolean cascade){
		if(isShoppingPeriodTool()){
			if(sakaiProxy.getDisableShoppingTreeView()){
				treeModel = null;
			}else{
				treeModel = projectLogic.createAccessTreeModelForUser(DelegatedAccessConstants.SHOPPING_PERIOD_USER, false, cascade);
				if(treeModel != null && ((DefaultMutableTreeNode) treeModel.getRoot()).getChildCount() == 0){
					treeModel = null;
				}
			}
		}else{
			if(sakaiProxy.getDisableUserTreeView()){
				treeModel = null;
			}else{
				treeModel = projectLogic.createAccessTreeModelForUser(userId, false, cascade);
			}
		}
	}
}
