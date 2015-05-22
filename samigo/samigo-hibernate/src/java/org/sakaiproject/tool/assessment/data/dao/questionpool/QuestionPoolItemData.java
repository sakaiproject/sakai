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

package org.sakaiproject.tool.assessment.data.dao.questionpool;

import java.io.Serializable;

import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.ifc.questionpool.QuestionPoolItemIfc;
/**
 *
 * @author $author$
 * @version $Id$
 */
public class QuestionPoolItemData
    implements Serializable, QuestionPoolItemIfc 
{
  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = 9180085666292824370L;

  private Long questionPoolId;
  private Long itemId;
  private ItemData itemData; //<-- is the item
    //private QuestionPool questionPool;

  public QuestionPoolItemData(){
  }

  public QuestionPoolItemData(Long questionPoolId, Long itemId){
    this.questionPoolId = questionPoolId;
    this.itemId = itemId;
  }

  public QuestionPoolItemData(Long questionPoolId, Long itemId, ItemData itemData){
    this.questionPoolId = questionPoolId;
    this.itemId = itemId;
    this.itemData = itemData;
  }

  public QuestionPoolItemData(ItemData itemData, QuestionPoolData questionPoolData){
    this.itemData = itemData;
    //this.questionPool = questionPool;
    //setQuestionPoolId(questionPoolProperties.getId());
    setItemId(itemData.getItemId());
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

  public Long getItemId()
  {
    return itemId;
  }

  public void setItemId(Long itemId)
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
