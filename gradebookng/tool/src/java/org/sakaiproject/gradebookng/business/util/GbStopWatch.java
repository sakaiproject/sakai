/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
package org.sakaiproject.gradebookng.business.util;

import org.apache.commons.lang.time.StopWatch;

import lombok.extern.slf4j.Slf4j;

/**
 * Stopwatch extension that times pieces of the logic to determine impact on any modifications.
 */
@Slf4j
public class GbStopWatch extends StopWatch {

	public void time(final String msg, final long time) {
		log.debug("Time for [" + msg + "] was: " + time + "ms");
	}

	public void timeWithContext(final String context, final String msg, final long time) {
		log.debug("Time for [" + context + "].[" + msg + "] was: " + time + "ms");
	}
}
