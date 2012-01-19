package org.sakaiproject.delegatedaccess.tool.pages;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.tree.AbstractTree;
import org.apache.wicket.markup.html.tree.BaseTree;
import org.apache.wicket.markup.html.tree.LinkTree;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
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
	
	protected AbstractTree getTree()
	{
		return tree;
	}

	public UserPage(){
		disableLink(firstLink);

		//this is the home page so set user as current user
		String userId;
		if(isShoppingPeriodTool()){
			userId = DelegatedAccessConstants.SHOPPING_PERIOD_USER;
		}else{
			//check if they should even have acces to this page:
			if(!hasDelegatedAccess){
				if(hasShoppingAdmin){
					setResponsePage(new ShoppingEditPage());
				}else{
					
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
		
		//tree:

		//Expand/Collapse Link
		add(getExpandCollapseLink());

		if(isShoppingPeriodTool()){
			treeModel = projectLogic.getTreeModelForShoppingPeriod();
			if(treeModel != null && ((DefaultMutableTreeNode) treeModel.getRoot()).getChildCount() == 0){
				treeModel = null;
			}
		}else{
			treeModel = projectLogic.createAccessTreeModelForUser(userId, false, true);
		}

		//a null model means the user doesn't have any associations
		tree = new LinkTree("tree", treeModel){
			@Override
			public boolean isVisible() {
				return treeModel != null;
			}
			protected void onNodeLinkClicked(Object node, BaseTree tree, AjaxRequestTarget target) {
				if(!tree.getTreeState().isNodeExpanded(node)){
					tree.getTreeState().expandNode(node);
				}else{
					tree.getTreeState().collapseNode(node);
				}

				if(tree.isLeaf(node)){
					//The user has clicked a leaf and chances are its a site.
					//all sites are leafs, but there may be non sites as leafs
					NodeModel nodeModel = (NodeModel) ((DefaultMutableTreeNode) node).getUserObject();
					if(nodeModel.getNode().description != null && nodeModel.getNode().description.startsWith("/site/")){
						Site site = sakaiProxy.getSiteByRef(nodeModel.getNode().description);
						if(site != null){
							if(!isShoppingPeriodTool()){
								//ensure the access for this user has been granted
								projectLogic.grantAccessToSite(nodeModel);
							}
							//redirect the user to the site
							target.appendJavascript("top.location='" + site.getUrl() + "'");
						}
					}
				}
			};
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
				return treeModel == null;
			}
		};
		if(isShoppingPeriodTool()){
			noAccessLabel.setDefaultModel(new StringResourceModel("noShoppingSites", null));
			
		}else{
			noAccessLabel.setDefaultModel(new StringResourceModel("noDelegatedAccess", null));
		}
		add(noAccessLabel);


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
				}
				setResponsePage(new UserPageSiteSearch(search, advancedOptions, treeModel));
			}
			@Override
			public boolean isVisible() {
				return treeModel != null;
			}
		};
		form.add(new TextField<String>("search", messageModel));
		form.add(new TextField<String>("instructorField", instructorFieldModel));
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
}
