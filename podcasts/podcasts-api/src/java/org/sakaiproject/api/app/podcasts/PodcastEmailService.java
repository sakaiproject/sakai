package org.sakaiproject.api.app.podcasts;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;

public interface PodcastEmailService extends EntityProducer {

	public static final String APPLICATION_ID = "sakai:podcasts";
	
	public static final String EVENT_PODCAST_ADD = "podcasts.add";
	
	public static final String EVENT_PODCAST_REVISE = "podcasts.revise";
	
	public static final String REFERENCE_ROOT = Entity.SEPARATOR + "podcasts";
	
	public static final String SYLLABUS_SERVICE_NAME = "org.sakaiproject.api.app.podcasts.PodcastEmailService";

}
