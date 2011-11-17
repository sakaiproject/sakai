package org.sakaiproject.delegatedaccess.tool.pages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.tree.TreeNode;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.sakaiproject.delegatedaccess.model.NodeModel;

/**
 * Creates the dropdown panel for the "Role" column in TreeTable
 *  
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
public class EditablePanelDropdown extends Panel
{

	private NodeModel nodeModel;
	private TreeNode node;

	public EditablePanelDropdown(String id, IModel inputModel, final NodeModel nodeModel, final TreeNode node, final Map<String, List<String>> realmMap)
	{
		super(id);

		this.nodeModel = nodeModel;
		this.node = node;

		//Create two simple models that will update the list options based on realmMap and the user's choice of realm
		IModel<List<? extends String>> realmChoicesModel = new AbstractReadOnlyModel<List<? extends String>>(){
			private static final long serialVersionUID = 1L;

			@Override
			public List<? extends String> getObject() {
				return new ArrayList<String>(realmMap.keySet());
			}

		};

		IModel<List<? extends String>> roleChoicesModel = new AbstractReadOnlyModel<List<? extends String>>(){
			private static final long serialVersionUID = 1L;

			@Override
			public List<? extends String> getObject() {
				List<String> roles = realmMap.get(nodeModel.getRealm());
				if(roles == null){
					roles = Collections.emptyList();
				}
				return roles;
			}

		};

		final DropDownChoice<String> roleChoices = new DropDownChoice<String>("roleChoices", new PropertyModel<String>(nodeModel, "role"), roleChoicesModel){
			@Override
			public boolean isVisible() {
				return nodeModel.isDirectAccess();
			}
		};
		roleChoices.setOutputMarkupId(true);
		roleChoices.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
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
		});
		add(roleChoices);


		final DropDownChoice realmChoices = new DropDownChoice("realmChoices", new PropertyModel<String>(nodeModel, "realm"), realmChoicesModel){
			@Override
			public boolean isVisible() {
				return nodeModel.isDirectAccess();
			}
		};
		realmChoices.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				//clear role choice since realm has changed
				nodeModel.setRole("");
				//re-add component in order for the list to re-populate
				target.addComponent(roleChoices);
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
		});
		add(realmChoices);

		//show the inherited role if the user hasn't selected this node
		IModel<String> labelModel = new AbstractReadOnlyModel<String>() {
			@Override
			public String getObject() {
				String[] inheritedAccess = nodeModel.getInheritedAccessRealmRole();
				if("".equals(inheritedAccess[0])){
					return "";
				}else{
					return inheritedAccess[0] + " : " + inheritedAccess[1];
				}
			}
		};
		Label label = new Label("realmRole", labelModel){
			public boolean isVisible() {
				return !nodeModel.isDirectAccess();
			};
		};
		add(label);

	}




}
