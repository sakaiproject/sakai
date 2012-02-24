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

package org.sakaiproject.tool.assessment.data.dao.authz;

import org.sakaiproject.tool.assessment.data.ifc.authz.FunctionIfc;

public class FunctionData
  implements FunctionIfc
{
  /**
	 * 
	 */
	private static final long serialVersionUID = -8873492307746711610L;
private long functionId;
  private String referenceName;
  private String displayName;
  private String description;
  private String functionTypeId;

  public FunctionData()
  {
  }

  /**
   * Creates a new FunctionData object.
   */
  public FunctionData(
    String functionId, String referenceName, String displayName, String description,
    String functionTypeId)
  {
    //this.functionId = functionId;
    this.functionId = (new Long(functionId)).longValue();
    this.referenceName = referenceName;
    this.displayName = displayName;
    this.description = description;
    this.functionTypeId = functionTypeId;
  }

  public long getFunctionId()
  {
    return this.functionId;
  }

  public void setFunctionId(long id)
  {
    this.functionId = id;
  }

  public String getReferenceName()
  {
    return this.referenceName;
  }

  public void setReferenceName(String referenceName)
  {
    this.referenceName = referenceName;
  }

  public String getDisplayName()
  {
    return this.displayName;
  }

  public void setDisplayName(String display_name)
  {
    this.displayName = display_name;
  }

  public String getDescription()
  {
    return this.description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getFunctionTypeId()
  {
    return this.functionTypeId;
  }

  public void setFunctionTypeId(String functionTypeId)
  {
    this.functionTypeId = functionTypeId;
  }

}
