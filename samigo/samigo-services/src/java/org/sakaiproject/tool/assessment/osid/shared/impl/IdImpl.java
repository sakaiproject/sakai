/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.osid.shared.impl;

import org.osid.shared.Id;

public class IdImpl implements Id {
  private String id;

  public IdImpl(String id){
    this.id = id;
  }

  public IdImpl(Long internalId){
    this.id = internalId.toString();
  }

  public IdImpl(long internalId) {
    this.id = Long.toString(internalId);
  }

  public String getIdString() throws org.osid.shared.SharedException{
    return id;
  }
  public boolean isEqual(Id pId) throws org.osid.shared.SharedException {
    if (pId.getIdString().equals(getIdString()))
      return true;
    else
      return false;
  }

}
