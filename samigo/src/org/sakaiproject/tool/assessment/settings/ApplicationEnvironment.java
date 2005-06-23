/**********************************************************************************
* $HeaderURL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.assessment.settings;

//import org.navigoproject.AbstractCompositeObject;

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
  private static final org.apache.log4j.Logger LOG =
    org.apache.log4j.Logger.getLogger(ApplicationEnvironment.class);
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
    LOG.debug("getInstance()");

    return _INSTANCE;
  }

  /**
   * Creates a new ApplicationEnvironment object.
   */
  private ApplicationEnvironment()
  {
    LOG.debug("new ApplicationEnvironment()");
    PathInfo pathInfo = PathInfo.getInstance();
    String pathToSecurity = pathInfo.getBasePathToSecurity();
    if(LOG.isDebugEnabled())
    {
      LOG.debug("pathToSecurity=" + pathToSecurity);
    }

    if((pathToSecurity == null) || (pathToSecurity.length() < 1))
    {
      throw new IllegalStateException(
        "PathInfo has not been initialized or is invalid");
    }

    pathToSecurity = pathToSecurity.toUpperCase();
    if(pathToSecurity.endsWith("DEV"))
    {
      LOG.debug("pathToSecurity.endsWith(\"DEV\")");
      this.development = true;
      this.production = false;

      return;
    }

    if(pathToSecurity.endsWith("TST"))
    {
      LOG.debug("pathToSecurity.endsWith(\"TST\")");
      this.testing = true;
      this.production = false;

      return;
    }

    if(pathToSecurity.endsWith("REG"))
    {
      LOG.debug("pathToSecurity.endsWith(\"REG\")");
      this.regression = true;
      this.production = false;

      return;
    }

    if(pathToSecurity.endsWith("STG"))
    {
      LOG.debug("pathToSecurity.endsWith(\"STG\")");
      this.staging = true;
      this.production = false;

      return;
    }

    if(pathToSecurity.endsWith("TRN"))
    {
      LOG.debug("pathToSecurity.endsWith(\"TRN\")");
      this.training = true;
      this.production = false;

      return;
    }

    if(pathToSecurity.endsWith("PRD"))
    {
      LOG.debug("pathToSecurity.endsWith(\"PRD\")");
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
