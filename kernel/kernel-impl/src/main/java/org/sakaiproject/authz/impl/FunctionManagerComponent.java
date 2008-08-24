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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.authz.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.FunctionManager;

/**
 * <p>
 * FunctionManagerComponent implements the FunctionMananger API.
 * </p>
 * 
 * @author Sakai Software Development Team
 */
public class FunctionManagerComponent implements FunctionManager
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(FunctionManagerComponent.class);

	/** List of security functions. */
	protected List m_registeredFunctions = new Vector();

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		M_log.info("init()");
	}

	/**
	 * Final cleanup.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Work interface methods: FunctionMananger
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @inheritDoc
	 */
	public void registerFunction(String function)
	{
		if (function == null) return;

		m_registeredFunctions.add(function);
	}

	/**
	 * @inheritDoc
	 */
	public List getRegisteredFunctions()
	{
		return new Vector(m_registeredFunctions);
	}

	/**
	 * @inheritDoc
	 */
	public List getRegisteredFunctions(String prefix)
	{
		List rv = new Vector();

		for (Iterator i = m_registeredFunctions.iterator(); i.hasNext();)
		{
			String function = (String) i.next();
			if (function.startsWith(prefix))
			{
				rv.add(function);
			}
		}

		return rv;
	}
}
