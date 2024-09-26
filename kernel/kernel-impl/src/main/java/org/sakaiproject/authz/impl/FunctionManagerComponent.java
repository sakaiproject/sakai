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

package org.sakaiproject.authz.impl;

import java.util.List;
import java.util.Vector;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.FunctionManager;

/**
 * <p>
 * FunctionManagerComponent implements the FunctionMananger API.
 * </p>
 * 
 * @author Sakai Software Development Team
 */
@Slf4j
public class FunctionManagerComponent implements FunctionManager
{
	/** List of security functions. */
	protected List<String> m_registeredFunctions = new Vector<>();

	/** List of user-mutable security functions. */
	protected List<String> m_registeredUserMutableFunctions = new Vector<>();

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		log.info("init()");
	}

	/**
	 * Final cleanup.
	 */
	public void destroy()
	{
		log.info("destroy()");
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Work interface methods: FunctionMananger
	 *********************************************************************************************************************************************************************************************************************************************************/

	@Override
	public void registerFunction(String function)
	{
		registerFunction(function, false);
	}

	@Override
	public void registerFunction(String function, boolean userMutable) {
		if (function == null) return;

		m_registeredFunctions.add(function);
		
		if (userMutable) {
			m_registeredUserMutableFunctions.add(function);
		}
	}

	@Override
	public List<String> getRegisteredFunctions()
	{
		return new Vector<>(m_registeredFunctions);
	}

	@Override
	public List<String> getRegisteredFunctions(String prefix)
	{
		List<String> rv = new Vector<>();

		for (String function : m_registeredFunctions)
		{
			if (function.startsWith(prefix))
			{
				rv.add(function);
			}
		}

		return rv;
	}

	@Override
	public List<String> getRegisteredUserMutableFunctions() {
		return new Vector<>(m_registeredUserMutableFunctions);
	}

	@Override
	public List<String> getRegisteredUserMutableFunctions(String prefix) {
		
		List<String> rv = new Vector<>();

		for (String function : m_registeredUserMutableFunctions)
		{
			if (function.startsWith(prefix))
			{
				rv.add(function);
			}
		}

		return rv;
	}
}
