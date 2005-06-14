package org.sakaiproject.tool.assessment.facade;

import java.util.Collection;
import java.util.Iterator;

/**
 * A Assessment iterator implementation.
 *
 * @author Rachel Gollub <rgollub@stanford.edu>
 */
public class AssessmentIteratorFacade
  //implements AssessmentIterator
{
  private Iterator assessmentIter;
  private int size = 0;

  /**
   * Creates a new AssessmentIteratorImpl object.
   *
   * @param passessments DOCUMENTATION PENDING
   */
  public AssessmentIteratorFacade(Collection passessments)
  {
    assessmentIter = passessments.iterator();
    this.size = passessments.size();
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   *
   * @throws DataFacadeException DOCUMENTATION PENDING
   */
  public boolean hasNextAssessment()
    throws DataFacadeException
  {
    try {
      return assessmentIter.hasNext();
    }
    catch (Exception e) {
      throw new DataFacadeException("No objects to return.");
    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   *
   * @throws DataFacadeException DOCUMENTATION PENDING
   */
  public AssessmentFacade nextAssessment()
    throws DataFacadeException
  {
    try
    {
      return (AssessmentFacade) assessmentIter.next();
    }
    catch(Exception e)
    {
      throw new DataFacadeException("No objects to return.");
    }
  }

  public int getSize(){
    return size;
  }

}
