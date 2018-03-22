/**********************************************************************************
 * $URL$
 * $Id$
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
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.print.PDFAssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.shared.PersonBean;
import org.sakaiproject.tool.assessment.ui.listener.delivery.BeginDeliveryActionListener;
import org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener;
import org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @version $Id$
 */
@Slf4j
public class ActionSelectListener implements ActionListener {

	/**
	 * Standard process action method.
	 * @param ae ActionEvent
	 * @throws AbortProcessingException
	 */
    @Override
    public void processAction(ActionEvent ae) throws AbortProcessingException {
		AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
		DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBean("delivery");
		PersonBean person = (PersonBean) ContextUtil.lookupBean("person");
		String publishedID = ContextUtil.lookupParam( "publishedId" );
		String action = ContextUtil.lookupParam( "action" );
		log.debug("**** action : " + action);

		if ("edit_pending".equals(action)) {
			EditAssessmentListener editAssessmentListener = new EditAssessmentListener();
			editAssessmentListener.processAction(null);
			author.setFirstFromPage("editAssessment");
			author.setJustPublishedAnAssessment(false);
		}
		else if ("preview_pending".equals(action)) {
			delivery.setActionString("previewAssessment");
			delivery.setIsFromPrint(false);
			author.setIsEditPendingAssessmentFlow(true);
			person.setPreviewFromPage("author");
			BeginDeliveryActionListener beginDeliveryActionListener = new BeginDeliveryActionListener();
			beginDeliveryActionListener.processAction(null);
			author.setOutcome("beginAssessment");
			author.setJustPublishedAnAssessment(false);
		}
		else if ("print_pending".equals(action) || "print_published".equals(action)) {
			delivery.setActionString("previewAssessment");
			delivery.setIsFromPrint(true);
			author.setIsEditPendingAssessmentFlow(true);
			if ("print_published".equals(action)) {
				author.setIsEditPendingAssessmentFlow(false);
				author.setJustPublishedAnAssessment(true);
			}
			else {
				author.setJustPublishedAnAssessment(false);
			}
			PDFAssessmentBean pdfBean = (PDFAssessmentBean)ContextUtil.lookupBean("pdfAssessment");
			pdfBean.prepPDF();
			pdfBean.setActionString("author");
			author.setOutcome("print");
		}
		else if ("settings_pending".equals(action)) {
			AuthorSettingsListener authorSettingsListener = new AuthorSettingsListener();
			authorSettingsListener.processAction(null);
			author.setFromPage("author");
			author.setFirstFromPage("author");
			author.setJustPublishedAnAssessment(false);
		}
		else if ("publish".equals(action)) {
			AuthorSettingsListener authorSettingsListener = new AuthorSettingsListener();
			authorSettingsListener.processAction(null);
			author.setIsErrorInSettings(false);
			ConfirmPublishAssessmentListener confirmPublishAssessmentListener = new ConfirmPublishAssessmentListener();
			confirmPublishAssessmentListener.setIsFromActionSelect(true);
			confirmPublishAssessmentListener.processAction(null);
			if (author.getIsErrorInSettings()) {
				author.setOutcome("editAssessmentSettings");	
			}
			else {
				PublishAssessmentListener publishAssessmentListener = new PublishAssessmentListener();
				publishAssessmentListener.processAction(null);
				author.setOutcome("saveSettingsAndConfirmPublish");		
			}
			author.setFromPage("author");
			author.setFirstFromPage("author");
		}
		else if ("duplicate".equals(action)) {
			ConfirmCopyAssessmentListener confirmCopyAssessmentListener = new ConfirmCopyAssessmentListener();
			confirmCopyAssessmentListener.processAction(null);
			author.setOutcome("confirmCopyAssessment");
			author.setJustPublishedAnAssessment(false);
		}
		else if ("export".equals(action)) {
			ChooseExportTypeListener chooseExportTypeListener = new ChooseExportTypeListener();
			chooseExportTypeListener.processAction(null);
			author.setOutcome("chooseExportType");
			author.setJustPublishedAnAssessment(false);
		}
		else if ("remove_selected".equals(action)) {
			RemoveAssessmentListener removeAssessmentListener = new RemoveAssessmentListener();
			removeAssessmentListener.processAction(null);
			author.setJustPublishedAnAssessment(false);
		}
		else if ("scores".equals(action)) {
			delivery.setActionString("gradeAssessment");
			ResetTotalScoreListener resetTotalScoreListener = new ResetTotalScoreListener();
			resetTotalScoreListener.processAction(null);
			TotalScoreListener totalScoreListener = new TotalScoreListener();
			totalScoreListener.processAction(null);
			author.setJustPublishedAnAssessment(true);
		}
		else if ("edit_published".equals(action)) {
			ConfirmEditPublishedAssessmentListener confirmEditPublishedAssessmentListener = new ConfirmEditPublishedAssessmentListener();
			confirmEditPublishedAssessmentListener.processAction(null);
			author.setOutcome("confirmEditPublishedAssessment");
			author.setFromPage("author");
			author.setEditPublishedAssessmentID( publishedID );
			author.setJustPublishedAnAssessment(true);
		}
		else if ("preview_published".equals(action)) {
			delivery.setActionString("previewAssessment");
			author.setIsEditPendingAssessmentFlow(false);
			person.setPreviewFromPage("author");
			BeginDeliveryActionListener beginDeliveryActionListener = new BeginDeliveryActionListener();
			beginDeliveryActionListener.processAction(null);
			author.setOutcome("beginAssessment");
			author.setJustPublishedAnAssessment(true);
		}
		else if ("settings_published".equals(action)) {
			EditPublishedSettingsListener editPublishedSettingsListener = new EditPublishedSettingsListener();
			editPublishedSettingsListener.processAction(null);
			author.setJustPublishedAnAssessment(true);
		}
	}
}
