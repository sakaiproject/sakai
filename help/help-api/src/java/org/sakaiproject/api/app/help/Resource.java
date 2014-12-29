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
 * Resource in the help system
 * @version $Id$ 
 */
public interface Resource
{
  /**
   * get the doc id for the resource
   * @return doc id
   */
  public String getDocId();

  /**
   * set the doc id for the resource
   * @param docId
   */
  public void setDocId(String docId);

  /**
   * get the location of this resource
   * @return location
   */
  public String getLocation();

  /**
   * set the location of this resource
   * @param location
   */
  public void setLocation(String source);
  
  /**
   * get the source of this resource
   * @return source
   */
  public String getSource();

  /**
   * set the source of this resource
   * @param source
   */
  public void setSource(String source);

  /**
   * get the time stamp of this resource
   * @return tstamp
   */
  public Long getTstamp();

  /**
   * set the time stamp of this resource
   * @param tstamp
   */
  public void setTstamp(Long tstamp);
  
  /**
   * get the name of this resource
   * @return name
   */
  public String getName();

  /**
   * set the name of this resource
   * @param name
   */
  public void setName(String name);

  /**
   * get the score for this resource
   * @return score
   */
  public float getScore();

  /**
   * set the score for this resource
   * @param score
   */
  public void setScore(float score);

  /**
   * get the formatted score for this resource
   * @return formatted score
   */
  public String getFormattedScore();

  /**
   * get the category for this resource
   * @param category
   */
  public void setCategory(Category category);
  
  /**
   * determine if this document is the default for a tool
   * @return
   */
  public String getDefaultForTool();
  
  /**
   * set whether this resource is the default for a tool
   * @param defaultForTool
   */
  public void setDefaultForTool(String defaultForTool);
  
  /**
   * determine if this document welcome page for the help tool
   * @return
   */
  public String getWelcomePage();
  
  /**
   * set whether this resource is the welcome page for the tool
   * @param defaultForTool
   */
  public void setWelcomePage(String welcomePage);
}


