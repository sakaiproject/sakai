/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/ufp/usermembership/trunk/api/src/java/org/sakaiproject/umem/api/Authz.java $
 * $Id: Authz.java 4297 2007-03-16 12:22:04Z nuno@ufp.pt $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.umem.api;

public interface Authz {

	/** Permissions */
	public static final String	PERMISSION_UMEM_VIEW		= "usermembership.view";

	// ################################################################
	// Public methods
	// ################################################################
	public boolean isUserAbleToViewUmem(String siteId);

}