package org.sakaiproject.tool.assessment.osid.authz.impl;

import java.util.Iterator;
import java.util.Collection;

import org.osid.authorization.Authorization;
import org.osid.authorization.AuthorizationException;

public class AuthorizationIteratorImpl
 implements org.osid.authorization.AuthorizationIterator
{
  private Iterator authorizationIter;

  public AuthorizationIteratorImpl(Collection authorizations)
  {
    this.authorizationIter = authorizations.iterator();
  }

  public boolean hasNextAuthorization()
    throws AuthorizationException
  {
    try{
      return authorizationIter.hasNext();
    }
    catch(Exception e){
      throw new AuthorizationException(e.getMessage());
    }
  }

  public Authorization nextAuthorization()
    throws AuthorizationException
  {
    try{
      return (Authorization) authorizationIter.next();
    }
    catch(Exception e){
      throw new AuthorizationException(e.getMessage());
    }
  }

  public void remove()
    throws AuthorizationException
  {
    try{
      authorizationIter.remove();
    }
    catch(Exception e){
      throw new AuthorizationException(e.getMessage());
    }
  }

}
