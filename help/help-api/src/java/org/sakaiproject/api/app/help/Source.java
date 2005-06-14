package org.sakaiproject.api.app.help;

import java.util.Map;
import java.util.Set;

/**********************************************************************************
 *
 * $Header: /cvs/sakai2/help/help-api/src/java/org/sakaiproject/api/app/help/Source.java,v 1.1 2005/05/18 15:14:22 jlannan.iupui.edu Exp $
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

/**
 * @version $Id$
 */
public interface Source
{
  /**
   * get attributes
   * @return map of attributes
   */
  public Map getAttributes();

  /**
   * set attributes
   * @param attributes
   */
  public void setAttributes(Map attributes);

  /**
   * get name
   * @return name
   */
  public String getName();

  /**
   * set name
   * @param name
   */
  public void setName(String name);

  /**
   * get url appenders
   * @return set of url appenders
   */
  public Set getUrlAppenders();

  /**
   * set url appenders
   * @param urlAppenders
   */
  public void setUrlAppenders(Set urlAppenders);
}

/**********************************************************************************
 *
 * $Header: /cvs/sakai2/help/help-api/src/java/org/sakaiproject/api/app/help/Source.java,v 1.1 2005/05/18 15:14:22 jlannan.iupui.edu Exp $
 *
 **********************************************************************************/