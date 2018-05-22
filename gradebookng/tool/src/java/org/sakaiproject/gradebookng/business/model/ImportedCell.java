/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
 * Represents a cell in the imported spreadsheet
 */
@ToString
public class ImportedCell implements Serializable {

	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private String score;

	@Getter
	@Setter
	private String rawScore;

	@Getter
	@Setter
	private String comment;

	public ImportedCell()
	{
		rawScore = "";
	}

}
