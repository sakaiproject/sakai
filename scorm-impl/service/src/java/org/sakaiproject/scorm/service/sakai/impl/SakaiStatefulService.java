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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.scorm.api.ScormConstants;
import org.sakaiproject.scorm.dao.LearnerDao;
import org.sakaiproject.scorm.exceptions.LearnerNotDefinedException;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.Learner;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormResultService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

public abstract class SakaiStatefulService implements LearningManagementSystem, ScormConstants {

	private static Log log = LogFactory.getLog(SakaiStatefulService.class);

	public boolean canConfigure(String context) {
		return hasPermission(context, "scorm.configure");
	}

	public boolean canDelete(String context) {
		return hasPermission(context, "scorm.delete");
	}

	public boolean canGrade(String context) {
		return hasPermission(context, "scorm.grade");
	}
	
	public boolean canLaunch(ContentPackage contentPackage) {
		return canLaunchAttemptInternal(contentPackage, -1);
	}

	public boolean canLaunchAttempt(ContentPackage contentPackage, long attemptNumber) {
		return canLaunchAttemptInternal(contentPackage, attemptNumber);
	}

	protected boolean canLaunchAttemptInternal(ContentPackage contentPackage, long attemptNumber) {
	    if (contentPackage == null) {
			return false;
		}

		String context = contentPackage.getContext();
		if (canModify(context)) {
			return true;
		} else if (hasPermission(context, "scorm.launch")) {

			int status = scormContentService().getContentPackageStatus(contentPackage);
			if (status != ScormConstants.CONTENT_PACKAGE_STATUS_OPEN && status != ScormConstants.CONTENT_PACKAGE_STATUS_OVERDUE) {
				return false;
			}
			if (attemptNumber == -1) {
				attemptNumber = scormResultService().countAttempts(contentPackage.getContentPackageId(), currentLearnerId());
			}
			// If the numberOfTries is not Unlimited then verify that we haven't hit the max
			if (contentPackage.getNumberOfTries() != -1 && attemptNumber != -1) {
				// attemptNumber starts at 1, so no + 1 needed here. 
				if (attemptNumber > contentPackage.getNumberOfTries()) {
					return false;
				}
			}
		}
		return true;
    }

	public boolean canLaunchNewWindow() {
		return true;
	}

	public boolean canModify(String context) {
		return canConfigure(context) || canDelete(context) || canGrade(context);
	}

	public boolean canUpload(String context) {
		return hasPermission(context, "scorm.upload");
	}

	public boolean canUseRelativeUrls() {
		return false;
	}

	public boolean canValidate(String context) {
		return hasPermission(context, "scorm.validate");
	}

	public boolean canViewResults(String context) {
		return hasPermission(context, "scorm.view.results");
	}

	protected abstract ServerConfigurationService configurationService();

	public String currentContext() {
		return toolManager().getCurrentPlacement().getContext();
	}

	public String currentLearnerId() {
		String learnerId = sessionManager().getCurrentSessionUserId();

		return learnerId;
	}

	public Learner getLearner(String learnerId) throws LearnerNotDefinedException {
		return learnerDao().load(learnerId);
	}

	public String getLearnerName(String learnerId) {
		String displayName = null;
		try {
			User user = userDirectoryService().getUser(learnerId);

			if (user != null) {
				displayName = user.getDisplayName();
			}

		} catch (UserNotDefinedException e) {
			log.error("Could not determine display name for user " + learnerId, e);
		}

		return displayName;
	}

	protected boolean hasPermission(String context, String lock) {
		String reference = siteService().siteReference(context);

		return unlockCheck(lock, reference);
	}

	public boolean isOwner() {
		return true;
	}

	protected abstract LearnerDao learnerDao();

	protected abstract ScormContentService scormContentService();

	protected abstract ScormResultService scormResultService();

	protected abstract SecurityService securityService();

	protected abstract SessionManager sessionManager();

	protected abstract SiteService siteService();

	protected abstract ToolManager toolManager();

	protected boolean unlockCheck(String lock, String ref) {
		boolean isAllowed = securityService().isSuperUser();
		if (!isAllowed) {
			// make a reference from the resource id, if specified
			/*String ref = null;
			if (id != null)
			{
				ref = siteService().siteReference(id);
			}*/

			isAllowed = ref != null && securityService().unlock(lock, ref);
		}

		return isAllowed;

	}

	protected abstract UserDirectoryService userDirectoryService();

}
