/**
 * $Id$
 * $URL$
 * Searcher.java - entity-broker - Apr 8, 2008 11:50:18 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.entityprovider.search;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a simple class which allows the passing of a set of search parameters in a nice way
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
   public void setConjunction(boolean conjunction) {
      this.conjunction = conjunction;
   }
   public boolean isConjunction() {
      return conjunction;
   }

   /**
    * Restrictions define limitations on the results of a search, e.g. propertyA > 100 or property B = 'jump'<br/> You
    * can add as many restrictions as you like and they will be applied in the array order
    */
   private Restriction[] restrictions = new Restriction[] {};
   public Restriction[] getRestrictions() {
      return restrictions;
   }

   /**
    * Orders define the order of the returned results of a search, You can add as many orders as you like and they will
    * be applied in the array order
    */
   private Order[] orders = new Order[] {};
   public Order[] getOrders() {
      return orders;
   }


   // CONSTRUCTORS

   /**
    * Empty constructor, 
    * if nothing is changed then this indicates that the search should return
    * all items in default order
    */
   public Search() {}

   /**
    * Do a simple search of a single property which must equal a single value
    * 
    * @param property
    *           the name of the field (property) in the persisted object
    * @param value
    *           the value of the {@link #property} (can be an array of items)
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
    *           the value of the {@link #property} (can be an array of items)
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
    * @param restriction add this order to the search filter,
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
    * @return true if this search has no defined restrictions and no orders
    * (i.e. this is a default search so return everything in default order),
    * false if there are any defined restrictions or orders
    */
   public boolean isEmpty() {
      boolean empty = false;
      if ((restrictions == null || restrictions.length == 0) 
            && (orders == null || orders.length == 0) ) {
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

}
