/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.importer.impl;

import org.sakaiproject.archive.api.ImportMetadata;

public class BasicImportMetadata implements ImportMetadata {
	private String fileName;
	private String id;
	private String legacyTool;
	private String sakaiServiceName;
	private String sakaiTool;
	private boolean isMandatory;
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public boolean isMandatory() {
		return isMandatory;
	}
	public void setMandatory(boolean isMandatory) {
		this.isMandatory = isMandatory;
	}
	public String getLegacyTool() {
		return legacyTool;
	}
	public void setLegacyTool(String legacyTool) {
		this.legacyTool = legacyTool;
	}
	public String getSakaiServiceName() {
		return sakaiServiceName;
	}
	public void setSakaiServiceName(String sakaiServiceName) {
		this.sakaiServiceName = sakaiServiceName;
	}
	public String getSakaiTool() {
		return sakaiTool;
	}
	public void setSakaiTool(String sakaiTool) {
		this.sakaiTool = sakaiTool;
	}

}
