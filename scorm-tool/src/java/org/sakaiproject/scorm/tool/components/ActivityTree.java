package org.sakaiproject.scorm.tool.components;

import java.io.Serializable;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.adl.sequencer.ISeqActivity;
import org.adl.sequencer.IValidRequests;
import org.adl.sequencer.SeqActivity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.CancelEventIfNoAjaxDecorator;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.tree.BaseTree;
import org.apache.wicket.markup.html.tree.LinkIconPanel;
import org.apache.wicket.markup.html.tree.LinkTree;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.time.Duration;
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.scorm.client.utils.ActivityAjaxEventBehavior;
import org.sakaiproject.scorm.tool.RunState;

public class ActivityTree extends LinkTree {
	private static final String BRANCH_NODE_IMAGE = "image/silk/application_side_tree.png";
	private static final String LEAF_NODE_IMAGE = "image/silk/tag_blue.png";
	
	private static Log log = LogFactory.getLog(ActivityTree.class);
	
	private static final long serialVersionUID = 1L;
	
	private RunState runState;
	private TreePanel treePanel;
	
	private boolean isEmpty = true;
	
	@SpringBean
	ScormClientFacade clientFacade;
		
	public ActivityTree(String id, RunState runState, TreePanel parent) {
		super(id);
		this.runState = runState;
		this.treePanel = parent;
		
		bindModel(runState);
		treePanel.getLaunchPanel().synchronizeState(runState, null);
	}
		
	private void bindModel(RunState runState) {
		IValidRequests requests = runState.getCurrentNavState();
				
		if (null != requests && null != requests.getTreeModel()) {
			setModel(new Model((Serializable)requests.getTreeModel()));
			isEmpty = false;
		} else {
			setModel(new Model((Serializable) new DefaultTreeModel(new DefaultMutableTreeNode())));
		}
	
		if (!isEmpty && runState != null) {
			TreeNode node = selectNode(runState.getCurrentActivityId());
		}
	}
	
	public TreeNode selectNode() {
		if (runState == null)
			return null;
		
		return selectNode(runState.getCurrentActivityId());
	}
	
	public TreeNode selectNode(String activityId) {
		if (activityId == null)
			return null;
		
		Model model = (Model)this.getModel();
		DefaultTreeModel treeModel = (DefaultTreeModel)model.getObject();
		
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)treeModel.getRoot();
		
		for (Enumeration<DefaultMutableTreeNode> e = root.breadthFirstEnumeration();e.hasMoreElements();) {
			DefaultMutableTreeNode node = e.nextElement();
			SeqActivity activity = (SeqActivity)node.getUserObject();
			
			String id = activity == null ? null : activity.getID();
			
			if (id != null && id.equals(activityId)) {
				if (!getTreeState().isNodeSelected(node))
					getTreeState().selectNode(node, true);
				return node;
			}
		}
		
		return null;
	}
	
	public boolean isChoice() {
		ISeqActivity activity = runState.getCurrentActivity();
		
		return null != activity && activity.getControlModeChoice();
	}
	
	public boolean isFlow() {
		ISeqActivity activity = runState.getCurrentActivity();
		
		return null != activity && activity.getControlModeFlow();
	}
	
	protected void onNodeLinkClicked(TreeNode node, BaseTree tree, AjaxRequestTarget target)
	{		
		log.info("onNodeLinkClicked");

		ISeqActivity activity = (ISeqActivity)((DefaultMutableTreeNode)node).getUserObject();
		
		log.info("ID: " + activity.getID() + " State ID: " + activity.getStateID());

		runState.navigate(activity, target);
		
		treePanel.getLaunchPanel().synchronizeState(runState, target);
		
		ActivityTree.this.bindModel(runState);
		
		//selectNode(runState.getCurrentActivityId());
		tree.updateTree(target);
		
		/*boolean makeVisible = isChoice();
		boolean isVisible = tree.isVisible();
		
		// Change visibility if necessary.
		if (isVisible == !makeVisible) {
			tree.setVisible(makeVisible);
			target.addComponent(parentPanel);
		}*/

	}

	@Override
	protected Component newNodeComponent(String id, IModel model)
	{
		return new ActivityLinkIconPanel(id, model, this, runState);
	}
	
	
	public MarkupContainer newLink(String id, final ILinkCallback callback)
	{
		if (getLinkType() == LinkType.REGULAR)
		{
			return new Link(id)
			{
				private static final long serialVersionUID = 1L;

				/**
				 * @see org.apache.wicket.markup.html.link.Link#onClick()
				 */
				public void onClick()
				{
					callback.onClick(null);
				}
			};
		}
		else if (getLinkType() == LinkType.AJAX)
		{
			return new ActivityAjaxLink(id)
			{
				private static final long serialVersionUID = 1L;

				/**
				 * @see org.apache.wicket.ajax.markup.html.AjaxLink#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
				 */
				public void onClick(AjaxRequestTarget target)
				{
					callback.onClick(target);
				}
			};
		}
		else
		{
			return new AjaxFallbackLink(id)
			{
				private static final long serialVersionUID = 1L;

				/**
				 * @see org.apache.wicket.ajax.markup.html.AjaxFallbackLink#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
				 */
				public void onClick(AjaxRequestTarget target)
				{
					callback.onClick(target);
				}
			};
		}
	}
	
	public abstract class ActivityAjaxLink extends AjaxLink {
		/**
		 * Construct.
		 * 
		 * @param id
		 */
		public ActivityAjaxLink(final String id)
		{
			this(id, null);
		}

		/**
		 * Construct.
		 * 
		 * @param id
		 * @param model
		 */
		public ActivityAjaxLink(final String id, final IModel model)
		{
			super(id, model);

			add(new ActivityAjaxEventBehavior("onclick")
			{
				private static final long serialVersionUID = 1L;

				protected void onEvent(AjaxRequestTarget target)
				{
					onClick(target);
				}

				protected IAjaxCallDecorator getAjaxCallDecorator()
				{
					return new CancelEventIfNoAjaxDecorator(ActivityAjaxLink.this.getAjaxCallDecorator());
				}

				protected void onComponentTag(ComponentTag tag)
				{
					// add the onclick handler only if link is enabled 
					if (isLinkEnabled())
					{
						super.onComponentTag(tag);
					}
				}
			}.setThrottleDelay(Duration.ONE_SECOND));
		}
	}
	
	
	public class ActivityLinkIconPanel extends LinkIconPanel {
		private Log log = LogFactory.getLog(ActivityLinkIconPanel.class);
		private RunState runState;
		
		private static final long serialVersionUID = 1L;

		public ActivityLinkIconPanel(String id, IModel model, ActivityTree tree, RunState runState) {
			super(id, model, tree);
			this.runState = runState;
		}
		
		protected void onNodeLinkClicked(TreeNode node, BaseTree tree, AjaxRequestTarget target)
		{
			//super.onNodeLinkClicked(node, tree, target);
			ActivityTree.this.onNodeLinkClicked(node, tree, target);
		}
		
		protected Component newContentComponent(String componentId, BaseTree tree, IModel model)
		{
			ISeqActivity activity = (ISeqActivity)((DefaultMutableTreeNode)model.getObject()).getUserObject();
			
			String text = (null == activity) ? "" : activity.getTitle();
			
			return new Label(componentId, text);
		}
	}


}
