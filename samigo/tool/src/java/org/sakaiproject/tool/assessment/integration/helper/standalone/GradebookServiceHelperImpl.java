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
package org.sakaiproject.tool.assessment.integration.helper.standalone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.data.dao.assessment.
  PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.ifc.grading.AssessmentGradingIfc;
import org.sakaiproject.tool.assessment.integration.helper.ifc.
  GradebookServiceHelper;

public class GradebookServiceHelperImpl implements GradebookServiceHelper
{
  private static Log log = LogFactory.getLog(GradebookServiceHelperImpl.class);

  public boolean gradebookExists(String gradebookUId)
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper method*/
    throw new java.lang.UnsupportedOperationException(
      "Method gradebookExists() not yet implemented.");
  }

  public void removeExternalAssessment(String gradebookUId,
                                       String publishedAssessmentId) throws
    Exception
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper method*/
    throw new java.lang.UnsupportedOperationException(
      "Method removeExternalAssessment() not yet implemented.");
  }

  public boolean addToGradebook(PublishedAssessmentData publishedAssessment) throws
    Exception
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper method*/
    throw new java.lang.UnsupportedOperationException(
      "Method addToGradebook() not yet implemented.");
  }

  public void updateExternalAssessmentScore(AssessmentGradingIfc ag) throws
    Exception
  {
    /**@todo Implement this org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper method*/
    throw new java.lang.UnsupportedOperationException(
      "Method updateExternalAssessmentScore() not yet implemented.");
  }
}