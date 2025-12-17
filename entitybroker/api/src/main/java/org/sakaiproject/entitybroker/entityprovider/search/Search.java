/**
 * $Id$
 * $URL$
 * Searcher.java - entity-broker - Apr 8, 2008 11:50:18 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.entitybroker.entityprovider.search;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is a simple class which allows the passing of a set of search parameters in a nice way<br/>
 * Example usage:<br/>
 * <code>Search s1 = new Search("title", curTitle); // search where title equals value of curTitle</code><br/>
 * <code>Search s2 = new Search("title", curTitle, Restriction.NOT_EQUALS); // search where title not equals value of curTitle</code><br/>
 * <code>Search s2 = new Search(<br/>
 *    new Restriction("title", curTitle),<br/> 
 *    new Order("title")<br/>
 * ); // search where title equals value of curTitle and order is by title ascending</code><br/>
 * <br/>
 * Most searches can be modeled this way fairly easily. There are many constructors to make
 * it easy for a developer to write the search they want inside the search constructor.<br/>
 * There are also some methods to allow easy construction of searches in multiple steps:
 * {@link #addOrder(Order)} and {@link #addRestriction(Restriction)} allow restrictions and orders
 * to be added after the search was constructed, they will correctly handle duplicate values as well.<br/>
 * <br/>
 * There is also an option to pass a search string as well which can contain
 * formatted text to be interpreted by whatever is using the search object<br/>
 * <br/>
 * Finally, there are a few methods to make it easier to unpack and work with the search object:
 * {@link #isEmpty()} and {@link #getRestrictionByProperty(String)} and {@link #getRestrictionsProperties()}
 * make it easier to get the restriction information out of the search object
 * 
 * @author Aaron Zeckoski (aaronz@caret.cam.ac.uk)
 */
public class Search {

   /**
    * the index of the first persisted result object to be retrieved (numbered from 0)
    */
   private long start = 0;
   public void setStart(long start) {
      this.start = start < 0 ? 0 : start;
   }
   public long getStart() {
      return start;
   }

   /**
    * the maximum number of persisted result objects to retrieve (or 0 for no limit)
    */
   private long limit = 0;
   public void setLimit(long limit) {
      this.limit = limit < 0 ? 0 : limit;
   }
   public long getLimit() {
      return limit;
   }

   /**
    * if true then all restrictions are run using AND, if false then all restrictions are run using OR
    */
   public boolean conjunction = true;
   /**
    * if true then all restrictions are run using AND, if false then all restrictions are run using OR
    */
   public boolean isConjunction() {
      return conjunction;
   }
   public void setConjunction(boolean conjunction) {
      this.conjunction = conjunction;
   }

   /**
    * Restrictions define limitations on the results of a search, e.g. propertyA > 100 or property B = 'jump'<br/> You
    * can add as many restrictions as you like and they will be applied in the array order
    */
   private Restriction[] restrictions = new Restriction[] {};
   /**
    * Restrictions define limitations on the results of a search, e.g. propertyA > 100 or property B = 'jump'<br/> You
    * can add as many restrictions as you like and they will be applied in the array order
    */
   public Restriction[] getRestrictions() {
      return restrictions;
   }
   public void setRestrictions(Restriction[] restrictions) {
      this.restrictions = restrictions;
   }

   /**
    * Orders define the order of the returned results of a search, You can add as many orders as you like and they will
    * be applied in the array order
    */
   private Order[] orders = new Order[] {};
   /**
    * Orders define the order of the returned results of a search, You can add as many orders as you like and they will
    * be applied in the array order
    */
   public Order[] getOrders() {
      return orders;
   }
   public void setOrders(Order[] orders) {
      this.orders = orders;
   }

   /**
    * Defines a search query string which will be interpreted into search params,
    * If not null this indicates that this is a string based "search"<br/>
    * The search string is just text - there is no required structure nor any modifiers. It is a freeform string.<br/>
    * Effectively the semantics are that it can be implemented in a relational database using 
    * like clauses for the relevant text fields - or perhaps just submitted to lucene and see which entities match.<br/>
    * If this is being sent to lucene - things like order, and restrictions might actually be added to the 
    * lucene query in addition to the simple search string.
    */
   private String queryString = null;
   /**
    * Defines a search query string which will be interpreted into search params,
    * If not null this indicates that this is a string based "search"<br/>
    * The search string is just text - there is no required structure nor any modifiers. It is a freeform string.<br/>
    * Effectively the semantics are that it can be implemented in a relational database using 
    * like clauses for the relevant text fields - or perhaps just submitted to lucene and see which entities match.<br/>
    * If this is being sent to lucene - things like order, and restrictions might actually be added to the 
    * lucene query in addition to the simple search string.
    */
   public String getQueryString() {
      return queryString;
   }
   public void setQueryString(String queryString) {
      this.queryString = queryString;
   }


   // CONSTRUCTORS

   /**
    * Empty constructor, 
    * if nothing is changed then this indicates that the search should return
    * all items in default order
    */
   public Search() {}

   /**
    * Copy constructor<br/>
    * Use this create a duplicate of a search object
    */
   public Search(Search search) {
      copy(search, this);
   }

   /**
    * Do a search using a query string<br/>
    * @param queryString a search query string,
    * can be combined with other parts of the search object
    * @see #queryString
    */
   public Search(String queryString) {
      this.queryString = queryString;
   }

   /**
    * Do a simple search of a single property which must equal a single value
    * 
    * @param property
    *           the name of the field (property) in the persisted object
    * @param value
    *           the value of the property (can be an array of items)
    */
   public Search(String property, Object value) {
      restrictions = new Restriction[] { new Restriction(property, value) };
   }

   /**
    * Do a simple search of a single property with a single type of comparison
    * 
    * @param property
    *           the name of the field (property) in the persisted object
    * @param value
    *           the value of the property (can be an array of items)
    * @param comparison the comparison to make between the property and the value,
    * use the defined constants from {@link Restriction}: e.g. EQUALS, LIKE, etc...
    */
   public Search(String property, Object value, int comparison) {
      restrictions = new Restriction[] { new Restriction(property, value, comparison) };
   }

   /**
    * Do a search of multiple properties which must equal corresponding values,
    * all arrays should be the same length
    * @param properties the names of the properties of the object 
    * @param values the values of the properties (can be an array of items)
    */
   public Search(String[] properties, Object[] values) {
      restrictions = new Restriction[properties.length];
      for (int i = 0; i < properties.length; i++) {
         restrictions[i] = new Restriction(properties[i], values[i]);
      }
   }

   /**
    * Do a search of multiple properties which must equal corresponding values,
    * control whether to do an AND or an OR between restrictions,
    * all arrays should be the same length
    * @param properties the names of the properties of the object 
    * @param values the values of the properties (can be an array of items)
    * @param conjunction if true then all restrictions are run using AND, 
    * if false then all restrictions are run using OR
    */
   public Search(String[] properties, Object[] values, boolean conjunction) {
      restrictions = new Restriction[properties.length];
      for (int i = 0; i < properties.length; i++) {
         restrictions[i] = new Restriction(properties[i], values[i]);
      }
      this.conjunction = conjunction;
   }

   /**
    * Do a search of multiple properties which are compared with corresponding values,
    * all arrays should be the same length
    * @param properties the names of the properties of the object 
    * @param values the values of the properties (can be an array of items)
    * @param comparisons the comparison to make between the property and the value,
    * use the defined constants from {@link Restriction}: e.g. EQUALS, LIKE, etc...
    */
   public Search(String[] properties, Object[] values, int[] comparisons) {
      restrictions = new Restriction[properties.length];
      for (int i = 0; i < properties.length; i++) {
         restrictions[i] = new Restriction(properties[i], values[i], comparisons[i]);
      }
   }

   /**
    * Do a search of multiple properties which are compared with corresponding values,
    * all arrays should be the same length
    * @param properties the names of the properties of the object 
    * @param values the values of the properties (can be an array of items)
    * @param comparisons the comparison to make between the property and the value,
    * use the defined constants from {@link Restriction}: e.g. EQUALS, LIKE, etc...
    * @param conjunction if true then all restrictions are run using AND, 
    * if false then all restrictions are run using OR
    */
   public Search(String[] properties, Object[] values, int[] comparisons, boolean conjunction) {
      restrictions = new Restriction[properties.length];
      for (int i = 0; i < properties.length; i++) {
         restrictions[i] = new Restriction(properties[i], values[i], comparisons[i]);
      }
      this.conjunction = conjunction;
   }

   /**
    * Do a search of multiple properties which are compared with corresponding values,
    * sort the returned results in ascending order defined by specific sortProperties,
    * all arrays should be the same length
    * @param properties the names of the properties of the object 
    * @param values the values of the properties (can be an array of items)
    * @param comparisons the comparison to make between the property and the value,
    * use the defined constants from {@link Restriction}: e.g. EQUALS, LIKE, etc...
    * @param orders orders to sort the returned results by
    */
   public Search(String[] properties, Object[] values, int[] comparisons, Order[] orders) {
      restrictions = new Restriction[properties.length];
      for (int i = 0; i < properties.length; i++) {
         restrictions[i] = new Restriction(properties[i], values[i], comparisons[i]);
      }
      this.orders = orders;
   }

   /**
    * Do a search of multiple properties which are compared with corresponding values,
    * sort the returned results in ascending order defined by specific sortProperties,
    * all arrays should be the same length
    * @param properties the names of the properties of the object 
    * @param values the values of the properties (can be an array of items)
    * @param comparisons the comparison to make between the property and the value,
    * use the defined constants from {@link Restriction}: e.g. EQUALS, LIKE, etc...
    * @param orders orders to sort the returned results by
    * @param firstResult the index of the first persisted result object to be retrieved (numbered from 0)
    * @param maxResults the maximum number of persisted result objects to retrieve (or <=0 for no limit)
    */
   public Search(String[] properties, Object[] values, int[] comparisons, 
         Order[] orders, long firstResult, long maxResults) {
      restrictions = new Restriction[properties.length];
      for (int i = 0; i < properties.length; i++) {
         restrictions[i] = new Restriction(properties[i], values[i], comparisons[i]);
      }
      this.orders = orders;
      this.start = firstResult;
      this.limit = maxResults;
   }

   /**
    * Defines a search which defines only a single restriction,
    * defaults to AND restriction comparison and returning all results
    * @param restriction define the limitations on the results of a search, 
    * e.g. propertyA > 100 or property B = 'jump'<br/> 
    * You can add as many restrictions as you like and they will be applied in the array order
    */
   public Search(Restriction restriction) {
      this.restrictions = new Restriction[] { restriction };
   }

   /**
    * Defines a search which defines only restrictions,
    * defaults to AND restriction comparisons and returning all results
    * @param restrictions define the limitations on the results of a search, 
    * e.g. propertyA > 100 or property B = 'jump'<br/> 
    * You can add as many restrictions as you like and they will be applied in the array order
    */
   public Search(Restriction[] restrictions) {
      this.restrictions = restrictions;
   }

   /**
    * Defines a search which defines only a single restriction and returns all items,
    * defaults to AND restriction comparisons
    * @param restriction define the limitations on the results of a search, 
    * e.g. propertyA > 100 or property B = 'jump'<br/> 
    * You can add as many restrictions as you like and they will be applied in the array order
    * @param order define the order of the returned results of a search (only one order)
    */
   public Search(Restriction restriction, Order order) {
      this.restrictions = new Restriction[] { restriction };
      this.orders = new Order[] { order };
   }

   /**
    * Defines a search which defines restrictions and return ordering,
    * defaults to AND restriction comparisons and returning all results
    * @param restrictions define the limitations on the results of a search, 
    * e.g. propertyA > 100 or property B = 'jump'<br/> 
    * You can add as many restrictions as you like and they will be applied in the array order
    * @param order define the order of the returned results of a search (only one order)
    */
   public Search(Restriction[] restrictions, Order order) {
      this.restrictions = restrictions;
      this.orders = new Order[] { order };
   }

   /**
    * Defines a search which defines restrictions and return ordering,
    * defaults to AND restriction comparisons and returning all results
    * @param restrictions define the limitations on the results of a search, 
    * e.g. propertyA > 100 or property B = 'jump'<br/> 
    * You can add as many restrictions as you like and they will be applied in the array order
    * @param orders define the order of the returned results of a search, 
    * You can add as many orders as you like and they will be applied in the array order
    */
   public Search(Restriction[] restrictions, Order[] orders) {
      this.restrictions = restrictions;
      this.orders = orders;
   }

   /**
    * Defines a search which defines only a single restriction and limits the returns,
    * defaults to AND restriction comparisons
    * @param restriction define the limitations on the results of a search, 
    * e.g. propertyA > 100 or property B = 'jump'<br/> 
    * You can add as many restrictions as you like and they will be applied in the array order
    * @param order define the order of the returned results of a search (only one order)
    * @param start the index of the first persisted result object to be retrieved (numbered from 0)
    * @param limit the maximum number of persisted result objects to retrieve (or <=0 for no limit)
    */
   public Search(Restriction restriction, Order order, long start, long limit) {
      this.restrictions = new Restriction[] { restriction };
      this.orders = new Order[] { order };
      this.start = start;
      this.limit = limit;
   }

   /**
    * Defines a search which defines restrictions and return ordering and limits the returns,
    * defaults to AND restriction comparisons
    * @param restrictions define the limitations on the results of a search, 
    * e.g. propertyA > 100 or property B = 'jump'<br/> 
    * You can add as many restrictions as you like and they will be applied in the array order
    * @param order define the order of the returned results of a search (only one order)
    * @param start the index of the first persisted result object to be retrieved (numbered from 0)
    * @param limit the maximum number of persisted result objects to retrieve (or <=0 for no limit)
    */
   public Search(Restriction[] restrictions, Order order, long start, long limit) {
      this.restrictions = restrictions;
      this.orders = new Order[] { order };
      this.start = start;
      this.limit = limit;
   }

   /**
    * Defines a search which defines restrictions and return ordering and limits the returns,
    * defaults to AND restriction comparisons
    * @param restrictions define the limitations on the results of a search, 
    * e.g. propertyA > 100 or property B = 'jump'<br/> 
    * You can add as many restrictions as you like and they will be applied in the array order
    * @param orders define the order of the returned results of a search, 
    * You can add as many orders as you like and they will be applied in the array order
    * @param start the index of the first persisted result object to be retrieved (numbered from 0)
    * @param limit the maximum number of persisted result objects to retrieve (or <=0 for no limit)
    */
   public Search(Restriction[] restrictions, Order[] orders, long start, long limit) {
      this.restrictions = restrictions;
      this.orders = orders;
      this.start = start;
      this.limit = limit;
   }

   /**
    * Defines a search which defines restrictions and return ordering and limits the returns,
    * also specifies the types of restriction comparisons (AND or OR)
    * @param restrictions define the limitations on the results of a search, 
    * e.g. propertyA > 100 or property B = 'jump'<br/> 
    * You can add as many restrictions as you like and they will be applied in the array order
    * @param order define the order of the returned results of a search (only one order)
    * @param start the index of the first persisted result object to be retrieved (numbered from 0)
    * @param limit the maximum number of persisted result objects to retrieve (or <=0 for no limit)
    * @param conjunction if true then all restrictions are run using AND, 
    * if false then all restrictions are run using OR
    */
   public Search(Restriction[] restrictions, Order order, long start, long limit, boolean conjunction) {
      this.restrictions = restrictions;
      this.orders = new Order[] { order };
      this.start = start;
      this.limit = limit;
      this.conjunction = conjunction;
   }

   /**
    * Defines a search which defines restrictions and return ordering and limits the returns,
    * also specifies the types of restriction comparisons (AND or OR)
    * @param restrictions define the limitations on the results of a search, 
    * e.g. propertyA > 100 or property B = 'jump'<br/> 
    * You can add as many restrictions as you like and they will be applied in the array order
    * @param orders define the order of the returned results of a search, 
    * You can add as many orders as you like and they will be applied in the array order
    * @param start the index of the first persisted result object to be retrieved (numbered from 0)
    * @param limit the maximum number of persisted result objects to retrieve (or <=0 for no limit)
    * @param conjunction if true then all restrictions are run using AND, 
    * if false then all restrictions are run using OR
    */
   public Search(Restriction[] restrictions, Order[] orders, long start, long limit, boolean conjunction) {
      this.restrictions = restrictions;
      this.orders = orders;
      this.start = start;
      this.limit = limit;
      this.conjunction = conjunction;
   }

   // HELPER methods

   /**
    * @param restriction add this restriction to the search filter,
    * will replace an existing restriction for a similar property
    */
   public void addRestriction(Restriction restriction) {
      if (restrictions != null) {
         int location = contains(restrictions, restriction);
         if (location >= 0 
               && location < restrictions.length) {
            restrictions[location] = restriction;
         } else {
            restrictions = appendArray(restrictions, restriction);
         }
      } else {
         restrictions = new Restriction[] {restriction};
      }
   }

   /**
    * @param order add this order to the search filter,
    * will replace an existing order for a similar property
    */
   public void addOrder(Order order) {
      if (orders != null) {
         int location = contains(orders, order);
         if (location >= 0 
               && location < orders.length) {
            orders[location] = order;
         } else {
            orders = appendArray(orders, order);
         }
      } else {
         orders = new Order[] {order};
      }
   }

   /**
    * Convenient method to find restrictions by their property,
    * if there happens to be more than one restriction with a property then
    * only the first one will be returned (since that is an invalid state)
    * 
    * @param property the property to match
    * @return the Restriction with this property or null if none found
    */
   public Restriction getRestrictionByProperty(String property) {
      Restriction r = null;
      if (restrictions != null && property != null) {
         for (int i = 0; i < restrictions.length; i++) {
            if (property.equals(restrictions[i].property)) {
               r = restrictions[i];
               break;
            }
         }         
      }
      return r;
   }

   /**
    * @return a list of all the properties on all restrictions in this search filter object
    */
   public List<String> getRestrictionsProperties() {
      List<String> l = new ArrayList<String>();
      if (restrictions != null) {
         for (int i = 0; i < restrictions.length; i++) {
            l.add(restrictions[i].property);
         }         
      }      
      return l;
   }

   /**
    * Finds if there are any search restrictions with one of the given properties, 
    * if so it returns the first of the found restriction,
    * otherwise returns null
    * 
    * @param properties an array of the properties (e.g. 'name','age') to find a value for
    * @return the value OR null if none found
    */
   public Restriction getRestrictionByProperties(String[] properties) {
       Restriction togo = null;
       for (int i = 0; i < properties.length; i++) {
           String property = properties[i];
           Restriction r = this.getRestrictionByProperty(property);
           if (r != null) {
               togo = r;
               break;
           }
       }
       return togo;
   }

   /**
    * Finds if there are any search restrictions with one of the given properties, 
    * if so it returns the first non-null value in the found restrictions,
    * otherwise returns null
    *
    * @param properties an array of the properties (e.g. 'name','age') to find a value for
    * @return the value OR null if none found
    */
   public Object getRestrictionValueByProperties(String[] properties) {
       Object value = null;
       for (int i = 0; i < properties.length; i++) {
           String property = properties[i];
           Restriction r = this.getRestrictionByProperty(property);
           if (r != null) {
               if (r.getValue() != null) {
                   value = r.getValue();
                   break;
               }
           }
       }
       return value;
   }

   /**
    * @return true if this search has no defined restrictions and no orders
    * (i.e. this is a default search so return everything in default order),
    * false if there are any defined restrictions or orders
    */
   public boolean isEmpty() {
      boolean empty = false;
      if ((restrictions == null || restrictions.length == 0) 
            && (orders == null || orders.length == 0) 
            && queryString == null) {
         empty = true;
      }
      return empty;
   }

   /**
    * Resets the search object to empty state
    */
   public void reset() {
      restrictions = new Restriction[] {};
      orders = new Order[] {};
      conjunction = false;
      queryString = null;
      start = 0;
      limit = 0;
   }

   /**
    * Checks to see if an array contains a value,
    * will return the position of the value or -1 if not found
    * 
    * @param <T>
    * @param array any array of objects
    * @param value the value to check for
    * @return array position if found, -1 if not found
    */
   public static <T> int contains(T[] array, T value) {
      int position = -1;
      if (value != null) {
         for (int i = 0; i < array.length; i++) {
            if (value.equals(array[i])) {
               position = i;
               break;
            }
         }
      }
      return position;
   }

   /**
    * Append an item to the end of an array and return the new array
    * 
    * @param array an array of items
    * @param value the item to append to the end of the new array
    * @return a new array with value in the last spot
    */
   @SuppressWarnings("unchecked")
   public static <T> T[] appendArray(T[] array, T value) {
      Class<?> type = array.getClass().getComponentType();
      T[] newArray = (T[]) Array.newInstance(type, array.length + 1);
      System.arraycopy( array, 0, newArray, 0, array.length );
      newArray[newArray.length-1] = value;
      return newArray;
   }

   /**
    * Utility method to convert an array to a string
    * @param array any array
    * @return a string version of the array
    */
   public static String arrayToString(Object[] array) {
      StringBuilder result = new StringBuilder();
      if (array != null && array.length > 0) {
         for (int i = 0; i < array.length; i++) {
            if (i > 0) {
               result.append(",");
            }
            if (array[i] != null) {
               result.append(array[i].toString());
            }
         }
      }
      return result.toString();
   }

   @Override
   protected Object clone() throws CloneNotSupportedException {
      return copy(this, null);
   }

   /**
    * Make a copy of a search object
    * @param original the search object to copy
    * @param copy the search object make equivalent to the original,
    * can be null to generate a new one
    * @return the copy of the original
    */
   public static Search copy(Search original, Search copy) {
      if (copy == null) {
         copy = new Search();
      }
      copy.setStart(original.getStart());
      copy.setLimit(original.getLimit());
      copy.setConjunction(original.isConjunction());
      copy.setQueryString(original.getQueryString());
      // TODO probably need to copy the arrays here
      copy.setRestrictions(original.getRestrictions());
      copy.setOrders(original.getOrders());
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (null == obj)
         return false;
      if (!(obj instanceof Search))
         return false;
      else {
         Search castObj = (Search) obj;
         boolean eq = this.start == castObj.start
               && this.limit == castObj.limit
               && this.conjunction == castObj.conjunction
               && (this.queryString == null ? castObj.queryString == null : this.queryString.equals(castObj.queryString))
               && Arrays.deepEquals(this.restrictions, castObj.restrictions)
               && Arrays.deepEquals(this.orders, castObj.orders);
         return eq;
      }
   }

   @Override
   public int hashCode() {
      if (this.isEmpty())
         return super.hashCode();
      String hashStr = this.getClass().getName() + ":" + this.start + ":" + this.limit + ":" + this.conjunction + ":"
         + this.queryString + ":" + arrayToString(restrictions) + ":" + arrayToString(orders);
      return hashStr.hashCode();
   }

   @Override
   public String toString() {
      return "search::start:" + start + ",limit:" + limit + ",conj:" + conjunction + ",query:" + queryString 
         + ",restricts:" + arrayToString(restrictions) + ",orders:" + arrayToString(orders);
   }

}
