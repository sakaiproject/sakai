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
import org.adl.datamodels.IDataManager;
import org.adl.datamodels.ieee.IValidatorFactory;
import org.adl.sequencer.ISeqActivityTree;
import org.adl.sequencer.ISequencer;
import org.adl.sequencer.IValidRequests;
import org.adl.validator.IValidatorOutcome;
import org.adl.validator.IValidator;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdLengthException;
import org.sakaiproject.exception.IdUniquenessException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.scorm.model.api.ContentPackageManifest;
import org.sakaiproject.scorm.service.api.ScormApplicationService;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormPermissionService;
import org.sakaiproject.scorm.service.api.ScormResultService;
import org.sakaiproject.scorm.service.api.ScormSequencingService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.w3c.dom.Document;

public interface ScormClientFacade extends Serializable {
	public static final String REFERENCE_ROOT = Entity.SEPARATOR + "scorm";
	public static final String SCORM_TOOL_ID="sakai.scorm.tool";
	public static final String SCORM_HELPER_ID="sakai.helper.tool";
	
	public static final String DEFAULT_USER_AUDIO_LEVEL = "1";
	public static final String DEFAULT_USER_AUDIO_CAPTIONING = "0";
	public static final String DEFAULT_USER_DELIVERY_SPEED = "1";
	public static final String DEFAULT_USER_LANGUAGE = "";
	
	public static final String PREF_USER_AUDIO_LEVEL = "1";
	public static final String PREF_USER_AUDIO_CAPTIONING = "0";
	public static final String PREF_USER_DELIVERY_SPEED = "1";
	public static final String PREF_USER_LANGUAGE = "";
	
	
	public ScormApplicationService applicationProgrammingInterface();
	
	public ScormContentService contentPackageInterface();

	public ScormPermissionService permissionInterface();
	
	public ScormResultService resultInterface();
	
	public ScormSequencingService sequencingInterface();
	
	
	/*
	public List<ContentEntity> getContentPackages();
	
	public String addContentPackage(File contentPackage, IValidator validator, IValidatorOutcome outcome) throws Exception;
	
	public String getContentPackageTitle(Document document);
	
	public void validate(String resourceId, boolean isManifestOnly, boolean isValidateToSchema) throws Exception;
	
	public IValidator validate(File contentPackage, boolean iManifestOnly, boolean iValidateToSchema);
	
	public IValidatorOutcome validateContentPackage(File contentPackage, boolean onlyValidateManifest);

	public void removeContentPackage(String contentPackageId);
	
	public ContentResource addManifest(ContentPackageManifest manifest, String id);
	
	//public ISequencer getSequencer(ContentPackageManifest manifest, String userId, String courseId);
	
	public void uploadZipArchive(File zipArchive) throws IdUnusedException, IdUniquenessException, IdLengthException, IdInvalidException, OverQuotaException, ServerOverloadException, PermissionException;
	
	public String identifyZipArchive();
	
	//public void uploadZipEntry(File zipEntry, String path);
	
	public ContentPackageManifest getManifest(String id);
	
	//public String getContext();
	
	//public String getPlacementId();
	
	//public MultiFileUploadPipe getMultiFileUploadPipe();
	
	public ResourceToolActionPipe getResourceToolActionPipe();
	
	public ContentEntity getContentEntity();
	
	//public void grantAlternativeRef(String resourceId);
	
	//public boolean isHelper();
	
	public void closePipe(ResourceToolActionPipe pipe);
	
	public String getCompletionURL();
	
	//public String getUserName();
	
	public HttpAccess getHttpAccess();
	
	//public List getTableOfContents();
	
	public ISequencer getSequencer(ISeqActivityTree tree);
	
	public String getConfigurationString(String key, String defaultValue);
	
	public IErrorManager getErrorManager();
	
	
	public ISeqActivityTree getActivityTree(String contentPackageId, String courseId, String userId, boolean isFresh);
	
	public void persistActivityTree(ISeqActivityTree tree);
	
	public IDataManager initialize(IRunState runState, String numberOfAttempts);
	
	public IValidRequests commit(IRunState runState);
	
	
	public User getCurrentUser();
	
	public User getUser(String userId) throws UserNotDefinedException;
	
	public List<IDataManager> getContentPackageDataManagers(String contentPackageId);
	
	public IDataManager getContentPackageDataManger(String contentPackageId, String userId);
	
	public IValidatorFactory newValidatorFactory();*/
}
