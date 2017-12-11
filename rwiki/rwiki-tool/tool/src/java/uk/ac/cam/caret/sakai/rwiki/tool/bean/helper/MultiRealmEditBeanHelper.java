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

import lombok.extern.slf4j.Slf4j;

import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.tool.RequestScopeSuperBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.MultiRealmEditBean;
import uk.ac.cam.caret.sakai.rwiki.utils.NameHelper;

@Slf4j
public class MultiRealmEditBeanHelper
{

	public static MultiRealmEditBean createMultiRealmEditBean( 
			RWikiObject currentRWikiObject,
			HttpServletRequest request, RequestScopeSuperBean rsac)
	{

		String localSpace = NameHelper.localizeSpace(currentRWikiObject.getName(),currentRWikiObject.getRealm());
		String pageName = NameHelper.localizeName(currentRWikiObject.getName(),localSpace);
		MultiRealmEditBean multiRealmEditBean = new MultiRealmEditBean();
		multiRealmEditBean.setLocalSpace(localSpace);
		multiRealmEditBean.setPageName(pageName);
		multiRealmEditBean.setRequest(request);
		multiRealmEditBean.setRequestScopeSuperBean(rsac);
		return multiRealmEditBean;
	}

}
