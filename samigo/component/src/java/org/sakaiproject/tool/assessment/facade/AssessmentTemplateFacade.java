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
package org.sakaiproject.tool.assessment.facade;

import java.util.Date;

import org.osid.assessment.Assessment;
import org.osid.assessment.AssessmentException;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentTemplateData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentTemplateIfc;
import org.sakaiproject.tool.assessment.osid.assessment.impl.AssessmentImpl;


public class AssessmentTemplateFacade
    extends AssessmentBaseFacade
       implements AssessmentTemplateIfc
{
  public static String AUTHORS = "ASSESSMENTTEMPLATE_AUTHORS";
  public static String KEYWORDS = "ASSESSMENTTEMPLATE_KEYWORDS";
  public static String OBJECTIVES = "ASSESSMENTTEMPLATE_OBJECTIVES";
  public static String BGCOLOR = "ASSESSMENTTEMPLATE_BGCOLOR";
  public static String BGIMAGE = "ASSESSMENTTEMPLATE_BGIMAGE";

  public static Long DEFAULTTEMPLATE = new Long("1");
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
  public AssessmentTemplateFacade(Long id, String title, Date lastModifiedDate) {
    // in the case of template assessmentBaseId is the assessmentTemplateId
    this.assessmentTemplateId = id;
    super.setAssessmentBaseId(id);
    super.setTitle(title);
    super.setLastModifiedDate(lastModifiedDate);
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
