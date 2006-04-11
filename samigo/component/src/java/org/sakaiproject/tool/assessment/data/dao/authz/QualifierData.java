/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
package org.sakaiproject.tool.assessment.data.dao.authz;

import org.sakaiproject.tool.assessment.data.ifc.authz.QualifierIfc;
import org.sakaiproject.tool.assessment.data.ifc.authz.QualifierIteratorIfc;

public class QualifierData
  implements QualifierIfc
{
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
