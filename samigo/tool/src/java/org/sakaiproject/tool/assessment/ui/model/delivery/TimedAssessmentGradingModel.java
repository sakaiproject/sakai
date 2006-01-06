/**********************************************************************************
* $URL: https://source.sakaiproject.org/svn/trunk/sakai/sam/component/src/java/org/sakaiproject/tool/assessment/data/dao/grading/AssessmentGradingData.java $
* $Id: AssessmentGradingData.java 4720 2005-12-16 03:29:39Z daisyf@stanford.edu $
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
package org.sakaiproject.tool.assessment.ui.model.delivery;

import java.util.Date;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class TimedAssessmentGradingModel
{
  private Long assessmentGradingId;
  private int timeLimit; // in seconds
  private Date beginDate;
  private Date expirationDate;
  private Date localBeginDate;
  private Date localExpirationDate;
  private boolean submittedForGrade=false;
  
  /* 30 sec, this is to allow JScript clock to catch up before server submit the
   * assessment for grade
   */ 
  private int latencyBuffer=30; 

  /* timedAG is removed from timedAssessmentQueue immediately if the submission for
   * grade is initiated by user OR the AutoSubmit script in the web page. If user exit
   * application unexpectedly (e.g.closing browser), server will do the submission 
   * when time is up and also clean up the timedAG in queue. So we add a transactionBuffer
   * here before server clean up
   */ 
  private int transactionBuffer=30; // 30 sec

  public TimedAssessmentGradingModel() {
  }

  public TimedAssessmentGradingModel(Long assessmentGradingId,
      int timeLimit, int latencyBuffer, int transactionBuffer,
      Date beginDate, Date localBeginDate, 
      boolean submittedForGrade){
    this.assessmentGradingId = assessmentGradingId;
    this.timeLimit = timeLimit;
    this.latencyBuffer = latencyBuffer;
    this.transactionBuffer = transactionBuffer;
    this.beginDate = beginDate;
    this.expirationDate = new Date(beginDate.getTime() + timeLimit + latencyBuffer);
    this.submittedForGrade = submittedForGrade;
    this.localBeginDate = localBeginDate;
    this.localExpirationDate = new Date(localBeginDate.getTime() + timeLimit + latencyBuffer);
  }

  public Long getAssessmentGradingId() {
    return assessmentGradingId;
  }

  public void setAssessmentGradingId(Long assessmentGradingId) {
    this.assessmentGradingId = assessmentGradingId;
  }

  public int getTimeLimit() {
    return timeLimit;
  }

  public void setTimeLimit(int timeLimit) {
    this.timeLimit = timeLimit;
  }

  public int getLatencyBuffer() {
    return latencyBuffer;
  }

  public void setLatencyBuffer(int latencyBuffer) {
    this.latencyBuffer = latencyBuffer;
  }

  public int getTransactionBuffer() {
    return transactionBuffer;
  }

  public void setTransactionBuffer(int transactionBuffer) {
    this.transactionBuffer = transactionBuffer;
  }

  public Date getBeginDate() {
    return beginDate;
  }

  public void setBeginDate(Date beginDate) {
    this.beginDate = beginDate;
  }

  public Date getExpirationDate() {
    return expirationDate;
  }

  public void setExpirationDate(Date expirationDate) {
    this.expirationDate = expirationDate;
  }

  public Date getLocalBeginDate() {
    return localBeginDate;
  }

  public void setLocalBeginDate(Date localBeginDate) {
    this.localBeginDate = localBeginDate;
  }

  public Date getLocalExpirationDate() {
    return localExpirationDate;
  }

  public void setLocalExpirationDate(Date localExpirationDate) {
    this.localExpirationDate = localExpirationDate;
  }

  public boolean getSubmittedForGrade() {
    return submittedForGrade;
  }

  public void setSubmittedForGrade(boolean submittedForGrade) {
    this.submittedForGrade = submittedForGrade;
  }

}
