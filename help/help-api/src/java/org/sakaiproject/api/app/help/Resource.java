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


