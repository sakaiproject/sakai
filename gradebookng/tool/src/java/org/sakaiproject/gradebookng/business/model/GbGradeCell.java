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
package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * Tracks the coordinates of the cell (student and assignment) and the last edit on this cell
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GbGradeCell implements Serializable {

	private static final long serialVersionUID = 1L;

	@Getter
	private final String studentUuid;

	@Getter
	private final long assignmentId;

	@Getter
	@Setter
	private Date lastUpdated;

	@Getter
	private final String lastUpdatedBy;

	public GbGradeCell(final String studentUuid, final long assignmentId, final String lastUpdatedBy) {
		this.studentUuid = studentUuid;
		this.assignmentId = assignmentId;
		this.lastUpdated = new Date();
		this.lastUpdatedBy = lastUpdatedBy;
	}

}
