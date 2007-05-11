package org.sakaiproject.scorm.tool.pages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.adl.sequencer.ADLLaunch;
import org.adl.sequencer.ADLSequencer;
import org.adl.sequencer.ADLTOC;
import org.adl.sequencer.ADLValidRequests;
import org.adl.sequencer.SeqActivity;
import org.adl.sequencer.SeqActivityTree;
import org.adl.sequencer.SeqNavRequests;
import org.sakaiproject.scorm.client.pages.BlankPage;
import org.sakaiproject.scorm.client.pages.Index;
import org.sakaiproject.scorm.tool.ScormTool;
import org.sakaiproject.scorm.tool.pages.LaunchFrameset;
import org.sakaiproject.scorm.tool.panels.ActivityTree;
import org.sakaiproject.sequencing.api.Sequencer;

import wicket.AttributeModifier;
import wicket.Component;
import wicket.IRequestTarget;
import wicket.MarkupContainer;
import wicket.PageMap;
import wicket.PageParameters;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.markup.html.AjaxLink;
import wicket.behavior.AbstractBehavior;
import wicket.extensions.markup.html.tree.AbstractTree;
import wicket.extensions.markup.html.tree.DefaultTreeState;
import wicket.extensions.markup.html.tree.ITreeState;
import wicket.extensions.markup.html.tree.Tree;
import wicket.markup.ComponentTag;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.ExternalLink;
import wicket.markup.html.link.Link;
import wicket.model.AbstractReadOnlyModel;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.request.target.basic.RedirectRequestTarget;

public class NavigationFrame extends WebPage {

	private static final long serialVersionUID = 1L;

	private Tree tree;
	
	protected AbstractTree getTree()
	{
		return tree;
	}
	
	/**
	 * Link that, when clicked, changes the frame target's frame class (and as
	 * that is a shared model which is also being used by the 'master page'
	 * {@link BodyFrame}, changes are immediately reflected) and set the
	 * response page to the top level page {@link BodyFrame}. Tags that use
	 * this link should have a <code>target="_parent"</code> attribute, so
	 * that the top frame will be refreshed.
	 */
	/*private static final class ChangeFramePageLink extends Link
	{
		private static final long serialVersionUID = 1L;

		private final LaunchFrameset frameset;

		private final Class pageClass;


		public ChangeFramePageLink(String id, LaunchFrameset frameset, Class pageClass)
		{
			super(id);
			this.frameset = frameset;
			this.pageClass = pageClass;
		}

		public void onClick()
		{
			// change frame class
			frameset.getFrameTarget().setFrameClass(pageClass);

			// trigger re-rendering of the page
			setResponsePage(frameset);
		}
	}*/

	public NavigationFrame() {
		super();
	}
	
	public NavigationFrame(final LaunchFrameset frameset) {
		super();
	
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

		TreeModel treeModel = createTreeModel();
		tree = new ActivityTree("tree", treeModel)
		{
			protected void onNodeLinkClicked(AjaxRequestTarget target, TreeNode node)
			{
				super.onNodeLinkClicked(target, node);
				
				SeqActivity item = (SeqActivity)((DefaultMutableTreeNode)node).getUserObject();
				
				System.out.println("RESOURCE: " + item.getResourceID());					
			}
		};
		add(tree);
		
		ITreeState treeState = tree.getTreeState();	
		
		TreeNode rootNode = (TreeNode)treeModel.getRoot();
		
		treeState.expandNode(rootNode);
		
		TreeNode selected = findSelectedNode(treeModel, rootNode);
		if (null != selected)
			treeState.selectNode(selected, true);
	}
	
	private TreeNode findSelectedNode(TreeModel treeModel, TreeNode node) {
		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)node;
		SeqActivity item = (SeqActivity)treeNode.getUserObject();
		
		// This is our exit out of the recursive search
		if (item.getIsSelected())
			return node;
		
		for (int i=0;i<treeNode.getChildCount();i++) {
			TreeNode child = treeNode.getChildAt(i);
			TreeNode selected = findSelectedNode(treeModel, child);
			if (null != selected)
				return selected;
		}
			
		// This is in case none of the nodes are selected
		return null;
	}
	
	
	
	/**
	 * Constructor
	 * 
	 * @param index
	 *            parent frame class
	 */
	/*public NavigationFrame(LaunchFrameset index)
	{
		//add(new ChangeFramePageLink("linkToPage1", index, ContentFrame.class));
		//add(new ChangeFramePageLink("linkToPage2", index, Page2.class));
	}*/

	/**
	 * No need for versioning this frame.
	 * 
	 * @see wicket.Component#isVersioned()
	 */
	/*public boolean isVersioned()
	{
		return false;
	}*/
	
	protected TreeModel createTreeModel() {
		ADLSequencer sequencer = ((ScormTool)getApplication()).getClientFacade().getSequencer();
		
		ADLValidRequests validRequests = new ADLValidRequests();
        
		
		ADLLaunch launchInfo = sequencer.navigate(SeqNavRequests.NAV_START); 

		validRequests = launchInfo.mNavState;
		
		return validRequests.mTreeModel;
	}
	
	
}
