/**********************************************************************************
 * $URL:  $
 * $Id: BasicContentPrintService.java 132652 2013-12-17 03:15:15Z zqian@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.content.impl;

import java.util.List;
import java.util.HashMap;

import org.sakaiproject.content.api.*;

/**
 * <p>
 * BasicContentService is an default implementation of the Sakai ContentPrintService.
 * </p>
 */
public class BasicContentPrintService implements ContentPrintService
{
	/**
	 * {@inheritDoc}
	*/
	public boolean isPrintable(ContentResource r)
	{
		// default to be false
		return false;
	}

	/**
	 * {@inheritDoc}
	*/
	public HashMap<String, String> printResource(ContentResource r, List<Object> params)
	{
		return null;
	}
} // BaseContentService
