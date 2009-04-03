/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/



package org.sakaiproject.tool.assessment.ui.bean.evaluation;

import java.io.Serializable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <p>Description: class form for evaluating student scores</p>
 *
 */
public class StudentScoresBean implements Serializable
{
  private static Log log = LogFactory.getLog(StudentScoresBean.class);

  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = 5517587781720762296L;

  private String studentName;
  private String firstName;
  private String lastName;
  private String comments;
  private String publishedId;
  private String studentId;
  private String assessmentGradingId;
  private String itemId; // ID of the first item; used by QuestionScores
  private String email;

  /**
   * Creates a new StudentScoresBean object.
   */
  public StudentScoresBean()
  {
    log.debug("Creating a new StudentScoresBean");
  }
  
  public String getStudentName()
  {
    return studentName;
  }

  public void setStudentName(String studentName)
  {
    this.studentName = studentName;
  }

  public String getFirstName()
  {
    return firstName;
  }

  public void setFirstName(String firstName)
  {
    this.firstName = firstName;
  }

  public String getLastName()
  {
    return lastName;
  }

  public void setLastName(String lastName)
  {
    this.lastName = lastName;
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
  
  public String getEmail()
  {
    return email;
  }

  public void setEmail(String email)
  {
	  this.email = email;
  }
}
