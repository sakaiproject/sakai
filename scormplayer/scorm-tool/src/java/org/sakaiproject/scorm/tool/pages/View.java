package org.sakaiproject.scorm.tool.pages;

import org.adl.sequencer.SeqNavRequests;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.client.ClientPage;
import org.sakaiproject.scorm.client.api.IRunState;
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.scorm.tool.ScoBean;
import org.sakaiproject.scorm.tool.RunState;
import org.sakaiproject.scorm.tool.components.ChoicePanel;
import org.sakaiproject.scorm.tool.components.LaunchPanel;
import org.sakaiproject.user.api.User;


public class View extends ClientPage {
	private static Log log = LogFactory.getLog(View.class);
	private static final long serialVersionUID = 1L;
	
	@SpringBean
	ScormClientFacade clientFacade;
	
	private RunState runState;
	
	public View() {
		this(new PageParameters());
	}
	
	public View(PageParameters pageParams) {
		super();
		
		String contentPackageId = pageParams.getString("contentPackage");
		
		if (contentPackageId != null) 
			contentPackageId = contentPackageId.replace(':', '/');
		
		int navRequest = SeqNavRequests.NAV_START;
		
		String result = null;
		
		
		try {
			if (pageParams.containsKey("navRequest"))
				navRequest = pageParams.getInt("navRequest");
			
			User user = clientFacade.getCurrentUser();
			
			String userId = user.getId();
			
			runState = new RunState(clientFacade, contentPackageId, contentPackageId, userId);
			
			result = runState.navigate(navRequest, null);
			
			if (result == null || result.contains("_TOC_")) {
				LaunchPanel launchPanel = new LaunchPanel("actionPanel", runState);
				add(launchPanel);
				ScoBean clientBean = runState.produceScoBean(runState.getCurrentSco());
				clientBean.clearState();
				launchPanel.synchronizeState(runState, null);
				return;
			} 
		} catch (Exception e) {
			result = e.getMessage();
			e.printStackTrace();
			
			log.error("Caught an exception: " , e);
		} 
		
		ChoicePanel choicePanel = new ChoicePanel("actionPanel", contentPackageId, result);
		add(choicePanel);
		
	}
		

	public void renderHead(IHeaderResponse response) {
		//response.renderJavascriptReference(HEADSCRIPTS);
		response.renderCSSReference(TOOLBASE_CSS);
		response.renderCSSReference(TOOL_CSS);
		//response.renderOnLoadJavascript(BODY_ONLOAD_ADDTL);
	}
	
	/**
	 * No need for versioning this frame.
	 * 
	 * @see org.apache.wicket.Component#isVersioned()
	 */
	public boolean isVersioned()
	{
		return false;
	}
	

	
}
