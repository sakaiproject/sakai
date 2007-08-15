package org.sakaiproject.scorm.tool.components;

import java.io.Serializable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestCodingStrategy;
import org.apache.wicket.request.target.component.BookmarkablePageRequestTarget;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.scorm.tool.ScoBean;
import org.sakaiproject.scorm.tool.RunState;
import org.sakaiproject.scorm.tool.pages.BlankPage;

import org.apache.wicket.ajax.AjaxRequestTarget;

public class LaunchPanel extends Panel {
	private static final long serialVersionUID = 1L;
	protected final FrameTarget frameTarget = new FrameTarget(BlankPage.class);
	public WebComponent contentFrameTag;
	
	@SpringBean
	ScormClientFacade clientFacade;
	
	//private ClientBean clientBean;
	
	private ApiPanel apiPanel;
	private TreePanel treePanel;
	private ButtonForm buttonForm;
	
	public LaunchPanel(String id, RunState runState) {
		super(id);
		
		String contentHref = "";
		
		if (runState != null && runState.getCurrentActivityId() != null)
			contentHref = runState.getCurrentHref();
		
		contentFrameTag = new WebComponent("contentFrame");
		contentFrameTag.setOutputMarkupId(true);
		contentFrameTag.add(new AttributeModifier("src", new Model(contentHref))); //new FrameModel()));
		add(contentFrameTag);
		
		//clientBean = new ClientBean(clientFacade, runState);
		
		apiPanel = new ApiPanel("api-panel", runState, this);
		apiPanel.setOutputMarkupId(true);
		add(apiPanel);
				
		treePanel = new TreePanel("navPanel", runState, this);
		treePanel.setOutputMarkupId(true);
		add(treePanel);
		
		buttonForm = new ButtonForm("buttonForm", runState, this);
		buttonForm.setOutputMarkupId(true);
		add(buttonForm);
	}
	
	
	public void synchronizeState(RunState runState, AjaxRequestTarget target) {
		
		if (null != treePanel) {
			treePanel.setTreeVisible(runState.isTreeVisible(), target);
		}
		
		if (null != buttonForm) {
			buttonForm.setNextButtonVisible(runState.isContinueEnabled() || runState.isContinueExitEnabled(), target);
			buttonForm.setPrevButtonVisible(runState.isPreviousEnabled(), target);
			buttonForm.setStartButtonVisible(runState.isStartEnabled(), target);
			buttonForm.setQuitButtonVisible(runState.isContinueExitEnabled(), target);
			buttonForm.setSuspendButtonVisible(runState.isSuspendEnabled(), target);
		
			//if (target != null)
			//	target.addComponent(buttonForm);
		}
		
		
	}
	

	public final class FrameModel extends Model
	{
		private static final long serialVersionUID = 1L;

		public Object getObject()
		{
			RequestCycle cycle = getRequestCycle();
			IRequestCodingStrategy encoder = cycle.getProcessor().getRequestCodingStrategy();
			
			return encoder.encode(cycle, new BookmarkablePageRequestTarget("contentFrame",
					frameTarget.getFrameClass(), new PageParameters()));
		}
	}
	
	public final class FrameTarget implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/** the class of the bookmarkable page. */
		private Class frameClass;
		
		private String url = null;

		/**
		 * Construct.
		 */
		public FrameTarget()
		{
		}

		/**
		 * Construct.
		 * 
		 * @param frameClass
		 */
		public FrameTarget(Class frameClass)
		{
			this.frameClass = frameClass;
		}

		/**
		 * Gets frame class.
		 * 
		 * @return lefFrameClass
		 */
		public Class getFrameClass()
		{
			return frameClass;
		}

		/**
		 * Sets frame class.
		 * 
		 * @param frameClass
		 *            lefFrameClass
		 */
		public void setFrameClass(Class frameClass)
		{
			this.frameClass = frameClass;
		}
		
	}


	public TreePanel getTreePanel() {
		return treePanel;
	}

	public ApiPanel getApiPanel() {
		return apiPanel;
	}
	
}
