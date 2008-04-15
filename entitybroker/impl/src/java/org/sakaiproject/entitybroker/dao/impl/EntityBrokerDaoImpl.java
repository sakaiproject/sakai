/**
 * $Id$
 * $URL$
 * EntityBrokerDaoImpl.java - entity-broker - Apr 15, 2008 4:29:18 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.dao.impl;

import java.util.List;

import org.sakaiproject.entitybroker.dao.EntityBrokerDao;
import org.sakaiproject.genericdao.api.finders.ByPropsFinder;
import org.sakaiproject.genericdao.hibernate.HibernateCompleteGenericDao;

/**
 * Implementation of DAO
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EntityBrokerDaoImpl extends HibernateCompleteGenericDao implements EntityBrokerDao {

   @SuppressWarnings("unchecked")
   public List<String> getEntityRefsForSearch(List<String> properties, List<String> values,
         List<Integer> comparisons, List<String> relations) {
      if (properties.size() != values.size() || values.size() != comparisons.size()
            || comparisons.size() != relations.size() || properties.isEmpty()) {
         throw new IllegalArgumentException(
               "properties and values and comparisons must be the same size and not empty");
      }

      String[] paramNames = new String[properties.size()];
      String[] paramValues = new String[properties.size()];

      StringBuilder hql = new StringBuilder();
      hql.append("select distinct eProp.entityRef from EntityProperty as eProp where");
      for (int i = 0; i < properties.size(); i++) {
         if (i == 0) {
            hql.append(" (");
         } else { // i > 0
            String relation = relations.get(i);
            if ("and".equals(relation)) {
               hql.append(") and (");
            } else {
               hql.append(" or ");
            }
         }

         int comparison = comparisons.get(i).intValue();
         paramNames[i] = "param" + i;
         hql.append("eProp.");
         hql.append(properties.get(i));

         if (ByPropsFinder.LIKE == comparison) {
            paramValues[i] = "%" + values.get(i) + "%";
            hql.append(" like :");
         } else { // assume equals
            paramValues[i] = values.get(i).toString();
            hql.append(" = :");
         }

         hql.append(paramNames[i]);
      }
      hql.append(") order by eProp.entityRef");

      return getHibernateTemplate().findByNamedParam(hql.toString(), paramNames, paramValues);
   }

   public int deleteProperties(String entityReference, String name) {
      StringBuilder hql = new StringBuilder();
      hql.append("delete from EntityProperty as eProp where eProp.entityRef = ?");

      String[] values = null;
      if (name == null) {
         values = new String[] { entityReference };
      } else {
         values = new String[] { entityReference, name };
         hql.append(" and eProp.propertyName = ?");
      }
      return getHibernateTemplate().bulkUpdate(hql.toString(), values);
   }

}
