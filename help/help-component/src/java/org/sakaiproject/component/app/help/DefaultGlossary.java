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

package org.sakaiproject.component.app.help;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.help.Glossary;
import org.sakaiproject.api.app.help.GlossaryEntry;
import org.sakaiproject.component.app.help.model.GlossaryEntryBean;

/**
 * default glossary
 * @version $Id$
 */
public class DefaultGlossary implements Glossary
{

  private String file;
  private String url;
  private Map glossary = new TreeMap();
  private boolean initialized = false;
  protected final Log logger = LogFactory.getLog(getClass());

  /**
   * initialize glossary
   */
  protected void init()
  {
    URL glossaryFile = this.getClass().getResource(getFile());
    Properties glossaryTerms = new Properties();
    try
    {
      glossaryTerms.load(glossaryFile.openStream());
      for (Enumeration i = glossaryTerms.propertyNames(); i.hasMoreElements();)
      {
        String term = (String) i.nextElement();
        glossary.put(term.toLowerCase(), new GlossaryEntryBean(term
            .toLowerCase(), glossaryTerms.getProperty(term)));
      }
      initialized = true;
    }
    catch (IOException e)
    {
      logger.error(e);
    }
  }

  /**
   * @see org.sakaiproject.api.app.help.Glossary#find(java.lang.String)
   */
  public GlossaryEntry find(String keyword)
  {
    if (!initialized) init();
    return (GlossaryEntryBean) glossary.get(keyword.toLowerCase());
  }

  /**
   * @see org.sakaiproject.api.app.help.Glossary#findAll()
   */
  public Collection findAll()
  {
    if (!initialized) init();
    return glossary.values();
  }

  /**
   * @see org.sakaiproject.api.app.help.Glossary#getUrl()
   */
  public String getUrl()
  {
    return url;
  }

  /**
   * set url
   * @param url
   */
  public void setUrl(String url)
  {
    this.url = url;
  }

  /**
   * get file
   * @return file name
   */
  public String getFile()
  {
    return file;
  }

  /**
   * set file name
   * @param file
   */
  public void setFile(String file)
  {
    if (!file.startsWith("/"))
    {
      this.file = "/" + file;
    }
    else
    {
      this.file = file;
    }
  }
}


