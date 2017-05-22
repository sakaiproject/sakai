/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.component.osid.id;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IdManager implements org.osid.id.IdManager
{
	org.osid.OsidContext context = null;

	java.util.Properties configuration = null;

	public org.osid.OsidContext getOsidContext() throws org.osid.id.IdException
	{
		return null;
	}

	public void assignOsidContext(org.osid.OsidContext context) throws org.osid.id.IdException
	{
		// Nothing to see here folks
	}

	public void assignConfiguration(java.util.Properties configuration) throws org.osid.id.IdException
	{
		// Nothing to see here folks
	}

	private void log(String entry) throws org.osid.id.IdException
	{
		log.debug("IdManager.log() entry: " + entry);
	}

	public org.osid.shared.Id createId() throws org.osid.id.IdException
	{
		try
		{
			return new Id();
		}
		catch (org.osid.shared.SharedException sex)
		{
			throw new org.osid.id.IdException(sex.getMessage());
		}
	}

	public org.osid.shared.Id getId(String idString) throws org.osid.id.IdException
	{
		if (idString == null)
		{
			throw new org.osid.id.IdException(org.osid.id.IdException.NULL_ARGUMENT);
		}
		try
		{
			return new Id(idString);
		}
		catch (org.osid.shared.SharedException sex)
		{
			throw new org.osid.id.IdException(sex.getMessage());
		}
	}

	public void osidVersion_2_0() throws org.osid.id.IdException
	{
	}
}

/**************************************************************************************************************************************************************************************************************************************************************
 * $Footer: $
 *************************************************************************************************************************************************************************************************************************************************************/
