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
import org.osid.authorization.Function;
import org.osid.authorization.FunctionIterator;

/**
 * DOCUMENT ME!
 *
 * @author <a href="mailto:rpembry@indiana.edu">Randall P. Embry</a>
 * @author <a href="mailto:jlannan@iupui.edu">Jarrod Lannan</a>
 * @version $Id$
 */
public class FunctionIteratorImpl
  implements FunctionIterator
{
  private Iterator functionIter;
  public FunctionIteratorImpl(Collection functions)
  {
    this.functionIter = functions.iterator();
  }

  public boolean hasNextFunction()
    throws AuthorizationException
  {
    try{
      return functionIter.hasNext();
    }
    catch(Exception e){
      throw new AuthorizationException(e.getMessage());
    }
  }

  public Function nextFunction()
    throws AuthorizationException
  {
    try{
      return (Function) functionIter.next();
    }
    catch(Exception e){
      throw new AuthorizationException(e.getMessage());
    }
  }
}
