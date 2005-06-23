/**********************************************************************************
* $HeadURL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
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
