/**********************************************************************************
 *
 * $Header: /cvs/sakai2/help/help-component-shared/src/java/org/sakaiproject/component/app/help/model/GlossaryEntryBean.java,v 1.1 2005/05/19 15:39:14 jlannan.iupui.edu Exp $
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

package org.sakaiproject.component.app.help.model;

import org.sakaiproject.api.app.help.GlossaryEntry;

/**
 * glossary entry bean
 * @version $Id$ 
 */
public class GlossaryEntryBean implements GlossaryEntry
{
  private String term;
  private String description;

  /**
   * overloaded constructor
   * @param term
   * @param description
   */
  public GlossaryEntryBean(String term, String description)
  {
    this.term = term;
    this.description = description;
  }

  /**
   * @see org.sakaiproject.api.app.help.GlossaryEntry#getDescription()
   */
  public String getDescription()
  {
    return description;
  }

  /**
   * @see org.sakaiproject.api.app.help.GlossaryEntry#getTerm()
   */
  public String getTerm()
  {
    return term;
  }

  /**
   * @see org.sakaiproject.api.app.help.GlossaryEntry#setDescription(java.lang.String)
   */
  public void setDescription(String description)
  {
    this.description = description;
  }

  /**
   * @see org.sakaiproject.api.app.help.GlossaryEntry#setTerm(java.lang.String)
   */
  public void setTerm(String term)
  {
    this.term = term;
  }
}

/**********************************************************************************
 *
 * $Header: /cvs/sakai2/help/help-component-shared/src/java/org/sakaiproject/component/app/help/model/GlossaryEntryBean.java,v 1.1 2005/05/19 15:39:14 jlannan.iupui.edu Exp $
 *
 **********************************************************************************/