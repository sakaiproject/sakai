/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.jcr.jackrabbit;

import java.io.Serializable;
import java.security.Principal;

/**
 * A system principal is a low level principal that gives the session the
 * ability to do anything unchecked.
 * 
 * @author ieb
 */
public class JCRSystemPrincipal implements Principal, Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6346392695482119713L;

	private String name;

	/**
	 * Creates a <code>SystemPrincipal</code>.
	 */
	public JCRSystemPrincipal(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return ("SystemPrincipal");
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj instanceof JCRSystemPrincipal)
		{
			return true;
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return name.hashCode();
	}

	// ------------------------------------------------------------< Principal >
	/**
	 * {@inheritDoc}
	 */
	public String getName()
	{
		return name;
	}

}
