/**********************************************************************************
* $HeadURL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.assessment.ui.bean.delivery;

import java.util.ArrayList;

import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;

/**
 * @author rgollub@stanford.edu
 * $Id$
 */
public class SelectionBean
{

  private ItemContentsBean parent;
  private ItemGradingData data;
  private boolean response;
  private AnswerIfc answer;
  private String feedback;
  private String responseId;

  public ItemContentsBean getItemContentsBean()
  {
    return parent;
  }

  public void setItemContentsBean(ItemContentsBean bean)
  {
    parent = bean;
  }

  public ItemGradingData getItemGradingData()
  {
    return data;
  }

  public void setItemGradingData(ItemGradingData newdata)
  {
    data = newdata;
  }

  public boolean getResponse()
  {
    return response;
  }

  public void setResponse(boolean newresp)
  {
    response = newresp;
    if (newresp)
    {
      ItemTextIfc itemText = (ItemTextIfc) parent.getItemData()
        .getItemTextSet().toArray()[0];
      if (data == null)
      {
        data = new ItemGradingData();
        data.setPublishedItem(parent.getItemData());
        data.setPublishedItemText(itemText);
        ArrayList items = parent.getItemGradingDataArray();
        items.add(data);
        parent.setItemGradingDataArray(items);
      }
      data.setPublishedAnswer(answer);
    }
    else if (data != null)
      data.setPublishedAnswer(null);
  }

  public AnswerIfc getAnswer()
  {
    return answer;
  }

  public void setAnswer(AnswerIfc newAnswer)
  {
    answer = newAnswer;
  }

  public String getFeedback()
  {
    if (feedback == null)
      return "";
    return feedback;
  }

  public void setFeedback(String newfb)
  {
    feedback = newfb;
  }

  public String getAnswerId()
  {
    return answer.getId().toString();
  }
}
