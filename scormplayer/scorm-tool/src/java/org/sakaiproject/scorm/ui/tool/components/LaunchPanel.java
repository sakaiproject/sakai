package org.sakaiproject.scorm.ui.tool.components;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.scorm.ui.tool.RunState;

public class LaunchPanel extends Panel implements IHeaderContributor {
	private static final long serialVersionUID = 1L;
	//protected final FrameTarget frameTarget = new FrameTarget(null);
	//public WebComponent contentFrameTag;
	
	@SpringBean
	ScormClientFacade clientFacade;
	
	//private ClientBean clientBean;
	
	private CommunicationPanel communicationPanel;
	private TreePanel treePanel;
	private ButtonForm buttonForm;
	
	public LaunchPanel(String id, final RunState runState) {
		super(id);
						
		//String contentHref = "";
		
		//if (runState != null && runState.getCurrentActivityId() != null)
		//	contentHref = runState.getCurrentHref();
		
		/*contentFrameTag = new WebComponent("contentFrame");
		contentFrameTag.setOutputMarkupId(true);
		contentFrameTag.add(new AttributeModifier("src", new Model(contentHref))); //new FrameModel()));
		add(contentFrameTag);*/
		
		//InlineFrame contentFrame = new InlineFrame("contentFrame", new CompletionPage());
		//add(contentFrame);
		
		//clientBean = new ClientBean(clientFacade, runState);
		
		communicationPanel = new CommunicationPanel("comPanel", runState, this);
		communicationPanel.setOutputMarkupId(true);
		add(communicationPanel);
				
		treePanel = new TreePanel("navPanel", runState, this);
		treePanel.setOutputMarkupId(true);
		add(treePanel);
		
		/*add(new AjaxLazyLoadPanel("navPanel"){
			private static final long serialVersionUID = 1L;

			@Override
            public Component getLazyLoadComponent(String lazyId)
            {
				treePanel = new TreePanel(lazyId, runState, LaunchPanel.this);
				treePanel.setOutputMarkupId(true);
                return treePanel;
            }
        });*/
		
		
		//treePanel.setVisible(false);
		
		buttonForm = new ButtonForm("buttonForm", runState, this);
		buttonForm.setOutputMarkupId(true);
		add(buttonForm);
		
		//buttonForm.setVisible(false);
	}
	
	
	public void renderHead(IHeaderResponse response) {
		//response.renderOnBeforeUnloadJavascript(javascript);
	}
	
	
	public void synchronizeState(RunState runState, AjaxRequestTarget target) {
		
		if (null != treePanel && null != treePanel.getActivityTree() && null != runState 
				&& treePanel.getActivityTree().isVisible() != runState.isTreeVisible()) {
			treePanel.setTreeVisible(runState.isTreeVisible(), target);
		}
		
		if (null != buttonForm) {
			buttonForm.setNextButtonVisible(runState.isContinueEnabled() || runState.isContinueExitEnabled(), target);
			buttonForm.setPrevButtonVisible(runState.isPreviousEnabled(), target);
			buttonForm.setStartButtonVisible(runState.isStartEnabled(), target);
			//buttonForm.setQuitButtonVisible(runState.isContinueExitEnabled(), target);
			buttonForm.setSuspendButtonVisible(runState.isSuspendEnabled(), target);
		
			//if (target != null)
			//	target.addComponent(buttonForm);
		}
		
		
	}
	
	
		
	/*public final class FrameModel extends Model
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

		private Class frameClass;
		
		private String url = null;


		public FrameTarget()
		{
		}

		public FrameTarget(Class frameClass)
		{
			this.frameClass = frameClass;
		}


		public Class getFrameClass()
		{
			return frameClass;
		}


		public void setFrameClass(Class frameClass)
		{
			this.frameClass = frameClass;
		}
		
	}*/


	public TreePanel getTreePanel() {
		return treePanel;
	}

	public CommunicationPanel getCommunicationPanel() {
		return communicationPanel;
	}

	public ButtonForm getButtonForm() {
		return buttonForm;
	}

	
	
}
