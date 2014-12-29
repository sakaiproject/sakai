/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
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



package org.sakaiproject.tool.assessment.data.ifc.assessment;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;

public interface AssessmentBaseIfc
    extends java.io.Serializable{

  public static Integer ACTIVE_STATUS = Integer.valueOf(1);
  public static Integer INACTIVE_STATUS = Integer.valueOf(0);
  public static Integer DEAD_STATUS = Integer.valueOf(2);
  public static Integer RETRACT_FOR_EDIT_STATUS = Integer.valueOf(3);
  public static String METADATAQUESTIONS_ISINSTRUCTOREDITABLE = "metadataQuestions_isInstructorEditable";
  public static String HASMETADATAFORQUESTIONS= "hasMetaDataForQuestions";
  public static String SHOWFEEDBACKAUTHORING= "showFeedbackAuthoring";

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
