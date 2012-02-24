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
/**
 * @author Zach A. Thomas <zach@aeroplanesoftware.com>
 * 
 * This interface encapsulates an operator used in a boolean expression.
 * An object that implements this interface will implement a getType method
 * which will return one of the following:
 * 
 * LESS_THAN
 * GREATER_THAN
 * EQUAL_TO
 * GREATER_THAN_EQUAL_TO
 * NO_OP
 *
 */
public interface Operator {
	
	public static final int LESS_THAN = 0;
	public static final int GREATER_THAN = 1;
	public static final int EQUAL_TO = 3;
	public static final int GREATER_THAN_EQUAL_TO = 4;
	public static final int NO_OP = 5;
	
	/**
	 * return the type of Operator this is, using integer constants defined on this interface
	 * @return
	 */
	public int getType();

}
