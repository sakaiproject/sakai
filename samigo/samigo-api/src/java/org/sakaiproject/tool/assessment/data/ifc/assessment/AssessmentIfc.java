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



package org.sakaiproject.tool.assessment.data.ifc.assessment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Rachel Gollub
 * @version 1.0
 */
public interface AssessmentIfc
    extends Serializable, AssessmentBaseIfc
{

  Long getAssessmentId();

  Long getAssessmentTemplateId();

  void setAssessmentTemplateId(Long assessmentTemplateId);

  Set getSectionSet();

  void setSectionSet(Set sectionSet);

  SectionDataIfc getSection(Long sequence);

  SectionDataIfc getDefaultSection();

  ArrayList getSectionArray();

  ArrayList getSectionArraySorted();

  Set getAssessmentAttachmentSet();

  void setAssessmentAttachmentSet(Set assessmentAttachmentSet);

  List getAssessmentAttachmentList();

  String getHasMetaDataForQuestions();
}
