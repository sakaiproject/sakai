/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/osid/assessment/impl/AssessmentIteratorImpl.java $
 * $Id: AssessmentIteratorImpl.java 9276 2006-05-10 23:04:20Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.osid.assessment.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import org.osid.assessment.Assessment;
import org.osid.assessment.AssessmentException;

public class AssessmentIteratorImpl implements Serializable, org.osid.assessment.AssessmentIterator
{
  private Iterator assessments;

  /**
   * Creates a new AssessmentIteratorImpl object.
   *
   * @param passessments DOCUMENTATION PENDING
   */
  public AssessmentIteratorImpl(Collection assessments)
  {
    this.assessments = assessments.iterator();
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public boolean hasNextAssessment()
    throws AssessmentException
  {
    return assessments.hasNext();
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   *
   * @throws AssessmentException DOCUMENTATION PENDING
   */
  public Assessment nextAssessment()
    throws AssessmentException
  {
    try
    {
      return (Assessment) assessments.next();
    }
    catch(Exception e)
    {
      throw new AssessmentException("No objects to return.");
    }
  }

}
