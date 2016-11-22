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

import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;

/**
 * @author rgollub@stanford.edu
 * $Id$
 */
public class FibBean
{

  private ItemContentsBean parent;
  private AnswerIfc answer;
  private ItemGradingData data;
  private String response;
  private String text;
  private Boolean isCorrect;
  private boolean hasInput;
  
  public ItemContentsBean getItemContentsBean()
  {
    return parent;
  }

  public void setItemContentsBean(ItemContentsBean bean)
  {
    parent = bean;
  }

  public AnswerIfc getAnswer()
  {
    return answer;
  }

  public void setAnswer(AnswerIfc newanswer)
  {
    answer = newanswer;
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
    if (response == null)
      return "";
    return response;
  }

  public void setResponse(String newresp)
  {
        response = newresp;
	    if (data == null)
	    {
	      data = new ItemGradingData();
	      data.setPublishedItemId(parent.getItemData().getItemId());
	      data.setPublishedItemTextId(answer.getItemText().getId());
	      data.setPublishedAnswerId(answer.getId());
	      List<ItemGradingData> items = parent.getItemGradingDataArray();
	      items.add(data);
	      parent.setItemGradingDataArray(items);
	    }
	    data.setAnswerText(newresp);
  }

  public String getText()
  {
    return text;
  }

  public void setText(String newtext)
  {
    text = newtext;
  }

  public Boolean getIsCorrect()
  {
    return isCorrect;
  }

  public void setIsCorrect(Boolean newb)
  {
    isCorrect = newb;
  }

  public Boolean getHasInput()
  {
    return hasInput;
  }

  public void setHasInput(boolean newin)
  {
    hasInput = newin;
  }
}
