/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/tags/sakai_2-4-0/samigo-app/src/java/org/sakaiproject/tool/assessment/ui/listener/author/PublishAssessmentNotificationListener.java $
 * $Id: ReorderPartsListener.java 16897 2006-10-09 00:28:33Z ktsao@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
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

package org.sakaiproject.tool.assessment.ui.listener.author;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishRepublishNotificationBean;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishedAssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @version $Id: PublishAssessmentNotificationListener.java 16897 2006-10-09 00:28:33Z ktsao@stanford.edu $
 */

public class PublishRepublishNotificationListener implements ValueChangeListener {
	private static Log log = LogFactory.getLog(PublishRepublishNotificationListener.class);

	/**
	 * Standard process action method.
	 * @param ae ValueChangeEvent
	 * @throws AbortProcessingException
	 */
	public void processValueChange(ValueChangeEvent ae)
	throws AbortProcessingException {
		AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
		String currentSiteId = "";
		String title = "";
		String startDateString = "";
		PublishRepublishNotificationBean publishRepublishNotification = (PublishRepublishNotificationBean) ContextUtil.lookupBean("publishRepublishNotification");
		boolean isEditPendingAssessmentFlow =  author.getIsEditPendingAssessmentFlow();
		if (isEditPendingAssessmentFlow) {
			AssessmentService assessmentService = new AssessmentService();
			AssessmentSettingsBean assessmentSettings = (AssessmentSettingsBean) ContextUtil.lookupBean("assessmentSettings");
			currentSiteId = assessmentService.getAssessmentSiteId(assessmentSettings.getAssessmentId().toString());
			title = assessmentSettings.getTitle();
			startDateString = assessmentSettings.getStartDateString();
		}
		else {
			PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
			PublishedAssessmentSettingsBean publishedAssessmentSettings = (PublishedAssessmentSettingsBean) ContextUtil.lookupBean("publishedSettings");
			currentSiteId = publishedAssessmentService.getPublishedAssessmentOwner(publishedAssessmentSettings.getAssessmentId());
			title = publishedAssessmentSettings.getTitle();
			startDateString = publishedAssessmentSettings.getStartDateString();
		}
		String newPos = ae.getNewValue().toString();
		log.debug("**** ae.getNewValue : " + newPos);

		if ("2".equals(newPos)) {
			// set Subject
			String siteTitle = "";
			try
			{
				Site site = SiteService.getSite(currentSiteId);
				siteTitle = site.getTitle();
			}
			catch (Exception ignore)
			{
			}

			publishRepublishNotification.setSendNotification(true);
			StringBuilder subject = new StringBuilder("[");
			subject.append(siteTitle);
			subject.append("] \"");
			subject.append(title);
			subject.append("\" ");
			if (isEditPendingAssessmentFlow) {
				if (startDateString == null || startDateString.trim().equals("")) {
					subject.append(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages", "is_available_immediately"));
				}
				else {
					subject.append(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages", "will_be_available_on"));
					subject.append(" ");
					subject.append(startDateString);
				}
			}
			else {
				subject.append(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages", "was_republished"));
			}
			publishRepublishNotification.setNotificationSubject(subject.toString());
			publishRepublishNotification.setSiteTitle(siteTitle);
		}
		else {
			publishRepublishNotification.setSendNotification(false);
		}
	}
}
