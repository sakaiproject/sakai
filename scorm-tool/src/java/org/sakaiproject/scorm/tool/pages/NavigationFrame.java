package org.sakaiproject.scorm.tool.pages;

import java.io.Serializable;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.adl.sequencer.ILaunch;
import org.adl.sequencer.ISeqActivity;
import org.adl.sequencer.ISequencer;
import org.adl.sequencer.IValidRequests;
import org.adl.sequencer.SeqNavRequests;
import org.adl.validator.contentpackage.ILaunchData;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.tree.ITreeState;
import org.apache.wicket.markup.html.tree.Tree;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestCodingStrategy;
import org.apache.wicket.request.target.basic.RedirectRequestTarget;
import org.apache.wicket.request.target.component.BookmarkablePageRequestTarget;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.scorm.client.utils.ActivityLink;
import org.sakaiproject.scorm.client.utils.IActivityLinkCallback;
import org.sakaiproject.scorm.model.api.ContentPackageManifest;
import org.sakaiproject.scorm.tool.components.ActivityTree;
import org.sakaiproject.scorm.tool.components.ApiPanel;

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
		
		//add(new ApiPanel("api-panel"));
		
		String contentPackageId = pageParams.getString("contentPackage");
		final ContentPackageManifest manifest = clientFacade.getManifest(contentPackageId);
		
		final ISequencer sequencer = clientFacade.getSequencer(manifest);
		TreeModel treeModel = createTreeModel(sequencer, manifest);
		
		final Tree tree = new ActivityTree("tree", treeModel)
		{
			protected void onNodeLinkClicked(AjaxRequestTarget target, TreeNode node)
			{
				super.onNodeLinkClicked(target, node);
				
				ISeqActivity item = (ISeqActivity)((DefaultMutableTreeNode)node).getUserObject();
				
				System.out.println("ID: " + item.getID() + " State ID: " + item.getStateID());
								
				ILaunch launch = sequencer.navigate(item.getID());
				String sco = launch.getSco();
				
				ILaunchData launchData = manifest.getLaunchData(sco);
				
				if (null != launchData) {
					String url = manifest.getUrl();
					String launchLine = launchData.getLaunchLine();
					
					StringBuffer href = new StringBuffer().append(url);
					
					if (!url.endsWith(Entity.SEPARATOR) && !launchLine.startsWith(Entity.SEPARATOR))
						href.append(Entity.SEPARATOR);
						
					href.append(launchLine);					
					
					if (null != href)
						target.appendJavascript("parent.content.location.href='" + href.toString() + "'");
				}
				
				TreeModel newTreeModel = refreshTreeModel(sequencer, manifest, item.getID());
				if (null != newTreeModel) {
					this.detachModel();
					this.setModel(new Model((Serializable)newTreeModel));
				}
				
				target.addComponent(this);
			}
			
			/*@Override
			protected MarkupContainer newNodeLink(MarkupContainer parent, String id, final TreeNode node)
			{
				ISeqActivity item = (ISeqActivity)((DefaultMutableTreeNode)node).getUserObject();
				String identifier = item.getID();
				ILaunchData launchData = manifest.getLaunchData(identifier);
				
				final String href = null;
				
				
				//System.out.println("State id: " + identifier + " Launch line: " + launchData.getLaunchLine());
				
				return new ActivityLink(id, href, new IActivityLinkCallback() {
					private static final long serialVersionUID = 1L;

					public void onClick(AjaxRequestTarget target)
					{
						getTreeState().selectNode(node, !getTreeState().isNodeSelected(node));
						onNodeLinkClicked(target, node);
						updateTree(target);
					}
				});
			}*/
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
		ISeqActivity item = (ISeqActivity)treeNode.getUserObject();
		
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
	
	protected TreeModel createTreeModel(ISequencer sequencer, ContentPackageManifest manifest) {	
		
		ILaunch launchInfo = sequencer.navigate(SeqNavRequests.NAV_START); 
		IValidRequests validRequests = launchInfo.getNavState();
		
		return validRequests.getTreeModel();
	}
	
	protected TreeModel refreshTreeModel(ISequencer sequencer, ContentPackageManifest manifest, String navRequest) {
		ILaunch launchInfo = sequencer.navigate(navRequest); 
		IValidRequests validRequests = launchInfo.getNavState();
		
		if (null == validRequests)
			return null;
		
		return validRequests.getTreeModel();
	}
	
	
}
