package org.sakaiproject.scorm.client.pages;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.adl.sequencer.ADLLaunch;
import org.adl.sequencer.ADLSequencer;
import org.adl.sequencer.ADLTOC;
import org.adl.sequencer.ADLValidRequests;
import org.adl.sequencer.SeqActivityTree;
import org.adl.sequencer.SeqNavRequests;
import org.sakaiproject.scorm.client.ScormTool;

import wicket.PageParameters;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.markup.html.AjaxLink;
import wicket.extensions.markup.html.tree.AbstractTree;
import wicket.extensions.markup.html.tree.Tree;
import wicket.markup.html.WebPage;
import wicket.markup.html.link.Link;
import wicket.model.IModel;
import wicket.model.Model;

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
	
	public NavigationFrame(LaunchFrameset frameset) {
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

		tree = new Tree("tree", createTreeModel()) 
		{
			protected void onNodeLinkClicked(AjaxRequestTarget target, TreeNode node)
			{
				super.onNodeLinkClicked(target, node);
				
				
				//contentPanel.updateMessage(target, renderNode(node));
			}
			
			protected String renderNode(TreeNode node)
			{
				ADLTOC item = (ADLTOC)((DefaultMutableTreeNode)node).getUserObject();
				return item.mTitle;				
			}
		};
		add(tree);
		
		tree.getTreeState().collapseAll();
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
		
		
		/*if (null != validRequests.mTOC) {
			// /TODO: setup a TOC property so that jsf can get it (as a List)
			// from a getter
			// / mTOC is Vector
			navAction = "loadUI_Choice";
		} else {
			navAction = "loadUI_Flow";
		}*/
		
		//sequencer.getValidRequests(validRequests);
		
        List<ADLTOC> tableOfContents = validRequests.mTOC;
			
		TreeModel model = null;
		
		MutableTreeNode rootNode = null;
		
		if (tableOfContents != null) {
			
			Map<Integer, MutableTreeNode> countMap = new HashMap<Integer, MutableTreeNode>();
			Map<Integer, List<ADLTOC>> depthMap = new HashMap<Integer, List<ADLTOC>>();
			
			int maxDepth = 0;
			
			for (ADLTOC contentItem : tableOfContents) {
				// Yay auto-boxing!
				Integer c = contentItem.mCount;
				Integer d = contentItem.mDepth;
				
				// Let's go ahead and create a new node for each content item
				MutableTreeNode node = new DefaultMutableTreeNode(contentItem);
				countMap.put(c, node);
				
				List depthList = depthMap.get(d);
				if (null == depthList) {
					depthList = new LinkedList<ADLTOC>();
					depthMap.put(d, depthList);
				}
				
				depthList.add(contentItem);
					
				if (d.intValue() > maxDepth)
					maxDepth = d.intValue();
				
				// The 'Root' item is the one with no parent
				if (contentItem.mParent == -1)
					rootNode = node;
				
				// Look for the 'Root' item
				/*if (contentItem.mParent == -1) {
					rootNode = new DefaultMutableTreeNode(contentItem);
					addChildren(tableOfContents, contentItem, rootNode);	
				}*/
			}
			
			// Start with the bottom
			for (int countDown = maxDepth;countDown >= 0;countDown--) {
				// Look at all the content items at this depth
				List<ADLTOC> depthList = depthMap.get(countDown);
				
				for (ADLTOC contentItem : depthList) {
					// Grab the actual node, we'll need it
					MutableTreeNode node = countMap.get(contentItem.mCount);
					// Now grab the parent node, if one exists
					if (contentItem.mParent != -1) {
						MutableTreeNode parent = countMap.get(contentItem.mParent);
						// Assume that we're walking across in the right order 
						((DefaultMutableTreeNode)parent).add(node);	
					}
				}
			}
		}
		
		//add(rootNode, list);
		if (null != rootNode)
			model = new DefaultTreeModel(rootNode);
		
		return model;
	}
	
	private int addChildren(List<ADLTOC> tableOfContents, ADLTOC parentItem, MutableTreeNode parentNode) {
		int count = parentItem.mCount;
		int found = 0;
		
		for (ADLTOC contentItem : tableOfContents) {
			// Look for items that have this item as their parent
			if (count == (contentItem.mParent + 1)) {
				MutableTreeNode childNode = new DefaultMutableTreeNode(contentItem);
				parentNode.insert(childNode, 0);
				found+=addChildren(tableOfContents, contentItem, childNode);
			}
		}
		
		for (Enumeration enumeration = parentNode.children();enumeration.hasMoreElements();) {
			MutableTreeNode child = (MutableTreeNode)enumeration.nextElement();
			
			found+=addChildren(tableOfContents, (ADLTOC)((DefaultMutableTreeNode)child).getUserObject(), child);
		}
		
		return found;
	}
	
	public class ContentItemModel extends Model {
		
	}
	
	
	/*private int addChildren(ADLTOC contentItem){
		int count=node.getCount();
		int found=0;
		logger.info("SCORMTool::ChoiceTreeBean:: node  (" + node.getIdentifier() +")");
		logger.info("SCORMTool::ChoiceTreeBean:: remaining items ("+ copy.size() +")");
		
		
		// find any objects that have this node's number as parent (eg, tocObject.getParent()+1==this.count)
		// .... make a new node for it, add it to this node, then call this method with that new node
		for(int i=copy.size()-1;i>=0;i-=(found+1)){ /// original TOC objects build with higher order items last
			SakaiScormTOC t = (SakaiScormTOC)copy.get(i);
			if(t.getParent()+1==count){
				logger.info("SCORMTool::ChoiceTreeBean:: adding subnode (" + t.getID() + ") to (" + node.getIdentifier() +")");
				SakaiScormTreeNode newNode = new SakaiScormTreeNode("sub-folder", t.getTitle(), t.getID(), false);
				setAttributes(newNode,t);
				node.getChildren().add(newNode);
				found++;copy.remove(i--);
				found+=addChildren(newNode);
			}
		}
		
		
		///Now, do the same for all the children of this node
		List children = node.getChildren();
		for(int i=0;i<children.size();++i){
			found+=addChildren((SakaiScormTreeNode)children.get(i));
		}
		return found;
		
	}*/
	
	
	
	private void add(MutableTreeNode parent, ADLTOC contentItem) {
		MutableTreeNode child;
		if (contentItem.mLeaf)
			child = addLeaf(parent, contentItem);
		else
			child = addBranch(parent, contentItem);
	}
	
	private MutableTreeNode addBranch(MutableTreeNode parent, ADLTOC contentItem) {
		MutableTreeNode child = new DefaultMutableTreeNode("subtree");
		
		((DefaultMutableTreeNode)parent).insert(child, 0);
		
		return child;
	}
	
	private MutableTreeNode addLeaf(MutableTreeNode parent, ADLTOC contentItem) {
		MutableTreeNode child = new DefaultMutableTreeNode(contentItem.mTitle);
		((DefaultMutableTreeNode)parent).insert(child, 0);

		return child;
	}
	
	
	protected TreeModel createTreeModel(String nothing) 
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
