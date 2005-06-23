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

package org.sakaiproject.tool.assessment.ui.bean.evaluation;

import java.io.Serializable;

/**
 * <p>Title: sakaiproject.org</p>
 * <p>Description: AAM - class form for evaluating student scores</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Stanford University</p>
 *
 * @author Rachel Gollub
 * @version 1.0
 */
public class StudentScoresBean
  implements Serializable
{
  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = 5517587781720762296L;

  private String studentName;
  private String comments;
  private String publishedId;
  private String studentId;
  private String assessmentGradingId;
  private String itemId; // ID of the first item; used by QuestionScores

  /**
   * Creates a new StudentScoresBean object.
   */
  public StudentScoresBean()
  {
    System.out.println("RACHEL: Creating a new studentscoresbean");
  }

  public String getStudentName()
  {
    return studentName;
  }

  public void setStudentName(String newname)
  {
    studentName = newname;
  }

  public String getComments()
  {
    return comments;
  }

  public void setComments(String newcomments)
  {
    comments = newcomments;
  }

  public String getPublishedId()
  {
    return publishedId;
  }

  public void setPublishedId(String newId)
  {
    publishedId = newId;
  }

  public String getStudentId()
  {
    return studentId;
  }

  public void setStudentId(String newId)
  {
    studentId = newId;
  }

  public String getAssessmentGradingId()
  {
    return assessmentGradingId;
  }

  public void setAssessmentGradingId(String newId)
  {
    assessmentGradingId = newId;
  }

  public String getItemId()
  {
    return itemId;
  }

  public void setItemId(String newId)
  {
    itemId = newId;
  }
}
