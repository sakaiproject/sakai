/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/trunk/sakai/sam/src/org/sakaiproject/tool/assessment/services/ItemService.java $
 * $Id: ItemService.java 1285 2005-08-19 02:05:48Z esmiley@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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
package org.sakaiproject.tool.assessment.shared.impl.assessment;

import java.util.List;

import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentTemplateData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentTemplateIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.shared.api.assessment.AssessmentServiceAPI;

/** @todo implement methods */
public class AssessmentServiceImpl
  implements AssessmentServiceAPI
{
  /** @todo implement method */
  public AssessmentTemplateIfc getAssessmentTemplate(String
    assessmentTemplateId)
  {
    return null;
  }

  /** @todo implement method */
  public AssessmentIfc getAssessment(String
                                     assessmentId)
  {
    return null;
  }

  /** @todo implement method */
  public AssessmentIfc getBasicInfoOfAnAssessment(
    String assessmentId)
  {
    return null;
  }

  /** @todo implement method */
  public List getAllAssessmentTemplates()
  {
    return null;
  }

  /** @todo implement method */
  public List getAllActiveAssessmentTemplates()
  {
    return null;
  }

  /** @todo implement method */
  public List
    getTitleOfAllActiveAssessmentTemplates()
  {
    return null;
  }

  /** @todo implement method */
  public List getAllAssessments(String orderBy)
  {
    return null;
  }

  /** @todo implement method */
  public List getAllActiveAssessments(String
                                      orderBy)
  {
    return null;
  }

  /** @todo implement method */
  public List getSettingsOfAllActiveAssessments(
    String orderBy)
  {
    return null;
  }

  /** @todo implement method */
  public List getBasicInfoOfAllActiveAssessments(
    String orderBy,
    boolean ascending)
  {
    return null;
  }

  /** @todo implement method */
  public List getBasicInfoOfAllActiveAssessments(
    String orderBy)
  {
    return null;
  }

  /** @todo implement method */
  public List getAllAssessments(int pageSize,
                                int pageNumber, String orderBy)
  {
    return null;
  }

  /** @todo implement method */
  public AssessmentIfc createAssessment(String
                                        title, String description,
                                        String typeId, String templateId)
  {
    return null;
  }

  /** @todo implement method */
  public int getQuestionSize(String assessmentId)
  {
    return 0;
  }

  /** @todo implement method */
  public void update(AssessmentIfc assessment)
  {
  }

  /** @todo implement method */
  public void save(AssessmentTemplateData template)
  {
  }

  /** @todo implement method */
  public void saveAssessment(AssessmentIfc assessment)
  {
  }

  /** @todo implement method */
  public void deleteAssessmentTemplate(Long assessmentId)
  {
  }

  /** @todo implement method */
  public void removeAssessment(String assessmentId)
  {
  }

  /** @todo implement method */
  public SectionDataIfc addSection(String assessmentId)
  {
    return null;
  }

  /** @todo implement method */
  public void removeSection(String sectionId)
  {
  }

  /** @todo implement method */
  public SectionDataIfc getSection(String sectionId)
  {
    return null;
  }

  /** @todo implement method */
  public void saveOrUpdateSection(SectionDataIfc section)
  {
  }

  /** @todo implement method */
  public void moveAllItems(String sourceSectionId, String destSectionId)
  {
  }

  /** @todo implement method */
  public void removeAllItems(String sourceSectionId)
  {
  }

  /** @todo implement method */
  public List
    getBasicInfoOfAllActiveAssessmentTemplates(String orderBy)
  {
    return null;
  }

  /** @todo implement method */
  public AssessmentIfc
    createAssessmentWithoutDefaultSection(
      String title, String description, String typeId, String templateId)
  {
    return null;
  }
}