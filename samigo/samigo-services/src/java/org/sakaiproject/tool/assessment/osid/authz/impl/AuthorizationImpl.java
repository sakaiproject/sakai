/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.osid.authz.impl;

import java.util.Date;

import org.osid.authorization.Authorization;
import org.osid.authorization.Function;
import org.osid.authorization.Qualifier;
import org.osid.shared.Id;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class AuthorizationImpl implements Authorization {
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Id agentId;
  private Function function;
  private Qualifier qualifier;
  private long effectiveDate;
  private long expirationDate;
  private Id modifiedBy;
  private long modifiedDate;
  //private boolean isExplicit;

  public AuthorizationImpl() {
  }

  public Id getAgentId() {
    return agentId;
  }

  public Function getFunction() {
    return function;
  }

  public Qualifier getQualifier() {
    return qualifier;
  }

  public long getEffectiveDate() {
    return effectiveDate;
  }

  public long getExpirationDate() {
    return expirationDate;
  }

  public Id getModifiedBy() {
    return modifiedBy;
  }

  public long getModifiedDate() {
    return modifiedDate;
  }


  public boolean isExplicit() {
    return false;
  }

  public void updateExpirationDate(long long0) {
  }

  public void updateEffectiveDate(long long0) {
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
}
