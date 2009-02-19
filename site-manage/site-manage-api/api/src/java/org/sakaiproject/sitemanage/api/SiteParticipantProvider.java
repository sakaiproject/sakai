package org.sakaiproject.sitemanage.api;

import org.sakaiproject.entity.api.EntityProducer;

public interface SiteParticipantProvider extends EntityProducer{
	
	/** This string can be used to find the service in the service manager. */
	public static final String SERVICE_NAME = SiteParticipantProvider.class.getName();

	/** This string starts the references to resources in this service. */
	public static final String REFERENCE_ROOT = "/participant";
	
	/** The type string for this application: should not change over time as it may be stored in various parts of persistent entities. */
	static final String APPLICATION_ID = "sakai:participant";

}
