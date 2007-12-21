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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.scorm.api.ScormConstants;
import org.sakaiproject.scorm.content.impl.ScormCHH;
import org.sakaiproject.scorm.dao.LearnerDao;
import org.sakaiproject.scorm.dao.api.ContentPackageDao;
import org.sakaiproject.scorm.dao.api.DataManagerDao;
import org.sakaiproject.scorm.dao.api.SeqActivityTreeDao;
import org.sakaiproject.scorm.exceptions.ValidationException;
import org.sakaiproject.scorm.model.ContentPackageManifestImpl;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.ContentPackageManifest;
import org.sakaiproject.scorm.model.api.UnvalidatedResource;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormPermissionService;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class ScormContentServiceImpl implements ScormContentService, ScormConstants {
	private static Log log = LogFactory.getLog(ScormContentServiceImpl.class);
		
	// Dependency injected lookup methods
	protected abstract ScormPermissionService permissionService();
	protected abstract ScormResourceService resourceService();
	
	protected abstract LearningManagementSystem lms();
	
	// Data access objects (also dependency injected by lookup method)
	protected abstract ContentPackageDao contentPackageDao();
	protected abstract DataManagerDao dataManagerDao();
	protected abstract LearnerDao learnerDao();
	protected abstract SeqActivityTreeDao seqActivityTreeDao();
	
	
	public String addContentPackage(File contentPackage, IValidator validator, IValidatorOutcome outcome) throws Exception {
		FileInputStream inputStream = new FileInputStream(contentPackage);
		
		//String resourceId = storeFile(contentPackage, "application/zip");
		String resourceId = resourceService().putArchive(inputStream, contentPackage.getName(), "application/zip");
		convertToContentPackage(resourceId, validator, outcome);
	
		return resourceId;
	}
	
	public ContentPackage getContentPackage(long contentPackageId) {
		return contentPackageDao().load(contentPackageId);
	}
	
	public List<ContentPackage> getContentPackages() {
		String context = lms().currentContext();
		
		List<ContentPackage> allPackages = contentPackageDao().find(context);
		List<ContentPackage> releasedPackages = new LinkedList<ContentPackage>();
		
		if (permissionService().canModify())
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
		
		String title = null;
		if (!titleNodes.isEmpty())
			title = DOMTreeUtility.getNodeValue(titleNodes.get(0));
		
		if (null == title) 
			title = "Unknown";
		
		return title;
	}
	
	/*public List<ContentResource> getZipArchives() {
		String siteId = toolManager().getCurrentPlacement().getContext();
		String siteCollectionId = contentService().getSiteCollection(siteId);
		
		return findPotentialContentPackages(siteCollectionId);
	}*/
	
	public void removeContentPackage(long contentPackageId) {

		ContentPackage contentPackage = contentPackageDao().load(contentPackageId);
		
		String manifestResourceId = resourceService().removeArchive(contentPackage.getResourceId());
		if (manifestResourceId != null)
			resourceService().removeManifest(contentPackage.getResourceId(), manifestResourceId);
		
		contentPackageDao().remove(contentPackage);
	}
	
	public void updateContentPackage(ContentPackage contentPackage) {
		String learnerId = lms().currentLearnerId();
		
		if (learnerId == null)
			learnerId = "unknown";
			
		contentPackage.setModifiedBy(learnerId);
		contentPackage.setModifiedOn(new Date());
		
		contentPackageDao().save(contentPackage);
	}
	
	public void validate(String resourceId, boolean isManifestOnly, boolean isValidateToSchema) throws Exception {
		//ContentResource resource = contentService().getResource(resourceId);
		
		UnvalidatedResource resource = resourceService().getUnvalidatedResource(resourceId);
		
		File file = createFile(resource.getContentStream());
		
		if (!file.exists()) {
			throw new ValidationException("noFile");
		}
		
		IValidator validator = validate(file, isManifestOnly, isValidateToSchema);		
		IValidatorOutcome validatorOutcome = validator.getADLValidatorOutcome();

		if (!validatorOutcome.getDoesIMSManifestExist()) {
			throw new ValidationException("noManifest");
		}
		
		if (!validatorOutcome.getIsWellformed()) {
			throw new ValidationException("notWellFormed");
		}
		
		if (!validatorOutcome.getIsValidRoot()) {
			throw new ValidationException("notValidRoot");
		}
		
		if (isValidateToSchema) {
			if (!validatorOutcome.getIsValidToSchema()) {
				throw new ValidationException("notValidSchema");
			}
			
			if (!validatorOutcome.getIsValidToApplicationProfile()) {
				throw new ValidationException("notValidApplicationProfile");
			}
			
			if (!validatorOutcome.getDoRequiredCPFilesExist()) {
				throw new ValidationException("notExistingRequiredFiles");
			}
		}
		
		
		convertToContentPackage(resourceId, validator, validatorOutcome);
		
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
	    String manifestResourceId = resourceService().putManifest(resourceId, manifest);
		resourceService().convertArchive(resourceId, manifestResourceId);
				
	    // Grab some important info about the site and user
	    String context = lms().currentContext();
	    String learnerId = lms().currentLearnerId();
	    Date now = new Date();
	        
	    // Now create a representation of this content package in the database
	    ContentPackage cp = new ContentPackage(manifest.getTitle(), resourceId);
	    cp.setContext(context);
	    cp.setReleaseOn(new Date());
	    cp.setCreatedBy(learnerId);
	    cp.setModifiedBy(learnerId);
	    cp.setCreatedOn(now);
	    cp.setModifiedOn(now);

	    contentPackageDao().save(cp);
	}
	
	/**
	 * Recursive method that gathers together all the zip files in a collection 
	 * 
	 * @param collectionId
	 * @return List of ContentResource objects
	 */
	/*private List<ContentResource> findPotentialContentPackages(String collectionId) {
		List<ContentResource> zipFiles = new LinkedList<ContentResource>();
		try {
			ContentCollection collection = contentService().getCollection(collectionId);
			List<ContentEntity> members = collection.getMemberResources();
			
			for (ContentEntity member : members) {
				if (member.isResource()) {
					String mimeType = ((ContentResource)member).getContentType();
					if (mimeType != null && mimeType.equals("application/zip")) 
						zipFiles.add((ContentResource)member);	
				} else if (member.isCollection() && member.getVirtualContentEntity() == null &&
						member.getContentHandler() == null)
					zipFiles.addAll(findPotentialContentPackages(member.getId()));
			}
		
		} catch (Exception e) {
			log.error("Caught an exception looking for content packages", e);
		}
		
		return zipFiles;
	}*/
	
	
	
	/**
	 * Stores the passed file in the content repository
	 * 
	 * @param file
	 * @return identifier for th
	 * @throws Exception
	 */
	/*private String storeFile(File file, String mimeType) throws Exception {
		byte[] content = getFileAsBytes(file);
				
		String siteId = toolManager().getCurrentPlacement().getContext();
		String collectionId = contentService().getSiteCollection(siteId);
		
		String fileName = file.getName();
		int extIndex = fileName.lastIndexOf('.');
		String basename = fileName.substring(0, extIndex);
		String extension = fileName.substring(extIndex);

		ContentResourceEdit edit = null;
		try {
			edit = contentService().addResource(collectionId,Validator.escapeResourceName(basename),Validator.escapeResourceName(extension),MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);
				
			edit.setContent(content);
			edit.setContentLength(content.length);
			edit.setContentType(mimeType);
        
			ResourcePropertiesEdit props = edit.getPropertiesEdit();
			props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, file.getName());
	        		
	        contentService().commitResource(edit);			

	        return edit.getId();
		} catch (Exception e) {
			if (edit != null)
				contentService().cancelResource(edit);
			throw e;
		}
	}*/
	
	
	
	


	
	private ContentPackageManifest createManifest(Document document, IValidator validator) {
		ContentPackageManifest manifest = new ContentPackageManifestImpl();
		
		String title = getContentPackageTitle(document);
		manifest.setTitle(title);
		
		// Grab the launch data
		manifest.setLaunchData(validator.getLaunchData(false, false));
		
		
		Node firstOrg = document.getElementsByTagName("organization").item(0);
		// Build a new seq activity tree
		ISeqActivityTree prototype = ADLSeqUtilities.buildActivityTree(firstOrg,
				DOMTreeUtility.getNode(document, "sequencingCollection"));
		
		manifest.setActTreePrototype(prototype);
		manifest.setDocument(document);
		
		return manifest;
	}
	
	
	private File createFile(InputStream inputStream) {
		log.error("NOT IMPLEMENTED - createFile!!!");
		return null;
	}
	
	private File createFile(byte[] bytes) {
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
	}
	
	

	/*public ISeqActivityTree getActivityTree(String courseId, String userId, boolean isFresh) {
		// First, let's check to see if we've gone a saved one
		
		ISeqActivityTree tree = null;
		
		if (!isFresh)
			tree = seqActivityTreeDao.find(courseId, userId);
		
		if (null == tree) {
			ContentPackageManifest manifest = contentPackageManifestDao().find(courseId);
			
			tree = manifest.getActTreePrototype();

			tree.setCourseID(courseId);
			tree.setLearnerID(userId);
	        
	        //seqActivityTreeDao.save(tree);
		}
		
		return tree;
	}*/
	
	

	// Recursively search for content packages in the file system
	/*private List<ContentEntity> findContentPackages(String collectionId) {
		List<ContentEntity> packages = new LinkedList<ContentEntity>();
		try {
			ContentCollection collection = contentService().getCollection(collectionId);
			List<ContentEntity> members = collection.getMemberResources();
			
			for (ContentEntity member : members) {
				if (member.isCollection()) {
					
					// We don't want to be recursive on Content Packages themselves
					if (member.getResourceType().equals(ScormCollectionType.SCORM_CONTENT_TYPE_ID)) {
						ResourceProperties props = member.getProperties();
					
						// We only want to return the top-level virtual collection
						if (props.getProperty(IS_CONTENT_PACKAGE_PROPERTY) != null)
							packages.add(member);
					} else
						packages.addAll(findContentPackages(member.getId()));
				}
			}
		
		} catch (Exception e) {
			log.error("Caught an exception looking for content packages", e);
		}
		
		return packages;
	}*/
	

	
	/*public List<ContentEntity> getContentPackages() {
		String siteId = toolManager().getCurrentPlacement().getContext();
		String siteCollectionId = contentService().getSiteCollection(siteId);
		
		return findContentPackages(siteCollectionId);
	}*/
	
	
	
	/*public InputStream getManifestAsStream(String contentPackageId) {
		InputStream stream = null;
		
		String path = new StringBuffer().append(contentPackageId).append("/imsmanifest.xml").toString();
		try {
			ContentResource resource = contentService().getResource(path);
			
			if (resource != null) {
				byte[] content = resource.getContent();
				
				if (content != null) {
					stream = new ByteArrayInputStream(content);
				} else {
					stream = resource.streamContent();
				}
			}
		} catch (Exception iue) {
			log.error("Caught an exception grabbing the manifest file as a stream", iue);
		}
		
		return stream;
	}*/
	
	/*public ContentPackageManifest getManifest(String contentPackageId) {
		ContentPackageManifest manifest = null;
		
		try {
			ContentResource contentPackageResource = contentService().getResource(contentPackageId);
			String manifestResourceId = (String)contentPackageResource.getProperties().get(MANIFEST_RESOURCE_ID_PROPERTY);
			ContentResource manifestResource = contentService().getResource(manifestResourceId);
			
			byte[] bytes = manifestResource.getContent();
		
			ByteArrayInputStream in = new ByteArrayInputStream(bytes);
			
			//FileInputStream in = new FileInputStream("/home/jrenfro/manifest.obj");
	        ObjectInputStream ie = new ObjectInputStream(in);
	        manifest = (ContentPackageManifest)ie.readObject();
	        ie.close();
	        in.close();
	        
	        manifest.setResourceId(contentPackageResource.getUrl());
	        
	        
		} catch (Exception ioe) {
			log.error("Caught io exception reading manifest from file!", ioe);
		}
		
		return manifest;
	}*/


	
	
	/*public IValidatorOutcome validateContentPackage(File contentPackage, boolean onlyValidateManifest) {
		String directoryPath = contentPackage.getParent();
		CPValidator validator = new CPValidator(directoryPath);
		validator.setSchemaLocation(directoryPath);
		validator.setPerformValidationToSchema(!onlyValidateManifest);
		boolean isValid = validator.validate(contentPackage.getPath(), "pif", "contentaggregation", onlyValidateManifest);

		IValidatorOutcome outcome = validator.getADLValidatorOutcome();

		
	
		if (isValid) {
			saveContentPackage(contentPackage, validator, outcome);
		
			//createContentPackage(contentPackage, validator, outcome);
		}
		
		return outcome;
	}*/
	
	/*public void uploadZipArchive(File zipArchive) throws IdUnusedException, IdUniquenessException, IdLengthException, IdInvalidException, OverQuotaException, ServerOverloadException, PermissionException  {
		addVirtualCollection(zipArchive, this.zipContentHostingHandler, ZipCollectionType.ZIP_COLLECTION_TYPE_ID);
	}*/
	
	
	
	/*public void addVirtualCollection(File file, ZipCHH chh, String resourceType) throws IdUnusedException, IdUniquenessException, IdLengthException, IdInvalidException, OverQuotaException, ServerOverloadException, PermissionException  {
		// Grab the pipe
		ToolSession toolSession = sessionManager().getCurrentToolSession();		
		
		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		
		byte[] content = getFileAsBytes(file);
        pipe.setRevisedContent(content);
        pipe.setRevisedMimeType("application/zip");
        pipe.setFileName(file.getName());
        
        pipe.setRevisedResourceProperty(ContentHostingHandlerResolver.CHH_BEAN_NAME, "org.sakaiproject.scorm.content.api.ZipCHH");
            
        
        
        
        pipe.setActionCanceled(false);
        pipe.setErrorEncountered(false);
        pipe.setActionCompleted(true);
		
        toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);
	}*/
	
	/*public String identifyZipArchive() {
		// Grab the pipe
		ToolSession toolSession = sessionManager().getCurrentToolSession();	
		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);

		
		ContentEntity ce = pipe.getContentEntity();
		
		String id = ce.getId();

        pipe.setActionCanceled(true);
        pipe.setErrorEncountered(false);
        pipe.setActionCompleted(false); 
           
        toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);

        return id;
	}*/
	
	
	/*
	 * private String saveContentPackage(File contentPackage, IValidator validator, IValidatorOutcome outcome) {
		// Grab the pipe
		ToolSession toolSession = sessionManager().getCurrentToolSession();	
		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
				
		byte[] content = getFileAsBytes(contentPackage);
			
        pipe.setRevisedContent(content);
        pipe.setRevisedMimeType("application/zip");
        pipe.setFileName(contentPackage.getName());
        
        ContentPackageManifest manifest = createManifest(outcome.getDocument(), validator);
        ContentResource manifestResource = storeManifest(manifest, pipe.getContentEntity().getId());
        String manifestResourceId = manifestResource.getId();
        
        pipe.setRevisedResourceProperty(ContentHostingHandlerResolver.CHH_BEAN_NAME, "org.sakaiproject.scorm.content.api.ScormCHH");
        pipe.setRevisedResourceProperty("MANIFEST_RESOURCE_ID", manifestResourceId);
        
        if (manifest != null)
        	pipe.setRevisedResourceProperty("CONTENT_PACKAGE_TITLE", manifest.getTitle());
            
        pipe.setActionCanceled(false);
        pipe.setErrorEncountered(false);
        pipe.setActionCompleted(true); 
           
        toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);

		return pipe.getContentEntity().getId();
	}
	 */
}
