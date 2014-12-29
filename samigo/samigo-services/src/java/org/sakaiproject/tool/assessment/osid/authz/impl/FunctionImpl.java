/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008, 2009 The Sakai Foundation
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

import org.osid.authorization.Function;
import org.osid.shared.Id;
import org.osid.shared.Type;

public class FunctionImpl implements Function {
  private Id id;

private String referenceName;
  private String description;
  private Type functionType;
  private Id qualifierHierarchyId;

  public FunctionImpl() {
  }
  public FunctionImpl(String referenceName, String description,
                      Type functionType, Id qualifierHierarchyId) {
    this.referenceName = referenceName;
    this.description = description;
    this.functionType = functionType;
    this.qualifierHierarchyId = qualifierHierarchyId;
  }
  public Id getId(){
    return id;
  }
  
  public void setId(Id id) {
		this.id = id;
	}
  
  public String getReferenceName() {
    return referenceName;
  }
  public String getDescription(){
    return description;
  }
  public Type getFunctionType(){
    return functionType;
  }
  public Id getQualifierHierarchyId(){
    return qualifierHierarchyId;
  }
  public void updateDescription(String parm1){
    this.description = parm1;
  }

}
