/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/facade/TypeFacadeQueriesAPI.java $
 * $Id: TypeFacadeQueriesAPI.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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


package org.sakaiproject.tool.assessment.facade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.osid.shared.Type;

public interface TypeFacadeQueriesAPI
{

  /**
   * set the typeFacadeMap for TypeFacadeQueries
   */
  public void setTypeFacadeMap();

  /**
   * get the typeFacadeMap
   */
  public HashMap getTypeFacadeMap();

  /**
   * This method returns the TypeFacade with the specified typeId found
   * in the typeFacadeMap that lives in cache.
   * @param typeId
   * @return TypeFacade
   */
  public TypeFacade getTypeFacadeById(Long typeId);

  /**
   * This method return Type with a specified typeId, used by
   * ItemFacade.getItemType()
   * @param typeId
   * @return org.osid.shared.Type
   */
  public Type getTypeById(Long typeId);

  /**
   * This method return an ArrayList (Long typeId, TypeFacade typeFacade)
   * with the specified authority and domain.
   * @param authority
   * @param domain
   * @return ArrayList
   */
  public ArrayList getArrayListByAuthorityDomain(String authority, String domain);

  /**
   * This method returns a Hashmap (Long typeId, TypeFacade typeFacade)
   * with the specified authority and domain.
   * @param authority
   * @param domain
   * @return HashMap
   */
  public HashMap getHashMapByAuthorityDomain(String authority, String domain);

  /**
   * This method return a List of TypeD from DB or cache (Hibernate decides)
   * with the specified authority & domain
   * @param authority
   * @param domain
   * @return List
   */
  public List getListByAuthorityDomain(String authority, String domain);

  public List getFacadeListByAuthorityDomain(String authority, String domain);

  public List getFacadeItemTypes();

  public void setFacadeItemTypes();

}
