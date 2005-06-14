package org.sakaiproject.tool.assessment.data.ifc.assessment;

import java.io.Serializable;
import java.util.ArrayList;
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

}
