/*
 *                       Navigo Software License
 *
 * Copyright 2003, Trustees of Indiana University, The Regents of the University
 * of Michigan, and Stanford University, all rights reserved.
 *
 * This work, including software, documents, or other related items (the
 * "Software"), is being provided by the copyright holder(s) subject to the
 * terms of the Navigo Software License. By obtaining, using and/or copying this
 * Software, you agree that you have read, understand, and will comply with the
 * following terms and conditions of the Navigo Software License:
 *
 * Permission to use, copy, modify, and distribute this Software and its
 * documentation, with or without modification, for any purpose and without fee
 * or royalty is hereby granted, provided that you include the following on ALL
 * copies of the Software or portions thereof, including modifications or
 * derivatives, that you make:
 *
 *    The full text of the Navigo Software License in a location viewable to
 *    users of the redistributed or derivative work.
 *
 *    Any pre-existing intellectual property disclaimers, notices, or terms and
 *    conditions. If none exist, a short notice similar to the following should
 *    be used within the body of any redistributed or derivative Software:
 *    "Copyright 2003, Trustees of Indiana University, The Regents of the
 *    University of Michigan and Stanford University, all rights reserved."
 *
 *    Notice of any changes or modifications to the Navigo Software, including
 *    the date the changes were made.
 *
 *    Any modified software must be distributed in such as manner as to avoid
 *    any confusion with the original Navigo Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 *
 * The name and trademarks of copyright holder(s) and/or Indiana University,
 * The University of Michigan, Stanford University, or Navigo may NOT be used
 * in advertising or publicity pertaining to the Software without specific,
 * written prior permission. Title to copyright in the Software and any
 * associated documentation will at all times remain with the copyright holders.
 * The export of software employing encryption technology may require a specific
 * license from the United States Government. It is the responsibility of any
 * person or organization contemplating export to obtain such a license before
 * exporting this Software.
 */

package org.sakaiproject.tool.assessment.settings;

//import org.navigoproject.AbstractCompositeObject;

/**
 * <p>Title: NavigoProject.org</p>
 * <p>Description: OKI based implementation</p>
 * <p>Copyright: Copyright 2003 Trustees of Indiana University</p>
 * <p>Company: </p>
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon</a>
 * @version $Id: ApplicationEnvironment.java,v 1.1 2005/04/12 15:37:02 daisyf.stanford.edu Exp $
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
