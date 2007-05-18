package org.sakaiproject.scorm.tool.pages;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.adl.sequencer.ADLLaunch;
import org.adl.sequencer.ADLSequencer;
import org.adl.sequencer.ADLValidRequests;
import org.adl.sequencer.SeqActivity;
import org.adl.sequencer.SeqNavRequests;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.tree.ITreeState;
import org.apache.wicket.markup.html.tree.Tree;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.scorm.tool.components.ActivityTree;

public class NavigationFrame extends WebPage {	
	private static final long serialVersionUID = 1L;

	@SpringBean
	ScormClientFacade clientFacade;
	
	/*private Tree tree;
	
	protected AbstractTree getTree()
	{
		return tree;
	}*/
	
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

	
	public NavigationFrame(PageParameters pageParams) {
		super();
		
		TreeModel treeModel = createTreeModel();
		final Tree tree = new ActivityTree("tree", treeModel)
		{
			protected void onNodeLinkClicked(AjaxRequestTarget target, TreeNode node)
			{
				super.onNodeLinkClicked(target, node);
				
				SeqActivity item = (SeqActivity)((DefaultMutableTreeNode)node).getUserObject();
				
				System.out.println("RESOURCE: " + item.getResourceID());					
			}
		};
		add(tree);
		
		add(new AjaxLink("expandAll") 
		{
			public void onClick(AjaxRequestTarget target)
			{
				tree.getTreeState().expandAll();
				tree.updateTree(target);
			}
		});
		
		add(new AjaxLink("collapseAll") 
		{
			public void onClick(AjaxRequestTarget target)
			{
				tree.getTreeState().collapseAll();
				tree.updateTree(target);
			}
		});
		
		add(new AjaxLink("switchRootless") 
		{
			public void onClick(AjaxRequestTarget target)
			{
				tree.setRootLess(!tree.isRootLess());
				tree.updateTree(target);
			}
		});
		
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
		ADLSequencer sequencer = clientFacade.getSequencer();
			//((ScormTool)getApplication()).getClientFacade().getSequencer();
		
		ADLValidRequests validRequests = new ADLValidRequests();
        
		
		ADLLaunch launchInfo = sequencer.navigate(SeqNavRequests.NAV_START); 

		validRequests = launchInfo.mNavState;
		
		return validRequests.mTreeModel;
	}
	
	
}
