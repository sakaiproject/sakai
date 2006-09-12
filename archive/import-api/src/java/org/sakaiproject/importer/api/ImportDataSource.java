package org.sakaiproject.importer.api;

import java.util.Collection;
import java.util.List;

public interface ImportDataSource {
	List getItemCategories();
	Collection getItemsForCategories(List categories);
	void setItemCategories(List categories);
	void setItems(Collection items);

}
