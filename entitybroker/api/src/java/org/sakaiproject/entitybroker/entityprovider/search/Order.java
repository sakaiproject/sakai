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
 * A pea which defines the order to return the results of a search
 *
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class Order {

	/**
	 * the name of the field (property) in the persisted object
	 */
	public String property;
	/**
	 * if true then the return order is ascending,
	 * if false then return order is descending
	 */
	public boolean ascending = true;

	/**
	 * a simple order for a property which is ascending
	 * @param property the name of the field (property) in the persisted object
	 */
	public Order(String property) {
		this.property = property;
		this.ascending = true;
	}

	/**
	 * define an order for a property
	 * @param property the name of the field (property) in the persisted object
	 * @param ascending if true then the return order is ascending,
	 * if false then return order is descending
	 */
	public Order(String property, boolean ascending) {
		this.property = property;
		this.ascending = ascending;
	}

   @Override
   public boolean equals(Object obj) {
      if (null == obj) return false;
      if (!(obj instanceof Order)) return false;
      else {
         Order castObj = (Order) obj;
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
      return "property:" + this.property + ", ascending:" + this.ascending;
   }

}
