/**********************************************************************************
* $URL$
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
package org.sakaiproject.tool.assessment.osid.assessment.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osid.assessment.AssessmentException;
import org.osid.assessment.Section;

public class SectionIteratorImpl
  implements org.osid.assessment.SectionIterator
{
  private static Log log = LogFactory.getLog(SectionIteratorImpl.class);
  private Iterator sectionIterator;

  private Iterator sectionIter;

  /**
   * Creates a new ItemIteratorImpl object.
   *
   * @param pitems DOCUMENTATION PENDING
   */
  public SectionIteratorImpl(Set sectionSet)
  {
    this.sectionIter = sectionSet.iterator();
  }

  public SectionIteratorImpl(List sectionList)
  {
    this.sectionIter = sectionList.iterator();
  }

  /* (non-Javadoc)
   * @see osid.assessment.SectionIterator#hasNext()
   */
  public boolean hasNextSection()
    throws AssessmentException
  {
    return sectionIter.hasNext();
  }

  /* (non-Javadoc)
   * @see osid.assessment.SectionIterator#next()
   */
  public Section nextSection()
    throws AssessmentException
  {
    return (Section)sectionIter.next();
  }

}
