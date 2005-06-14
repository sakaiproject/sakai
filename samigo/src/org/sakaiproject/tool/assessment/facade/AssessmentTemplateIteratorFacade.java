package org.sakaiproject.tool.assessment.facade;

import java.util.Collection;
import java.util.Iterator;

/**
 * A AssessmentTemplate iterator implementation.
 *
 * @author Rachel Gollub <rgollub@stanford.edu>
 */
public class AssessmentTemplateIteratorFacade
  //implements AssessmentTemplateIterator
{
  private Iterator assessmentTemplateIter;
  private int size = 0;

  /**
   * Creates a new AssessmentTemplateIteratorImpl object.
   *
   * @param passessmentTemplates DOCUMENTATION PENDING
   */
  public AssessmentTemplateIteratorFacade(Collection passessmentTemplates)
  {
    assessmentTemplateIter = passessmentTemplates.iterator();
    this.size = passessmentTemplates.size();
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   *
   * @throws DataFacadeException DOCUMENTATION PENDING
   */
  public boolean hasNextAssessmentTemplate()
    throws DataFacadeException
  {
    try{
      return assessmentTemplateIter.hasNext();
    }
    catch(Exception e){
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
  public AssessmentTemplateFacade nextAssessmentTemplate()
    throws DataFacadeException
  {
    try
    {
      return (AssessmentTemplateFacade) assessmentTemplateIter.next();
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
