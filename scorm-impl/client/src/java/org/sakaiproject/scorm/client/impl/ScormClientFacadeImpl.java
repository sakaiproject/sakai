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
package org.sakaiproject.scorm.client.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.adl.api.ecmascript.APIErrorManager;
import org.adl.api.ecmascript.IErrorManager;
import org.adl.parsers.dom.DOMTreeUtility;
import org.adl.sequencer.ADLSeqUtilities;
import org.adl.sequencer.ADLSequencer;
import org.adl.sequencer.ADLValidRequests;
import org.adl.sequencer.ISeqActivityTree;
import org.adl.sequencer.ISequencer;
import org.adl.sequencer.SeqActivityTree;
import org.adl.validator.IValidatorOutcome;
import org.adl.validator.contentpackage.CPValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingHandlerResolver;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.MultiFileUploadPipe;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.content.api.ResourceTypeRegistry;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.scorm.model.ContentPackageManifestImpl;
import org.sakaiproject.scorm.model.api.ContentPackageManifest;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.UserDirectoryService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class ScormClientFacadeImpl implements ScormClientFacade {
	private static Log log = LogFactory.getLog(ScormClientFacadeImpl.class);
	
	// Dependency injected lookup methods
	protected ContentHostingService contentService() { return null; }
	protected EntityManager entityManager() { return null; }
	protected SessionManager sessionManager() { return null; }
	protected UserDirectoryService userDirectoryService() { return null; }
	protected ToolManager toolManager() { return null; }
	protected ServerConfigurationService serverConfigurationService() { return null; }
	
	// Dependency injected properties
	protected ResourceTypeRegistry resourceTypeRegistry;
	
	
	public void init() {
		getResourceTypeRegistry().register(new ScormContentType(this));
		entityManager().registerEntityProducer(this, REFERENCE_ROOT);
	}
	
	public List getContentPackages() {		
		List<ContentResource> allResources = contentService().findResources(null, null, null);
		List<ContentResource> contentPackages = new LinkedList<ContentResource>();
		
		for (ContentResource resource : allResources) {
			if (resource.getResourceType().equals(ScormContentType.SCORM_CONTENT_TYPE_ID)) 
				contentPackages.add(resource);			
		}
		
		return contentPackages;
	}
	
	public String addContentPackage(File contentPackage, CPValidator validator, IValidatorOutcome outcome) {
		// Grab the pipe
		ToolSession toolSession = sessionManager().getCurrentToolSession();	
		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		
		InputStream stream = null;
		//try {
			//stream = new FileInputStream(contentPackage);
			//pipe.setRevisedContentStream(stream);
			byte[] content = getFileAsBytes(contentPackage);
			
            pipe.setRevisedContent(content);
			pipe.setRevisedMimeType("application/zip");
            pipe.setFileName(contentPackage.getName());
            
            ContentPackageManifest manifest = createManifest(outcome.getDocument(), validator);
            ContentResource manifestResource = addManifest(manifest, pipe.getContentEntity().getId());
            String manifestResourceId = manifestResource.getId();
            
            pipe.setRevisedResourceProperty(ContentHostingHandlerResolver.CHH_BEAN_NAME, "org.sakaiproject.scorm.client.api.ContentHostingHandler");
            pipe.setRevisedResourceProperty("MANIFEST_RESOURCE_ID", manifestResourceId);
            
            pipe.setActionCanceled(false);
            pipe.setErrorEncountered(false);
            pipe.setActionCompleted(true); 
           
            toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);
		/*} catch (IOException ioe) {
			if (null != pipe) {
				pipe.setActionCanceled(true);
				pipe.setErrorEncountered(true);
				pipe.setActionCompleted(false);
			}
			log.error("Caught an io exception trying to upload file!", ioe);
		} finally {
			if (null != stream)
				try { 
					stream.close();
				} catch (IOException nioe) {
					log.info("Caught an io exception trying to close stream!", nioe);
				}
		}*/
		
		/*Object done = toolSession.getAttribute(ResourceToolAction.DONE);
		if (done != null)
		{
			toolSession.removeAttribute(ResourceToolAction.STARTED);
			Tool tool = toolManager().getCurrentTool();
		
			String url = (String) sessionManager().getCurrentToolSession().getAttribute(tool.getId() + Tool.HELPER_DONE_URL);
		
			sessionManager().getCurrentToolSession().removeAttribute(tool.getId() + Tool.HELPER_DONE_URL);
		
			return url;
		}*/
		
		return pipe.getContentEntity().getId();
	}
	
	public IValidatorOutcome validateContentPackage(File contentPackage, boolean doValidateSchema) {
		String directoryPath = contentPackage.getParent();
		CPValidator validator = new CPValidator(directoryPath);
		validator.setSchemaLocation(directoryPath);
		validator.setPerformValidationToSchema(doValidateSchema);
		boolean isValid = validator.validate(contentPackage.getPath(), "pif", "contentaggregation", false);

		IValidatorOutcome outcome = validator.getADLValidatorOutcome();
		
		if (isValid) {
			//ContentPackageManifest manifest = createManifest(outcome.getDocument(), validator);
			
			addContentPackage(contentPackage, validator, outcome);
			
			//addManifest(manifest, id);
		}
		
		return outcome;
	}
	

	

	
	public ContentResource addManifest(ContentPackageManifest manifest, String id) {
		
		ContentResource resource = null;
		
		String name = "manifest.obj"; // + manifest.getTitle();
		String site = getContext();
		String tool = "scorm";
		String type = "application/octet-stream";
		
		//XStream xstream = new XStream();

		/*try {
			FileOutputStream outFile = new FileOutputStream("/home/jrenfro/manifest.obj");
	        ObjectOutputStream s = new ObjectOutputStream(outFile);
	        s.writeObject(manifest);
	        s.flush();
	        s.close();
	        outFile.close();
		} catch (IOException ioe) {
			log.error("Caught an io exception trying to write manifest object to file!", ioe);
		}*/
		
		
		//String xml = xstream.toXML(manifest);
		/*ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		try {
			ObjectOutputStream out = new ObjectOutputStream(byteOut);
			out.writeObject(manifest);
			out.close();
		} catch (IOException ioe) {
			log.error("Caught an exception serializing manifest! ", ioe);
		}*/ 
		
		try {
			//FileInputStream fileIn = new FileInputStream("/home/jrenfro/file.txt");
			//name = "file.txt";
			//type = "text/plain";
			
			/*int len = 0;
			byte[] buf = new byte[1024];
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			
			while ((len = fileIn.read(buf)) > 0) {
				byteOut.write(buf,0,len);
			}
			
			fileIn.close();*/
			
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(byteOut);
			out.writeObject(manifest);
			out.close();
			
			
			resource = contentService().addAttachmentResource(name, site, tool, type, byteOut.toByteArray(), null);
		} catch (Exception soe) {
			log.error("Caught an exception adding an attachment resource!", soe);
		} finally {
			//if (null != byteOut)
			//	try { byteOut.close(); } catch (IOException nioe) { log.warn("Caught io exception closing!"); }
			
		}
		
		
		return resource;
	}
	
	
	public byte[] getFileAsBytes(File file) {
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
	        
	        manifest.setUrl(manifestResource.getUrl());
	        
	        
		} catch (Exception ioe) {
			log.error("Caught io exception reading manifest from file!", ioe);
		}
		
		return manifest;
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
		List titleNodes = DOMTreeUtility.getNodes(defaultNode, "title");
		
		String title = null;
		if (!titleNodes.isEmpty())
			title = DOMTreeUtility.getNodeValue((Node) titleNodes.get(0));
		
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
		
		
		return manifest;
	}
	
	
	
	public String getContext() {
		return toolManager().getCurrentPlacement().getContext();
	}
	
	/*public void jumpToTool() {
		String skin = ServerConfigurationService.getString("skin.default");

		// find the tool registered for this
		ActiveTool tool = ActiveToolManager.getActiveTool("sakai.help");
		if (tool == null)
		{
			portal.doError(req, res, session, Portal.ERROR_WORKSITE);
			return;
		}

		// form a placement based on ... help TODO: is this enough?
		// Note: the placement is transient, but will always have the same id
		// and (null) context
		org.sakaiproject.util.Placement placement = new org.sakaiproject.util.Placement(
				"help", tool.getId(), tool, null, null, null);

		portal
				.forwardTool(tool, req, res, placement, skin, toolContextPath,
						toolPathInfo);
		
	}*/
	
	public String getUserName() {
		return userDirectoryService().getCurrentUser().getDisplayName();
	}

	public String getPlacementId() {
		//ToolSession toolSession = sessionManager().getCurrentToolSession();
		//return toolSession.getPlacementId();
		return toolManager().getCurrentPlacement().getId();
	}
	
	public boolean isHelper() {
		Tool tool = toolManager().getCurrentTool();
		
		return SCORM_HELPER_ID.equals(tool.getId());
	}
	
	public ResourceToolActionPipe getResourceToolActionPipe() {
		ToolSession toolSession = sessionManager().getCurrentToolSession();	
		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		
		return pipe;
	}
	
	public MultiFileUploadPipe getMultiFileUploadPipe() {
		ToolSession toolSession = sessionManager().getCurrentToolSession();
		
		Enumeration enumeration = toolSession.getAttributeNames();
		
		while (enumeration.hasMoreElements()) {
			String name = (String)enumeration.nextElement();
			
			System.out.println("NAME: " + name);
		}
		
		MultiFileUploadPipe mfp = (MultiFileUploadPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);		
		
		return mfp;	
	}
	
	
	public void closePipe(ResourceToolActionPipe pipe) {
		pipe.setActionCanceled(false);
		pipe.setErrorEncountered(false);
		pipe.setActionCompleted(true);
		
		ToolSession toolSession = sessionManager().getCurrentToolSession();
		toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);
	}
	
	public String getCompletionURL() {
		ToolSession toolSession = sessionManager().getCurrentToolSession();
		
		Object done = toolSession.getAttribute(ResourceToolAction.DONE);
		
		if (done != null)
		{
			toolSession.removeAttribute(ResourceToolAction.STARTED);
			Tool tool = toolManager().getCurrentTool();
		
			String url = (String) sessionManager().getCurrentToolSession().getAttribute(tool.getId() + Tool.HELPER_DONE_URL);
		
			sessionManager().getCurrentToolSession().removeAttribute(tool.getId() + Tool.HELPER_DONE_URL);
		
			return url;
		}
		
		return null;
	}
	
	
		
	
	public void grantAlternativeRef(String resourceId) {
		try {
			ContentResourceEdit resource = contentService().editResource(resourceId);
	        ResourcePropertiesEdit resourceProperties = resource.getPropertiesEdit();
	        /*resourceProperties.addProperty (ResourceProperties.PROP_DISPLAY_NAME, object.getDisplayName());
	        resourceProperties.addProperty (ResourceProperties.PROP_DESCRIPTION, object.getDisplayName());
	        resourceProperties.addProperty(ResourceProperties.PROP_CONTENT_ENCODING, "UTF-8");
	        resourceProperties.addProperty(ResourceProperties.PROP_STRUCTOBJ_TYPE, getTypeId());
	        resourceProperties.addProperty(ResourceProperties.PROP_IS_COLLECTION, Boolean.FALSE.toString());*/
	        resourceProperties.addProperty(ContentHostingService.PROP_ALTERNATE_REFERENCE, REFERENCE_ROOT);
	
	        //resource.setContent(getInfoBytes(artifact));
	        contentService().commitResource(resource);
		} catch (Exception e) {
			log.error("Unable to grant an alternate reference root to this resource", e);
		}
	}
	
	public ISequencer getSequencer() {
		SeqActivityTree seqActivityTree = new SeqActivityTree();

        String mTreePath = "/home/jrenfro/serialize.obj";

        try {
	        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(mTreePath));
	        seqActivityTree = (SeqActivityTree)objectInputStream.readObject();
	        objectInputStream.close();
        } catch (Exception ioe) {
        	log.error("Unable to read serialize.obj from file", ioe);
        }
        
        // Create the sequencer and set the tree		
        ISequencer sequencer = new ADLSequencer();
        sequencer.setActivityTree(seqActivityTree);
        
        return sequencer;
	}
	
	public ISequencer getSequencer(ContentPackageManifest manifest) {
		// Create the sequencer and set the tree		
        ISequencer sequencer = new ADLSequencer();
        sequencer.setActivityTree(manifest.getActTreePrototype());
        
        return sequencer;
	}
	
	
	public IErrorManager getErrorManager() {
		return new APIErrorManager(IErrorManager.SCORM_2004_API);
	}
	
	public List getTableOfContents() {
		
		SeqActivityTree seqActivityTree = new SeqActivityTree();

        String mTreePath = "/home/jrenfro/serialize.obj";

        try {
	        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(mTreePath));
	        seqActivityTree = (SeqActivityTree)objectInputStream.readObject();
	        objectInputStream.close();
        } catch (Exception ioe) {
        	log.error("Unable to read serialize.obj from file", ioe);
        }
        
        // Create the sequencer and set the tree
        ISequencer sequencer = new ADLSequencer();
        sequencer.setActivityTree(seqActivityTree);
		
        ADLValidRequests validRequests = new ADLValidRequests();
        sequencer.getValidRequests(validRequests);
        
        return validRequests.mTOC;
	}
	
	
	
	public String getConfigurationString(String key, String defaultValue) {
		return serverConfigurationService().getString(key, defaultValue);
	}
	
	
	/**
	 * Dependency: inject the ResourceTypeRegistry
	 * @param registry
	 */
	public void setResourceTypeRegistry(ResourceTypeRegistry registry)
	{
		resourceTypeRegistry = registry;
	}
	
	/**
	 * @return the ResourceTypeRegistry
	 */
	public ResourceTypeRegistry getResourceTypeRegistry()
	{
		return resourceTypeRegistry;
	}
	
	
	/*
	 * Implementation of EntityProducer methods
	 */
	
	public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments) {
		return contentService().archive(siteId, doc, stack, archivePath, attachments);
	}
	public Entity getEntity(Reference ref) {
		return contentService().getEntity(ref);
	}
	public Collection getEntityAuthzGroups(Reference ref, String userId) {
		return contentService().getEntityAuthzGroups(ref, userId);
	}
	public String getEntityDescription(Reference ref) {
		return contentService().getEntityDescription(ref);
	}
	public ResourceProperties getEntityResourceProperties(Reference ref) {
		return contentService().getEntityResourceProperties(ref);
	}
	public String getEntityUrl(Reference ref) {
		return contentService().getEntityUrl(ref);
	}
	public HttpAccess getHttpAccess() {
		return new ScormClientHttpAccess();
	}
	public String getLabel() {
		return "scorm";
	}
	public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans, Set userListAllowImport) {
		return null;
	}
	public boolean parseEntityReference(String reference, Reference ref) {
		String id = null;
		String context = "";

		// for content hosting resources and collections
		if (reference.startsWith(REFERENCE_ROOT))
		{
			// parse out the local resource id
			id = reference.substring(REFERENCE_ROOT.length(), reference.length());
		}
		// not mine
		else
		{
			return false;
		}
		
		// Pass the remainder of the reference on to the content hosting service
		contentService().parseEntityReference(id, ref);
		
		return true;
	}
	public boolean willArchiveMerge() {
		return false;
	}
	
	
}
