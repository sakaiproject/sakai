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

import java.util.Collection;
import java.util.List;

import org.sakaiproject.archive.api.ImportMetadata;

/**
 * An ImportDataSource is an object that acts as a container for the Importable objects in an archive.
 * You can think of it as the abstract representation of some archive.
 * When an archive has been parsed an ImportDataSource is returned that allows
 * the handlers to the be called passing Importable items.
 */
public interface ImportDataSource {
	List<ImportMetadata> getItemCategories();
	Collection<Importable> getItemsForCategories(List<ImportMetadata> categories);
	void setItemCategories(List<ImportMetadata> categories);
	void setItems(Collection<Importable> items);

	/**
	 * This should be called when the content has been imported and is no longer needed.
	 */
	void cleanup();
}
