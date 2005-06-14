package org.sakaiproject.tool.assessment.facade;

import java.util.Collection;
import java.util.Iterator;

import org.osid.shared.SharedException;

/**
 * A QuestionPool iterator implementation.
 *
 * @author Rachel Gollub <rgollub@stanford.edu>
 */
public class QuestionPoolIteratorFacade
{
  private Iterator questionPools;
  private int size = 0;

  /**
   * Creates a new QuestionPoolIteratorImpl object.
   *
   * @param pquestionPools DOCUMENTATION PENDING
   */
  public QuestionPoolIteratorFacade(Collection pquestionPools)
  {
    questionPools = pquestionPools.iterator();
    this.size = pquestionPools.size();
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   *
   * @throws SharedException DOCUMENTATION PENDING
   */
  public boolean hasNext()
    throws SharedException
  {
    return questionPools.hasNext();
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   *
   * @throws SharedException DOCUMENTATION PENDING
   */
  public QuestionPoolFacade next()
    throws SharedException
  {
    try
    {
      return (QuestionPoolFacade) questionPools.next();
    }
    catch(Exception e)
    {
      throw new SharedException("No objects to return.");
    }
  }

  public int getSize(){
    return size;
  }

}
