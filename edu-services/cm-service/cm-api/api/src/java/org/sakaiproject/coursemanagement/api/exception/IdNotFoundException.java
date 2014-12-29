/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.coursemanagement.api.exception;

/**
 * An exception thrown when an an object lookup fails because an object with the ID does not exist.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class IdNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -8588237050380289434L;

	/**
	 * Constructs an IdNotFoundException with a generic message (useful when the
	 * more specific constructor isn't suitable, because we don't know what kind of ID
	 * wasn't found).
	 * 
	 * @param message
	 */
	public IdNotFoundException(String message) {
		super(message);
	}

	/**
	 * Constructs an IdNotFoundException indicating the class for which the id could
	 * not be located.
	 * 
	 * @param id The ID that can't be found
	 * @param className The class of object for which we were looking.
	 */
	public IdNotFoundException(String id, String className) {
		super("No " + className + " found with id " + id);
	}
}
