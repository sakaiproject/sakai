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

import org.sakaiproject.tool.assessment.data.ifc.authz.FunctionIfc;
import org.sakaiproject.tool.assessment.data.ifc.authz.FunctionIteratorIfc;
import org.sakaiproject.tool.assessment.facade.DataFacadeException;

/**
 *
 * @author <a href="mailto:rpembry@indiana.edu">Randall P. Embry</a>
 * @author <a href="mailto:jlannan@iupui.edu">Jarrod Lannan</a>
 * @version $Id$
 */
public class FunctionIteratorFacade
  implements FunctionIteratorIfc
{
  private Iterator functionIter;
  public FunctionIteratorFacade(Collection functions)
    throws DataFacadeException
  {
    try{
      this.functionIter = functions.iterator();
    }
    catch(Exception e){
      throw new DataFacadeException(e.getMessage());
    }
  }

  public boolean hasNextFunction()
    throws DataFacadeException
  {
    try{
      return functionIter.hasNext();
    }
    catch(Exception e){
      throw new DataFacadeException(e.getMessage());
    }
  }

  public FunctionIfc nextFunction()
    throws DataFacadeException
  {
    try{
      return (FunctionIfc) functionIter.next();
    }
    catch(Exception e){
      throw new DataFacadeException(e.getMessage());
    }
  }
}
