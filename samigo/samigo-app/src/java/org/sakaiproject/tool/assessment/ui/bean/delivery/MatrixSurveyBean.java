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

package org.sakaiproject.tool.assessment.ui.bean.delivery;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;

/**
 * @author kimhuang@rutgers.edu
 * $Id$
 */
@Slf4j
 public class MatrixSurveyBean
{

	private ItemContentsBean parent;
	private ItemTextIfc itemText;
	private ItemGradingData data;
	private String responseId;
	private AnswerIfc[] answerArray;
	private String[] answerSid;
	private boolean response;


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

  public AnswerIfc[] getAnswerArray()
  {
	//answer.getText() should be the one item in rowChoices
    return answerArray;
  }

  public void setAnswerArray(AnswerIfc[] newanswer)
  {
    answerArray = newanswer;
  }
  
  public String[] getAnswerSid()
  {
	  return answerSid;
  }
  public void setAnswerSid(String[] sid){
	  answerSid = sid;
  }
  public ItemGradingData getItemGradingData()
  {
    return data;
  }

  public void setItemGradingData(ItemGradingData newdata)
  {
    data = newdata;
  }

 public String getResponseId()
  {
    return responseId;
  }
  public void setResponseId(String newresp)
  {

    responseId = newresp;
    if (newresp != null)
    {
    	//1. remove all the old selections with the same Itemtextid(row)from ItemGradingDataArray 
    	List<ItemGradingData> items = parent.getItemGradingDataArray();
    	for(int i = 0; i<items.size(); i++)
    	{
    		ItemGradingData gradingData = (ItemGradingData) items.get(i);
    		if (gradingData.getPublishedItemTextId().equals(itemText.getId()) ){
    			items.remove(i);
    		}
    	}

		// 2. add the new selection to the list
    	if (data ==null)
            data = new ItemGradingData();
    	
        data.setPublishedItemId(parent.getItemData().getItemId());
        data.setPublishedItemTextId(itemText.getId());
        data.setPublishedAnswerId(new Long(newresp));
        
        log.debug("After setting the data>>ItemId>>itemTextId+newresp "+ data.getPublishedItemId()+data.getPublishedItemTextId()+data.getPublishedAnswerId()+"\n");
    	items = parent.getItemGradingDataArray();
    	items.add(data);
    	parent.setItemGradingDataArray(items);

    }
    }

public void setResponseFromCleanRadioButton() {
            response = false; 
            responseId = null;
            data = null;
 }

public boolean getResponse()
{
	return response;
}
	
public void setResponse(boolean newresp)
{
	response = newresp;

       
}
}
