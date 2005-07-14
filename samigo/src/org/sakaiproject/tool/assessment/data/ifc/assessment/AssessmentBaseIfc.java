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
package org.sakaiproject.tool.assessment.data.ifc.assessment;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;

public interface AssessmentBaseIfc
    extends java.io.Serializable{

  public static Integer ACTIVE_STATUS = new Integer(1);
  public static Integer INACTIVE_STATUS = new Integer(0);
  public static Integer DEAD_STATUS = new Integer(2);
  public static String METADATAQUESTIONS_ISINSTRUCTOREDITABLE = "metadataQuestions_isInstructorEditable";
  public static String HASMETADATAFORQUESTIONS= "hasMetaDataForQuestions";

  Long getAssessmentBaseId();

  void setAssessmentBaseId(Long id);

  Boolean getIsTemplate();

  void setIsTemplate(Boolean isTemplate);

  Long getParentId();

  void setParentId(Long parentId);

  String getTitle();

  void setTitle(String title);

  String getDescription();

  void setDescription(String description);

  String getComments();

  void setComments(String comments);

  Integer getInstructorNotification();

  void setInstructorNotification(Integer instructorNotification);

  Integer getTesteeNotification();

  void setTesteeNotification(Integer testeeNotification);

  Integer getMultipartAllowed();

  void setMultipartAllowed(Integer multipartAllowed);

  Long getTypeId();

  void setTypeId(Long typeId);

  Integer getStatus();

  void setStatus(Integer status);

  String getCreatedBy();

  void setCreatedBy(String createdBy);

  Date getCreatedDate();

  void setCreatedDate(Date createdDate);

  String getLastModifiedBy();

  void setLastModifiedBy(String lastModifiedBy);

  Date getLastModifiedDate();

  void setLastModifiedDate(Date lastModifiedDate);

  AssessmentAccessControlIfc getAssessmentAccessControl();

  void setAssessmentAccessControl(AssessmentAccessControlIfc assessmentAccessControl);

  EvaluationModelIfc getEvaluationModel();

  void setEvaluationModel(EvaluationModelIfc evaluationModel);

  AssessmentFeedbackIfc getAssessmentFeedback();

  void setAssessmentFeedback(AssessmentFeedbackIfc assessmentFeedback);

  Set getSecuredIPAddressSet();

  void setSecuredIPAddressSet(Set securedIPAddressSet);

  Set getAssessmentMetaDataSet();

  void setAssessmentMetaDataSet(Set assessmentMetaDataSet);

  HashMap getAssessmentMetaDataMap(Set assessmentMetaDataSet);

  HashMap getAssessmentMetaDataMap();

  String getAssessmentMetaDataByLabel(String label) ;

  void addAssessmentMetaData(String label, String entry);

  void updateAssessmentMetaData(String label, String entry);

  TypeIfc getType();

}
