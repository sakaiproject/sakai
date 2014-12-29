/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.portal.render.portlet.services.state;

/**
 * An encoder used to recode an allready encoded set of bytes in order to ensure
 * the bits can be used in a url.
 * 
 * @since Sakai 2.2.4
 * @version $Rev$
 */
public interface WebRecoder
{

	/**
	 * Recode the bits into a websafe version.
	 * 
	 * @param bits
	 *        the original bits
	 * @return websafe version
	 */
	String encode(byte[] bits);

	/**
	 * Decode the bits into a websafe version.
	 * 
	 * @param encoded
	 *        websafe version
	 * @return the original encoded bits
	 */
	byte[] decode(String encoded);
}
