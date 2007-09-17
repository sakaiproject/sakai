/**
 * EntityBrokerDao.java - created by aaronz on Jun 20, 2007
 */

package org.sakaiproject.entitybroker.dao;

import java.util.List;

import org.sakaiproject.genericdao.api.CompleteGenericDao;

/**
 * DAO for access to the database for entity broker internal writes
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface EntityBrokerDao extends CompleteGenericDao {

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
   public List<String> getEntityRefsForSearch(List<String> properties, List<String> values,
         List<Integer> comparisons, List<String> relations);

   /**
    * Remove properties from an entity without wasting time doing a lookup first
    * 
    * @param entityReference
    *           unique reference to an entity
    * @param name
    *           the name of the property to remove, leaving this null will remove all properties
    * @return the number of properties removed
    */
   public int deleteProperties(String entityReference, String name);

}
