package org.sakaiproject.tool.assessment.facade.authz;

import org.sakaiproject.tool.assessment.data.ifc.authz.AuthorizationIteratorIfc;
import org.sakaiproject.tool.assessment.data.ifc.authz.AuthorizationIfc;
import org.sakaiproject.tool.assessment.facade.DataFacadeException;
import java.util.Iterator;
import java.util.Collection;

public class AuthorizationIteratorFacade
  implements AuthorizationIteratorIfc
{
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
