package org.sakaiproject.scorm.client.pages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.markup.html.AjaxLink;
import wicket.extensions.markup.html.tree.AbstractTree;
import wicket.extensions.markup.html.tree.Tree;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.WebPage;
import wicket.markup.html.link.ExternalLink;
import wicket.markup.html.link.Link;

/**
 * Page that shows a simple tree (not a table).
 *  
 * @author Matej
 *
 */
public class SimpleTreePage extends WebPage
{
	private Tree tree;
	private ContentPanel contentPanel;

	protected AbstractTree getTree()
	{
		return tree;
	}
	
	/**
	 * Page constructor
	 *
	 */
	public SimpleTreePage()
	{
		add(new AjaxLink("expandAll") 
		{
			public void onClick(AjaxRequestTarget target)
			{
				getTree().getTreeState().expandAll();
				getTree().updateTree(target);
			}
		});
		
		add(new AjaxLink("collapseAll") 
		{
			public void onClick(AjaxRequestTarget target)
			{
				getTree().getTreeState().collapseAll();
				getTree().updateTree(target);
			}
		});
		
		add(new AjaxLink("switchRootless") 
		{
			public void onClick(AjaxRequestTarget target)
			{
				getTree().setRootLess(!getTree().isRootLess());
				getTree().updateTree(target);
			}
		});
		
		contentPanel = new ContentPanel("contentpanel", "initial message");
		tree = new Tree("tree", createTreeModel()) 
		{
			protected void onNodeLinkClicked(AjaxRequestTarget target, TreeNode node)
			{
				super.onNodeLinkClicked(target, node);
				contentPanel.updateMessage(target, renderNode(node));
				//target.addComponent(contentPanel);
				//target.respond(this.getRequestCycle());
			}
			
			protected String renderNode(TreeNode node)
			{
				return (String)((DefaultMutableTreeNode)node).getUserObject();
				
				/*if (null == node)
					return "Null node";
				ModelBean bean = (ModelBean) ((DefaultMutableTreeNode)node).getUserObject();
				if (null == bean)
					return "Null bean";
				return bean.getProperty1();*/
			}
		};
		add(tree);		
		
		add(contentPanel);
		tree.getTreeState().collapseAll();
	}

	protected TreeModel createTreeModel() 
	{
		List l1 = new ArrayList();
		l1.add("test 1.1");
		l1.add("test 1.2");
		l1.add("test 1.3");
		List l2 = new ArrayList();
		l2.add("test 2.1");
		l2.add("test 2.2");
		l2.add("test 2.3");
		List l3 = new ArrayList();
		l3.add("test 3.1");
		l3.add("test 3.2");
		l3.add("test 3.3");
				
		l2.add(l3);
		
		l2.add("test 2.4");
		l2.add("test 2.5");
		l2.add("test 2.6");
		
		l3 = new ArrayList();
		l3.add("test 3.1");
		l3.add("test 3.2");
		l3.add("test 3.3");
		l2.add(l3);
		
		l1.add(l2);

		l2 = new ArrayList();
		l2.add("test 2.1");
		l2.add("test 2.2");
		l2.add("test 2.3");

		l1.add(l2);		
		
		l1.add("test 1.3");
		l1.add("test 1.4");
		l1.add("test 1.5");
		
		return convertToTreeModel(l1);
	}
	
	private TreeModel convertToTreeModel(List list)
	{
		TreeModel model = null;
		String root = new String("ROOT");
		MutableTreeNode rootNode = new DefaultMutableTreeNode(root);
		add(rootNode, list);
		model = new DefaultTreeModel(rootNode);	
		return model;
	}

	private void add(MutableTreeNode parent, List sub)
	{
		for (Iterator i = sub.iterator(); i.hasNext();)
		{
			Object o = i.next();
			if (o instanceof List)
			{
				MutableTreeNode child = new DefaultMutableTreeNode("subtree");	//new ModelBean("subtree..."));
				((DefaultMutableTreeNode)parent).add(child);
				add(child, (List)o);
			}
			else
			{
				MutableTreeNode child = new DefaultMutableTreeNode("leaf");	//new ModelBean(o.toString()));
				((DefaultMutableTreeNode)parent).add(child);
			}
		}
	}
	
}
