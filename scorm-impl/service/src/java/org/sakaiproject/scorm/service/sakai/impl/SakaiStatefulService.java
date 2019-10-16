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
package org.sakaiproject.scorm.service.sakai.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import static org.sakaiproject.scorm.api.ScormConstants.*;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.scorm.dao.LearnerDao;
import org.sakaiproject.scorm.exceptions.LearnerNotDefinedException;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.Learner;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormResultService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

@Slf4j
public abstract class SakaiStatefulService implements LearningManagementSystem
{
	protected abstract ServerConfigurationService configurationService();
	protected abstract LearnerDao learnerDao();
	protected abstract ScormContentService scormContentService();
	protected abstract ScormResultService scormResultService();
	protected abstract SecurityService securityService();
	protected abstract SessionManager sessionManager();
	protected abstract SiteService siteService();
	protected abstract ToolManager toolManager();
	protected abstract UserDirectoryService userDirectoryService();

	@Getter @Setter
	private UserTimeService userTimeService;

	@Override
	public boolean canConfigure(String context)
	{
		return hasPermission(context, PERM_CONFIG);
	}

	@Override
	public boolean canDelete(String context)
	{
		return hasPermission(context, PERM_DELETE);
	}

	@Override
	public boolean canGrade(String context)
	{
		return hasPermission(context, PERM_GRADE);
	}

	@Override
	public boolean canLaunch(ContentPackage contentPackage)
	{
		return canLaunchAttemptInternal(contentPackage, -1);
	}

	@Override
	public boolean canLaunchAttempt(ContentPackage contentPackage, long attemptNumber)
	{
		return canLaunchAttemptInternal(contentPackage, attemptNumber);
	}

	protected boolean canLaunchAttemptInternal(ContentPackage contentPackage, long attemptNumber)
	{
		if (contentPackage == null)
		{
			return false;
		}

		String context = contentPackage.getContext();
		if (canModify(context))
		{
			return true;
		}
		else if (hasPermission(context, PERM_LAUNCH))
		{
			int status = scormContentService().getContentPackageStatus(contentPackage);
			if (status != CONTENT_PACKAGE_STATUS_OPEN && status != CONTENT_PACKAGE_STATUS_OVERDUE)
			{
				return false;
			}

			if (attemptNumber == -1)
			{
				attemptNumber = scormResultService().countAttempts(contentPackage.getContentPackageId(), currentLearnerId());
			}

			// If the numberOfTries is not Unlimited then verify that we haven't hit the max
			if (contentPackage.getNumberOfTries() != -1 && attemptNumber != -1)
			{
				// attemptNumber starts at 1, so no + 1 needed here. 
				if (attemptNumber > contentPackage.getNumberOfTries())
				{
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public boolean canLaunchNewWindow()
	{
		return true;
	}

	@Override
	public boolean canModify(String context)
	{
		return canConfigure(context) || canDelete(context) || canGrade(context);
	}

	@Override
	public boolean canUpload(String context)
	{
		return hasPermission(context, PERM_UPLOAD);
	}

	@Override
	public boolean canUseRelativeUrls()
	{
		return false;
	}

	@Override
	public boolean canValidate(String context)
	{
		return hasPermission(context, PERM_VALIDATE);
	}

	@Override
	public boolean canViewResults(String context)
	{
		return hasPermission(context, PERM_VIEW_RESULTS);
	}

	@Override
	public String currentContext()
	{
		return toolManager().getCurrentPlacement().getContext();
	}

	@Override
	public String currentLearnerId()
	{
		return sessionManager().getCurrentSessionUserId();
	}

	@Override
	public Learner getLearner(String learnerId) throws LearnerNotDefinedException
	{
		return learnerDao().load(learnerId);
	}

	@Override
	public String getLearnerName(String learnerId)
	{
		String displayName = null;
		try
		{
			User user = userDirectoryService().getUser(learnerId);
			if (user != null)
			{
				displayName = user.getDisplayName();
			}
		} 
		catch (UserNotDefinedException e)
		{
			log.error("Could not determine display name for user {}", learnerId, e);
		}

		return displayName;
	}

	protected boolean hasPermission(String context, String lock)
	{
		String reference = siteService().siteReference(context);
		return unlockCheck(lock, reference);
	}

	@Override
	public boolean isOwner()
	{
		return true;
	}

	protected boolean unlockCheck(String lock, String ref)
	{
		boolean isAllowed = securityService().isSuperUser();
		if (!isAllowed)
		{
			isAllowed = ref != null && securityService().unlock(lock, ref);
		}

		return isAllowed;
	}
}
