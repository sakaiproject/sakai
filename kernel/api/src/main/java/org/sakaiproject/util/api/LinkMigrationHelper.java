package org.sakaiproject.util.api;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



public interface LinkMigrationHelper {
	
	public String bracketAndNullifySelectedLinks(String m) throws Exception ;
			
	public String migrateAllLinks(Set entrySet, String msgBody);
	
	public String migrateOneLink(String fromContextRef, String targetContextRef, String msgBody);


}
