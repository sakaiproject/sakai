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
/**
 * DOCUMENTATION PENDING
 *
 * @author $author$
 * @version $Id$
 */
public class QuestionPoolAccessData
  implements Serializable
{
  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = 9180085666292824370L;

  private Long questionPoolId;
  private String agentId;
  private Long accessTypeId;

  public QuestionPoolAccessData(){
  }

  public QuestionPoolAccessData(Long questionPoolId, String agentId, Long accessTypeId){
    this.questionPoolId = questionPoolId;
    this.agentId = agentId;
    this.accessTypeId = accessTypeId;
  }

  public Long getQuestionPoolId()
  {
    return questionPoolId;
  }

  public void setQuestionPoolId(Long questionPoolId)
  {
    this.questionPoolId = questionPoolId;
  }

  public String getAgentId()
  {
    return agentId;
  }

  public void setAgentId(String agentId)
  {
    this.agentId = agentId;
  }

  public Long getAccessTypeId()
  {
    return accessTypeId;
  }

  public void setAccessTypeId(Long accessTypeId)
  {
    this.accessTypeId = accessTypeId;
  }

  public boolean equals(Object questionPoolAccess){
    boolean returnValue = false;
    if (this == questionPoolAccess)
      returnValue = true;
    if (questionPoolAccess != null && questionPoolAccess.getClass()==this.getClass()){
      QuestionPoolAccessData qpi = (QuestionPoolAccessData)questionPoolAccess;
      if ((this.getAccessTypeId()).equals(qpi.getAccessTypeId())
          && (this.getAgentId()).equals(qpi.getAgentId())
          && (this.getQuestionPoolId()).equals(qpi.getQuestionPoolId()))
        returnValue = true;
    }
    return returnValue;
  }

  public int hashCode(){
    String s = this.agentId+":"+(this.questionPoolId).toString()+":"+(this.accessTypeId).toString();
    return (s.hashCode());
  }
}
