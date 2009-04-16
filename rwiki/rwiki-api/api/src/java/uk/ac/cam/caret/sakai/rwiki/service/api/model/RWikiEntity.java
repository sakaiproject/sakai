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


package uk.ac.cam.caret.sakai.rwiki.service.api.model;

import org.sakaiproject.entity.api.Entity;
import org.w3c.dom.Element;

public interface RWikiEntity extends Entity
{

	/**
	 * Get the Rwiki Object that the entiy represent
	 * 
	 * @return
	 */

	RWikiObject getRWikiObject();

	/**
	 * Populates the Object using an XML block
	 * 
	 * @param el
	 * @param defaultRealm
	 * @throws Exception
	 */
	void fromXml(Element el, String defaultRealm) throws Exception;

	public static final String RP_ID = "id";

	public static final String RP_OWNER = "owner";

	public static final String RP_REALM = "realm";

	public static final String RP_REFERENCED = "referenced";

	public static final String RP_RWID = "rwid";

	public static final String RP_SHA1 = "sha1";

	public static final String RP_USER = "user";

	public static final String RP_GROUP_ADMIN = "group-admin";

	public static final String RP_GROUP_READ = "group-read";

	public static final String RP_GROUP_WRITE = "group-write";

	public static final String RP_OWNER_ADMIN = "owner-admin";

	public static final String RP_OWNER_READ = "owner-read";

	public static final String RP_OWNER_WRITE = "owner-write";

	public static final String RP_PUBLIC_READ = "public-read";

	public static final String RP_PUBLIC_WRITE = "public-write";

	public static final String RP_REVISION = "revision";

	public static final String RP_VERSION = "version";

	public static final String RP_NAME = "name";

	public static final String RP_CONTAINER = "container";

	/**
	 * Is the entity a container ?
	 * 
	 * @return
	 */
	boolean isContainer();

}
