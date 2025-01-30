/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.model.SelectItem;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionMetaDataIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.SectionBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.util.api.FormattedText;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */
public class EditPartListener
    implements ActionListener
{
  private boolean isEditPendingAssessmentFlow = true;
  
  public EditPartListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
	FacesContext context = FacesContext.getCurrentInstance();
	AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
    SectionBean sectionBean = (SectionBean) ContextUtil.lookupBean("sectionBean");
    AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
    AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");
    isEditPendingAssessmentFlow = author.getIsEditPendingAssessmentFlow();
    String sectionId = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("sectionId");
    
    if (sectionId != null){
	  sectionBean.setSectionId(sectionId);
    }
    else {
	  // i am afraid on returning from removal, EditPartListener is accled to re-populate the part so i can't read sectionId from a form. - daisyf
	  sectionId = sectionBean.getSectionId();
    }

    // #1a. prepare sectionBean
    AssessmentService assessmentService = null;
    SectionFacade section = null;

    List<SelectItem> sectionList = assessmentBean.getSectionList();
    boolean foundPart = false;
    for (int i=0; i<sectionList.size();i++){
      SelectItem s = sectionList.get(i);
      
      // only pick this part if it's actually in current assessment
      if (sectionId.equals((String)s.getValue())) foundPart = true;
    }

    // Permission check
    if ((!foundPart) || !authzBean.isUserAllowedToEditAssessment(assessmentBean.getAssessmentId(), assessmentBean.getAssessment().getCreatedBy(), !isEditPendingAssessmentFlow)) {
      throw new IllegalArgumentException("Permission check for sectionId failed: " + sectionId);
    }

    if (isEditPendingAssessmentFlow) {
    	EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_REVISE, "siteId=" + AgentFacade.getCurrentSiteId() + ", sectionId=" + sectionId, true));
    	assessmentService = new AssessmentService();
    }
    else {
    	EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_PUBLISHED_ASSESSMENT_REVISE, "siteId=" + AgentFacade.getCurrentSiteId() + ", sectionId=" + sectionId, true));
    	assessmentService = new PublishedAssessmentService();
    }
    section = assessmentService.getSection(sectionId);
    section.setAssessment(assessmentBean.getAssessment());
    sectionBean.setSection(section);
    sectionBean.setSectionTitle(ComponentManager.get(FormattedText.class).convertFormattedTextToPlaintext(section.getTitle()));
    sectionBean.setSectionDescription(section.getDescription());

    sectionBean.setNoOfItems(String.valueOf(section.getItemSet().size()));
    populateMetaData(section, sectionBean);

    boolean hideRandom = false;
    if ((sectionBean.getType() == null) || sectionBean.getType().equals(SectionDataIfc.QUESTIONS_AUTHORED_ONE_BY_ONE.toString())) {
      int itemsize = Integer.parseInt(sectionBean.getNoOfItems());
      if( itemsize > 0) {
        hideRandom = true;
      }
    }
    sectionBean.setHideRandom(hideRandom);
  }

  private void populateMetaData(SectionFacade section, SectionBean bean)  {
    Set<SectionMetaDataIfc> metaDataSet= section.getSectionMetaDataSet();
    // we need to order the labels to pick the n first random selected
    List<SectionMetaDataIfc> orderedMetadata = metaDataSet.stream().sorted(Comparator.comparing(SectionMetaDataIfc::getLabel)).collect(Collectors.toList());

    // reset to null
    bean.setKeyword(null);
    bean.setObjective(null);
    bean.setRubric(null);
    boolean isRandomizationTypeSet = false;
    boolean isPointValueHasOverrided = false;
    boolean isDiscountValueHasOverrided = false;
    FormattedText formattedText = ComponentManager.get(FormattedText.class);
    List<String> selectedQuestionsFixed = new ArrayList<>();
    List<String> selectedPools = new ArrayList<>();
    int selectedCount = 0;
    for (SectionMetaDataIfc meta : orderedMetadata) {
       if (meta.getLabel().equals(SectionMetaDataIfc.OBJECTIVES)){
         bean.setObjective(formattedText.convertFormattedTextToPlaintext(meta.getEntry()));
       }
       if (meta.getLabel().equals(SectionMetaDataIfc.KEYWORDS)){
         bean.setKeyword(formattedText.convertFormattedTextToPlaintext(meta.getEntry()));
       }
       if (meta.getLabel().equals(SectionMetaDataIfc.RUBRICS)){
         bean.setRubric(formattedText.convertFormattedTextToPlaintext(meta.getEntry()));
       }

       if (meta.getLabel().equals(SectionDataIfc.AUTHOR_TYPE)){
         bean.setType(meta.getEntry());
       }

       if (meta.getLabel().equals(SectionDataIfc.QUESTIONS_ORDERING)){
         bean.setQuestionOrdering(meta.getEntry());
       }

       if (meta.getLabel().equals(SectionDataIfc.POOLID_FOR_RANDOM_DRAW)){
         bean.setSelectedPool(meta.getEntry());
       }

       if (meta.getLabel().equals(SectionDataIfc.POOLID_FOR_FIXED_AND_RANDOM_DRAW)){
           bean.setSelectedPoolFixed(meta.getEntry());
           Long selectedPoolId = Long.parseLong(meta.getEntry());
           QuestionPoolService qpservice = new QuestionPoolService();
           String agentId = AgentFacade.getAgentString();
           QuestionPoolFacade qp = qpservice.getPool(selectedPoolId, agentId);
           bean.setAllItems(new ArrayList(qp.getQuestions()));
       }

       if (meta.getLabel().startsWith(SectionDataIfc.FIXED_QUESTION_IDS)){
           selectedQuestionsFixed.add(meta.getEntry());
       }

       if (meta.getLabel().equals(SectionDataIfc.NUM_QUESTIONS_FIXED)){
           bean.setNumberSelectedFixed(meta.getEntry());
       }

       if (meta.getLabel().startsWith(SectionDataIfc.POOLID_FOR_RANDOM_DRAW)){
         selectedPools.add(meta.getEntry());
       }

       if (meta.getLabel().startsWith(SectionDataIfc.RANDOM_POOL_COUNT)){
         selectedCount = Integer.valueOf(meta.getEntry());
       }

       if (meta.getLabel().equals(SectionDataIfc.NUM_QUESTIONS_DRAWN)){
         bean.setNumberSelected(meta.getEntry());
       }
       
       if (meta.getLabel().equals(SectionDataIfc.RANDOMIZATION_TYPE)){
           bean.setRandomizationType(meta.getEntry());
           isRandomizationTypeSet = true;
       }
       
       if (meta.getLabel().equals(SectionDataIfc.POINT_VALUE_FOR_QUESTION)){
    	   if (meta.getEntry() != null && !meta.getEntry().equals("")) {
    		   bean.setPointValueHasOverrided(true);
    		   isPointValueHasOverrided = true;
    	   }
           bean.setRandomPartScore(meta.getEntry());
       }
       if (meta.getLabel().equals(SectionDataIfc.DISCOUNT_VALUE_FOR_QUESTION)){
    	   if (meta.getEntry() != null && !meta.getEntry().equals("")) {
    		   bean.setDiscountValueHasOverrided(true);
    		   isDiscountValueHasOverrided = true;
    	   }
    	   bean.setRandomPartDiscount(meta.getEntry());
       }
       if (meta.getLabel().equals(SectionMetaDataIfc.TIMED)){
           if(StringUtils.isNotBlank(meta.getEntry()) && !meta.getEntry().equalsIgnoreCase("false")){
               bean.setTimedSection(true);
               try {
                   bean.setTimeLimit(Integer.valueOf(meta.getEntry()));
               }catch(NumberFormatException ex) {
                   bean.setTimeLimit(0); 
               }
           } else {
               bean.setTimedSection(false);
           }
       }
    }
    
    if (!selectedQuestionsFixed.isEmpty()) {
       bean.setFixedQuestionIds(selectedQuestionsFixed.toArray(String[]::new));
    }
    
    if (selectedPools.size() > 1) {
        bean.setSelectedPoolsMultiple(selectedPools.subList(0, selectedCount).toArray(String[]::new));// metadata are ordered
    } else {
        bean.setSelectedPoolsMultiple(null);
    }

    if (!isRandomizationTypeSet) {
 	   bean.setRandomizationType(SectionDataIfc.PER_SUBMISSION);
    }
    if (!isPointValueHasOverrided) {
        bean.setPointValueHasOverrided(false);
        bean.setRandomPartScore(null);
    }
    if (!isDiscountValueHasOverrided) {
    	bean.setDiscountValueHasOverrided(false);
    	bean.setRandomPartDiscount(null);
    }
  }

 }

