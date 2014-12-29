/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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
