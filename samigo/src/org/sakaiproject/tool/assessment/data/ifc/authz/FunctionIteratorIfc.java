package org.sakaiproject.tool.assessment.data.ifc.authz;

public  interface FunctionIteratorIfc
    extends java.io.Serializable
{
  boolean hasNextFunction();

  FunctionIfc nextFunction();
}
