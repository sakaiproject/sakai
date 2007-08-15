package org.sakaiproject.scorm.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.adl.parsers.dom.DOMTreeUtility;
import org.adl.sequencer.ADLSeqUtilities;
import org.adl.sequencer.ISeqActivityTree;
import org.adl.validator.IValidatorOutcome;
import org.adl.validator.contentpackage.CPValidator;
import org.adl.validator.contentpackage.ManifestHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingHandlerResolver;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdLengthException;
import org.sakaiproject.exception.IdUniquenessException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.scorm.content.api.Addable;
import org.sakaiproject.scorm.content.impl.ScormCollectionType;
import org.sakaiproject.scorm.content.impl.ZipCHH;
import org.sakaiproject.scorm.content.impl.ZipCollectionType;
import org.sakaiproject.scorm.model.ContentPackageManifestImpl;
import org.sakaiproject.scorm.model.api.ContentPackageManifest;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class ScormContentServiceImpl implements ScormContentService {
	private static Log log = LogFactory.getLog(ScormContentServiceImpl.class);
	
	// Dependency injected lookup methods
	protected ContentHostingService contentService() { return null; }
	protected SessionManager sessionManager() { return null; }
	protected ToolManager toolManager() { return null; }
	
	private ZipCHH zipContentHostingHandler;
	
	protected Addable contentHostingHandler() { return null; }
	
	public ContentResource addManifest(ContentPackageManifest manifest, String id) {
		ContentResource resource = null;
		
		String name = "manifest.obj"; // + manifest.getTitle();
		String site = getContext();
		String tool = "scorm";
		String type = "application/octet-stream";
				
		try {	
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(byteOut);
			out.writeObject(manifest);
			out.close();
			
			
			resource = contentService().addAttachmentResource(name, site, tool, type, byteOut.toByteArray(), null);
		} catch (Exception soe) {
			log.error("Caught an exception adding an attachment resource!", soe);
		} 
		
		return resource;
	}

	public List<ContentResource> getContentPackages() {
		List<ContentResource> allResources = contentService().findResources(null, null, null);
		List<ContentResource> contentPackages = new LinkedList<ContentResource>();
		
		for (ContentResource resource : allResources) {
			String resourceType = resource.getResourceType();
			if (resourceType != null && resourceType.equals(ScormCollectionType.SCORM_CONTENT_TYPE_ID)) 
				contentPackages.add(resource);			
		}
		
		return contentPackages;
	}
	
	public InputStream getManifestAsStream(String contentPackageId) {
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
	}
	
	public ContentPackageManifest getManifest(String contentPackageId) {
		ContentPackageManifest manifest = null;
		
		try {
			ContentResource contentPackageResource = contentService().getResource(contentPackageId);
			String manifestResourceId = (String)contentPackageResource.getProperties().get("MANIFEST_RESOURCE_ID");
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
	}

	public IValidatorOutcome validateContentPackage(File contentPackage, boolean onlyValidateManifest) {
		String directoryPath = contentPackage.getParent();
		CPValidator validator = new CPValidator(directoryPath);
		validator.setSchemaLocation(directoryPath);
		validator.setPerformValidationToSchema(!onlyValidateManifest);
		boolean isValid = validator.validate(contentPackage.getPath(), "pif", "contentaggregation", onlyValidateManifest);

		IValidatorOutcome outcome = validator.getADLValidatorOutcome();

		
		/*if ( (onlyValidateManifest && outcome.getDoesIMSManifestExist()  &&
	               outcome.getIsWellformed() && outcome.getIsValidRoot()) || 
	         (!onlyValidateManifest && 
	               (outcome.getDoesIMSManifestExist() && 
	                outcome.getIsValidRoot() && 
	                outcome.getIsWellformed() && 
	                outcome.getIsValidToSchema() &&
	                outcome.getIsValidToApplicationProfile() && 
	                outcome.getDoRequiredCPFilesExist())) )
	         {
	            outcome.rollupSubManifests( false );
	            Document mDocument = outcome.getDocument();

	            Vector mLaunchDataList = validator.getLaunchData(mDocument, false, false);

	            Node mManifest = mDocument.getDocumentElement();

	            // get information from manifest and update database
	            Vector mOrganizationList = ManifestHandler.getOrganizationNodes(mManifest, false);
	            
	            // JLR: We don't need this because we're not using the db to store the ItemInfo, etc.
	            boolean mDBUpdateResult = true; //updateDB();
	            
	            // get ssp addition
	            boolean sspDBUpdateResult = true;
	            Vector resources = ManifestHandler.getSSPResourceList( mManifest );
	            
	            // Only update SSP Database if intial database insert was successful
	            if ( mDBUpdateResult )
	            {
	               // FIXME: JLR - Do we need to worry about SSP?
	               //sspDBUpdateResult = updateSSPDB( resources );
	            }
	                     
	         }*/
		
		if (isValid) {
			addContentPackage(contentPackage, validator, outcome);
		}
		
		return outcome;
	}
	
	public void uploadZipArchive(File zipArchive) throws IdUnusedException, IdUniquenessException, IdLengthException, IdInvalidException, OverQuotaException, ServerOverloadException, PermissionException  {
		addVirtualCollection(zipArchive, this.zipContentHostingHandler, ZipCollectionType.ZIP_COLLECTION_TYPE_ID);
	}
	
	public void addVirtualCollection(File file, ZipCHH chh, String resourceType) throws IdUnusedException, IdUniquenessException, IdLengthException, IdInvalidException, OverQuotaException, ServerOverloadException, PermissionException  {
		// Grab the pipe
		ToolSession toolSession = sessionManager().getCurrentToolSession();	
		/*ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);

		ContentEntity collection = pipe.getContentEntity();		
		String filename = file.getName();
		int extensionIndex = filename.lastIndexOf('.');
		
		String basename = filename;
		String extension = "";
		
		if (extensionIndex != -1 && extensionIndex + 1 < filename.length()) {
			basename = filename.substring(0, extensionIndex);
			extension = filename.substring(extensionIndex + 1);
		}
		
		ContentResourceEdit cr = contentService().addResource(collection.getId(), Validator.escapeResourceName(basename), Validator.escapeResourceName(extension), 100);
		
		// TODO: Can we switch this to only use stream instead of bytes?
		byte[] content = getFileAsBytes(file);
		cr.setContent(content);
		// TODO: Can we handle other content types by extension here?
		cr.setContentType("application/zip");
		cr.setResourceType(resourceType);
		//cr.setContentHandler(chh);
		
		ResourcePropertiesEdit properties = cr.getPropertiesEdit();
		properties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, filename);
		properties.addProperty(ContentHostingHandlerResolver.CHH_BEAN_NAME, chh.getContentHostingHandlerName());
		
		//ContentEntity vce = chh.getVirtualContentEntity(cr, cr.getId() + "/");
		//cr.setVirtualContentEntity(vce);
		
		contentService().commitResource(cr);
		
		pipe.setActionCanceled(false);
        pipe.setErrorEncountered(false);
        pipe.setActionCompleted(true);*/
		
		
		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		
		byte[] content = getFileAsBytes(file);
        pipe.setRevisedContent(content);
        pipe.setRevisedMimeType("application/zip");
        pipe.setFileName(file.getName());
        
        pipe.setRevisedResourceProperty(ContentHostingHandlerResolver.CHH_BEAN_NAME, "org.sakaiproject.scorm.content.api.ZipCHH");
            
        pipe.setActionCanceled(false);
        pipe.setErrorEncountered(false);
        pipe.setActionCompleted(true);
        
        /*ContentEntity ce = pipe.getContentEntity();
        //ce.setContentHandler(zipContentHostingHandler);
        
        ContentEntity vce = zipContentHostingHandler.getVirtualContentEntity(ce, ce.getId() + "/");
        ce.setVirtualContentEntity(vce);
        vce.setContentHandler(zipContentHostingHandler);
        */
		
		//pipe.setActionCanceled(true);
        //pipe.setErrorEncountered(false);
        //pipe.setActionCompleted(true);
		
        toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);
	}
	
	public String identifyZipArchive() {
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
	}
	
	
	private String addContentPackage(File contentPackage, CPValidator validator, IValidatorOutcome outcome) {
		// Grab the pipe
		ToolSession toolSession = sessionManager().getCurrentToolSession();	
		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
				
		byte[] content = getFileAsBytes(contentPackage);
			
        pipe.setRevisedContent(content);
        pipe.setRevisedMimeType("application/zip");
        pipe.setFileName(contentPackage.getName());
        
        ContentPackageManifest manifest = createManifest(outcome.getDocument(), validator);
        ContentResource manifestResource = addManifest(manifest, pipe.getContentEntity().getId());
        String manifestResourceId = manifestResource.getId();
        
        pipe.setRevisedResourceProperty(ContentHostingHandlerResolver.CHH_BEAN_NAME, "org.sakaiproject.scorm.content.api.ScormCHH");
        pipe.setRevisedResourceProperty("MANIFEST_RESOURCE_ID", manifestResourceId);
            
        pipe.setActionCanceled(false);
        pipe.setErrorEncountered(false);
        pipe.setActionCompleted(true); 
           
        toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);

		return pipe.getContentEntity().getId();
	}
	
	private ContentPackageManifest createManifest(Document document, CPValidator validator) {
		ContentPackageManifest manifest = new ContentPackageManifestImpl();
		
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
	
	private String getContext() {
		return toolManager().getCurrentPlacement().getContext();
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
	
	public ZipCHH getZipContentHostingHandler() {
		return this.zipContentHostingHandler;
	}
	
	
	public void setZipContentHostingHandler(ZipCHH zipContentHostingHandler) {
		this.zipContentHostingHandler = zipContentHostingHandler;
	}
}
