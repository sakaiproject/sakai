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




package org.sakaiproject.tool.assessment.shared.api.assessment;

import java.util.List;

import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentTemplateIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;

/**
 * The AssessmentServiceAPI declares a shared interface to get/set assessment
 * information.
 * @author Ed Smiley <esmiley@stanford.edu>
 */
public interface AssessmentServiceAPI
{
  public AssessmentTemplateIfc getAssessmentTemplate(String assessmentTemplateId);

  public AssessmentIfc getAssessment(String assessmentId);

  public AssessmentIfc getBasicInfoOfAnAssessment(String assessmentId);

  public List getAllAssessmentTemplates();

  public List getAllActiveAssessmentTemplates();

  public List getTitleOfAllActiveAssessmentTemplates();

  public List getAllAssessments(String orderBy);

  public List getAllActiveAssessments(String orderBy);

  public List getSettingsOfAllActiveAssessments(String orderBy);

  public List getBasicInfoOfAllActiveAssessments(String orderBy, boolean ascending);

  public List getBasicInfoOfAllActiveAssessments(String orderBy);

  public List getAllAssessments(
      int pageSize, int pageNumber, String orderBy);

  public AssessmentIfc createAssessment(
    String title, String description, String typeId, String templateId);

  public int getQuestionSize(String assessmentId);

  public void update(AssessmentIfc assessment);

  public void save(AssessmentTemplateIfc template);

  public void saveAssessment(AssessmentIfc assessment);

  public void deleteAssessmentTemplate(Long assessmentId);

  public void removeAssessment(String assessmentId);

  public SectionDataIfc addSection(String assessmentId);

  public void removeSection(String sectionId);

  public SectionDataIfc getSection(String sectionId);

  public void saveOrUpdateSection(SectionDataIfc section);

  public void moveAllItems(String sourceSectionId, String destSectionId);

  public void removeAllItems(String sourceSectionId);

  public List getBasicInfoOfAllActiveAssessmentTemplates(String orderBy);

  public AssessmentIfc createAssessmentWithoutDefaultSection(
      String title, String description, String typeId, String templateId);
}
