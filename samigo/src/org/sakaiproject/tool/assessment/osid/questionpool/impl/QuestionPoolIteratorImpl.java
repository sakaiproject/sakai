package org.sakaiproject.tool.assessment.osid.questionpool.impl;

import java.util.Collection;
import java.util.Iterator;

import org.osid.shared.SharedException;
import org.sakaiproject.tool.assessment.business.questionpool.QuestionPool;
import org.sakaiproject.tool.assessment.business.questionpool.QuestionPoolIterator;

/**
 * A QuestionPool iterator implementation.
 *
 * @author Rachel Gollub <rgollub@stanford.edu>
 */
public class QuestionPoolIteratorImpl
  implements QuestionPoolIterator
{
  private Iterator questionPools;

  /**
   * Creates a new QuestionPoolIteratorImpl object.
   *
   * @param pquestionPools DOCUMENTATION PENDING
   */
  public QuestionPoolIteratorImpl(Collection pquestionPools)
  {
    questionPools = pquestionPools.iterator();
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
  public QuestionPool next()
    throws SharedException
  {
    try
    {
      return (QuestionPool) questionPools.next();
    }
    catch(Exception e)
    {
      throw new SharedException("No objects to return.");
    }
  }
}
