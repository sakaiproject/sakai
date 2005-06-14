package org.sakaiproject.tool.assessment.data.dao.questionpool;

import java.io.Serializable;

import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
/**
 * DOCUMENTATION PENDING
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
