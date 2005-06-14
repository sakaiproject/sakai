package org.sakaiproject.tool.assessment.data.ifc.authz;

public  interface AuthorizationIteratorIfc
    extends java.io.Serializable
{
  boolean hasNextAuthorization();

  AuthorizationIfc nextAuthorization();
}
