/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/samigo-services/src/java/org/sakaiproject/tool/assessment/facade/AssessmentGradingFacadeQueriesAPI.java $
 * $Id: AssessmentGradingFacadeQueriesAPI.java 120911 2013-03-07 22:32:47Z ktsao@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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
package org.sakaiproject.tool.assessment.data.exception;

/**
 * An exception from a error accessing or modifying Data in storage
 * This needs to extend RuntimeException so as to maintain correct Hibernate
 * Behaviour regarding rollbacks.
 * @author dhorwitz
 * @since 2.10
 */
public class SamigoDataAccessException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8292080017823847011L;

	/**
	 * 
	 * @param cause
	 */
	public SamigoDataAccessException(Throwable cause) {
		super(cause);
	}
	
	
}
