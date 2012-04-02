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
