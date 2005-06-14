package org.sakaiproject.tool.assessment.osid.assessment.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import org.osid.assessment.Assessment;
import org.osid.assessment.AssessmentException;

public class AssessmentIteratorImpl implements Serializable, org.osid.assessment.AssessmentIterator
{
  private Iterator assessments;

  /**
   * Creates a new AssessmentIteratorImpl object.
   *
   * @param passessments DOCUMENTATION PENDING
   */
  public AssessmentIteratorImpl(Collection assessments)
  {
    this.assessments = assessments.iterator();
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public boolean hasNextAssessment()
    throws AssessmentException
  {
    return assessments.hasNext();
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   *
   * @throws AssessmentException DOCUMENTATION PENDING
   */
  public Assessment nextAssessment()
    throws AssessmentException
  {
    try
    {
      return (Assessment) assessments.next();
    }
    catch(Exception e)
    {
      throw new AssessmentException("No objects to return.");
    }
  }

}
