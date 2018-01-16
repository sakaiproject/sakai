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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Encapsulates the details of an imported grade for a student and any comment
 *
 * TODO refactor to ProcessedCell
 */
@ToString
public class ProcessedGradeItemDetail implements Serializable {

	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private GbUser user;

	@Getter
	@Setter
	private String grade;

	@Getter
	@Setter
	private String comment;
}
