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

package org.sakaiproject.jcr.test.mock;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.FunctionManager;

/**
 * @author ieb
 *
 */
public class MockFunctionManager implements FunctionManager
{

	private static final Log log = LogFactory.getLog(MockFunctionManager.class);

	/* (non-Javadoc)
	 * @see org.sakaiproject.authz.api.FunctionManager#getRegisteredFunctions()
	 */
	public List getRegisteredFunctions()
	{
		return new ArrayList();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.authz.api.FunctionManager#getRegisteredFunctions(java.lang.String)
	 */
	public List getRegisteredFunctions(String prefix)
	{
		return new ArrayList();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.authz.api.FunctionManager#registerFunction(java.lang.String)
	 */
	public void registerFunction(String function)
	{
		log.info("Registering "+function);
	}

}
