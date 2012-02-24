/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/api/src/main/java/org/sakaiproject/antivirus/api/VirusFoundException.java $
 * $Id: VirusFoundException.java 68335 2009-10-29 08:18:43Z david.horwitz@uct.ac.za $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.conditions.api;

import org.apache.commons.collections.Predicate;

/**
 * extension of <code>org.apache.commons.collections.Predicate</code> to include all the operations we need to 
 * evaluate a condition like "gradebook grade is less than 79"
 * @author Zach A. Thomas <zach@aeroplanesoftware.com>
 *
 */
public interface Condition extends Predicate {
	/**
	 * get this condition's boolean operator, e.g. less_than, greater_than_or_equal, etc.	
	 * @return <code>String</code> representation of the operator
	 */
	public String getOperator();
	
	/**
	 * a class name of the data object that will be paired with this <code>Condition</code> at evaulation time
	 * @return class name of the object that will be used to evaluate this condition
	 */
	public String getReceiver();
	
	/**
	 * the name of the method that will be called on the receiver to evaluate the condition
	 * @return name of the method to call on the receiver to evaluate this condition
	 */
	public String getMethod();
	
	/**
	 * get the object that represents the argument to this condition.
	 * e.g. in the condition 'assignment 2 grade is less than 79' the argument is 79
	 * @return the argument that has been stored with this condition to evaluate it
	 */
	public Object getArgument();

}
