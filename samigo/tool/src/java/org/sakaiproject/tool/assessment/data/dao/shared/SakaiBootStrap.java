/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.assessment.data.dao.shared;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.framework.sql.SqlService;

/**
 * This class will be used to initialize the state of the application.
 * 
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon</a>
 * @version $Id$
 */
public class SakaiBootStrap
{
  private static final String SAKAI_SAMIGO_DDL_NAME = "sakai_samigo";

  private static final Log LOG = LogFactory.getLog(SakaiBootStrap.class);

  /** Dependency: SqlService. */
  private SqlService sqlService;

  /** Configuration: to run the ddl on init or not. */
  private boolean autoDdl = false;

  public SakaiBootStrap()
  {
    LOG.debug("new SakaiBootStrap()");

    ; // no behavior
  }

  public void init()
  {
    LOG.info("init()");

    sqlService = org.sakaiproject.service.framework.sql.cover.SqlService
        .getInstance();
    if (sqlService == null)
    {
      LOG.error("SqlService cannot be found!");
      throw new IllegalStateException("SqlService cannot be found!");
    }

    if (autoDdl)
    {
      LOG.debug("autoDdl enabled; running DDL...");
      sqlService.ddl(this.getClass().getClassLoader(), SAKAI_SAMIGO_DDL_NAME);
    }

    LOG.info("init() completed successfully");
  }

  /**
   * Configuration: to run the ddl on init or not.
   * 
   * @param value
   *        the auto ddl value.
   */
  public void setAutoDdl(String value)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("setAutoDdl(String " + value + ")");
    }

    autoDdl = new Boolean(value).booleanValue();
  }

}



