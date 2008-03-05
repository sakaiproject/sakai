/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.scorm.ui.player.components;

import java.io.Serializable;
import java.net.URLDecoder;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.adl.sequencer.ISeqActivity;
import org.adl.sequencer.SeqActivity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.CancelEventIfNoAjaxDecorator;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.tree.BaseTree;
import org.apache.wicket.markup.html.tree.LinkIconPanel;
import org.apache.wicket.markup.html.tree.LinkTree;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.time.Duration;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.scorm.service.api.ScormSequencingService;
import org.sakaiproject.scorm.ui.ResourceNavigator;
import org.sakaiproject.scorm.ui.UISynchronizerPanel;
import org.sakaiproject.scorm.ui.player.behaviors.ActivityAjaxEventBehavior;

public class ActivityTree extends LinkTree {	
	private static Log log = LogFactory.getLog(ActivityTree.class);
	
	private static final long serialVersionUID = 1L;
	private static final String ARIA_TREE_ROLE = "wairole:tree";
	
	protected SessionBean sessionBean;
	protected UISynchronizerPanel synchronizer;
	
	protected boolean wasEmpty = false;
	protected boolean isEmpty = true;
	
	@SpringBean
	transient LearningManagementSystem lms;
	@SpringBean
	transient ScormResourceService resourceService;
	@SpringBean
	transient ScormSequencingService sequencingService;
	
	
	public ActivityTree(String id, SessionBean sessionBean, UISynchronizerPanel synchronizer) {
		super(id);
		this.synchronizer = synchronizer;
		this.sessionBean = sessionBean;
		this.setOutputMarkupId(true);
		
		bindModel(sessionBean);
		if (synchronizer != null)
			synchronizer.synchronizeState(sessionBean, null);
		add(new AttributeModifier("role", new Model(ARIA_TREE_ROLE)));
		
		getTreeState().expandAll();
	}
		
	protected void bindModel(SessionBean sessionBean) {
		TreeModel treeModel = sequencingService.getTreeModel(sessionBean);
		
		if (null != treeModel) {
			setModel(new Model((Serializable)treeModel));
			isEmpty = false;
		} else {
			setModel(new Model((Serializable) new DefaultTreeModel(new DefaultMutableTreeNode())));
		}
	
		if (!isEmpty && sessionBean != null) {
			selectNode(sessionBean.getActivityId());
		} 
	}
	
	public TreeNode selectNode() {
		if (sessionBean == null)
			return null;
		
		return selectNode(sessionBean.getActivityId());
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
			
			if (log.isDebugEnabled()) {
				log.debug("Activity: " + activity.getID());
				log.debug("Title: " + activity.getTitle());
				log.debug("Is Constrain Choice: " + activity.getConstrainChoice());
				log.debug("Is Control Forward Only: " + activity.getControlForwardOnly());
				log.debug("Is Control Mode Choice: " + activity.getControlModeChoice());
				log.debug("Is Control Mode Choice Exit: " + activity.getControlModeChoiceExit());
				log.debug("Is Control Mode Flow: " + activity.getControlModeFlow());
			}
			
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
		return sequencingService.isControlModeChoice(sessionBean);
	}
	
	public boolean isFlow() {
		return sequencingService.isControlModeFlow(sessionBean);
	}
	
	protected void onNodeLinkClicked(TreeNode node, BaseTree tree, AjaxRequestTarget target)
	{		
		log.debug("onNodeLinkClicked");

		ISeqActivity activity = (ISeqActivity)((DefaultMutableTreeNode)node).getUserObject();
		
		if (log.isDebugEnabled())
			log.debug("ID: " + activity.getID() + " State ID: " + activity.getStateID());

		sequencingService.navigateToActivity(activity.getID(), sessionBean, new LocalResourceNavigator(), target);
		
		if (synchronizer != null)
			synchronizer.synchronizeState(sessionBean, target);
		
		// FIXME: Turning this off to see if its necessary -- probably needs some more intricate solution
		// FIXME: Turning back on for the moment.
		ActivityTree.this.bindModel(sessionBean);
		
		if (isEmpty() && !wasEmpty) {
			ActivityTree.this.setVisible(false);
			if (synchronizer != null)
				target.addComponent((Component)synchronizer);
			wasEmpty = true;
		}
		
		this.updateTree(target);
	}

	@Override
	protected Component newNodeComponent(String id, IModel model)
	{
		return new ActivityLinkIconPanel(id, model, this);
	}
	
	private static final LinkType ACTIVITY_AJAX_LinkType = new LinkType("ACTIVITY_AJAX");
	
	public LinkType getLinkType()
	{
		return ACTIVITY_AJAX_LinkType;
	}
	
	public MarkupContainer newLink(String id, final ILinkCallback callback)
	{
		if (getLinkType() == ACTIVITY_AJAX_LinkType)
		{
			return new ActivityAjaxLink(id)
			{
				private static final long serialVersionUID = 1L;
				
				public void onClick(AjaxRequestTarget target)
				{
					callback.onClick(target);
				}
				
			};
		} 
		
		return super.newLink(id, callback);	
	}
	
	
	public abstract class ActivityAjaxLink extends AjaxLink {
		private static final String ARIA_TREEITEM_ROLE = "wairole:treeitem";
		
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
			
			
			/*if (lms.canUseRelativeUrls()) {
				
				add(new AjaxEventBehavior("onclick")
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
				
			} else {
			*/
				add(new ActivityAjaxEventBehavior("onclick", lms.canUseRelativeUrls()) {
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
			//}
			
			add(new AttributeModifier("role", new Model(ARIA_TREEITEM_ROLE)));
		}
	}
	
	private static final ResourceReference RESOURCE_FOLDER_OPEN = new ResourceReference(ActivityLinkIconPanel.class, "res/application_side_tree.gif");
	private static final ResourceReference RESOURCE_FOLDER_CLOSED = new ResourceReference(ActivityLinkIconPanel.class, "res/application_side_tree.gif");
	private static final ResourceReference RESOURCE_ITEM = new ResourceReference(ActivityLinkIconPanel.class, "res/tag_blue.gif");	
	
	public class ActivityLinkIconPanel extends LinkIconPanel {
		
		private Log log = LogFactory.getLog(ActivityLinkIconPanel.class);
		
		private static final long serialVersionUID = 1L;

		public ActivityLinkIconPanel(String id, IModel model, ActivityTree tree) {
			super(id, model, tree);
		}
		
		protected void onNodeLinkClicked(TreeNode node, BaseTree tree, AjaxRequestTarget target)
		{
			ActivityTree.this.onNodeLinkClicked(node, tree, target);
		}
		
		protected Component newContentComponent(String componentId, BaseTree tree, IModel model)
		{
			ISeqActivity activity = (ISeqActivity)((DefaultMutableTreeNode)model.getObject()).getUserObject();

			String text = "";
			try {
				text = (null == activity) ? "" : URLDecoder.decode(activity.getTitle(), "UTF-8");
			} catch (Exception e) {
				log.error("Caught exception ", e);
			}
				
			return new Label(componentId, text);
		}
		
		
		/**
		 * Returns resource reference for closed folder icon.
		 * @param node
		 * @return resource reference
		 */
		protected ResourceReference getResourceFolderClosed(TreeNode node)
		{
			return RESOURCE_FOLDER_CLOSED;
		}

		/**
		 * Returns resource reference for open folder icon.
		 * @param node
		 * @return resource reference
		 */
		protected ResourceReference getResourceFolderOpen(TreeNode node)
		{
			return RESOURCE_FOLDER_OPEN;
		}

		/**
		 * Returns resource reference for a leaf icon.
		 * @param node
		 * @return resource reference
		 */
		protected ResourceReference getResourceItemLeaf(TreeNode node)
		{
			return RESOURCE_ITEM;
		}
	}

	public boolean isEmpty() {
		return isEmpty;
	}
	
	public class LocalResourceNavigator extends ResourceNavigator {

		private static final long serialVersionUID = 1L;

		@Override
		public Object getApplication() {
			return this.getApplication();
		}
		
		@Override
		protected ScormResourceService resourceService() {
			return ActivityTree.this.resourceService;
		}
		
		public Component getFrameComponent() {
			if (synchronizer != null && synchronizer.getContentPanel() != null) 
				return synchronizer.getContentPanel();
			return null;
		}
		
		public boolean useLocationRedirect() {
			return false;
		}
		
	}
	

}
