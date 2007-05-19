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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.adl.api.ecmascript.APIErrorManager;
import org.adl.api.ecmascript.IErrorManager;
import org.adl.sequencer.ADLSequencer;
import org.adl.sequencer.ADLValidRequests;
import org.adl.sequencer.ISequencer;
import org.adl.sequencer.SeqActivityTree;
import org.adl.validator.IValidatorOutcome;
import org.adl.validator.contentpackage.CPValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
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
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.UserDirectoryService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
		/*List<ContentCollection> collections = new LinkedList<ContentCollection>();
		Map<String, String> collectionMap = contentService().getCollectionMap();
		
		for (String collectionId : collectionMap.keySet()) {
			try {
				ContentCollection collection = contentService().getCollection(collectionId);
		
				collections.add(collection);
			} catch (Exception e) {
				log.error("Unable to retrieve the content collection for " + collectionId, e);
			}
		}*/
		
		List<ContentResource> allResources = contentService().findResources(null, null, null);
		List<ContentResource> contentPackages = new LinkedList<ContentResource>();
		
		for (ContentResource resource : allResources) {
			if (resource.getResourceType().equals(ScormContentType.SCORM_CONTENT_TYPE_ID)) 
				contentPackages.add(resource);			
		}
		
		return contentPackages;
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
		
		Enumeration enumeration = toolSession.getAttributeNames();
		
		while (enumeration.hasMoreElements()) {
			String name = (String)enumeration.nextElement();
			
			System.out.println("NAME: " + name);
		}
		
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
	
	public IValidatorOutcome validateContentPackage(File contentPackage) {
		String directoryPath = contentPackage.getParent();
		CPValidator validator = new CPValidator(directoryPath);
		validator.setSchemaLocation(directoryPath);
		validator.setPerformValidationToSchema(true);
		boolean isValid = validator.validate(contentPackage.getPath(), "pif", "contentaggregation", false);

		
		return validator.getADLValidatorOutcome();
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
