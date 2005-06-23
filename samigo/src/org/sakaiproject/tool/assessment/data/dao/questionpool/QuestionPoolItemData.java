/**********************************************************************************
* $HeadURL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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
package org.sakaiproject.tool.assessment.data.dao.questionpool;

import java.io.Serializable;

import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
/**
 *
 * @author $author$
 * @version $Id$
 */
public class QuestionPoolItemData
  implements Serializable
{
  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = 9180085666292824370L;

  private Long questionPoolId;
  private String itemId;
  private ItemData itemData; //<-- is the item
    //private QuestionPool questionPool;

  public QuestionPoolItemData(){
  }

  public QuestionPoolItemData(Long questionPoolId, String itemId){
    this.questionPoolId = questionPoolId;
    this.itemId = itemId;
  }

  public QuestionPoolItemData(Long questionPoolId, String itemId, ItemData itemData){
    this.questionPoolId = questionPoolId;
    this.itemId = itemId;
    this.itemData = itemData;
  }

  public QuestionPoolItemData(ItemData itemData, QuestionPoolData questionPoolData){
    this.itemData = itemData;
    //this.questionPool = questionPool;
    //setQuestionPoolId(questionPoolProperties.getId());
    setItemId(itemData.getItemIdString());
    setQuestionPoolId(questionPoolData.getQuestionPoolId());
  }

  public Long getQuestionPoolId()
  {
    return questionPoolId;
  }

  public void setQuestionPoolId(Long questionPoolId)
  {
    this.questionPoolId = questionPoolId;
  }

  public String getItemId()
  {
    return itemId;
  }

  public void setItemId(String itemId)
  {
    this.itemId = itemId;
  }

  public boolean equals(Object questionPoolItem){
    boolean returnValue = false;
    if (this == questionPoolItem)
      returnValue = true;
    if (questionPoolItem != null && questionPoolItem.getClass()==this.getClass()){
      QuestionPoolItemData qpi = (QuestionPoolItemData)questionPoolItem;
      if ((this.getItemId()).equals(qpi.getItemId())
          && (this.getQuestionPoolId()).equals(qpi.getQuestionPoolId()))
        returnValue = true;
    }
    return returnValue;
  }

  public int hashCode(){
    String s = this.itemId+":"+(this.questionPoolId).toString();
    return (s.hashCode());
  }
}
