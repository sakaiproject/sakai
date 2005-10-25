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

package org.sakaiproject.component.app.help.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.sakaiproject.api.app.help.Category;
import org.sakaiproject.api.app.help.Resource;

/**
 * resource bean
 * @version $Id$
 */
public class ResourceBean implements Resource, Comparable
{
  private Long id;
  private String docId;
  private String name;
  private Set contexts = new HashSet();
  private String location;
  private String source;
  private Long tstamp;
  private float score;
  private String formattedScore;
  private String defaultForTool;
  private String welcomePage;
  private Category category;

  /**
   * get id
   * @return Returns the id.
   */
  public Long getId()
  {
    return id;
  }

  /**
   * set id
   * @param id The id to set.
   */
  public void setId(Long id)
  {
    this.id = id;
  }

  /**
   * @see org.sakaiproject.api.app.help.Resource#getDocId()
   */
  public String getDocId()
  {
    return docId;
  }

  /**
   * @see org.sakaiproject.api.app.help.Resource#setDocId(java.lang.String)
   */
  public void setDocId(String docId)
  {
    this.docId = docId;
  }

  /**
   * @see org.sakaiproject.api.app.help.Resource#getLocation()
   */
  public String getLocation()
  {
    return location;
  }

  /**
   * @see org.sakaiproject.api.app.help.Resource#setLocation(java.lang.String)
   */
  public void setLocation(String location)
  {
    this.location = location;
  }

  /**
   * @see org.sakaiproject.api.app.help.Resource#getContexts()
   */
  public Set getContexts()
  {
    return contexts;
  }

  /**
   * @see org.sakaiproject.api.app.help.Resource#setContexts(java.util.Set)
   */
  public void setContexts(Set contexts)
  {
    this.contexts = contexts;
  }

  /**
   * @see org.sakaiproject.api.app.help.Resource#getUrl()
   */
  public URL getUrl() throws MalformedURLException
  {
    return new URL(getLocation());
  }

  /**
   * @see org.sakaiproject.api.app.help.Resource#getName()
   */
  public String getName()
  {
    return name;
  }

  /**
   * @see org.sakaiproject.api.app.help.Resource#setName(java.lang.String)
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * @see org.sakaiproject.api.app.help.Resource#getSource()
   */
  public String getSource()
  {
    return source;
  }

  /**
   * @see org.sakaiproject.api.app.help.Resource#setSource(java.lang.String)
   */
  public void setSource(String source)
  {
    this.source = source;
  }
    
  /**
   * @see org.sakaiproject.api.app.help.Resource#getTstamp()
   */
  public Long getTstamp() {	
	return tstamp;
  }

  /**
   * @see org.sakaiproject.api.app.help.Resource#setTstamp(java.lang.Long)
   */
  public void setTstamp(Long tstamp) {
	this.tstamp = tstamp;	
  }

  /**
   * @see org.sakaiproject.api.app.help.Resource#getScore()
   */
  public float getScore()
  {
    return score;
  }

  /**
   * @see org.sakaiproject.api.app.help.Resource#setScore(float)
   */
  public void setScore(float score)
  {
    this.score = score;
  }
  
  
  /**
   * @see org.sakaiproject.api.app.help.Resource#getDefaultForTool()
   */
  public String getDefaultForTool()
  {    
    return defaultForTool;
  }
  
  /**
   * @see org.sakaiproject.api.app.help.Resource#setDefaultForTool(java.lang.String)
   */
  public void setDefaultForTool(String defaultForTool)
  {
    this.defaultForTool = defaultForTool;
  }
  
  /**
   * @see org.sakaiproject.api.app.help.Resource#getFormattedScore()
   */
  public String getFormattedScore()
  {
    formattedScore = String.valueOf(score);
    int index = formattedScore.indexOf(".");
    formattedScore = formattedScore.substring(0, index + 2);
    return formattedScore + "%";
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object object)
  {
    ResourceBean resourceBean = (ResourceBean) object;
    if (resourceBean.score != 0){
      return Float.compare(resourceBean.score, score);
    }
    else{
      return (id.compareTo(resourceBean.id));
    }
  }

  /**
   * business key 
   * @return businessKey
   */
  private String businessKey()
  {
    StringBuffer sb = new StringBuffer();
    sb.append(docId);
    return sb.toString();
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (!(obj instanceof ResourceBean)) return false;
    ResourceBean other = (ResourceBean) obj;
    return this.businessKey().equals(other.businessKey());
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    return businessKey().hashCode();
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("docId=");
    sb.append(docId);
    sb.append(", name=");
    sb.append(name);
    sb.append(", location=");
    sb.append(location);
    sb.append(", defaultForTool=");
    sb.append(defaultForTool);
    sb.append(", welcomePage=");
    sb.append(welcomePage);
    sb.append(", source=");
    sb.append(source);    
    return sb.toString();
  }

  /**
   * get category
   * @return Returns the category.
   */
  public Category getCategory()
  {
    return category;
  }

  /**
   * set category
   * @param category The category to set.
   */
  public void setCategory(Category category)
  {
    this.category = category;
  }

  public String getWelcomePage()
  {
    return welcomePage;
  }

  public void setWelcomePage(String welcomePage)
  {
    this.welcomePage = welcomePage;
  }
}


