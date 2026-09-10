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

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.osid.shared.Type;
import org.sakaiproject.tool.assessment.data.dao.shared.TypeD;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.osid.shared.extension.TypeExtension;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
public class TypeFacadeQueries implements TypeFacadeQueriesAPI {

  private Map<Long, TypeFacade> typeFacadeMap;
  private List<TypeFacade> itemTypes;
  @Setter private SessionFactory sessionFactory;

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
    @Transactional(readOnly = true)
    public Map<Long, TypeFacade> getTypeFacadeMap() {
	    return this.typeFacadeMap;
    }

    /**
     * This method returns the TypeFacade with the specified typeId found
     * in the typeFacadeMap that lives in cache.
     * @param typeId
     * @return TypeFacade
     */
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
	private List<TypeD> getAllTypes() {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<TypeD> cq = cb.createQuery(TypeD.class);
        Root<TypeD> root = cq.from(TypeD.class);
        cq.select(root);

        return session.createQuery(cq).list();
    }

    /**
     * This method returns a HashMap (Long typeId, TypeFacade typeFacade)
     * containing all the TypeFacade available
     * @return HashMap
     */
    @Transactional(readOnly = true)
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
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<TypeD> cq = cb.createQuery(TypeD.class);
        Root<TypeD> root = cq.from(TypeD.class);
        cq.where(
                cb.equal(root.get("authority"), authority),
                cb.equal(root.get("domain"), domain));
        Query<TypeD> q = session.createQuery(cq);
        q.setCacheable(true);

        return q.list();
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
