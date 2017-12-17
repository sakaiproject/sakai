/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.facade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Query;
import org.osid.shared.Type;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

import org.sakaiproject.tool.assessment.data.dao.shared.TypeD;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.osid.shared.extension.TypeExtension;

@Slf4j
public class TypeFacadeQueries extends HibernateDaoSupport implements TypeFacadeQueriesAPI{

  private Map<Long, TypeFacade> typeFacadeMap;
  private List<TypeFacade> itemTypes;

  public TypeFacadeQueries() {
  }

    /**
     * set the typeFacadeMap for TypeFacadeQueries
     */
    public void setTypeFacadeMap(){
	    this.typeFacadeMap = getMapForAllTypes();
    }

    /**
     * get the typeFacadeMap
     */
    public Map<Long, TypeFacade> getTypeFacadeMap() {
	    return this.typeFacadeMap;
    }

    /**
     * This method returns the TypeFacade with the specified typeId found
     * in the typeFacadeMap that lives in cache.
     * @param typeId
     * @return TypeFacade
     */
    public TypeFacade getTypeFacadeById(Long typeId) {
	    TypeFacade typeFacade = null;
	    Map<Long, TypeFacade> typeMap = getTypeFacadeMap();
	    typeFacade = typeMap.get(typeId);
	    return typeFacade;
    }

    /**
     * This method return Type with a specified typeId, used by
     * ItemFacade.getItemType()
     * @param typeId
     * @return org.osid.shared.Type or null if no types of that id
     */
    public Type getTypeById(Long typeId){
	    TypeFacade typeFacade = getTypeFacadeById(typeId);
	    // SAM-1792 this could be a request for an unknown type
	    if (typeFacade == null) {
		    log.warn("Unable to find Item Type: " + typeId.toString());
		    return null;
	    }
	    TypeExtension type = new TypeExtension(typeFacade.getAuthority(),
					       typeFacade.getDomain(),
					       typeFacade.getKeyword(),
					       typeFacade.getDescription());
	    return type;
    }

    /**
     * This method return an ArrayList (Long typeId, TypeFacade typeFacade)
     * with the specified authority and domain.
     * @param authority
     * @param domain
     * @return ArrayList
     */
    public List getArrayListByAuthorityDomain(String authority, String domain) {
	List typeList = getListByAuthorityDomain(authority, domain);
	List typeFacadeList = new ArrayList();
	for (int i = 0; i < typeList.size(); i++) {
	    TypeD typeData = (TypeD) typeList.get(i);
	    TypeFacade typeFacade = new TypeFacade(typeData);
	    typeFacadeList.add(typeFacade);
	}
	return typeFacadeList;
    }

    /**
     * This method returns a Hashmap (Long typeId, TypeFacade typeFacade)
     * with the specified authority and domain.
     * @param authority
     * @param domain
     * @return HashMap
     */
    public Map getHashMapByAuthorityDomain(String authority, String domain) {
	List typeList = getListByAuthorityDomain(authority, domain);
	return createTypeFacadeMapById(typeList);
    }

    /**
     * fetch a list of TypeD from the DB or cache (Hibernate decides)
     * @return a list of TypeD
     */
    @SuppressWarnings("unchecked")
	private List<TypeD> getAllTypes() {
    	return (List<TypeD>) getHibernateTemplate().find("from TypeD");
    }

    /**
     * This method returns a HashMap (Long typeId, TypeFacade typeFacade)
     * containing all the TypeFacade available
     * @return HashMap
     */
    private Map<Long, TypeFacade> getMapForAllTypes() {
	    List<TypeD> typeList = getAllTypes();
	    return createTypeFacadeMapById(typeList);
    }

    /**
     * This method constructs a HashMap (Long typeId, TypeFacade typeFacade) with
     * the items in typeList
     * @param typeList
     * @return a HashMap
     */
    private Map<Long, TypeFacade> createTypeFacadeMapById(List typeList){
	Map<Long, TypeFacade> typeFacadeMap = new HashMap<Long, TypeFacade>();
	for (int i = 0; i < typeList.size(); i++) {
	    TypeD typeData = (TypeD) typeList.get(i);
	    TypeFacade typeFacade = new TypeFacade(typeData);
	    typeFacadeMap.put(typeData.getTypeId(), typeFacade);
	}
	return typeFacadeMap;
    }

    /**
     * This method return a List of TypeD from DB or cache (Hibernate decides)
     * with the specified authority & domain
     * @param authority
     * @param domain
     * @return List
     */
    public List getListByAuthorityDomain(final String authority, final String domain) {
        HibernateCallback<List> hcb = session -> {
            Query q = session.createQuery("from TypeD as t where t.authority = :auth and t.domain = :domain");
            q.setString("auth", authority);
            q.setString("domain", domain);
            q.setCacheable(true);
            return q.list();
        };
        return getHibernateTemplate().execute(hcb);
    }

    public List getFacadeListByAuthorityDomain(String authority, String domain) {
      List typeList = new ArrayList();
      List list =  getListByAuthorityDomain(authority, domain);
      for (int i=0; i<list.size();i++){
        TypeD type = (TypeD)list.get(i);
        TypeFacade f = new TypeFacade(type.getAuthority(), type.getDomain(),
                        type.getKeyword(), type.getDescription());
        f.setTypeId(type.getTypeId());
        typeList.add(f);
      }
      return (List)typeList;
    }

    public List getFacadeItemTypes() {
      if (this.itemTypes == null){
        this.itemTypes = getFacadeListByAuthorityDomain(TypeIfc.SITE_AUTHORITY, TypeIfc.DOMAIN_ASSESSMENT_ITEM);
      }
      return this.itemTypes;
    }

    public void setFacadeItemTypes() {
      this.itemTypes = getFacadeItemTypes();
    }
}
