package org.sakaiproject.tool.assessment.facade.authz;

import java.util.Collection;
import java.util.Iterator;

import org.sakaiproject.tool.assessment.data.ifc.authz.FunctionIfc;
import org.sakaiproject.tool.assessment.data.ifc.authz.FunctionIteratorIfc;
import org.sakaiproject.tool.assessment.facade.DataFacadeException;

/**
 * DOCUMENT ME!
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
