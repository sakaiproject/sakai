/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.component.api.ServerConfigurationService;

/**
 * Help Manager for the Sakai Help Tool.
 * @version $Id$ 
 */
public interface HelpManager
{
  public static final String TOOLCONFIG_HELP_COLLECTIONS = "help.collections";
  
  public static final String HELP_DOC_REGEXP = "^[A-Za-z0-9._-]+$";
  
  /**
   * Synchronize initialization of the manager.
   */
  public void initialize();
  
  /**
   * reInitialization of the help tool.
   */
  public void reInitialize();

  /**
   * Create Category
   * @return Category
   */
  public Category createCategory();

  /**
   * Store Category
   * @param category
   */
  public void storeCategory(Category category);

  /**
   * find all contexts associated with mappedView
   * @param mappedView
   * @return - list of contexts (String)
   */
  public List<String> getContexts(String mappedView);

  /**
   * returns a list of all active contexts.  Active contexts
   * are created when the user navigates around the site.
   * @param session
   * @return
   */
  public List getActiveContexts(Map session);

  /**
   * adds a context to the active context list
   * @param session
   * @param mappedView
   */
  public void addContexts(Map session, String mappedView);

  /**
   * get Resources for a context
   * @param context
   * @return set of resources associated with the supplied context
   */
  public Set<Resource> getResources(Long context);

  /**
   * get a resource by id
   * @param id
   * @return
   */
  public Resource getResource(Long id);

  /**
   * create a resource
   * @return Resource
   */
  public Resource createResource();

  /**
   * persist a resource
   * @param resource
   */
  public void storeResource(Resource resource);

  /**
   * delete a resource by id
   * @param resourceId
   */
  public void deleteResource(Long resourceId);

  /**
   * get source
   * @param id
   * @return Source
   */
  public Source getSource(Long id);

  /**
   * store source
   * @param source
   */
  public void storeSource(Source source);

  /**
   * delete source by id
   * @param sourceId
   */
  public void deleteSource(Long sourceId);

  /**
   * get context by id
   * @param id
   * @return Context
   */
  public Context getContext(Long id);

  /**
   * store context
   * @param context
   */
  public void storeContext(Context context);

  /**
   * delete context by id
   * @param contextId
   */
  public void deleteContext(Long contextId);

  /**
   *
   * @param session
   * @return map of resources keyed by active contexts
   */
  public Map<String, Set<Resource>> getResourcesForActiveContexts(Map session);

  /**
   *
   * @param query
   * @return set of resources found by searching with the supplied query.
   * @throws RuntimeException - if query can't be parsed
   */
  public Set<Resource> searchResources(String query) throws RuntimeException;

  /**
   * get table of contents of manager
   * @return TableOfContents
   */
  public TableOfContents getTableOfContents();

  /**
   * set table of contents
   * @param toc
   */
  public void setTableOfContents(TableOfContents toc);

  /**
   * searches the glossary for the keyword.
   * Returns a GlossaryEntry for this keyword if found,
   * return null if no entry is found.
   * @param keyword
   * @return
   */
  public GlossaryEntry searchGlossary(String keyword);

  /**
   * get glossary
   * @return Glossary
   */
  public Glossary getGlossary();

  /**
   * get resource by doc id
   * @param helpDocIdString
   * @return Resource
   */
  public Resource getResourceByDocId(String helpDocIdString);

  /**
   * get support email address
   * @return address as string
   */
  public String getSupportEmailAddress();
  
  /**
   * get REST configuration
   * @return REST configuration
   */
  public RestConfiguration getRestConfiguration();
  
  /**
   * get static EXTERNAL_LOCATION
   * @return EXTERNAL_LOCATION
   */
  public String getExternalLocation();
  
  /**
   * get server config service
   * @return
   */
  public ServerConfigurationService getServerConfigurationService();
 
  /**
   * set server config service
   * @param s
   */
  public void setServerConfigurationService(ServerConfigurationService s);
  
  /**
   * get Welcome Page
   * @return docId of Welcome Page
   */
  public String getWelcomePage();
    
}


