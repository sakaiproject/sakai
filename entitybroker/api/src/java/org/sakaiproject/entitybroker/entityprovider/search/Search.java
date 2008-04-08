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

/**
 * This is a simple pea which allows the passing of a set of search parameters in a nice way
 * 
 * @author Aaron Zeckoski (aaronz@caret.cam.ac.uk)
 */
public class Search {

   /**
    * the index of the first persisted result object to be retrieved (numbered from 0)
    */
   public long start = 0;

   /**
    * the maximum number of persisted result objects to retrieve (or <=0 for no limit)
    */
   public long limit = 0;

   /**
    * if true then all restrictions are run using AND, if false then all restrictions are run using OR
    */
   public boolean conjunction = true;

   /**
    * Restrictions define limitations on the results of a search, e.g. propertyA > 100 or property B = 'jump'<br/> You
    * can add as many restrictions as you like and they will be applied in the array order
    */
   public Restriction[] restrictions;

   /**
    * Orders define the order of the returned results of a search, You can add as many orders as you like and they will
    * be applied in the array order
    */
   public Order[] orders;


   // CONSTRUCTORS

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

}
