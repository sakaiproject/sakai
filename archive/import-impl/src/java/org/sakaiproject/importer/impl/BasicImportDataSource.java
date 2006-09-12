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
	
	private List itemCategories;
	private Collection items;

	public List getItemCategories() {
		return itemCategories;
	}
	
	public void setItemCategories(List itemCategories) {
		this.itemCategories = itemCategories;
	}

	public Collection getItemsForCategories(List categories) {
		Collection rv = new ArrayList();
		// create the Set of selected archive items
		Set selectedCategories = new HashSet();
		for(Iterator iter = categories.iterator();iter.hasNext();) {
			selectedCategories.add(((ImportMetadata)iter.next()).getLegacyTool());
		}
		
		Importable item;
		for(Iterator iter = items.iterator(); iter.hasNext();) {
			item = (Importable)iter.next();
			if(selectedCategories.contains(item.getLegacyGroup())) {
				rv.add(item);
			}
		}
		return rv;
	}
	
	public void setItems(Collection items) {
		this.items = items;
	}

}
