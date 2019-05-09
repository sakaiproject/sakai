/**
 * Copyright (c) 2006-2018 The Apereo Foundation
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
package org.sakaiproject.sitestats.api.event.detailed.samigo;

import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;

/**
 * Interface to tag the various classes that can be resolved from a Samigo event reference
 * @author plukasew
 */
public interface SamigoData extends ResolvedEventData
{
	// anonymous assessments provide no further details
	public static final class AnonymousAssessment implements SamigoData {}
	public static final AnonymousAssessment ANON_ASSESSMENT = new AnonymousAssessment();
}
