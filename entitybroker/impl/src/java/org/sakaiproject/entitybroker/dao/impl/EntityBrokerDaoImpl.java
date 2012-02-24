/**
 * $Id$
 * $URL$
 * EntityBrokerDaoImpl.java - entity-broker - Apr 15, 2008 4:29:18 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2007, 2008 The Sakai Foundation
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
 */

package org.sakaiproject.entitybroker.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.entitybroker.dao.EntityBrokerDao;
import org.sakaiproject.entitybroker.dao.EntityProperty;
import org.sakaiproject.entitybroker.dao.EntityTagApplication;
import org.sakaiproject.genericdao.api.mappers.NamesRecord;
import org.sakaiproject.genericdao.api.mappers.StatementMapper;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;
import org.sakaiproject.genericdao.springjdbc.JdbcGeneralGenericDao;
import org.sakaiproject.genericdao.util.JDBCUtils.QueryData;

/**
 * Internal dao for entity broker internal services
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EntityBrokerDaoImpl extends JdbcGeneralGenericDao implements EntityBrokerDao {

   /**
    * Get a list of unique entity references for a set of search params, all lists must be the same
    * size
    * 
    * @param properties
    *           the persistent object properties
    * @param values
    *           the values to match against the properties
    * @param comparisons
    *           the type of comparisons to make between property and value
    * @param relations
    *           the relation to the previous search param (must be "and" or "or") - note that the
    *           first relation is basically thrown away
    * @return a list of unique {@link String}s which represent entity references
    */
   @SuppressWarnings("unchecked")
   public List<String> getEntityRefsForSearch(List<String> properties, List<String> values,
         List<Integer> comparisons, List<String> relations) {
      if (properties.size() != values.size() || values.size() != comparisons.size()
            || comparisons.size() != relations.size() || properties.isEmpty()) {
         throw new IllegalArgumentException(
         "properties and values and comparisons must be the same size and not empty");
      }

      NamesRecord nr = getNamesRecord(EntityProperty.class);
      String entityRefColumn = nr.getColumnForProperty("entityRef");
      List<Object> params = new ArrayList<Object>();

      StringBuilder whereSQL = new StringBuilder();
      for (int i = 0; i < properties.size(); i++) {
         if (i == 0) {
            whereSQL.append(" where (");
         } else { // i > 0
            String relation = relations.get(i);
            if ("and".equals(relation)) {
               whereSQL.append(") and (");
            } else {
               whereSQL.append(" or ");
            }
         }

         int comparison = comparisons.get(i).intValue();
         String column = nr.getColumnForProperty(properties.get(i));
         if (column != null) {
            whereSQL.append( 
                  makeComparisonSQL(params, column, comparison, 
                        Restriction.LIKE == comparison ? "%" + values.get(i) + "%" : values.get(i) 
                  ) 
            );
         }
      }
      whereSQL.append(") order by ");
      whereSQL.append( entityRefColumn );

      String sql = makeSQL(getSelectTemplate(EntityProperty.class), 
            getTableNameFromClass(EntityProperty.class), 
            StatementMapper.SELECT, "distinct(" + entityRefColumn + ")",
            StatementMapper.WHERE, whereSQL.toString());

      List<String> results = getJdbcTemplate().queryForList(sql, params.toArray(), String.class);
      return results;
   }

   /**
    * Remove properties from an entity without wasting time doing a lookup first
    * 
    * @param entityReference
    *           unique reference to an entity
    * @param name
    *           the name of the property to remove, leaving this null will remove all properties
    * @return the number of properties removed
    */
   public int deleteProperties(String entityReference, String name) {
      Search search = new Search("entityRef", entityReference);
      if (name != null && name.length() > 0) {
         search.addRestriction( new Restriction("propertyName", name) );
      }
      QueryData sd = makeQueryFromSearch(EntityProperty.class, search);
      String sql = makeSQL(getDeleteTemplate(EntityProperty.class), 
            getTableNameFromClass(EntityProperty.class), 
            StatementMapper.WHERE, sd.getAfterTableSQL());
      return getJdbcTemplate().update(sql, sd.getArgs());
   }

   public int deleteTags(String entityReference, String[] tags) {
      Search search = new Search("entityRef", entityReference);
      if (tags != null && tags.length > 0) {
         search.addRestriction( new Restriction("tag", tags) );
      }
      QueryData sd = makeQueryFromSearch(EntityTagApplication.class, search);
      String sql = makeSQL(getDeleteTemplate(EntityTagApplication.class), 
            getTableNameFromClass(EntityTagApplication.class), 
            StatementMapper.WHERE, sd.getAfterTableSQL());
      return getJdbcTemplate().update(sql, sd.getArgs());
   }

}
