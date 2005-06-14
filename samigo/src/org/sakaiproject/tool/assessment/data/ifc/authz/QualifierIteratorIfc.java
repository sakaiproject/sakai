package org.sakaiproject.tool.assessment.data.ifc.authz;

public  interface QualifierIteratorIfc
    extends java.io.Serializable
{
  boolean hasNextQualifier();

  QualifierIfc nextQualifier();
}
