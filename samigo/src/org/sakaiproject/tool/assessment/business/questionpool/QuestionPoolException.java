package org.sakaiproject.tool.assessment.business.questionpool;

import org.osid.OsidException;

/**
 * This class provides a specific QuestionPool exception.
 *
 * @author Rachel Gollub <rgollub@stanford.edu>
 */
public class QuestionPoolException
  extends OsidException
{
  /**
   * Creates a new QuestionPoolException object.
   *
   * @param message DOCUMENTATION PENDING
   */
  public QuestionPoolException(String message)
  {
    super(message);
  }
}
