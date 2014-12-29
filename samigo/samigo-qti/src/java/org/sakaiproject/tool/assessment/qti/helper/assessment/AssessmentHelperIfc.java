/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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



package org.sakaiproject.tool.assessment.qti.helper.assessment;

import java.io.InputStream;

import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.qti.asi.Assessment;
import java.util.Set;

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

  public void updateIPAddressSet(Assessment assessmentXml,
                                  Set securedIPAddressSet);
  
  public void updateAttachmentSet(Assessment assessmentXml, Set attachmentSet);

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
