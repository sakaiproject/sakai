/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/facade/TypeFacadeQueries.java $
 * $Id: TypeFacadeQueries.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osid.shared.Type;
import org.sakaiproject.tool.assessment.data.dao.shared.TypeD;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.osid.shared.extension.TypeExtension;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;


public class TypeFacadeQueries extends HibernateDaoSupport implements TypeFacadeQueriesAPI{

  private static Log log = LogFactory.getLog(TypeFacadeQueries.class);
  private HashMap typeFacadeMap;
  private List itemTypes;

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
    public HashMap getTypeFacadeMap(){
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
	HashMap typeMap = getTypeFacadeMap();
	typeFacade = (TypeFacade)typeMap.get(typeId);
	return typeFacade;
    }

    /**
     * This method return Type with a specified typeId, used by
     * ItemFacade.getItemType()
     * @param typeId
     * @return org.osid.shared.Type
     */
    public Type getTypeById(Long typeId){
	TypeFacade typeFacade = getTypeFacadeById(typeId);
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
    public ArrayList getArrayListByAuthorityDomain(String authority, String domain) {
	List typeList = getListByAuthorityDomain(authority, domain);
	ArrayList typeFacadeList = new ArrayList();
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
    public HashMap getHashMapByAuthorityDomain(String authority, String domain) {
	List typeList = getListByAuthorityDomain(authority, domain);
	return createTypeFacadeMapById(typeList);
    }

    /**
     * fetch a list of TypeD from the DB or cache (Hibernate decides)
     * @return a list of TypeD
     */
    private List getAllTypes() {
	return getHibernateTemplate().find("from TypeD");
    }

    /**
     * This method returns a HashMap (Long typeId, TypeFacade typeFacade)
     * containing all the TypeFacade available
     * @return HashMap
     */
    private HashMap getMapForAllTypes() {
	//HashMap typeMap = new HashMap();
	List typeList = getAllTypes();
	return createTypeFacadeMapById(typeList);
    }

    /**
     * This method constructs a HashMap (Long typeId, TypeFacade typeFacade) with
     * the items in typeList
     * @param typeList
     * @return a HashMap
     */
    private HashMap createTypeFacadeMapById(List typeList){
	HashMap typeFacadeMap = new HashMap();
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
        final HibernateCallback hcb = new HibernateCallback(){
        	public Object doInHibernate(Session session) throws HibernateException, SQLException {
        		Query q = session.createQuery("from TypeD as t where t.authority=? and t.domain=?");
        		q.setString(0, authority);
        		q.setString(1, domain);
        		return q.list();
        	};
        };
        return getHibernateTemplate().executeFind(hcb);

//        return getHibernateTemplate().find(
//                                           "from TypeD as t where t.authority=? and t.domain=?",
//                                           new Object[] { authority, domain },
//                                           new org.hibernate.type.Type[]{ Hibernate.STRING, Hibernate.STRING });
    }

    public List getFacadeListByAuthorityDomain(String authority, String domain) {
      ArrayList typeList = new ArrayList();
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
