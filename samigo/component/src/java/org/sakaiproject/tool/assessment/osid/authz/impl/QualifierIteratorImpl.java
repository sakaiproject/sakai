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
package org.sakaiproject.tool.assessment.osid.authz.impl;

import java.util.Iterator;
import java.util.Collection;

import org.osid.authorization.AuthorizationException;
import org.osid.authorization.Qualifier;
import org.osid.authorization.QualifierIterator;

/**
 * DOCUMENT ME!
 *
 * @author <a href="mailto:jlannan@iupui.edu">Jarrod Lannan</a>
 * @version $Id$
 */
public class QualifierIteratorImpl
  implements QualifierIterator
{
  private Iterator qualifierIter;

  /**
   * DOCUMENT ME!
   *
   * @param i
   */
  public QualifierIteratorImpl(Collection c)
  {
    qualifierIter = c.iterator();
  }

  /* (non-Javadoc)
   * @see osid.authorization.QualifierIterator#hasNext()
   */
  public boolean hasNextQualifier()
    throws AuthorizationException
  {
    try{
      return qualifierIter.hasNext();
    }
    catch(Exception e){
      throw new AuthorizationException(e.getMessage());
    }
  }

  /* (non-Javadoc)
   * @see osid.authorization.QualifierIterator#next()
   */
  public Qualifier nextQualifier()
    throws AuthorizationException
  {
    try {
      return (Qualifier)qualifierIter.next();
    }
    catch (Exception e) {
      throw new AuthorizationException(e.getMessage());
    }
  }

}
