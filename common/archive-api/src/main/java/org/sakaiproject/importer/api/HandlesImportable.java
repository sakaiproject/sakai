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

package org.sakaiproject.importer.api;

/**
 * A handler takes a vendor-neutral content object and stuffs it into a particular Sakai tool.
 * A handler belongs to one and only one Sakai tool. A handler may
 * be called upon many times in the process of importing a single archive.
 */
public interface HandlesImportable {
	
	/**
	 * Can this handler deal with this importable type.
	 * @param typeName The type of the importable.
	 * @return <code>true</code> if this handler can handle this type.
	 */
	boolean canHandleType(String typeName);
	
	/**
	 * Import the supplied importable into the site.
	 * @param thing The Importable to be imported.
	 * @param siteId The site ID into which 
	 */
	void handle(Importable thing, String siteId);

}
