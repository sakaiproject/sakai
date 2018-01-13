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

package org.sakaiproject.archive.impl;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.archive.api.ImportMetadata;

/**
 * @author rshastri <a href="mailto:rshastri@iupui.edu ">Rashmi Shastri </a>
 * @version $Id$
 *  
 */
@Slf4j
public class ImportMetadataImpl implements ImportMetadata
{
  private String id;
  private String legacyTool;
  private String sakaiTool;
  private String sakaiServiceName;
  private String fileName;
  private boolean mandatory = false;

  /**
   * Should only be constructed by ImportMetadataService.
   */
  ImportMetadataImpl()
  {
    log.debug("new ImportMetadata()");
  }

  /**
   * @return Returns the id.
   */
  public String getId()
  {
    return id;
  }

  /**
   * @param id
   *          The id to set.
   */
  public void setId(String id)
  {
    this.id = id;
  }

  /**
   * @return Returns the fileName.
   */
  public String getFileName()
  {
    return fileName;
  }

  /**
   * @param fileName
   *          The fileName to set.
   */
  public void setFileName(String fileName)
  {
    this.fileName = fileName;
  }

  /**
   * @return Returns the legacyTool.
   */
  public String getLegacyTool()
  {
    return legacyTool;
  }

  /**
   * @param legacyTool
   *          The legacyTool to set.
   */
  public void setLegacyTool(String legacyTool)
  {
    this.legacyTool = legacyTool;
  }

  /**
   * @return Returns the mandatory.
   */
  public boolean isMandatory()
  {
    return mandatory;
  }

  /**
   * @param mandatory
   *          The mandatory to set.
   */
  public void setMandatory(boolean mandatory)
  {
    this.mandatory = mandatory;
  }

  /**
   * @return Returns the sakaiServiceName.
   */
  public String getSakaiServiceName()
  {
    return sakaiServiceName;
  }

  /**
   * @param sakaiServiceName
   *          The sakaiServiceName to set.
   */
  public void setSakaiServiceName(String sakaiServiceName)
  {
    this.sakaiServiceName = sakaiServiceName;
  }

  /**
   * @return Returns the sakaiTool.
   */
  public String getSakaiTool()
  {
    return sakaiTool;
  }

  /**
   * @param sakaiTool
   *          The sakaiTool to set.
   */
  public void setSakaiTool(String sakaiTool)
  {
    this.sakaiTool = sakaiTool;
  }

}
