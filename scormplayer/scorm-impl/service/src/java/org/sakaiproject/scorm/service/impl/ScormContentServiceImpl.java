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
package org.sakaiproject.scorm.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.adl.parsers.dom.DOMTreeUtility;
import org.adl.sequencer.ADLSeqUtilities;
import org.adl.sequencer.ISeqActivityTree;
import org.adl.validator.IValidator;
import org.adl.validator.IValidatorOutcome;
import org.adl.validator.contentpackage.CPValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.scorm.api.ScormConstants;
import org.sakaiproject.scorm.dao.LearnerDao;
import org.sakaiproject.scorm.dao.api.ContentPackageDao;
import org.sakaiproject.scorm.dao.api.ContentPackageManifestDao;
import org.sakaiproject.scorm.dao.api.DataManagerDao;
import org.sakaiproject.scorm.exceptions.InvalidArchiveException;
import org.sakaiproject.scorm.exceptions.ResourceNotDeletedException;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.ContentPackageManifest;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class ScormContentServiceImpl implements ScormContentService, ScormConstants {
	private static Log log = LogFactory.getLog(ScormContentServiceImpl.class);
		
	// Dependency injected lookup methods
	protected abstract ScormResourceService resourceService();
	
	protected abstract LearningManagementSystem lms();
	
	// Data access objects (also dependency injected by lookup method)
	protected abstract ContentPackageDao contentPackageDao();
	protected abstract ContentPackageManifestDao contentPackageManifestDao();
	protected abstract DataManagerDao dataManagerDao();
	protected abstract LearnerDao learnerDao();
	
	public ContentPackage getContentPackage(long contentPackageId) {
		return contentPackageDao().load(contentPackageId);
	}
	
	public List<ContentPackage> getContentPackages() {
		String context = lms().currentContext();
		
		List<ContentPackage> allPackages = contentPackageDao().find(context);
		List<ContentPackage> releasedPackages = new LinkedList<ContentPackage>();
		
		if (lms().canModify(context))
			return allPackages;
		
		for (ContentPackage cp : allPackages) {
			if (cp.isReleased())
				releasedPackages.add(cp);
		}

		return releasedPackages;
	}
	
	public int getContentPackageStatus(ContentPackage contentPackage) {
		int status = CONTENT_PACKAGE_STATUS_UNKNOWN;
		Date now = new Date();
		
		if (now.after(contentPackage.getReleaseOn())) {
			if (contentPackage.getDueOn() == null || contentPackage.getAcceptUntil() == null)
				status = CONTENT_PACKAGE_STATUS_OPEN;			
			else if (now.before(contentPackage.getDueOn())) 
				status = CONTENT_PACKAGE_STATUS_OPEN;
			else if (now.before(contentPackage.getAcceptUntil()))
				status = CONTENT_PACKAGE_STATUS_OVERDUE;
			else 
				status = CONTENT_PACKAGE_STATUS_CLOSED;
		} else {
			status = CONTENT_PACKAGE_STATUS_NOTYETOPEN;
		}
		
		return status;
	}
	
	public String getContentPackageTitle(Document document) {
		String title = null;
		try {
			Node orgRoot = document.getElementsByTagName("organizations").item(0);
			String defaultId = DOMTreeUtility.getAttributeValue(orgRoot, "default");
			
			NodeList orgs = document.getElementsByTagName("organization");
	
			Node defaultNode = null;
			for (int i = 0; i < orgs.getLength(); ++i) {
				if (DOMTreeUtility.getAttributeValue(orgs.item(i), "identifier")
						.equalsIgnoreCase(defaultId)) {
					defaultNode = orgs.item(i);
					break;
				}
			}
			List<Node> titleNodes = DOMTreeUtility.getNodes(defaultNode, "title");
			
			
			if (!titleNodes.isEmpty())
				title = DOMTreeUtility.getNodeValue(titleNodes.get(0));
		
		} catch (Exception e) {
			log.warn("Caught an exception looking for content package title");
		}
		
		if (null == title) 
			title = "Unknown";
		
		return title;
	}
	
	
	public void removeContentPackage(long contentPackageId) throws ResourceNotDeletedException {

		ContentPackage contentPackage = contentPackageDao().load(contentPackageId);
		
		contentPackageDao().remove(contentPackage);
		
		resourceService().removeResources(contentPackage.getResourceId());
		
	}
	
	public void updateContentPackage(ContentPackage contentPackage) {
		String learnerId = lms().currentLearnerId();
		
		if (learnerId == null)
			learnerId = "unknown";
			
		contentPackage.setModifiedBy(learnerId);
		contentPackage.setModifiedOn(new Date());
		
		contentPackageDao().save(contentPackage);
	}
	
	public int validate(String resourceId, boolean isManifestOnly, boolean isValidateToSchema) {
		File file = createFile(resourceService().getArchiveStream(resourceId));
		
		int result = VALIDATION_SUCCESS;
		
		if (!file.exists()) {
			return VALIDATION_NOFILE;
		}
		
		IValidator validator = validate(file, isManifestOnly, isValidateToSchema);		
		IValidatorOutcome validatorOutcome = validator.getADLValidatorOutcome();

		if (!validatorOutcome.getDoesIMSManifestExist()) {
			return VALIDATION_NOMANIFEST;
		}
		
		if (!validatorOutcome.getIsWellformed()) {
			result = VALIDATION_NOTWELLFORMED;
		}
		
		if (!validatorOutcome.getIsValidRoot()) {
			result = VALIDATION_NOTVALIDROOT;
		}
		
		if (isValidateToSchema) {
			if (!validatorOutcome.getIsValidToSchema()) {
				result = VALIDATION_NOTVALIDSCHEMA;
			}
			
			if (!validatorOutcome.getIsValidToApplicationProfile()) {
				result = VALIDATION_NOTVALIDPROFILE;
			}
			
			if (!validatorOutcome.getDoRequiredCPFilesExist()) {
				result = VALIDATION_MISSINGREQUIREDFILES;
			}
		}
		
		try {
			convertToContentPackage(resourceId, validator, validatorOutcome);
		} catch (InvalidArchiveException iae) {
			return VALIDATION_WRONGMIMETYPE;
		} catch (Exception e) {
			log.error("Failed to convert content package for resourceId: " + resourceId, e);
			return VALIDATION_CONVERTFAILED;
		}
			
		return result;
	}
	
	public IValidator validate(File contentPackage, boolean iManifestOnly, boolean iValidateToSchema) {
		String directoryPath = contentPackage.getParent();
		IValidator validator = new CPValidator(directoryPath);
		((CPValidator)validator).setSchemaLocation(directoryPath);
		validator.setPerformValidationToSchema(iValidateToSchema);
		validator.validate(contentPackage.getPath(), "pif", "contentaggregation", iManifestOnly);

		return validator;
	}
	
	

	// Helper methods
	
	/**
	 * Takes the identifier for a content package that's been stored in the content repository
	 * and creates the necessary objects in the database to make it recognizable as a content
	 * package. 
	 */
	private void convertToContentPackage(String resourceId, IValidator validator, IValidatorOutcome outcome) throws Exception {

		ContentPackageManifest manifest = createManifest(outcome.getDocument(), validator);
		
		// Grab some important info about the site and user
	    String context = lms().currentContext();
	    String learnerId = lms().currentLearnerId();
	    Date now = new Date();
		
		String title = getContentPackageTitle(outcome.getDocument());
		
		int packageCount = contentPackageDao().countContentPackages(context, title);
		
		if (packageCount > 1) {
			title = new StringBuilder(title).append(" (").append(packageCount).append(")").toString();
		}
		
		String archiveId = resourceService().convertArchive(resourceId, title);
	
	    
	    Serializable manifestId = contentPackageManifestDao().save(manifest);
	    
	    // Now create a representation of this content package in the database
	    ContentPackage cp = new ContentPackage(title, archiveId);
	    cp.setContext(context);
	    cp.setManifestId(manifestId);
	    cp.setReleaseOn(new Date());
	    cp.setCreatedBy(learnerId);
	    cp.setModifiedBy(learnerId);
	    cp.setCreatedOn(now);
	    cp.setModifiedOn(now);

	    contentPackageDao().save(cp);
	    
	}
	
	private ContentPackageManifest createManifest(Document document, IValidator validator) {
		ContentPackageManifest manifest = new ContentPackageManifest();
		
		// Grab the launch data
		manifest.setLaunchData(validator.getLaunchData(false, false));
		
		Node firstOrg = document.getElementsByTagName("organization").item(0);
		// Build a new seq activity tree
		ISeqActivityTree prototype = ADLSeqUtilities.buildActivityTree(firstOrg,
				DOMTreeUtility.getNode(document, "sequencingCollection"));
		
		manifest.setActTreePrototype(prototype);
		
		return manifest;
	}
	
	
	private File createFile(InputStream inputStream) {
		File tempFile = null;
		try {
			tempFile = File.createTempFile("scorm", ".zip");
			
			FileOutputStream fileOut = new FileOutputStream(tempFile);
			
			int len = 0;
			byte[] buf = new byte[1024];
			while ((len = inputStream.read(buf)) > 0) {
				fileOut.write(buf,0,len);
			}
			
			fileOut.close();
			inputStream.close();
		} catch (IOException ioe) {
			log.error("Caught an io exception trying to write byte array into temp file");
		}
		
		return tempFile;
	}
	
	/*private File createFile(byte[] bytes) {
		File tempFile = null;
		try {
			tempFile = File.createTempFile("scorm", ".zip");
			
			FileOutputStream fileOut = new FileOutputStream(tempFile);
			
			fileOut.write(bytes);
			
			fileOut.close();
		} catch (IOException ioe) {
			log.error("Caught an io exception trying to write byte array into temp file");
		}
		
		return tempFile;
	}
	
	private byte[] getFileAsBytes(File file) {
		int len = 0;
		byte[] buf = new byte[1024];
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		
		try {
			FileInputStream fileIn = new FileInputStream(file);
			while ((len = fileIn.read(buf)) > 0) {
				byteOut.write(buf,0,len);
			}
			
			fileIn.close();
		} catch (IOException ioe) {
			log.error("Caught an io exception trying to write file into byte array!", ioe);
		}
		
		return byteOut.toByteArray();
	}*/
	

}
