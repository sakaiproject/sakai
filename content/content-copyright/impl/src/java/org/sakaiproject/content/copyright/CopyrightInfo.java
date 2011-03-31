package org.sakaiproject.content.copyright;

import java.util.List;
import java.util.ArrayList;

public class CopyrightInfo implements org.sakaiproject.content.copyright.api.CopyrightInfo {
	List<org.sakaiproject.content.copyright.api.CopyrightItem> items = new ArrayList();
	
	public CopyrightInfo(){
		items = new ArrayList();
	}
	public void add(org.sakaiproject.content.copyright.api.CopyrightItem item){
		items.add(item);
	}
	public List<org.sakaiproject.content.copyright.api.CopyrightItem> getItems(){
		return items;
	}
	
}