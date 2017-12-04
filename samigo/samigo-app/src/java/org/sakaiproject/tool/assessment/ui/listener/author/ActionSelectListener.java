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
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;

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
public class ActionSelectListener implements ValueChangeListener {

	/**
	 * Standard process action method.
	 * @param ae ValueChangeEvent
	 * @throws AbortProcessingException
	 */
	public void processValueChange(ValueChangeEvent ae)
			throws AbortProcessingException {
		AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
		DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBean("delivery");
		PersonBean person = (PersonBean) ContextUtil.lookupBean("person");
		String newValue = ae.getNewValue().toString();
		String publishedID = ContextUtil.lookupParam( "publishedId" );
		log.debug("**** ae.getNewValue : " + newValue);

		
		if ("edit_pending".equals(newValue)) {
			EditAssessmentListener editAssessmentListener = new EditAssessmentListener();
			editAssessmentListener.processAction(null);
			author.setFirstFromPage("editAssessment");
			author.setJustPublishedAnAssessment(false);
		}
		else if ("preview_pending".equals(newValue)) {
			delivery.setActionString("previewAssessment");
			delivery.setIsFromPrint(false);
			author.setIsEditPendingAssessmentFlow(true);
			person.setPreviewFromPage("author");
			BeginDeliveryActionListener beginDeliveryActionListener = new BeginDeliveryActionListener();
			beginDeliveryActionListener.processAction(null);
			author.setOutcome("beginAssessment");
			author.setJustPublishedAnAssessment(false);
		}
		else if ("print_pending".equals(newValue) || "print_published".equals(newValue)) {
			delivery.setActionString("previewAssessment");
			delivery.setIsFromPrint(true);
			author.setIsEditPendingAssessmentFlow(true);
			if ("print_published".equals(newValue)) {
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
		else if ("settings_pending".equals(newValue)) {
			AuthorSettingsListener authorSettingsListener = new AuthorSettingsListener();
			authorSettingsListener.processAction(null);
			author.setFromPage("author");
			author.setFirstFromPage("author");
			author.setJustPublishedAnAssessment(false);
		}
		else if ("publish".equals(newValue)) {
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
		else if ("duplicate".equals(newValue)) {
			ConfirmCopyAssessmentListener confirmCopyAssessmentListener = new ConfirmCopyAssessmentListener();
			confirmCopyAssessmentListener.processAction(null);
			author.setOutcome("confirmCopyAssessment");
			author.setJustPublishedAnAssessment(false);
		}
		else if ("export".equals(newValue)) {
			ChooseExportTypeListener chooseExportTypeListener = new ChooseExportTypeListener();
			chooseExportTypeListener.processAction(null);
			author.setOutcome("chooseExportType");
			author.setJustPublishedAnAssessment(false);
		}
		else if ("remove_pending".equals(newValue)) {
			ConfirmRemoveAssessmentListener confirmRemoveAssessmentListener = new ConfirmRemoveAssessmentListener();
			confirmRemoveAssessmentListener.processAction(null);
			author.setJustPublishedAnAssessment(false);
		}
		else if ("scores".equals(newValue)) {
			delivery.setActionString("gradeAssessment");
			ResetTotalScoreListener resetTotalScoreListener = new ResetTotalScoreListener();
			resetTotalScoreListener.processAction(null);
			TotalScoreListener totalScoreListener = new TotalScoreListener();
			totalScoreListener.processAction(null);
			author.setJustPublishedAnAssessment(true);
		}
		else if ("edit_published".equals(newValue)) {
			ConfirmEditPublishedAssessmentListener confirmEditPublishedAssessmentListener = new ConfirmEditPublishedAssessmentListener();
			confirmEditPublishedAssessmentListener.processAction(null);
			author.setOutcome("confirmEditPublishedAssessment");
			author.setFromPage("author");
			author.setEditPublishedAssessmentID( publishedID );
			author.setJustPublishedAnAssessment(true);
		}
		else if ("preview_published".equals(newValue)) {
			delivery.setActionString("previewAssessment");
			author.setIsEditPendingAssessmentFlow(false);
			person.setPreviewFromPage("author");
			BeginDeliveryActionListener beginDeliveryActionListener = new BeginDeliveryActionListener();
			beginDeliveryActionListener.processAction(null);
			author.setOutcome("beginAssessment");
			author.setJustPublishedAnAssessment(true);
		}
		else if ("settings_published".equals(newValue)) {
			EditPublishedSettingsListener editPublishedSettingsListener = new EditPublishedSettingsListener();
			editPublishedSettingsListener.processAction(null);
			author.setJustPublishedAnAssessment(true);
		}
		else if ("remove_published".equals(newValue)) {
			ConfirmRemovePublishedAssessmentListener confirmRemovePublishedAssessmentListener = new ConfirmRemovePublishedAssessmentListener();
			confirmRemovePublishedAssessmentListener.processAction(null);
			author.setJustPublishedAnAssessment(true);
		}
	}
}
