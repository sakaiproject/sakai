/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation, The MIT Corporation
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

package org.sakaiproject.tool.gradebook.facades.standalone;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.ServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.gradebook.facades.ContextManagement;

/**
 * An implementation of the ContextManagement facade to support demos and UI tests.
 */
public class ContextManagementStandaloneImpl implements ContextManagement {
	private static Log logger = LogFactory.getLog(ContextManagementStandaloneImpl.class);

	private static final String GRADEBOOK_UID_PARAM = "gradebookUid";

	public String getGradebookUid(Object request) {
		String gradebookUid = (String)((ServletRequest)request).getParameter(GRADEBOOK_UID_PARAM);
		if (gradebookUid != null) {
			try {
				gradebookUid = URLDecoder.decode(gradebookUid, "UTF-8");
			} catch (UnsupportedEncodingException ex) {
				logger.error("Unlikely exception thrown", ex);
			}
		}
		if (logger.isDebugEnabled()) logger.debug("getGradebookUid returning " + gradebookUid);
		return gradebookUid;
	}
}



