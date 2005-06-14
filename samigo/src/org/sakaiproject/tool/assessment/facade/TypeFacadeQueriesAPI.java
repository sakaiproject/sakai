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