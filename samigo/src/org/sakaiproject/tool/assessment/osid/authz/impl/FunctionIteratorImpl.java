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
 * @version $Id: FunctionIteratorImpl.java,v 1.1 2004/10/21 17:10:37 daisyf.stanford.edu Exp $
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
