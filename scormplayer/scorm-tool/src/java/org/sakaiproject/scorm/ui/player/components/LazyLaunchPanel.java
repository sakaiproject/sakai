/**
 * Copyright (c) 2007 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.scorm.ui.player.components;

import java.util.List;
import lombok.Getter;

import lombok.extern.slf4j.Slf4j;

import org.adl.sequencer.IValidRequests;
import org.adl.sequencer.SeqNavRequests;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.scorm.model.api.Attempt;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.ContentPackageResource;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.navigation.INavigable;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.scorm.service.api.ScormResultService;
import org.sakaiproject.scorm.service.api.ScormSequencingService;
import org.sakaiproject.scorm.ui.ResourceNavigator;
import org.sakaiproject.scorm.ui.player.pages.ScormPlayerPage;
import org.sakaiproject.scorm.ui.player.util.ContentPackageWebResource;

@Slf4j
public class LazyLaunchPanel extends LazyLoadPanel
{
	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.scorm.service.api.ScormResourceService")
	ScormResourceService resourceService;

	@SpringBean(name = "org.sakaiproject.scorm.service.api.ScormResultService")
	ScormResultService resultService;

	@SpringBean(name = "org.sakaiproject.scorm.service.api.ScormSequencingService")
	ScormSequencingService sequencingService;

	@SpringBean(name = "org.sakaiproject.scorm.service.api.LearningManagementSystem")
	LearningManagementSystem learningManagementSystem;

	private ScormPlayerPage view;
	@Getter private LaunchPanel launchPanel;
	private final LocalResourceNavigator navigator;
	private final int userNavRequest;
	private static final String NAV_RESULT_TOC = "_TOC_";
	private static final String NAV_RESULT_INVALID_REQ = "_INVALIDNAVREQ_";
	private static final String NAV_RESULT_END_SESSION = "_ENDSESSION_";

	public LazyLaunchPanel(String id, SessionBean sessionBean, int userNavRequest, ScormPlayerPage view)
	{
		super(id, new Model(sessionBean), view.getPageParameters());
		this.navigator = new LocalResourceNavigator();
		this.userNavRequest = userNavRequest;
		this.view = view;
	}

	@Override
	public Component getLazyLoadComponent(String lazyId, AjaxRequestTarget target)
	{
		SessionBean sessionBean = (SessionBean) getDefaultModelObject();
		modelChanging();
		Component component = launch(sessionBean, lazyId, target);
		modelChanged();
		return component;
	}

	private boolean canLaunch(SessionBean sessionBean)
	{
		// Verify that the user is allowed to start a new attempt
		ContentPackage contentPackage = sessionBean.getContentPackage();
		return learningManagementSystem.canLaunchAttempt(contentPackage, sessionBean.getAttemptNumber());
	}

	private int chooseStartOrResume(SessionBean sessionBean, INavigable navigator, AjaxRequestTarget target)
	{
		int navRequest = SeqNavRequests.NAV_NONE;
		sessionBean.setAttempt(null);
		sequencingService.navigate(SeqNavRequests.NAV_NONE, sessionBean, null, target);
		IValidRequests navigationState = sessionBean.getNavigationState();
		if (navigationState.isStartEnabled())
		{
			navRequest = SeqNavRequests.NAV_START;
		}

		int attemptsCount = resultService.countAttempts(sessionBean.getContentPackage().getContentPackageId(), sessionBean.getLearnerId());
		long attemptNumber;

		if (attemptsCount > 0)
		{
			// Since attempts are order by attempt number, descending, then the first one is the max
			Attempt attempt = resultService.getNewstAttempt(sessionBean.getContentPackage().getContentPackageId(), sessionBean.getLearnerId());

			if (attempt.isSuspended())
			{
				// If the user suspended the last attempt, let them return to it.
				attemptNumber = attempt.getAttemptNumber();
				sessionBean.setAttempt(attempt);
				navRequest = SeqNavRequests.NAV_RESUMEALL;
			}
			else if (attempt.isNotExited())
			{
				// Or if the server crashed mid-session or something, just continue playing...
				attemptNumber = attempt.getAttemptNumber();
				sessionBean.setAttempt(attempt);
			}
			else
			{
				// Check if there is a limit to the amount of attempts, attempt numbers start with 1 
				int numberOfTries = sessionBean.getContentPackage().getNumberOfTries();
				if (numberOfTries != -1 && attempt.getAttemptNumber() >= numberOfTries)
				{
					attemptNumber = attempt.getAttemptNumber();
				}
				else
				{
					// Otherwise, we can start a new one
					attemptNumber = attempt.getAttemptNumber() + 1;
				}
			}
		}
		else
		{
			attemptNumber = 1; // Attempt nr. starts a 1.
		}

		sessionBean.setAttemptNumber(attemptNumber);
		return navRequest;
	}

	private Component launch(SessionBean sessionBean, String lazyId, AjaxRequestTarget target)
	{
		String result = null;
		try
		{
			// If a content package has been suspended, we want to resume, otherwise start
			int navRequest = chooseStartOrResume(sessionBean, navigator, target);

			// Sometimes the user may want to override this
			if (userNavRequest != -1)
			{
				navRequest = userNavRequest;
			}

			// Make sure the user's alloed to launch
			if (!canLaunch(sessionBean))
			{
				return new DeniedPanel(lazyId, sessionBean);
			}

			result = tryLaunch(sessionBean, navRequest, target);

			if (result == null || result.contains(NAV_RESULT_TOC))
			{
				launchPanel = new LaunchPanel(lazyId, sessionBean, view);
				loadSharedResources(sessionBean.getContentPackage().getResourceId());

				log.debug("PlayerPage sco is {}", sessionBean.getScoId());

				view.synchronizeState(sessionBean, target);

				navigator.displayResource(sessionBean, null);
				return launchPanel;
			}

			log.debug("Result is {}", result);

		}
		catch (Exception e)
		{
			result = e.getMessage();
			log.error("Caught an exception: ", e);
		}

		return new ChoicePanel(lazyId, sessionBean.getContentPackage().getContentPackageId(), sessionBean.getContentPackage().getResourceId(), result);
	}

	private void loadSharedResources(String resourceId)
	{
		List<ContentPackageResource> resources = resourceService.getResources(resourceId);

		for (ContentPackageResource cpResource : resources)
		{
			String resourceName = cpResource.getPath();

			ContentPackageWebResource resource = (ContentPackageWebResource) getApplication().getSharedResources().get(ScormPlayerPage.class, resourceName, null, null, null, false);
			if (resource == null || resource.lastModifiedTime().getMilliseconds() != cpResource.getLastModified())
			{
				ContentPackageWebResource webResource = new ContentPackageWebResource(cpResource);
				log.debug("Adding a shared resource as {}", resourceName);

				getWebApplication().mountResource("play", webResource);
			}
		}
	}

	private String tryLaunch(SessionBean sessionBean, int navRequest, AjaxRequestTarget target)
	{
		String result = sequencingService.navigate(navRequest, sessionBean, null, target);

		// Success is null.
		if (result == null || result.contains(NAV_RESULT_TOC))
		{
			return null;
		}

		// If we get an invalid nav request, chances are that we need to abandon and start again
		if (result.equals(NAV_RESULT_INVALID_REQ))
		{
			IValidRequests state = sessionBean.getNavigationState();
			if (state.isSuspendEnabled())
			{
				result = sequencingService.navigate(SeqNavRequests.NAV_SUSPENDALL, sessionBean, null, target);
				if (StringUtils.equals(result, NAV_RESULT_END_SESSION))
				{
					result = sequencingService.navigate(SeqNavRequests.NAV_RESUMEALL, sessionBean, null, target);
					if (result == null || result.contains(NAV_RESULT_TOC))
					{
						return result;
					}
				}
			}
			if (StringUtils.equals(result, NAV_RESULT_INVALID_REQ))
			{
				result = sequencingService.navigate(SeqNavRequests.NAV_ABANDONALL, sessionBean, null, target);

				// If it worked, start again
				if (StringUtils.equals(result, NAV_RESULT_END_SESSION))
				{
					state = sessionBean.getNavigationState();
					result = sequencingService.navigate(SeqNavRequests.NAV_NONE, sessionBean, null, target);
					state = sessionBean.getNavigationState();

					// Only start if allowed...
					if (state.isStartEnabled())
					{
						result = sequencingService.navigate(SeqNavRequests.NAV_START, sessionBean, null, target);
					}
				}
			}
		}

		// Otherwise, we may need to issue a 'None'
		else if (result.equals("_SEQBLOCKED_"))
		{
			result = sequencingService.navigate(SeqNavRequests.NAV_NONE, sessionBean, null, target);
		}
		if (result == null || result.contains(NAV_RESULT_TOC))
		{
			sessionBean.setStarted(true);
		}

		return result;
	}

	public class LocalResourceNavigator extends ResourceNavigator
	{
		private static final long serialVersionUID = 1L;

		@Override
		protected ScormResourceService resourceService()
		{
			return LazyLaunchPanel.this.resourceService;
		}

		@Override
		public Component getFrameComponent()
		{
			return launchPanel != null ? launchPanel.getContentPanel() : null;
		}
	}
}
