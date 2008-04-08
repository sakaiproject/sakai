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

import java.util.Collection;

/**
 * Defines a restriction in a search, this is like saying:
 * where userId = '123'; OR where userId like '%aaronz%';
 *
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class Restriction {

	public final static int EQUALS = 0;
	public final static int GREATER = 1;
	public final static int LESS = 2;
	public final static int LIKE = 3;
	public final static int NULL = 4;
	public final static int NOT_NULL = 5;
	public final static int NOT_EQUALS = 6;

	/**
	 * the name of the field (property) in the persisted object
	 */
	public String property;
	/**
	 * the value of the {@link #property} (can be an array of items)
	 */
	public Object value;
	/**
	 * the comparison to make between the property and the value,
	 * use the defined constants: e.g. EQUALS, LIKE, etc...
	 */
	public int comparison = EQUALS;

	/**
	 * Simple restriction where the property must equal the value
	 * @param property the name of the field (property) in the persisted object
	 * @param value the value of the {@link #property} (can be an array of items or list)
	 */
	public Restriction(String property, Object value) {
		this.property = property;
		this.value = value;
	}

	/**
	 * Restriction which defines the type of comparison to make between a property and value
	 * @param property the name of the field (property) in the persisted object
	 * @param value the value of the {@link #property} (can be an array of items or list)
	 * @param comparison the comparison to make between the property and the value,
	 * use the defined constants: e.g. EQUALS, LIKE, etc...
	 */
	public Restriction(String property, Object value, int comparison) {
		this.property = property;
		this.value = value;
		this.comparison = comparison;
	}

	// HELPER methods

	/**
	 * @return the value if it is not an array or collection,
	 * if it is then return the first object in the array or collection,
	 * otherwise return null (including when value is null)
	 */
	@SuppressWarnings("unchecked")
   public Object getSingleValue() {
	   Object result = null;
	   if (value != null) {
   	   if (value.getClass().isArray()) {
   	      if (((Object[])value).length > 0) {
   	         result = ((Object[])value)[0];
   	      }
   	   } else if (value.getClass().isAssignableFrom(Collection.class)) {
            if (! ((Collection)value).isEmpty()) {
               result = ((Collection)value).iterator().next();
            }
   	   } else {
   	      result = value;
   	   }
	   }
	   return result;
	}

   /**
    * @return the value as an array if it is an array or a collection,
    * if it is a single value then return null (also when value is null)
    */
   @SuppressWarnings("unchecked")
   public Object[] getArrayValue() {
      Object[] result = null;
      if (value != null) {
         if (value.getClass().isArray()) {
            result = (Object[])value;
         } else if (value.getClass().isAssignableFrom(Collection.class)) {
            result = ((Collection)value).toArray();
         }
      }      
      return result;
   }


   @Override
   public boolean equals(Object obj) {
      if (null == obj) return false;
      if (!(obj instanceof Restriction)) return false;
      else {
         Restriction castObj = (Restriction) obj;
         if (null == this.property || null == castObj.property) return false;
         else return (
               this.property.equals(castObj.property)
         );
      }
   }

   @Override
   public int hashCode() {
      if (null == this.property) return super.hashCode();
      String hashStr = this.getClass().getName() + ":" + this.property.hashCode();
      return hashStr.hashCode();
   }

   @Override
   public String toString() {
      return "property:" + this.property + ", value:" + this.value + ", comparison:" + this.comparison;
   }

}
