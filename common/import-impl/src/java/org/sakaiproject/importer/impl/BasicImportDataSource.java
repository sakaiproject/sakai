/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.importer.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.sakaiproject.archive.api.ImportMetadata;
import org.sakaiproject.importer.api.ImportDataSource;
import org.sakaiproject.importer.api.Importable;

public class BasicImportDataSource implements ImportDataSource {
	
	private List<ImportMetadata> itemCategories;
	private Collection<Importable> items;

	public List<ImportMetadata> getItemCategories() {
		return itemCategories;
	}
	
	public void setItemCategories(List<ImportMetadata> itemCategories) {
		this.itemCategories = itemCategories;
	}

	public Collection<Importable> getItemsForCategories(List<ImportMetadata> categories) {
		Collection<Importable> rv = new ArrayList<Importable>();
		// create the Set of selected archive items
		Set<String> selectedCategories = new HashSet<String>();
		for(Iterator<ImportMetadata> iter = categories.iterator();iter.hasNext();) {
			selectedCategories.add(iter.next().getLegacyTool());
		}
		
		Importable item;
		for(Iterator<Importable> iter = items.iterator(); iter.hasNext();) {
			item = iter.next();
			if("mandatory".equals(item.getLegacyGroup()) || "".equals(item.getLegacyGroup()) || selectedCategories.contains(item.getLegacyGroup())) {
				rv.add(item);
			}
		}
		return rv;
	}
	
	public void setItems(Collection<Importable> items) {
		this.items = items;
	}

	public void cleanup() {
		//items.iterator().next().get
		
	}

}
