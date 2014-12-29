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
