package org.sakaiproject.tool.assessment.data.ifc.authz;

public  interface FunctionIfc
    extends java.io.Serializable
{
  //String getFunctionId();
  long getFunctionId();

  //void setFunctionId(String id);
  void setFunctionId(long id);

  String getReferenceName();

  void setReferenceName(String referenceName);

  String getDisplayName();

  void setDisplayName(String displayName);

  String getDescription();

  void setDescription(String description);

  String getFunctionTypeId();

  void setFunctionTypeId(String functionTypeId);

}
