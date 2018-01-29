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

package edu.indiana.lib.osid.component.id;

import lombok.extern.slf4j.Slf4j;

import org.osid.OsidContext;
import org.osid.id.IdException;
import org.osid.shared.SharedException;

/**
 *
 * @inheritDoc
 *
 */
@Slf4j
public class IdManager implements org.osid.id.IdManager
{
	OsidContext context = null;

	java.util.Properties configuration = null;

	public OsidContext getOsidContext() throws IdException
	{
		return null;
	}

	public void assignOsidContext(OsidContext context) throws IdException
	{
		// Nothing to see here folks
	}

	public void assignConfiguration(java.util.Properties configuration) throws IdException
	{
		// Nothing to see here folks
	}

	public org.osid.shared.Id createId() throws IdException
	{
		try
		{
			return new Id();
		}
		catch (SharedException sex)
		{
			throw new IdException(sex.getMessage());
		}
	}

	public org.osid.shared.Id getId(String idString) throws IdException
	{
		if (idString == null)
		{
			throw new IdException(IdException.NULL_ARGUMENT);
		}
		try
		{
			return new Id(idString);
		}
		catch (SharedException sex)
		{
			throw new IdException(sex.getMessage());
		}
	}

	public void osidVersion_2_0() throws IdException
	{
	}
}
