package org.sakaiproject.tool.assessment.ui.listener.evaluation;

import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.AgentResults;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.StudentScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.SubmissionNavBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.TotalScoresBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

@Slf4j
public class SubmissionNavListener implements ValueChangeListener  {


    public void processValueChange(ValueChangeEvent valueChangeEvent) throws AbortProcessingException {
        String gradingId = (String) valueChangeEvent.getNewValue();
        log.debug("gradingId: [{}]", gradingId);

        submissionNav(gradingId);

        StudentScoresBean studentScoresBean = (StudentScoresBean) ContextUtil.lookupBean("studentScores");
        StudentScoreListener studentScoreListener = new StudentScoreListener();
        studentScoreListener.studentScores(studentScoresBean.getPublishedId(),
                gradingId, studentScoresBean.getItemId(), false);
    }

    public void submissionNav(String gradingId) {
        TotalScoresBean totalScoresBean = (TotalScoresBean) ContextUtil.lookupBean("totalScores");
        DeliveryBean deliveryBean = (DeliveryBean) ContextUtil.lookupBean("delivery");
        SubmissionNavBean submissionNavBean = (SubmissionNavBean) ContextUtil.lookupBean("submissionNav");

        List<AgentResults> agents = new ArrayList<>(totalScoresBean.getAllAgents());
        boolean displaySubmissionDate = StringUtils.equals(TotalScoresBean.ALL_SUBMISSIONS, totalScoresBean.getAllSubmissions());
        submissionNavBean.populate(agents, gradingId, displaySubmissionDate);

        deliveryBean.setNextAssessmentGradingId(Long.valueOf(gradingId));

        FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(
                FacesContext.getCurrentInstance(), null, "studentScores");
    }
}
