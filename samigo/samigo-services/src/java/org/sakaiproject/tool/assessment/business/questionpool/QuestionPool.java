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


package org.sakaiproject.tool.assessment.business.questionpool;
import java.io.Serializable;

import org.osid.shared.Id;
import org.osid.shared.Type;

/**
 * This interface provides common methods for accessing a question pool.
 * A question pool is defined as a centralized repository where questions
 * are stored.  They allow one to use the same question on multiple
 * tests without duplicating data, and provide an assessment-independent
 * way to store questions.  This interface is based on the OKI standards,
 * in the hopes it will be adopted as part of the assessment package.
 *
 * @author Rachel Gollub <rgollub@stanford.edu>
 */
public interface QuestionPool
  extends Serializable
{
  /**
   * DOCUMENTATION PENDING
   *
   * @param displayName DOCUMENTATION PENDING
   *
   * @throws QuestionPoolException DOCUMENTATION PENDING
   */
  void updateDisplayName(String displayName)
    throws QuestionPoolException;

  /**
   * DOCUMENTATION PENDING
   *
   * @param description DOCUMENTATION PENDING
   *
   * @throws QuestionPoolException DOCUMENTATION PENDING
   */
  void updateDescription(String description)
    throws QuestionPoolException;

  /**
   * DOCUMENTATION PENDING
   *
   * @param data DOCUMENTATION PENDING
   *
   * @throws QuestionPoolException DOCUMENTATION PENDING
   */
  void updateData(java.io.Serializable data)
    throws QuestionPoolException;

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   *
   * @throws QuestionPoolException DOCUMENTATION PENDING
   */
  String getDisplayName()
    throws QuestionPoolException;

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   *
   * @throws QuestionPoolException DOCUMENTATION PENDING
   */
  String getDescription()
    throws QuestionPoolException;

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   *
   * @throws QuestionPoolException DOCUMENTATION PENDING
   */
  Id getId()
    throws QuestionPoolException;

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   *
   * @throws QuestionPoolException DOCUMENTATION PENDING
   */
  Type getQuestionPoolType()
    throws QuestionPoolException;

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   *
   * @throws QuestionPoolException DOCUMENTATION PENDING
   */
  Serializable getData()
    throws QuestionPoolException;

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   *
   * @throws QuestionPoolException DOCUMENTATION PENDING
   */
  Id getParentId()
    throws QuestionPoolException;

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   *
   * @throws QuestionPoolException DOCUMENTATION PENDING
   */
  QuestionPool getParentPool()
    throws QuestionPoolException;

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   *
   * @throws QuestionPoolException DOCUMENTATION PENDING
   */
  void setParentId(Id parentId)
    throws QuestionPoolException;

}
