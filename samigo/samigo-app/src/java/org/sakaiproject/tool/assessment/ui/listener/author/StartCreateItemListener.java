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
import java.util.List;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.CalculatedQuestionBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ImageMapItemBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemBean;
import org.sakaiproject.tool.assessment.ui.bean.author.MatchItemBean;
import org.sakaiproject.tool.assessment.ui.bean.author.SearchQuestionBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.cover.SessionManager;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Organization: Sakai Project</p>
 */
@Slf4j
public class StartCreateItemListener implements ValueChangeListener, ActionListener
{

  //private static ContextUtil cu;
  //private String scalename;  // used for multiple choice Survey

  String currsection = null;
  // both actionListener and valueChangeListener methods are used,
  // for authoring asseessments and qpools

  /**
   * Standard process action method.
   * @param ae ValueChangeEvent
   * @throws AbortProcessingException
   */
  public void processValueChange(ValueChangeEvent ae) throws AbortProcessingException
  {
    log.debug("StartCreateItemListener valueChangeLISTENER.");
    ItemAuthorBean itemauthorbean = (ItemAuthorBean) ContextUtil.lookupBean("itemauthor");

    //FacesContext context = FacesContext.getCurrentInstance();

    String selectedvalue= (String) ae.getNewValue();
    if ((selectedvalue!=null) && (!selectedvalue.equals("")) ){
      itemauthorbean.setItemType(selectedvalue);

    boolean update = false;
    String curritemid = null;
    
    // check if it is coming from Item Modify page.
    ItemBean curritem = itemauthorbean.getCurrentItem();
    if (curritem!=null) {
      curritemid = curritem.getItemId();
      currsection = curritem.getSelectedSection();
      update = true;

      log.debug("change question type , itemid is not null");
    }
    else {
      log.debug("didn't change question type, itemid is null");
    }

    if (!startCreateItem(itemauthorbean))
    {
      throw new RuntimeException("failed to startCreatItem.");
    }

    if (update){
        // if update, then update currentItem's itemId.
       itemauthorbean.getCurrentItem().setItemId(curritemid);
    }




    }
  }

  /**
   * Standard process action method.
   * @param ae ActionEvent
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws AbortProcessingException
  // used by question pool's selectQuestionType.jsp
  {
    log.debug("StartCreateItemListener actionLISTENER.");
    ItemAuthorBean itemauthorbean = (ItemAuthorBean) ContextUtil.lookupBean("itemauthor");

    if (!startCreateItem(itemauthorbean))
    {
      throw new RuntimeException("failed to startCreatItem.");
    }

  }


  public boolean startCreateItem(ItemAuthorBean itemauthorbean) {

   String nextpage= null;
   ItemBean item = new ItemBean();
      //This needs to happen in any case
   itemauthorbean.setTagsTempListToJson("[]");

      AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
   try{
    // check to see if we arrived here from question pool

// need to set indivdiual item properties
      itemauthorbean.setCurrentItem(item);

/*
 Use TypeFacade 's constants for item type,
  Note:   10 = import from question pool
  public static Long MULTIPLE_CHOICE = new Long(1);
  public static Long MULTIPLE_CORRECT = new Long(2); //not used
  public static Long MULTIPLE_CHOICE_SURVEY = new Long(3);
  public static Long TRUE_FALSE = new Long(4);
  public static Long ESSAY_QUESTION = new Long(5);
  public static Long FILE_UPLOAD = new Long(6);
  public static Long AUDIO_RECORDING = new Long(7);
  public static Long FILL_IN_BLANK = new Long(8);
    public static Long FILL_IN_NUMERIC = new Long(11);
  public static Long MATCHING = new Long(9);
*/

        item.setItemType(itemauthorbean.getItemType());
        itemauthorbean.setItemType("");
        itemauthorbean.setItemTypeString("");
        itemauthorbean.setAttachmentList(null);
        itemauthorbean.setResourceHash(null);
        itemauthorbean.setTagsList(null);

        int itype=0; //
log.debug("item.getItemType() = " + item.getItemType());
        if ((item.getItemType()!=null) && !("".equals(item.getItemType())))
        {
log.debug("item.getItemType() integer = " + item.getItemType());
          itype = new Integer(item.getItemType()).intValue();
        }
        else if ("".equals(item.getItemType()))
        {
log.debug("item.getItemType() , use default type 1 = " + item.getItemType());
          itype = 1; // we only appear to get here when when the mouse is clicked a lot.
        }
log.debug("after getting item.getItemType() ");
        switch (itype) {
                case 1:
                        nextpage = "multipleChoiceItem";
                        break;
                case 2:
// never really use this, put here for completeness
                        nextpage = "multipleChoiceItem";
                        break;
                case 12:
                		nextpage = "multipleChoiceItem";
                		break;
                case 3:
                        nextpage = "surveyItem";
                        break;
                case 4:
                        nextpage = "trueFalseItem";
                        break;
                case 5:
                        nextpage = "shortAnswerItem";
                        break;
               case 6:
                        nextpage = "fileUploadItem";
                        break;
                case 7:
                        nextpage = "audioRecItem";
                        // Set default values
                        item.setTimeAllowed("30");
                        item.setNumAttempts("9999");
                        break;
                case 8:
                        nextpage = "fillInBlackItem";
                        break;
                case 11:
                        nextpage = "fillInNumericItem";
                        break;
                case 13:
                        nextpage = "matrixChoicesSurveyItem";
                        break;
                case 14:
                    nextpage = "emiItem";
                    break;
                    
                case 9:
     			MatchItemBean matchitem = new MatchItemBean();

      			item.setCurrentMatchPair(matchitem);
      			item.setMatchItemBeanList(new ArrayList());
                        nextpage = "matchingItem";
                        break;
                case 15: // CALCULATED_QUESTION
                    CalculatedQuestionBean bean = new CalculatedQuestionBean();
                    item.setCalculatedQuestion(bean);
                    nextpage = "calculatedQuestionVariableItem";
                    break;
                case 16: // IMAGEMAP_QUESTION
                    ImageMapItemBean imgbean = new ImageMapItemBean();
          			item.setImageMapItemBeanList(new ArrayList());
                            nextpage = "imageMapItem";
                            break;    
                case 10:
                    QuestionPoolBean qpoolBean= (QuestionPoolBean) ContextUtil.lookupBean("questionpool");
                    qpoolBean.setImportToAuthoring(true);
                    nextpage = "poolList";

                    // set the current part for question copying
                    String partNo = itemauthorbean.getInsertToSection();
                    List<SectionContentsBean> sections = assessmentBean.getSections();
                    for (SectionContentsBean content : sections)
                    {
                        if (partNo != null && partNo.equals(content.getNumber()))
                        {
                            qpoolBean.setSelectedSection(content.getSectionId());
                            break;
                        }
                    }

                    break;
                case 17:
                    SearchQuestionBean searchQuestionBean= (SearchQuestionBean) ContextUtil.lookupBean("searchQuestionBean");
                    nextpage = "searchQuestion";

                    if(itemauthorbean.getTarget().equals("questionpool")) {
                        searchQuestionBean.setComesFromPool(true);
                        searchQuestionBean.setSelectedQuestionPool(itemauthorbean.getQpoolId());
                        break;
                    }else {
                        searchQuestionBean.setComesFromPool(false);
                        String partNoQS = itemauthorbean.getInsertToSection();
                        List<SectionContentsBean> sectionsQS = assessmentBean.getSections();
                        for (SectionContentsBean content : sectionsQS)
                        {
                            if (partNoQS != null && partNoQS.equals(content.getNumber()))
                            {
                                searchQuestionBean.setSelectedSection(content.getSectionId());
                                break;
                            }
                        }
                    }
                break;
                case 100:
				ToolSession currentToolSession = SessionManager.getCurrentToolSession();
				currentToolSession.setAttribute("QB_insert_possition", itemauthorbean.getInsertPosition());
				currentToolSession.setAttribute("QB_assessemnt_id",	assessmentBean.getAssessmentId());
				currentToolSession.setAttribute("QB_assessemnt_sections", itemauthorbean.getSectionList());
				currentToolSession.setAttribute("QB_insert_section", itemauthorbean.getInsertToSection());
				nextpage = "searchQuestionBank";
				break;                        
        }
   }
    catch(RuntimeException e)
    {
      log.error(e.getMessage(), e);
      throw e;
    }

// check for metadata settings
    if ("assessment".equals(itemauthorbean.getTarget())) {
      AssessmentService assessdelegate = new AssessmentService();
      AssessmentFacade assessment = assessdelegate.getAssessment(assessmentBean.getAssessmentId());
      itemauthorbean.setShowMetadata(assessment.getHasMetaDataForQuestions());
      itemauthorbean.setShowFeedbackAuthoring(assessment.getShowFeedbackAuthoring());

      // set section

        if (itemauthorbean.getInsertToSection()!=null) {
       // for inserting an item, this should be sequence, e.g. 1, 2, ...etc
	  String sectionid= (assessment.getSection(new Long(itemauthorbean.getInsertToSection()))).getSectionId().toString();
          item.setSelectedSection(sectionid);
        }
        else {
        	// modify items, change type, will take you here.
        	item.setSelectedSection(currsection);
        }

	    // reset insertToSection to null;
        //itemauthorbean.setInsertToSection(null);

    }
    else {
     // for question pool , always show metadata as default
      itemauthorbean.setShowMetadata("true");
    }


    // set outcome for action
    itemauthorbean.setOutcome(nextpage);
    return true;

  }
}
