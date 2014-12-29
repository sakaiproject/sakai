/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 Sakai Foundation
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

package org.sakaiproject.javax;


/**
 * A pea which defines the order to return the results of a search
 *
 * @author Aaron Zeckoski (azeckoski@gmail.com)
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
	public String toString() {
	   return "order::prop:" + property + ",asc:" + ascending;
	}

}
