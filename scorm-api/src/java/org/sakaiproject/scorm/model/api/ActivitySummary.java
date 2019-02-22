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

public class ActivitySummary implements Serializable
{
	private static final long serialVersionUID = 1L;

	// General data
	@Setter @Getter private String scoId;
	@Setter @Getter private String title;
	@Setter @Getter private String learnerId;
	@Setter @Getter private long contentPackageId;
	@Setter @Getter private long attemptNumber;

	// Progress data
	@Setter @Getter private double progressMeasure;
	@Setter @Getter private double completionThreshold;
	@Setter @Getter private String completionStatus;
	@Setter @Getter private String successStatus;
	@Setter @Getter private String learnerLocation;
	@Setter @Getter private String totalSessionSeconds;
	@Setter @Getter private long maxSecondsAllowed;

	// Score data
	@Setter @Getter private double scaled;
	@Setter @Getter private double raw;
	@Setter @Getter private double min;
	@Setter @Getter private double max;
	@Setter @Getter private double scaledToPass;
}
