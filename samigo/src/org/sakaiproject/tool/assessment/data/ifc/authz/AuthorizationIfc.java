package org.sakaiproject.tool.assessment.data.ifc.authz;

import java.util.Date;

public  interface AuthorizationIfc
    extends java.io.Serializable
{

  String getAgentIdString();

  void setAgentIdString(String id);

  String getFunctionId();

  void setFunctionId(String id);

  String getQualifierId();

  void setQualifierId(String id);

  Date getAuthorizationEffectiveDate();

  void setAuthorizationEffectiveDate(Date cal);

  Date getAuthorizationExpirationDate();

  void setAuthorizationExpirationDate(Date cal);

  String getLastModifiedBy();

  void setLastModifiedBy(String id);

  Date getLastModifiedDate();

  void setLastModifiedDate(Date cal);

  Boolean getIsExplicitBoolean();

  void setIsExplicitBoolean(Boolean type);

  Boolean getIsActiveNowBoolean();

}
