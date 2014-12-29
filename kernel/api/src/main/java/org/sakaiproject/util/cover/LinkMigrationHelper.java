package org.sakaiproject.util.cover;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;




public class LinkMigrationHelper {

	private static org.sakaiproject.util.api.LinkMigrationHelper thisLinkMigrationHelper=null;
	
	private static final Log LOG = LogFactory.getLog(LinkMigrationHelper.class);
	

	private static org.sakaiproject.util.api.LinkMigrationHelper getLinkMigrationHelper(){
		if(thisLinkMigrationHelper==null){
			thisLinkMigrationHelper = (org.sakaiproject.util.api.LinkMigrationHelper) ComponentManager.get(org.sakaiproject.util.api.LinkMigrationHelper.class);
		}
		return thisLinkMigrationHelper;
	}
	
	public static String bracketAndNullifySelectedLinks(String m) throws Exception {
		
		return getLinkMigrationHelper().bracketAndNullifySelectedLinks(m);
	}
	
	
	public static String migrateAllLinks(Set entrySet, String msgBody){
		return getLinkMigrationHelper().migrateAllLinks(entrySet, msgBody);
	}
	
	public static String migrateOneLink(String fromContextRef, String targetContextRef, String msgBody){
		
		return getLinkMigrationHelper().migrateOneLink(fromContextRef, targetContextRef, msgBody);
	}
	


}
