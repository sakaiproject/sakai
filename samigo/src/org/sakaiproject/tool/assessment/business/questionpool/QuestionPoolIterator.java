package org.sakaiproject.tool.assessment.business.questionpool;

import org.osid.shared.SharedException;

/**
 * This is an OKI-style iterator interface for QuestionPools.
 *
 * @author Rachel Gollub <rgollub@stanford.edu>
 */
public interface QuestionPoolIterator
  extends java.io.Serializable
{
  /**
   * Return true if there are more pools, false if there are not.
   */
  boolean hasNext()
    throws SharedException;

  /**
   * Return the next pool.
   */
  QuestionPool next()
    throws SharedException;
}
