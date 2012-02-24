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
 * Encodes PortletState into a serialized form.
 * 
 * @since Sakai 2.2.4
 * @version $Rev$
 */
public interface PortletStateEncoder
{

	/**
	 * Encode the PortletState into a string
	 * 
	 * @param portletState
	 *        the current portlet state
	 * @return serialized form of the state.
	 */
	String encode(PortletState portletState);

	/**
	 * Encode decode the PortletState from the serialized form.
	 * 
	 * @param serialized
	 * @return materialized portlet state.
	 */
	PortletState decode(String serialized);
}
