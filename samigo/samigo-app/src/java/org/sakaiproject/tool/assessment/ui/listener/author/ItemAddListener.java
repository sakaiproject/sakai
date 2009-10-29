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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.tool.assessment.data.dao.assessment.Answer;
import org.sakaiproject.tool.assessment.data.dao.assessment.AnswerFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemText;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAnswer;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAnswerFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemText;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.PublishedItemFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.facade.TypeFacade;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.PublishedItemService;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AnswerBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemBean;
import org.sakaiproject.tool.assessment.ui.bean.author.MatchItemBean;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolBean;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolDataBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.util.FormattedText;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Organization: Sakai Project</p>
 */

public class ItemAddListener
    implements ActionListener {

  private static Log log = LogFactory.getLog(ItemAddListener.class);
  //private static ContextUtil cu;
  //private String scalename; // used for multiple choice Survey
  private boolean error = false;
  private boolean isPendingOrPool = false;
  private boolean isEditPendingAssessmentFlow = true;

  /**
   * Standard process action method.
   * @param ae ActionEvent
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws AbortProcessingException {

    //boolean correct=false;

    log.debug("ItemAdd LISTENER.");

    ItemAuthorBean itemauthorbean = (ItemAuthorBean) ContextUtil.lookupBean("itemauthor");
    ItemBean item = itemauthorbean.getCurrentItem();
    String iText = ContextUtil.stringWYSIWYG(item.getItemText());
    String iInstruction = ContextUtil.stringWYSIWYG(item.getInstruction());
    String iType = item.getItemType();
    String err="";
    FacesContext context=FacesContext.getCurrentInstance();
   
    // SAK-6050
    // if((!iType.equals(TypeFacade.MATCHING.toString())&&((iText==null)||(iText.replaceAll("<.*?>", "").trim().equals(""))))|| (iType.equals(TypeFacade.MATCHING.toString()) && ((iInstruction==null)||(iInstruction.replaceAll("<.*?>", "").trim().equals(""))))){
    if((!iType.equals(TypeFacade.MATCHING.toString())&&((iText==null)||(iText.toLowerCase().replaceAll("<^[^(img)]*?>", "").trim().equals(""))))|| (iType.equals(TypeFacade.MATCHING.toString()) && ((iInstruction==null)||(iInstruction.toLowerCase().replaceAll("<^[^(img)]*?>", "").trim().equals(""))))){ 
	
 
	String emptyText_err = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","emptyText_error");     
	context.addMessage(null,new FacesMessage(emptyText_err));
	return;

    }   
   
    if(iType.equals(TypeFacade.MULTIPLE_CHOICE.toString()))
	checkMC(true);

    if(iType.equals(TypeFacade.MULTIPLE_CORRECT.toString()))
	checkMC(false);
    
    if(iType.equals(TypeFacade.MULTIPLE_CORRECT_SINGLE_SELECTION.toString()))
    checkMC(false);
    
    if(iType.equals(TypeFacade.MATCHING.toString()))
        {   
            ArrayList l=item.getMatchItemBeanList();
	    if (l==null || l.size()==0){
		String noPairMatching_err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","noMatchingPair_error");
		context.addMessage(null,new FacesMessage(noPairMatching_err));
		error=true;
	    }
	}
    if(error)
	return;
    
    if(iType.equals(TypeFacade.MULTIPLE_CHOICE_SURVEY.toString()))
    {   
      String scaleName = item.getScaleName();
      if (scaleName == null){
	    err = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","corrAnswer");
	    context.addMessage(null,new FacesMessage(err));
	    item.setOutcome("surveyItem");
		item.setPoolOutcome("surveyItem");
	    return;
      }
    }
    
    if(iType.equals(TypeFacade.TRUE_FALSE.toString()))
    {   
      String corrAnswer = item.getCorrAnswer();
      if (corrAnswer == null){
	    err = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","corrAnswer");
	    context.addMessage(null,new FacesMessage(err));
	    item.setOutcome("trueFalseItem");
		item.setPoolOutcome("trueFalseItem");
	    return;
      }
    }
    
    if(iType.equals(TypeFacade.FILL_IN_BLANK.toString())){
	
    	if(isErrorFIB()){
    		err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","pool_missingBracket_error");
    		context.addMessage(null,new FacesMessage(err));
    		item.setOutcome("fillInBlackItem");
    		item.setPoolOutcome("fillInBlackItem");
    		return;
    	}
    }
    
    if(iType.equals(TypeFacade.FILL_IN_NUMERIC.toString())){
    	
    	if(isErrorFIN()){
    	    err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","pool_missingBracket_error");
    	    context.addMessage(null,new FacesMessage(err));
    	    item.setOutcome("fillInNumericItem");
    	    item.setPoolOutcome("fillInNumericItem");
    	    return;

    	}
    }
    
    if(iType.equals(TypeFacade.AUDIO_RECORDING.toString())){
    	try {
	   		String timeAllowed = item.getTimeAllowed().trim();
	   		int intTimeAllowed = Integer.parseInt(timeAllowed);
	   		if (intTimeAllowed < 1) {
	   			throw new RuntimeException();
	   		}
    	}
		catch (RuntimeException e){
			err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","submissions_allowed_error");
    	    context.addMessage(null,new FacesMessage(err));
    	    item.setOutcome("audioRecItem");
    	    item.setPoolOutcome("audioRecItem");
    	    return;
		}    	
    }
	
    if (!saveItem(itemauthorbean)){
	throw new RuntimeException("failed to saveItem.");
    }
    item.setOutcome("editAssessment");
    item.setPoolOutcome("editPool");
    itemauthorbean.setItemTypeString("");
  }
    

  public void checkMC(boolean isSingleSelect){
	  ItemAuthorBean itemauthorbean = (ItemAuthorBean) ContextUtil.lookupBean("itemauthor");
	  ItemBean item =itemauthorbean.getCurrentItem();
	  boolean correct=false;
	  int countAnswerText=0;
	  //String[] choiceLabels= {"A", "B", "C", "D", "E", "F","G", "H","I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
	  int indexLabel= 0;
	  //   List label = new List();
	  Iterator iter = item.getMultipleChoiceAnswers().iterator();
	  boolean missingchoices=false;

	  StringBuilder missingLabelbuf = new StringBuilder();


	  //String missingLabel="";
	  String txt="";
	  String label="";
	  FacesContext context=FacesContext.getCurrentInstance();
	  int corrsize = item.getMultipleChoiceAnswers().size();
	  String[] corrChoices = new String[corrsize];
	  int counter=0;
	  boolean isCorrectChoice = false;
	  if(item.getMultipleChoiceAnswers()!=null){
		  while (iter.hasNext()) {
			  AnswerBean answerbean = (AnswerBean) iter.next();
			  String answerTxt=ContextUtil.stringWYSIWYG(answerbean.getText());
			  //  if(answerTxt.replaceAll("<.*?>", "").trim().equals(""))        
			  // SAK-6050
			  if(answerTxt.toLowerCase().replaceAll("<^[^(img)]*?>", "").trim().equals("")) {
				  answerbean.setText("");
			  }

			  label = answerbean.getLabel();
			  txt=answerbean.getText();

			  corrChoices[counter]=label;
			  isCorrectChoice = isCorrectChoice(item,label);
			  if(isCorrectChoice && ((txt==null) ||(txt.equals("")))){          
				  error=true;
				  String empty_correct_err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","empty_correct_error");
				  context.addMessage(null,new FacesMessage(empty_correct_err+label));

			  }

			  if ((txt!=null)&& (!txt.equals(""))) {
				  countAnswerText++;
				  if(isCorrectChoice){
					  correct=true;
					  counter++;
				  }

				  if(!label.equals(AnswerBean.getChoiceLabels()[indexLabel])){
					  missingchoices= true;
					  if( "".equals(missingLabelbuf.toString()))
						  missingLabelbuf.append(" "+AnswerBean.getChoiceLabels()[indexLabel]);
					  else
						  missingLabelbuf.append(", "+AnswerBean.getChoiceLabels()[indexLabel]);           
					  indexLabel++;
				  }
				  indexLabel++;
			  }
		  } // end of while
	    
	    String missingLabel = missingLabelbuf.toString();
	    // Fixed for 7208
	    // Following the above logic, at this point, no matter the last choice (lable is corrChoices[counter])
	    // is a correct answer or not, it will be the last value in array corrChoice[].
	    // Therefore, make a call to isCorrectChoice() to see if it is indeed a correct choice
	    if (counter < corrChoices.length && !isCorrectChoice(item, corrChoices[counter])) {
	    	corrChoices[counter] = null;
	    }
	    item.setCorrAnswers(corrChoices);
	    if(!error){
        
	    if(correct==false){
                if(isSingleSelect){
		    String singleCorrect_error=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","singleCorrect_error");
                    context.addMessage(null,new FacesMessage(singleCorrect_error));
	
		}
                else{
		    String multiCorrect_error=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","multiCorrect_error");
		    context.addMessage(null,new FacesMessage(multiCorrect_error));
                
		}
		error=true;

	    } else if(countAnswerText<=1){
		String answerList_err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","answerList_error");
		context.addMessage(null,new FacesMessage(answerList_err));
		error=true;

	    }
	    else if(missingchoices){
      
            	String selectionError=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","missingChoices_error");
		context.addMessage(null,new FacesMessage(selectionError+missingLabel));
		error=true;
	
	    }
	
           
	    }
	}
	if(error){
	    item.setOutcome("multipleChoiceItem");
            item.setPoolOutcome("multipleChoiceItem");
	}


    }

    public boolean isErrorFIB() {
	ItemAuthorBean itemauthorbean = (ItemAuthorBean) ContextUtil.lookupBean("itemauthor");
	ItemBean item =itemauthorbean.getCurrentItem();
	int index=0;
	boolean FIBerror=false;
//	String err="";
	boolean hasOpen=false;
	int opencount=0;
	int closecount=0;
	boolean notEmpty=false;
	int indexOfOpen=-1;
	String text=item.getItemText();
	while(index<text.length()){ 
	    char c=text.charAt(index);
	    if(c=='{'){
		opencount++;
		if(hasOpen){
		    FIBerror=true;
		    break;
		}
		else{
		    hasOpen=true;
		    indexOfOpen=index;
		}
	    }
	    else if(c=='}'){
		closecount++;
		if(!hasOpen){
		    FIBerror=true;
		    break;
		}
		else{
		    if((notEmpty==true)&&(index+1 !=index)&&(!(text.substring(indexOfOpen+1,index).equals("</p><p>")))){
		       hasOpen=false;
                       notEmpty=false;
		    }
		    else{
		    //error for emptyString
			FIBerror=true;
			break;
		   }

		}
	    }
       
	    else{
           
		if((hasOpen==true)&&(Character.getType(c)!=12) &&(Character.getType(c)!=25)){
	    	notEmpty=true; 
		}
	    }
	
	
	    index++;
     }//end while
    if((hasOpen==true)||(opencount<1)||(opencount!=closecount)||(FIBerror==true)){
	return true;
    }
    else{ 
	return false;
    }
}
	
    public boolean isErrorFIN() {
    	ItemAuthorBean itemauthorbean = (ItemAuthorBean) ContextUtil.lookupBean("itemauthor");
    	ItemBean item =itemauthorbean.getCurrentItem();
    	int index=0;
    	boolean FINerror=false;
    	//String err="";
    	boolean hasOpen=false;
    	int opencount=0;
    	int closecount=0;
    	boolean notEmpty=false;
    	int indexOfOpen=-1;
    	String text=item.getItemText();
    	while(index<text.length()){ 
    	    char c=text.charAt(index);
    	    if(c=='{'){
    		opencount++;
    		if(hasOpen){
    		    FINerror=true;
    		    break;
    		}
    		else{
    		    hasOpen=true;
    		    indexOfOpen=index;
    		}
    	    }
    	    else if(c=='}'){
    		closecount++;
    		if(!hasOpen){
    		    FINerror=true;
    		    break;
    		}
    		else{
    		    if((notEmpty==true)&&(index+1 !=index)&&(!(text.substring(indexOfOpen+1,index).equals("</p><p>")))){
    		       hasOpen=false;
                           notEmpty=false;
    		    }
    		    else{
    		    //error for emptyString
    			FINerror=true;
    			break;
    		   }

    		}
    	    }
           
    	    else{
               
    		if((hasOpen==true)&&(Character.getType(c)!=12) &&(Character.getType(c)!=25)){
    	    	notEmpty=true; 
    		}
    	    }
    	
    	
    	    index++;
         }//end while
        if((hasOpen==true)||(opencount<1)||(opencount!=closecount)||(FINerror==true)){
    	return true;
        }
        else{ 
    	return false;
        }
    }
    
  

  public boolean saveItem(ItemAuthorBean itemauthor) {
    boolean update = false;
    try {
      ItemBean bean = itemauthor.getCurrentItem();
      ItemFacade item;
      AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
      isEditPendingAssessmentFlow = author.getIsEditPendingAssessmentFlow();
      log.debug("**** isEditPendingAssessmentFlow : " + isEditPendingAssessmentFlow);
      String target = itemauthor.getTarget();
      boolean isFromQuestionPool = false;
      if (target != null && target.equals(ItemAuthorBean.FROM_QUESTIONPOOL)) {
    	  isFromQuestionPool = true;
      }
      log.debug("**** isFromQuestionPool : " + isFromQuestionPool);
      isPendingOrPool = isEditPendingAssessmentFlow || isFromQuestionPool;
      ItemService delegate;
      if (isPendingOrPool) {
    	  EventTrackingService.post(EventTrackingService.newEvent("sam.assessment.revise", "itemId=" + itemauthor.getItemId(), true));
      	  delegate = new ItemService();
      }
      else {
    	  EventTrackingService.post(EventTrackingService.newEvent("sam.pubassessment.revise", "itemId=" + itemauthor.getItemId(), true));
      	  delegate = new PublishedItemService();
      }
      // update not working yet, delete, then add
      if ( (bean.getItemId() != null) && (!bean.getItemId().equals("0"))) {
        update = true;
        // if modify ,itemid shouldn't be null , or 0.
        Long oldId = Long.valueOf(bean.getItemId());
        if (isPendingOrPool) {
        	delegate.deleteItemContent(oldId, AgentFacade.getAgentString());
        }
    	item = delegate.getItem(oldId,AgentFacade.getAgentString());
      }
      else{
     	if (isPendingOrPool) {
     		item = new ItemFacade();
     	}
     	else {
     		item = new PublishedItemFacade();
     	}
      }
      item.setScore(Float.valueOf(bean.getItemScore()));
      item.setDiscount(Float.valueOf(bean.getItemDiscount()));
      item.setHint("");

      item.setStatus(ItemDataIfc.ACTIVE_STATUS);

      item.setTypeId(Long.valueOf(bean.getItemType()));

      item.setCreatedBy(AgentFacade.getAgentString());
      item.setCreatedDate(new Date());
      item.setLastModifiedBy(AgentFacade.getAgentString());
      item.setLastModifiedDate(new Date());

      if (bean.getInstruction() != null) {
        // for matching
        item.setInstruction(bean.getInstruction());
      }
      // update hasRationale
      if (bean.getRationale() != null) {
        item.setHasRationale(Boolean.valueOf(bean.getRationale()));
      }
      else {
        item.setHasRationale(Boolean.FALSE);
      }

      // update maxNumAttempts for audio
      if (bean.getNumAttempts() != null) {
        item.setTriesAllowed(Integer.valueOf(bean.getNumAttempts()));
      }

      // save timeallowed for audio recording
      if (bean.getTimeAllowed() != null) {
        item.setDuration(Integer.valueOf(bean.getTimeAllowed()));
      }

      if (update && !isPendingOrPool) {
    	  //prepare itemText, including answers
            item.setItemTextSet(preparePublishedText(item, bean, delegate));
         
          // prepare MetaData
          item.setItemMetaDataSet(preparePublishedMetaData(item, bean));

          // prepare feedback, because this is UPDATE
          // if it's an empty string, we need to update feedback to an empty string
          // not like below (below we don't ADD if the feedback is null or empty string)
          if ((bean.getCorrFeedback() != null)) {
            		updateItemFeedback(item, ItemFeedbackIfc.CORRECT_FEEDBACK, stripPtags(bean.getCorrFeedback()));
              }
              if ((bean.getIncorrFeedback() != null)) {
                	updateItemFeedback(item, ItemFeedbackIfc.INCORRECT_FEEDBACK, stripPtags(bean.getIncorrFeedback()));
              }
              if ((bean.getGeneralFeedback() != null)) {
                	updateItemFeedback(item, ItemFeedbackIfc.GENERAL_FEEDBACK, stripPtags(bean.getGeneralFeedback()));
              }
      }
      else {
        	//prepare itemText, including answers
            if (!item.getTypeId().equals(TypeFacade.MATCHING)) {
              item.setItemTextSet(prepareText(item, bean, itemauthor));
            }
            else {
              item.setItemTextSet(prepareTextForMatching(item, bean, itemauthor));
            }
            // prepare MetaData
            item.setItemMetaDataSet(prepareMetaData(item, bean));

            // prepare feedback, only store if feedbacks are not empty
            if ( (bean.getCorrFeedback() != null) &&
      			  (!bean.getCorrFeedback().equals(""))) {
            	item.setCorrectItemFeedback(stripPtags(bean.getCorrFeedback()));
      	  	}
      	  	if ( (bean.getIncorrFeedback() != null) &&
      			  (!bean.getIncorrFeedback().equals(""))) {
      	  		item.setInCorrectItemFeedback(stripPtags(bean.getIncorrFeedback()));
      	  	}
      	  	if ( (bean.getGeneralFeedback() != null) &&
      			  (!bean.getGeneralFeedback().equals(""))) {
      	  		item.setGeneralItemFeedback(stripPtags(bean.getGeneralFeedback()));
      	  	}
      }

      if (isFromQuestionPool) {
        // Came from Pool manager

        delegate.saveItem(item);

       // added by daisyf, 10/10/06
       updateAttachment(item.getItemAttachmentList(), itemauthor.getAttachmentList(),
                        (ItemDataIfc)item.getData(), true);
       item = delegate.getItem(item.getItemId().toString());


        QuestionPoolService qpdelegate = new QuestionPoolService();

        if (!qpdelegate.hasItem(item.getItemIdString(),
        		Long.valueOf(itemauthor.getQpoolId()))) {
          qpdelegate.addItemToPool(item.getItemIdString(),
                                   Long.valueOf(itemauthor.getQpoolId()));

        }

        QuestionPoolBean qpoolbean = (QuestionPoolBean) ContextUtil.lookupBean("questionpool");
        QuestionPoolDataBean contextCurrentPool = qpoolbean.getCurrentPool();
       
        qpoolbean.buildTree();

        /*
            // Reset question pool bean
            QuestionPoolFacade thepool= qpdelegate.getPool(new Long(itemauthor.getQpoolId()), AgentFacade.getAgentString());
            qpoolbean.getCurrentPool().setNumberOfQuestions(thepool.getQuestionSize().toString());
         */
        qpoolbean.startEditPoolAgain(itemauthor.getQpoolId());
        QuestionPoolDataBean currentPool = qpoolbean.getCurrentPool();
        currentPool.setDisplayName(contextCurrentPool.getDisplayName());
        currentPool.setOrganizationName(contextCurrentPool.getOrganizationName());
        currentPool.setDescription(contextCurrentPool.getDescription());
        currentPool.setObjectives(contextCurrentPool.getObjectives());
        currentPool.setKeywords(contextCurrentPool.getKeywords());
        
        ArrayList addedQuestions = qpoolbean.getAddedQuestions();
        if (addedQuestions == null) {
        	addedQuestions = new ArrayList();
        }
        addedQuestions.add(item.getItemId());
        qpoolbean.setAddedPools(addedQuestions);
        // return to edit pool
        itemauthor.setOutcome("editPool");
      }
      // Came from Questionbank Authoring
      else if (itemauthor.getTarget() != null && (itemauthor.getTarget().equals("sambank"))) {
		delegate.saveItem(item);
		itemauthor.setItemNo(item.getItemId().toString());
      }
      else {
        // Came from Assessment Authoring

        AssessmentService assessdelegate;
        if (isEditPendingAssessmentFlow) {
        	assessdelegate = new AssessmentService();
        }
        else {
        	assessdelegate = new PublishedAssessmentService();
        }
        // add the item to the specified part, otherwise add to default
        if (bean.getSelectedSection() != null) {
// need to do  add a temp part first if assigned to a temp part SAK-2109
 
          SectionFacade section;

	  if ("-1".equals(bean.getSelectedSection())) {
	    AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
// add a new section
      	    section = assessdelegate.addSection(assessmentBean.getAssessmentId());
          }

	  else {
            section = assessdelegate.getSection(bean.getSelectedSection());
          }
          item.setSection(section);

          if (update) {
	  // if Modify, need to reorder if assgned to different section '
            if ( (bean.getOrigSection() != null) &&
		(!bean.getOrigSection().equals(bean.getSelectedSection()))) {
                // if reassigned to different section
              Integer oldSeq = item.getSequence();
              item.setSequence( Integer.valueOf(section.getItemSet().size() + 1));

              // reorder the sequences of items in the OrigSection
    	      SectionFacade origsect= assessdelegate.getSection(bean.getOrigSection());
	      shiftItemsInOrigSection(delegate, origsect, oldSeq);


            }
            else {
              // no action needed
            }
          }

          if (!update) {
            if ( (itemauthor.getInsertPosition() == null) ||
                ("".equals(itemauthor.getInsertPosition()))
                || !section.getSequence().toString().equals(itemauthor.getInsertToSection())) {
              // if adding to the end
              if (section.getItemSet() != null) {
            	  item.setSequence(Integer.valueOf(section.getItemSet().size() + 1));
              }
              else {
	 	// this is a new part, not saved yet 
		item.setSequence(Integer.valueOf(1));
              }
            }
            else {
              // if inserting or a question
              String insertPos = itemauthor.getInsertPosition();
              shiftSequences(delegate, section, Integer.valueOf(insertPos));
              int insertPosInt = (Integer.valueOf(insertPos)).intValue() + 1;
              item.setSequence(Integer.valueOf(insertPosInt));
              // reset InsertPosition
              itemauthor.setInsertPosition("");
            }
          }
          if (itemauthor.getInsertToSection() != null) {
    		  // reset insertToSection to null;
    		  itemauthor.setInsertToSection(null);
    	  }
          
          delegate.saveItem(item);

          // added by daisyf, 10/10/06
          updateAttachment(item.getItemAttachmentList(), itemauthor.getAttachmentList(),
                           (ItemDataIfc)item.getData(), isEditPendingAssessmentFlow);
          item = delegate.getItem(item.getItemId().toString());

        }

        QuestionPoolService qpdelegate = new QuestionPoolService();
	// removed the old pool-item mappings
          if ( (bean.getOrigPool() != null) && (!bean.getOrigPool().equals(""))) {
            qpdelegate.removeQuestionFromPool(item.getItemIdString(),
            		Long.valueOf(bean.getOrigPool()));
          }

        // if assign to pool, add the item to the pool
        if ( (bean.getSelectedPool() != null) && !bean.getSelectedPool().equals("")) {
        	// if the item is already in the pool then do not add.
          // This is a scenario where the item might already be in the pool:
          // create an item in an assessemnt and assign it to p1
          // copy item from p1 to p2. 
          // now the item is already in p2. and if you want to edit the original item in the assessment, and reassign it to p2, you will get a duplicate error. 

          if (!qpdelegate.hasItem(item.getItemIdString(),
                                 Long.valueOf(bean.getSelectedPool()))) {
            qpdelegate.addItemToPool(item.getItemIdString(),
            					Long.valueOf(bean.getSelectedPool()));
          }
        }

        // #1a - goto editAssessment.jsp, so reset assessmentBean
        AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean(
            "assessmentBean");
        AssessmentIfc assessment = assessdelegate.getAssessment(
            Long.valueOf(assessmentBean.getAssessmentId()));
        assessmentBean.setAssessment(assessment);

        itemauthor.setOutcome("editAssessment");

      }
      // sorry, i need this for item attachment, used by SaveItemAttachmentListener. 
      itemauthor.setItemId(item.getItemId().toString());
      return true;
    }
    catch (RuntimeException e) {
      e.printStackTrace();
      return false;
    }
  }

  private HashSet prepareTextForMatching(ItemFacade item, ItemBean bean,
			ItemAuthorBean itemauthor) {
		// looping through matchItemBean
		ArrayList matchItemBeanList = bean.getMatchItemBeanList();
		HashSet textSet = new HashSet();
		Iterator choiceiter = matchItemBeanList.iterator();
		while (choiceiter.hasNext()) {

			MatchItemBean choicebean = (MatchItemBean) choiceiter.next();

			ItemText choicetext = new ItemText();
			choicetext.setItem(item.getData()); // all set to the same
												// ItemFacade
			choicetext.setSequence(choicebean.getSequence());

			choicetext.setText(stripPtags(choicebean.getChoice()));

			// need to loop through matches for in matchItemBean list
			// and add all possible matches to this choice

			// log.info(
			Iterator answeriter = matchItemBeanList.iterator();
			HashSet answerSet = new HashSet();
			Answer answer = null;
			while (answeriter.hasNext()) {

				MatchItemBean answerbean = (MatchItemBean) answeriter.next();

				if (answerbean.getSequence().equals(choicebean.getSequence())) {
					answer = new Answer(choicetext, stripPtags(answerbean
							.getMatch()), answerbean.getSequence(), AnswerBean
							.getChoiceLabels()[answerbean.getSequence()
							.intValue() - 1], Boolean.TRUE, null, Float.valueOf(
							bean.getItemScore()), Float.valueOf(bean.getItemDiscount()));

					// only add feedback for correct pairs
					HashSet answerFeedbackSet = new HashSet();
					answerFeedbackSet.add(new AnswerFeedback(answer,
							AnswerFeedbackIfc.CORRECT_FEEDBACK,
							stripPtags(answerbean.getCorrMatchFeedback())));
					answerFeedbackSet.add(new AnswerFeedback(answer,
							AnswerFeedbackIfc.INCORRECT_FEEDBACK,
							stripPtags(answerbean.getIncorrMatchFeedback())));
					answer.setAnswerFeedbackSet(answerFeedbackSet);

				} else {
					answer = new Answer(choicetext, stripPtags(answerbean
							.getMatch()), answerbean.getSequence(), AnswerBean
							.getChoiceLabels()[answerbean.getSequence()
							.intValue() - 1], Boolean.FALSE, null,  Float.valueOf(
							bean.getItemScore()), Float.valueOf(bean.getItemDiscount()));
				}

				// record answers for all combination of pairs

				HashSet answerFeedbackSet = new HashSet();
				answerFeedbackSet.add(new AnswerFeedback(answer,
						AnswerFeedbackIfc.CORRECT_FEEDBACK,
						stripPtags(answerbean.getCorrMatchFeedback())));
				answerFeedbackSet.add(new AnswerFeedback(answer,
						AnswerFeedbackIfc.INCORRECT_FEEDBACK,
						stripPtags(answerbean.getIncorrMatchFeedback())));
				answer.setAnswerFeedbackSet(answerFeedbackSet);

				//
				answerSet.add(answer);

			}
			choicetext.setAnswerSet(answerSet);
			textSet.add(choicetext);

		}
		return textSet;
	}

	private HashSet prepareText(ItemFacade item, ItemBean bean,
			ItemAuthorBean itemauthor) {
		HashSet textSet = new HashSet();
		HashSet answerSet1 = new HashSet();

		// ///////////////////////////////////////////////////////////
		// 1. save Question Text for items with single Question Text
		// (except matching)
		// ///////////////////////////////////////////////////////////
		ItemText text1 = new ItemText();
		text1.setItem(item.getData());
		text1.setSequence(Long.valueOf(1));
		text1.setText(bean.getItemText());
		
		// ///////////////////////////////////////////////////////////
		//
		// 2. save Answers
		//
		// ///////////////////////////////////////////////////////////
		if (item.getTypeId().equals(TypeFacade.TRUE_FALSE)) {

			// find correct answer

			Answer newanswer = null;
			for (int i = 0; i < bean.getAnswers().length; i++) {
				String theanswer = bean.getAnswers()[i];
				// String thelabel = bean.getAnswerLabels()[i]; // store
				// thelabel as the answer text
				if (theanswer.equals(bean.getCorrAnswer())) {
					// label is null because we don't use labels in true/false
					// questions
					// labels are like a, b, c, or i, ii, iii, in multiple
					// choice type

					newanswer = new Answer(text1, theanswer, Long.valueOf(i + 1),
							"", Boolean.TRUE, null, Float.valueOf(bean
									.getItemScore()), Float.valueOf(bean.getItemDiscount()));
				} else {
					newanswer = new Answer(text1, theanswer, Long.valueOf(i + 1),
							"", Boolean.FALSE, null, Float.valueOf(bean
									.getItemScore()), Float.valueOf(bean.getItemDiscount()));
				}
				answerSet1.add(newanswer);
			}

			text1.setAnswerSet(answerSet1);
			textSet.add(text1);
		} else if (item.getTypeId().equals(TypeFacade.ESSAY_QUESTION)) {

			// Storing the model answer essay as an Answer, and feedback in the
			// Answerfeedback

			String theanswer = bean.getCorrAnswer();
			if (theanswer == null) {
				theanswer = ""; // can be empty
			}

			// label is null because we don't use labels in essay questions
			// theanswer is the model answer used as a sample for student
			Answer modelanswer = new Answer(text1, theanswer, Long.valueOf(1),
					null, Boolean.TRUE, null, Float.valueOf(bean.getItemScore()), Float.valueOf(bean.getItemDiscount()));

			HashSet answerFeedbackSet1 = new HashSet();

			answerFeedbackSet1.add(new AnswerFeedback(modelanswer,
					"modelanswer", stripPtags(bean.getCorrFeedback())));
			modelanswer.setAnswerFeedbackSet(answerFeedbackSet1);

			answerSet1.add(modelanswer);
			text1.setAnswerSet(answerSet1);
			textSet.add(text1);
		}

		else if (item.getTypeId().equals(TypeFacade.MULTIPLE_CHOICE_SURVEY)) {

			String scalename = bean.getScaleName();
			String[] choices = getSurveyChoices(scalename);

			for (int i = 0; i < choices.length; i++) {
				Answer answer1 = new Answer(text1, choices[i], Long.valueOf(i + 1),
						null, null, null, Float.valueOf(bean.getItemScore()), Float.valueOf(bean.getItemDiscount()));
				answerSet1.add(answer1);
			}
			text1.setAnswerSet(answerSet1);
			textSet.add(text1);
		}

		// not doing parsing in authoring

		else if (item.getTypeId().equals(TypeFacade.FILL_IN_BLANK)) {
			// this is for fill in blank
			String entiretext = bean.getItemText();
			String fibtext = entiretext.replaceAll("[\\{][^\\}]*[\\}]", "{}");
			text1.setText(fibtext);
			// log.info(" new text without answer is = " + fibtext);
			Object[] fibanswers = getFIBanswers(entiretext).toArray();
			for (int i = 0; i < fibanswers.length; i++) {
				String oneanswer = (String) fibanswers[i];
				Answer answer1 = new Answer(text1, oneanswer, Long.valueOf(i + 1),
						null, Boolean.TRUE, null,
						Float.valueOf(bean.getItemScore()), Float.valueOf(bean.getItemDiscount()));
				answerSet1.add(answer1);
			}

			text1.setAnswerSet(answerSet1);
			textSet.add(text1);

		}

		else if (item.getTypeId().equals(TypeFacade.FILL_IN_NUMERIC)) {
			// this is for fill in numeric
			String entiretext = bean.getItemText();
			String fintext = entiretext.replaceAll("[\\{][^\\}]*[\\}]", "{}");
			text1.setText(fintext);
			// log.info(" new text without answer is = " + fintext);
			Object[] finanswers = getFINanswers(entiretext).toArray();
			for (int i = 0; i < finanswers.length; i++) {
				String oneanswer = (String) finanswers[i];
				Answer answer1 = new Answer(text1, oneanswer, Long.valueOf(i + 1),
						null, Boolean.TRUE, null,
						Float.valueOf(bean.getItemScore()), Float.valueOf(bean.getItemDiscount()));
				answerSet1.add(answer1);
			}

			text1.setAnswerSet(answerSet1);
			textSet.add(text1);

		}

		else if ((item.getTypeId().equals(TypeFacade.MULTIPLE_CHOICE))
				|| (item.getTypeId().equals(TypeFacade.MULTIPLE_CORRECT))
				|| (item.getTypeId().equals(TypeFacade.MULTIPLE_CORRECT_SINGLE_SELECTION))) {
			// this is for both single/multiple correct multiple choice types

			// for single choice
			// String theanswer=bean.getCorrAnswer();
			Iterator iter = bean.getMultipleChoiceAnswers().iterator();
			Answer answer = null;
			while (iter.hasNext()) {
				AnswerBean answerbean = (AnswerBean) iter.next();
				if (isCorrectChoice(bean, answerbean.getLabel().trim())) {
					answer = new Answer(text1,
							stripPtags(answerbean.getText()), answerbean
									.getSequence(), answerbean.getLabel(),
							Boolean.TRUE, null, Float.valueOf(bean.getItemScore()), Float.valueOf(bean.getItemDiscount()));
				} else {
					answer = new Answer(text1,
							stripPtags(answerbean.getText()), answerbean
									.getSequence(), answerbean.getLabel(),
							Boolean.FALSE, null, Float.valueOf(bean.getItemScore()), Float.valueOf(bean.getItemDiscount()));
				}
				HashSet answerFeedbackSet1 = new HashSet();
				answerFeedbackSet1.add(new AnswerFeedback(answer,
						AnswerFeedbackIfc.GENERAL_FEEDBACK,
						stripPtags(answerbean.getFeedback())));
				answer.setAnswerFeedbackSet(answerFeedbackSet1);

				answerSet1.add(answer);
			}

			text1.setAnswerSet(answerSet1);
			textSet.add(text1);

		}

		// for file Upload and audio recording
		else {
			// no answers need to be added
			textSet.add(text1);
		}

		/////////////////////////////////////////////////////////////
		//END
		/////////////////////////////////////////////////////////////

		return textSet;
	} 

  private Set preparePublishedText(ItemFacade item, ItemBean bean, ItemService delegate) {

	  if (item.getTypeId().equals(TypeFacade.TRUE_FALSE)) {
		  preparePublishedTextForTF(item, bean);
	  }
	  else if (item.getTypeId().equals(TypeFacade.ESSAY_QUESTION)) {
		  preparePublishedTextForEssay(item, bean);
	  }
	  else if (item.getTypeId().equals(TypeFacade.MULTIPLE_CHOICE_SURVEY)) {
		  preparePublishedTextForSurvey(item, bean, delegate);
	  }
	  else if (item.getTypeId().equals(TypeFacade.FILL_IN_BLANK)) {
		  preparePublishedTextForFIBFIN(item, bean, delegate, true);
	  }
	  else if (item.getTypeId().equals(TypeFacade.FILL_IN_NUMERIC)) {
		  preparePublishedTextForFIBFIN(item, bean, delegate, false);
	  }
	  else if ( (item.getTypeId().equals(TypeFacade.MULTIPLE_CHOICE)) ||
	             (item.getTypeId().equals(TypeFacade.MULTIPLE_CORRECT)) ||
	             (item.getTypeId().equals(TypeFacade.MULTIPLE_CORRECT_SINGLE_SELECTION))) {
		  preparePublishedTextForMC(item, bean, delegate);
	  }
	  else if (item.getTypeId().equals(TypeFacade.MATCHING)) {
		  preparePublishedTextForMatching(item, bean, delegate);
	  }
	  // for file Upload and audio recording
	  else {
		  // no answers need to be added
		  preparePublishedTextForOthers(item, bean);
	  }
	  Set textSet = item.getItemTextSet(); 
	  return textSet;
  }
  
  private void preparePublishedTextForTF(ItemFacade item, ItemBean bean) {
	  Set answerSet = null;
	  AnswerIfc answer = null;
	  ItemTextIfc text = null;
	  Set textSet = item.getItemTextSet();
	  Iterator iter = textSet.iterator();
	  while (iter.hasNext()) {
		  text = (ItemTextIfc) iter.next();
		  text.setText(bean.getItemText());
		  answerSet = text.getAnswerSet();
		  Iterator answerIter = answerSet.iterator();
		  while (answerIter.hasNext()) {
			  answer = (AnswerIfc) answerIter.next();
			  answer.setScore(Float.valueOf(bean.getItemScore()));
			  answer.setDiscount(Float.valueOf(bean.getItemDiscount()));
			  if (answer.getText().equals(bean.getCorrAnswer())) {
				  answer.setIsCorrect(Boolean.TRUE);
			  }
			  else {
				  answer.setIsCorrect(Boolean.FALSE);
			  }
		  }
	  }
  }
  
  private void preparePublishedTextForEssay(ItemFacade item, ItemBean bean) {
	  Set answerSet = null;
	  AnswerIfc answer = null;
	  ItemTextIfc text = null;
	  Set textSet = item.getItemTextSet();
	  Iterator iter = textSet.iterator();
	  while (iter.hasNext()) {
		  text = (ItemTextIfc) iter.next();
		  text.setText(bean.getItemText());
		  answerSet = text.getAnswerSet();
		  Iterator answerIter = answerSet.iterator();
		  // Storing the model answer essay as an Answer, and feedback in the Answerfeedback	
		  while (answerIter.hasNext()) {
			  answer = (AnswerIfc) answerIter.next();
			  answer.setScore(Float.valueOf(bean.getItemScore()));
			  String theanswer = bean.getCorrAnswer();
		      if (theanswer == null) {
		        theanswer = ""; // can be empty
		      }
			  answer.setText(theanswer);
			  Set answerFeedbackSet = answer.getAnswerFeedbackSet();
			  Iterator answerFeedbackIter = answerFeedbackSet.iterator();
			  while (answerFeedbackIter.hasNext()) {
				  AnswerFeedbackIfc answerFeedback = (AnswerFeedbackIfc) answerFeedbackIter.next();
				  answerFeedback.setText(stripPtags(bean.getCorrFeedback()));
			  }
		  }		
	  }
  }
  
  private void preparePublishedTextForSurvey(ItemFacade item, ItemBean bean, ItemService delegate) {
	  String scalename = bean.getScaleName();
	  String[] choices = getSurveyChoices(scalename);
	  Set answerSet = new HashSet();
	  Set textSet = item.getItemTextSet();
	  ItemTextIfc text = null;
	  Iterator iter = textSet.iterator();
	  while (iter.hasNext()) {
		  text = (ItemTextIfc) iter.next();
		  text.setText(bean.getItemText());
		  answerSet = text.getAnswerSet();
		  // For survey type, we erase all the existing ones. Because there is no benefit do update and then add/delete like FIB, FIN, or MC
		  delegate.deleteSet(answerSet);
		  
		  for (int i = 0; i < choices.length; i++) {
			  AnswerIfc answer = new PublishedAnswer(text, choices[i], Long.valueOf(i + 1),
					null, null, null, Float.valueOf(bean.getItemScore()), Float.valueOf(bean.getItemDiscount()));
			  answerSet.add(answer);
		  }
		  text.setAnswerSet(answerSet);
		  textSet.add(text);
	  }
  }
  
  private void preparePublishedTextForFIBFIN(ItemFacade item, ItemBean bean, ItemService delegate, boolean isFIB) {
		Set answerSet = null;
		Set textSet = item.getItemTextSet();
		ItemTextIfc text = null;
		String entiretext = bean.getItemText();
		String updatedText = entiretext.replaceAll("[\\{][^\\}]*[\\}]", "{}");
		log.debug(" new text without answer is = " + updatedText);
		Iterator iter = textSet.iterator();
		while (iter.hasNext()) {
			text = (ItemTextIfc) iter.next();
			text.setText(updatedText);
			Object[] answers;
			if (isFIB) {
				answers = getFIBanswers(entiretext).toArray();
			}
			else {
				answers = getFINanswers(entiretext).toArray();
			}
			int newAnswersSize = answers.length;
			int i = 0;
			HashSet toBeRemovedSet = new HashSet();
			answerSet = text.getAnswerSet();
			Iterator answerIter = answerSet.iterator();
			while (answerIter.hasNext()) {
				AnswerIfc answer = (AnswerIfc) answerIter.next();
				answer.setScore(Float.valueOf(bean.getItemScore()));
				i = answer.getSequence().intValue();
				if (i <= newAnswersSize) {
					String oneanswer = (String) answers[i - 1];
					answer.setText(oneanswer);
				} else {
					toBeRemovedSet.add(answer);
				}
			}
			if (answerSet.size() < newAnswersSize) {
				for (int j = answerSet.size(); j < newAnswersSize; j++) {
					String oneanswer = (String) answers[j];
					AnswerIfc answer = new PublishedAnswer(text, oneanswer,
							Long.valueOf(j + 1), null, Boolean.TRUE, null,
							Float.valueOf(bean.getItemScore()), Float.valueOf(bean.getItemDiscount()));
					answerSet.add(answer);
				}
			}
			answerSet.removeAll(toBeRemovedSet);
			delegate.deleteSet(toBeRemovedSet);
		}
	}
  

  private void preparePublishedTextForMC(ItemFacade item, ItemBean bean, ItemService delegate) {
		Set answerSet = null;
		Set textSet = item.getItemTextSet();
		ItemTextIfc text = null;
		Iterator iter = textSet.iterator();
		while (iter.hasNext()) {
			text = (ItemTextIfc) iter.next();
			text.setText(bean.getItemText());
			ArrayList newAnswerList = bean.getMultipleChoiceAnswers();
			HashMap newAnswerMap = new HashMap();
			Iterator newAnswerIter = newAnswerList.iterator();
			while (newAnswerIter.hasNext()) {
				AnswerBean answerBean = (AnswerBean) newAnswerIter.next();
				newAnswerMap.put(answerBean.getSequence(), answerBean);
			}
			
			int newAnswersSize = newAnswerList.size();
			int i = 0;
			HashSet toBeRemovedSet = new HashSet();
			AnswerBean answerBean = null;
			answerSet = text.getAnswerSet();
			Iterator answerIter = answerSet.iterator();
			while (answerIter.hasNext()) {
				AnswerIfc answer = (AnswerIfc) answerIter.next();
				answer.setDiscount(Float.valueOf(bean.getItemDiscount()));
				i = answer.getSequence().intValue();
				if (i <= newAnswersSize) {				
					answer.setScore(Float.valueOf(bean.getItemScore()));
					answerBean = (AnswerBean) newAnswerMap.get(Long.valueOf(String.valueOf(i)));
					String oneAnswer = stripPtags(answerBean.getText());
					String oneLabel = answerBean.getLabel();
					log.debug("oneAnswer = " + oneAnswer);
					log.debug("oneLabel = " + oneLabel);
					answer.setText(oneAnswer);
					answer.setLabel(oneLabel);
					if (isCorrectChoice(bean, oneLabel.trim())) {
						answer.setIsCorrect(Boolean.TRUE);
					}
					else {
						answer.setIsCorrect(Boolean.FALSE);
					}
					Set answerFeedbackSet = answer.getAnswerFeedbackSet();
					Iterator answerFeedbackIter = answerFeedbackSet.iterator();
					while (answerFeedbackIter.hasNext()) {
						AnswerFeedbackIfc answerFeedback = (AnswerFeedbackIfc) answerFeedbackIter.next();
						answerFeedback.setText(stripPtags(answerBean.getFeedback()));
					}
				} else {
					toBeRemovedSet.add(answer);
				}
			}
			if (answerSet.size() < newAnswersSize) {
				for (int j = answerSet.size() + 1; j < newAnswersSize + 1; j++) {
					answerBean = (AnswerBean) newAnswerMap.get(Long.valueOf(String.valueOf(j)));
					String oneAnswer = stripPtags(answerBean.getText());
					String oneLabel = answerBean.getLabel();
					AnswerIfc answer = null;
					if (isCorrectChoice(bean, answerBean.getLabel().trim())) {
						answer = new PublishedAnswer(text, oneAnswer,
							Long.valueOf(j), oneLabel, Boolean.TRUE, null,
							Float.valueOf(bean.getItemScore()), Float.valueOf(bean.getItemDiscount()));
					}
					else {
						answer = new PublishedAnswer(text, oneAnswer,
								Long.valueOf(j), oneLabel, Boolean.FALSE, null,
								Float.valueOf(bean.getItemScore()), Float.valueOf(bean.getItemDiscount()));
					}
					HashSet answerFeedbackSet = new HashSet();
				    answerFeedbackSet.add(new PublishedAnswerFeedback(answer,
				                                             AnswerFeedbackIfc.GENERAL_FEEDBACK,
				                                             stripPtags(answerBean.getFeedback())));
				    answer.setAnswerFeedbackSet(answerFeedbackSet);
					answerSet.add(answer);
				}
			}
			answerSet.removeAll(toBeRemovedSet);
			delegate.deleteSet(toBeRemovedSet);
		}
  }
  
  private void preparePublishedTextForMatching(ItemFacade item,
			ItemBean bean, ItemService delegate) {
		Set textSet = item.getItemTextSet();
		Iterator textIter = textSet.iterator();
		HashMap itemTextMap = new HashMap();
		while (textIter.hasNext()) {
			ItemTextIfc itemText = (ItemTextIfc) textIter.next();
			itemTextMap.put(itemText.getSequence(), itemText);
		}

		// looping through matchItemBean
		ArrayList matchItemBeanList = bean.getMatchItemBeanList();
		Iterator choiceIter = matchItemBeanList.iterator();
		HashMap newMatchItemMap = new HashMap();
		Set answerSet = null;
		ItemTextIfc itemText = null;
		AnswerIfc answer = null;
		MatchItemBean choiceBean = null;
		MatchItemBean matchBean = null;
		Long choiceSequence = null;
		Long matchSequence = null;

		while (choiceIter.hasNext()) {
			choiceBean = (MatchItemBean) choiceIter.next();
			choiceSequence = choiceBean.getSequence();
			if (itemTextMap.get(choiceSequence) == null) {
				// new - add it in
				itemText = new PublishedItemText();
				itemText.setItem(item.getData());
				itemText.setSequence(choiceBean.getSequence());
				itemText.setText(stripPtags(choiceBean.getChoice()));
			} else {
				itemText = (ItemTextIfc) itemTextMap.get(choiceSequence);
				itemText.setText(choiceBean.getChoice());
			}
			HashMap answerMap = new HashMap();
			answerSet = itemText.getAnswerSet();
			if (answerSet != null) {
				Iterator answerIter = answerSet.iterator();
				while (answerIter.hasNext()) {
					answer = (AnswerIfc) answerIter.next();
					answerMap.put(answer.getSequence(), answer);
				}
			}
			else {
				answerSet = new HashSet();
				itemText.setAnswerSet(answerSet);
			    textSet.add(itemText);
			}
			Iterator matchIter = matchItemBeanList.iterator();
			while (matchIter.hasNext()) {
				matchBean = (MatchItemBean) matchIter.next();
				matchSequence = matchBean.getSequence();
				if (answerMap.get(matchSequence) == null) {
					// new - add it in
					if (matchBean.getSequence()
							.equals(choiceBean.getSequence())) {
						answer = new PublishedAnswer(itemText,
								stripPtags(matchBean.getMatch()), matchBean
										.getSequence(), AnswerBean
										.getChoiceLabels()[matchBean
										.getSequence().intValue() - 1],
								Boolean.TRUE, null, new Float(bean
										.getItemScore()), Float.valueOf(bean.getItemDiscount()));
					} else {
						answer = new PublishedAnswer(itemText,
								stripPtags(matchBean.getMatch()), matchBean
										.getSequence(), AnswerBean
										.getChoiceLabels()[matchBean
										.getSequence().intValue() - 1],
								Boolean.FALSE, null, new Float(bean
										.getItemScore()), Float.valueOf(bean.getItemDiscount()));
					}

					// record answers for all combination of pairs
					HashSet answerFeedbackSet = new HashSet();
					answerFeedbackSet.add(new PublishedAnswerFeedback(answer,
							AnswerFeedbackIfc.CORRECT_FEEDBACK,
							stripPtags(matchBean.getCorrMatchFeedback())));
					answerFeedbackSet.add(new PublishedAnswerFeedback(answer,
							AnswerFeedbackIfc.INCORRECT_FEEDBACK,
							stripPtags(matchBean.getIncorrMatchFeedback())));
					answer.setAnswerFeedbackSet(answerFeedbackSet);
					answerSet.add(answer);
					
				} else {
					answer = (AnswerIfc) answerMap.get(matchSequence);
					answer.setScore(Float.valueOf(bean.getItemScore()));
					String oneAnswer = stripPtags(matchBean.getMatch());
					String oneLabel = AnswerBean.getChoiceLabels()[matchSequence
							.intValue() - 1];
					log.debug("oneAnswer = " + oneAnswer);
					log.debug("oneLabel = " + oneLabel);
					answer.setText(oneAnswer);
					answer.setLabel(oneLabel);
					if (choiceSequence == matchSequence) {
						answer.setIsCorrect(Boolean.TRUE);
					} else {
						answer.setIsCorrect(Boolean.FALSE);
					}
					Set answerFeedbackSet = answer.getAnswerFeedbackSet();
					Iterator answerFeedbackIter = answerFeedbackSet.iterator();
					String feedback = "";
					while (answerFeedbackIter.hasNext()) {
						AnswerFeedbackIfc answerFeedback = (AnswerFeedbackIfc) answerFeedbackIter
								.next();
						if (answerFeedback.getTypeId().equals(AnswerFeedbackIfc.CORRECT_FEEDBACK)) {
							answerFeedback.setText(stripPtags(matchBean.getCorrMatchFeedback()));
						}
						else if (answerFeedback.getTypeId().equals(AnswerFeedbackIfc.INCORRECT_FEEDBACK)) {
							answerFeedback.setText(stripPtags(matchBean.getIncorrMatchFeedback()));
						}
					}
				}
			}
		}

		int oldSize = textSet.size();
		int newSize = matchItemBeanList.size();
		if (oldSize > newSize) {
			HashSet toBeRemovedTextSet = new HashSet();
			HashSet toBeRemovedAnswerSet = new HashSet();
			// Need to remove from answer too
			for (int i = 1; i < newSize + 1; i++) {
				ItemTextIfc text = (ItemTextIfc) itemTextMap.get(Long.valueOf(i));
				answerSet = text.getAnswerSet();
				if (answerSet != null) {
					Iterator answerIter = answerSet.iterator();
					while (answerIter.hasNext()) {
						answer = (AnswerIfc) answerIter.next();
						for (int j = newSize + 1; j < oldSize + 1; j++) {
							if (answer.getSequence().intValue() == j) {
								toBeRemovedAnswerSet.add(answer);
							}
						}
					}
					answerSet.removeAll(toBeRemovedAnswerSet);
					delegate.deleteSet(toBeRemovedAnswerSet);
				}
			}
			for (int i = newSize + 1; i < oldSize + 1; i++) {
				ItemTextIfc text = (ItemTextIfc) itemTextMap.get(Long.valueOf(i));
				toBeRemovedTextSet.add(text);
			}
			textSet.removeAll(toBeRemovedTextSet);
			delegate.deleteSet(toBeRemovedTextSet);
		}
  }


  private void preparePublishedTextForOthers(ItemFacade item, ItemBean bean) {
	  ItemTextIfc text = null;
	  Set textSet = item.getItemTextSet();
	  Iterator iter = textSet.iterator();
	  while (iter.hasNext()) {
		  text = (ItemTextIfc) iter.next();
		  text.setText(bean.getItemText());
	  }
  }
  
  protected HashSet prepareMetaData(ItemFacade item, ItemBean bean) {
		HashSet set = new HashSet();
		if (bean.getKeyword() != null) {
			set.add(new ItemMetaData(item.getData(),
					ItemMetaDataIfc.KEYWORD, ContextUtil.processFormattedText(log, bean.getKeyword())));
		}
		if (bean.getRubric() != null) {
			set.add(new ItemMetaData(item.getData(),
					ItemMetaDataIfc.RUBRIC, ContextUtil.processFormattedText(log, bean.getRubric())));
		}
		if (bean.getObjective() != null) {
			set.add(new ItemMetaData(item.getData(),
					ItemMetaDataIfc.OBJECTIVE, ContextUtil.processFormattedText(log, bean.getObjective())));
		}
		// Randomize property got left out, added in metadata
		if (bean.getRandomized() != null) {
		set.add(new ItemMetaData(item.getData(),
					ItemMetaDataIfc.RANDOMIZE, bean.getRandomized()));
		}
		// 2/19/06 use PREDEFINED_SCALE to be in sync with what we are using
		// for import/export
		if (bean.getScaleName() != null) {
			set.add(new ItemMetaData(item.getData(),
					ItemMetaDataIfc.PREDEFINED_SCALE, bean.getScaleName()));
		}
		// save settings for case sensitive for FIB. Default=false
		set.add(new ItemMetaData(item.getData(),
				ItemMetaDataIfc.CASE_SENSITIVE_FOR_FIB, Boolean
						.toString(bean.getCaseSensitiveForFib())));
		// save settings for mutually exclusive for FIB. Default=false
		// first check to see if it's a valid mutually exclusive mutiple
		// answers FIB
		boolean wellformatted = false;
		if (bean.getMutuallyExclusiveForFib()) {
			wellformatted = isValidMutualExclusiveFIB(bean);
		}

		set.add(new ItemMetaData(item.getData(),
				ItemMetaDataIfc.MUTUALLY_EXCLUSIVE_FOR_FIB, Boolean
						.toString(wellformatted)));
		// Do we need Mutually exclusive for numeric responses, what about
		// questions like
		// the Square root of 4 is {2|-2} and {2|-2}.
		// save settings for mutually exclusive for FIN. Default=false
		// first check to see if it's a valid mutually exclusive mutiple
	
		// answers FIN
		/*
		 * boolean wellformattedFIN = false;
		 * 
		 * set.add(new ItemMetaData(item.getData(),
		 * ItemMetaDataIfc.MUTUALLY_EXCLUSIVE_FOR_FIN,
		 * Boolean.toString(wellformattedFIN)));
		 * 
		 */

		// save part id
		if (bean.getSelectedSection() != null) {
			set.add(new ItemMetaData(item.getData(),
					ItemMetaDataIfc.PARTID, bean.getSelectedSection()));
		}
		// save pool id
		if (bean.getSelectedPool() != null) {
			set.add(new ItemMetaData(item.getData(),
					ItemMetaDataIfc.POOLID, bean.getSelectedPool()));
		}
		// save timeallowed for audio recording
		/*
		 * // save them in ItemFacade if (bean.getTimeAllowed()!=null){
		 * set.add(new ItemMetaData(item.getData(),
		 * ItemMetaDataIfc.TIMEALLOWED, bean.getTimeAllowed())); }
		 */
		// save timeallowed for audio recording
		/*
		 * // save them in ItemFacade if (bean.getNumAttempts()!=null){
		 * set.add(new ItemMetaData(item.getData(),
		 * ItemMetaDataIfc.NUMATTEMPTS, bean.getNumAttempts())); }
		 */
		return set;
  }

  protected Set preparePublishedMetaData(ItemFacade item, ItemBean bean) {
	  Set itemMetaDataSet = item.getItemMetaDataSet();
	  Iterator iter = itemMetaDataSet.iterator();
	  while (iter.hasNext()) {
		  ItemMetaDataIfc itemMetaData = (ItemMetaDataIfc) iter.next();
		  if (itemMetaData.getLabel().equals(ItemMetaDataIfc.KEYWORD)){
			  itemMetaData.setEntry(ContextUtil.processFormattedText(log, bean.getKeyword()));
		  }
		  else if (itemMetaData.getLabel().equals(ItemMetaDataIfc.RUBRIC)){
			  itemMetaData.setEntry(ContextUtil.processFormattedText(log, bean.getKeyword()));
		  }
		  else if (itemMetaData.getLabel().equals(ItemMetaDataIfc.OBJECTIVE)){
			  itemMetaData.setEntry(ContextUtil.processFormattedText(log, bean.getObjective()));
		  }
		  else if (itemMetaData.getLabel().equals(ItemMetaDataIfc.RANDOMIZE)){
			  itemMetaData.setEntry(bean.getRandomized());
		  }
		  else if (itemMetaData.getLabel().equals(ItemMetaDataIfc.PREDEFINED_SCALE)){
			  itemMetaData.setEntry(bean.getScaleName());
		  }
		  else if (itemMetaData.getLabel().equals(ItemMetaDataIfc.CASE_SENSITIVE_FOR_FIB)){
			  itemMetaData.setEntry(Boolean.toString(bean.getCaseSensitiveForFib()));
		  }
		  else if (itemMetaData.getLabel().equals(ItemMetaDataIfc.KEYWORD)){
			  itemMetaData.setEntry(bean.getKeyword());
		  }
	  
		  // save settings for mutually exclusive for FIB. Default=false
		  // first check to see if it's a valid mutually exclusive mutiple
		  // answers FIB
		  else if (itemMetaData.getLabel().equals(ItemMetaDataIfc.MUTUALLY_EXCLUSIVE_FOR_FIB)){
			  boolean wellformatted = false;
			  if (bean.getMutuallyExclusiveForFib()) {
				wellformatted = isValidMutualExclusiveFIB(bean);
			  }
			  itemMetaData.setEntry(Boolean
						.toString(wellformatted));
		  }
		  
		  else if (itemMetaData.getLabel().equals(ItemMetaDataIfc.PARTID)){
			  itemMetaData.setEntry(bean.getSelectedSection());
		  }
		  else if (itemMetaData.getLabel().equals(ItemMetaDataIfc.POOLID)){
			  itemMetaData.setEntry(bean.getSelectedPool());
		  }
	  }
	  return itemMetaDataSet;
	}

  private static ArrayList getFIBanswers(String entiretext) {
	String fixedText = entiretext.replaceAll("&nbsp;", " "); // replace &nbsp
																// to " "
																// (instead of
																// "") just want
																// to reserve
																// the original
																// input
    String[] tokens = fixedText.split("[\\}][^\\{]*[\\{]");
    ArrayList list = new ArrayList();
    if (tokens.length==1) {
        String[] afteropen= tokens[0].split("\\{");
        if (afteropen.length>1) {
// must have text in between {}
          String[] lastpart = afteropen[1].split("\\}");
          String answer = FormattedText.escapeHtml(FormattedText.convertFormattedTextToPlaintext(lastpart[0].replaceAll("&lt;.*?&gt;", "")), false);
          list.add(answer);
        }
    }
    else {
      for (int i = 0; i < tokens.length; i++) {
      if (i == 0) {
        String[] firstpart = tokens[i].split("\\{");
	  if (firstpart.length>1) {
		String answer = FormattedText.escapeHtml(FormattedText.convertFormattedTextToPlaintext(firstpart[1].replaceAll("&lt;.*?&gt;", "")), false);
          list.add(answer);
        }
      }
      else if (i == (tokens.length - 1)) {
        String[] lastpart = tokens[i].split("\\}");
        String answer = FormattedText.escapeHtml(FormattedText.convertFormattedTextToPlaintext(lastpart[0].replaceAll("&lt;.*?&gt;", "")), false);
        list.add(answer);
      }
      else {
    	String answer = FormattedText.escapeHtml(FormattedText.convertFormattedTextToPlaintext(tokens[i].replaceAll("&lt;.*?&gt;", "")), false);
        list.add(answer);
      }
      }
    } // token.length>1

    return list;

  }

  private static ArrayList getFINanswers(String entiretext) {
		String fixedText = entiretext.replaceAll("&nbsp;", " "); // replace &nbsp to " " (instead of "") just want to reserve the original input
	    String[] tokens = fixedText.split("[\\}][^\\{]*[\\{]");
	    ArrayList list = new ArrayList();
	    if (tokens.length==1) {
	        String[] afteropen= tokens[0].split("\\{");
	        if (afteropen.length>1) {
//	 must have text in between {}
	          String[] lastpart = afteropen[1].split("\\}");
	          list.add(lastpart[0]);
	        }
	    }
	    else {
	      for (int i = 0; i < tokens.length; i++) {
	      if (i == 0) {
	        String[] firstpart = tokens[i].split("\\{");
		if (firstpart.length>1) {
	          list.add(firstpart[1]);
	        }
	      }
	      else if (i == (tokens.length - 1)) {
	        String[] lastpart = tokens[i].split("\\}");
	        list.add(lastpart[0]);
	      }
	      else {
	        list.add(tokens[i]);
	      }
	      }
	    } // token.length>1

	    return list;

	  }
  
  /**
   ** returns if the multile choice label is the correct choice,
   ** bean.getCorrAnswers() returns a string[] of labels
   ** bean.getCorrAnswer() returns a string of label
   **/
  public boolean isCorrectChoice(ItemBean bean, String label) {
    boolean returnvalue = false;
    if (TypeFacade.MULTIPLE_CHOICE.toString().equals(bean.getItemType())) {
      String corranswer = ContextUtil.lookupParam("itemForm:selectedRadioBtn");
      if (corranswer.equals(label)) {
        returnvalue = true;
      }
      else {
        returnvalue = false;
      }
    }
    else {
      ArrayList corranswersList = ContextUtil.paramArrayValueLike(
          "mccheckboxes");
      Iterator iter = corranswersList.iterator();
      while (iter.hasNext()) {

        String currentcorrect = (String) iter.next();
        if (currentcorrect.trim().equals(label)) {
          returnvalue = true;
          break;
        }
        else {
          returnvalue = false;
        }
      }
    }

    return returnvalue;
  }

  /**
   ** shift sequence number down when inserting or reordering
   **/

  public void shiftSequences(ItemService delegate, SectionFacade sectfacade, Integer currSeq) {
    Set itemset = sectfacade.getItemFacadeSet();
    Iterator iter = itemset.iterator();
    while (iter.hasNext()) {
      ItemFacade itemfacade = (ItemFacade) iter.next();
      Integer itemfacadeseq = itemfacade.getSequence();
      if (itemfacadeseq.compareTo(currSeq) > 0) {
        itemfacade.setSequence(Integer.valueOf(itemfacadeseq.intValue() + 1));
        delegate.saveItem(itemfacade);
      }
    }
  }





  private void shiftItemsInOrigSection(ItemService delegate, SectionFacade sectfacade, Integer currSeq){
    Set itemset = sectfacade.getItemFacadeSet();
// should be size-1 now.
      Iterator iter = itemset.iterator();
      while (iter.hasNext()) {
        ItemFacade  itemfacade = (ItemFacade) iter.next();
        Integer itemfacadeseq = itemfacade.getSequence();
        if (itemfacadeseq.compareTo(currSeq) > 0 ){
          itemfacade.setSequence( Integer.valueOf(itemfacadeseq.intValue()-1) );
          delegate.saveItem(itemfacade);
        }
      }

  }

  private String stripPtags(String origtext) {
   // interim solution for the wywisyg bug. This will strip off the first <p> and last </p> if both exists.
    String newanswer = origtext;
    if ((origtext!= null)&& (origtext.startsWith("<p")) && (origtext.endsWith("</p>")) ){
       newanswer = origtext.substring(origtext.indexOf(">") + 1, origtext.lastIndexOf("</p>"));
       return newanswer.trim();
    }
    else {
      return newanswer;

    }
 }


  private boolean isValidMutualExclusiveFIB(ItemBean bean){
    // all answer sets have to be identical, case insensitive

     String entiretext = bean.getItemText();
     entiretext = entiretext.replaceAll("[\\{][^\\}]*[\\}]", "{}");

Object[] fibanswers = getFIBanswers(entiretext).toArray();
      List blanklist = new  ArrayList();
      for (int i = 0; i < fibanswers.length; i++) {
        String oneanswer = (String) fibanswers[i];
        String[] oneblank = oneanswer.split("\\|");
        Set oneblankset = new HashSet();
        for (int j = 0; j < oneblank.length; j++) {
           oneblankset.add(oneblank[j].trim().toLowerCase());
        }
        blanklist.add(oneblankset);

      }
      // now check if there are at least 2 sets, and make sure they are identically, all should contain only lowercase strings. 
      boolean invalid= false;
      if (blanklist.size()<=1){
           invalid = true;
      }
      else {
      for (int i = 1; i < blanklist.size(); i++) {
        if (!(blanklist.get(0).equals(blanklist.get(i)))){
           invalid = true;
           break;
        }
      }
      }


    return !invalid; 
  } 

  private void updateAttachment(List oldList, List newList, ItemDataIfc item, boolean pendingOrPool){
    if ((oldList == null || oldList.size() == 0 ) && (newList == null || newList.size() == 0)) return;
    List list = new ArrayList();
    HashMap map = getAttachmentIdHash(oldList);
    for (int i=0; i<newList.size(); i++){
      ItemAttachmentIfc a = (ItemAttachmentIfc)newList.get(i);
      if (map.get(a.getAttachmentId())!=null){
        // exist already, remove it from map
        map.remove(a.getAttachmentId());
      }
      else{
        // new attachments
        a.setItem(item);
        list.add(a);
      }
    }      
    // save new ones
    AssessmentService service;
    if (pendingOrPool) {
    	service = new AssessmentService();
    }
    else {
    	service = new PublishedAssessmentService();
    }
    service.saveOrUpdateAttachments(list);

    // remove old ones
    Set set = map.keySet();
    Iterator iter = set.iterator();
    while (iter.hasNext()){
      Long attachmentId = (Long)iter.next();
      service.removeItemAttachment(attachmentId.toString());
    }
  }

  private HashMap getAttachmentIdHash(List list){
    HashMap map = new HashMap();
    for (int i=0; i<list.size(); i++){
      ItemAttachmentIfc a = (ItemAttachmentIfc)list.get(i);
      map.put(a.getAttachmentId(), a);
    }
    return map;
  }
  
  private void updateItemFeedback(ItemFacade item, String feedbackTypeId, String feedbackText) {
	  Set itemFeedbackSet = item.getItemFeedbackSet();
	  if ((itemFeedbackSet == null || itemFeedbackSet.size() == 0) || !item.getItemFeedbackMap(itemFeedbackSet).containsKey(feedbackTypeId)) {
		  item.addItemFeedback(feedbackTypeId, feedbackText);
	  }
	  else {
		  Iterator iter = itemFeedbackSet.iterator();
		  while (iter.hasNext()) {
			  ItemFeedbackIfc itemFeedback = (ItemFeedbackIfc) iter.next();
			  if (itemFeedback.getTypeId().equals(feedbackTypeId)) {
				  itemFeedback.setText(feedbackText);
				  item.getItemFeedbackMap(itemFeedbackSet).put(feedbackTypeId, feedbackText);
			  }
		  }
	  }
  }
  
  private String [] getSurveyChoices(String scalename) {
	  String[] choices = new String[2];
	  // label is null because we don't use labels in survey
	  if (ItemMetaDataIfc.SURVEY_YES.equals(scalename)) {
		  choices = new String[2];
		  choices[0] = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","no");
		  choices[1] = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","yes");
	  }

	  if (ItemMetaDataIfc.SURVEY_AGREE.equals(scalename)) {
		  choices = new String[2];
		  choices[0] = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","disagree");
		  choices[1] = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","agree");
	  }
	  if (ItemMetaDataIfc.SURVEY_UNDECIDED.equals(scalename)) {
		  choices = new String[3];
		  choices[0] = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","disagree");
		  choices[1] = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","undecided");
		  choices[2] = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","agree");
	  }

	  if (ItemMetaDataIfc.SURVEY_AVERAGE.equals(scalename)) {
		  choices = new String[3];
		  choices[0] = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","below_average");
		  choices[1] = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","average");
		  choices[2] = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","above_average");
	  }
	  if (ItemMetaDataIfc.SURVEY_STRONGLY_AGREE.equals(scalename)) {
		  choices = new String[5];
		  choices[0] = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","strongly_disagree");
		  choices[1] = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","disagree");
		  choices[2] = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","undecided");
		  choices[3] = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","agree");
		  choices[4] = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","strongly_agree");
	  }

	  if (ItemMetaDataIfc.SURVEY_EXCELLENT.equals(scalename)) {
		  choices = new String[5];
		  choices[0] = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","unacceptable");
		  choices[1] = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","below_average");
		  choices[2] = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","average");
		  choices[3] = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","above_average");
		  choices[4] = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","excellent");
	  }
	  if (ItemMetaDataIfc.SURVEY_5.equals(scalename)) {
		  choices = new String[5];
		  choices[0] = "1";
		  choices[1] = "2";
		  choices[2] = "3";
		  choices[3] = "4";
		  choices[4] = "5";
	  }
	  if (ItemMetaDataIfc.SURVEY_10.equals(scalename)) {
		  choices = new String[10];
		  choices[0] = "1";
		  choices[1] = "2";
		  choices[2] = "3";
		  choices[3] = "4";
		  choices[4] = "5";
		  choices[5] = "6";
		  choices[6] = "7";
		  choices[7] = "8";
		  choices[8] = "9";
		  choices[9] = "10";
	  }
	  return choices;
  }
}
