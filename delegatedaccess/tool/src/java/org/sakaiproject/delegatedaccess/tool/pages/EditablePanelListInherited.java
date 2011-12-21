package org.sakaiproject.delegatedaccess.tool.pages;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.TreeNode;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.delegatedaccess.model.ListOptionSerialized;
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;

public class EditablePanelListInherited extends Panel{
	private boolean loadedFlag = false;

	public EditablePanelListInherited(String id, IModel inputModel, final NodeModel nodeModel, final TreeNode node, final int userType, final int fieldType){
		super(id);
		final WebMarkupContainer inheritedSpan = new WebMarkupContainer("inheritedSpan");
		inheritedSpan.setOutputMarkupId(true);
		final String inheritedSpanId = inheritedSpan.getMarkupId();
		add(inheritedSpan);
		
		final IModel<List<? extends ListOptionSerialized>> inheritedRestrictedToolsModel = new AbstractReadOnlyModel<List<? extends ListOptionSerialized>>(){
			private static final long serialVersionUID = 1L;

			@Override
			public List<? extends ListOptionSerialized> getObject() {
				if(loadedFlag){
					List<ListOptionSerialized> selectedOptions = null;
					List<ListOptionSerialized> inheritedOptions = null;
					if(DelegatedAccessConstants.TYPE_LISTFIELD_TERMS == fieldType){
						selectedOptions = nodeModel.getSelectedTerms();
						inheritedOptions = nodeModel.getInheritedTerms();
					}else{
						selectedOptions = nodeModel.getSelectedRestrictedTools();
						inheritedOptions = nodeModel.getInheritedRestrictedTools();
					}

					if(DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER == userType && !nodeModel.getNodeShoppingPeriodAdmin()){
						List<ListOptionSerialized> returnList =selectedOptions;
						if(returnList.isEmpty()){
							returnList = inheritedOptions;
						}
						return returnList;
					}else{
						return inheritedOptions;
					}
				}else{
					return new ArrayList<ListOptionSerialized>();
				}
			}

		};
		
		final ListView<ListOptionSerialized> inheritedListView = new ListView<ListOptionSerialized>("inheritedRestrictedTools",inheritedRestrictedToolsModel){
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<ListOptionSerialized> item) {
				ListOptionSerialized tool = (ListOptionSerialized) item.getModelObject();
				Label name = new Label("name", tool.getName());
				item.add(name);
			}

			@Override
			public boolean isVisible() {
				if(loadedFlag){
					List<ListOptionSerialized> inheritedOptions = null;
					String[] nodeOptions = null;
					if(DelegatedAccessConstants.TYPE_LISTFIELD_TERMS == fieldType){
						inheritedOptions = nodeModel.getInheritedTerms();
						nodeOptions = nodeModel.getNodeTerms();
					}else{
						inheritedOptions = nodeModel.getInheritedRestrictedTools();
						nodeOptions = nodeModel.getNodeRestrictedTools();
					}
					if(DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER == userType){
						return (inheritedOptions != null && !inheritedOptions.isEmpty())
						|| (!nodeModel.getNodeShoppingPeriodAdmin() && nodeOptions.length > 0);
					}else{
						return inheritedOptions != null && !inheritedOptions.isEmpty();
					}
				}else{
					return false;
				}
			}
		};
		inheritedListView.setOutputMarkupId(true);
		inheritedSpan.add(inheritedListView);
		
		AjaxLink<Void> inheritedToolsLink = new AjaxLink<Void>("inheritedToolsLink"){
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				if(!loadedFlag){
					loadedFlag = true;
					inheritedListView.setDefaultModel(inheritedRestrictedToolsModel);
					target.addComponent(inheritedSpan);
				}
				target.appendJavascript("document.getElementById('" + inheritedSpanId + "').style.display='';");
			}
		};
		
		add(inheritedToolsLink);
		
		AjaxLink<Void> closeInheritedSpanLink = new AjaxLink<Void>("closeInheritedSpanLink") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				target.appendJavascript("document.getElementById('" + inheritedSpanId + "').style.display='none';");
				if(nodeModel.isDirectAccess()){
					//In order for the models to refresh, you have to call "expand" or "collapse" then "updateTree",
					//since I don't want to expand or collapse, I just call whichever one the node is already
					//Refreshing the tree will update all the models and information (like role) will be generated onClick
					if(((BaseTreePage)target.getPage()).getTree().getTreeState().isNodeExpanded(node)){
						((BaseTreePage)target.getPage()).getTree().getTreeState().expandNode(node);
					}else{
						((BaseTreePage)target.getPage()).getTree().getTreeState().collapseNode(node);
					}
					((BaseTreePage)target.getPage()).getTree().updateTree(target);
				}
			}
		};
		inheritedSpan.add(closeInheritedSpanLink);

		Label inheritedNodeTitle = new Label("inheritedNodeTitle", nodeModel.getNode().title);
		inheritedSpan.add(inheritedNodeTitle);
		
		
		
		Label noInheritedToolsLabel = new Label("noToolsInherited", new StringResourceModel("inheritedNothing", null)){
			public boolean isVisible() {
				if(loadedFlag){
					List<ListOptionSerialized> inheritedOptions = null;
					String[] nodeOptions = null;
					if(DelegatedAccessConstants.TYPE_LISTFIELD_TERMS == fieldType){
						inheritedOptions = nodeModel.getInheritedTerms();
						nodeOptions = nodeModel.getNodeTerms();
					}else{
						inheritedOptions = nodeModel.getInheritedRestrictedTools();
						nodeOptions = nodeModel.getNodeRestrictedTools();
					}
					if(DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER == userType){
						return (nodeModel.getNodeShoppingPeriodAdmin() && (inheritedOptions == null || inheritedOptions.isEmpty()))
						|| (!nodeModel.getNodeShoppingPeriodAdmin() && nodeOptions.length == 0);
					}else{
						return inheritedOptions == null || inheritedOptions.isEmpty();
					}
				}else{
					return false;
				}
			};
		};
		inheritedSpan.add(noInheritedToolsLabel);
	}

	public boolean isLoadedFlag() {
		return loadedFlag;
	}
}
