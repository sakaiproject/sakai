/**
 * Copyright (c) 2007 The Apereo Foundation
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
package org.sakaiproject.scorm.model.api;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

public class Progress implements Serializable
{
	private static final long serialVersionUID = 1L;

	// cmi.progress_measure
	@Getter @Setter private double progressMeasure;

	// cmi.completion_threshold
	@Getter @Setter private double completionThreshold;

	// cmi.completion_status
	@Getter @Setter private String completionStatus;

	// cmi.success_status
	@Getter @Setter private String successStatus;

	// cmi.location
	@Getter @Setter private String learnerLocation;

	// cmi.max_time_allowed
	@Getter @Setter private long maxSecondsAllowed;

	// cmi.total_time
	@Getter @Setter private String totalSessionSeconds;
}
