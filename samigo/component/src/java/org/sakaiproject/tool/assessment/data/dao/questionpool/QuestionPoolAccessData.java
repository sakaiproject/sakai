/**********************************************************************************
* $URL$
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
