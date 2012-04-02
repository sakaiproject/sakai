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

package org.sakaiproject.tool.assessment.facade.authz;

import java.util.Collection;
import java.util.Iterator;

import org.sakaiproject.tool.assessment.data.ifc.authz.AuthorizationIfc;
import org.sakaiproject.tool.assessment.data.ifc.authz.AuthorizationIteratorIfc;
import org.sakaiproject.tool.assessment.facade.DataFacadeException;

public class AuthorizationIteratorFacade
  implements AuthorizationIteratorIfc
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 941272870128424454L;
private Iterator authorizationIter;

  public AuthorizationIteratorFacade(Collection c)
  {
    authorizationIter = c.iterator();
  }

  public boolean hasNextAuthorization()
    throws DataFacadeException
  {
    try{
      return authorizationIter.hasNext();
    }
    catch(Exception e){
      throw new DataFacadeException(e.getMessage());
    }
  }

  public AuthorizationIfc nextAuthorization()
    throws DataFacadeException
  {
    try {
      return (AuthorizationIfc)authorizationIter.next();
    }
    catch (Exception e) {
      throw new DataFacadeException(e.getMessage());
    }
  }

}
