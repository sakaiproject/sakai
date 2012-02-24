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
