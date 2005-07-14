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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.service.framework.config.ServerConfigurationService;

/**
 * Help Manager for the Sakai Help Tool.
 * @version $Id$ 
 */
public interface HelpManager
{

  /**
   * Synchronize initialization of the manager.
   */
  public void initialize();

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
  public List getContexts(String mappedView);

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
  public Set getResources(Long context);

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
  public Map getResourcesForActiveContexts(Map session);

  /**
   *
   * @param query
   * @return set of resources found by searching with the supplied query.
   * @throws RuntimeException - if query can't be parsed
   */
  public Set searchResources(String query) throws RuntimeException;

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
   * get static REST url
   * @return REST url
   */
  public String getStaticRestUrl();
  
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
}


