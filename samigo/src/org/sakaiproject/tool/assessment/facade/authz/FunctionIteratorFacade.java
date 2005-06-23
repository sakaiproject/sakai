/**********************************************************************************
* $HeadURL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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
