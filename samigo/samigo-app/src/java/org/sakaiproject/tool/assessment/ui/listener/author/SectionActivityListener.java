/**
 * Copyright (c) 2005-2017 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.ui.listener.author;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;
import javax.faces.model.SelectItem;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.facade.AgentFacade;

import org.sakaiproject.tool.assessment.services.PersistenceService;

import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.shared.api.grading.GradingSectionAwareServiceAPI;
import org.sakaiproject.tool.assessment.shared.impl.grading.GradingSectionAwareServiceImpl;
import org.sakaiproject.tool.assessment.ui.bean.author.SectionActivityBean;
import org.sakaiproject.tool.assessment.ui.bean.util.Validator;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.BeanSort;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;

@Slf4j
public class SectionActivityListener implements ActionListener, ValueChangeListener
{
    private static BeanSort bs;

    public SectionActivityListener()
    {
    }

    public void processAction(ActionEvent ae)
    {
        log.debug("*****Log: inside SectionActivityListener =debugging ActionEvent: " + ae);

        // get service and managed bean    
        GradingSectionAwareServiceAPI service = new GradingSectionAwareServiceImpl();
        SectionActivityBean sab = (SectionActivityBean) ContextUtil.lookupBean("sectionActivity");

        List<EnrollmentRecord> list = service.getAvailableEnrollments(AgentFacade.getCurrentSiteId(), AgentFacade.getAgentString());
        Map<String, String> userNamesMap = getUserIdNameMap(list);
        List<SelectItem> userNamesList = new ArrayList();
        for (Map.Entry pairs : userNamesMap.entrySet()) {
            userNamesList.add(new SelectItem((String)pairs.getKey(), (String)pairs.getValue()));
        }
        sab.setDisplayNamesList(userNamesList);

        //initial selectedUser
        if(sab.getSelectedUser() == null || "".equals(sab.getSelectedUser())) {
            for (Map.Entry pairs : userNamesMap.entrySet()){
                String firstUserId = (String)pairs.getKey();
                sab.setSelectedUser(firstUserId);
                break;
            }
        }
        List<SectionActivityData> dataList = getSectionActivityDataList(sab.getSelectedUser());

        if (ContextUtil.lookupParam("sortBy") != null && !ContextUtil.lookupParam("sortBy").trim().equals("")){
            sab.setSortType(ContextUtil.lookupParam("sortBy"));
        }

        boolean sortAscending;
        if (ContextUtil.lookupParam("sortAscending") != null && !ContextUtil.lookupParam("sortAscending").trim().equals("")){
            sortAscending = Boolean.valueOf(ContextUtil.lookupParam("sortAscending"));
            sab.setSortAscending(sortAscending);
        }

        String sortProperty = sab.getSortType();
        bs = new BeanSort(dataList, sortProperty);
        if ((sortProperty).equals("assessmentName")) {
            bs.toStringSort();
        }
        if ((sortProperty).equals("assessmentId")) {
            bs.toNumericSort();
        }
        if ((sortProperty).equals("submitDate")) {
            bs.toDateSort();
        }
        if (sab.isSortAscending()) {
            dataList = (ArrayList)bs.sort();
        }
        else {
            dataList= (ArrayList)bs.sortDesc();
        }
        sab.setSectionActivityDataList(dataList);

    }

    public List<SectionActivityData> getSectionActivityDataList(String selectedUser) {
        PublishedAssessmentService assessmentService = new PublishedAssessmentService();
        List<SectionActivityData> dataList = new ArrayList<>();
        String siteId = AgentFacade.getCurrentSiteId();
        List<AssessmentGradingData> list = assessmentService.getAllAssessmentsGradingDataByAgentAndSiteId(selectedUser, siteId);
        for (AssessmentGradingData agd : list) {
            Long publishAssessmentId = agd.getPublishedAssessmentId();
            PublishedAssessmentData publishedAssessmentData = assessmentService.getBasicInfoOfPublishedAssessment(publishAssessmentId.toString()); 
            String title = publishedAssessmentData.getTitle();
            Date submitDate = agd.getSubmittedDate();
            Double finalScore = agd.getFinalScore();
            Long assessmentGradingId = agd.getAssessmentGradingId();

            PublishedAssessmentData assessmentData =PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().loadPublishedAssessment(publishAssessmentId);

            // sectionSet of publishedAssessment is defined as lazy loading in
            // Hibernate, so we need to initialize them. Unfortunately the current
            // spring-1.0.2.jar does not support HibernateTemplate.intialize(Object)
            // so we need to do it ourselves
            Set<PublishedSectionData> sectionSet = PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().getSectionSetForAssessment(assessmentData);
            assessmentData.setSectionSet(sectionSet);

            Double maxScore = assessmentData.getTotalScore();
            Double percentage = 0.0;
            boolean notAvailableGrade = false;
            if(maxScore != null && maxScore != 0) {
                BigDecimal finalScoreBigDecimal = new BigDecimal(finalScore.toString());
                BigDecimal maxScoreBigDecimal = new BigDecimal(maxScore.toString());
                BigDecimal grade_temp = (finalScoreBigDecimal.divide(maxScoreBigDecimal, new MathContext(10))).multiply(new BigDecimal(100));
                percentage = grade_temp.doubleValue();
            } else {
                notAvailableGrade = true;
            }
            SectionActivityData sad = new SectionActivityData();
            sad.setAssessmentId(publishAssessmentId);
            sad.setAssessmentName(title);
            sad.setSubmitDate(submitDate);
            sad.setPercentage(percentage);
            sad.setCorrect(finalScore);
            sad.setTotal(maxScore);
            sad.setAssessmentGradingId(assessmentGradingId);
            sad.setNotAvailableGrade(notAvailableGrade);
            sad.setAnonymousGrading(Objects.equals(EvaluationModelIfc.ANONYMOUS_GRADING, assessmentData.getEvaluationModel().getAnonymousGrading()));
            dataList.add(sad);
        }      
        return dataList;
    }

    private Map<String, String> getUserIdNameMap(List<EnrollmentRecord> list) {
        Map<String, String> nameMap = new HashMap();
        for (EnrollmentRecord enr : list) {
            String uid = enr.getUser().getUserUid();
            String displayName = enr.getUser().getDisplayName();

            nameMap.put(uid, displayName);         
        }
        Map sortedMap = sortByValue(nameMap);
        return sortedMap;
    }

    private Map sortByValue(Map map) {
        List list = new LinkedList(map.entrySet());
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((String)((Map.Entry) (o1)).getValue()).toLowerCase()).compareTo(((String)((Map.Entry) (o2)).getValue()).toLowerCase());
            }
        });

        Map result = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public void processValueChange(ValueChangeEvent event)
    {
        SectionActivityBean sab = (SectionActivityBean) ContextUtil.lookupBean("sectionActivity");
        String selectUser = (String)event.getNewValue();
        sab.setSelectedUser(selectUser);
        sab.setSortAscending(true);
        sab.setSortType("assessmentName");
        List<SectionActivityData> dataList = getSectionActivityDataList(sab.getSelectedUser());
        bs = new BeanSort(dataList, "assessmentName");
        dataList = (ArrayList)bs.sort();
        sab.setSectionActivityDataList(dataList);
    }

    public class SectionActivityData implements Serializable{
        private static final long serialVersionUID = 1L;
        private Long assessmentId;
        private String assessmentName;
        private Date submitDate;
        private Double percentage;
        private Double correct;
        private Double total;
        private Long assessmentGradingId;
        private boolean notAvailableGrade;
        private boolean anonymousGrading;

        public Long getAssessmentId() {
            return assessmentId;
        }

		public void setAssessmentId(Long assessmentId) {
            this.assessmentId = assessmentId;
        }
        public String getAssessmentName() {
            return assessmentName;
        }
        public void setAssessmentName(String assessmentName) {
            this.assessmentName = assessmentName;
        }
        public Date getSubmitDate() {
            return submitDate;
        }
        public void setSubmitDate(Date submitDate) {
            this.submitDate = submitDate;
        }
        public Double getPercentage() {
            return percentage;
        }
        public void setPercentage(Double percentage) {
            this.percentage = percentage;
        }
        public Double getCorrect() {
            return correct;
        }
        public void setCorrect(Double correct) {
            this.correct = correct;
        }
        public Double getTotal() {
            return total;
        }
        public void setTotal(Double total) {
            this.total = total;
        }
        public Long getAssessmentGradingId() {
            return assessmentGradingId;
        }
        public void setAssessmentGradingId(Long assessmentGradingId) {
            this.assessmentGradingId = assessmentGradingId;
        }
        public boolean isNotAvailableGrade() {
            return notAvailableGrade;
        }
        public void setNotAvailableGrade(boolean notAvailableGrade) {
            this.notAvailableGrade = notAvailableGrade;
        }
        public boolean isAnonymousGrading() {
            return anonymousGrading;
        }
        public void setAnonymousGrading(boolean anonymousGrading) {
            this.anonymousGrading = anonymousGrading;
        }

        //This is borrowed from Total Score page for grade display. We'd like to keep consistent for displaying the grade in all pages.
        public String getRoundedFinalScore() {
            if (correct!= null){
                try {
                    String newscore = ContextUtil.getRoundedValue(correct.toString().replace(',', '.'), 2);
                    return Validator.check(newscore, "N/A").replace(',', '.');
                }
                catch (Exception e) {
                    return Validator.check(correct.toString(), "0").replace(',', '.');
                }
            }
            return "0";
        }

        public String getRoundedPercentage() {
            if (percentage!= null){
                try {
                    String newscore = ContextUtil.getRoundedValue(percentage.toString().replace(',', '.'), 2);
                    return Validator.check(newscore, "N/A").replace(',', '.');
                }
                catch (Exception e) {
                    return Validator.check(percentage.toString(), "0").replace(',', '.');
                }
            }
            return "0";
        }

        public String getRoundedMaxScore() {
            if (total!= null){
                try {
                    String newscore = ContextUtil.getRoundedValue(total.toString().replace(',', '.'), 2);
                    return Validator.check(newscore, "N/A").replace(',', '.');
                }
                catch (Exception e) {
                    return Validator.check(total.toString(), "0").replace(',', '.');
                }
            }
            return "0";
        }
    }
}
