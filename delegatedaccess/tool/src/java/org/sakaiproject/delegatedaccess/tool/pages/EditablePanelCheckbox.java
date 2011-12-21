package org.sakaiproject.delegatedaccess.tool.pages;

import javax.swing.tree.TreeNode;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.Strings;
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;

/**
 * This is the panel that holds the checkbox for the TreeTable's access column
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
public class EditablePanelCheckbox extends Panel
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

	/**
	 * Creates a simple checkbox panel for TreeTable's access column.
	 * @param id
	 * @param inputModel
	 * @param nodeModel
	 * @param node
	 */
	public EditablePanelCheckbox(String id, IModel inputModel, final NodeModel nodeModel, final TreeNode node, final int type)
	{
		super(id);

		this.nodeModel = nodeModel;
		this.node = node;

		final CheckBox field = new CheckBox("checkboxField", inputModel){
			@Override
			public boolean isVisible() {
				if(DelegatedAccessConstants.TYPE_SHOPPING_PERIOD_ADMIN == type){
					return !nodeModel.getInheritedShoppingPeriodAdmin();
				}else if(DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER == type){
					return nodeModel.getNodeShoppingPeriodAdmin();
				}else{
					return true;
				}
			}

		};
		add(field);

		field.add(new AjaxFormComponentUpdatingBehavior("onClick")
		{
			protected void onUpdate(AjaxRequestTarget target)
			{
				//toggle selection to trigger a reload on the current node 
				((BaseTreePage)target.getPage()).getTree().getTreeState().selectNode(node, !((BaseTreePage)target.getPage()).getTree().getTreeState().isNodeSelected(node));
				
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
	}
}
