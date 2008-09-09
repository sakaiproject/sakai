package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishedAssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class RepublishAssessmentListener implements ActionListener {

	private static Log log = LogFactory
			.getLog(RepublishAssessmentListener.class);

	public void processAction(ActionEvent ae) throws AbortProcessingException {
		AssessmentBean assessmentBean = (AssessmentBean) ContextUtil
				.lookupBean("assessmentBean");
		boolean hasGradingData = assessmentBean.getHasGradingData();

		String publishedAssessmentId = assessmentBean.getAssessmentId();
		log.debug("publishedAssessmentId = " + publishedAssessmentId);
		PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
		
		// Go to database to get the newly updated data. The data inside beans might not be up to date.
		PublishedAssessmentFacade assessment = publishedAssessmentService.getPublishedAssessment(publishedAssessmentId);
		EventTrackingService.post(EventTrackingService.newEvent("sam.pubassessment.republish", "publishedAssessmentId=" + publishedAssessmentId, true));

		assessment.setStatus(AssessmentBaseIfc.ACTIVE_STATUS);
		publishedAssessmentService.saveAssessment(assessment);

		// If there are submissions, need to regrade them
		if (hasGradingData) {
			regradeRepublishedAssessment(publishedAssessmentService, (PublishedAssessmentIfc) assessment);
		}

		AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
		//		 get the managed bean, author and set all the list
		GradingService gradingService = new GradingService();
		HashMap map = gradingService
				.getSubmissionSizeOfAllPublishedAssessments();

		// 1. need to update active published list in author bean
		ArrayList activePublishedList = publishedAssessmentService
				.getBasicInfoOfAllActivePublishedAssessments(author
						.getPublishedAssessmentOrderBy(), author
						.isPublishedAscending());
		author.setPublishedAssessments(activePublishedList);
		setSubmissionSize(activePublishedList, map);

		// 2. need to update inactive published list in author bean
		ArrayList inactivePublishedList = publishedAssessmentService
				.getBasicInfoOfAllInActivePublishedAssessments(author
						.getInactivePublishedAssessmentOrderBy(), author
						.isInactivePublishedAscending());
		author.setInactivePublishedAssessments(inactivePublishedList);
		setSubmissionSize(inactivePublishedList, map);

		// 3. reset the core listing
		// 'cos user may change core assessment title and publish - sigh
		AssessmentService assessmentService = new AssessmentService();
		ArrayList assessmentList = assessmentService
				.getBasicInfoOfAllActiveAssessments(author
						.getCoreAssessmentOrderBy(), author.isCoreAscending());
		// get the managed bean, author and set the list
		author.setAssessments(assessmentList);
		author.setOutcome("author");
	}

	private void setSubmissionSize(ArrayList list, HashMap map) {
		for (int i = 0; i < list.size(); i++) {
			PublishedAssessmentFacade p = (PublishedAssessmentFacade) list
					.get(i);
			Integer size = (Integer) map.get(p.getPublishedAssessmentId());
			if (size != null) {
				p.setSubmissionSize(size.intValue());
				//log.info("*** submission size" + size.intValue());
			}
		}
	}
	
	private void regradeRepublishedAssessment (PublishedAssessmentService pubService, PublishedAssessmentIfc publishedAssessment) {
		HashMap publishedItemHash = pubService.preparePublishedItemHash(publishedAssessment);
		HashMap publishedItemTextHash = pubService.preparePublishedItemTextHash(publishedAssessment);
		HashMap publishedAnswerHash = pubService.preparePublishedAnswerHash(publishedAssessment);
		PublishedAssessmentSettingsBean publishedAssessmentSettings = (PublishedAssessmentSettingsBean) ContextUtil
			.lookupBean("publishedSettings");
		// Actually we don't really need to consider linear or random here.
		// boolean randomAccessAssessment = publishedAssessmentSettings.getItemNavigation().equals("2");
		boolean updateMostCurrentSubmission = publishedAssessmentSettings.getupdateMostCurrentSubmission();
		GradingService service = new GradingService();
		List list = service.getAllAssessmentGradingData(publishedAssessment.getPublishedAssessmentId());
		Iterator iter = list.iterator();
		
		if (updateMostCurrentSubmission) {
		    String currentAgent = "";
			while (iter.hasNext()) {
				AssessmentGradingData adata = (AssessmentGradingData) iter.next();
				if (!currentAgent.equals(adata.getAgentId())){
					if (adata.getForGrade().booleanValue()) {
						adata.setForGrade(Boolean.FALSE);
					}
					currentAgent = adata.getAgentId();
				}
				service.storeGrades(adata, true, publishedAssessment, publishedItemHash, publishedItemTextHash, publishedAnswerHash, true);
			}
		}
		else {
			while (iter.hasNext()) {
				AssessmentGradingData adata = (AssessmentGradingData) iter.next();
				service.storeGrades(adata, true, publishedAssessment, publishedItemHash, publishedItemTextHash, publishedAnswerHash, true);
			}
		}
	}
}
