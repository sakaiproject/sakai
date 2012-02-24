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
import org.sakaiproject.event.api.NotificationAction;

/**
 * A Rule extends <code>org.apache.commons.collections.Predicate</code> and
 * decorates it with an enum that represents the relationship among the
 * Predicates within the rule: the Predicates that make up a rule are
 * either AND'd together or OR'd together.
 * 
 * A Rule is also a <code>Command</code>, which means it has an execute method that can
 * be invoked to perform some action.
 * 
 * @author Zach A. Thomas <zach@aeroplanesoftware.com>
 */
public interface Rule extends Predicate, NotificationAction {

	/**
	 * A Rule may have an AND relationship among its Predicates, or it may have an OR.
	 * This enumeration is a convenience for setting this relationship on the Rule.
	 *
	 */
	enum Conjunction {
		AND, OR
	};
	
}
