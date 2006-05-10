/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/


package org.sakaiproject.tool.assessment.settings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Title: NavigoProject.org</p>
 * <p>Description: OKI based implementation</p>
 * <p>Copyright: Copyright 2003 Trustees of Indiana University</p>
 * <p>Company: </p>
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon</a>
 * @version $Id$
 */
public class ApplicationEnvironment
    //  extends AbstractCompositeObject
{
      private static Log log = LogFactory.getLog(ApplicationEnvironment.class);
  private static ApplicationEnvironment _INSTANCE =
    new ApplicationEnvironment();
  private boolean development = false;
  private boolean testing = false;
  private boolean regression = false;
  private boolean staging = false;
  private boolean training = false;
  private boolean production = true; // favor secure production (safe than sorry)

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public static ApplicationEnvironment getInstance()
  {
    log.debug("getInstance()");

    return _INSTANCE;
  }

  /**
   * Creates a new ApplicationEnvironment object.
   */
  private ApplicationEnvironment()
  {
    log.debug("new ApplicationEnvironment()");
    PathInfo pathInfo = PathInfo.getInstance();
    String pathToSecurity = pathInfo.getBasePathToSecurity();
    if(log.isDebugEnabled())
    {
      log.debug("pathToSecurity=" + pathToSecurity);
    }

    if((pathToSecurity == null) || (pathToSecurity.length() < 1))
    {
      throw new IllegalStateException(
        "PathInfo has not been initialized or is invalid");
    }

    pathToSecurity = pathToSecurity.toUpperCase();
    if(pathToSecurity.endsWith("DEV"))
    {
      log.debug("pathToSecurity.endsWith(\"DEV\")");
      this.development = true;
      this.production = false;

      return;
    }

    if(pathToSecurity.endsWith("TST"))
    {
      log.debug("pathToSecurity.endsWith(\"TST\")");
      this.testing = true;
      this.production = false;

      return;
    }

    if(pathToSecurity.endsWith("REG"))
    {
      log.debug("pathToSecurity.endsWith(\"REG\")");
      this.regression = true;
      this.production = false;

      return;
    }

    if(pathToSecurity.endsWith("STG"))
    {
      log.debug("pathToSecurity.endsWith(\"STG\")");
      this.staging = true;
      this.production = false;

      return;
    }

    if(pathToSecurity.endsWith("TRN"))
    {
      log.debug("pathToSecurity.endsWith(\"TRN\")");
      this.training = true;
      this.production = false;

      return;
    }

    if(pathToSecurity.endsWith("PRD"))
    {
      log.debug("pathToSecurity.endsWith(\"PRD\")");
      this.production = true;

      return;
    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public boolean isDevelopment()
  {
    return development;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public boolean isTesting()
  {
    return testing;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public boolean isRegression()
  {
    return regression;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public boolean isStaging()
  {
    return staging;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public boolean isProduction()
  {
    return production;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public boolean isTraining()
  {
    return training;
  }
}
