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

import java.io.Serializable;

import org.osid.shared.Id;
import org.osid.shared.Type;
import org.sakaiproject.tool.assessment.business.questionpool.QuestionPool;
import org.sakaiproject.tool.assessment.business.questionpool.QuestionPoolException;

/**
 * This class implements common methods for accessing a question pool.
 * A question pool is defined as a centralized repository where questions
 * are stored.  They allow one to use the same question on multiple
 * tests without duplicating data, and provide an assessment-independent
 * way to store questions.
 *
 * @author Ed Smiley <esmiley@stanford.edu>
 */
public class QuestionPoolImpl
  implements QuestionPool
{
  private String displayName;
  private String description;
  private Id id;
  private Type questionPoolType;
  private Serializable data;
  private Id parentId;
  private QuestionPool parentPool;

  /**
   * Creates a new QuestionPoolImpl object.
   */
  public QuestionPoolImpl()
  {
    // This can hold data until we create an actual object for it.
  }

  /**
   * Constructor.
   * Each question pool has a unique Id object and owns the Id of
   * its parent. See getId(), getParentId()
   *
   * @param newId the id
   * @param newParentId the id of its parent
   */
  public QuestionPoolImpl(Id newId, Id newParentId)
  {
    id = newId;
    parentId = newParentId;
  }


  /**
   *
   * @param pdisplayName the display name for the question pool
   * @throws QuestionPoolException
   */
  public void updateDisplayName(String pdisplayName)
    throws QuestionPoolException
  {
    setDisplayName(displayName);
  }

  public void setDisplayName(String pdisplayName)
    throws QuestionPoolException
  {
    displayName = pdisplayName;
  }

  /**
   *
   * @param pdescription the description for the question pool
   * @throws QuestionPoolException
   */
  public void updateDescription(String pdescription)
    throws QuestionPoolException
  {
    description = pdescription;
  }

  /**
   *
   * @param pdata the extra data member for the question pool
   * @throws QuestionPoolException
   */
  public void updateData(Serializable pdata)
    throws QuestionPoolException
  {
    data = pdata;
  }

  /**
   *
   * @return the display name for the question pool
   * @throws QuestionPoolException
   */
  public String getDisplayName()
    throws QuestionPoolException
  {
    return displayName;
  }

  /**
   *
   * @return the description for the question pool
   * @throws QuestionPoolException
   */
  public String getDescription()
    throws QuestionPoolException
  {
    return description;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   *
   * @throws QuestionPoolException DOCUMENTATION PENDING
   */
  public Id getId()
    throws QuestionPoolException
  {
    return id;
  }

  /**
   *
   * @return the type of pool for the question pool
   * @throws QuestionPoolException
   */
  public Type getQuestionPoolType()
    throws QuestionPoolException
  {
    return questionPoolType;
  }

  // daisyf added this method on 8/20/04 to allow QuestionPoolQueries to function, line 115
  public void updateQuestionPoolType(Type questionPoolType)
    throws QuestionPoolException
  {
    this.questionPoolType = questionPoolType;
  }

  /**
   *
   * @return the extra data for the question pool
   * @throws QuestionPoolException
   */
  public Serializable getData()
    throws QuestionPoolException
  {
    return data;
  }

  /**
   *
   * @return the id object for the question pool
   * @throws QuestionPoolException
   */
  public Id getParentId()
    throws QuestionPoolException
  {
    return parentId;
  }

  /**
   *
   * Sets the parent id object for the question pool
   * @throws QuestionPoolException
   */
  public void setParentId(Id parentId)
    throws QuestionPoolException
  {
    this.parentId = parentId;
  }


  /**
   *
   * @return the parent pool for the question pool
   * @throws QuestionPoolException
   */
  public QuestionPool getParentPool()
    throws QuestionPoolException
  {
    return parentPool;
  }

}
