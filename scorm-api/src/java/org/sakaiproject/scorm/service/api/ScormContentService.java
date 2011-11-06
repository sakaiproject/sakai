/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.scorm.service.api;

import java.util.List;

import org.sakaiproject.scorm.api.ScormConstants;
import org.sakaiproject.scorm.exceptions.ResourceNotDeletedException;
import org.sakaiproject.scorm.exceptions.ResourceStorageException;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.w3c.dom.Document;

public interface ScormContentService extends ScormConstants {

	public ContentPackage getContentPackage(long contentPackageId) throws ResourceStorageException;
	
	public ContentPackage getContentPackageByResourceId(String resourceId) throws ResourceStorageException;
	
	public List<ContentPackage> getContentPackages() throws ResourceStorageException;
	
	public int getContentPackageStatus(ContentPackage contentPackage);
	
	public String getContentPackageTitle(Document document);

	public void removeContentPackage(long contentPackageId) throws ResourceNotDeletedException;
	
	public void updateContentPackage(ContentPackage contentPackage) throws ResourceStorageException;
	
	public int validate(String resourceId, boolean isManifestOnly, boolean isValidateToSchema) throws ResourceStorageException;

}
