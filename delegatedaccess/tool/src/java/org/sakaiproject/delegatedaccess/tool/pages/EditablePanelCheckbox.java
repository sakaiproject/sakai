package org.sakaiproject.delegatedaccess.tool.pages;

import javax.swing.tree.TreeNode;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.sakaiproject.delegatedaccess.model.NodeModel;

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
	public EditablePanelCheckbox(String id, IModel inputModel, NodeModel nodeModel, final TreeNode node)
	{
		super(id);

		this.nodeModel = nodeModel;
		this.node = node;

		CheckBox field = new CheckBox("checkboxField", inputModel);
		add(field);

		field.add(new AjaxFormComponentUpdatingBehavior("onClick")
		{
			protected void onUpdate(AjaxRequestTarget target)
			{
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
		});
	}
}
