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

package org.sakaiproject.importer.impl.importables;

import java.io.InputStream;

public class FileResource extends AbstractImportable {
	private String destinationResourcePath;
	private String contentType;
	private String title;
	private String description;
	private String fileName;
    private InputStream fileInputStream;
    
	public String getTypeName() {
		return "sakai-file-resource";
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDestinationResourcePath() {
		return destinationResourcePath;
	}

	public void setDestinationResourcePath(String destinationResourcePath) {
		this.destinationResourcePath = destinationResourcePath;
	}

	public InputStream getInputStream() {
		return fileInputStream;
	}

	public void setInputStream(InputStream fileInputStream) {
		this.fileInputStream = fileInputStream;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
