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

import java.util.Set;

/**
 * Help Manager for the Sakai Help Tool.
 * @version $Id$ 
 */
public interface HelpManager
{
  String TOOLCONFIG_HELP_COLLECTIONS = "help.collections";
  
  String HELP_DOC_REGEXP = "^[A-Za-z0-9._-]+$";
  
  /**
   * Synchronize initialization of the manager.
   */
  void initialize();
  
  /**
   * reInitialization of the help tool.
   */
  void reInitialize();

  /**
   * get a resource by id
   * @param id
   * @return
   */
  Resource getResource(Long id);

  /**
   * create a resource
   * @return Resource
   */
  Resource createResource();

  /**
   * persist a resource
   * @param resource
   */
  void storeResource(Resource resource);

  /**
   * delete a resource by id
   * @param resourceId
   */
  void deleteResource(Long resourceId);

  /**
   *
   * @param query
   * @return set of resources found by searching with the supplied query.
   * @throws RuntimeException - if query can't be parsed
   */
  Set<Resource> searchResources(String query) throws RuntimeException;

  /**
   * get table of contents of manager
   * @return TableOfContents
   */
  TableOfContents getTableOfContents();

  /**
   * set table of contents
   * @param toc
   */
  void setTableOfContents(TableOfContents toc);

  /**
   * get resource by doc id
   * @param helpDocIdString
   * @return Resource
   */
  Resource getResourceByDocId(String helpDocIdString);

  /**
   * get support email address
   * @return address as string
   */
  String getSupportEmailAddress();
  
  /**
   * get REST configuration
   * @return REST configuration
   */
  RestConfiguration getRestConfiguration();
  
  /**
   * get static EXTERNAL_LOCATION
   * @return EXTERNAL_LOCATION
   */
  String getExternalLocation();
  
  /**
   * get Welcome Page
   * @return docId of Welcome Page
   */
  String getWelcomePage();
    
}


