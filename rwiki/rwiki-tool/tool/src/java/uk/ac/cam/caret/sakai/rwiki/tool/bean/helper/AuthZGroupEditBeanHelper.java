/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package uk.ac.cam.caret.sakai.rwiki.tool.bean.helper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import uk.ac.cam.caret.sakai.rwiki.tool.bean.AuthZGroupEditBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ViewBean;


public class AuthZGroupEditBeanHelper
{

	public static final String REALM_EDIT_BEAN_ATTR = "realmEditBean";

	public static AuthZGroupEditBean createRealmEditBean(
			HttpServletRequest request, ViewBean vb)
	{
		HttpSession session = request.getSession();

		AuthZGroupEditBean rb;
		try
		{
			rb = (AuthZGroupEditBean) session
					.getAttribute(REALM_EDIT_BEAN_ATTR);
		}
		catch (ClassCastException ex)
		{
			rb = null;
		}

		if (rb == null)
		{
			rb = new AuthZGroupEditBean(vb.getPageName(), vb.getLocalSpace());
			session.setAttribute(REALM_EDIT_BEAN_ATTR, rb);
		}

		return rb;
	}

}
