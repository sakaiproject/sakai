/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/


package org.sakaiproject.tool.assessment.facade.authz;

import java.util.Date;

import org.osid.authorization.Function;
import org.osid.authorization.Qualifier;
import org.osid.shared.Id;

import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.assessment.data.ifc.authz.AuthorizationIfc;

public class AuthorizationFacade
    implements AuthorizationIfc {
  /**
	 * 
	 */
	private static final long serialVersionUID = -262696599577971352L;
private Id agentId;
  private Function function;
  private Qualifier qualifier;
  private long effectiveDate;
  private long expirationDate;
  private Id modifiedBy;
  private long modifiedDate;
  private boolean isExplicit;
  private boolean isActiveNow;

  private AuthorizationIfc data;
  private String functionId;
  private String qualifierId;
  private Date authorizationEffectiveDate;
  private Date authorizationExpirationDate;
  private String agentIdString;
  private String lastModifiedBy;
  private Date lastModifiedDate;
  private Boolean isExplicitBoolean;
  private Boolean isActiveNowBoolean;

  public AuthorizationFacade() {
    this.data = new AuthorizationData(); //should do a create method later
  }

  public AuthorizationFacade(
      String agentIdString, String functionId, String qualifierId,
      Date authorizationEffectiveDate, Date authorizationExpirationDate,
      String lastModifiedBy, Date lastModifiedDate,
      Boolean isExplicitBoolean) {
    this.data = new AuthorizationData(); //should do a create method later
    setAgentIdString(agentIdString);
    setFunctionId(functionId);
    setQualifierId(qualifierId);
    setAuthorizationEffectiveDate(authorizationEffectiveDate);
    setAuthorizationExpirationDate(authorizationExpirationDate);
    setLastModifiedBy(lastModifiedBy);
    setLastModifiedDate(lastModifiedDate);
    setIsExplicitBoolean(isExplicitBoolean);
  }

  public AuthorizationFacade(AuthorizationIfc data){
    this.data = data;
    this.agentIdString = data.getAgentIdString();
    this.functionId = data.getFunctionId();
    this.qualifierId = data.getQualifierId();
    this.authorizationEffectiveDate = data.getAuthorizationEffectiveDate();
    this.authorizationExpirationDate = data.getAuthorizationExpirationDate();
    this.lastModifiedBy = data.getLastModifiedBy();
    this.lastModifiedDate = data.getLastModifiedDate();
    this.isExplicitBoolean = data.getIsExplicitBoolean();
  }


  public Id getAgentId() {
    return null;
  }

  public String getAgentIdString()
  {
    return data.getAgentIdString();
  }

  public void setAgentIdString(String id)
  {
    this.agentIdString = id;
    data.setAgentIdString(id);
  }

  public Function getFunction() {
    return null;
  }

  public Qualifier getQualifier() {
    return null;
  }

  public long getEffectiveDate() {
    Date d = data.getAuthorizationEffectiveDate();
    if ( d!=null )
     return d.getTime();
    else
      return 0;
  }

  public long getExpirationDate() {
    Date d = data.getAuthorizationExpirationDate();
    if ( d!= null )
     return d.getTime();
    else
      return 0;
  }

  public Id getModifiedBy() {
    return null;
  }

  public long getModifiedDate() {
    Date d = data.getLastModifiedDate();
    if ( d!= null )
     return d.getTime();
    else
      return 0;

  }

  public void updateExpirationDate(long expirationDate) {
    this.expirationDate = expirationDate;
    setAuthorizationExpirationDate(new Date(expirationDate));
  }

  public void updateEffectiveDate(long effectiveDate) {
    this.effectiveDate = effectiveDate;
    setAuthorizationEffectiveDate(new Date(effectiveDate));
  }

  public boolean getIsExplicit() {
    return data.getIsExplicitBoolean().booleanValue();
  }

  public void setIsExplicit(boolean type) {
    this.isExplicit = type;
    data.setIsExplicitBoolean(Boolean.valueOf(type));
  }


  public boolean isActiveNow() {
    int effectiveVal = (getEffectiveDate() == 0) ? 0 : 1;
    int expirationVal = (getExpirationDate() == 0) ? 0 : 2;

    // current time in ms
    long nowMillis = (new Date()).getTime();
    boolean returnVal = false;

    switch(effectiveVal + expirationVal)
    {
      case 0: // both are 0
        returnVal = true;
        break;

      case 1: // effectiveDate is not 0
        if(nowMillis > getEffectiveDate())
          returnVal = true;
        else
          returnVal = false;

        break;
      case 2: // expirationDate is not null
        if(nowMillis < getExpirationDate())
          returnVal = true;
        else
          returnVal = false;

        break;

      case 3: // both effectiveDate and expirationDate are not null
        if((nowMillis > getEffectiveDate()) && (nowMillis < getExpirationDate()))
          returnVal = true;
        else
          returnVal = false;
    }
    return returnVal;
  }

  public AuthorizationIfc getData(){
    return this.data;
  }

  public void setAgentId(String id) {
  }

  public String getFunctionId() {
    return data.getFunctionId();
  }

  public void setFunctionId(String id) {
    this.functionId = id;
    data.setFunctionId(id);
  }

  public String getQualifierId() {
    return data.getQualifierId();
  }

  public void setQualifierId(String id) {
    this.qualifierId = id;
    data.setQualifierId(id);
  }

  public void setAuthorizationEffectiveDate(Date cal) {
    this.authorizationEffectiveDate = cal;
    data.setAuthorizationEffectiveDate(cal);
    if (cal != null)
      this.effectiveDate = cal.getTime();
  }

  public void setAuthorizationExpirationDate(Date cal) {
    this.authorizationExpirationDate = cal;
    data.setAuthorizationExpirationDate(cal);
    if (cal != null)
      this.expirationDate = cal.getTime();
  }

  public String getLastModifiedBy() {
    return data.getLastModifiedBy();
  }

  public void setLastModifiedBy(String id) {
    this.lastModifiedBy = id;
    data.setLastModifiedBy(id);
  }

  public Date getLastModifiedDate() {
    return data.getLastModifiedDate();
  }

  public void setLastModifiedDate(Date cal) {
    this.lastModifiedDate = cal;
    data.setLastModifiedDate(cal);
  }

  public Boolean getIsExplicitBoolean() {
    return data.getIsExplicitBoolean();
  }

  public void setIsExplicitBoolean(Boolean type) {
    this.isExplicitBoolean = type;
    data.setIsExplicitBoolean(type);
  }

  public void setIsActiveNowBoolean(Boolean isActiveNowBoolean) {
  }

  public Boolean getIsActiveNowBoolean() {
    return data.getIsActiveNowBoolean();
  }

  public Date getAuthorizationEffectiveDate() {
    return data.getAuthorizationEffectiveDate();
  }

  public Date getAuthorizationExpirationDate() {
    return data.getAuthorizationExpirationDate();
  }
}
