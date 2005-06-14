/**********************************************************************************
 *
 * $Header: /cvs/sakai2/sam/src/org/sakaiproject/tool/assessment/business/entity/helper/assessment/AssessmentHelperIfc.java,v 1.6 2005/05/31 19:14:29 janderse.umich.edu Exp $
 *
 ***********************************************************************************/
/*
 * Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.assessment.business.entity.helper.assessment;

import java.io.InputStream;

import org.sakaiproject.tool.assessment.business.entity.asi.Assessment;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;

/**
 * Interface for QTI-versioned assessment helper implementation.
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley esmiley@stanford.edu
 * @version $Id$
 */
public interface AssessmentHelperIfc
{

  /**
   * Read XML docuemnt from input stream
   *
   * @param inputStream input stream
   *
   * @return  the XML assessment
   */
  public Assessment readXMLDocument(
    InputStream inputStream);

  /**
   * Set feedback settings in XML
   * @param assessmentXml
   * @param feedback
   */
  public void updateFeedbackModel(Assessment assessmentXml,
    AssessmentFeedbackIfc feedback);

  /**
   * Set evaluation settings in XML.
   * @param assessmentXml
   * @param evaluationModel
   */
  public void updateEvaluationModel(Assessment assessmentXml,
    EvaluationModelIfc evaluationModel);

  /**
   * Set access control settings in XML.
   * @param assessmentXml
   * @param accessControl
   */
  public void updateAccessControl(Assessment assessmentXml,
    AssessmentAccessControlIfc accessControl);

  public void updateMetaData(Assessment assessmentXml,
    AssessmentFacade assessment);

  /**
 * Set the assessment description.
 * This is valid for all undelimited single item texts.
 * Not valid for matching or fill in the blank
 * @param description assessment description
 * @param assessmentXml the xml
 */
  public void setDescriptiveText(String description, Assessment assessmentXml);



}
/**********************************************************************************
 *
 * $Header: /cvs/sakai2/sam/src/org/sakaiproject/tool/assessment/business/entity/helper/assessment/AssessmentHelperIfc.java,v 1.6 2005/05/31 19:14:29 janderse.umich.edu Exp $
 *
 ***********************************************************************************/
