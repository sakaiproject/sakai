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

import org.sakaiproject.tool.assessment.data.ifc.authz.QualifierIfc;
import org.sakaiproject.tool.assessment.data.ifc.authz.QualifierIteratorIfc;

public class QualifierData
  implements QualifierIfc
{
/**
	 * 
	 */
	private static final long serialVersionUID = -8657439480162351350L;
//  private String qualifierId;
  private long qualifierId;
  private String referenceName;
  private String displayName;
  private String description;
  private String qualifierTypeId;
  private QualifierIteratorIfc parentsIter;
  private QualifierIteratorIfc childrenIter;

  /**
   * Creates a new QualifierData object.
   */
  public QualifierData()
  {
  }

  /**
   * Creates a new QualifierData object.
   */
  public QualifierData(
    String qualifierId, String referenceName, String displayName, String description,
    String qualifierTypeId)
  {
//    this.qualifierId = qualifierId;
    this.qualifierId = (new Long(qualifierId)).longValue();
    this.referenceName = referenceName;
    this.displayName = displayName;
    this.description = description;
    this.qualifierTypeId = qualifierTypeId;
  }

  public long getQualifierId()
  {
    return this.qualifierId;
  }

  public void setQualifierId(long id)
  {
    this.qualifierId = id;
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

  public String getQualifierTypeId()
  {
    return this.qualifierTypeId;
  }

  public void setQualifierTypeId(String qualifierTypeId)
  {
    this.qualifierTypeId = qualifierTypeId;
  }

}
