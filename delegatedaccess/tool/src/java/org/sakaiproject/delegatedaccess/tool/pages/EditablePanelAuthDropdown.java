package org.sakaiproject.delegatedaccess.tool.pages;

import java.util.Arrays;

import javax.swing.tree.TreeNode;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.sakaiproject.delegatedaccess.model.NodeModel;

/**
 * This is the .auth or .anon dropdown option for shopping period admin
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
public class EditablePanelAuthDropdown extends Panel{

	public EditablePanelAuthDropdown(String id, IModel model, final NodeModel nodeModel, final TreeNode node) {
		super(id, model);

		final DropDownChoice choice=new DropDownChoice("dropDownChoice", model, Arrays.asList(".auth", ".anon")){
			@Override
			public boolean isVisible() {
				return nodeModel.isDirectAccess() && nodeModel.getNodeShoppingPeriodAdmin();
			}
		};
		choice.add(new AjaxFormComponentUpdatingBehavior("onchange")
		{
			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				nodeModel.setShoppingPeriodAuth(choice.getModelObject().toString());

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
		add(choice);

		IModel<String> labelModel = new AbstractReadOnlyModel<String>() {
			@Override
			public String getObject() {
				if(nodeModel.isDirectAccess()){
					return nodeModel.getNodeShoppingPeriodAuth();
				}else{
					return nodeModel.getInheritedShoppingPeriodAuth();
				}
			}
		};
		add(new Label("inherited", labelModel){
			public boolean isVisible() {
				return !nodeModel.isDirectAccess() || !nodeModel.getNodeShoppingPeriodAdmin();
			};
		});
	}

}
