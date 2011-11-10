package org.sakaiproject.delegatedaccess.tool.pages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.sakaiproject.delegatedaccess.model.ToolSerialized;

public class EditablePanelList  extends Panel
{

	/**
	 * Panel constructor.
	 * 
	 * @param id
	 *            Markup id
	 * 
	 * @param inputModel
	 *            Model of the text field
	 */
	private NodeModel nodeModel;
	private TreeNode node;
	private List<ToolSerialized> localToolList = null;
	/**
	 * Creates a simple checkbox panel for TreeTable's access column.
	 * @param id
	 * @param inputModel
	 * @param nodeModel
	 * @param node
	 */
	public EditablePanelList(String id, IModel inputModel, final NodeModel nodeModel, final TreeNode node)
	{
		super(id);

		this.nodeModel = nodeModel;
		this.node = node;
		
		if(localToolList == null){
			localToolList = nodeModel.getRestrictedTools();
		}
		
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
				if(nodeModel.isDirectAccess()){
					target.appendJavascript("document.getElementById('" + editableSpanId + "').style.display='';");
				}else{
					target.appendJavascript("document.getElementById('" + inheritedSpanId + "').style.display='';");
				}
			}
			@Override
			public boolean isVisible() {
				return nodeModel.isDirectAccess();
			}
		};
		add(restrictToolsLink);
		
		AjaxLink<Void> inheritedToolsLink = new AjaxLink<Void>("inheritedToolsLink"){
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				if(nodeModel.isDirectAccess()){
					target.appendJavascript("document.getElementById('" + editableSpanId + "').style.display='';");
				}else{
					target.appendJavascript("document.getElementById('" + inheritedSpanId + "').style.display='';");
				}
			}
			@Override
			public boolean isVisible() {
				return !nodeModel.isDirectAccess() && nodeModel.getInheritedRestrictedTools() != null && !nodeModel.getInheritedRestrictedTools().isEmpty();
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
		
		AjaxLink<Void> saveEditableSpanLink = new AjaxLink<Void>("saveEditableSpanLink") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				target.appendJavascript("document.getElementById('" + editableSpanId + "').style.display='none';");
				//In order for the models to refresh, you have to call "expand" or "collapse" then "updateTree",
				//since I don't want to expand or collapse, I just call whichever one the node is already
				//Refreshing the tree will update all the models and information (like role) will be generated onClick
				if(((UserEditPage)target.getPage()).getTree().getTreeState().isNodeExpanded(node)){
					((UserEditPage)target.getPage()).getTree().getTreeState().expandNode(node);
				}else{
					((UserEditPage)target.getPage()).getTree().getTreeState().collapseNode(node);
				}
				((UserEditPage)target.getPage()).getTree().updateTree(target);
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
				return nodeModel.isDirectAccess();
			}
		};
		inheritedSpan.add(switchInheritedSpanLink);
		
		Label inheritedMenuSpan = new Label("inheritedMenuSpan", " | "){
			@Override
			public boolean isVisible() {
				return nodeModel.isDirectAccess();
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
					if(((UserEditPage)target.getPage()).getTree().getTreeState().isNodeExpanded(node)){
						((UserEditPage)target.getPage()).getTree().getTreeState().expandNode(node);
					}else{
						((UserEditPage)target.getPage()).getTree().getTreeState().collapseNode(node);
					}
					((UserEditPage)target.getPage()).getTree().updateTree(target);
				}
			}
		};
		inheritedSpan.add(closeInheritedSpanLink);
		
		Label inheritedNodeTitle = new Label("inheritedNodeTitle", nodeModel.getNode().title);
		inheritedSpan.add(inheritedNodeTitle);
		
		
		ListView<ToolSerialized> listView = new ListView<ToolSerialized>("list", nodeModel.getRestrictedTools()) {

			private static final long serialVersionUID = 1L;
			
			@Override
			protected void populateItem(ListItem<ToolSerialized> item) {
				ToolSerialized wrapper = item.getModelObject();
				item.add(new Label("name", wrapper.getToolName()));
				final CheckBox checkBox = new CheckBox("check", new PropertyModel(wrapper, "selected"));
				checkBox.setOutputMarkupId(true);
				final String toolId = wrapper.getToolId();
				checkBox.add(new AjaxFormComponentUpdatingBehavior("onClick")
				{
					protected void onUpdate(AjaxRequestTarget target){
						nodeModel.setToolRestricted(toolId, isChecked());
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
		

		IModel<List<? extends ToolSerialized>> inheritedRestrictedToolsModel = new AbstractReadOnlyModel<List<? extends ToolSerialized>>(){
			private static final long serialVersionUID = 1L;

			@Override
			public List<? extends ToolSerialized> getObject() {
				return nodeModel.getInheritedRestrictedTools();
			}

		};
		
		
		
		ListView<ToolSerialized> inheritedListView = new ListView<ToolSerialized>("inheritedRestrictedTools",inheritedRestrictedToolsModel){
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<ToolSerialized> item) {
				ToolSerialized tool = (ToolSerialized) item.getModelObject();
				Label name = new Label("name", tool.getToolName());
				item.add(name);
			}
			
			@Override
			public boolean isVisible() {
				return nodeModel.getInheritedRestrictedTools() != null && !nodeModel.getInheritedRestrictedTools().isEmpty();
			}
		};
		inheritedSpan.add(inheritedListView);
		
		Label noInheritedToolsLabel = new Label("noToolsInherited", new StringResourceModel("noToolsInherited", null)){
			public boolean isVisible() {
				return nodeModel.getInheritedRestrictedTools() == null || nodeModel.getInheritedRestrictedTools().isEmpty();
			};
		};
		inheritedSpan.add(noInheritedToolsLabel);
		
	}
	
}
