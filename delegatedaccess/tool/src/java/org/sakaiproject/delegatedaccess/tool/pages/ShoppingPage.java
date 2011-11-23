package org.sakaiproject.delegatedaccess.tool.pages;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.tree.AbstractTree;
import org.apache.wicket.markup.html.tree.BaseTree;
import org.apache.wicket.markup.html.tree.LinkTree;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;
import org.sakaiproject.site.api.Site;

public class ShoppingPage extends ShoppingBasePage{
	private BaseTree tree;
	boolean expand = true;
	private String search = "";

	protected AbstractTree getTree()
	{
		return tree;
	}

	public ShoppingPage(){
		disableLink(firstLink);

		//this is the home page so set user as current user
		String userId = sakaiProxy.getCurrentUserId();

		//tree:

		//Expand/Collapse Link
		add(getExpandCollapseLink());

		final TreeModel treeModel = projectLogic.createAccessTreeModelForUser(DelegatedAccessConstants.SHOPPING_PERIOD_USER, false, true);

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

		noAccessLabel.setDefaultModel(new StringResourceModel("noShoppingSites", null));        
		add(noAccessLabel);


		//Create Search Form:
		final PropertyModel<String> messageModel = new PropertyModel<String>(this, "search");
		Form<?> form = new Form("form"){
			@Override
			protected void onSubmit() {	
				setResponsePage(new ShoppingPageSiteSearch(search, treeModel));
			}
		};
		form.add(new TextField<String>("search", messageModel){
			@Override
			public boolean isVisible() {
				return treeModel != null;
			}
		});
		form.add(new WebMarkupContainer("searchHeader"){
			@Override
			public boolean isVisible() {
				return treeModel != null;
			};
		});
		form.add(new Button("submitButton"){
			@Override
			public boolean isVisible() {
				return treeModel != null;
			}
		});

		add(form);


	}

	/**
	 * Helper function for collapseEmptyFoldersHelper
	 * @param node
	 */
	protected void collapseEmptyFoldersHelper(DefaultMutableTreeNode node){
		if(node != null){
			if(!node.isLeaf() && node.getChildCount() == 0){
				//this is a node that isn't a leaf but hasn't had the children updated, make it collapse
				getTree().getTreeState().collapseNode(node);
			}
			for(int i = 0; i < node.getChildCount(); i++){
				collapseEmptyFoldersHelper((DefaultMutableTreeNode)node.getChildAt(i));
			}
		}
	}

	protected AjaxLink getExpandCollapseLink(){
		//Expand Collapse Link:
		final Label expandCollapse = new Label("expandCollapse", new StringResourceModel("exapndNodes", null));
		expandCollapse.setOutputMarkupId(true);
		AjaxLink expandLink  = new AjaxLink("expandAll")
		{
			boolean expand = true;
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				if(expand){
					getTree().getTreeState().expandAll();
					expandCollapse.setDefaultModel(new StringResourceModel("collapseNodes", null));
					collapseEmptyFolders();
				}else{
					getTree().getTreeState().collapseAll();
					expandCollapse.setDefaultModel(new StringResourceModel("exapndNodes", null));
				}
				target.addComponent(expandCollapse);
				getTree().updateTree(target);
				expand = !expand;

			}
			@Override
			public boolean isVisible() {
				return getTree().getDefaultModelObject() != null;
			}
		};
		expandLink.add(expandCollapse);
		return expandLink;
	}
	
	/**
	 * This collapses all empty folders in the tree.  This helps the user know that they need to click on the folder
	 * in order to populate the children nodes.  (used when the structure is being populated on the fly w/ajax and the
	 * folders haven't been populated yet)
	 */
	protected void collapseEmptyFolders(){
		collapseEmptyFoldersHelper((DefaultMutableTreeNode) getTree().getModelObject().getRoot());
	}
}