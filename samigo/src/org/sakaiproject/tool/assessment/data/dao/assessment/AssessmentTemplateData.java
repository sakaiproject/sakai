/**********************************************************************************
* $HeadURL$
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
package org.sakaiproject.tool.assessment.data.dao.assessment;
import java.util.Date;

public class AssessmentTemplateData
    extends  org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentBaseData
    implements java.io.Serializable,
               org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentTemplateIfc
{
  public static String AUTHORS = "ASSESSMENTTEMPLATE_AUTHORS";
  public static String KEYWORDS = "ASSESSMENTTEMPLATE_KEYWORDS";
  public static String OBJECTIVES = "ASSESSMENTTEMPLATE_OBJECTIVES";
  public static String BGCOLOR = "ASSESSMENTTEMPLATE_BGCOLOR";
  public static String BGIMAGE = "ASSESSMENTTEMPLATE_BGIMAGE";

  /* AssessmentTemplate also has AssessmentAccessControl and EvaluationModel
   * but it does not have section
   * private AssessmentAccessControlIfc assessmentAccessControl;
   * private EvaluationModelIfc evaluationModel;
   */

  public AssessmentTemplateData(){
    setIsTemplate(new Boolean("true"));
  }

  public AssessmentTemplateData(Long assessmentTemplateId, String title){
    // in the case of template assessmentBaseId is the assessmentTemplateId
    super(assessmentTemplateId,title);
  }

  public AssessmentTemplateData(Long assessmentTemplateId, String title, Date lastModifiedDate){
    super(assessmentTemplateId,title,lastModifiedDate);
  }

  public AssessmentTemplateData(Long parentId,
                  String title, String description, String comments,
                  Long typeId,
                  Integer instructorNotification, Integer testeeNotification,
                  Integer multipartAllowed, Integer status, String createdBy,
                  Date createdDate, String lastModifiedBy,
                  Date lastModifiedDate) {
    super(new Boolean("true"),parentId,
               title, description, comments,
               typeId,
               instructorNotification, testeeNotification,
               multipartAllowed, status, createdBy,
               createdDate, lastModifiedBy,
               lastModifiedDate);
  }

  public Long getAssessmentTemplateId(){
    return super.getAssessmentBaseId();
  }

  public void setAssessmentTemplateId(Long templateId) {
    super.setAssessmentBaseId(templateId);
  }
}
