/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
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

package uk.ac.cam.caret.sakai.rwiki.component;

import org.sakaiproject.util.ResourceLoader;

/**
 * @author ieb
 *
 */
public class Messages
{
	private static final String BUNDLE_NAME = "uk.ac.cam.caret.sakai.rwiki.component.bundle.Messages"; //$NON-NLS-1$

	private static final ResourceLoader rl = new ResourceLoader(BUNDLE_NAME);

	private Messages()
	{
	}

	public static String getString(String key)
	{
		return rl.getString(key);
	}
}
