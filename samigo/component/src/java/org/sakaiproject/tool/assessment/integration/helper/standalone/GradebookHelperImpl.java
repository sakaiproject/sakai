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

import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookHelper;

/**
 *
 * <p>Description:
 * This is a stub standalone context implementation helper delegate class for
 * the GradebookFacade class.  The helper methods use hardcoded values.
 * "Standalone" means that Samigo (Tests and Quizzes)
 * is running without the context of the Sakai portal and authentication
 * mechanisms, and therefore we use stub methods.</p>
 * <p>Note: To customize behavior you can add your own helper class to the
 * Spring injection via the integrationContext.xml for your context.
 * The particular integrationContext.xml to be used is selected by the
 * build process.
 * </p>
 * <p>Sakai Project Copyright (c) 2005</p>
 * <p> </p>
 * @author Ed Smiley <esmiley@stanford.edu>
 *
 */
public class GradebookHelperImpl implements GradebookHelper
{
  private static Log log = LogFactory.getLog(GradebookHelperImpl.class);

  /**
   * Hardcoded stub.  Get current gradebook uid.
   * @return Gradebook #10
   */
  public String getGradebookUId(){
    return "QA_8";
  }

  /**
   * Hardcoded stub.  Get teh default gradebook uid.
   * @return Gradebook #1
   */
  public String getDefaultGradebookUId(){
    return "QA_1";
  }
}