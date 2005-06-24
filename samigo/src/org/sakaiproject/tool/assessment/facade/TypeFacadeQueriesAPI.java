/**********************************************************************************
* $HeadURL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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