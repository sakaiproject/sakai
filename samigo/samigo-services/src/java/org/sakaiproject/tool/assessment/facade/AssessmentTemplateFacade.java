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

package org.sakaiproject.tool.assessment.facade;

import java.util.Date;

import org.osid.assessment.Assessment;
import org.osid.assessment.AssessmentException;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentTemplateData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentTemplateIfc;
import org.sakaiproject.tool.assessment.osid.assessment.impl.AssessmentImpl;


public class AssessmentTemplateFacade
    extends AssessmentBaseFacade
    implements java.io.Serializable, AssessmentTemplateIfc
{
  private static final long serialVersionUID = 7526471155622776147L;
  public static final String AUTHORS = "ASSESSMENTTEMPLATE_AUTHORS";
  public static final String KEYWORDS = "ASSESSMENTTEMPLATE_KEYWORDS";
  public static final String OBJECTIVES = "ASSESSMENTTEMPLATE_OBJECTIVES";
  public static final String BGCOLOR = "ASSESSMENTTEMPLATE_BGCOLOR";
  public static final String BGIMAGE = "ASSESSMENTTEMPLATE_BGIMAGE";

  public static final Long DEFAULTTEMPLATE = new Long("1");
  public static final Integer INACTIVE_STATUS = new Integer("0");
  public static final Integer ACTIVE_STATUS = new Integer("1");
  private org.osid.assessment.Assessment assessment;
  private AssessmentTemplateIfc data;
  private Long assessmentTemplateId;

  public AssessmentTemplateFacade() {
    super();
    this.data = new AssessmentTemplateData();
    AssessmentImpl assessmentImpl = new AssessmentImpl(); //<-- place holder
    assessment = (Assessment)assessmentImpl;
    try {
      assessment.updateData(this.data);
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
  }

  /**
   * IMPORTANT: this constructor do not have "data", this constructor is
   * merely used for holding assessmentBaseId (which is the templateId) & Title
   * for displaying purpose.
   * This constructor does not persist data (which it has none) to DB
   * @param id
   * @param title
   */
  public AssessmentTemplateFacade(Long id, String title) {
    // in the case of template assessmentBaseId is the assessmentTemplateId
    this.assessmentTemplateId = id;
    super.setAssessmentBaseId(id);
    super.setTitle(title);
  }

  /**
   * IMPORTANT: this constructor do not have "data", this constructor is
   * merely used for holding assessmentBaseId (which is the assessmentId), Title
   * & lastModifiedDate for displaying purpose.
   * This constructor does not persist data (which it has none) to DB
   * @param id
   * @param title
   * @param lastModifiedDate
   */
  public AssessmentTemplateFacade(Long id, String title, Date lastModifiedDate, Long typeId) {
    // in the case of template assessmentBaseId is the assessmentTemplateId
    this.assessmentTemplateId = id;
    super.setAssessmentBaseId(id);
    super.setTitle(title);
    super.setLastModifiedDate(lastModifiedDate);
    super.setTypeId(typeId);
  }

  public AssessmentTemplateFacade(AssessmentTemplateIfc data) {
    super(data);
    this.data = data;
    AssessmentImpl assessmentImpl = new AssessmentImpl(); // place holder
    assessment = (Assessment)assessmentImpl;
    try {
      assessment.updateData(this.data);
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    this.assessmentTemplateId = data.getAssessmentTemplateId();
  }

  public Long getAssessmentTemplateId() {
    try {
      this.data = (AssessmentTemplateIfc) assessment.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getAssessmentTemplateId();
 }

  public void setAssessmentTemplateId(Long newId) {
    try {
      this.data = (AssessmentTemplateIfc) assessment.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    this.data.setAssessmentTemplateId(newId);
  }

}
