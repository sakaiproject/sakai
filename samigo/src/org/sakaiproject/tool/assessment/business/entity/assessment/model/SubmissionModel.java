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


package org.sakaiproject.tool.assessment.business.entity.assessment.model;


/**
 * This keeps track of the submission scheme, and the number allowed.
 *
 * @author Rachel Gollub
 * @author Ed Smiley
 */
public class SubmissionModel
{
  private String numberSubmissions;
  private int submissionsAllowed;
  private int submissionModelId;

  /**
   * Creates a new SubmissionModel object.
   */
  public SubmissionModel()
  {
    //    numberSubmissions = AllChoices.numberSubmissionsShort[0];
    numberSubmissions = AllChoices.numberSubmissionsLong[0];
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public int getSubmissionModelId()
  {
    return submissionModelId;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param newId DOCUMENTATION PENDING
   */
  public void setSubmissionModelId(int newId)
  {
    submissionModelId = newId;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getNumberSubmissions()
  {
    return numberSubmissions;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pnumberSubmissions DOCUMENTATION PENDING
   */
  public void setNumberSubmissions(String pnumberSubmissions)
  {
    numberSubmissions = pnumberSubmissions;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public int getSubmissionsAllowed()
  {
    return submissionsAllowed;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param psubmissionsAllowed DOCUMENTATION PENDING
   */
  public void setSubmissionsAllowed(int psubmissionsAllowed)
  {
    submissionsAllowed = psubmissionsAllowed;
  }
}
