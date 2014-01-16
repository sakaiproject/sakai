/**********************************************************************************
 * $URL$
 * $Id$
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

package org.sakaiproject.importer.api;

/**
 * Importables are extracted as a collection by a parser, and passed one at a time
 * to one or more handlers to be added to Sakai.
 */
public interface Importable {
	String getGuid();
	/**
	 * Get the type for this importable.
	 * @return A String type, this is used for deciding which handler to use.
	 */
	String getTypeName();
	String getLegacyGroup();
	String getContextPath();
	Importable getParent();
	void setParent(Importable parent);
	void setLegacyGroup(String legacyGroup);
	void setContextPath(String path);
}
