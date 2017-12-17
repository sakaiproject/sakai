/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionMetaDataIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.facade.TypeFacade;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.SectionBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.TextFormat;
import org.sakaiproject.util.FormattedText;

/**
 * <p>Title: Samigo</p>2
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */
@Slf4j
public class SavePartListener
    implements ActionListener
{
  private boolean isEditPendingAssessmentFlow;

  public SavePartListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    FacesContext context = FacesContext.getCurrentInstance();
    //Map reqMap = context.getExternalContext().getRequestMap();
    //Map requestParams = context.getExternalContext().getRequestParameterMap();

    AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean(
								   "assessmentBean");
    String assessmentId = assessmentBean.getAssessmentId();

    SectionBean sectionBean= (SectionBean) ContextUtil.lookupBean(
                         "sectionBean");
    // create an assessment based on the title entered and the assessment
    // template selected
    // #1 - read from form editpart.jsp
    String title = TextFormat.convertPlaintextToFormattedTextNoHighUnicode(sectionBean.getSectionTitle()).trim();
    if(title == null || title.equals("")){
    	String err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "empty_part_title_error");
    	context.addMessage(null, new FacesMessage(err));
    	sectionBean.setOutcome("editPart");
        return;
    }
    
    String description = sectionBean.getSectionDescription();
    String sectionId = sectionBean.getSectionId();
    
    AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
    isEditPendingAssessmentFlow = author.getIsEditPendingAssessmentFlow();
	
    // #1a. prepare sectionBean
    AssessmentService assessmentService = null;
    SectionFacade section = null;
    
    // permission check
    String creator;
    if (isEditPendingAssessmentFlow)
    {
        assessmentService = new AssessmentService();
        AssessmentFacade af = assessmentService.getBasicInfoOfAnAssessment(assessmentId);
        creator = af.getCreatedBy();
    }
    else
    {
        PublishedAssessmentService pubService = new PublishedAssessmentService();
        assessmentService = pubService;
        PublishedAssessmentFacade paf = pubService.getSettingsOfPublishedAssessment(assessmentId);
        creator = paf.getCreatedBy();
    }
    
    AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");
    if (!authzBean.isUserAllowedToEditAssessment(assessmentId, creator, !isEditPendingAssessmentFlow))
    {
        String err = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "denied_edit_assessment_error");
        context.addMessage(null, new FacesMessage(err));
        sectionBean.setOutcome("editPart");
        return;
    }
    
    if (isEditPendingAssessmentFlow) {
    	EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_REVISE, "siteId=" + AgentFacade.getCurrentSiteId() + ", sectionId=" + sectionId, true));
    }
    else {
    	EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_PUBLISHED_ASSESSMENT_REVISE, "siteId=" + AgentFacade.getCurrentSiteId() + ", sectionId=" + sectionId, true));
    }

    boolean addItemsFromPool = false;
	
    sectionBean.setOutcome("editAssessment");
   
    if((sectionBean.getType().equals("2"))&& (sectionBean.getSelectedPool().equals(""))){
          
	String selectedPool_err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","selectedPool_error");
	context.addMessage(null,new FacesMessage(selectedPool_err));
	sectionBean.setOutcome("editPart");
	return ;

    }

    if (isEditPendingAssessmentFlow && !("".equals(sectionBean.getType()))  && ((SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.toString()).equals(sectionBean.getType()))) {
      addItemsFromPool = true;

      if (validateItemsDrawn(sectionBean)) {
          section = getOrAddSection(assessmentService, assessmentId, sectionId);
      }
      else {
        sectionBean.setOutcome("editPart");
        return;
      }
    }
    else {
    	section = getOrAddSection(assessmentService, assessmentId, sectionId);
    }

    if (section == null) {
    	log.info("section == null - Should not come to here. Simply return.");
    	log.info("assessmentId =" + assessmentId);
    	log.info("sectionId =" + sectionId);
    	return;
    }
    log.debug("**** section title ="+section.getTitle());
    log.debug("**** title ="+title);
    
    // title, description, and question ordering are editable for both pending and publish assessments
    if (title != null)
      section.setTitle(title);
    section.setDescription(description);
	if (!("".equals(sectionBean.getQuestionOrdering())))
	  section.addSectionMetaData(SectionDataIfc.QUESTIONS_ORDERING, sectionBean.getQuestionOrdering());

    if (isEditPendingAssessmentFlow) {
    	if (!("".equals(sectionBean.getKeyword())))
    		section.addSectionMetaData(SectionMetaDataIfc.KEYWORDS, TextFormat.convertPlaintextToFormattedTextNoHighUnicode(sectionBean.getKeyword()));

    	if (!("".equals(sectionBean.getObjective())))
    		section.addSectionMetaData(SectionMetaDataIfc.OBJECTIVES, TextFormat.convertPlaintextToFormattedTextNoHighUnicode(sectionBean.getObjective()));

    	if (!("".equals(sectionBean.getRubric())))
    		section.addSectionMetaData(SectionMetaDataIfc.RUBRICS, TextFormat.convertPlaintextToFormattedTextNoHighUnicode(sectionBean.getRubric()));

    	if (!("".equals(sectionBean.getType())))  {
    		section.addSectionMetaData(SectionDataIfc.AUTHOR_TYPE, sectionBean.getType());
    		if ((SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.toString()).equals(sectionBean.getType()))  {
    			if ((sectionBean.getNumberSelected()!=null) && !("".equals(sectionBean.getNumberSelected())))
    			{
    				section.addSectionMetaData(SectionDataIfc.NUM_QUESTIONS_DRAWN, sectionBean.getNumberSelected());
    			}

    			if (!("".equals(sectionBean.getSelectedPool())))
    			{
    				section.addSectionMetaData(SectionDataIfc.POOLID_FOR_RANDOM_DRAW, sectionBean.getSelectedPool());
    				String poolname = "";
    				QuestionPoolService qpservice = new QuestionPoolService();
    				QuestionPoolFacade poolfacade = qpservice.getPool(new Long(sectionBean.getSelectedPool()), AgentFacade.getAgentString());
    				if (poolfacade!=null) {
    					poolname = poolfacade.getTitle();
    				}
    				section.addSectionMetaData(SectionDataIfc.POOLNAME_FOR_RANDOM_DRAW, poolname);
    			}

    			section.addSectionMetaData(SectionDataIfc.RANDOMIZATION_TYPE, sectionBean.getRandomizationType());
    		}
    	}
    	
    	if(addItemsFromPool){
    		boolean hasRandomPartScore = false;
    		Double score = null;
    		String requestedScore = sectionBean.getRandomPartScore();
    		if (requestedScore != null && !requestedScore.equals("")) {
    			hasRandomPartScore = true;
    			score = new Double(requestedScore);
    		}
    		boolean hasRandomPartDiscount = false;
    		Double discount = null;
    		String requestedDiscount = sectionBean.getRandomPartDiscount();
    		if (requestedDiscount != null && !requestedDiscount.equals("")) {
    			hasRandomPartDiscount = true;
    			discount = new Double(requestedDiscount);
    		}
    		
    		if (hasRandomPartScore && score != null) {
    			section.addSectionMetaData(SectionDataIfc.POINT_VALUE_FOR_QUESTION, score.toString());
    		}
    		else {
    			section.addSectionMetaData(SectionDataIfc.POINT_VALUE_FOR_QUESTION, "");
    		}

    		if (hasRandomPartDiscount && discount != null) {
    			section.addSectionMetaData(SectionDataIfc.DISCOUNT_VALUE_FOR_QUESTION, discount.toString());
    		}
    		else {
    			section.addSectionMetaData(SectionDataIfc.DISCOUNT_VALUE_FOR_QUESTION, "");
    		}
    	}
    }

    assessmentService.saveOrUpdateSection(section);

    if (addItemsFromPool){
    	//update random questions from question pool
    	int success = assessmentService.updateRandomPoolQuestions(assessmentService.getSection(section.getSectionId().toString()));
    	if(success != AssessmentService.UPDATE_SUCCESS){
    		if(success == AssessmentService.UPDATE_ERROR_DRAW_SIZE_TOO_LARGE){
    			//shouldn't get here since there is a check, but might as well verify
    			String err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","qdrawn_error");
    			context.addMessage(null,new FacesMessage(err+ " " + section.getSectionMetaDataByLabel(SectionDataIfc.NUM_QUESTIONS_DRAWN)));
    		}
    	}
    }

    // added by daisyf, 10/10/06
    updateAttachment(section.getSectionAttachmentList(), sectionBean.getAttachmentList(), section.getData());

    // #2 - goto editAssessment.jsp, so reset assessmentBean
    AssessmentIfc assessment = assessmentService.getAssessment(
        Long.valueOf(assessmentBean.getAssessmentId()));
    assessmentBean.setAssessment(assessment);
    assessmentService.updateAssessmentLastModifiedInfo(assessment);
    
    EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_REVISE, "siteId=" + AgentFacade.getCurrentSiteId() + ", sectionId=" + section.getSectionId(), true));
  }

  public SectionFacade addPart(String assessmentId){
    AssessmentService assessmentService = new AssessmentService();
    SectionFacade section = assessmentService.addSection(
			     assessmentId);
    return section;
  }

  private SectionFacade getOrAddSection(AssessmentService assessmentService, String assessmentId, String sectionId) {
	  SectionFacade section;
	  if ("".equals(sectionId)){
		  section = assessmentService.addSection(assessmentId);
		  //This is never read in the code
		  //sectionId = section.getSectionId().toString();
	  }
	  else {
		  section = assessmentService.getSection(sectionId);
	  }
	  return section;
  }

  public boolean validateItemsDrawn(SectionBean sectionBean){
     FacesContext context = FacesContext.getCurrentInstance();
     String numberDrawn = sectionBean.getNumberSelected();
     String err;
    
     QuestionPoolService qpservice = new QuestionPoolService();

     List itemlist = qpservice.getAllItems(Long.valueOf(sectionBean.getSelectedPool()) );
     int itemcount = itemlist.size();
     String itemcountString=" "+Integer.toString(itemcount);

     try{
	 int numberDrawnInt = Integer.parseInt(numberDrawn);
	 if(numberDrawnInt <=0 || numberDrawnInt>itemcount){
	     err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","qdrawn_error");
	     context.addMessage(null,new FacesMessage(err+itemcountString ));
	     return false;

	 }
	
     } catch(NumberFormatException e){
	 err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","qdrawn_error");
	 context.addMessage(null,new FacesMessage(err+itemcountString ));
	 return false;
     }
     
     String randomScore = sectionBean.getRandomPartScore();
     if (randomScore != null && !randomScore.equals("")) {    	 
    	 try{
    		 double randomScoreDouble = Double.parseDouble(randomScore);
    		 if(randomScoreDouble < 0.0){
    			 err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","qdrawn_pt_error");
    			 context.addMessage(null,new FacesMessage(err ));
    			 return false;
    		 }
    	 } catch(NumberFormatException e){
    		 err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","qdrawn_pt_error");
    		 context.addMessage(null,new FacesMessage(err ));
    		 return false;
    	 }
     }
     else {
		 err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","qdrawn_null_error_pos");
		 context.addMessage(null,new FacesMessage(err ));
		 return false;
     }

     String randomDiscount = sectionBean.getRandomPartDiscount();
     if (randomDiscount != null && !randomDiscount.equals("")) {
    	 try{
    		 double randomDiscountDouble = Double.parseDouble(randomDiscount);
    		 if(randomDiscountDouble < 0.0){
    			 err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","qdrawn_pt_error");
    			 context.addMessage(null,new FacesMessage(err ));
    			 return false;
    		 }
    	 } catch(NumberFormatException e){
    		 err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","qdrawn_pt_error");
    		 context.addMessage(null,new FacesMessage(err ));
    		 return false;
    	 }
     }
     else {
		 err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","qdrawn_null_error_neg");
		 context.addMessage(null,new FacesMessage(err ));
		 return false; 
     }
     return true;
  }

    private void updateAttachment(List oldList, List newList, SectionDataIfc section){
    if ((oldList == null || oldList.size() == 0 ) && (newList == null || newList.size() == 0)) return;
    List list = new ArrayList();
    Map map = getAttachmentIdHash(oldList);
    for (int i=0; i<newList.size(); i++){
      SectionAttachmentIfc a = (SectionAttachmentIfc)newList.get(i);
      if (map.get(a.getAttachmentId())!=null){
        // exist already, remove it from map
        map.remove(a.getAttachmentId());
      }
      else{
        // new attachments
        a.setSection(section);
        list.add(a);
      }
    }      
    // save new ones
    AssessmentService assessmentService = null;
    if (isEditPendingAssessmentFlow) {
    	assessmentService = new AssessmentService();
    }
    else {
    	assessmentService = new PublishedAssessmentService();
    }
    assessmentService.saveOrUpdateAttachments(list);

    // remove old ones
    Set set = map.keySet();
    Iterator iter = set.iterator();
    while (iter.hasNext()){
      Long attachmentId = (Long)iter.next();
      assessmentService.removeSectionAttachment(attachmentId.toString());
    }
  }

  private Map getAttachmentIdHash(List list){
    Map map = new HashMap();
    for (int i=0; i<list.size(); i++){
      SectionAttachmentIfc a = (SectionAttachmentIfc)list.get(i);
      map.put(a.getAttachmentId(), a);
    }
    return map;
  }


}
