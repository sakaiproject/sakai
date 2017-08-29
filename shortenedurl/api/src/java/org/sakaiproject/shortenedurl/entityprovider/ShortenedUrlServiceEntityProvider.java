/**
 * Copyright (c) 2009-2010 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.shortenedurl.entityprovider;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * EntityProvider interface for the ShortenedUrlService to allow URL shortening via GET requests.
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public interface ShortenedUrlServiceEntityProvider extends EntityProvider {

	public final static String ENTITY_PREFIX = "url";
	
}