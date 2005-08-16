/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.ArrayList;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.TypeFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemBean;
import org.sakaiproject.tool.assessment.ui.bean.author.MatchItemBean;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Organization: Sakai Project</p>
 */

public class StartCreateItemListener implements ValueChangeListener, ActionListener
{

  private static Log log = LogFactory.getLog(StartCreateItemListener.class);
  private static ContextUtil cu;
  private String scalename;  // used for multiple choice Survey


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
    ItemAuthorBean itemauthorbean = (ItemAuthorBean) cu.lookupBean("itemauthor");

    FacesContext context = FacesContext.getCurrentInstance();


    String selectedvalue= (String) ae.getNewValue();
    if ((selectedvalue!=null) && (!selectedvalue.equals("")) ){
      itemauthorbean.setItemType(selectedvalue);

    boolean update = false;
    String curritemid = null;
    // check if it is coming from Item Modify page.
    ItemBean curritem = itemauthorbean.getCurrentItem();
    if (curritem!=null) {
      curritemid = curritem.getItemId();
      update = true;
    }
    else {
      log.debug("Not from modify , itemid  (should be null) = " + curritemid);
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
    ItemAuthorBean itemauthorbean = (ItemAuthorBean) cu.lookupBean("itemauthor");

    if (!startCreateItem(itemauthorbean))
    {
      throw new RuntimeException("failed to startCreatItem.");
    }

  }


  public boolean startCreateItem(ItemAuthorBean itemauthorbean) {

   String nextpage= null;
   ItemBean item = new ItemBean();
   try{
    // check to see if we arrived here from question pool

// need to get assessmentid
//  String assessmentId = ContextUtil.lookupParam("assessmentid");

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
  public static Long MATCHING = new Long(9);
*/

        item.setItemType(itemauthorbean.getItemType());


        itemauthorbean.setItemType("");
        itemauthorbean.setItemTypeString("");
        int itype=0; //
        if (item.getItemType()!=null)
        {
          itype = new Integer(item.getItemType()).intValue();
        }
        else if ("".equals(item.getItemType()))
        {
          itype = 1; // we only appear to get here when when the mouse is clicked a lot.
        }
        switch (itype) {
                case 1:
                        item.setMultipleCorrect(Boolean.FALSE.booleanValue());
                        item.setMultipleCorrectString(TypeFacade.MULTIPLE_CHOICE.toString());
                        nextpage = "multipleChoiceItem";
                        break;
                case 2:
// never really use this, put here for completeness
                        item.setMultipleCorrect(Boolean.TRUE.booleanValue());
                        item.setMultipleCorrectString(TypeFacade.MULTIPLE_CORRECT.toString());
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
                        break;
                case 8:
                        nextpage = "fillInBlackItem";
                        break;
                case 9:
     			MatchItemBean matchitem = new MatchItemBean();

      			item.setCurrentMatchPair(matchitem);
      			item.setMatchItemBeanList(new ArrayList());
                        nextpage = "matchingItem";
                        break;
                case 10:
    			QuestionPoolBean qpoolBean= (QuestionPoolBean) cu.lookupBean("questionpool");
			qpoolBean.setImportToAuthoring(true);
                        nextpage = "poolList";
                        break;
        }
   }
    catch(Exception e)
    {
      e.printStackTrace();
      throw new Error(e);
    }

// check for metadata settings
    if ("assessment".equals(itemauthorbean.getTarget())) {
      AssessmentService assessdelegate = new AssessmentService();
      AssessmentBean assessmentBean = (AssessmentBean) cu.lookupBean("assessmentBean");
      AssessmentFacade assessment = assessdelegate.getAssessment(assessmentBean.getAssessmentId());
      itemauthorbean.setShowMetadata(assessment.getHasMetaDataForQuestions());

      // set section

        if (itemauthorbean.getInsertToSection()!=null) {
       // for inserting an item, this should be sequence, e.g. 1, 2, ...etc
	  String sectionid= (assessment.getSection(new Long(itemauthorbean.getInsertToSection()))).getSectionId().toString();
          item.setSelectedSection(sectionid);
        }
        else {
         // do not set section here, sections are set by the form
       // for adding an item, add to the last section, sequence = 1
          //SectionDataIfc section = assessment.getDefaultSection();
          //String sectionid = section.getSectionId().toString();
          //item.setSelectedSection(sectionid);
        }

	// reset insertToSection to null;
        itemauthorbean.setInsertToSection(null);


        if (item.getItemType().equals("10")) {
         // do not set section here, sections are set by the editPool form
    	  // QuestionPoolBean qpoolBean= (QuestionPoolBean) cu.lookupBean("questionpool");
	  //qpoolBean.setSelectedSection(item.getSelectedSection());
        }

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
