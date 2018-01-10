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

package org.sakaiproject.component.app.help;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.app.help.Glossary;
import org.sakaiproject.api.app.help.GlossaryEntry;
import org.sakaiproject.component.app.help.model.GlossaryEntryBean;

/**
 * default glossary
 * @version $Id$
 */
@Slf4j
public class DefaultGlossary implements Glossary
{
  private String file;
  private String url;
  private Map<String, GlossaryEntry> glossary = new TreeMap<String, GlossaryEntry>();
  private boolean initialized = false;

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
      
      for (String term : glossaryTerms.stringPropertyNames())
      {
        glossary.put(term.toLowerCase(), new GlossaryEntryBean(term
            .toLowerCase(), glossaryTerms.getProperty(term)));
      }
      initialized = true;
    }
    catch (IOException e)
    {
      log.error(e.getMessage());
    }
  }

  /**
   * @see org.sakaiproject.api.app.help.Glossary#find(java.lang.String)
   */
  public GlossaryEntry find(String keyword)
  {
    if (!initialized) init();
    return glossary.get(keyword.toLowerCase());
  }

  /**
   * @see org.sakaiproject.api.app.help.Glossary#findAll()
   */
  public Collection<GlossaryEntry> findAll()
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


