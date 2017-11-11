/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.cheftool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.util.Validator;

/**
 * <p>
 * VmServlet adds a standard validator to the VmServlet from the velocity module {@link org.sakaiproject.vm.VmServlet}.
 * </p>
 */
public abstract class VmServlet extends org.sakaiproject.vm.VmServlet
{

	private static final long serialVersionUID = 1L;
	
	/** A validator. */
	protected final Validator m_validator = new Validator();

	/**
	 * Add some standard references to the vm context.
	 * 
	 * @param request
	 *        The request.
	 * @param response
	 *        The response.
	 */
	protected void setVmStdRef(HttpServletRequest request, HttpServletResponse response)
	{
		super.setVmStdRef(request, response);

		// include some standard references
		setVmReference("sakai_Validator", m_validator, request);
	}
}
