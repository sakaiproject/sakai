/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
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

import org.sakaiproject.tool.assessment.data.ifc.authz.QualifierIfc;
import org.sakaiproject.tool.assessment.data.ifc.authz.QualifierIteratorIfc;
import org.sakaiproject.tool.assessment.facade.DataFacadeException;

public class QualifierIteratorFacade
  implements QualifierIteratorIfc
{
  private Iterator qualifierIter;

  public QualifierIteratorFacade(Collection c)
  {
    qualifierIter = c.iterator();
  }

  public boolean hasNextQualifier()
    throws DataFacadeException
  {
    try{
      return qualifierIter.hasNext();
    }
    catch(Exception e){
      throw new DataFacadeException(e.getMessage());
    }
  }

  public QualifierIfc nextQualifier()
    throws DataFacadeException
  {
    try {
      return (QualifierIfc)qualifierIter.next();
    }
    catch (Exception e) {
      throw new DataFacadeException(e.getMessage());
    }
  }

}
