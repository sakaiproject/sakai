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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tags.api.Tag;
import org.sakaiproject.tags.api.TagService;
import org.sakaiproject.tool.assessment.data.dao.assessment.Answer;
import org.sakaiproject.tool.assessment.data.dao.assessment.AnswerFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.FavoriteColChoices;
import org.sakaiproject.tool.assessment.data.dao.assessment.FavoriteColChoicesItem;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemText;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAnswer;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAnswerFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemText;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTagIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.PublishedItemFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.facade.TypeFacade;
import org.sakaiproject.tool.assessment.services.FinFormatException;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.PublishedItemService;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AnswerBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.CalculatedQuestionAnswerIfc;
import org.sakaiproject.tool.assessment.ui.bean.author.CalculatedQuestionFormulaBean;
import org.sakaiproject.tool.assessment.ui.bean.author.CalculatedQuestionVariableBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ImageMapItemBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemBean;
import org.sakaiproject.tool.assessment.ui.bean.author.MatchItemBean;
import org.sakaiproject.tool.assessment.ui.bean.author.CalculatedQuestionBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolBean;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolDataBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.TextFormat;
import org.sakaiproject.tool.assessment.ws.Item;
import org.sakaiproject.util.FormattedText;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Organization: Sakai Project</p>
 */
@Slf4j
public class ItemAddListener
    implements ActionListener {

  private static final TagService tagService= (TagService) ComponentManager.get( TagService.class );
    //private static ContextUtil cu;
  //private String scalename; // used for multiple choice Survey
  private boolean error = false;
  private boolean isPendingOrPool = false;
  private boolean isEditPendingAssessmentFlow = true;
  AssessmentService assessdelegate;

  /**
   * Standard process action method.
   * @param ae ActionEvent
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws AbortProcessingException {

	log.debug("ItemAdd LISTENER.");

    ItemAuthorBean itemauthorbean = (ItemAuthorBean) ContextUtil.lookupBean("itemauthor");
    ItemBean item = itemauthorbean.getCurrentItem();
    
    item.setEmiVisibleItems("0");
    String iText = item.getItemText();
    String iInstruction = item.getInstruction();
    String iType = item.getItemType();
    String err="";
    FacesContext context=FacesContext.getCurrentInstance();

    // To keep the temporal list of tags when validation fails
    String[] tagsFromForm = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterValuesMap().get("tag_selector[]");
    if (itemauthorbean.getDeleteTagsAllowed()) {
    	//we update the full list of tags for the input field
    	itemauthorbean.setTagsListToJson(convertTagSelectOptionsToJson(tagsFromForm));
    } else {
    	//we update the list of tags for the NEW tags input field, but tagsListToJson will maintain the original value
    	itemauthorbean.setTagsTempListToJson(convertTagSelectOptionsToJson(tagsFromForm));    
    }

    // SAK-6050
    // if((!iType.equals(TypeFacade.MATCHING.toString())&&((iText==null)||(iText.replaceAll("<.*?>", "").trim().equals(""))))|| (iType.equals(TypeFacade.MATCHING.toString()) && ((iInstruction==null)||(iInstruction.replaceAll("<.*?>", "").trim().equals(""))))){
    if( (!iType.equals(TypeFacade.MATCHING.toString())&&((iText==null) ||(iText.toLowerCase().replaceAll("<^[^(img)]*?>", "").trim().equals(""))))|| (iType.equals(TypeFacade.MATCHING.toString()) && ((iInstruction==null)||(iInstruction.toLowerCase().replaceAll("<^[^(img)]*?>", "").trim().equals(""))))){
    	
    	// Like Matching CaculatedQuestion will also use Instruction instead of itemText
    	if (!iType.equals(TypeFacade.CALCULATED_QUESTION.toString()) && !iType.equals(TypeFacade.IMAGEMAP_QUESTION.toString()) ) {
			String emptyText_err = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","emptyText_error");     
			context.addMessage(null,new FacesMessage(emptyText_err));
			return;
    	}
    }   

    if(iType.equals(TypeFacade.EXTENDED_MATCHING_ITEMS.toString())) {
    	checkEMI();
    }
    
    if(iType.equals(TypeFacade.MULTIPLE_CHOICE.toString())) {
    	checkMC(true);
    }

    if(iType.equals(TypeFacade.MULTIPLE_CORRECT.toString())) {
    	checkMC(false);
    }
    
    if(iType.equals(TypeFacade.MULTIPLE_CORRECT_SINGLE_SELECTION.toString())) {
    	checkMC(false);
    }
    
    if(iType.equals(TypeFacade.MATCHING.toString())) {   
		List l=item.getMatchItemBeanList();
	    if (l==null || l.size()==0){
		String noPairMatching_err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","noMatchingPair_error");
		context.addMessage(null,new FacesMessage(noPairMatching_err));
		error=true;
	    }
	}
    
    if(error) { 
    	return;
    }
    
    if(iType.equals(TypeFacade.MULTIPLE_CHOICE_SURVEY.toString()))
    {   
      String scaleName = item.getScaleName();
      if (StringUtils.isBlank(scaleName)){
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
      if (StringUtils.isBlank(corrAnswer)){
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
	
    if (iType.equals(TypeFacade.MATRIX_CHOICES_SURVEY.toString())){
    	if (isRowEmpty()){
    		err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","row_field_empty");
    		context.addMessage(null,new FacesMessage(err));
    		item.setOutcome("matrixChoicesSurveyItem");
    		item.setPoolOutcome("matrixChoicesSurveyItem");
    		return;
    	}
    	if (isColumnslessthan2()) {
    		err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","less_than_two_columns");
    		context.addMessage(null,new FacesMessage(err));
    		item.setOutcome("matrixChoicesSurveyItem");
    		item.setPoolOutcome("matrixChoicesSurveyItem");
    		return;
    	}
    } 

    // CALCULATED_QUESTION
    if (iType.equals(TypeFacade.CALCULATED_QUESTION.toString())) {
        
        // make sure any last minute updates to instructions are handles
        // this also does the standard validations
        CalculatedQuestionExtractListener extractListener = new CalculatedQuestionExtractListener();
        List<String> errors = extractListener.validate(item,false);
        if (errors.size() > 0) {
            for (String curError : errors) {
                context.addMessage(null, new FacesMessage(curError));
            }
            error = true;
        }
                
        if(error) {
            item.setOutcome("calculatedQuestion");
            item.setPoolOutcome("calculatedQuestion");
            return;
        }
        
        // if no errors remove disabled variables and formulas before saving
        CalculatedQuestionBean question = item.getCalculatedQuestion();
        Iterator<CalculatedQuestionVariableBean> variableIter = question.getVariables().values().iterator();        
        while (variableIter.hasNext()) {
            CalculatedQuestionVariableBean variable = variableIter.next();
            if (!variable.getActive()) {
                variableIter.remove();
            }
        }
        Iterator<CalculatedQuestionFormulaBean> formulaIter = question.getFormulas().values().iterator();
        while (formulaIter.hasNext()) {
            CalculatedQuestionFormulaBean formula = formulaIter.next();
            if (!formula.getActive()) {
                formulaIter.remove();
            }
        }
    }
    if (iType.equals(TypeFacade.IMAGEMAP_QUESTION.toString())) {
 	   
        List l=item.getImageMapItemBeanList();
   	    if (l==null || l.size()==0){
   		String noPairImageMap_err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","noImageMapPair_error");
   		context.addMessage(null,new FacesMessage(noPairImageMap_err));
   		error=true;
   	    
    	}
   	 if(error)
   		return;
    }
	try {
		saveItem(itemauthorbean);
	}
	catch (FinFormatException e) {
		err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","fin_invalid_characters_error");
	    context.addMessage(null,new FacesMessage(err));
	    item.setOutcome("fillInNumericItem");
	    item.setPoolOutcome("fillInNumericItem");
	    return;
	}

    item.setOutcome("editAssessment");
    item.setPoolOutcome("editPool");
    itemauthorbean.setItemTypeString("");
  }

	private String convertTagSelectOptionsToJson(String[] tagsFromForm){

		String tagsListToJson = "[";
		if (tagsFromForm!=null) {
			Boolean more = false;
			for (String s:tagsFromForm) {
				if (more) {
					tagsListToJson += ",";
				}
				if (tagService.getTags().getForId(s).isPresent()) {
					Tag tag = tagService.getTags().getForId(s).get();
					String tagLabel = tag.getTagLabel();
					String tagCollectionName = tag.getCollectionName();
					tagsListToJson += "{\"tagId\":\"" + s + "\",\"tagLabel\":\"" + tagLabel + "\",\"tagCollectionName\":\"" + tagCollectionName + "\"}";
					more = true;
				}
			}
		}
		tagsListToJson += "]";
		return tagsListToJson;
	}

	private void checkEMI() {
		ItemAuthorBean itemauthorbean = (ItemAuthorBean) ContextUtil
				.lookupBean("itemauthor");
		ItemBean item = itemauthorbean.getCurrentItem();
		FacesContext context = FacesContext.getCurrentInstance();

		boolean missingOrInvalidAnswerOptionLabels = false;
		boolean blankSimpleOptionText = false;
		boolean tooFewAnswerOptions = false;
		boolean richTextOptionsError = false;
		
		if(item.getAnswerOptionsSimpleOrRich().equals("2")) {//Simple Paste
			if (item.getEmiAnswerOptionsPaste() != null
					&& !item.getEmiAnswerOptionsPaste().trim().equals("")) {
				item.populateEmiAnswerOptionsFromPasted();
			}
			item.setAnswerOptionsSimpleOrRich(ItemDataIfc.ANSWER_OPTIONS_SIMPLE.toString());
		}//no else here. we need to go into the next if!
		if (item.getAnswerOptionsSimpleOrRich().equals(ItemDataIfc.ANSWER_OPTIONS_SIMPLE.toString())) {

			List<AnswerBean> answerOptions = item.getEmiAnswerOptionsClean();
			String txt = "";
			Iterator<AnswerBean> iter = answerOptions.iterator();
			while (iter.hasNext()) {
				AnswerBean answerbean = iter.next();
				txt = answerbean.getText().trim();
				if ((txt == null) || (txt.equals(""))) {
					blankSimpleOptionText = true;
				}
			} // end of while
		}
		else { // Rich Options
			String richText = item.getEmiAnswerOptionsRich();;
			if (richText.toLowerCase().replaceAll("<^[^(img)]*?>", "").trim()
					.equals("")) {
				item.setEmiAnswerOptionsRich("");
			}
			if (item.getEmiAnswerOptionsRich().equals("") && !itemauthorbean.getHasAttachment()) {
				richTextOptionsError = true;
			}
		}
		
		String availableOptionLabels = item.getEmiAnswerOptionLabelsSorted();
		if (availableOptionLabels.length()<2) {
			tooFewAnswerOptions = true;
		}
		else if (!availableOptionLabels.startsWith("A") || !ItemDataIfc.ANSWER_OPTION_LABELS.contains(availableOptionLabels)) {
			missingOrInvalidAnswerOptionLabels = true;
		}
		
		// Validate Correct Option Labels here because these require cross-field
		// validation
		List qaCombos = (List) item.getEmiQuestionAnswerCombinationsClean();
		Iterator qaCombosIter = qaCombos.iterator();
		int invalidQACombos = 0;
		while (qaCombosIter.hasNext()) {
			AnswerBean qaCombo = (AnswerBean) qaCombosIter.next();
			boolean isValidQACombo = qaCombo.isValidCorrectOptionLabels(availableOptionLabels, context);
			if (!isValidQACombo) invalidQACombos++;
		}

		if (!error) {
			if (blankSimpleOptionText) {
				String simpleTextOptionsBlank_err = ContextUtil
						.getLocalizedString(
								"org.sakaiproject.tool.assessment.bundle.AuthorMessages",
								"simple_text_options_blank_error");
				context.addMessage(null, new FacesMessage(simpleTextOptionsBlank_err));
				error = true;
			}			
			
			if (tooFewAnswerOptions) {
				String answerList_err = ContextUtil
						.getLocalizedString(
								"org.sakaiproject.tool.assessment.bundle.AuthorMessages",
								"answerList_error");
				context.addMessage(null, new FacesMessage(answerList_err));
				error = true;
			} 
			
			if (missingOrInvalidAnswerOptionLabels) {
				String answerOptionLabelError = ContextUtil
						.getLocalizedString(
								"org.sakaiproject.tool.assessment.bundle.AuthorMessages",
								"missing_or_invalid_answer_options_labels_error");
				context.addMessage(null, new FacesMessage(answerOptionLabelError + ": "
						+ availableOptionLabels));
				error = true;
			}
			
			if (richTextOptionsError) {
				String richOptions_err = ContextUtil
						.getLocalizedString(
								"org.sakaiproject.tool.assessment.bundle.AuthorMessages",
								"rich_text_options_error");
				context.addMessage(null, new FacesMessage(richOptions_err));
				error = true;
			}			
			
			if (invalidQACombos > 0) {
				error = true;
			}
		}

		if (error) {
			item.setOutcome("emiItem");
			item.setPoolOutcome("emiItem");
		}
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
			  String answerTxt=answerbean.getText();
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
		    if((notEmpty==true)&&(indexOfOpen+1 !=index)&&(!(text.substring(indexOfOpen+1,index).equals("</p><p>")))){
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
           
		if((hasOpen==true)&&(!Character.isWhitespace(c))){
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
    		    if((notEmpty==true)&&(indexOfOpen+1 !=index)&&(!(text.substring(indexOfOpen+1,index).equals("</p><p>")))){
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
               
    		if((hasOpen==true)&&(!Character.isWhitespace(c))){
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
    
    public boolean isRowEmpty() {
    	ItemAuthorBean itemauthorbean = (ItemAuthorBean) ContextUtil.lookupBean("itemauthor");
    	ItemBean item =itemauthorbean.getCurrentItem();

    	String text=item.getRowChoices();
    	if (text != null && text.trim().length() == 0)
    		return true;
    	else if (text == null)
    		return true;
    	return false;

    }

    public boolean isColumnslessthan2(){
    	ItemAuthorBean itemauthorbean = (ItemAuthorBean) ContextUtil.lookupBean("itemauthor");
    	ItemBean item =itemauthorbean.getCurrentItem();

    	String text=item.getColumnChoices();
    	String[] columns;
    	columns = text.split(System.getProperty("line.separator"));
    	if(columns.length < 2) {
    		return true;	
    	}
    	return false;
    }

  public void saveItem(ItemAuthorBean itemauthor) throws FinFormatException{
	  boolean update = false;
      ItemBean bean = itemauthor.getCurrentItem();
      ItemFacade item;
      AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
      isEditPendingAssessmentFlow = author.getIsEditPendingAssessmentFlow();
      log.debug("**** isEditPendingAssessmentFlow : " + isEditPendingAssessmentFlow);
      String target = itemauthor.getTarget();
      boolean isFromQuestionPool = false;
      if (target != null && (target.equals(ItemAuthorBean.FROM_QUESTIONPOOL) && ! author.getIsEditPoolFlow())) {
    	  isFromQuestionPool = true;
      }
      log.debug("**** isFromQuestionPool : " + isFromQuestionPool);
      isPendingOrPool = isEditPendingAssessmentFlow || (isFromQuestionPool && ! author.getIsEditPoolFlow());
      ItemService delegate;
      if (isPendingOrPool) {
    	  EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_REVISE, "siteId=" + AgentFacade.getCurrentSiteId() + ", itemId=" + itemauthor.getItemId(), true));
      	  delegate = new ItemService();
      }
      else {
    	  EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_PUBLISHED_ASSESSMENT_REVISE, "siteId=" + AgentFacade.getCurrentSiteId() + ", itemId=" + itemauthor.getItemId(), true));
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
      item.setScore(Double.valueOf(bean.getItemScore()));
      item.setDiscount(Double.valueOf(bean.getItemDiscount()));
      //default should be "true", so anything other than "false" is a true value
      item.setScoreDisplayFlag(!"false".equals(bean.getItemScoreDisplayFlag()));
      item.setMinScore(bean.getItemMinScore());
      item.setHint("");

      item.setStatus(ItemDataIfc.ACTIVE_STATUS);

      item.setTypeId(Long.valueOf(bean.getItemType()));

  	  if (item.getTypeId().equals(TypeFacade.EXTENDED_MATCHING_ITEMS)) {
	    item.setAnswerOptionsSimpleOrRich(Integer.valueOf(bean.getAnswerOptionsSimpleOrRich()));
	    item.setAnswerOptionsRichCount(Integer.valueOf(bean.getAnswerOptionsRichCount()));
  	  }
      
      item.setCreatedBy(AgentFacade.getAgentString());
      item.setCreatedDate(new Date());
      item.setLastModifiedBy(AgentFacade.getAgentString());
      item.setLastModifiedDate(new Date());

      if (bean.getInstruction() != null) {
        // for matching and matrix Survey
        item.setInstruction(bean.getInstruction());
      }
      // update hasRationale
      if (bean.getRationale() != null) {
        item.setHasRationale(Boolean.valueOf(bean.getRationale()));
      }
      else {
        item.setHasRationale(Boolean.FALSE);
      }

      item.setPartialCreditFlag(Boolean.valueOf(bean.getPartialCreditFlag()));
      
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
    	  	if (item.getTypeId().equals(TypeFacade.EXTENDED_MATCHING_ITEMS)) {
                item.setItemTextSet(prepareTextForEMI(item, bean, itemauthor));
    	  	}
    	  	else if (item.getTypeId().equals(TypeFacade.MATCHING)) {
                item.setItemTextSet(prepareTextForMatching(item, bean, itemauthor));
            }
			else if(item.getTypeId().equals(TypeFacade.CALCULATED_QUESTION)) {
              item.setItemTextSet(prepareTextForCalculatedQuestion(item, bean, itemauthor));
			}
	    	  else if(item.getTypeId().equals(TypeFacade.MATRIX_CHOICES_SURVEY)) {
	    		  item.setItemTextSet(prepareTextForMatrixChoice(item, bean, itemauthor));
	    	  }
	    	  else if(item.getTypeId().equals(TypeFacade.IMAGEMAP_QUESTION)) {
		          item.setItemTextSet(prepareTextForImageMapQuestion(item, bean, itemauthor));
		  	  }
	    	  else {  //Other Types
	    		  item.setItemTextSet(prepareText(item, bean, itemauthor));
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

	  updateAttachments(itemauthor.getAttachmentList(), item);

	  //Manage the tags.
	  String[] tagsFromForm= FacesContext.getCurrentInstance().getExternalContext().getRequestParameterValuesMap().get("tag_selector[]");
	  Set<ItemTagIfc> originalTagList = itemauthor.getTagsList();

	  if (itemauthor.getDeleteTagsAllowed()){
	  	//Let's check the ones that have been deleted and delete from the item
	  	if (originalTagList!=null){
			Iterator<ItemTagIfc> ite = originalTagList.iterator();
			while ( ite.hasNext() ) {
			  ItemTagIfc tagToShow = (ItemTagIfc)ite.next();
			  if (tagsFromForm==null || !(Arrays.asList(tagsFromForm).contains(tagToShow.getTagId()))){
			  	  item.removeItemTagByTagId(tagToShow.getTagId());
			  }
		  	}
	  	}
	  }

	  //Let's add the new ones
	  if (tagsFromForm!=null){
		  for (String s:tagsFromForm) {
			  Boolean found = false;
			  if (originalTagList != null) {
				  Iterator<ItemTagIfc> it = originalTagList.iterator();
				  while (it.hasNext()) {
					  ItemTagIfc tagToShow = (ItemTagIfc) it.next();
					  if (tagToShow.getTagId().equals(s)) {
						  found = true;  //If it is in the list... we don't need to do anything.
						  break;
					  }
				  }
			  }
			  if (!found) {  //If it is not in the list... we need to add it.
				  if (tagService.getTags().getForId(s).isPresent()) {
					  Tag tag = tagService.getTags().getForId(s).get();
					  item.addItemTag(s, tag.getTagLabel(), tag.getTagCollectionId(), tag.getCollectionName());
				  }

			  }
		  }
	  }
      itemauthor.setTagsList(item.getItemTagSet());//To avoid add extra labels when refreshing the page manually.


	  if (isFromQuestionPool) {
        // Came from Pool manager
		  if (item.getTypeId().equals(TypeFacade.EXTENDED_MATCHING_ITEMS)) {
			  Iterator emiItemIter = itemauthor.getCurrentItem().getEmiQuestionAnswerCombinationsClean().iterator();
			  while (emiItemIter.hasNext()) {
				  AnswerBean answerBean = (AnswerBean)emiItemIter.next();
				  ItemTextIfc itemText = item.getItemTextBySequence(answerBean.getSequence());
				  updateItemTextAttachment(answerBean.getAttachmentList(),
						  itemText, true);
			  }
		  }

        delegate.saveItem(item);



       item = delegate.getItem(item.getItemId().toString());


        QuestionPoolService qpdelegate = new QuestionPoolService();

        if (!qpdelegate.hasItem(item.getItemIdString(),
        		Long.valueOf(itemauthor.getQpoolId()))) {
          qpdelegate.addItemToPool(item.getItemId(),
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
        
        List addedQuestions = qpoolbean.getAddedQuestions();
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
    	  
    	  if(assessdelegate == null) {
    		  assessdelegate = isEditPendingAssessmentFlow ? new AssessmentService() : new PublishedAssessmentService();
    	  }
    	  
        if (bean.getSelectedSection() != null) {
 
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
          
          if (author != null && author.getIsEditPoolFlow()) {
              section.getData().setLastModifiedDate(item.getLastModifiedDate());
              DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
              section.addSectionMetaData(SectionDataIfc.QUESTIONS_RANDOM_DRAW_DATE, df.format(item.getLastModifiedDate()));
              assessdelegate.saveOrUpdateSection(section);
          }

		  if (item.getTypeId().equals(TypeFacade.EXTENDED_MATCHING_ITEMS)) {
			for(AnswerBean answerBean: itemauthor.getCurrentItem().getEmiQuestionAnswerCombinationsClean()){
				ItemTextIfc itemText = item.getItemTextBySequence(answerBean.getSequence());
				updateItemTextAttachment(answerBean.getAttachmentList(),
						itemText, isEditPendingAssessmentFlow);
			}
		  }

		  delegate.saveItem(item);

          item = delegate.getItem(item.getItemId().toString());

        }

        // prepare saving column choice to favorites (SAM_FAVORITECOLCHOICES_T and SAM_FAVORITECOLCHOICESITEM_T)
        // and save
        if (bean.getAddToFavorite()){
        	FavoriteColChoices favorite = new FavoriteColChoices();
        	favorite.setFavoriteName(bean.getFavoriteName().trim());
        	//find the agentId
        	favorite.setOwnerStringId(AgentFacade.getAgentString());

        	String[] temp = bean.getColumnChoices().split(System.getProperty("line.separator"));
        	//remove the empty string
        	List<String> stringList = new ArrayList<String>();

        	for(String string : temp) {
        		if(string != null && string.trim().length() > 0) {
        			stringList.add(string);
        		}
        	}
        	temp = stringList.toArray(new String[stringList.size()]);
        	for(int i=0; i<temp.length; i++){
        		FavoriteColChoicesItem favoriteChoiceItem = new FavoriteColChoicesItem(StringUtils.chomp(temp[i]),Integer.valueOf(i));
        		favoriteChoiceItem.setFavoriteChoice(favorite);
        		favorite.getFavoriteItems().add(favoriteChoiceItem);
        	}
        	delegate.saveFavoriteColumnChoices(favorite);
        }

        QuestionPoolService qpdelegate = new QuestionPoolService();
	// removed the old pool-item mappings
          if ( (bean.getOrigPool() != null) && (!bean.getOrigPool().equals(""))) {
            qpdelegate.removeQuestionFromPool(item.getItemId(),
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
            qpdelegate.addItemToPool(item.getItemId(),
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
	  //This is the event used by the Elasticsearch to catalog the questions.
	  // We can't rely in the sam.assessment.review one because
	  // It happens before the saving si we don't have the new ItemId
	  if (isPendingOrPool) {
		  EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_SAVEITEM, "/sam/" + AgentFacade.getCurrentSiteId() + "/saved itemId=" + item.getItemId().toString(), true));
	  }
	  else {
		  EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_PUBLISHED_ASSESSMENT_SAVEITEM, "/sam/" + AgentFacade.getCurrentSiteId() + "/saved  publishedItemId=" + item.getItemId().toString(), true));
	  }


      // sorry, i need this for item attachment, used by SaveItemAttachmentListener.
      bean.setItemId(item.getItemId().toString());
      itemauthor.setItemId(item.getItemId().toString());

	  //Once we have saved the items... we need to MAYBE update tags in other questions.
	  //We use these 2 properties
	  //samigo.author.multitag.singlequestion=true
	  //samigo.author.multitag.singlequestion.check=true
	  //Admins can always decide.

	  boolean saveMultiTagsCheck;
	  String[] multiTagsSingleCheck = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterValuesMap().get("multiTagsSingleCheck");
	  if ((multiTagsSingleCheck != null) && (multiTagsSingleCheck.length>0)){
		  saveMultiTagsCheck=true;
	  }else{
		  saveMultiTagsCheck=false;
	  }

	  AuthorizationBean authorizationBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");
	  //If we are admins... ONLY if saveMultiTagscheck. If we are other user, then if the check is on or
	  // if the property samigo.author.multitag.singlequestion is set to true
	  if (saveMultiTagsCheck || (itemauthor.getMultiTagsSingleQuestion() && !(authorizationBean.isSuperUser()))){
		 delegate.saveTagsInHashedQuestions(item);
	  }
  }

/**
 * for the current choice, loop through all answers and add unique matches to the list of valid matches for the choice. 
 * @param choicebean current choice
 * @param matchItemBeanList
 * @param item
 * @param bean
 * @return
 */
  private ItemText selectAnswers(MatchItemBean choicebean, List<MatchItemBean> matchItemBeanList, ItemFacade item, ItemBean bean) {
	  
	  // Create a list of valid answers to loop through.  Ignore answers that are distractors
	  // or are controlled by another MatchItemBean
	  List<MatchItemBean> validAnswers = new ArrayList<MatchItemBean>();
	  Iterator<MatchItemBean>validAnswerIter = matchItemBeanList.iterator();
	  while (validAnswerIter.hasNext()) {
		  MatchItemBean validAnswer = validAnswerIter.next();
		  if (MatchItemBean.CONTROLLING_SEQUENCE_DEFAULT.equals(validAnswer.getControllingSequence())) {
			  validAnswers.add(validAnswer);
		  }
	  }
	  
	  ItemText choicetext = new ItemText();
	  choicetext.setItem(item.getData()); // all set to the same
	  choicetext.setSequence(choicebean.getSequence());
	  choicetext.setText(stripPtags(choicebean.getChoice()));

	  // loop through matches for in validAnswers list and add all to this choice
	  Iterator<MatchItemBean>answeriter = validAnswers.iterator();	  
	  Set<AnswerIfc> answerSet = new HashSet<AnswerIfc>();
	  while (answeriter.hasNext()) {
		  Answer answer = null;
		  MatchItemBean answerbean = (MatchItemBean) answeriter.next();
		  if (answerbean.getSequence().equals(choicebean.getSequence()) ||
				  answerbean.getSequenceStr().equals(choicebean.getControllingSequence())) {
			  // correct answers
			  answer = new Answer(choicetext, stripPtags(answerbean
					  .getMatch()), answerbean.getSequence(), AnswerBean
					  .getChoiceLabels()[answerbean.getSequence()
					                     .intValue() - 1], Boolean.TRUE, null, Double.valueOf(
					                    		 bean.getItemScore()), Double.valueOf(0d), Double.valueOf(bean.getItemDiscount()));

		  } else {
			  // incorrect answers
			  answer = new Answer(choicetext, stripPtags(answerbean
					  .getMatch()), answerbean.getSequence(), AnswerBean
					  .getChoiceLabels()[answerbean.getSequence()
					                     .intValue() - 1], Boolean.FALSE, null,  Double.valueOf(
					                    		 bean.getItemScore()), Double.valueOf(0d), Double.valueOf(bean.getItemDiscount()));
		  }

		  // record answers for all combination of pairs
		  HashSet<AnswerFeedback> answerFeedbackSet = new HashSet<AnswerFeedback>();
		  answerFeedbackSet.add(new AnswerFeedback(answer,
				  AnswerFeedbackIfc.CORRECT_FEEDBACK,
				  stripPtags(answerbean.getCorrMatchFeedback())));
		  answerFeedbackSet.add(new AnswerFeedback(answer,
				  AnswerFeedbackIfc.INCORRECT_FEEDBACK,
				  stripPtags(answerbean.getIncorrMatchFeedback())));
		  answer.setAnswerFeedbackSet(answerFeedbackSet);
		  answerSet.add(answer);
	  }
	  choicetext.setAnswerSet(answerSet);
	  return choicetext;
  }
  
  private Set prepareTextForMatching(ItemFacade item, ItemBean bean, ItemAuthorBean itemauthor) {
	  // looping through matchItemBean
	  List<MatchItemBean>matchItemBeanList = bean.getMatchItemBeanList();
	  HashSet<ItemText> textSet = new HashSet<ItemText>();
	  
	  Iterator<MatchItemBean> choiceiter = matchItemBeanList.iterator();
	  while (choiceiter.hasNext()) {
		  MatchItemBean choicebean = choiceiter.next();
		  ItemText choicetext = selectAnswers(choicebean, matchItemBeanList, item, bean);
		  textSet.add(choicetext);  
	  }
	  return textSet;
  }
  
  /**
   * Updated and refactored - Aug 2010
   * Prepare Text for Extended Matching Item Questions
   * @param item
   * @param bean
   * @param itemauthor
   * @return
   */
  private HashSet prepareTextForEMI(ItemFacade item, ItemBean bean,
			ItemAuthorBean itemauthor) {
	  
		HashSet textSet = new HashSet();
		HashSet answerSet1 = new HashSet();
	  
	  	// ///////////////////////////////////////////////////////////
		//
		// 1. save Theme and Lead-In Text and Answer Options
		//  
		// ///////////////////////////////////////////////////////////
		ItemTextIfc textTheme = new ItemText();
		textTheme.setItem(item.getData());
		textTheme.setSequence(ItemTextIfc.EMI_THEME_TEXT_SEQUENCE);
		textTheme.setText(bean.getItemText());
		
		ItemTextIfc textAnswerOptions = new ItemText();
		textAnswerOptions.setItem(item.getData());
		textAnswerOptions.setSequence(ItemTextIfc.EMI_ANSWER_OPTIONS_SEQUENCE);
		textAnswerOptions.setText(bean.getEmiAnswerOptionsRich());

		ItemTextIfc textLeadIn = new ItemText();
		textLeadIn.setItem(item.getData());
		textLeadIn.setSequence(ItemTextIfc.EMI_LEAD_IN_TEXT_SEQUENCE);
		textLeadIn.setText(bean.getLeadInStatement());
		
		// ///////////////////////////////////////////////////////////
		//
		// 2. save Answer Options - emiAnswerOptions
		// with ItemText  (seq=ItemTextIfc.EMI_ANSWER_OPTIONS_SEQUENCE).
		// These will be used to construct the actual answers.
		// ///////////////////////////////////////////////////////////
		Iterator iter = bean.getEmiAnswerOptionsClean().iterator();
		AnswerIfc answer = null;
		while (iter.hasNext()) {
			AnswerBean answerbean = (AnswerBean) iter.next();
			answer = new Answer(textAnswerOptions, stripPtags(answerbean.getText()),
					answerbean.getSequence(), answerbean.getLabel(),
					Boolean.FALSE, null,
					null, null, null, null);
			answerSet1.add(answer);
		}
		
		textAnswerOptions.setAnswerSet(answerSet1);
		textSet.add(textTheme);
		textSet.add(textAnswerOptions);
		textSet.add(textLeadIn);

		// ///////////////////////////////////////////////////////////
		//
		// 3. Prepare and save actual answers from answer components 
		// (emiAnswerOptions and emiQuestionAnswerCombinations)
		// ///////////////////////////////////////////////////////////
                @SuppressWarnings("unchecked")
		List<AnswerBean> emiQuestionAnswerCombinations = bean.getEmiQuestionAnswerCombinationsClean();
                int answerCombinations = emiQuestionAnswerCombinations.size();
                iter = emiQuestionAnswerCombinations.iterator();
		AnswerBean qaCombo = null;
		Double itemScore = 0.0;
		while (iter.hasNext()) {
			qaCombo = (AnswerBean) iter.next();
			
			ItemTextIfc itemText = new ItemText();
			itemText.setItem(item.getData());
			itemText.setSequence(qaCombo.getSequence());
			itemText.setText(qaCombo.getText());
			int requiredOptions = (Integer.valueOf(qaCombo.getRequiredOptionsCount())).intValue();
			if (requiredOptions == 0) {
				requiredOptions = qaCombo.correctOptionsCount();
			}
			itemText.setRequiredOptionsCount(requiredOptions);
			itemScore += qaCombo.getScore();

			//for emi the score per correct answer is itemTotal/requiredOptions
			//the discount is 1/2 the negative of that for answers more then required
			HashSet answerSet = new HashSet();
			
			if (Integer.valueOf(bean.getAnswerOptionsSimpleOrRich()).equals(ItemDataIfc.ANSWER_OPTIONS_SIMPLE) ) {
				Iterator selectionOptions = textAnswerOptions.getAnswerArraySorted().iterator();
				while (selectionOptions.hasNext()) {
					AnswerIfc selectOption = (AnswerIfc) selectionOptions.next();
					answerSet.add(getAnswer(qaCombo, itemText, selectOption.getText(),
							selectOption.getSequence(), selectOption.getLabel(), requiredOptions));
				}
			}
			else { // ANSWER_OPTION_RICH
				int answerOptionsCount = Integer.valueOf(bean.getAnswerOptionsRichCount());
				for (int i=0; i<answerOptionsCount; i++) {
					String label = ItemDataIfc.ANSWER_OPTION_LABELS.substring(i, i+1);
					answerSet.add(getAnswer(qaCombo, itemText, label,
							Long.valueOf(i), label, requiredOptions));
				}
			}
			itemText.setAnswerSet(answerSet);
			textSet.add(itemText);
			
		}
		item.setScore(itemScore);
		return textSet;
	}
  
	private AnswerIfc getAnswer(AnswerBean qaCombo, ItemTextIfc itemText, String text, Long sequence, String label, int requiredOptions) {
		String correctLabels = qaCombo.getCorrectOptionLabels();
		int correctRequiredCount = correctLabels.length()<requiredOptions?correctLabels.length():requiredOptions;
		boolean isCorrect = qaCombo.getCorrectOptionLabels().contains(label);

		// item option score
		Double score = qaCombo.getScore() / correctRequiredCount;
		
		return new Answer(itemText, text,
				sequence, label, isCorrect,
				qaCombo.getScoreUserSet() ? "user" : "auto", 
				isCorrect ? score : 0.0, null, isCorrect ? 0.0 : -score / 2, null);
	}

  private String[] returnMatrixChoices(ItemBean bean,String str){
	  String[] result=null,temp=null;
	  if ("row".equals(str))
		  temp = bean.getRowChoices().split(System.getProperty("line.separator"));
	  else 
		  temp = bean.getColumnChoices().split(System.getProperty("line.separator"));

	  //remove the empty string
	  List<String> stringList = new ArrayList<String>();

	  for(String string : temp) {
		  if(string != null && string.trim().length() > 0) {
			  stringList.add(string);
		  }
	  }
	  temp = stringList.toArray(new String[stringList.size()]);	  
	  result = new String[temp.length];
	  for (int i=0; i<temp.length;i++){
		  //remove new line
		  result[i] = StringUtils.chomp(temp[i]);
	  }

	  return result;

  }
  private HashSet prepareTextForMatrixChoice(ItemFacade item, ItemBean bean,
		  ItemAuthorBean itemauthor) {
	  // looping through row and column choices
	  String[] rowChoices = returnMatrixChoices(bean,"row");
	  String[] columnChoices = returnMatrixChoices(bean,"column");

	  bean.setInstruction(bean.getItemText());
	  item.getData().setInstruction(bean.getItemText());
	  HashSet textSet = new HashSet();

	  for(int i = 0; i<rowChoices.length;i++)
	  {
		  ItemText itemText = new ItemText();
		  itemText.setItem(item.getData());
		  itemText.setSequence(Long.valueOf(i+1));
		  itemText.setText(rowChoices[i]);
		  HashSet answerSet = new HashSet();
		  Answer answer = null;
		  for(int j=0; j< columnChoices.length;j++){
			  answer = new Answer(itemText,columnChoices[j],Long.valueOf(j+1),null, null, null, Double.valueOf(bean.getItemScore()), Double.valueOf(0d), Double.valueOf(bean.getItemDiscount()));
			  answerSet.add(answer);
		  }
		  itemText.setAnswerSet(answerSet);
		  textSet.add(itemText);
	  }
	  return textSet;
  }

	private HashSet prepareText(ItemFacade item, ItemBean bean,
			ItemAuthorBean itemauthor) throws FinFormatException {
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
							"", Boolean.TRUE, null, Double.valueOf(bean
									.getItemScore()), Double.valueOf(0d), Double.valueOf(bean.getItemDiscount()));
				} else {
					newanswer = new Answer(text1, theanswer, Long.valueOf(i + 1),
							"", Boolean.FALSE, null, Double.valueOf(bean
									.getItemScore()), Double.valueOf(0d), Double.valueOf(bean.getItemDiscount()));
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
					null, Boolean.TRUE, null, Double.valueOf(bean.getItemScore()), Double.valueOf(0d), Double.valueOf(bean.getItemDiscount()));

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
						null, null, null, Double.valueOf(bean.getItemScore()), Double.valueOf(0d), Double.valueOf(bean.getItemDiscount()));
				answerSet1.add(answer1);
			}
			text1.setAnswerSet(answerSet1);
			textSet.add(text1);
		}

		// not doing parsing in authoring

		else if (item.getTypeId().equals(TypeFacade.FILL_IN_BLANK)) {
			// this is for fill in blank
			String entiretext = bean.getItemText();
			String processedText [] = processFIBFINText(entiretext);
			text1.setText(processedText[0]);;
			Object[] fibanswers = getFIBanswers(processedText[1]).toArray();
			for (int i = 0; i < fibanswers.length; i++) {
				String oneanswer = (String) fibanswers[i];
				Answer answer1 = new Answer(text1, oneanswer, Long.valueOf(i + 1),
						null, Boolean.TRUE, null,
						Double.valueOf(bean.getItemScore()), Double.valueOf(0d), Double.valueOf(bean.getItemDiscount()));
				answerSet1.add(answer1);
			}

			text1.setAnswerSet(answerSet1);
			textSet.add(text1);

		}

		else if (item.getTypeId().equals(TypeFacade.FILL_IN_NUMERIC)) {
			// this is for fill in numeric
			String entiretext = bean.getItemText();
			String processedText [] = processFIBFINText(entiretext);
			text1.setText(processedText[0]);;
			Object[] finanswers = getFINanswers(processedText[1]).toArray();
			for (int i = 0; i < finanswers.length; i++) {
				String oneanswer = (String) finanswers[i];
				Answer answer1 = new Answer(text1, oneanswer, Long.valueOf(i + 1),
						null, Boolean.TRUE, null,
						Double.valueOf(bean.getItemScore()), Double.valueOf(0d), Double.valueOf(bean.getItemDiscount()));
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
							Boolean.TRUE, null, Double.valueOf(bean.getItemScore()), Double.valueOf(100d), Double.valueOf(bean.getItemDiscount()));
				} else {
					if (item.getTypeId().equals(TypeFacade.MULTIPLE_CHOICE) && item.getPartialCreditFlag()){
						Double pc =  Double.valueOf(answerbean.getPartialCredit()); //--mustansar
						if (pc == null) {
							pc = Double.valueOf(0d);
						}
						answer = new Answer(text1, 
								stripPtags(answerbean.getText()), 
								answerbean.getSequence(),
								answerbean.getLabel(),
								Boolean.FALSE, null, Double.valueOf(bean.getItemScore()) , 
								pc,
								Double.valueOf(bean.getItemDiscount()));}
					else {
						answer = new Answer(text1, 
								stripPtags(answerbean.getText()), 
								answerbean.getSequence(),
								answerbean.getLabel(),
								Boolean.FALSE, null, Double.valueOf(bean.getItemScore()) ,
								Double.valueOf(0d), //No partial Credit since it is not enabled the column is not null 
								Double.valueOf(bean.getItemDiscount()));
					}
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
	
	  /**
	   * prepareTextForCalculatedqQestion takes the formulas and variables that are 
	   * stored in CalculatedQuestionFormulaBeans and CalculatedQuestionVariableBeans
	   * and translates them into ItemTextIfc and AnswerIfc objects.  The only difference
	   * between the formula and the variable is what information is actually kept in
	   * the answer.text field.  ItemText has the name of the formula or variable in ItemTextIfc.text
	   * AnswerIfc.text stores either a formula as (formula string)|(tolerance),(decimal places) or a
	   * variable as (min)|(max),(decimal places).
	   * <p>Unlike matching answers, answerfeedback is not kept here; only the feedback associated with the entire
	   * question is persisted.
	   * @param item
	   * @param bean
	   * @param itemauthor
	   * @return
	   */
	  private Set<ItemText> prepareTextForCalculatedQuestion(ItemFacade item, ItemBean bean,
	          ItemAuthorBean itemauthor) {
	      CalculatedQuestionBean calcBean = bean.getCalculatedQuestion();      
	      Set<ItemText> textSet = new HashSet<ItemText>();
	      double score = Double.valueOf(bean.getItemScore());
	      double partialCredit = 0d;
	      double discount = Double.valueOf(bean.getItemDiscount());
	      String grade = null;            
	      
	      // Variables and formulas are very similar, and both have entries in the 
	      // sam_itemtext_t table.  Because of the way the data is structured, every 
	      // answer stored in sam_answer_t is a possible match to every ItemText 
	      // stored in sam_itemtext_t.   If there is one variable and one formula, 
	      // there are 2 entries in sam_itemtext_t and 4 entries in sam_answer_t.  
	      // 2 variables and 2 formulas has 4 entries in sam_itemtext_t and 16 entries 
	      // in sam_answer_t.  This is required for the current design (which makes 
	      // more sense for other question types; we're just trying to work within 
	      // that structure.  We loop through each formula and variable to create 
	      // an entry in sam_itemtext_t (ItemText choiceText).  Then for each 
	      // choicetext, we loop through all variables and formulas to create the 
	      // answer objects.
	      List<CalculatedQuestionAnswerIfc> list = new ArrayList<CalculatedQuestionAnswerIfc>();
	      list.addAll(calcBean.getFormulas().values());
	      list.addAll(calcBean.getVariables().values());
	      
	      // loop through all variables and formulas to create ItemText objects
	      for (CalculatedQuestionAnswerIfc varFormula : list) {
	          ItemText choiceText = new ItemText();
	          choiceText.setItem(item.getData());
	          choiceText.setText(varFormula.getName());
	          Long sequence = varFormula.getSequence();
	          choiceText.setSequence(sequence);
	          
	          Set<AnswerIfc> answerSet = new HashSet<AnswerIfc>();
	          
	          // loop through all variables and formulas to create all answers for the ItemText object
	          for (CalculatedQuestionAnswerIfc curVarFormula : list) {
	              String match = curVarFormula.getMatch();
	              Long curSequence = curVarFormula.getSequence();
	              boolean isCorrect = curSequence.equals(sequence);
	              String choiceLabel = curVarFormula.getName();
	              Answer answer = new Answer(choiceText, match, curSequence, choiceLabel,
	                      isCorrect, grade, score, partialCredit, discount);
	              answerSet.add(answer);
	          }
	          choiceText.setAnswerSet(answerSet);
	          textSet.add(choiceText);          
	      }
	            
	      return textSet;
	  }
	  
	  /**
	   * prepareTextForImageMapQestion 
	   * @param item
	   * @param bean
	   * @param itemauthor
	   * @return
	   */
	private Set<ItemText> prepareTextForImageMapQuestion(ItemFacade item, ItemBean bean, ItemAuthorBean itemauthor) {
		List<ImageMapItemBean>imageMapItemBeanList = bean.getImageMapItemBeanList();
		HashSet<ItemText> textSet = new HashSet<ItemText>();

		for(ImageMapItemBean choicebean : imageMapItemBeanList)
		{
			ItemText choicetext = new ItemText();
			choicetext.setItem(item.getData()); // all set to the same
			choicetext.setSequence(choicebean.getSequence());
			choicetext.setText(stripPtags(choicebean.getChoice()));

			HashSet<Answer> answerSet = new HashSet<Answer>();

			// correct answer
			Answer  answer = new Answer(choicetext, 
										stripPtags(choicebean.getMatch()), 
										choicebean.getSequence(), 
										AnswerBean.getChoiceLabels(choicebean.getSequence().intValue() - 1)[choicebean.getSequence().intValue() - 1], 
										Boolean.TRUE, 
										null, 
										Double.valueOf(bean.getItemScore()), 
										Double.valueOf(0d), 
										Double.valueOf(bean.getItemDiscount()));
			HashSet<AnswerFeedback> answerFeedbackSet = new HashSet<AnswerFeedback>();
			answerFeedbackSet.add(new AnswerFeedback(answer, AnswerFeedbackIfc.CORRECT_FEEDBACK, stripPtags(choicebean.getCorrImageMapFeedback())));
			answerFeedbackSet.add(new AnswerFeedback(answer, AnswerFeedbackIfc.INCORRECT_FEEDBACK, stripPtags(choicebean.getIncorrImageMapFeedback())));
			answer.setAnswerFeedbackSet(answerFeedbackSet);
			answerSet.add(answer);
			choicetext.setAnswerSet((HashSet)answerSet);
			textSet.add(choicetext);
		}
		return textSet;
	}
	  
  private Set preparePublishedText(ItemFacade item, ItemBean bean, ItemService delegate) throws FinFormatException{

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
	  else if (item.getTypeId().equals(TypeFacade.EXTENDED_MATCHING_ITEMS)) {
		  item.setAnswerOptionsSimpleOrRich(Integer.valueOf(bean.getAnswerOptionsSimpleOrRich()));
		  item.setAnswerOptionsRichCount(Integer.valueOf(bean.getAnswerOptionsRichCount()));
		  preparePublishedTextForEMI(item, bean, delegate);		  
	  }
	  else if (item.getTypeId().equals(TypeFacade.CALCULATED_QUESTION)) {
	      preparePublishedTextForCalculatedQueston(item, bean, delegate);
	  }
	  else if (item.getTypeId().equals(TypeFacade.IMAGEMAP_QUESTION)) {
		  preparePublishedTextForImageMapQuestion(item, bean, delegate);
	  }
	  else if(item.getTypeId().equals(TypeFacade.MATRIX_CHOICES_SURVEY)) {
		  preparePublishedTextForMatrixSurvey(item,bean,delegate);
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
			  answer.setScore(Double.valueOf(bean.getItemScore()));
			  answer.setDiscount(Double.valueOf(bean.getItemDiscount()));
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
			  answer.setScore(Double.valueOf(bean.getItemScore()));
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
					null, null, null, Double.valueOf(bean.getItemScore()), null, Double.valueOf(bean.getItemDiscount()));
			  answerSet.add(answer);
		  }
		  text.setAnswerSet(answerSet);
		  textSet.add(text);
	  }
  }
  
  private void preparePublishedTextForFIBFIN(ItemFacade item, ItemBean bean, ItemService delegate, boolean isFIB) throws FinFormatException{
		Set answerSet = null;
		Set textSet = item.getItemTextSet();
		ItemTextIfc text = null;
		String entiretext = bean.getItemText();
		String processedText [] = processFIBFINText(entiretext);
		String updatedText = processedText[0];
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
				answer.setScore(Double.valueOf(bean.getItemScore()));
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
							Double.valueOf(bean.getItemScore()), null, Double.valueOf(bean.getItemDiscount()));
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
			List newAnswerList = bean.getMultipleChoiceAnswers();
			Map newAnswerMap = new HashMap();
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
				answer.setDiscount(Double.valueOf(bean.getItemDiscount()));
				i = answer.getSequence().intValue();
				if (i <= newAnswersSize) {				
					answer.setScore(Double.valueOf(bean.getItemScore()));
					answerBean = (AnswerBean) newAnswerMap.get(Long.valueOf(String.valueOf(i)));
					String oneAnswer = stripPtags(answerBean.getText());
					answer.setPartialCredit(Double.valueOf(answerBean.getPartialCredit()));
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
							Double.valueOf(bean.getItemScore()), Double.valueOf(100d), Double.valueOf(bean.getItemDiscount()));
					}
					else {
						answer = new PublishedAnswer(text, oneAnswer,
								Long.valueOf(j), oneLabel, Boolean.FALSE, null,
								Double.valueOf(bean.getItemScore()), Double.valueOf(answerBean.getPartialCredit()), Double.valueOf(bean.getItemDiscount()));
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
  
  private void preparePublishedTextForCalculatedQueston(ItemFacade item, ItemBean bean, ItemService delegate) {
      Set<ItemTextIfc> itemTextSet = item.getItemTextSet();            
      CalculatedQuestionBean calcBean = bean.getCalculatedQuestion();      
      double score = Double.valueOf(bean.getItemScore());
      double partialCredit = 0d;
      double discount = Double.valueOf(bean.getItemDiscount());
      String grade = null;            
      
      // Variables and formulas are very similar, and both have entries in the 
      // sam_itemtext_t table.  Because of the way the data is structured, every 
      // answer stored in sam_answer_t is a possible match to every ItemText 
      // stored in sam_itemtext_t.   If there is one variable and one formula, 
      // there are 2 entries in sam_itemtext_t and 4 entries in sam_answer_t.  
      // 2 variables and 2 formulas has 4 entries in sam_itemtext_t and 16 entries 
      // in sam_answer_t.  This is required for the current design (which makes 
      // more sense for other question types; we're just trying to work within 
      // that structure.  We loop through each formula and variable to create 
      // an entry in sam_itemtext_t (ItemText choiceText).  Then for each 
      // choicetext, we loop through all variables and formulas to create the 
      // answer objects.
      List<CalculatedQuestionAnswerIfc> list = new ArrayList<CalculatedQuestionAnswerIfc>();
      list.addAll(calcBean.getFormulas().values());
      list.addAll(calcBean.getVariables().values());
      
      // loop through all variables and formulas to create ItemText objects
      for (CalculatedQuestionAnswerIfc varFormula : list) {
          ItemTextIfc choiceText = null;
          for (ItemTextIfc itemText : itemTextSet) {
              if (itemText.getSequence().equals(varFormula.getSequence())) {
                  choiceText = itemText;
              }
          }
          if (choiceText == null) {
              choiceText = new PublishedItemText();
              choiceText.setItem(item.getData());
              choiceText.setSequence(varFormula.getSequence());
              itemTextSet.add(choiceText);
          }
          choiceText.setText(varFormula.getName());
          Long sequence = choiceText.getSequence();
          
          Set<AnswerIfc> answerSet = choiceText.getAnswerSet();
          if (answerSet == null) {
              answerSet = new HashSet<AnswerIfc>();
              choiceText.setAnswerSet(answerSet);
          }
          
          // loop through all variables and formulas to create all answers for the ItemText object
          for (CalculatedQuestionAnswerIfc curVarFormula : list) {
              String match = curVarFormula.getMatch();
              Long curSequence = curVarFormula.getSequence();
              boolean isCorrect = curSequence.equals(sequence);
              String choiceLabel = AnswerBean.getChoiceLabels()[curSequence.intValue()];
              boolean foundAnswer = false;
              for (AnswerIfc curAnswer : answerSet) {
                  if (curAnswer.getSequence().equals(sequence)) {
                      curAnswer.setText(varFormula.getMatch());
                      foundAnswer = true;
                      break;
                  }
              }
              if (!foundAnswer) {
                  AnswerIfc answer = new PublishedAnswer(choiceText, match, curSequence, choiceLabel,
                          isCorrect, grade, score, partialCredit, discount);
                  answerSet.add(answer);
              }
          }
      }
      
      // If we're saving fewer variables/formulas than are currently in  the 
      // itemtext/answer list, we need to delete the extra ones.
      // ASSUMPTION - that sequences in sam_itemtext_t and sam_answer_t have no gaps.  If
      // there are 3 entries in sam_itemtext_t, they are numbered 1, 2, and 3 
      // (not 1, 2, 4 for example).  This assumption should be verified
      if (list.size() < itemTextSet.size()) {
          Set<ItemTextIfc> toBeRemovedTextSet = new HashSet<ItemTextIfc>();
          Set<AnswerIfc> toBeRemovedAnswerSet = new HashSet<AnswerIfc>();
          for (ItemTextIfc curItemText : itemTextSet) {
              Set<AnswerIfc> answerSet = curItemText.getAnswerSet();
              for (AnswerIfc curAnswer : answerSet) {
                  if (curAnswer.getSequence() > list.size()) {
                      toBeRemovedAnswerSet.add(curAnswer);
                  }
              }
              answerSet.removeAll(toBeRemovedAnswerSet);
              delegate.deleteSet(toBeRemovedAnswerSet);
              if (curItemText.getSequence() > list.size()) {
                  toBeRemovedTextSet.add(curItemText);
              }
          }
          itemTextSet.removeAll(toBeRemovedTextSet);
          delegate.deleteSet(toBeRemovedTextSet);          
      }
  }

  private void preparePublishedTextForImageMapQuestion(ItemFacade item, ItemBean bean, ItemService delegate) {
		Set textSet = item.getItemTextSet();
		Iterator textIter = textSet.iterator();
		Map itemTextMap = new HashMap();
		while (textIter.hasNext()) {
			ItemTextIfc itemText = (ItemTextIfc) textIter.next();
			itemTextMap.put(itemText.getSequence(), itemText);
		}
		
		List<ImageMapItemBean>imageMapItemBeanList = bean.getImageMapItemBeanList();
		
		Set answerSet = null;
		ItemTextIfc choicetext = null;
		AnswerIfc answer = null;
		Long choiceSequence = null;
		Long matchSequence = null;

		for(ImageMapItemBean choicebean : imageMapItemBeanList)
		{
			choiceSequence = choicebean.getSequence();
			if (itemTextMap.get(choiceSequence) == null) {
				// new - add it in
				choicetext = new PublishedItemText();
				choicetext.setItem(item.getData()); // all set to the same
				choicetext.setSequence(choicebean.getSequence());
				choicetext.setText(stripPtags(choicebean.getChoice()));
			} else {
				choicetext = (ItemTextIfc) itemTextMap.get(choiceSequence);
				choicetext.setText(choicebean.getChoice());
			}
			Map answerMap = new HashMap();
			answerSet = choicetext.getAnswerSet();
			if (answerSet != null) {
				Iterator answerIter = answerSet.iterator();
				while (answerIter.hasNext()) {
					answer = (AnswerIfc) answerIter.next();
					answerMap.put(answer.getSequence(), answer);
				}
			}
			else {
				answerSet = new HashSet();
				choicetext.setAnswerSet(answerSet);
				textSet.add(choicetext);
			}
			
			matchSequence = choicebean.getSequence();
			if (answerMap.get(matchSequence) == null) {
				// new - add it in

				// correct answer
				answer = new PublishedAnswer(choicetext, 
											stripPtags(choicebean.getMatch()), 
											choicebean.getSequence(), 
											AnswerBean.getChoiceLabels()[choicebean.getSequence().intValue() - 1], 
											Boolean.TRUE, 
											null, 
											Double.valueOf(bean.getItemScore()), 
											Double.valueOf(0d), 
											Double.valueOf(bean.getItemDiscount()));
				
				Set<AnswerFeedbackIfc> answerFeedbackSet = new HashSet<AnswerFeedbackIfc>();
				answerFeedbackSet.add(new PublishedAnswerFeedback(answer, AnswerFeedbackIfc.CORRECT_FEEDBACK, stripPtags(choicebean.getCorrImageMapFeedback())));
				answerFeedbackSet.add(new PublishedAnswerFeedback(answer, AnswerFeedbackIfc.INCORRECT_FEEDBACK, stripPtags(choicebean.getIncorrImageMapFeedback())));
				answer.setAnswerFeedbackSet(answerFeedbackSet);
				answerSet.add(answer);
				
			} else {
				answer = (AnswerIfc) answerMap.get(matchSequence);
				answer.setScore(Double.valueOf(bean.getItemScore()));
				String oneAnswer = stripPtags(choicebean.getMatch());
				String oneLabel = AnswerBean.getChoiceLabels()[matchSequence
					.intValue() - 1];
				log.debug("oneAnswer = " + oneAnswer);
				log.debug("oneLabel = " + oneLabel);
				answer.setText(oneAnswer);
				answer.setLabel(oneLabel);
				// for new answers above, the default is true, not clear if we need to do something fancier here.
				answer.setIsCorrect(Boolean.TRUE);
				Set answerFeedbackSet = answer.getAnswerFeedbackSet();
				Iterator answerFeedbackIter = answerFeedbackSet.iterator();
				String feedback = "";
				while (answerFeedbackIter.hasNext()) {
					AnswerFeedbackIfc answerFeedback = (AnswerFeedbackIfc) answerFeedbackIter
						.next();
					if (answerFeedback.getTypeId().equals(AnswerFeedbackIfc.CORRECT_FEEDBACK)) {
						answerFeedback.setText(stripPtags(choicebean.getCorrImageMapFeedback()));
					}
					else if (answerFeedback.getTypeId().equals(AnswerFeedbackIfc.INCORRECT_FEEDBACK)) {
						answerFeedback.setText(stripPtags(choicebean.getIncorrImageMapFeedback()));
					}
				}
			}
		}
		
		int oldSize = textSet.size();
		int newSize = imageMapItemBeanList.size();
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
  
  private void preparePublishedTextForMatching(ItemFacade item,
			ItemBean bean, ItemService delegate) {
		Set textSet = item.getItemTextSet();
		Iterator textIter = textSet.iterator();
		Map itemTextMap = new HashMap();
		while (textIter.hasNext()) {
			ItemTextIfc itemText = (ItemTextIfc) textIter.next();
			itemTextMap.put(itemText.getSequence(), itemText);
		}

		// looping through matchItemBean
		List matchItemBeanList = bean.getMatchItemBeanList();
		Iterator choiceIter = matchItemBeanList.iterator();
		
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
			Map answerMap = new HashMap();
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
								Boolean.TRUE, null, new Double(bean.getItemScore()), null, Double.valueOf(bean.getItemDiscount()));
					} else {
						answer = new PublishedAnswer(itemText,
								stripPtags(matchBean.getMatch()), matchBean
										.getSequence(), AnswerBean
										.getChoiceLabels()[matchBean
										.getSequence().intValue() - 1],
								Boolean.FALSE, null, new Double(bean.getItemScore()), null, Double.valueOf(bean.getItemDiscount()));
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
					answer.setScore(Double.valueOf(bean.getItemScore()));
					String oneAnswer = stripPtags(matchBean.getMatch());
					String oneLabel = AnswerBean.getChoiceLabels()[matchSequence
							.intValue() - 1];
					log.debug("oneAnswer = " + oneAnswer);
					log.debug("oneLabel = " + oneLabel);
					answer.setText(oneAnswer);
					answer.setLabel(oneLabel);
					if (choiceSequence.longValue() == matchSequence.longValue()) {
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
  
  /**
   * Prepare Text for Extended Matching Item Questions
   * @param item
   * @param bean
   * @param itemauthor
   * @return
   */
  private void preparePublishedTextForEMI(ItemFacade item,
			ItemBean bean, ItemService delegate) {
	  
	  	// ///////////////////////////////////////////////////////////
		//
		// 1. save Theme and Lead-In Text and Answer Options
		//  
		// ///////////////////////////////////////////////////////////
        ItemTextIfc textTheme = item.getItemTextBySequence(ItemTextIfc.EMI_THEME_TEXT_SEQUENCE);
		textTheme.setText(bean.getItemText());
		
		ItemTextIfc textAnswerOptions = item.getItemTextBySequence(ItemTextIfc.EMI_ANSWER_OPTIONS_SEQUENCE);
		textAnswerOptions.setText(bean.getEmiAnswerOptionsRich());

		ItemTextIfc textLeadIn = item.getItemTextBySequence(ItemTextIfc.EMI_LEAD_IN_TEXT_SEQUENCE);
		textLeadIn.setText(bean.getLeadInStatement());
		
		// ///////////////////////////////////////////////////////////
		//
		// 2. save Answer Options - emiAnswerOptions
		// with ItemText  (seq=ItemTextIfc.EMI_ANSWER_OPTIONS_SEQUENCE).
		// These will be used to construct the actual answers.
        //
        // ///////////////////////////////////////////////////////////
        Set<AnswerIfc> answerOptions = textAnswerOptions.getAnswerSet();
        Set<AnswerIfc> deleteAnswerOptions = new HashSet<AnswerIfc>(answerOptions);
        answerOptions.clear();
        outer: for(AnswerBean answerBean: bean.getEmiAnswerOptionsClean()){
            for(AnswerIfc currentAnswerOption: deleteAnswerOptions){
                if(currentAnswerOption.getSequence().equals(answerBean.getSequence())){
                    //update the existing answer
                    currentAnswerOption.setText(stripPtags(answerBean.getText()));
                    currentAnswerOption.setLabel(answerBean.getLabel());
                    currentAnswerOption.setIsCorrect(Boolean.FALSE);
                    answerOptions.add(currentAnswerOption);
                    continue outer;
                }
            }
            //This is a new answer option, so add it
            //later (a little below this) we will add all the answer options to the items too
            AnswerIfc answerOption = new PublishedAnswer(textAnswerOptions,
					stripPtags(answerBean.getText()), answerBean
					.getSequence(), answerBean.getLabel(),
					Boolean.FALSE, null, null, null, null, null);
			answerOptions.add(answerOption);
		}
        //The deleted answer options that will be removed
        deleteAnswerOptions.removeAll(answerOptions);
        delegate.deleteSet(deleteAnswerOptions);
		
		// ///////////////////////////////////////////////////////////
		//
		// 3. Prepare and save actual answers from answer components 
		// (emiAnswerOptions and emiQuestionAnswerCombinations)
		// ///////////////////////////////////////////////////////////
		List<AnswerBean> emiQuestionAnswerCombinations = bean.getEmiQuestionAnswerCombinationsClean();
        Double itemScore = 0.0;
        Set<ItemTextIfc> deleteItemText = new HashSet<ItemTextIfc>(item.getItemTextSet());
        item.getItemTextSet().clear();
        //Put the theme, options and lead in back
        for(ItemTextIfc currentItemText : deleteItemText) {
          if (currentItemText.getSequence() < 1) {
              item.getItemTextSet().add(currentItemText);
          }
        }
        deleteItemText.removeAll(item.getItemTextSet());
        for(AnswerBean answerBean: emiQuestionAnswerCombinations){
            ItemTextIfc itemText = null;
			for(ItemTextIfc currentItemText: deleteItemText){
                if(currentItemText.getSequence().equals(answerBean.getSequence())){
                    itemText = currentItemText;
                    break;
                }
            }
            if(itemText == null){
                itemText = new PublishedItemText();
                itemText.setItem(item.getData());
                itemText.setSequence(answerBean.getSequence());
            }
			itemText.setText(answerBean.getText());
			
			int requiredOptions = (Integer.valueOf(answerBean.getRequiredOptionsCount())).intValue();
			if (requiredOptions == 0) {
				requiredOptions = answerBean.correctOptionsCount();
			}
			itemText.setRequiredOptionsCount(requiredOptions);
            itemScore += answerBean.getScore();
			
            Set<AnswerIfc> deleteItemAnswerSet = new HashSet<AnswerIfc>(itemText.getAnswerSet());
            itemText.getAnswerSet().clear();
			if (Integer.valueOf(bean.getAnswerOptionsSimpleOrRich()).equals(ItemDataIfc.ANSWER_OPTIONS_SIMPLE) ) {
                outer2: for(AnswerIfc currentAnswerOption: answerOptions){
				    String correctLabels = answerBean.getCorrectOptionLabels();
                    int correctRequiredCount = correctLabels.length()<requiredOptions?correctLabels.length():requiredOptions;
                    boolean isCorrect = answerBean.getCorrectOptionLabels().contains(currentAnswerOption.getLabel());

                    // item option score
                    Double score = answerBean.getScore() / correctRequiredCount;
                    for(AnswerIfc currentItemAnswer: deleteItemAnswerSet){
                        //if the answer option was removed before then the item
                        //answer will also be removed (not added back here)
                        if(currentItemAnswer.getSequence().equals(currentAnswerOption.getSequence())){
                            currentItemAnswer.setText(currentAnswerOption.getText());
                            currentItemAnswer.setLabel(currentAnswerOption.getLabel());
                            currentItemAnswer.setIsCorrect(isCorrect);
                            currentItemAnswer.setGrade(answerBean.getScoreUserSet() ? "user" : "auto");
                            currentItemAnswer.setScore(isCorrect ? score : 0.0);
                            currentItemAnswer.setPartialCredit(null);
                            currentItemAnswer.setDiscount(isCorrect ? 0.0 : -score / 2);
                            currentItemAnswer.setAnswerFeedbackSet(null);
                            itemText.getAnswerSet().add(currentItemAnswer);
                            continue outer2;
                        }
                    }
                    //if we get here then there is a new answer option and we need to 
                    //add it to the itemtext
                    itemText.getAnswerSet().add(new PublishedAnswer(itemText, currentAnswerOption.getText(),
                                            currentAnswerOption.getSequence(), currentAnswerOption.getLabel(),
                                            isCorrect, answerBean.getScoreUserSet() ? "user" : "auto",
                                            isCorrect ? score : 0.0, null, isCorrect ? 0.0 : -score / 2));
				}
			}
			else { // ANSWER_OPTION_RICH
				int answerOptionsCount = Integer.valueOf(bean.getAnswerOptionsRichCount());
                outer3: for (int i=0; i<answerOptionsCount; i++) {
					String label = ItemDataIfc.ANSWER_OPTION_LABELS.substring(i, i+1);
					String correctLabels = answerBean.getCorrectOptionLabels();
                    int correctRequiredCount = correctLabels.length()<requiredOptions?correctLabels.length():requiredOptions;
                    boolean isCorrect = answerBean.getCorrectOptionLabels().contains(label);

                    // item option score
                    Double score = answerBean.getScore() / correctRequiredCount;
                    for(AnswerIfc currentItemAnswer: deleteItemAnswerSet){
                        //if the answer option was removed before then the item
                        //answer will also be removed (not added back here)
                        if(currentItemAnswer.getSequence() == i){
                            currentItemAnswer.setText(label);
                            currentItemAnswer.setLabel(label);
                            currentItemAnswer.setIsCorrect(isCorrect);
                            currentItemAnswer.setGrade(answerBean.getScoreUserSet() ? "user" : "auto");
                            currentItemAnswer.setScore(isCorrect ? score : 0.0);
                            currentItemAnswer.setPartialCredit(null);
                            currentItemAnswer.setDiscount(isCorrect ? 0.0 : -score / 2);
                            currentItemAnswer.setAnswerFeedbackSet(null);
                            itemText.getAnswerSet().add(currentItemAnswer);
                            continue outer3;
                        }
                    }
                    //if we get here then there is a new answer option and we need to 
                    //add it to the itemtext
                    itemText.getAnswerSet().add(new PublishedAnswer(itemText, label,
                                            Long.valueOf(i), label,
                                            isCorrect, answerBean.getScoreUserSet() ? "user" : "auto",
                                            isCorrect ? score : 0.0, null, isCorrect ? 0.0 : -score / 2));
				}
			}
            deleteItemAnswerSet.removeAll(itemText.getAnswerSet());
            delegate.deleteSet(deleteItemAnswerSet);
			item.getItemTextSet().add(itemText);
		}
        deleteItemText.removeAll(item.getItemTextSet());
        delegate.deleteSet(deleteItemText);
        item.setScore(itemScore);
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
					ItemMetaDataIfc.KEYWORD, TextFormat.convertPlaintextToFormattedTextNoHighUnicode(bean.getKeyword())));
		}
		if (bean.getRubric() != null) {
			set.add(new ItemMetaData(item.getData(),
					ItemMetaDataIfc.RUBRIC, TextFormat.convertPlaintextToFormattedTextNoHighUnicode(bean.getRubric())));
		}
		if (bean.getObjective() != null) {
			set.add(new ItemMetaData(item.getData(),
					ItemMetaDataIfc.OBJECTIVE, TextFormat.convertPlaintextToFormattedTextNoHighUnicode(bean.getObjective())));
		}
		
		// Randomize property got left out, added in metadata
		if (bean.getRandomized() != null) {
		set.add(new ItemMetaData(item.getData(),
					ItemMetaDataIfc.RANDOMIZE, bean.getRandomized()));
		}
		// Required all ok for Image MAP property got left out, added in metadata
		if (bean.getRequireAllOk() != null) {
		set.add(new ItemMetaData(item.getData(),
					ItemMetaDataIfc.REQUIRE_ALL_OK, bean.getRequireAllOk()));
		}

		// The imageMap Image URL added in Metadata
		if (bean.getImageMapSrc() != null) {
		set.add(new ItemMetaData(item.getData(),
					ItemMetaDataIfc.IMAGE_MAP_SRC, bean.getImageMapSrc()));
		}
		// MSMC property got left out, added in metadata
		if (bean.getMcmsPartialCredit() != null) {
		set.add(new ItemMetaData(item.getData(),
					ItemMetaDataIfc.MCMS_PARTIAL_CREDIT, bean.getMcmsPartialCredit()));
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

	    // save settings for ignore spaces for FIB. Default=false
		set.add(new ItemMetaData(item.getData(),
				ItemMetaDataIfc.IGNORE_SPACES_FOR_FIB, Boolean
						.toString(bean.getIgnoreSpacesForFib())));

		// sam-939
		set.add(new ItemMetaData(item.getData(),
				ItemMetaDataIfc.FORCE_RANKING, Boolean
				.toString(bean.getForceRanking())));
		set.add(new ItemMetaData(item.getData(),
				ItemMetaDataIfc.ADD_COMMENT_MATRIX, Boolean
				.toString(bean.getAddComment())));
		set.add(new ItemMetaData(item.getData(),
				ItemMetaDataIfc.MX_SURVEY_QUESTION_COMMENTFIELD, bean.getCommentField()));

		set.add(new ItemMetaData(item.getData(),
				ItemMetaDataIfc.MX_SURVEY_RELATIVE_WIDTH, Integer.toString( bean.getRelativeWidth())));

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
			  itemMetaData.setEntry(TextFormat.convertPlaintextToFormattedTextNoHighUnicode(bean.getKeyword()));
		  }
		  else if (itemMetaData.getLabel().equals(ItemMetaDataIfc.RUBRIC)){
			  itemMetaData.setEntry(TextFormat.convertPlaintextToFormattedTextNoHighUnicode(bean.getKeyword()));
		  }
		  else if (itemMetaData.getLabel().equals(ItemMetaDataIfc.OBJECTIVE)){
			  itemMetaData.setEntry(TextFormat.convertPlaintextToFormattedTextNoHighUnicode(bean.getObjective()));
		  }
		  else if (itemMetaData.getLabel().equals(ItemMetaDataIfc.RANDOMIZE)){
			  itemMetaData.setEntry(bean.getRandomized());
		  }
		  else if (itemMetaData.getLabel().equals(ItemMetaDataIfc.REQUIRE_ALL_OK)){
			  itemMetaData.setEntry(bean.getRequireAllOk());
		  }
		  else if (itemMetaData.getLabel().equals(ItemMetaDataIfc.IMAGE_MAP_SRC)){
			  itemMetaData.setEntry(bean.getImageMapSrc());
		  }
		  else if (itemMetaData.getLabel().equals(ItemMetaDataIfc.PREDEFINED_SCALE)){
			  itemMetaData.setEntry(bean.getScaleName());
		  }
		  else if (itemMetaData.getLabel().equals(ItemMetaDataIfc.CASE_SENSITIVE_FOR_FIB)){
			  itemMetaData.setEntry(Boolean.toString(bean.getCaseSensitiveForFib()));
		  }
		  else if (itemMetaData.getLabel().equals(ItemMetaDataIfc.KEYWORD)){
			  itemMetaData.setEntry(bean.getKeyword());
		  }else if(itemMetaData.getLabel().equals(ItemMetaDataIfc.MCMS_PARTIAL_CREDIT)){
			  itemMetaData.setEntry(bean.getMcmsPartialCredit());
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

		  else if (itemMetaData.getLabel().equals(ItemMetaDataIfc.IGNORE_SPACES_FOR_FIB)){
			  itemMetaData.setEntry(Boolean.toString(bean.getIgnoreSpacesForFib()));
		  }
		  
		  else if (itemMetaData.getLabel().equals(ItemMetaDataIfc.PARTID)){
			  itemMetaData.setEntry(bean.getSelectedSection());
		  }
		  else if (itemMetaData.getLabel().equals(ItemMetaDataIfc.POOLID)){
			  itemMetaData.setEntry(bean.getSelectedPool());
		  }
		  else if (itemMetaData.getLabel().equals(ItemMetaDataIfc.FORCE_RANKING)){
			  itemMetaData.setEntry(Boolean.toString(bean.getForceRanking()));
		  }
		  else if (itemMetaData.getLabel().equals(ItemMetaDataIfc.MX_SURVEY_RELATIVE_WIDTH)){
			  itemMetaData.setEntry(Integer.toString(bean.getRelativeWidth()));
		  }
		  else if (itemMetaData.getLabel().equals(ItemMetaDataIfc.ADD_COMMENT_MATRIX)){
			  itemMetaData.setEntry(Boolean.toString(bean.getAddComment()));
		  }
		  else if (itemMetaData.getLabel().equals(ItemMetaDataIfc.MX_SURVEY_QUESTION_COMMENTFIELD)){
			  itemMetaData.setEntry(bean.getCommentField());
		  }
	  }
	  return itemMetaDataSet;
	}
  
  private void preparePublishedTextForMatrixSurvey(ItemFacade item, ItemBean bean, ItemService delegate){
	  item.getData().setInstruction(bean.getItemText());
	  Set textSet = item.getItemTextSet();
	  Iterator textIter = textSet.iterator();
	  Map itemTextMap = new HashMap();
	  while (textIter.hasNext()) {
			ItemTextIfc itemText = (ItemTextIfc) textIter.next();
			itemTextMap.put(itemText.getSequence(), itemText);
	  }
	  String[] rowChoices = returnMatrixChoices(bean,"row");
	  String[] columnChoices = returnMatrixChoices(bean,"column");
	  ItemTextIfc publishedItemText = null;
	  Long rowChoiceSequence = null, columnChoiceSequence = null;
	  Set answerSet = null;
	  AnswerIfc publishedAnswer = null;
	  for(int i = 0; i<rowChoices.length;i++)
	  {
		  rowChoiceSequence = Long.valueOf(i+1);
		  if(!itemTextMap.containsKey(rowChoiceSequence)){
			  publishedItemText = new PublishedItemText();
			  publishedItemText.setItem(item.getData());
			  publishedItemText.setSequence(rowChoiceSequence);
			  publishedItemText.setText(rowChoices[i]);
		  }	else {
			    publishedItemText = (ItemTextIfc) itemTextMap.get(rowChoiceSequence);
			    publishedItemText.setText(rowChoices[i]);
		  }
		  Map answerMap = new HashMap();
		  answerSet = publishedItemText.getAnswerSet();
		  if (answerSet != null) {
			    Iterator answerIter = answerSet.iterator();
				while (answerIter.hasNext()) {
					publishedAnswer = (AnswerIfc) answerIter.next();
					answerMap.put(publishedAnswer.getSequence(), publishedAnswer);
				}
		  } else {
				answerSet = new HashSet();
				publishedItemText.setAnswerSet(answerSet);
			    textSet.add(publishedItemText);
		  }
		  for(int j = 0; j<columnChoices.length;j++){
			  columnChoiceSequence = Long.valueOf(j+1);
			  if (!answerMap.containsKey(columnChoiceSequence)) {
				publishedAnswer = new PublishedAnswer(publishedItemText,columnChoices[j],columnChoiceSequence,null, null, null, Double.valueOf(bean.getItemScore()), Double.valueOf(0d), Double.valueOf(bean.getItemDiscount()));
			  	answerSet.add(publishedAnswer);
			  }	else {
				  publishedAnswer = (AnswerIfc) answerMap.get(columnChoiceSequence);
				  publishedAnswer.setText(columnChoices[j]);
			  }	
		  }
		  int oldAnswerSetSize = publishedItemText.getAnswerSet().size();
		  int newAnswerSetSize = columnChoices.length;
		  if(oldAnswerSetSize > newAnswerSetSize){
			  HashSet toBeRemovedAnswerSet = new HashSet();
			  
			  for (int j = newAnswerSetSize + 1; j < oldAnswerSetSize + 1; j++) {
				  publishedAnswer = (AnswerIfc) answerMap.get(Long.valueOf(j));	
				  toBeRemovedAnswerSet.add(publishedAnswer);
			  }
			  answerSet.removeAll(toBeRemovedAnswerSet);
			  delegate.deleteSet(toBeRemovedAnswerSet);
		  }
	  }
	  int oldTextSetSize = textSet.size();
	  int newTextSetSize = rowChoices.length;
	  if (oldTextSetSize > newTextSetSize) {
			HashSet toBeRemovedTextSet = new HashSet();
			for (int i = newTextSetSize + 1; i < oldTextSetSize + 1; i++) {
				ItemTextIfc text = (ItemTextIfc) itemTextMap.get(Long.valueOf(i));
				toBeRemovedTextSet.add(text);
			}
			textSet.removeAll(toBeRemovedTextSet);
			delegate.deleteSet(toBeRemovedTextSet);
	  }
  }

  private static List getFIBanswers(String entiretext) {
	  String fixedText = entiretext.replaceAll("&nbsp;", " "); // replace &nbsp to " " (instead of "") just want to reserve the original input
	  String[] tokens = fixedText.split("[\\}][^\\{]*[\\{]");
	  List list = new ArrayList();
	  if (tokens.length==1) {
		  String[] afteropen= tokens[0].split("\\{");
		  if (afteropen.length>1) {
			  //	 must have text in between {}
			  String[] lastpart = afteropen[1].split("\\}");
			  String answer = FormattedText.convertFormattedTextToPlaintext(lastpart[0].replaceAll("&lt;.*?&gt;", ""));
			  list.add(answer);
		  }
	  }
	  else {
		  for (int i = 0; i < tokens.length; i++) {
			  if (i == 0) {
				  String[] firstpart = tokens[i].split("\\{");
				  if (firstpart.length>1) {
					  String answer = FormattedText.convertFormattedTextToPlaintext(firstpart[1].replaceAll("&lt;.*?&gt;", ""));
					  list.add(answer);
				  }
			  }
			  else if (i == (tokens.length - 1)) {
				  String[] lastpart = tokens[i].split("\\}");
				  String answer = FormattedText.convertFormattedTextToPlaintext(lastpart[0].replaceAll("&lt;.*?&gt;", ""));
				  list.add(answer);
			  }
			  else {
				  String answer = FormattedText.convertFormattedTextToPlaintext(tokens[i].replaceAll("&lt;.*?&gt;", ""));
				  list.add(answer);
			  }
		  }
	  } // token.length>1

	  return list;

  }

  private static List getFINanswers(String entiretext) throws FinFormatException {
	  String fixedText = entiretext.replaceAll("&nbsp;", " "); // replace &nbsp to " " (instead of "") just want to reserve the original input
	  String[] tokens = fixedText.split("[\\}][^\\{]*[\\{]");
	  List list = new ArrayList();
	  if (tokens.length==1) {
		  String[] afteropen= tokens[0].split("\\{");
		  if (afteropen.length>1) {
			  // must have text in between {}
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

  /*
  private static boolean isValidFINAnswer(String answer){
	  String processedAnswer = "";
	  if (answer.indexOf("|") == -1) {
		  processedAnswer = answer.replaceAll(" ", "").replaceAll(",", ".");
		  // Test if it is a valid Double
		  try {
			  Double.parseDouble(processedAnswer); 
		  }
		  catch (NumberFormatException e) {
			  return false;
		  }
		  return true;
	  }

	  String[] tokens = answer.split("\\|");
	  if (tokens.length != 2) {
		  return false;
	  }
	  for (int i = 0; i < 2; i++) {
		  String tmpAnswer = tokens[i].replaceAll(" ", "").replaceAll(",", ".");
		  // Test if it is a valid Double
		  try {
			  Double.parseDouble(tmpAnswer);
		  }
		  catch (NumberFormatException e) {
			  return false;
		  }
	  }
	  return true;
  }
  */
  
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
      List corranswersList = ContextUtil.paramArrayValueLike(
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
     String processedText [] = processFIBFINText(entiretext);
     log.debug("processedText[1]=" + processedText[1]);
     Object[] fibanswers = getFIBanswers(processedText[1]).toArray();
      List blanklist = new  ArrayList();
      for (int i = 0; i < fibanswers.length; i++) {
    	log.debug("fibanswers[" + i + "]=" + fibanswers[i]);
        String oneanswer = (String) fibanswers[i];
        String[] oneblank = oneanswer.split("\\|");
        Set oneblankset = new HashSet();
        for (int j = 0; j < oneblank.length; j++) {
           oneblankset.add(oneblank[j].trim().toLowerCase());
        }
        blanklist.add(oneblankset);

      }
      // now check if there are at leastF 2 sets, and make sure they are identically, all should contain only lowercase strings. 
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

  private void updateItemTextAttachment(List newList, ItemTextIfc targetItemText, boolean pendingOrPool) {
	  final Map<Long, ItemTextAttachmentIfc> oldIds = targetItemText.getItemTextAttachmentMap();
	  if (!(oldIds.isEmpty())){
		  for (ItemTextAttachmentIfc itemTextAttachmentIfcOld : oldIds.values()){
		  }
	  }
	  if (newList != null && !(newList.isEmpty())) {
		  for (Object o : newList) {
			 ItemTextAttachmentIfc newAttachment = (ItemTextAttachmentIfc) o;
			 final Long newAttachmentId = newAttachment.getAttachmentId();
			 if (oldIds.containsKey(newAttachmentId)) {
			  oldIds.remove(newAttachmentId);
			 }
			 //We need to add all the items always because answers are
			  //completely deleted when saving an item.
			 targetItemText.addNewItemTextAttachment(newAttachment);
		  }
	  }
	  // any "oldIds" left over must be orphans. delete them.
	  for (Map.Entry<Long, ItemTextAttachmentIfc> e : oldIds.entrySet()) {
		  targetItemText.removeItemTextAttachment(e.getValue());
	  }
  }

	private void updateAttachments(List newList, ItemFacade targetItem) {
		final Map<Long, ItemAttachmentIfc> oldIds = targetItem.getItemAttachmentMap();
		if ( newList != null && !(newList.isEmpty()) ) {
			for (Object o : newList) {
				ItemAttachmentIfc newAttachment = (ItemAttachmentIfc) o;
				final Long newAttachmentId = newAttachment.getAttachmentId();
				if (oldIds.containsKey(newAttachmentId)) {
					// reiteration of existing attachment, no-op
					oldIds.remove(newAttachmentId);
				} else {
					targetItem.addItemAttachment(newAttachment);
				}
			}
		}
		// any "oldIds" left over must be orphans. delete them.
		for ( Map.Entry<Long, ItemAttachmentIfc> e : oldIds.entrySet() ) {
			targetItem.removeItemAttachment(e.getValue());
		}
	}

  private void updateItemFeedback(ItemFacade item, String feedbackTypeId, String feedbackText) {
	  Set itemFeedbackSet = item.getItemFeedbackSet();
	  if ((itemFeedbackSet == null || itemFeedbackSet.size() == 0)) {
		  item.addItemFeedback(feedbackTypeId, feedbackText);
	  }
	  else {
          boolean feedbackTypeExists = false;
		  Iterator iter = itemFeedbackSet.iterator();
		  while (iter.hasNext()) {
			  ItemFeedbackIfc itemFeedback = (ItemFeedbackIfc) iter.next();
			  if (itemFeedback.getTypeId().equals(feedbackTypeId)) {
                  feedbackTypeExists = true;
				  itemFeedback.setText(feedbackText);
				  break;
			  }
		  }
          //If the feedback type was not found in the set, add it so changes are not lost.
          if (!feedbackTypeExists) {
        	  item.addItemFeedback(feedbackTypeId, feedbackText);
          }
	  }
  }
  
  private String [] getSurveyChoices(String scalename) {
	  String[] choices = new String[2];
	  // label is null because we don't use labels in survey
	  if (ItemMetaDataIfc.SURVEY_YES.equals(scalename)) {
		  choices = new String[2];
		  choices[0] = "st_yes";
		  choices[1] = "st_no";
	  }

	  if (ItemMetaDataIfc.SURVEY_AGREE.equals(scalename)) {
		  choices = new String[2];
		  choices[0] = "st_disagree";
		  choices[1] = "st_agree";
	  }
	  if (ItemMetaDataIfc.SURVEY_UNDECIDED.equals(scalename)) {
		  choices = new String[3];
		  choices[0] = "st_disagree";
		  choices[1] = "st_undecided";
		  choices[2] = "st_agree";
	  }

	  if (ItemMetaDataIfc.SURVEY_AVERAGE.equals(scalename)) {
		  choices = new String[3];
		  choices[0] = "st_below_average";
		  choices[1] = "st_average";
		  choices[2] = "st_above_average";
	  }
	  if (ItemMetaDataIfc.SURVEY_STRONGLY_AGREE.equals(scalename)) {
		  choices = new String[5];
		  choices[0] = "st_strongly_disagree";
		  choices[1] = "st_disagree";
		  choices[2] = "st_undecided";
		  choices[3] = "st_agree";
		  choices[4] = "st_strongly_agree";
	  }

	  if (ItemMetaDataIfc.SURVEY_EXCELLENT.equals(scalename)) {
		  choices = new String[5];
		  choices[0] = "st_unacceptable";
		  choices[1] = "st_below_average";
		  choices[2] = "st_average";
		  choices[3] = "st_above_average";
		  choices[4] = "st_excellent";
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

  private String [] processFIBFINText(String entiretext) {
	  String[] processedText = new String[2];
	  Pattern pattern1 = Pattern.compile("[\\{][^\\}]*[\\}]");
	  Matcher matcher1 = pattern1.matcher(entiretext);
	  StringBuilder textStringBuilder1 = new StringBuilder(); 
	  String tmpString1 = null;
	  int index1 = 0;
	  while (matcher1.find()) {
		  String group = matcher1.group();
		  textStringBuilder1.append(entiretext.substring(index1, matcher1.start()));
		  tmpString1 = group.replaceAll("<.*?>", "");
		  textStringBuilder1.append(tmpString1);
		  index1 = matcher1.end();
	  }
	  textStringBuilder1.append(entiretext.substring(index1));
	  String modifiedText = textStringBuilder1.toString();

	  //String[] tmpString = modifiedText.split("(<([a-z]\\w*)\\b[^>]*>(.*?)</\\1\\s*>)|(<([a-z]\\w*)\\b[^>]*/>)");
	  Pattern pattern2 = Pattern.compile("(<([a-z]\\w*)\\b[^>]*>(.*?)</\\1\\s*>)|(<([a-z]\\w*)\\b[^>]*/>)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	  Matcher matcher2 = pattern2.matcher(modifiedText);
	  int index2 = 0;
	  StringBuilder textStringBuilder2 = new StringBuilder(); 
	  StringBuilder textStringBuilder3 = new StringBuilder();
	  String tmpString2 = null;
	  while (matcher2.find()) {
		  String group = matcher2.group();
		  log.debug("group" + group);
		  tmpString2 = modifiedText.substring(index2, matcher2.start());
		  log.debug("tmpString2" + tmpString2);
		  if (tmpString2 != null) {
			  textStringBuilder2.append(tmpString2.replaceAll("[\\{][^\\}]*[\\}]", "{}"));
			  textStringBuilder3.append(tmpString2);
			  log.debug("textStringBuilder2=" + textStringBuilder2);
			  log.debug("textStringBuilder3=" + textStringBuilder3);
		  }
		  textStringBuilder2.append(group);
		  index2 = matcher2.end();
		  log.debug("index2=" + index2);
	  }
	  tmpString2 = modifiedText.substring(index2);
	  if (tmpString2 != null) {
		  textStringBuilder2.append(tmpString2.replaceAll("[\\{][^\\}]*[\\}]", "{}"));
		  textStringBuilder3.append(tmpString2);
		  log.debug("textStringBuilder2=" + textStringBuilder2);
		  log.debug("textStringBuilder3=" + textStringBuilder3);
	  }
	  processedText[0] = textStringBuilder2.toString();
	  processedText[1] = textStringBuilder3.toString();

	  return processedText;
  }
}
