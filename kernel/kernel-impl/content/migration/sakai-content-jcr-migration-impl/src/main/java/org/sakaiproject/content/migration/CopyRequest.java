package org.sakaiproject.content.migration;

import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.migration.api.ContentToJCRCopier;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.jcr.api.JCRService;

public class CopyRequest extends SakaiRequestEmulator implements Runnable {
	private static Log log = LogFactory.getLog(CopyRequest.class);
	
	private static final String ORIGINAL_MIGRATION_EVENT = "ORIGINAL_MIGRATION";

	private ContentToJCRCopier copier;
	private ThingToMigrate thing;
	private JCRService jcrService;
	private ContentHostingService oldContentService;
	
	
	public void init() {
		copier = (ContentToJCRCopier) ComponentManager.get("org.sakaiproject.content.migration.api.ContentToJCRCopier");
		jcrService = (JCRService) ComponentManager.get("org.sakaiproject.jcr.api.JCRService");
		oldContentService = (ContentHostingService) ComponentManager.get("org.sakaiproject.content.api.ContentHostingService.dbservice");
	}
	
	public void destroy() {
	}
	
	public CopyRequest() {
		
	}
	
	public void run() {
		setTestUser(SUPER_USER);
		startEmulatedRequest(SUPER_USER);
		log.info("About to try and migrate: " + thing.contentId + " , " + thing.eventType);
		migrateOneItem();
		endEmulatedRequest();
	}

	private void migrateOneItem()
	{
		setTestUser(SUPER_USER);
		startEmulatedRequest(SUPER_USER);
		// ContentResources in the Original CHS always end with '/'
		Session jcrSession = null;
		try {
			jcrSession = jcrService.login();
	} catch (LoginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(); }
	 catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (thing.contentId.endsWith("/"))
		{ /*
			try {
				oldContentService.getCollection(thing.contentId);
			} catch (IdUnusedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (PermissionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} */
			// This is a ContentCollection
			if (thing.eventType.equals(ORIGINAL_MIGRATION_EVENT))
			{
				copier.copyCollectionFromCHStoJCR(jcrSession, thing.contentId);
			}
			else if (thing.eventType.equals(ContentHostingService.EVENT_RESOURCE_ADD))
			{
				copier.copyCollectionFromCHStoJCR(jcrSession, thing.contentId);
			}
			else if (thing.eventType.equals(ContentHostingService.EVENT_RESOURCE_REMOVE))
			{
				copier.deleteItem(jcrSession, thing.contentId);
			}
			else if (thing.eventType.equals(ContentHostingService.EVENT_RESOURCE_WRITE))
			{
				copier.copyCollectionFromCHStoJCR(jcrSession, thing.contentId);
			}
			
		}
		else
		{ /*
			try {
				oldContentService.getResource(thing.contentId);
			} catch (PermissionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IdUnusedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} */
			// This is a ContentResource
			
			if (thing.eventType.equals(ORIGINAL_MIGRATION_EVENT))
			{
				copier.copyResourceFromCHStoJCR(jcrSession, thing.contentId);
			}
			else if (thing.eventType.equals(ContentHostingService.EVENT_RESOURCE_ADD))
			{
				copier.copyResourceFromCHStoJCR(jcrSession, thing.contentId);
			}
			else if (thing.eventType.equals(ContentHostingService.EVENT_RESOURCE_REMOVE))
			{
				copier.deleteItem(jcrSession, thing.contentId);
			}
			else if (thing.eventType.equals(ContentHostingService.EVENT_RESOURCE_WRITE))
			{
				copier.copyResourceFromCHStoJCR(jcrSession, thing.contentId);
			} 
		}
		//try {
		//	Thread.sleep(1000);
		//} catch (InterruptedException e) {
		//	log.error("Problems while sleeping during CHS->JCR Migration.", e);
		//}
		
		endEmulatedRequest();
	}

	/*
	public void setCopier(ContentToJCRCopier copier) {
		this.copier = copier;
	}

	public void setThing(ThingToMigrate thing) {
		this.thing = thing;
	}

	public void setJcrService(JCRService jcrService) {
		this.jcrService = jcrService;
	}

	public void setOldContentService(ContentHostingService oldContentService) {
		this.oldContentService = oldContentService;
	}
	*/
}
