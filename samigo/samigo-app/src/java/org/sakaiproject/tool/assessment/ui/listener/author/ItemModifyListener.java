/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.TypeFacade;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.PublishedItemService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AnswerBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemBean;
import org.sakaiproject.tool.assessment.ui.bean.author.MatchItemBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.util.FormattedText;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Organization: Sakai Project</p>
 */

public class ItemModifyListener implements ActionListener
{
  private static Log log = LogFactory.getLog(ItemModifyListener.class);
  //private String scalename;  // used for multiple choice Survey

  /**
   * Standard process action method.
   * @param ae ActionEvent
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    log.debug("ItemModify LISTENER.");
    ItemAuthorBean itemauthorbean = (ItemAuthorBean) ContextUtil.lookupBean("itemauthor");

    String itemId= ContextUtil.lookupParam("itemid");
    if (itemId != null){
      itemauthorbean.setItemId(itemId);
    }
    else{ 
      // i am afraid on returning from attachment resource management to the item modify page, 
      // I need to call ItemModifyListener, see SamigoJsfTool.java
      // to save any new attachments and re-populate the attachment list.
      // so i can't read itemId from a form. - daisyf
      itemId = itemauthorbean.getItemId();
    }
 
    String poolid = ContextUtil.lookupParam("poolId");
    if(poolid!=null) {
       itemauthorbean.setQpoolId(poolid);
    }

    String target= ContextUtil.lookupParam("target");
    if (target!=null){
      itemauthorbean.setTarget(target);
    }

    if (!populateItemBean(itemauthorbean, itemId))
    {
      throw new RuntimeException("failed to populateItemBean.");
    }

  }


  public boolean populateItemBean(ItemAuthorBean itemauthorbean, String itemId) {
      String nextpage= null;
      ItemBean bean = new ItemBean();
      AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
      boolean isEditPendingAssessmentFlow = author.getIsEditPendingAssessmentFlow();
      log.debug("**** isEditPendingAssessmentFlow : " + isEditPendingAssessmentFlow);
      ItemService delegate = null;
      AssessmentService assessdelegate= null;
      if (isEditPendingAssessmentFlow) {
    	  delegate = new ItemService();
    	  assessdelegate = new AssessmentService();
      }
      else {
    	  delegate = new PublishedItemService();
    	  assessdelegate = new PublishedAssessmentService();
      }
    try{
      ItemFacade itemfacade = delegate.getItem(new Long(itemId), AgentFacade
				.getAgentString());
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

      Float discountpoints = itemfacade.getDiscount();
      String discount;
      if (discountpoints!=null)
      {
    	  discount = discountpoints.toString();
      }
      else // cover modifying an imported XML assessment that has no score yet
      {
    	  discount ="0.0";
      }
      bean.setItemDiscount(discount);

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

      // attach item attachemnt to itemAuthorBean
      List attachmentList = itemfacade.getData().getItemAttachmentList(); 
      itemauthorbean.setAttachmentList(attachmentList);
      itemauthorbean.setResourceHash(null);

      int itype=0; // default to true/false
      if (itemauthorbean.getItemType()!=null) {
                itype = new Integer(itemauthorbean.getItemType()).intValue();
      }
      switch (itype) {
                case 1:
                        itemauthorbean.setItemTypeString("Multiple Choice");
                        nextpage = "multipleChoiceItem";
                        break;
                case 2:
                        itemauthorbean.setItemTypeString("Multiple Choice");
                        nextpage = "multipleChoiceItem";
                        break;
                case 12:
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
                case 11:
                    itemauthorbean.setItemTypeString("Fill In Numeric");  //  need to get it from properties file
                    nextpage = "fillInNumericItem";
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
    catch(RuntimeException e)
    {
      e.printStackTrace();
      return false;
    }

    if ("assessment".equals(itemauthorbean.getTarget())) {
// check for metadata settings
      AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
      AssessmentIfc assessment = assessdelegate.getAssessment(Long.valueOf(assessmentBean.getAssessmentId()));
      itemauthorbean.setShowMetadata(assessment.getHasMetaDataForQuestions());
    }
    else {
     // for question pool , always show metadata as default
      itemauthorbean.setShowMetadata("true");
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
       ItemTextIfc  itemText = (ItemTextIfc) iter.next();
       bean.setItemText(itemText.getText());

/////////////////////////////////////////////////////////////
// Get current Answers choices
/////////////////////////////////////////////////////////////


       if (new Long(itemauthorbean.getItemType()).equals(TypeFacade.TRUE_FALSE)) {

       Set answerSet = itemText.getAnswerSet();
       Iterator iter1 = answerSet.iterator();
       while (iter1.hasNext()){

       // should only be one element in the Set, except for Matching

    	   AnswerIfc answer = (AnswerIfc) iter1.next();
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

    	   AnswerIfc answer = (AnswerIfc) iter1.next();
         bean.setCorrAnswer(answer.getText());
	// get answerfeedback
         Set feedbackSet=  answer.getAnswerFeedbackSet();
         Iterator iter2 = feedbackSet.iterator();
         while (iter2.hasNext()){
		bean.setCorrFeedback(((AnswerFeedbackIfc)iter2.next()).getText() );
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
    	   AnswerIfc answerobj = (AnswerIfc) iter1.next();
         String answer = answerobj.getText();
         Long seq = answerobj.getSequence();
         if ( (answerArray[seq.intValue()-1] == null ) || (answerArray[seq.intValue()-1].equals("")) ) {
           answerArray[seq.intValue()-1] = answer;
	 }
 	 else {
           answerArray[seq.intValue()-1] = answerArray[seq.intValue()-1] + " | " + answer;
	 }

       }
       for (int i=0; i<answerArray.length; i++) {
	 replaced = orig.replaceFirst("\\{\\}", "{"+answerArray[i]+"}");
         orig = replaced;
       }

       bean.setItemText(replaced);


       } //fib
       
       if (new Long(itemauthorbean.getItemType()).equals(TypeFacade.FILL_IN_NUMERIC)) {

//    	 restore the original question text, which includes answers in the braces.

    	       String orig = itemText.getText();
    	       String replaced = null;
    	       Set answerSet = itemText.getAnswerSet();
    	       Iterator iter1 = answerSet.iterator();
    	       //need to check sequence no, since this answerSet returns answers in random order
    	       int count = answerSet.size();
    	       String[] answerArray = new String[count];
    	       while (iter1.hasNext()){
    	    	   AnswerIfc answerobj = (AnswerIfc) iter1.next();
    	         String answer = answerobj.getText();
    	         Long seq = answerobj.getSequence();
    	         if ( (answerArray[seq.intValue()-1] == null ) || (answerArray[seq.intValue()-1].equals("")) ) {
    	           answerArray[seq.intValue()-1] = answer;
    		 }
    	 	 else {
    	           answerArray[seq.intValue()-1] = answerArray[seq.intValue()-1] + " | " + answer;
    		 }

    	       }
    	       for (int i=0; i<answerArray.length; i++) {
    		 replaced = orig.replaceFirst("\\{\\}", "{"+answerArray[i]+"}");
    	         orig = replaced;
    	       }

    	       bean.setItemText(replaced);


    	       } //fin
       
       
       

       if ((Long.valueOf(itemauthorbean.getItemType()).equals(TypeFacade.MULTIPLE_CHOICE)) ||(Long.valueOf(itemauthorbean.getItemType()).equals(TypeFacade.MULTIPLE_CORRECT)) || (Long.valueOf(itemauthorbean.getItemType()).equals(TypeFacade.MULTIPLE_CORRECT_SINGLE_SELECTION)) ) {
	 Set answerobjlist = itemText.getAnswerSet();
         String afeedback =  "" ;
	 Iterator iter1 = answerobjlist.iterator();
	 ArrayList answerbeanlist = new ArrayList();
	 ArrayList correctlist = new ArrayList();
       //need to check sequence no, since this answerSet returns answers in random order
         int count = answerobjlist.size();
         AnswerIfc[] answerArray = new AnswerIfc[count];
         while(iter1.hasNext())
         {
           AnswerIfc answerobj = (AnswerIfc) iter1.next();
           Long seq = answerobj.getSequence();
           answerArray[seq.intValue()-1] = answerobj;
         }
         for (int i=0; i<answerArray.length; i++) {
           Set feedbackSet = answerArray[i].getAnswerFeedbackSet();
	   // contains only one element in the Set
	   if (feedbackSet.size() == 1) {
	     AnswerFeedbackIfc afbobj=(AnswerFeedbackIfc) feedbackSet.iterator().next();
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
       if (Long.valueOf(itemauthorbean.getItemType()).equals(TypeFacade.MULTIPLE_CHOICE)) {
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
       if (Long.valueOf(itemauthorbean.getItemType()).equals(TypeFacade.MULTIPLE_CORRECT) || Long.valueOf(itemauthorbean.getItemType()).equals(TypeFacade.MULTIPLE_CORRECT_SINGLE_SELECTION)) {
	int corrsize = correctlist.size();
	String[] corrchoices = new String[corrsize];
	Iterator iter3 = correctlist.iterator();
        int counter =  0;
        while(iter3.hasNext())
	{
	    corrchoices[counter]=  ((AnswerBean)iter3.next()).getLabel();
	    counter++;
        }
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

    Set itemtextSet = itemfacade.getItemTextSet();
    Iterator iter = itemtextSet.iterator();
    ArrayList matchItemBeanList = new ArrayList();


    while (iter.hasNext()){
       ItemTextIfc  itemText = (ItemTextIfc) iter.next();
       MatchItemBean choicebean =  new MatchItemBean();
       choicebean.setChoice(itemText.getText());

       Set answerSet = itemText.getAnswerSet();
       Iterator iter1 = answerSet.iterator();
       while (iter1.hasNext()){
    	 AnswerIfc answer = (AnswerIfc) iter1.next();
         if (answer.getIsCorrect() != null &&
             answer.getIsCorrect().booleanValue()){
           choicebean.setMatch(answer.getText());
           choicebean.setSequence(answer.getSequence());
           choicebean.setIsCorrect(Boolean.TRUE);
           Set feedbackSet = answer.getAnswerFeedbackSet();
           Iterator iter2 = feedbackSet.iterator();
           while (iter2.hasNext()){

        	   AnswerFeedbackIfc feedback =(AnswerFeedbackIfc) iter2.next();
             if (feedback.getTypeId().equals(AnswerFeedbackIfc.CORRECT_FEEDBACK)) {
               choicebean.setCorrMatchFeedback(feedback.getText());
             }
             else if (feedback.getTypeId().equals(AnswerFeedbackIfc.INCORRECT_FEEDBACK)) {
               choicebean.setIncorrMatchFeedback(feedback.getText());
             }
           }
         }
       }
       matchItemBeanList.add(choicebean);
     }

     bean.setMatchItemBeanList(matchItemBeanList);
     //	bean.getMatchItemBeanList().size()  );


  }

  private void populateMetaData(ItemAuthorBean itemauthorbean, ItemFacade itemfacade, ItemBean bean)  {


    Set itemtextSet = itemfacade.getItemMetaDataSet();
    Iterator iter = itemtextSet.iterator();
    while (iter.hasNext()){
    	ItemMetaDataIfc meta= (ItemMetaDataIfc) iter.next();
       if (meta.getLabel().equals(ItemMetaDataIfc.OBJECTIVE)){
	 bean.setObjective(FormattedText.convertFormattedTextToPlaintext(meta.getEntry()));
       }
       if (meta.getLabel().equals(ItemMetaDataIfc.KEYWORD)){
	 bean.setKeyword(FormattedText.convertFormattedTextToPlaintext(meta.getEntry()));
       }
       if (meta.getLabel().equals(ItemMetaDataIfc.RUBRIC)){
	 bean.setRubric(FormattedText.convertFormattedTextToPlaintext(meta.getEntry()));
       }
       if (meta.getLabel().equals(ItemMetaDataIfc.RANDOMIZE)){
	 bean.setRandomized(meta.getEntry());
       }

       // for Multiple Choice Survey get survey type 
       // use PREDEFINED_SCALE
       if (meta.getLabel().equals(ItemMetaDataIfc.PREDEFINED_SCALE)){
	 bean.setScaleName(meta.getEntry());
       }

       // lydial (2/19/2006): for backward compatibility only. We used to use SCALENAME as the metadata key, while import/export used PREDEFINED_SCALE, now everything is using PREDEFINED_SCALE 
       if (meta.getLabel().equals(ItemMetaDataIfc.SCALENAME)){
	 bean.setScaleName(meta.getEntry());
         // now converting old metadata value to new ones, so that both manually created and imported assessments use the same metadata values. 
         if (ItemMetaDataIfc.SURVEY_YESNO.equals(meta.getEntry())) {
	   bean.setScaleName(ItemMetaDataIfc.SURVEY_YES);
         } 
         if (ItemMetaDataIfc.SURVEY_SCALEFIVE.equals(meta.getEntry())) {
	   bean.setScaleName(ItemMetaDataIfc.SURVEY_5);
         } 
         if (ItemMetaDataIfc.SURVEY_SCALETEN.equals(meta.getEntry())) {
	   bean.setScaleName(ItemMetaDataIfc.SURVEY_10);
         } 
       }

	// get settings for case sensitivity for fib 
        // If metadata doesn't exist, by default it is false. 
       if (meta.getLabel().equals(ItemMetaDataIfc.CASE_SENSITIVE_FOR_FIB)){
	 //bean.setCaseSensitiveForFib((new Boolean(meta.getEntry())).booleanValue());
	 bean.setCaseSensitiveForFib(Boolean.valueOf(meta.getEntry()).booleanValue());
       }

	// get settings for mutually exclusive for fib. 
        // If metadata doesn't exist, by default it is false. 
       if (meta.getLabel().equals(ItemMetaDataIfc.MUTUALLY_EXCLUSIVE_FOR_FIB)){
	 //bean.setMutuallyExclusiveForFib((new Boolean(meta.getEntry())).booleanValue());
	 bean.setMutuallyExclusiveForFib(Boolean.valueOf(meta.getEntry()).booleanValue());
       }
       
       

	// get part id for the item
       if (meta.getLabel().equals(ItemMetaDataIfc.PARTID)){
    	   // Because the PARTID in sam_publisheditemmetadata_t is not correct,
    	   // get it from itemfacade instead
    	   //bean.setSelectedSection(meta.getEntry());
    	   //bean.setOrigSection(meta.getEntry());
    	   if (itemfacade.getData().getSection() == null) {
        	   bean.setSelectedSection("0");
        	   bean.setOrigSection("0");    		   
    	   }
    	   else {
        	   bean.setSelectedSection(itemfacade.getData().getSection().getSectionId().toString());
        	   bean.setOrigSection(itemfacade.getData().getSection().getSectionId().toString());
    	   }
       }

	// get pool id for the item
       if (meta.getLabel().equals(ItemMetaDataIfc.POOLID)){
	 bean.setSelectedPool(meta.getEntry());
	 bean.setOrigPool(meta.getEntry());
       }

	// get timeallowed for audio recording item
       if (meta.getLabel().equals(ItemMetaDataIfc.TIMEALLOWED)){
	 bean.setTimeAllowed(meta.getEntry());
       }

	// get number of attempts for audio recording item
       if (meta.getLabel().equals(ItemMetaDataIfc.NUMATTEMPTS)){
	 bean.setNumAttempts(meta.getEntry());
       }


     }
  }

}
