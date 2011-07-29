/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/tags/samigo-2.7.0/samigo-app/src/java/org/sakaiproject/tool/assessment/ui/bean/delivery/FibBean.java $
 * $Id: FibBean.java 59684 2009-04-03 23:33:27Z arwhyte@umich.edu $
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

package org.sakaiproject.tool.assessment.ui.bean.delivery;

import java.util.ArrayList;
import java.util.Iterator;

//import javax.faces.model.SelectItem;

import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;

/**
 * @author kimhuang@rutgers.edu
 * $Id: MatrixSurveyBean.java 59684 2009-04-03 23:33:27Z kimhuang@rutgers.edu $
 */
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

	//System.out.print("\nkim debug: MatrixSurveyBean:setResponse: newresp: " +newresp+"\n");
	//System.out.print("\nkim debug: MatrixSurveyBean: setResponse: ItemText : " + itemText.getText());
    //System.out.print("\nkim debug: MatrixSurveyBean:setResponse:setPublishedItemTextId:itemText.getId()\n"+itemText.getId()+"\n");
    responseId = newresp;
    if (newresp != null)
    {
    	//1. remove all the old selections with the same Itemtextid(row)from ItemGradingDataArray 
    	ArrayList items = parent.getItemGradingDataArray();
    	for(int i = 0; i<items.size(); i++)
    	{
    		ItemGradingData gradingData = (ItemGradingData) items.get(i);
    		if (gradingData.getPublishedItemTextId().equals(itemText.getId()) ){
    			items.remove(i);
    		}
    	}
/*
    	//debugging purpose
    	Iterator iter = parent.getItemGradingDataArray().iterator();
    	while(iter.hasNext())
    	{
    		ItemGradingData gradingData = (ItemGradingData) iter.next();
    		System.out.print("\n kim debug: MatrixSurveyBean.java- traversing gradingData.ItemId:"+ 
    				gradingData.getPublishedItemId()+" ItemTextId:"+ 
    				gradingData.getPublishedItemTextId() +" AnswerId: "+ gradingData.getPublishedAnswerId()+"\n");
    	}
    	*/
    	// 2. add the new selection to the list
    	if (data ==null)
            data = new ItemGradingData();
    	
        data.setPublishedItemId(parent.getItemData().getItemId());
        data.setPublishedItemTextId(itemText.getId());
        data.setPublishedAnswerId(new Long(newresp));
        
        //System.out.print("\n matrixSurveyBean.java: after setting the data>>ItemId>>itemTextId+newresp "+ data.getPublishedItemId()+data.getPublishedItemTextId()+data.getPublishedAnswerId()+"\n");
    	items = parent.getItemGradingDataArray();
    	items.add(data);
    	parent.setItemGradingDataArray(items);
    	
//debugging purpose
    	/*
    	iter = parent.getItemGradingDataArray().iterator();
    	while(iter.hasNext())
    	{
    		ItemGradingData gradingData = (ItemGradingData) iter.next();
    		System.out.print("\n kim debug2: ItemGradingDataArray: gradingData.ItemId: " + gradingData.getPublishedItemId());
    		System.out.print("\n kim debug2: ItemGradingDataArray: gradingData.ItemTextId: " + gradingData.getPublishedItemTextId());
    		System.out.print("\n kim debug2: ItemGradingDataArray: gradingData.AnswerId: " + gradingData.getPublishedAnswerId());
    	}
    	*/
    }
    	
    }

public void setResponseFromCleanRadioButton() {
            response = false; 
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
