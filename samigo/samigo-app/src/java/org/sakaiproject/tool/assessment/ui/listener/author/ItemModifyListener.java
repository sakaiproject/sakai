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
import java.util.Collections;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.AgentDataIfc;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.PublishedItemFacade;
import org.sakaiproject.tool.assessment.facade.TypeFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.PublishedItemService;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AnswerBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.CalculatedQuestionFormulaBean;
import org.sakaiproject.tool.assessment.ui.bean.author.CalculatedQuestionVariableBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ImageMapItemBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemBean;
import org.sakaiproject.tool.assessment.ui.bean.author.MatchItemBean;
import org.sakaiproject.tool.assessment.ui.bean.author.CalculatedQuestionBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.user.api.UserDirectoryService;

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
      FacesContext context = FacesContext.getCurrentInstance();
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

    try {
      ItemFacade itemfacade = delegate.getItem(itemId);

      // Check permissions: if sequence is null, the item is *not* in a pool then the poolId would be null
      if (itemauthorbean.getQpoolId() == null) {
        AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");
        // the way to get assessment ID is completely different for published and core
        // you'd think a slight variant of the published would work for core, but it generates an error
        Long assessmentId = null;
        String createdBy = null;
        if (isEditPendingAssessmentFlow) {
          Long sectionId = itemfacade.getSection().getSectionId();
          AssessmentFacade af = assessdelegate.getBasicInfoOfAnAssessmentFromSectionId(sectionId);
          assessmentId = af.getAssessmentBaseId();
          createdBy = af.getCreatedBy();
        }
        else {
          PublishedAssessmentIfc assessment = (PublishedAssessmentIfc)itemfacade.getSection().getAssessment();
          assessmentId = assessment.getPublishedAssessmentId();
          createdBy = assessment.getCreatedBy();
        }
        if (!authzBean.isUserAllowedToEditAssessment(assessmentId.toString(), createdBy, !isEditPendingAssessmentFlow)) {
          String err=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "denied_edit_assessment_error");
          context.addMessage(null,new FacesMessage(err));
          itemauthorbean.setOutcome("author");
          if (log.isDebugEnabled()) {
            log.debug("itemID " + itemId + " for assignment " + assessmentId.toString() + " is being returned null from populateItemBean because it fails isUSerAllowedToEditAssessment for " + createdBy);
          }
          return false;
        }
      }
      else {
          // This item is in a question pool
          UserDirectoryService userDirectoryService = ComponentManager.get(UserDirectoryService.class);
          String currentUserId = userDirectoryService.getCurrentUser().getId();
          QuestionPoolService qpdelegate = new QuestionPoolService();
          List<Long> poolIds = qpdelegate.getPoolIdsByItem(itemId);
          boolean authorized = false;
          poolloop:
          for (Long poolId: poolIds) {
              List agents = qpdelegate.getAgentsWithAccess(poolId);
              for (Object agent: agents) {
                  if (currentUserId.equals(((AgentDataIfc)agent).getIdString())) {
                      authorized = true;
                      break poolloop;
                  }
              }
          }
          if (!authorized) {
              String err=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "denied_edit_assessment_error");
              context.addMessage(null,new FacesMessage(err));
              itemauthorbean.setOutcome("author");
              if (log.isDebugEnabled()) {
                  log.debug("itemID " + itemId + " in pool is being returned null from populateItemBean because it fails isUSerAllowedToEditAssessment for user " + currentUserId);
              }
              return false;
          }
      }

      bean.setItemId(itemfacade.getItemId().toString());
      bean.setItemType(itemfacade.getTypeId().toString());
      itemauthorbean.setItemType(itemfacade.getTypeId().toString());

      // if the item only exists in pool, sequence = null
      if (itemfacade.getSequence()!=null) {
        itemauthorbean.setItemNo(String.valueOf(itemfacade.getSequence().intValue() ));
      }

      Double score = itemfacade.getScore();
      if (score == null)
       {
         // cover modifying an imported XML assessment that has no score yet
         score = 0.0d;
       }
      bean.setItemScore(score);
      bean.setItemScoreDisplayFlag(itemfacade.getScoreDisplayFlag() ? "true" : "false");
 
      Double minPoints = itemfacade.getMinScore();
      Double minScore;
      if (minPoints!=null && !"".equals(minPoints))
       {
    	  minScore = minPoints;
    	  bean.setItemMinScore(minScore);
       }else{
    	   bean.setItemMinScore(0.0d);
       }
      

      Double discount = itemfacade.getDiscount();
      if (discount == null)
      {
        // cover modifying an imported XML assessment that has no score yet
    	  discount = 0.0d;
      }
      bean.setItemDiscount(discount);

      // partical credit flag
      String partialCreditFlag= "FALSE";
      Boolean hasPartialCredit = itemfacade.getPartialCreditFlag();
      if (hasPartialCredit != null) {
    	  partialCreditFlag = hasPartialCredit.toString();
      } 
      bean.setPartialCreditFlag(partialCreditFlag);
           
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
      else if (Long.valueOf(itemauthorbean.getItemType()).equals(TypeFacade.EXTENDED_MATCHING_ITEMS)) {
    	  populateItemTextForEMI(itemauthorbean, itemfacade, bean);
      }
      else if (new Long(itemauthorbean.getItemType()).equals(TypeFacade.CALCULATED_QUESTION)) {
          populateItemTextForCalculatedQuestion(itemauthorbean, itemfacade, bean);          
      }
      else if (new Long(itemauthorbean.getItemType()).equals(TypeFacade.IMAGEMAP_QUESTION)) {
    	  populateItemTextForImageMapQuestion(itemauthorbean, itemfacade, bean);          
      }
      else if (new Long(itemauthorbean.getItemType()).equals(TypeFacade.MATRIX_CHOICES_SURVEY)){
    	  populateItemTextForMatrix(itemauthorbean, itemfacade, bean);
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
                        // need to get it from properties file
                        nextpage = "poolList";
                        break;
                case 13:
                	    itemauthorbean.setItemTypeString("Matrix Choices Survey");  //  need to get it from properties file
                	    nextpage = "matrixChoicesSurveyItem";
                	    break;
                case 14:
                	itemauthorbean.setItemTypeString("Extended Matching Items");  //  need to get it from properties file
                	nextpage = "emiItem";
                	break;
                case 15: // CALCULATED_QUESTION
                    itemauthorbean.setItemTypeString("Calculated Question");  //  need to get it from properties file
                    MatchItemBean variableItem = new MatchItemBean();
                    bean.setCurrentMatchPair(variableItem);
                    nextpage = "calculatedQuestionVariableItem";
                    break;
                case 16: // IMAGEMAP_QUESTION
                    itemauthorbean.setItemTypeString("Image Map Question");  //  need to get it from properties file
                    ImageMapItemBean variableItemImag = new ImageMapItemBean();
                    //bean.setCurrentImageMapPair(variableItemImag);
                    nextpage = "imageMapItem";
                    break;
        }
    }
    catch(RuntimeException e)
    {
        log.error("Could not populate ItemBean", e);
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
		  
		  /////////////////////////////////////////////////////////////
		  // Get current Answers choices
		  /////////////////////////////////////////////////////////////
		  bean.setItemText(itemText.getText());

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
		    		  // due to http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6325587  
		    	 	  // we need to call Matcher.quoteReplacement()
			    	  replaced = orig.replaceFirst("\\{\\}", java.util.regex.Matcher.quoteReplacement("{"+answerArray[i]+"}"));
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
		// making sure if there is any partial credit in place we account for that --mustansar
		if  (Long.valueOf(itemauthorbean.getItemType()).equals(TypeFacade.MULTIPLE_CHOICE)) {
			if (answerArray[i].getPartialCredit() != null) {
				answerbean.setPartialCredit(Integer.toString(answerArray[i].getPartialCredit().intValue())); 
			}
			else {
				answerbean.setPartialCredit("0"); 
			}
		}
		answerbeanlist.add(answerbean);
         }

         
/*         
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
*/

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

  private void populateItemTextForEMI(ItemAuthorBean itemauthorbean, ItemFacade itemfacade, ItemBean bean)  {
	  if (!Long.valueOf(itemauthorbean.getItemType()).equals(TypeFacade.EXTENDED_MATCHING_ITEMS)) return;
	  
	  bean.setAnswerOptionsRichCount(itemfacade.getAnswerOptionsRichCount()==null?"0":itemfacade.getAnswerOptionsRichCount().toString());
	  bean.setAnswerOptionsSimpleOrRich(itemfacade.getAnswerOptionsSimpleOrRich().toString());
	  
	  Set itemtextSet = itemfacade.getItemTextSet();
	  Iterator iter = itemtextSet.iterator();

	  ArrayList qaComboList = new ArrayList();

	  while (iter.hasNext()){
		  ItemTextIfc  itemText = (ItemTextIfc) iter.next();
		  
		  /////////////////////////////////////////////////////////////
		  // Get current Answers choices
		  /////////////////////////////////////////////////////////////
			  if (itemText.getSequence().equals(ItemTextIfc.EMI_THEME_TEXT_SEQUENCE)) {
				  bean.setItemText(itemText.getText());
				  continue;
			  }
			  else if (itemText.getSequence().equals(ItemTextIfc.EMI_LEAD_IN_TEXT_SEQUENCE)) {
				  bean.setLeadInStatement(itemText.getText());
				  continue;
			  }
			  else if (itemText.getSequence().equals(ItemTextIfc.EMI_ANSWER_OPTIONS_SEQUENCE)) {
				  bean.setEmiAnswerOptionsRich(itemText.getText());
				  ArrayList answerOptionsList = new ArrayList();
				  Set answerobjlist = itemText.getAnswerSet();
				  Iterator ansIter = answerobjlist.iterator();
				  while(ansIter.hasNext())
				  {
					  AnswerIfc answerobj = (AnswerIfc) ansIter.next();
					  AnswerBean answerbean = new AnswerBean();
					  answerbean.setText(answerobj.getText());
					  answerbean.setSequence(answerobj.getSequence());
					  answerbean.setLabel(answerobj.getLabel());
					  answerOptionsList.add(answerbean);
				  }
				  Collections.sort(answerOptionsList);
				  bean.setEmiAnswerOptions(answerOptionsList);
				  continue;
			  }
			  else { // Actual Answers - EMI QA Combo Items
				  AnswerBean answerbean = new AnswerBean();
				  answerbean.setText(itemText.getText());
				  answerbean.setSequence(itemText.getSequence());
				  answerbean.setLabel(itemText.getSequence().toString());
				  answerbean.setRequiredOptionsCount(itemText.getRequiredOptionsCount().toString());
				  
				  Set answerobjlist = itemText.getAnswerSet();
				  String afeedback =  "" ;
				  Iterator ansIter = answerobjlist.iterator();
				  //need to check sequence no, since this answerSet returns answers in random order
				  int count = answerobjlist.size();
				  AnswerIfc[] answerArray = new AnswerIfc[count];
				  int seq = 0;
				  List correctAnswerOptions = new ArrayList();
				  Double itemAnswerScore = 0.0;
				  boolean itemScoreUserSet = false;
				  while(ansIter.hasNext())
				  {
					  AnswerIfc answerobj = (AnswerIfc) ansIter.next();
					  if (answerobj.getIsCorrect()) {
						  correctAnswerOptions.add(answerobj.getLabel());
						  itemAnswerScore = answerobj.getScore();
						  itemScoreUserSet = "user".equals(answerobj.getGrade());
					  }
				  }
				  if(itemAnswerScore == null){
					  itemAnswerScore = 0.0;
				  }
				  int correctRequiredCount = correctAnswerOptions.size()<itemText.getRequiredOptionsCount().intValue()?
						  correctAnswerOptions.size():itemText.getRequiredOptionsCount().intValue();
				  answerbean.setScore(((double)Math.round((itemAnswerScore * correctRequiredCount)*100))/100);
				  answerbean.setScoreUserSet(itemScoreUserSet);
				  Collections.sort(correctAnswerOptions);
				  Iterator correctAnsLabels = correctAnswerOptions.iterator();
				  String correctOptionLabels = "";
				  while(correctAnsLabels.hasNext())
				  {
					  correctOptionLabels += (String) correctAnsLabels.next();
					  /*
					  if (correctAnsLabels.hasNext()) {
						  correctOptionLabels += ",";
					  }
					  */
				  }
				  answerbean.setCorrectOptionLabels(correctOptionLabels);

			      Set<ItemTextAttachmentIfc> attachmentSet = itemfacade.getItemTextBySequence(answerbean.getSequence()).getItemTextAttachmentSet();
			      ArrayList<ItemTextAttachmentIfc> attachmentList = new ArrayList<ItemTextAttachmentIfc>();
			      for (Iterator<ItemTextAttachmentIfc> it = attachmentSet.iterator(); it.hasNext();) {
			    	  attachmentList.add(it.next());
			      }
				  answerbean.setAttachmentList(attachmentList);
				  answerbean.setResourceHash(null);
				  qaComboList.add(answerbean);
			  }
			  continue;
		  
		  /////////////////////////////////////////////////////////////
		  // Finish Answers
		  /////////////////////////////////////////////////////////////

	  } // looping through itemtextSet , only loop once for these types,

	  Collections.sort(qaComboList);
	  bean.setEmiQuestionAnswerCombinations(qaComboList);
}

  private void populateItemTextForMatrix(ItemAuthorBean itemauthorbean, ItemFacade itemfacade, ItemBean bean){
	  Set itemtextSet = itemfacade.getItemTextSet();
	  Iterator iter = itemtextSet.iterator();    
	  StringBuffer rowChoices = new StringBuffer();
	  while (iter.hasNext()){
		  ItemTextIfc  itemText = (ItemTextIfc) iter.next();
		  if (!"".equals(itemText.getText())){
			  rowChoices = rowChoices.append(itemText.getText());
			  rowChoices = rowChoices.append(System.getProperty("line.separator"));
		  }
		  Set answerSet = itemText.getAnswerSet();
		  Iterator iter1 = answerSet.iterator();
		  StringBuffer columnChoices = new StringBuffer();
		  while (iter1.hasNext()){
			  AnswerIfc answer = (AnswerIfc) iter1.next();
			  if (!"".equals(answer.getText())){
				  columnChoices = columnChoices.append(answer.getText());
				  columnChoices = columnChoices.append(System.getProperty("line.separator"));
			  }
		  }
		  bean.setColumnChoices(columnChoices.toString());
	  } 
	  bean.setRowChoices(rowChoices.toString()); 
	  bean.setItemText(itemfacade.getText());
  }

  private void populateItemTextForCalculatedQuestion(ItemAuthorBean itemauthorbean, ItemFacade itemfacade, ItemBean bean) {
      CalculatedQuestionBean calcQuestionBean = new CalculatedQuestionBean();
      String instructions = itemfacade.getInstruction();
      GradingService gs = new GradingService();
      List<String> variables = gs.extractVariables(instructions);
      List<ItemTextIfc> list = itemfacade.getItemTextArray();
      for (ItemTextIfc itemBean : list) {
          if (variables.contains(itemBean.getText())) {
              CalculatedQuestionVariableBean variable = new CalculatedQuestionVariableBean();
              List<AnswerIfc> answers = itemBean.getAnswerArray();
              for (AnswerIfc answer : answers) {
                  if (answer.getIsCorrect()) {
                      String text = answer.getText();
                      String min = text.substring(0, text.indexOf("|"));
                      String max = text.substring(text.indexOf("|") + 1, text.indexOf(","));
                      String decimalPlaces = text.substring(text.indexOf(",") + 1);              
                      variable.setName(itemBean.getText());
                      variable.setSequence(itemBean.getSequence());
                      variable.setMin(min);
                      variable.setMax(max);
                      variable.setDecimalPlaces(decimalPlaces);
                      calcQuestionBean.addVariable(variable);
                      break;
                  }
              }
          } else {
              CalculatedQuestionFormulaBean formula = new CalculatedQuestionFormulaBean();
              List<AnswerIfc> answers = itemBean.getAnswerArray();
              for (AnswerIfc answer : answers) {
                  if (answer.getIsCorrect()) {
                      String text = answer.getText();
                      String formulaStr = text.substring(0, text.indexOf("|"));
                      String tolerance = text.substring(text.indexOf("|") + 1, text.indexOf(","));
                      String decimalPlaces = text.substring(text.indexOf(",") + 1);
                      formula.setName(itemBean.getText());
                      formula.setSequence(itemBean.getSequence());
                      formula.setText(formulaStr);
                      formula.setTolerance(tolerance);
                      formula.setDecimalPlaces(decimalPlaces);
                      calcQuestionBean.addFormula(formula);
                      break;
                  }
              }
          }
      }
      // extract the calculation formulas and populate the calcQuestionBean (we are ignoring the error returns for now)
      CalculatedQuestionExtractListener.createCalculationsFromInstructions(calcQuestionBean, instructions, gs);
      CalculatedQuestionExtractListener.validateCalculations(calcQuestionBean, gs);
      bean.setCalculatedQuestion(calcQuestionBean);
  }
  
  private void populateItemTextForImageMapQuestion(ItemAuthorBean itemauthorbean, ItemFacade itemfacade, ItemBean bean)  {

	  Set itemtextSet = itemfacade.getItemTextSet();
	    Iterator<ItemTextIfc> choiceIter = itemtextSet.iterator();
	    ArrayList<ImageMapItemBean> imageMapItemBeanList = new ArrayList<ImageMapItemBean>();

	    // once a match has been assigned to a choice, subsequent matches set the controlling sequences
	    Map<String, ImageMapItemBean> alreadyMatched = new HashMap<String, ImageMapItemBean>(); 
	    ////REVISAR La ANTERIOR
	    // loop through all choices
	    while (choiceIter.hasNext()){
	       ItemTextIfc itemText = choiceIter.next();
	       ImageMapItemBean choicebean =  new ImageMapItemBean();
	       choicebean.setChoice(itemText.getText());
	       choicebean.setSequence(itemText.getSequence());
	       choicebean.setSequenceStr(itemText.getSequence().toString());
	       Set<AnswerIfc> answerSet = itemText.getAnswerSet();
	       Iterator<AnswerIfc> answerIter = answerSet.iterator();
	       
	       // loop through all matches
	       while (answerIter.hasNext()){
	    	 AnswerIfc answer = answerIter.next();
	         if (answer.getIsCorrect() != null &&
	             answer.getIsCorrect().booleanValue()){
	           choicebean.setMatch(answer.getText());
	           choicebean.setIsCorrect(Boolean.TRUE);
	           
	           // if match has been used already, set the controlling sequence
	           if (alreadyMatched.containsKey(answer.getLabel())) {
	        	   ImageMapItemBean matchBean = alreadyMatched.get(answer.getLabel());
	        	   choicebean.setControllingSequence(matchBean.getSequenceStr());
	           } else {
	        	   alreadyMatched.put(answer.getLabel(), choicebean);
	           }
	           
	           // add feedback
	           Set<AnswerFeedbackIfc> feedbackSet = answer.getAnswerFeedbackSet();
	           Iterator<AnswerFeedbackIfc> feedbackIter = feedbackSet.iterator();
	           while (feedbackIter.hasNext()){

	        	   AnswerFeedbackIfc feedback = feedbackIter.next();
	             if (feedback.getTypeId().equals(AnswerFeedbackIfc.CORRECT_FEEDBACK)) {
	               choicebean.setCorrImageMapFeedback(feedback.getText());
	             }
	             else if (feedback.getTypeId().equals(AnswerFeedbackIfc.INCORRECT_FEEDBACK)) {
	               choicebean.setIncorrImageMapFeedback(feedback.getText());
	             }
	           }
	         }
	       }
	       
	       // if match was not found, must be a distractor
	       /*if (choicebean.getMatch() == null || "".equals(choicebean.getMatch())) {
	    	   choicebean.setMatch(MatchItemBean.CONTROLLING_SEQUENCE_DISTRACTOR);
	    	   choicebean.setIsCorrect(Boolean.TRUE);
	    	   choicebean.setControllingSequence(MatchItemBean.CONTROLLING_SEQUENCE_DISTRACTOR);
	       }*/
	       imageMapItemBeanList.add(choicebean);
	     }

	     bean.setImageMapItemBeanList(imageMapItemBeanList);
  }
  
 private void populateItemTextForMatching(ItemAuthorBean itemauthorbean, ItemFacade itemfacade, ItemBean bean)  {

    Set itemtextSet = itemfacade.getItemTextSet();
    Iterator<ItemTextIfc> choiceIter = itemtextSet.iterator();
    ArrayList<MatchItemBean> matchItemBeanList = new ArrayList<MatchItemBean>();

    // once a match has been assigned to a choice, subsequent matches set the controlling sequences
    Map<String, MatchItemBean> alreadyMatched = new HashMap<String, MatchItemBean>();

    // loop through all choices
    while (choiceIter.hasNext()){
       ItemTextIfc itemText = choiceIter.next();
       MatchItemBean choicebean =  new MatchItemBean();
       choicebean.setChoice(itemText.getText());
       choicebean.setSequence(itemText.getSequence());
       choicebean.setSequenceStr(itemText.getSequence().toString());
       Set<AnswerIfc> answerSet = itemText.getAnswerSet();
       Iterator<AnswerIfc> answerIter = answerSet.iterator();
       
       // loop through all matches
       while (answerIter.hasNext()){
    	 AnswerIfc answer = answerIter.next();
         if (answer.getIsCorrect() != null &&
             answer.getIsCorrect().booleanValue()){
           choicebean.setMatch(answer.getText());
           choicebean.setIsCorrect(Boolean.TRUE);
           
           // if match has been used already, set the controlling sequence
           if (alreadyMatched.containsKey(answer.getLabel())) {
        	   MatchItemBean matchBean = alreadyMatched.get(answer.getLabel());
        	   choicebean.setControllingSequence(matchBean.getSequenceStr());
           } else {
        	   alreadyMatched.put(answer.getLabel(), choicebean);
           }
           
           // add feedback
           Set<AnswerFeedbackIfc> feedbackSet = answer.getAnswerFeedbackSet();
           Iterator<AnswerFeedbackIfc> feedbackIter = feedbackSet.iterator();
           while (feedbackIter.hasNext()){

        	   AnswerFeedbackIfc feedback = feedbackIter.next();
             if (feedback.getTypeId().equals(AnswerFeedbackIfc.CORRECT_FEEDBACK)) {
               choicebean.setCorrMatchFeedback(feedback.getText());
             }
             else if (feedback.getTypeId().equals(AnswerFeedbackIfc.INCORRECT_FEEDBACK)) {
               choicebean.setIncorrMatchFeedback(feedback.getText());
             }
           }
         }
       }
       
       // if match was not found, must be a distractor
       if (choicebean.getMatch() == null || "".equals(choicebean.getMatch())) {
    	   choicebean.setMatch(MatchItemBean.CONTROLLING_SEQUENCE_DISTRACTOR);
    	   choicebean.setIsCorrect(Boolean.TRUE);
    	   choicebean.setControllingSequence(MatchItemBean.CONTROLLING_SEQUENCE_DISTRACTOR);
       }
       matchItemBeanList.add(choicebean);
     }

     bean.setMatchItemBeanList(matchItemBeanList);
  }

  private void populateMetaData(ItemAuthorBean itemauthorbean, ItemFacade itemfacade, ItemBean bean)  {


    Set itemtextSet = itemfacade.getItemMetaDataSet();
    Iterator iter = itemtextSet.iterator();
    
    boolean hasSetPartId =  false;
    
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
       if (meta.getLabel().equals(ItemMetaDataIfc.REQUIRE_ALL_OK)){
    		 bean.setRequireAllOk(meta.getEntry());
    	       }
    	       if (meta.getLabel().equals(ItemMetaDataIfc.IMAGE_MAP_SRC)){
    	    		 bean.setImageMapSrc(meta.getEntry());
    	       }
       if (meta.getLabel().equals(ItemMetaDataIfc.MCMS_PARTIAL_CREDIT)){
    	   bean.setMcmsPartialCredit(meta.getEntry());
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
    	   bean.setCaseSensitiveForFib(Boolean.valueOf(meta.getEntry()).booleanValue());
       }

       // get settings for mutually exclusive for fib. 
       // If metadata doesn't exist, by default it is false. 
       if (meta.getLabel().equals(ItemMetaDataIfc.MUTUALLY_EXCLUSIVE_FOR_FIB)){
    	   bean.setMutuallyExclusiveForFib(Boolean.valueOf(meta.getEntry()).booleanValue());
       }
       
       // If metadata doesn't exist, by default it is false. 
       if (meta.getLabel().equals(ItemMetaDataIfc.MUTUALLY_EXCLUSIVE_FOR_FIB)){
    	   bean.setMutuallyExclusiveForFib(Boolean.valueOf(meta.getEntry()).booleanValue());
       }

       // get settings for add_to_favorites for matrix 
       // If metadata doesn't exist, by default it is false. 
       if (meta.getLabel().equals(ItemMetaDataIfc.ADD_TO_FAVORITES_MATRIX)){
    	   bean.setAddToFavorite(Boolean.valueOf(meta.getEntry()).booleanValue());
       }
       if (meta.getLabel().equals(ItemMetaDataIfc.ADD_COMMENT_MATRIX)){
    	   bean.setAddComment(Boolean.valueOf(meta.getEntry()).booleanValue());
       }
       if (meta.getLabel().equals(ItemMetaDataIfc.FORCE_RANKING)){
    	   bean.setForceRanking(Boolean.valueOf(meta.getEntry()).booleanValue());
       }
       if (meta.getLabel().equals(ItemMetaDataIfc.MX_SURVEY_QUESTION_COMMENTFIELD)){
    	   bean.setCommentField(meta.getEntry());
       }
       if (meta.getLabel().equals(ItemMetaDataIfc.MX_SURVEY_RELATIVE_WIDTH)){
    	   bean.setRelativeWidth(Integer.valueOf(meta.getEntry()).intValue());
       }

       // get part id for the item
       if (meta.getLabel().equals(ItemMetaDataIfc.PARTID)){
    	   hasSetPartId = true;
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
    
    if (!hasSetPartId && itemfacade != null && itemfacade instanceof PublishedItemFacade &&  
    	itemfacade.getData()!= null && itemfacade.getData().getSection() != null && 
    	itemfacade.getData().getSection().getSectionId() != null) {
    	bean.setSelectedSection(itemfacade.getData().getSection().getSectionId().toString());
    }
  }

}
