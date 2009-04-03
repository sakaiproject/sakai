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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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


