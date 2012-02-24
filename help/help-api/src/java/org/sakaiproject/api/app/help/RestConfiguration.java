/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.api.app.help; 



/**
 * @author <a href="mailto:jlannan.iupui.edu">Jarrod Lannan</a>
 * @version $Id$
 * 
 */
public interface RestConfiguration
{
  /**
   * get organization
   * @return organization
   */
  public String getOrganization();
  
  /**
   * set organization
   * @param organization
   */
  public void setOrganization(String organization);
  
  /**
   * get REST credentials
   * @return REST credentails
   */
  public String getRestCredentials();
  
  /**
   * set REST credentials
   * @param credentials
   */
  public void setRestCredentials(String credentials);
  
  /**
   * get REST URL
   * @return REST URL
   */
  public String getRestUrl();
  
  /**
   * set REST URL
   * @param url
   */
  public void setRestUrl(String url);
  
  /**
   * get REST domain
   * @return REST domain
   */
  public String getRestDomain();
  
  /**
   * set REST domain
   * @param domain
   */
  public void setRestDomain(String domain);
  
  /**
   * get cache interval
   * @return cache interval
   */
  public long getCacheInterval();
  
  /**
   * set cache interval
   * @param interval
   */
  public void setCacheInterval(long interval);
  
  /**
   * get REST URL with domain
   * @return a String of the URL
   */
  public String getRestUrlInDomain();
  
  /**
   * gets the REST URL for corpus xml document
   * @return REST corpus URL as String
   */
  public String getRestCorpusUrl();
  
  /**
   * get corpus document (xml containing all available docs in domain)
   * @return a String containing the corpus document
   */
  public String getCorpusDocument();
  
  /**
   * upon initialization of the help tool this method will get the resource name out of the xml
   * @param xml
   * @return name of document
   */
  public String getResourceNameFromCorpusDoc(String xml);
  
}


