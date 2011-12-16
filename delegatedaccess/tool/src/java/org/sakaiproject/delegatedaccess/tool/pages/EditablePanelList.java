package org.sakaiproject.delegatedaccess.tool.pages;

import java.util.List;

import javax.swing.tree.TreeNode;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.string.Strings;
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.model.ListOptionSerialized;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;

/**
 * 
 * This is the panel (table cell) for the restricted tools column
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */

public class EditablePanelList  extends Panel
{

	private NodeModel nodeModel;
	private TreeNode node;

	public EditablePanelList(String id, IModel inputModel, final NodeModel nodeModel, final TreeNode node, final int userType, final int fieldType)
	{
		super(id);

		this.nodeModel = nodeModel;
		this.node = node;

		WebMarkupContainer editableSpan = new WebMarkupContainer("editableSpan");
		editableSpan.setOutputMarkupId(true);
		final String editableSpanId = editableSpan.getMarkupId();
		add(editableSpan);

		WebMarkupContainer inheritedSpan = new WebMarkupContainer("inheritedSpan");
		inheritedSpan.setOutputMarkupId(true);
		final String inheritedSpanId = inheritedSpan.getMarkupId();
		add(inheritedSpan);


		AjaxLink<Void> restrictToolsLink = new AjaxLink<Void>("restrictToolsLink"){
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				target.appendJavascript("document.getElementById('" + editableSpanId + "').style.display='';");
			}
			@Override
			public boolean isVisible() {
				if(DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER == userType){
					return nodeModel.isDirectAccess() && nodeModel.getNodeShoppingPeriodAdmin();
				}else{
					return nodeModel.isDirectAccess();
				}
			}
		};
		add(restrictToolsLink);
		
		Label restrictToolsLinkLabel = new Label("restrictToolsSpan");
		if(DelegatedAccessConstants.TYPE_LISTFIELD_TERMS == fieldType){
			restrictToolsLinkLabel.setDefaultModel(new StringResourceModel("termHeader", null));
		}else{
			if(DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER == userType){
				restrictToolsLinkLabel.setDefaultModel(new StringResourceModel("showToolsHeader", null));
			}else{
				restrictToolsLinkLabel.setDefaultModel(new StringResourceModel("restrictedToolsHeader", null));
			}
		}
		restrictToolsLink.add(restrictToolsLinkLabel);
		

		AjaxLink<Void> inheritedToolsLink = new AjaxLink<Void>("inheritedToolsLink"){
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				target.appendJavascript("document.getElementById('" + inheritedSpanId + "').style.display='';");
			}
			@Override
			public boolean isVisible() {
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
					return (!nodeModel.isDirectAccess() && inheritedOptions != null && !inheritedOptions.isEmpty())
					|| (!nodeModel.getNodeShoppingPeriodAdmin() && nodeOptions.length > 0);
				}else{
					return !nodeModel.isDirectAccess() && inheritedOptions != null && !inheritedOptions.isEmpty();
				}
			}
		};
		
		add(inheritedToolsLink);
		

		AjaxLink<Void> switchEditableSpanLink = new AjaxLink<Void>("switchEditableSpanLink") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				target.appendJavascript("document.getElementById('" + editableSpanId + "').style.display='none';");
				target.appendJavascript("document.getElementById('" + inheritedSpanId + "').style.display='';");
			}
		};
		editableSpan.add(switchEditableSpanLink);
		Label switchEditableSpanLinkTitle = new Label("switchEditableSpanLinkTitle");
		if(fieldType == DelegatedAccessConstants.TYPE_LISTFIELD_TERMS){
			switchEditableSpanLinkTitle.setDefaultModel(new StringResourceModel("switchEditableTerms", null));
		}else{
			switchEditableSpanLinkTitle.setDefaultModel(new StringResourceModel("switchEditableTools", null));
		}
		switchEditableSpanLink.add(switchEditableSpanLinkTitle);
		

		AjaxLink<Void> saveEditableSpanLink = new AjaxLink<Void>("saveEditableSpanLink") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				target.appendJavascript("document.getElementById('" + editableSpanId + "').style.display='none';");
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
		};
		editableSpan.add(saveEditableSpanLink);

		Label editableSpanLabel = new Label("editableNodeTitle", nodeModel.getNode().title);
		editableSpan.add(editableSpanLabel);

		AjaxLink<Void> switchInheritedSpanLink = new AjaxLink<Void>("switchInheritedSpanLink") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				target.appendJavascript("document.getElementById('" + inheritedSpanId + "').style.display='none';");
				target.appendJavascript("document.getElementById('" + editableSpanId + "').style.display='';");
			}
			@Override
			public boolean isVisible() {
				if(DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER == userType){
					return nodeModel.isDirectAccess() && nodeModel.getNodeShoppingPeriodAdmin();
				}else{
					return nodeModel.isDirectAccess();
				}
			}
		};
		inheritedSpan.add(switchInheritedSpanLink);
		
		Label restrictToolsLinkSpan = new Label("switchInheritedSpan");
		if(DelegatedAccessConstants.TYPE_LISTFIELD_TERMS == fieldType){
			restrictToolsLinkSpan.setDefaultModel(new StringResourceModel("termHeader", null));
		}else{
			if(DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER == userType){
				restrictToolsLinkSpan.setDefaultModel(new StringResourceModel("showToolsHeader", null));
			}else{
				restrictToolsLinkSpan.setDefaultModel(new StringResourceModel("restrictedToolsHeader", null));
			}
		}
		switchInheritedSpanLink.add(restrictToolsLinkSpan);

		Label inheritedMenuSpan = new Label("inheritedMenuSpan", " | "){
			@Override
			public boolean isVisible() {
				if(DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER == userType){
					return nodeModel.isDirectAccess() && nodeModel.getNodeShoppingPeriodAdmin();
				}else{
					return nodeModel.isDirectAccess();
				}
			}
		};
		inheritedSpan.add(inheritedMenuSpan);

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

		List<ListOptionSerialized> listOptions = null;
		if(DelegatedAccessConstants.TYPE_LISTFIELD_TERMS == fieldType){
			listOptions = nodeModel.getTerms();
		}else{
			listOptions = nodeModel.getRestrictedTools();
		}
		ListView<ListOptionSerialized> listView = new ListView<ListOptionSerialized>("list", listOptions) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<ListOptionSerialized> item) {
				ListOptionSerialized wrapper = item.getModelObject();
				item.add(new Label("name", wrapper.getName()));
				final CheckBox checkBox = new CheckBox("check", new PropertyModel(wrapper, "selected"));
				checkBox.setOutputMarkupId(true);
				final String toolId = wrapper.getId();
				checkBox.add(new AjaxFormComponentUpdatingBehavior("onClick")
				{
					protected void onUpdate(AjaxRequestTarget target){
						if(DelegatedAccessConstants.TYPE_LISTFIELD_TERMS == fieldType){
							nodeModel.setTerm(toolId, isChecked());
						}else{
							nodeModel.setToolRestricted(toolId, isChecked());
						}
					}

					private boolean isChecked(){
						final String value = checkBox.getValue();
						if (value != null)
						{
							try
							{
								return Strings.isTrue(value);
							}
							catch (Exception e)
							{
								return false;
							}
						}
						return false;
					}
				});
				item.add(checkBox);

			}
		};
		editableSpan.add(listView);


		IModel<List<? extends ListOptionSerialized>> inheritedRestrictedToolsModel = new AbstractReadOnlyModel<List<? extends ListOptionSerialized>>(){
			private static final long serialVersionUID = 1L;

			@Override
			public List<? extends ListOptionSerialized> getObject() {
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
			}

		};



		ListView<ListOptionSerialized> inheritedListView = new ListView<ListOptionSerialized>("inheritedRestrictedTools",inheritedRestrictedToolsModel){
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<ListOptionSerialized> item) {
				ListOptionSerialized tool = (ListOptionSerialized) item.getModelObject();
				Label name = new Label("name", tool.getName());
				item.add(name);
			}

			@Override
			public boolean isVisible() {
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
			}
		};
		inheritedSpan.add(inheritedListView);

		Label noInheritedToolsLabel = new Label("noToolsInherited", new StringResourceModel("inheritedNothing", null)){
			public boolean isVisible() {
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

			};
		};
		inheritedSpan.add(noInheritedToolsLabel);
		
		Label editToolsTitle = new Label("editToolsTitle");
		if(DelegatedAccessConstants.TYPE_LISTFIELD_TERMS == fieldType){
			editToolsTitle.setDefaultModel(new StringResourceModel("editableTermsTitle", null));
		}else{
			if(DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER == userType){
				editToolsTitle.setDefaultModel(new StringResourceModel("editableShowToolsTitle", null));
			}else{
				editToolsTitle.setDefaultModel(new StringResourceModel("editableRestrictedToolsTitle", null));
			}
		}
		editableSpan.add(editToolsTitle);
		
		Label editToolsInstructions = new Label("editToolsInstructions");
		if(DelegatedAccessConstants.TYPE_LISTFIELD_TERMS == fieldType){
			editToolsInstructions.setDefaultModel(new StringResourceModel("editableTermsDescription", null));
		}else{
			if(DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER == userType){
				editToolsInstructions.setDefaultModel(new StringResourceModel("editableShowToolsDescription", null));
			}else{
				editToolsInstructions.setDefaultModel(new StringResourceModel("editableRestrictedToolsDescription", null));
			}
		}
		editableSpan.add(editToolsInstructions);

	}

}
