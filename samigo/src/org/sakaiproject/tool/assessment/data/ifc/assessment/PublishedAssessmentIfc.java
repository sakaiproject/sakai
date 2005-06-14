package org.sakaiproject.tool.assessment.data.ifc.assessment;

import java.io.Serializable;

/**
 * @author Rachel Gollub
 * @version 1.0
 */
public interface PublishedAssessmentIfc
    extends Serializable, AssessmentIfc
{
  Long getPublishedAssessmentId();

  void setPublishedAssessmentId(Long publishedAssessmentId);

  AssessmentIfc getAssessment();

  void setAssessment(AssessmentIfc assessment);

  String getOwnerSite();

  void setOwnerSite(String ownerSite);

  String getOwnerSiteId();

}
