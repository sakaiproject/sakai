/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.sitemanage.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sitemanage.api.AffiliatedSectionProvider;

/**
 * @author zqian
 *
 */
public class AffiliatedSectionProviderImpl implements AffiliatedSectionProvider {
	

	private static final Log log = LogFactory.getLog(AffiliatedSectionProviderImpl.class);
	
	public List getAffiliatedSectionEids(String userEid, String academicSessionEid)
	{
		return null;
	}
	
	public void init() {
	}

	public void destroy() {
	}
}
