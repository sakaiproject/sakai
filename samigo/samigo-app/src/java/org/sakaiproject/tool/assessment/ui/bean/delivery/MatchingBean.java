/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
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



package org.sakaiproject.tool.assessment.ui.bean.delivery;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;

import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.services.GradingService;

/**
 * @author rgollub@stanford.edu
 * $Id$
 */
public class MatchingBean
{
	private final String NONE_OF_THE_ABOVE = "-1";

  private ItemContentsBean parent;
  private ItemTextIfc itemText;
  private ItemGradingData data;
  private String response;
  private List choices;
  private String text;
  private String feedback;
  private AnswerIfc answer;
  private Boolean isCorrect;
  private String itemSequence;

  public String getItemSequence() {
	return itemSequence;
}

public void setItemSequence(String itemSequence) {
	this.itemSequence = itemSequence;
}

public class AnswerLabelWithCorrectStatus {
	  private boolean isCorrect;
	  private String answerLabel;
	  public AnswerLabelWithCorrectStatus(boolean correct, String label) {
		  isCorrect = correct;
		  answerLabel = label;
	  }
	  public Boolean getIsCorrect() {
		  return isCorrect;
	  }
	  public String getAnswerLabel() {
		  return answerLabel;
	  }
  }

  public ItemContentsBean getItemContentsBean()
  {
    return parent;
  }

  public void setItemContentsBean(ItemContentsBean bean)
  {
    parent = bean;
  }

  public ItemTextIfc getItemText()
  {
    return itemText;
  }

  public void setItemText(ItemTextIfc newtext)
  {
    itemText = newtext;
  }

  public ItemGradingData getItemGradingData()
  {
    return data;
  }

  public void setItemGradingData(ItemGradingData newdata)
  {
    data = newdata;
  }

  public String getResponse()
  {
    return response;
  }

  public void setResponse(String newresp)
  {
	  
    if (parent.getItemData().getTypeId().equals(TypeIfc.EXTENDED_MATCHING_ITEMS)) {
    	this.setResponseEMI(newresp);
    	return;
    }
	  
    response = newresp;
    if (data == null)
    {
      data = new ItemGradingData();
      data.setPublishedItemId(parent.getItemData().getItemId());
      data.setPublishedItemTextId(itemText.getId());
      List<ItemGradingData> items = parent.getItemGradingDataArray();
      items.add(data);
      parent.setItemGradingDataArray(items);
    }
    
    // Fixed for SAK-5535
    // If we don't reset published answer id to null, the previous selected value will remain in the bean
    // That is, the "select" selection in dropdown will never be set
    if ("0".equals(newresp)) {
    	data.setPublishedAnswerId(null);
    }
    // used for matching questions that have distractors.  If the user chooses the answer
    // None of the Above, that answer value will be saved in the database.
    if (NONE_OF_THE_ABOVE.equals(newresp)) {
    	data.setPublishedAnswerId(Long.parseLong(NONE_OF_THE_ABOVE));
    }
    Iterator<AnswerIfc> iter = itemText.getAnswerSet().iterator();
    while (iter.hasNext())
    {
      AnswerIfc answer = iter.next();
      if (answer.getId().toString().equals(newresp))
      {
        data.setPublishedAnswerId(answer.getId());
        break;
      }
    }
  }

  public List getChoices()
  {
    return choices;
  }

  public void setChoices(List newch)
  {
    choices = newch;
  }

  public String getText()
  {
    return text;
  }

  public void setText(String newtext)
  {
    text = newtext;
  }

  public String getFeedback()
  {
    return feedback;
  }

  public void setFeedback(String newfb)
  {
    feedback = newfb;
  }


  public void setAnswer(AnswerIfc answer){
    this.answer = answer;
  }

  public AnswerIfc getAnswer(){
    return answer;
  }

  public void setIsCorrect(Boolean isCorrect){
    this.isCorrect = isCorrect;
  }
  
  public Boolean getIsCorrect() {
    if (this.getIsDistractor()) {
      if (this.response == null) {
        return false;
      }
      return Integer.parseInt(this.response) < 0;
    } else {
        return isCorrect;
    }
  }
  
  public boolean getIsDistractor() {
	  GradingService gs = new GradingService();
	  return gs.isDistractor(this.getItemText());
  }
  
  public void setResponseEMI(String newresp) {
	  	newresp = newresp.toUpperCase().trim();
		String label=null;
		String temp = "";
		
		// remove white space and delimiter characters
		for (int i=0; i<newresp.length(); i++) {
			label = newresp.substring(i, i+1);
			if (label.trim().equals("") || ItemDataIfc.ANSWER_OPTION_VALID_DELIMITERS.contains(label)) continue;
			temp += label;
		}
		
		response = temp;
		
		String processedResponses = "";
		List<ItemGradingData> itemGradingList = parent.getItemGradingDataArray();
		List<ItemGradingData> newItemGradingList = new ArrayList<ItemGradingData>();
		Iterator<ItemGradingData> itemGradingDataIter = itemGradingList.iterator();
		String answerLabel = null;
		ItemGradingData itemGradingData = null;

		// This step saves re-selected options and eliminates
		// previously selected options that were 
		// not selected again
		while (itemGradingDataIter.hasNext()) {
			itemGradingData = itemGradingDataIter.next();
			//Only add or eliminate itemGradings for this sub-question (ItemText)
			if (!itemGradingData.getPublishedItemTextId().equals(this.getItemText().getId())) {
				newItemGradingList.add(itemGradingData);
				continue;
			}
			// Could be null if there is a "fake" response created for linear organization, empty response
			if (itemGradingData.getPublishedAnswerId() != null) {
				Iterator<AnswerIfc> iter = itemText.getAnswerSet().iterator();
			    while (iter.hasNext()){
			      AnswerIfc answer = iter.next();
			      if (answer.getId().equals(itemGradingData.getPublishedAnswerId())){
			    	  answerLabel = answer.getLabel();
			    	  if (response.contains(answerLabel)) {
			    		  newItemGradingList.add(itemGradingData);
			    		  processedResponses += answerLabel;
			    	  }
			    	  break;
			      }
			    }
			}
		}

		// This step saves valid new responses
		for (int i = 0; i < response.length(); i++) {
			answerLabel = response.substring(i, i + 1);
			// If not a valid Answer Option label, bypass processing
			if (!ItemDataIfc.ANSWER_OPTION_LABELS.contains(answerLabel)) continue;
			// If this response is already processed bypass processing
			if (processedResponses.contains(answerLabel)) continue;
				
			processedResponses += answerLabel;
			itemGradingData = new ItemGradingData();
			itemGradingData.setPublishedItemId(parent.getItemData()
					.getItemId());
			Iterator<AnswerIfc> iter = getItemText().getAnswerSet().iterator();
			while (iter.hasNext()) {
				AnswerIfc selectedAnswer = iter.next();
				if (selectedAnswer.getLabel().equals(answerLabel)) {
					itemGradingData.setPublishedItemTextId(selectedAnswer
							.getItemText().getId());
					itemGradingData.setPublishedAnswerId(selectedAnswer.getId());
					break;
				}
			}
			newItemGradingList.add(itemGradingData);
		}
		parent.setItemGradingDataArray(newItemGradingList); 		
  }
  
  public List<AnswerLabelWithCorrectStatus> getEmiResponseAndCorrectStatusList() {
	  List<AnswerLabelWithCorrectStatus> responseList = new ArrayList<AnswerLabelWithCorrectStatus>();
	  String resp = getResponse();
	  if (resp == null || resp.trim().equals("")) return responseList;
	  for (int i=0; i<resp.length(); i++) {
		  String answerLabel = resp.substring(i, i+1);
		  boolean correct = itemText.getEmiCorrectOptionLabels().contains(answerLabel);
		  AnswerLabelWithCorrectStatus responseWithStatus = this.new AnswerLabelWithCorrectStatus(correct, answerLabel);
		  responseList.add(responseWithStatus);
	  }
	  return responseList;
  }
  
	public void validateEmiResponse(FacesContext context, 
          UIComponent toValidate,
          Object value) {

		String response = ((String) value).trim().toUpperCase();
		
		String processed = "";
		
		((UIInput)toValidate).setValid(true);

		// Assuming that the user can elect not to answer - i.e. a blank response is OK
		if (response.length() == 0) return;
		
		if (response.length() != 0) {
			for (int i=0; i<response.length(); i++) {
				String label = response.substring(i, i+1).trim();
				if (label.equals("") || ItemDataIfc.ANSWER_OPTION_VALID_DELIMITERS.contains(label)) continue;
				String q = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.DeliveryMessages","q");     
				if (!parent.getItemData().isValidEmiAnswerOptionLabel(label)) {
					((UIInput)toValidate).setValid(false);
					String please_select_from_available = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.DeliveryMessages","please_select_from_available");     
					FacesMessage message = new FacesMessage(MessageFormat.format(please_select_from_available, new Object[]{response, parent.getNumber(), itemText.getSequence(), parent.getItemData().getEmiAnswerOptionLabels()}));
					context.addMessage(toValidate.getClientId(context), message);
					break;
				}
				if (processed.contains(label)) {
					((UIInput)toValidate).setValid(false);
					String duplicate_responses = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.DeliveryMessages","duplicate_responses");     
					FacesMessage message = new FacesMessage(duplicate_responses + " '" + response + "' - " + q + " " + parent.getNumber() + "(" + itemText.getSequence() + ")" );
					context.addMessage(toValidate.getClientId(context), message);
					break;
				}
				processed += label;
			}
		}
	}
}
