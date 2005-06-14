package org.sakaiproject.tool.assessment.data.ifc.authz;

public  interface QualifierIfc
    extends java.io.Serializable
{
//  String getQualifierId();
  long getQualifierId();

  //void setQualifierId(String id);
  void setQualifierId(long id);

  String getReferenceName();

  void setReferenceName(String referenceName);

  String getDisplayName();

  void setDisplayName(String displayName);

  String getDescription();

  void setDescription(String description);

  String getQualifierTypeId();

  void setQualifierTypeId(String qualifierTypeId);

}
