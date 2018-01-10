/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.shared;

import java.io.File;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.db.api.SqlService;

/**
 * This class will be used to initialize the state of the application.
 * 
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon</a>
 * @version $Id$
 */
@Slf4j
public class SakaiBootStrap
{
  private static final String SAKAI_SAMIGO_DDL_NAME = "sakai_samigo";
  
  private static final String SQL_UPDATE_SCRIPT_NAME = "sakai_samigo_post_schema_update";
  
  private static final String SAKAI_AUTO_DDL_PROPERTY = "auto.ddl";

  /** Dependency: SqlService. */
  private SqlService sqlService;

  /** Configuration: to run the ddl on init or not. */
  private boolean autoDdl = false;

  public SakaiBootStrap()
  {
    log.debug("new SakaiBootStrap()");

    ; // no behavior
  }

  public void init()
  {

    autoDdl = ServerConfigurationService.getBoolean(SAKAI_AUTO_DDL_PROPERTY, autoDdl);

    sqlService = org.sakaiproject.db.cover.SqlService
        .getInstance();
    if (sqlService == null)
    {
      log.error("SakaiBootStrap.init(): SqlService cannot be found!");
      throw new IllegalStateException("SqlService cannot be found!");
    }

    if (autoDdl)
    {
      log.info("SakaiBootStrap.init(): autoDdl enabled; running DDL...");

      // Don't take down the entire instance if this update script fails!
      // This update script makes sure the Oracle MEDIA column uses a blob 
      // and also makes sure indexes are created. As soon as one update fails,
      // the entire script fails.
      try {
        sqlService.ddl(this.getClass().getClassLoader(), SQL_UPDATE_SCRIPT_NAME);
      }
      catch (Throwable t) {
        log.warn("SakaiBootStrap.init(): ", t);
      }

      // Don't take down the entire instance if this series of inserts fails!
      try {
        sqlService.ddl(this.getClass().getClassLoader(), SAKAI_SAMIGO_DDL_NAME);
      }
      catch (Throwable t) {
        log.warn("SakaiBootStrap.init(): ", t);
      }

    } else {
      log.debug("****autoDdl disabled.");
    }

    String uploadPath = ServerConfigurationService.getString("samigo.answerUploadRepositoryPath", "${sakai.home}/samigo/answerUploadRepositoryPath/");

    if(uploadPath != null)
    {
        File samigoDir = new File(uploadPath);

        if(!samigoDir.exists())
        {
            log.info(samigoDir + " doesn't exist. Creating it now ...");
            if(samigoDir.mkdirs())
            {
                log.info(samigoDir + " created.");
            }
            else
            {
                log.error("samigo.answerUploadRepositoryPath was not set. No Samigo upload folder has been created.");
            }
        }
        else
        {
            log.info(samigoDir + " exists. It will not be recreated.");
        }
    }
    else
    {
        log.error("samigo.answerUploadRepositoryPath was not set. No Samigo upload folder has been created.");
    }
  }

  /**
   * Configuration: to run the ddl on init or not.
   * 
   * @param value
   *        the auto ddl value.
   */
  public void setAutoDdl(String value)
  {
    if (log.isDebugEnabled())
    {
      log.debug("setAutoDdl(String " + value + ")");
    }

    //autoDdl = new Boolean(value).booleanValue();
    if (("true").equals(value)){
	autoDdl = true;
    }
  }

}
