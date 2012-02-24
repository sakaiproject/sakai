/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.portal.api;

/**
 * This interface should be implemented by a Stylable provider to control the portal in 
 * the browser either by CSS or by javascript manipulation of the DOM in the browser.
 * 
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 * 
 */
public interface StyleAbleProvider
{

	/**
	 * Generate the javascript in the header for the user
	 * 
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	String generateJavaScript(String userId) throws Exception;

	/**
	 * Generate the StyleSheet in the header for the user
	 * 
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	String generateStyleSheet(String userId) throws Exception;

}
