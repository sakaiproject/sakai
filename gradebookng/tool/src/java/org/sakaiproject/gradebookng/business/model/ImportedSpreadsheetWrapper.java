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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import org.sakaiproject.gradebookng.business.importExport.HeadingValidationReport;
import org.sakaiproject.gradebookng.business.importExport.UsernameIdentifier;

/**
 * Wraps an imported file
 */
public class ImportedSpreadsheetWrapper implements Serializable {

	private static final long serialVersionUID = 1L;

	@Getter
	private List<ImportedRow> rows;

	@Getter
	@Setter
	private List<ImportedColumn> columns;

	@Getter
	private UsernameIdentifier userIdentifier;

	@Getter
	private final HeadingValidationReport headingReport;

	public ImportedSpreadsheetWrapper() {
		rows = new ArrayList<>();
		columns = new ArrayList<>();
		userIdentifier = null;
		headingReport = new HeadingValidationReport();
	}

	public void setRows(List<ImportedRow> rows, Map<String, GbUser> rosterMap) {
		this.rows = rows;
		userIdentifier = new UsernameIdentifier(rows, rosterMap);
	}
}
