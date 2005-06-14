/*
 * Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
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
 */


package org.sakaiproject.tool.assessment.ui.bean.delivery;

import java.util.ArrayList;
import java.util.Iterator;

import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;

/**
 * @author rgollub@stanford.edu
 * $Id: MatchingBean.java,v 1.4 2005/03/23 06:29:17 rgollub.stanford.edu Exp $
 */
public class MatchingBean
{

  private ItemContentsBean parent;
  private ItemTextIfc itemText;
  private ItemGradingData data;
  private String response;
  private ArrayList choices;
  private String text;
  private String feedback;

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
    response = newresp;
    if (data == null)
    {
      data = new ItemGradingData();
      data.setPublishedItem(parent.getItemData());
      data.setPublishedItemText(itemText);
      ArrayList items = parent.getItemGradingDataArray();
      items.add(data);
      parent.setItemGradingDataArray(items);
    }
    Iterator iter = itemText.getAnswerSet().iterator();
    while (iter.hasNext())
    {
      AnswerIfc answer = (AnswerIfc) iter.next();
      if (answer.getId().toString().equals(newresp))
      {
        data.setPublishedAnswer(answer);
        break;
      }
    }  
  }

  public ArrayList getChoices()
  {
    return choices;
  }

  public void setChoices(ArrayList newch)
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

  public boolean getIsCorrect()
  {
    if (data != null && data.getPublishedAnswer() != null && 
        data.getPublishedAnswer().getIsCorrect() != null &&
        data.getPublishedAnswer().getIsCorrect().booleanValue())
      return true;
    return false;
  }
}
