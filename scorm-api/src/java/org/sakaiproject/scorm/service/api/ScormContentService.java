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

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.adl.validator.IValidatorOutcome;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdLengthException;
import org.sakaiproject.exception.IdUniquenessException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.scorm.model.api.ContentPackageManifest;

public interface ScormContentService {
		
	public ContentResource addManifest(ContentPackageManifest manifest, String id);
	
	public List getContentPackages();
	
	public ContentPackageManifest getManifest(String contentPackageId);
	
	public InputStream getManifestAsStream(String contentPackageId);
	
	public IValidatorOutcome validateContentPackage(File contentPackage, boolean onlyValidateManifest);
	
	public void uploadZipArchive(File zipArchive) throws IdUnusedException, IdUniquenessException, IdLengthException, IdInvalidException, OverQuotaException, PermissionException, ServerOverloadException;
	
	public String identifyZipArchive();
	
	//public void uploadZipEntry(File zipEntry, String path);
}
