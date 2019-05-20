/**
 * Copyright (c) 2006-2019 The Apereo Foundation
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
package org.sakaiproject.sitestats.api.event.detailed.content;

import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;

/**
 * Interface to tag the various classes that can be resolved from a Content (Resources) event reference
 * @author plukasew
 */
public interface ContentData extends ResolvedEventData
{
	// a deleted resource (no additional info available)
	public static final class Deleted implements ContentData {}
	public static final Deleted DELETED = new Deleted();
}
