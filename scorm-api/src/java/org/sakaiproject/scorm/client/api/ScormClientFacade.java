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
package org.sakaiproject.scorm.client.api;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import org.adl.api.ecmascript.IErrorManager;
import org.adl.sequencer.ISequencer;
import org.adl.validator.IValidatorOutcome;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.MultiFileUploadPipe;
import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.scorm.model.api.ContentPackageManifest;

public interface ScormClientFacade extends EntityProducer, Serializable {
	public static final String REFERENCE_ROOT = Entity.SEPARATOR + "scorm";
	public static final String SCORM_TOOL_ID="sakai.scorm.tool";
	public static final String SCORM_HELPER_ID="sakai.helper.tool";
	
	public List getContentPackages();
	
	//public String addContentPackage(File contentPackage, String mimeType);
	
	public IValidatorOutcome validateContentPackage(File contentPackage, boolean doValidateSchema);

	public ContentResource addManifest(ContentPackageManifest manifest, String id);
	
	public ISequencer getSequencer(ContentPackageManifest manifest);
	
	
	public ContentPackageManifest getManifest(String id);
	
	//public String getContext();
	
	//public String getPlacementId();
	
	//public MultiFileUploadPipe getMultiFileUploadPipe();
	
	public ResourceToolActionPipe getResourceToolActionPipe();
	
	//public void grantAlternativeRef(String resourceId);
	
	//public boolean isHelper();
	
	public void closePipe(ResourceToolActionPipe pipe);
	
	public String getCompletionURL();
	
	//public String getUserName();
	
	public HttpAccess getHttpAccess();
	
	//public List getTableOfContents();
	
	public ISequencer getSequencer();
	
	public String getConfigurationString(String key, String defaultValue);
	
	public IErrorManager getErrorManager();
	
}
