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
import java.util.Iterator;
import java.util.Set;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.Answer;
import org.sakaiproject.tool.assessment.data.dao.assessment.AnswerFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemText;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerFeedbackIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.TypeFacade;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AnswerBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemBean;
import org.sakaiproject.tool.assessment.ui.bean.author.MatchItemBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @version $Id$
 */

public class ItemModifyListener implements ActionListener
{
  private static Log log = LogFactory.getLog(ItemModifyListener.class);
  private static ContextUtil cu;
  private String scalename;  // used for multiple choice Survey

  /**
   * Standard process action method.
   * @param ae ActionEvent
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    log.info("ItemModify LISTENER.");
    //System.out.println("lydiatest BEGIN MOdify ");
    ItemAuthorBean itemauthorbean = (ItemAuthorBean) cu.lookupBean("itemauthor");

    String itemId= cu.lookupParam("itemid");
    String target= cu.lookupParam("target");

    String poolid = cu.lookupParam("poolId");
    if(poolid!=null) {
       itemauthorbean.setQpoolId(poolid);
    }

    itemauthorbean.setTarget(target);

    if (!populateItemBean(itemauthorbean, itemId))
    {
      throw new RuntimeException("failed to populateItemBean.");
    }

  }


  public boolean populateItemBean(ItemAuthorBean itemauthorbean, String itemId) {
        String nextpage= null;
      //System.out.println("lydiatest in ItemModifyListenr.  populateItemBean ");
      ItemBean bean = new ItemBean();

    try{
      // need to update indivdiual pool properties

      ItemService delegate = new ItemService();
      ItemFacade itemfacade =  delegate.getItem(new Long(itemId), AgentFacade.getAgentString());


      //System.out.println("lydiatest itemfacade.gettypeid() " + itemfacade.getTypeId());
      bean.setItemId(itemfacade.getItemId().toString());
      bean.setItemType(itemfacade.getTypeId().toString());
      itemauthorbean.setItemType(itemfacade.getTypeId().toString());

      // if the item only exists in pool, sequence = null
      if (itemfacade.getSequence()!=null) {
        itemauthorbean.setItemNo(String.valueOf(itemfacade.getSequence().intValue() ));
      }

      Float points = itemfacade.getScore();
      String score;
      if (points!=null)
       {
        score = points.toString();
       }
      else // cover modifying an imported XML assessment that has no score yet
       {
         score ="0.0";
       }
      bean.setItemScore(score);
      if (itemfacade.getHasRationale() !=null) {
        bean.setRationale(itemfacade.getHasRationale().toString());
      }
      if (itemfacade.getInstruction() !=null) {
        bean.setInstruction(itemfacade.getInstruction());
      }

      if (itemfacade.getDuration() !=null) {
        bean.setTimeAllowed(itemfacade.getDuration().toString());
      }

      if (itemfacade.getTriesAllowed() !=null) {
        bean.setNumAttempts(itemfacade.getTriesAllowed().toString());
      }

      bean.setCorrFeedback(itemfacade.getCorrectItemFeedback());
      bean.setIncorrFeedback(itemfacade.getInCorrectItemFeedback());
      bean.setGeneralFeedback(itemfacade.getGeneralItemFeedback());
      populateMetaData(itemauthorbean, itemfacade, bean);

      if (new Long(itemauthorbean.getItemType()).equals(TypeFacade.MATCHING)) {
        populateItemTextForMatching(itemauthorbean, itemfacade, bean);
      }
      else {
        populateItemText(itemauthorbean, itemfacade, bean);
      }


        int itype=0; // default to true/false
        if (itemauthorbean.getItemType()!=null) {
                itype = new Integer(itemauthorbean.getItemType()).intValue();
        }
    //System.out.println("lydiatest selected which type : " + itype);
        switch (itype) {
                case 1:
                        bean.setMultipleCorrect(false);
                        bean.setMultipleCorrectString(TypeFacade.MULTIPLE_CHOICE.toString());
    //System.out.println("lydiatest set multiplecorr string : " + bean.getMultipleCorrectString());
                        itemauthorbean.setItemTypeString("Multiple Choice");
                        nextpage = "multipleChoiceItem";
                        break;
                case 2:
                        bean.setMultipleCorrect(true);
                        bean.setMultipleCorrectString(TypeFacade.MULTIPLE_CORRECT.toString());
    //System.out.println("lydiatest set multiplecorr string : " + bean.getMultipleCorrectString());
                        itemauthorbean.setItemTypeString("Multiple Choice");
                        nextpage = "multipleChoiceItem";
                        break;
                case 3:
                        itemauthorbean.setItemTypeString("Survey");  // need to get it from properties file
                        nextpage = "surveyItem";
                        break;
                case 4:
                        itemauthorbean.setItemTypeString("True or False");  //  need to get it from properties file
                        nextpage = "trueFalseItem";
                        break;
                case 5:
                        itemauthorbean.setItemTypeString("Short Answers/Essay");  //  need to get it from properties file
                        nextpage = "shortAnswerItem";
                        break;
                case 6:
                        itemauthorbean.setItemTypeString("File Upload");  //  need to get it from properties file
                        nextpage = "fileUploadItem";
                        break;
                case 7:
                        itemauthorbean.setItemTypeString("Audio Recording");  //  need to get it from properties file
                        nextpage = "audioRecItem";
                        break;
                case 8:
                        itemauthorbean.setItemTypeString("Fill In the Blank");  //  need to get it from properties file
                        nextpage = "fillInBlackItem";
                        break;
                case 9:
                        itemauthorbean.setItemTypeString("Matching");  //  need to get it from properties file
			MatchItemBean matchitem = new MatchItemBean();
                        bean.setCurrentMatchPair(matchitem);
                        nextpage = "matchingItem";
                        break;
                case 10:
                        itemauthorbean.setItemTypeString("Importing from Question Pool");
 //  need to get it from properties file
                        nextpage = "poolList";
                        break;
        }
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return false;
    }

    if ("assessment".equals(itemauthorbean.getTarget())) {
// check for metadata settings
      AssessmentService assessdelegate = new AssessmentService();
      AssessmentBean assessmentBean = (AssessmentBean) cu.lookupBean("assessmentBean");
      AssessmentFacade assessment = assessdelegate.getAssessment(assessmentBean.getAssessmentId());
      itemauthorbean.setShowMetadata(assessment.getHasMetaDataForQuestions());
      //System.out.println("lydiatest showMetadata : " + itemauthorbean.getShowMetadata());
    }
    else {
     // for question pool , always show metadata as default
      itemauthorbean.setShowMetadata("true");
      //System.out.println("lydiatest showMetadata : " + itemauthorbean.getShowMetadata());
    }


      // set current ItemBean in ItemAuthorBean
      itemauthorbean.setCurrentItem(bean);

	// set outcome for action
	itemauthorbean.setOutcome(nextpage);
	return true;
  }


  private void populateItemText(ItemAuthorBean itemauthorbean, ItemFacade itemfacade, ItemBean bean)  {

    Set itemtextSet = itemfacade.getItemTextSet();
    Iterator iter = itemtextSet.iterator();
    while (iter.hasNext()){
       ItemText  itemText = (ItemText) iter.next();
       bean.setItemText(itemText.getText());

/////////////////////////////////////////////////////////////
// Get current Answers choices
/////////////////////////////////////////////////////////////


       if (new Long(itemauthorbean.getItemType()).equals(TypeFacade.TRUE_FALSE)) {

       Set answerSet = itemText.getAnswerSet();
       Iterator iter1 = answerSet.iterator();
       while (iter1.hasNext()){

       // should only be one element in the Set, except for Matching

         Answer answer = (Answer) iter1.next();
         if (answer.getIsCorrect() != null &&
             answer.getIsCorrect().booleanValue()){
	   bean.setCorrAnswer(answer.getText());
   	 }
       }
       }

       if (new Long(itemauthorbean.getItemType()).equals(TypeFacade.ESSAY_QUESTION)) {

       Set answerSet = itemText.getAnswerSet();
       Iterator iter1 = answerSet.iterator();
       while (iter1.hasNext()){

       // should only be one element in the Set, except for Matching

         Answer answer = (Answer) iter1.next();
         bean.setCorrAnswer(answer.getText());
	// get answerfeedback
         Set feedbackSet=  answer.getAnswerFeedbackSet();
         Iterator iter2 = feedbackSet.iterator();
         while (iter2.hasNext()){
		bean.setCorrFeedback(((AnswerFeedback)iter2.next()).getText() );
 	 }

       }

       }


       if (new Long(itemauthorbean.getItemType()).equals(TypeFacade.FILL_IN_BLANK)) {

// restore the original question text, which includes answers in the braces.

       String orig = itemText.getText();
       String replaced = null;
       Set answerSet = itemText.getAnswerSet();
       Iterator iter1 = answerSet.iterator();
       //need to check sequence no, since this answerSet returns answers in random order
       int count = answerSet.size();
       String[] answerArray = new String[count];
       while (iter1.hasNext()){
	 Answer answerobj = (Answer) iter1.next();
         String answer = answerobj.getText();
         Long seq = answerobj.getSequence();
//System.out.println(" lydiatest ....build array answer seq = " +seq + " answer " + answer);
         if ( (answerArray[seq.intValue()-1] == null ) || (answerArray[seq.intValue()-1].equals("")) ) {
           answerArray[seq.intValue()-1] = answer;
	 }
 	 else {
           answerArray[seq.intValue()-1] = answerArray[seq.intValue()-1] + " | " + answer;
	 }

//System.out.println(" lydiatest ....answrarray [ " +( seq.intValue()-1) + "] = " + answer);
       }
       for (int i=0; i<answerArray.length; i++) {
	 replaced = orig.replaceFirst("\\{\\}", "{"+answerArray[i]+"}");
         orig = replaced;
//System.out.println(" lydiatest ....restore " + orig );
       }

//System.out.println(" lydiatest ....restore " + orig );

       bean.setItemText(replaced);


       } //fib

       if ((new Long(itemauthorbean.getItemType()).equals(TypeFacade.MULTIPLE_CHOICE)) ||(new Long(itemauthorbean.getItemType()).equals(TypeFacade.MULTIPLE_CORRECT)) ) {
	 Set answerobjlist = itemText.getAnswerSet();
         String afeedback =  "" ;
	 Iterator iter1 = answerobjlist.iterator();
	 ArrayList answerbeanlist = new ArrayList();
	 ArrayList correctlist = new ArrayList();
       //need to check sequence no, since this answerSet returns answers in random order
         int count = answerobjlist.size();
         Answer[] answerArray = new Answer[count];
         while(iter1.hasNext())
         {
           Answer answerobj = (Answer) iter1.next();
           Long seq = answerobj.getSequence();
           answerArray[seq.intValue()-1] = answerobj;
         }
         for (int i=0; i<answerArray.length; i++) {
           Set feedbackSet = answerArray[i].getAnswerFeedbackSet();
	   // contains only one element in the Set
	   if (feedbackSet.size() == 1) {
	     AnswerFeedback afbobj=(AnswerFeedback) feedbackSet.iterator().next();
             afeedback = afbobj.getText();
           }
	   AnswerBean answerbean = new AnswerBean();
                answerbean.setText(answerArray[i].getText());
                answerbean.setSequence(answerArray[i].getSequence());
                answerbean.setLabel(answerArray[i].getLabel());
                answerbean.setFeedback(afeedback);
                answerbean.setIsCorrect(answerArray[i].getIsCorrect());
		if (answerbean.getIsCorrect() != null &&
                    answerbean.getIsCorrect().booleanValue()) {
		  correctlist.add(answerbean);
		}
                answerbeanlist.add(answerbean);
         }

	// set correct choice for single correct
       if (new Long(itemauthorbean.getItemType()).equals(TypeFacade.MULTIPLE_CHOICE)) {
	 Iterator iter2 = correctlist.iterator();
         while(iter2.hasNext())
	 {
	   AnswerBean corrbean= (AnswerBean) iter2.next();
		// should only have one correct answer
		bean.setCorrAnswer(corrbean.getLabel());
	   break;
	 }
	}

	// set correct choice for multiple correct
       if (new Long(itemauthorbean.getItemType()).equals(TypeFacade.MULTIPLE_CORRECT)) {
	int corrsize = correctlist.size();
	String[] corrchoices = new String[corrsize];
	Iterator iter3 = correctlist.iterator();
        int counter =  0;
        while(iter3.hasNext())
	{
	    corrchoices[counter]=  ((AnswerBean)iter3.next()).getLabel();
	    counter++;
        }
	//System.out.println("lydiatest corchoices are " + corrchoices.toString());
	bean.setCorrAnswers(corrchoices);
       }


	 bean.setMultipleChoiceAnswers(answerbeanlist);


       } // mc

/////////////////////////////////////////////////////////////
// Finish Answers
/////////////////////////////////////////////////////////////

    } // looping through itemtextSet , only loop once for these types,
  }



 private void populateItemTextForMatching(ItemAuthorBean itemauthorbean, ItemFacade itemfacade, ItemBean bean)  {

    //System.out.println("lydiatest in populateItemTextForMatching" );
    Set itemtextSet = itemfacade.getItemTextSet();
    Iterator iter = itemtextSet.iterator();
    ArrayList matchItemBeanList = new ArrayList();


    while (iter.hasNext()){
       ItemText  itemText = (ItemText) iter.next();
       MatchItemBean choicebean =  new MatchItemBean();
       choicebean.setChoice(itemText.getText());

     //System.out.println("lydiatest in populateItemTextForMatching , set choice = " + choicebean.getChoice());
       Set answerSet = itemText.getAnswerSet();
       Iterator iter1 = answerSet.iterator();
       while (iter1.hasNext()){
         Answer answer = (Answer) iter1.next();
         if (answer.getIsCorrect() != null &&
             answer.getIsCorrect().booleanValue()){
           choicebean.setMatch(answer.getText());
     //System.out.println("lydiatest in populateItemTextForMatching , set match() = " + choicebean.getMatch());
           choicebean.setSequence(answer.getSequence());
           choicebean.setIsCorrect(Boolean.TRUE);
           Set feedbackSet = answer.getAnswerFeedbackSet();
           Iterator iter2 = feedbackSet.iterator();
           while (iter2.hasNext()){

             AnswerFeedback feedback =(AnswerFeedback) iter2.next();
             if (feedback.getTypeId().equals(AnswerFeedbackIfc.CORRECT_FEEDBACK)) {
               choicebean.setCorrMatchFeedback(feedback.getText());
             }
             else if (feedback.getTypeId().equals(AnswerFeedbackIfc.INCORRECT_FEEDBACK)) {
               choicebean.setIncorrMatchFeedback(feedback.getText());
             }
           }
     //System.out.println("lydiatest in populateItemTextForMatching , set corrfeedback = " + choicebean.getCorrMatchFeedback());
     //System.out.println("lydiatest in populateItemTextForMatching , set corrfeedback = " + choicebean.getIncorrMatchFeedback());
         }
       }
       matchItemBeanList.add(choicebean);
     }

     bean.setMatchItemBeanList(matchItemBeanList);
     //System.out.println("lydiatest in populateItemTextForMatching , matchitembeanlist.size() = " +
     //	bean.getMatchItemBeanList().size()  );


  }







  private void populateMetaData(ItemAuthorBean itemauthorbean, ItemFacade itemfacade, ItemBean bean)  {


    Set itemtextSet = itemfacade.getItemMetaDataSet();
    Iterator iter = itemtextSet.iterator();
    while (iter.hasNext()){
       ItemMetaData meta= (ItemMetaData) iter.next();
       if (meta.getLabel().equals(ItemMetaData.OBJECTIVE)){
	 bean.setObjective(meta.getEntry());
       }
       if (meta.getLabel().equals(ItemMetaData.KEYWORD)){
	 bean.setKeyword(meta.getEntry());
       }
       if (meta.getLabel().equals(ItemMetaData.RUBRIC)){
	 bean.setRubric(meta.getEntry());
       }
       if (meta.getLabel().equals(ItemMetaData.RANDOMIZE)){
	 bean.setRandomized(meta.getEntry());
       }

	// get scalename for fill in blank from the metadata set
       if (meta.getLabel().equals(ItemMetaData.SCALENAME)){
	 bean.setScaleName(meta.getEntry());
       }

	// get part id for the item
       if (meta.getLabel().equals(ItemMetaData.PARTID)){
	 bean.setSelectedSection(meta.getEntry());
	 bean.setOrigSection(meta.getEntry());
       }

	// get pool id for the item
       if (meta.getLabel().equals(ItemMetaData.POOLID)){
	 bean.setSelectedPool(meta.getEntry());
	 bean.setOrigPool(meta.getEntry());
       }

	// get timeallowed for audio recording item
       if (meta.getLabel().equals(ItemMetaData.TIMEALLOWED)){
	 bean.setTimeAllowed(meta.getEntry());
       }

	// get number of attempts for audio recording item
       if (meta.getLabel().equals(ItemMetaData.NUMATTEMPTS)){
	 bean.setNumAttempts(meta.getEntry());
       }


     }
  }







}
