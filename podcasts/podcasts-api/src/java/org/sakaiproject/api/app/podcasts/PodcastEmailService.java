/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.api.app.podcasts;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;

public interface PodcastEmailService extends EntityProducer {

	public static final String APPLICATION_ID = "sakai:podcasts";
	
	public static final String EVENT_PODCAST_ADD = "podcasts.add";
	
	public static final String EVENT_PODCAST_REVISE = "podcasts.revise";
	
	public static final String REFERENCE_ROOT = Entity.SEPARATOR + "podcasts";
	
	public static final String PODCASTS_SERVICE_NAME = "org.sakaiproject.api.app.podcasts.PodcastEmailService";

}
