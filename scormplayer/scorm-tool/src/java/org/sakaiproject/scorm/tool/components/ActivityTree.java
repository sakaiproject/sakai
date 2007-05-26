package org.sakaiproject.scorm.tool.components;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.adl.sequencer.ISeqActivity;
import org.sakaiproject.scorm.client.utils.ActivityLink;
import org.sakaiproject.scorm.client.utils.IActivityLinkCallback;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.tree.Tree;
import org.apache.wicket.model.IModel;

public class ActivityTree extends Tree {
	private static final long serialVersionUID = 1L;
	
	public ActivityTree(String id) {
		super(id);
	}
	
	public ActivityTree(String id, IModel model)
	{
		super(id, model);
	}
	
	public ActivityTree(String id, TreeModel model)
	{
		super(id, model);
	}
	

	@Override
	protected String renderNode(TreeNode node)
	{
		ISeqActivity item = (ISeqActivity)((DefaultMutableTreeNode)node).getUserObject();
		return item.getTitle();
	}	
	
	/*@Override
	protected void populateTreeItem(WebMarkupContainer item, int level)
	{
		final TreeNode node = (TreeNode)item.getModelObject();

		item.add(newIndentation(item, "indent", (TreeNode)item.getModelObject(), level));

		item.add(newJunctionLink(item, "link", "image", node));

		MarkupContainer nodeLink = new ActivityLink("nodeLink", "http://www.google.com", node);
			//newNodeLink(item, "nodeLink", node);
		item.add(nodeLink);

		nodeLink.add(newNodeIcon(nodeLink, "icon", node));

		nodeLink.add(new Label("label", new AbstractReadOnlyModel()
		{
			private static final long serialVersionUID = 1L;

			public Object getObject(Component c)
			{
				return renderNode(node);
			}
		}));

		// do distinguish between selected and unselected rows we add an
		// behavior
		// that modifies row css class.
		item.add(new AbstractBehavior()
		{
			private static final long serialVersionUID = 1L;

			public void onComponentTag(Component component, ComponentTag tag)
			{
				super.onComponentTag(component, tag);
				if (getTreeState().isNodeSelected(node))
				{
					tag.put("class", "row-selected");
				}
				else
				{
					tag.put("class", "row");
				}
			}
		});
	}*/
	
	/*@Override
	protected MarkupContainer newNodeLink(MarkupContainer parent, String id, final TreeNode node)
	{
		return new ActivityLink(id, "http://www.google.com", new IActivityLinkCallback() {
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target)
			{
				getTreeState().selectNode(node, !getTreeState().isNodeSelected(node));
				onNodeLinkClicked(target, node);
				updateTree(target);
			}
		});
	}*/

}
