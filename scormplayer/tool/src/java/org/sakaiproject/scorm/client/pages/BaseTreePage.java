package org.sakaiproject.scorm.client.pages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.markup.html.AjaxLink;
import wicket.extensions.markup.html.tree.AbstractTree;
import wicket.markup.html.WebPage;

/**
 * This is a base class for all pages with tree example.
 *  
 * @author Matej Knopp
 */
public abstract class BaseTreePage extends WebPage
{

	/**
	 * Default constructor
	 */
	public BaseTreePage()
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
	}

	/**
	 * Returns the tree on this pages. This is used to collapse, expand the tree 
	 * and to switch the rootless mode.
	 * 
	 * @return
	 * 		Tree instance on this page
	 */
	protected abstract AbstractTree getTree();
	
	/**
	 * Creates the model that feeds the tree.
	 * @return
	 * 		New instance of tree model.
	 */
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
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new ModelBean("ROOT"));
		add(rootNode, list);
		model = new DefaultTreeModel(rootNode);		
		return model;
	}

	private void add(DefaultMutableTreeNode parent, List sub)
	{
		for (Iterator i = sub.iterator(); i.hasNext();)
		{
			Object o = i.next();
			if (o instanceof List)
			{
				DefaultMutableTreeNode child = new DefaultMutableTreeNode(new ModelBean("subtree..."));
				parent.add(child);
				add(child, (List)o);
			}
			else
			{
				DefaultMutableTreeNode child = new DefaultMutableTreeNode(new ModelBean(o.toString()));
				parent.add(child);
			}
		}
	}
	
}