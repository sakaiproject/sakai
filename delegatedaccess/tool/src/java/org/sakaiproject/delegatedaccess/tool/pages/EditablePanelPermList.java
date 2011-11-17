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
import org.sakaiproject.delegatedaccess.model.PermissionSerialized;

public class EditablePanelPermList extends Panel{

	private NodeModel nodeModel;
	private TreeNode node;
	
	
	public EditablePanelPermList(String id, IModel model, final NodeModel nodeModel, final TreeNode node) {
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


		AjaxLink<Void> restrictLink = new AjaxLink<Void>("restrictLink"){
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
		add(restrictLink);

		AjaxLink<Void> inheritedLink = new AjaxLink<Void>("inheritedLink"){
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
				return !nodeModel.isDirectAccess() && nodeModel.getInheritedSelectedPermissions() != null && !nodeModel.getInheritedSelectedPermissions().isEmpty();
			}
		};
		add(inheritedLink);

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


		ListView<PermissionSerialized> listView = new ListView<PermissionSerialized>("list", nodeModel.getShoppingPeriodPerms()) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<PermissionSerialized> item) {
				PermissionSerialized wrapper = item.getModelObject();
				item.add(new Label("name", wrapper.getPermissionId()));
				final CheckBox checkBox = new CheckBox("check", new PropertyModel(wrapper, "selected"));
				checkBox.setOutputMarkupId(true);
				final String permId = wrapper.getPermissionId();
				checkBox.add(new AjaxFormComponentUpdatingBehavior("onClick")
				{
					protected void onUpdate(AjaxRequestTarget target){
						nodeModel.setPermissionSelected(permId, isChecked());
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


		IModel<List<? extends PermissionSerialized>> inheritedPermsModel = new AbstractReadOnlyModel<List<? extends PermissionSerialized>>(){
			private static final long serialVersionUID = 1L;

			@Override
			public List<? extends PermissionSerialized> getObject() {
				return nodeModel.getInheritedSelectedPermissions();
			}

		};



		ListView<PermissionSerialized> inheritedListView = new ListView<PermissionSerialized>("inheritedPerms",inheritedPermsModel){
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<PermissionSerialized> item) {
				PermissionSerialized perm = (PermissionSerialized) item.getModelObject();
				Label name = new Label("name", perm.getPermissionId());
				item.add(name);
			}

			@Override
			public boolean isVisible() {
				return nodeModel.getInheritedSelectedPermissions() != null && !nodeModel.getInheritedSelectedPermissions().isEmpty();
			}
		};
		inheritedSpan.add(inheritedListView);

		Label noInheritedLabel = new Label("noPermsInherited", new StringResourceModel("noPermsInherited", null)){
			public boolean isVisible() {
				return nodeModel.getInheritedSelectedPermissions() == null || nodeModel.getInheritedSelectedPermissions().isEmpty();
			};
		};
		inheritedSpan.add(noInheritedLabel);
		
		
	}

}
