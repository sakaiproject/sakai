/**********************************************************************************
*
* $Header: /cvs/sakai2/help/help-component/src/java/org/sakaiproject/component/app/help/RestConfigurationImpl.java,v 1.1 2005/06/05 05:15:17 jlannan.iupui.edu Exp $
*
***********************************************************************************
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
package org.sakaiproject.component.app.help; 

import org.sakaiproject.api.app.help.RestConfiguration;



/**
 * @author <a href="mailto:jlannan.iupui.edu">Jarrod Lannan</a>
 * @version $Id$
 * 
 */
public class RestConfigurationImpl implements RestConfiguration
{

  /** user:pass as string ... will be converted to Base64 **/
  private String restCredentials;

  private String organization;
  private String restDomain;
  private String restUrl;
  private long cacheInterval;
  
  /**
   * @see org.sakaiproject.api.app.help.RestConfiguration#getOrganization()
   */
  public String getOrganization()
  {
    return organization;
  }

  /**
   * set Organization
   * @param organization
   */
  public void setOrganization(String organization){
    this.organization = organization;
  }
  
  /**
   * @see org.sakaiproject.api.app.help.RestConfiguration#getRestCredentials()
   */
  public String getRestCredentials()
  {   
    return restCredentials;
  }
  
  /**
   * set REST credentials
   * @param restCredentials
   */
  public void setRestCredentials(String restCredentials){
    this.restCredentials = restCredentials;
  }
  
  /**
   * @see org.sakaiproject.api.app.help.RestConfiguration#getRestDomain()
   */
  public String getRestDomain()
  {
    return restDomain;
  }
  
  /**
   * set REST domain
   * @param restDomain
   */
  public void setRestDomain(String restDomain){
    this.restDomain = restDomain;
  }
  
  /**
   * @see org.sakaiproject.api.app.help.RestConfiguration#getRestUrl()
   */
  public String getRestUrl()
  {
    return restUrl;
  }
  
  /**
   * set REST URL
   * @param restUrl
   */
  public void setRestUrl(String restUrl){
    this.restUrl = restUrl;
  }

  /**
   * @see org.sakaiproject.api.app.help.RestConfiguration#getCacheInterval()
   */
  public long getCacheInterval() {
	return cacheInterval;	
  }

/**
   * set cache interval
   * @param cacheInterval
   */
  public void setCacheInterval(long cacheInterval) {
	this.cacheInterval = cacheInterval;
  }
  
  
}

/**********************************************************************************
*
* $Header: /cvs/sakai2/help/help-component/src/java/org/sakaiproject/component/app/help/RestConfigurationImpl.java,v 1.1 2005/06/05 05:15:17 jlannan.iupui.edu Exp $
*
**********************************************************************************/