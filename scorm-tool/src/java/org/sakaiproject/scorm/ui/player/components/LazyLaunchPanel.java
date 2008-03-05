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

import java.util.List;

import org.adl.sequencer.SeqNavRequests;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebResource;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.model.api.Attempt;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.ContentPackageResource;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.navigation.INavigable;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.scorm.service.api.ScormResultService;
import org.sakaiproject.scorm.service.api.ScormSequencingService;
import org.sakaiproject.scorm.ui.ResourceNavigator;
import org.sakaiproject.scorm.ui.player.pages.PlayerPage;
import org.sakaiproject.scorm.ui.player.util.ContentPackageWebResource;

public class LazyLaunchPanel extends LazyLoadPanel {

	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(LazyLaunchPanel.class);
	
	@SpringBean
	transient ScormResourceService resourceService;
	@SpringBean
	transient ScormResultService resultService;
	@SpringBean
	transient ScormSequencingService sequencingService;
	
	private PlayerPage view;
	private LaunchPanel launchPanel;
	
	private final LocalResourceNavigator navigator;
	private final int userNavRequest;
	
	public LazyLaunchPanel(String id, SessionBean sessionBean, int userNavRequest, PlayerPage view) {
		super(id, new Model(sessionBean));
		this.navigator = new LocalResourceNavigator();
		this.userNavRequest = userNavRequest;
		this.view = view;
	}

	@Override
    public Component getLazyLoadComponent(String lazyId, AjaxRequestTarget target) {
		SessionBean sessionBean = (SessionBean)getModelObject();
		
		modelChanging();
		
		Component component = launch(sessionBean, lazyId, target);
		
		modelChanged();
		
		return component;
	}

	
	private boolean canLaunch(SessionBean sessionBean) {
		// Verify that the user is allowed to start a new attempt
		ContentPackage contentPackage = sessionBean.getContentPackage();
		
		// If the numberOfTries is not Unlimited then verify that we haven't hit the max
		if (contentPackage != null && contentPackage.getNumberOfTries() != -1) 	
			return (sessionBean.getAttemptNumber() <= contentPackage.getNumberOfTries());
		
		return true;
	}
	
	private int chooseStartOrResume(SessionBean sessionBean, INavigable navigator, AjaxRequestTarget target) {
		int navRequest = SeqNavRequests.NAV_START;
		
		List<Attempt> attempts = resultService.getAttempts(sessionBean.getContentPackage().getContentPackageId(), sessionBean.getLearnerId());
		
		long attemptNumber = 1;
		
		if (attempts != null && attempts.size() > 0) {
			// Since attempts are order by attempt number, descending, then the first one is the max
			Attempt attempt = attempts.get(0);
			
			if (attempt.isSuspended()) {
				// If the user suspended the last attempt, let them return to it.
				attemptNumber = attempt.getAttemptNumber();
				navRequest = SeqNavRequests.NAV_RESUMEALL;
				attempt.setSuspended(false);
				resultService.saveAttempt(attempt);
			} else if (attempt.isNotExited()) {
				// Or if the server crashed mid-session or something, then abandon the old one and start over
				attemptNumber = attempt.getAttemptNumber() + 1;
				/*log.warn("Abandoning old attempt and re-starting . . . ");
				sessionBean.setRestart(true);
				String result = sequencingService.navigate(SeqNavRequests.NAV_ABANDONALL, sessionBean, navigator, target);
			
				if (result != null && result.equals("_ENDSESSION_")) {
					attempt.setNotExited(false);
					resultService.saveAttempt(attempt);
				}*/
			} else {
				// Otherwise, we can start a new one
				attemptNumber = attempt.getAttemptNumber() + 1;
			}
		}
		
		sessionBean.setAttemptNumber(attemptNumber);
		
		return navRequest;
	}
	
	private Component launch(SessionBean sessionBean, String lazyId, AjaxRequestTarget target) {
		
		String result = null;
		
		
		try {
			
			// If a content package has been suspended, we want to resume, otherwise start
			int navRequest = chooseStartOrResume(sessionBean, navigator, target);
			
			// Sometimes the user may want to override this
			if (userNavRequest != -1)
				navRequest = userNavRequest;
			
			// Make sure the user's allowed to launch
			if (!canLaunch(sessionBean)) 			
				return new DeniedPanel(lazyId, sessionBean);
				
			result = tryLaunch(sessionBean, navRequest, target);
						
			if (result == null || result.contains("_TOC_")) {
				launchPanel = new LaunchPanel(lazyId, sessionBean, view);
				
				loadSharedResources(sessionBean.getContentPackage().getResourceId());
				
				if (log.isDebugEnabled())
					log.debug("PlayerPage sco is " + sessionBean.getScoId());
				
				//ScoBean scoBean = api.produceScoBean(sessionBean.getScoId(), sessionBean);
				//scoBean.clearState();
				view.synchronizeState(sessionBean, target);
				
				if (launchPanel.getTree().isEmpty()) {
					launchPanel.getTree().setVisible(false);
				}
				
				navigator.displayResource(sessionBean, null);
				
				return launchPanel;
			} 
			
			if (log.isDebugEnabled())
				log.debug("Result is " + result);
			
		} catch (Exception e) {
			result = e.getMessage();
			e.printStackTrace();
			
			log.error("Caught an exception: " , e);
		} 
		
		return new ChoicePanel(lazyId, sessionBean.getContentPackage().getContentPackageId(), sessionBean.getContentPackage().getResourceId(), result);
	}
	
	
	private void loadSharedResources(String resourceId) {
		List<ContentPackageResource> resources = resourceService.getResources(resourceId);

		getApplication().getSharedResources().putClassAlias(PlayerPage.class, "play");
		
		for (ContentPackageResource cpResource : resources) {
			String resourceName = cpResource.getPath();
			
			if (getApplication().getSharedResources().get(resourceName) == null) {
				
				WebResource webResource = new ContentPackageWebResource(cpResource);
				
				if (log.isDebugEnabled()) 
					log.debug("Adding a shared resource as " + resourceName);
				
				getApplication().getSharedResources().add(PlayerPage.class, resourceName, null, null, webResource);

			}
		}
	}
	
	private String tryLaunch(SessionBean sessionBean, int navRequest, AjaxRequestTarget target) {
		String result = sequencingService.navigate(navRequest, sessionBean, null, target);
				
		// Success is null.
		if (result == null || result.contains("_TOC_"))
			return null;
		
		// If we get an invalid nav request, chances are that we need to abandon and start again
		if (result.equals("_INVALIDNAVREQ_")) {
			sessionBean.setRestart(true);
			result = sequencingService.navigate(SeqNavRequests.NAV_ABANDONALL, sessionBean, null, target);
			
			// If it worked, start again
			if (result.equals("_ENDSESSION_")) {
				sessionBean.setRestart(true);
				result = sequencingService.navigate(SeqNavRequests.NAV_START, sessionBean, null, target);
			}
		// Otherwise, we may need to issue a 'None' 
		} else if (result.equals("_SEQBLOCKED_")) {
			result = sequencingService.navigate(SeqNavRequests.NAV_NONE, sessionBean, null, target);
		
		}

		return result;
	}
	
	
	public class LocalResourceNavigator extends ResourceNavigator {

		private static final long serialVersionUID = 1L;

		@Override
		public Object getApplication() {
			return this.getApplication();
		}
		
		@Override
		protected ScormResourceService resourceService() {
			return LazyLaunchPanel.this.resourceService;
		}
		
		public Component getFrameComponent() {
			if (launchPanel != null)
				return launchPanel.getContentPanel();
			return null;
		}
		
	}


	public LaunchPanel getLaunchPanel() {
		return launchPanel;
	}
}
