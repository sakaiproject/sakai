/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.util;

/**
 * <p>
 * ExternalTrustedEvidence is a utility class that implements the ExternalTrustedEvidence interface.
 * </p>
 */
public class ExternalTrustedEvidence implements org.sakaiproject.user.api.ExternalTrustedEvidence
{
	/** The user identifier string. */
	protected String m_identifier = null;

	/**
	 * Construct, with identifier and password.
	 * 
	 * @param identifier
	 *        The user identifier string.
	 */
	public ExternalTrustedEvidence(String identifier)
	{
		m_identifier = identifier;
	}

	/**
	 * @inheritDoc
	 */
	public String getIdentifier()
	{
		return m_identifier;
	}

	public String toString()
	{
		return this.getClass().getName() + " id: " + m_identifier;
	}
}
