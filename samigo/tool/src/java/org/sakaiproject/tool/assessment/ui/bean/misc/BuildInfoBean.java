/**********************************************************************************
* $URL$
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

package org.sakaiproject.tool.assessment.ui.bean.misc;


/**
 * Encapsulate build information
 * formerly in org.navigoproject.ui.web.debug;
 *
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon</a>
 * @version $Id$
 */
public class BuildInfoBean
{
  private String buildVersion;
  private String buildTime;
  private String buildTag;

  /**
   * build information
   *
   * @return build information
   */
  public String getBuildTag()
  {
    return buildTag;
  }

  /**
   * build information
   *
   * @return build time
   */
  public String getBuildTime()
  {
    return buildTime;
  }

  /**
   * build information
   *
   * @return version
   */
  public String getBuildVersion()
  {
    return buildVersion;
  }

  /**
   * build information
   *
   * @param string
   */
  public void setBuildTag(String string)
  {
    buildTag = string;
  }

  /**
   * build information
   *
   * @param string
   */
  public void setBuildTime(String string)
  {
    buildTime = string;
  }

  /**
   * build information
   *
   * @param string
   */
  public void setBuildVersion(String string)
  {
    buildVersion = string;
  }
}
