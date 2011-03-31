package org.sakaiproject.content.copyright.api;

import java.util.List;

public interface CopyrightInfo{
	
	public void add(CopyrightItem item);
	public List<CopyrightItem> getItems();
	
}