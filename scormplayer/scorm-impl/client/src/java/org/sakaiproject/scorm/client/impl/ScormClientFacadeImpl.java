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
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.adl.api.ecmascript.APIErrorManager;
import org.adl.api.ecmascript.IErrorManager;
import org.adl.datamodels.DMErrorCodes;
import org.adl.datamodels.DMFactory;
import org.adl.datamodels.DMInterface;
import org.adl.datamodels.DMProcessingInfo;
import org.adl.datamodels.IDataManager;
import org.adl.datamodels.SCODataManager;
import org.adl.datamodels.ieee.IValidatorFactory;
import org.adl.datamodels.ieee.SCORM_2004_DM;
import org.adl.datamodels.ieee.ValidatorFactory;
import org.adl.datamodels.nav.SCORM_2004_NAV_DM;
import org.adl.sequencer.ADLObjStatus;
import org.adl.sequencer.ADLValidRequests;
import org.adl.sequencer.ISeqActivity;
import org.adl.sequencer.ISeqActivityTree;
import org.adl.sequencer.ISequencer;
import org.adl.sequencer.IValidRequests;
import org.adl.sequencer.SeqObjective;
import org.adl.sequencer.impl.ADLSequencer;
import org.adl.validator.IValidatorOutcome;
import org.adl.validator.contentpackage.ILaunchData;
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
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdLengthException;
import org.sakaiproject.exception.IdUniquenessException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.scorm.client.api.IRunState;
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.scorm.dao.api.DataManagerDao;
import org.sakaiproject.scorm.dao.api.SeqActivityTreeDao;
import org.sakaiproject.scorm.model.api.ContentPackageManifest;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ScormClientFacadeImpl implements ScormClientFacade {
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(ScormClientFacadeImpl.class);
	
	// Dependency injected lookup methods
	protected ContentHostingService contentService() { return null; }
	protected EntityManager entityManager() { return null; }
	protected SessionManager sessionManager() { return null; }
	protected UserDirectoryService userDirectoryService() { return null; }
	protected ToolManager toolManager() { return null; }
	protected ServerConfigurationService serverConfigurationService() { return null; }
	protected ScormContentService scormContentService() { return null; }
	
	// Dependency injected properties
	protected ResourceTypeRegistry resourceTypeRegistry;
	protected SeqActivityTreeDao seqActivityTreeDao;
	protected DataManagerDao dataManagerDao;
	
	
	public void init() {
		entityManager().registerEntityProducer(this, REFERENCE_ROOT);
	}
	
	public List getContentPackages() {		
		return scormContentService().getContentPackages();
	}
	
	public IValidatorOutcome validateContentPackage(File contentPackage, boolean onlyValidateManifest) {
		return scormContentService().validateContentPackage(contentPackage, onlyValidateManifest);
	}
	
	public ContentResource addManifest(ContentPackageManifest manifest, String id) {
		return scormContentService().addManifest(manifest, id);
	}

	public ContentPackageManifest getManifest(String contentPackageId) {
		return scormContentService().getManifest(contentPackageId);
	}
	
	public String getContext() {
		return toolManager().getCurrentPlacement().getContext();
	}
	
	public void uploadZipArchive(File zipArchive) throws IdUnusedException, IdUniquenessException, IdLengthException, IdInvalidException, OverQuotaException, ServerOverloadException, PermissionException{
		scormContentService().uploadZipArchive(zipArchive);
	}
	
	public String identifyZipArchive() {
		return scormContentService().identifyZipArchive();
	}
	
	/*public void uploadZipEntry(File zipEntry, String path) {
		scormContentService().uploadZipEntry(zipEntry,path);
	}*/
	
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
		
		toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);
		toolSession.removeAttribute(ResourceToolAction.STARTED);
		Tool tool = toolManager().getCurrentTool();
		
		String url = (String) sessionManager().getCurrentToolSession().getAttribute(tool.getId() + Tool.HELPER_DONE_URL);
	
		sessionManager().getCurrentToolSession().removeAttribute(tool.getId() + Tool.HELPER_DONE_URL);
			
		return url;
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
	
	public ISequencer getSequencer(ISeqActivityTree tree) {
        // Create the sequencer and set the tree		
        ISequencer sequencer = new ADLSequencer();
        sequencer.setActivityTree(tree);
        
        return sequencer;
	}
	
	/*public ISequencer getSequencer(ContentPackageManifest manifest, String userId, String courseId) {
		// Create the sequencer and set the tree		
        ISequencer sequencer = new ADLSequencer();
        
        ISeqActivityTree mSeqActivityTree = manifest.getActTreePrototype();

        mSeqActivityTree.setCourseID(courseId);
        mSeqActivityTree.setLearnerID(userId);
        
        seqActivityTreeDao.save(mSeqActivityTree);        
        sequencer.setActivityTree(mSeqActivityTree);
        
        return sequencer;
	}*/
	
	public ISeqActivityTree getActivityTree(String contentPackageId, String courseId, String userId, boolean isFresh) {
		// First, let's check to see if we've gone a saved one
		
		ISeqActivityTree tree = null;
		
		if (!isFresh)
			tree = seqActivityTreeDao.find(courseId, userId);
		
		if (null == tree) {
			ContentPackageManifest manifest = getManifest(contentPackageId);
			
			tree = manifest.getActTreePrototype();

			tree.setCourseID(courseId);
			tree.setLearnerID(userId);
	        
	        //seqActivityTreeDao.save(tree);
		}
		
		return tree;
	}
	
	
	public IErrorManager getErrorManager() {
		return new APIErrorManager(IErrorManager.SCORM_2004_API);
	}
	
	/*public List getTableOfContents() {
		
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
	}*/
	
	
	
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
	
	
	
	/**
	 * API IMPLEMENTATION
	 */
	public IDataManager initialize(IRunState runState, String numberOfAttempts) {
		log.debug("Service - Initialize");
   
		if (null == runState)
			return null;
		
		String courseId = runState.getCurrentCourseId();
		String scoId = runState.getCurrentSco();
		String userId = runState.getCurrentUserId();
		
        boolean isNewDataManager = false;
        IDataManager scoDataManager = null;
        
// FIXME : We need to look in the db first -- but currently it doesn't seem that data is getting saved correctly
        // First, check to see if we have a ScoDataManager persisted
        scoDataManager = dataManagerDao.find(courseId, userId);

        if (scoDataManager == null) {
	        // If not, create one
	        scoDataManager = new SCODataManager(courseId, userId);
	        scoDataManager.setValidatorFactory(new ValidatorFactory());
	        
	        
	        //  Add a SCORM 2004 Data Model
	        scoDataManager.addDM(DMFactory.DM_SCORM_2004);
	
	        //  Add a SCORM 2004 Nav Data Model
	        scoDataManager.addDM(DMFactory.DM_SCORM_NAV);
	
	        //  Add a SSP Datamodel
	        scoDataManager.addDM(DMFactory.DM_SSP);

	        User user = null;
	        
	        try {
	        	user = userDirectoryService().getUser(userId);
	        } catch (Exception exc) {
	        	log.error("Unable to find user for " + userId, exc);
	        }
	        	
	        // TODO: Need to replace this with some reasonable alternative
	        initSCOData(scoDataManager, runState, user, courseId, scoId);
	
	        // TODO: Need to replace this with some reasonable alternative
	        // SSP addition
	        initSSPData(scoDataManager, courseId, scoId, numberOfAttempts, userId);
        	        
	        isNewDataManager = true;
        } else {
        	scoDataManager.setValidatorFactory(new ValidatorFactory());
        }
        
        
        runState.setDataManager(scoDataManager);

        List mStatusVector = runState.getCurrentObjStatusSet();

        ADLObjStatus mObjStatus = new ADLObjStatus();

        // Temporary variables for obj initialization
        int err = 0;
        String obj = new String();

        // Initialize Objectives based on global objectives
        if (mStatusVector != null) {
           if (isNewDataManager) {
              for( int i = 0; i < mStatusVector.size(); i++ ) {
                 // initialize objective status from sequencer
                 mObjStatus = (ADLObjStatus)mStatusVector.get(i);

                 // Set the objectives id
                 obj = "cmi.objectives." + i + ".id";

                 err = DMInterface.processSetValue(obj, mObjStatus.mObjID, true, scoDataManager);

                 // Set the objectives success status
                 obj = "cmi.objectives." + i + ".success_status";

                 if( mObjStatus.mStatus.equalsIgnoreCase("satisfied") )
                 {
                    err = DMInterface.processSetValue(obj, "passed", true, scoDataManager);
                 }
                 else if( mObjStatus.mStatus.equalsIgnoreCase("notSatisfied") )
                 {
                    err = DMInterface.processSetValue(obj, "failed", true, scoDataManager);
                 }

                 // Set the objectives scaled score
                 obj = "cmi.objectives." + i + ".score.scaled";

                 if( mObjStatus.mHasMeasure ) {
                    Double norm = new Double(mObjStatus.mMeasure);
                    err = DMInterface.processSetValue(obj, norm.toString(), true, scoDataManager);
                 }
              }
           }
           else {
              for( int i = 0; i < mStatusVector.size(); i++ )
              {
                 int idx = -1;

                 // initialize objective status from sequencer
                 mObjStatus = (ADLObjStatus)mStatusVector.get(i);

                 // get the count of current objectives
                 DMProcessingInfo pi = new DMProcessingInfo();
                 int result = DMInterface.processGetValue("cmi.objectives._count", true, scoDataManager, pi);

                 int objCount = ( new Integer(pi.mValue) ).intValue();

                 // Find the current index for this objective
                 for( int j = 0; j < objCount; j++ )
                 {
                    pi = new DMProcessingInfo();
                    obj = "cmi.objectives." + j + ".id";

                    result = DMInterface.processGetValue(obj, true, scoDataManager, pi);

                    
                    if( pi.mValue.equals(mObjStatus.mObjID) )
                    {
                       
                       idx = j;
                       break;
                    }
                 }

                 if( idx != -1 )
                 {
                                     
                    // Set the objectives success status
                    obj = "cmi.objectives." + idx + ".success_status";

                    if( mObjStatus.mStatus.equalsIgnoreCase("satisfied") )
                    {
                       err = DMInterface.processSetValue(obj, "passed", true, scoDataManager);
                    }
                    else if( mObjStatus.mStatus.equalsIgnoreCase("notSatisfied") )
                    {
                       err = DMInterface.processSetValue(obj, "failed", true, scoDataManager);
                    }

                    // Set the objectives scaled score
                    obj = "cmi.objectives." + idx + ".score.scaled";

                    if( mObjStatus.mHasMeasure )
                    {
                       Double norm = new Double(mObjStatus.mMeasure);
                       err = DMInterface.processSetValue(obj, norm.toString(), true, scoDataManager);
                    }
                 }
                 else
                 {
                    log.warn("  OBJ NOT FOUND --> " + mObjStatus.mObjID);
                 }

              }
           }
        }
        
        persistDataManager(runState, scoDataManager);
        
        return scoDataManager;
	}
	
	public void persistActivityTree(ISeqActivityTree tree) {
		seqActivityTreeDao.save(tree);
	}
	
	private void persistDataManager(IRunState runState, IDataManager scoDataManager) {
		if (null != runState.getCurrentNavState()) {
	        SCORM_2004_NAV_DM navDM = (SCORM_2004_NAV_DM)scoDataManager.getDataModel("adl");
	
			navDM.setValidRequests(runState.getCurrentNavState());
	        
			dataManagerDao.save(scoDataManager);
        } else {
        	log.error("Current nav state is null!");
        }
	}
	
	
	public IValidRequests commit(IRunState runState) {
		log.debug("Service - Commit");
		
		if (null == runState)
			return null;
		
	      boolean setPrimaryObjScore = false;
		boolean suspended = false;

		String primaryObjectiveId = null;
		String activityId = runState.getCurrentActivityId();

		try {
			IDataManager scoDataManager = runState.getDataManager();

			String completionStatus = null;
			String SCOEntry = null;
			double normalScore = -1.0;
			String masteryStatus = null;
			String sessionTime = null;
			String score = null;

			// call terminate on the sco data
			scoDataManager.terminate();

			int err = 0;
			DMProcessingInfo dmInfo = new DMProcessingInfo();

			// Get the current completion_status
			err = DMInterface.processGetValue("cmi.completion_status", true,
					scoDataManager, dmInfo);
			completionStatus = dmInfo.mValue;

			if (completionStatus.equals("not attempted")) {
				completionStatus = "incomplete";
			}

			// Get the current success_status
			err = DMInterface.processGetValue("cmi.success_status", true,
					scoDataManager, dmInfo);
			masteryStatus = dmInfo.mValue;

			// Get the current entry
			err = DMInterface.processGetValue("cmi.entry", true, true, scoDataManager,
					dmInfo);
			SCOEntry = dmInfo.mValue;

			// Get the current scaled score
			err = DMInterface.processGetValue("cmi.score.scaled", true,
					scoDataManager, dmInfo);

			if (err == DMErrorCodes.NO_ERROR) {
				log.debug("Got score, with no error");
				score = dmInfo.mValue;
			} else {
				log.warn("Failed getting score, got err: " + err);
				score = "";
			}

			// Get the current session time
			err = DMInterface.processGetValue("cmi.session_time", true,
					scoDataManager, dmInfo);
			if (err == DMErrorCodes.NO_ERROR) {
				sessionTime = dmInfo.mValue;
			}

			ISequencer theSequencer = runState.getSequencer();

			if (theSequencer != null) {
				ISeqActivity act = runState.getCurrentActivity();

				// Only modify the TM if the activity is tracked
				if (act.getIsTracked()) {

					// Update the activity's status
					if (log.isDebugEnabled()) {
						log.debug(act.getID() + " is TRACKED -- ");
						log.debug("Performing default mapping to TM");
					}
					
					String primaryObjID = null;
					boolean foundPrimaryObj = false;
					boolean setPrimaryObjSuccess = false;
					boolean sesPrimaryObjScore = false;

					// Find the primary objective ID
					Vector objs = (Vector) act.getObjectives();

					if (objs != null) {
						for (int j = 0; j < objs.size(); j++) {
							SeqObjective obj = (SeqObjective) objs.elementAt(j);
							if (obj.mContributesToRollup) {
								if (obj.mObjID != null) {
									primaryObjID = obj.mObjID;
								}
								break;
							}
						}
					}

					// Get the activities objective list
					// Map the DM to the TM
					err = DMInterface.processGetValue("cmi.objectives._count",
							true, scoDataManager, dmInfo);
					Integer size = new Integer(dmInfo.mValue);
					int numObjs = size.intValue();

					// Loop through objectives updating TM
					for (int i = 0; i < numObjs; i++) {
						log.debug("CMISerlet - IN MAP OBJ LOOP");
						String objID = new String("");
						String objMS = new String("");
						String objScore = new String("");
						String obj = new String("");

						// Get this objectives id
						obj = "cmi.objectives." + i + ".id";
						err = DMInterface.processGetValue(obj, true, scoDataManager,
								dmInfo);

						objID = dmInfo.mValue;

						if (primaryObjID != null && objID.equals(primaryObjID)) {
							foundPrimaryObj = true;
						} else {
							foundPrimaryObj = false;
						}

						// Get this objectives mastery
						obj = "cmi.objectives." + i + ".success_status";
						err = DMInterface.processGetValue(obj, true, scoDataManager,
								dmInfo);
						objMS = dmInfo.mValue;

						// Report the success status
						if (objMS.equals("passed")) {
							theSequencer.setAttemptObjSatisfied(activityId,
									objID, "satisfied");
							if (foundPrimaryObj) {
								setPrimaryObjSuccess = true;
							}
						} else if (objMS.equals("failed")) {
							theSequencer.setAttemptObjSatisfied(activityId,
									objID, "notSatisfied");

							if (foundPrimaryObj) {
								setPrimaryObjSuccess = true;
							}
						} else {
							theSequencer.setAttemptObjSatisfied(activityId,
									objID, "unknown");
						}

						// Get this objectives measure
						obj = "cmi.objectives." + i + ".score.scaled";
						err = DMInterface.processGetValue(obj, true, scoDataManager,
								dmInfo);
						if (err == DMErrorCodes.NO_ERROR) {
							objScore = dmInfo.mValue;
						}

						// Report the measure
						if (!objScore.equals("") && !objScore.equals("unknown")) {
							try {
								normalScore = (new Double(objScore))
										.doubleValue();
								theSequencer.setAttemptObjMeasure(activityId,
										objID, normalScore);

								if (foundPrimaryObj) {
									setPrimaryObjScore = true;
								}
							} catch (Exception e) {
								log.error("  ::--> ERROR: Invalid score");
								log.error("  ::  " + normalScore);

								log.error("Exception was: ", e);
							}
						} else {
							theSequencer.clearAttemptObjMeasure(activityId,
									objID);
						}
					}

					// Report the completion status
					theSequencer.setAttemptProgressStatus(activityId,
							completionStatus);

					if (SCOEntry.equals("resume")) {
						theSequencer.reportSuspension(activityId, true);
					} else {

						// TODO: replace with db code.
						// Clean up session level SSP buckets
						// RTEFileHandler fileHandler = new RTEFileHandler();
						// fileHandler.deleteAttemptSSPData(userId, courseId,
						// scoId);

						theSequencer.reportSuspension(activityId, false);
					}

					// Report the success status
					if (masteryStatus.equals("passed")) {
						theSequencer.setAttemptObjSatisfied(activityId,
								primaryObjectiveId, "satisfied");
					} else if (masteryStatus.equals("failed")) {
						theSequencer.setAttemptObjSatisfied(activityId,
								primaryObjectiveId, "notSatisfied");
					} else {
						if (!setPrimaryObjSuccess) {
							theSequencer.setAttemptObjSatisfied(activityId,
									primaryObjectiveId, "unknown");
						}
					}

					// Report the measure
					if (!score.equals("") && !score.equals("unknown")) {
						try {
							normalScore = (new Double(score)).doubleValue();
							theSequencer.setAttemptObjMeasure(activityId,
									primaryObjectiveId, normalScore);
						} catch (Exception e) {
							log.error("  ::--> ERROR: Invalid score");
							log.error("  ::  " + normalScore);

							log.error("Exception was: ", e);
						}
					} else {
						if (!setPrimaryObjScore) {
							theSequencer.clearAttemptObjMeasure(activityId,
									primaryObjectiveId);
						}
					}
				}

				// May need to get the current valid requests
				ADLValidRequests validRequests = new ADLValidRequests();
				theSequencer.getValidRequests(validRequests);

				log.debug("Sequencer is initialized and statuses have been set");

				ISeqActivityTree theTempTree = theSequencer.getActivityTree();

				theTempTree.clearSessionState();

				//persistActivityTree(theTempTree);
				persistDataManager(runState, scoDataManager);
				
				
				return validRequests;
			}
		} catch (Exception e) {
			log.error("Caught an exception:", e);
		}

		return null;
	}
	
	
	private void initSCOData(IDataManager ioSCOData, IRunState runState, User user, String iCourseID, String iItemID) {
		   
		try {   	
			String masteryScore = new String();
			String dataFromLMS = new String();
			String maxTime = new String();
			String timeLimitAction = new String();
			String completionThreshold = new String();
				
			// User preferences
			String audLev = DEFAULT_USER_AUDIO_LEVEL;
			String audCap = DEFAULT_USER_AUDIO_CAPTIONING;
			String delSpd = DEFAULT_USER_DELIVERY_SPEED;
			String lang = DEFAULT_USER_LANGUAGE;
				
			// Get the learner preference values from Sakai
			ResourceProperties props = user.getProperties();
				
		    if (props != null) {
		       	if (null != props.getProperty(PREF_USER_AUDIO_LEVEL))
		       		audLev = props.getProperty(PREF_USER_AUDIO_LEVEL);
		       	if (null != props.getProperty(PREF_USER_AUDIO_CAPTIONING))
		       		audCap = props.getProperty(PREF_USER_AUDIO_CAPTIONING);
		       	if (null != props.getProperty(PREF_USER_DELIVERY_SPEED))
		       		delSpd = props.getProperty(PREF_USER_DELIVERY_SPEED);
		       	if (null != props.getProperty(PREF_USER_LANGUAGE))
		       		lang = props.getProperty(PREF_USER_LANGUAGE);
		    }
				
		    // Get sco data from manifest
		    ILaunchData launchData = runState.getCurrentLaunchData();
		    masteryScore = launchData.getMinNormalizedMeasure(); //rsItem.getString("MinNormalizedMeasure");
            dataFromLMS = launchData.getDataFromLMS(); //rsItem.getString("DataFromLMS");
            maxTime = launchData.getAttemptAbsoluteDurationLimit(); // rsItem.getString("AttemptAbsoluteDurationLimit");
            timeLimitAction = launchData.getTimeLimitAction(); //rsItem.getString("TimeLimitAction");
            completionThreshold = launchData.getCompletionThreshold(); // rsItem.getString("CompletionThreshold");

			String element = new String();

			if (null != user) {
				// Initialize the learner id
				element = "cmi.learner_id";
				DMInterface.processSetValue(element, user.getId(), true,
						ioSCOData);

				// Initialize the learner name
				element = "cmi.learner_name";
				DMInterface.processSetValue(element, user.getDisplayName(),
						true, ioSCOData);
			}

			// Initialize the cmi.credit value
			element = "cmi.credit";
			DMInterface.processSetValue(element, "credit", true, ioSCOData);

			// Initialize the mode
			element = "cmi.mode";
			DMInterface.processSetValue(element, "normal", true, ioSCOData);

			// Initialize any launch data
			if (dataFromLMS != null && !dataFromLMS.equals("")) {
				element = "cmi.launch_data";
				DMInterface.processSetValue(element, dataFromLMS, true,
						ioSCOData);
			}

			// Initialize the scaled passing score
			if (masteryScore != null && !masteryScore.equals("")) {
				element = "cmi.scaled_passing_score";
				DMInterface.processSetValue(element, masteryScore, true,
						ioSCOData);
			}

			// Initialize the time limit action
			if (timeLimitAction != null && !timeLimitAction.equals("")) {
				element = "cmi.time_limit_action";
				DMInterface.processSetValue(element, timeLimitAction, true,
						ioSCOData);
			}

			// Initialize the completion_threshold
			if (completionThreshold != null && !completionThreshold.equals("")) {
				element = "cmi.completion_threshold";
				DMInterface.processSetValue(element, completionThreshold, true,
						ioSCOData);
			}

			// Initialize the max time allowed
			if (maxTime != null && !maxTime.equals("")) {
				element = "cmi.max_time_allowed";
				DMInterface.processSetValue(element, maxTime, true, ioSCOData);
			}

			// Initialize the learner preferences based on the SRTE
			// learner profile information

			// audio_level
			element = "cmi.learner_preference.audio_level";
			DMInterface.processSetValue(element, audLev, true, ioSCOData);

			// audio_captioning
			element = "cmi.learner_preference.audio_captioning";
			DMInterface.processSetValue(element, audCap, true, ioSCOData);

			// delivery_speed
			element = "cmi.learner_preference.delivery_speed";
			DMInterface.processSetValue(element, delSpd, true, ioSCOData);

			// language
			element = "cmi.learner_preference.language";
			DMInterface.processSetValue(element, lang, true, ioSCOData);

		} catch (Exception e) {
			log.error("Caught an exception while trying to initalize the sco data", e);
		}
	}
	
	
	
	   private void initSSPData(IDataManager ioSCOData, String iCourseID,
			String iScoID, String iNumAttempt, String iUserID) {
			String element = new String();
			int err;
	
			// Initialize the learner id
			element = "ssp.init.userid";
			err = DMInterface.processSetValue(element, iUserID, true, ioSCOData);
	
			// Initialize the course id
			element = "ssp.init.courseid";
			err = DMInterface.processSetValue(element, iCourseID, true, ioSCOData);
	
			// Initialize the attempt id
			element = "ssp.init.attemptnum";
			err = DMInterface
					.processSetValue(element, iNumAttempt, true, ioSCOData);
	
			// FIXME: Figure out where this comes from and what it means...
			// Initialize the attempt id
			//element = "ssp.init.url";
			//err = DMInterface.processSetValue(element, iSSPLocation, true,
			//		ioSCOData);
	
			// Initialize the sco id
			element = "ssp.init.scoID";
			err = DMInterface.processSetValue(element, iScoID, true, ioSCOData);
	
			/*ILaunchData launchData = runState.getCurrentLaunchData();
			String persistence = launchData.getPersistState();
			String minSize = launchData.getRe
			String requestedSize = new String();
			String reducible = new String();

			String allocateString = new String();
			
			
			// Check for MANDATORY requested
			if ((requestedSize == null) || (requestedSize.equals(""))) {
				err = 351;
			} else {
				allocateString += "{requested=" + requestedSize + "}";
			}

			// Check for OPTIONAL persistence
			if ((persistence == null) || (persistence.equals(""))) {
				allocateString = allocateString;
			} else {
				allocateString += "{persistence=" + persistence + "}";
			}

			// Check for OPTIONAL min
			if ((minSize == null) || (minSize.equals(""))) {
				allocateString = allocateString;
			} else {
				allocateString += "{minimum=" + minSize + "}";
			}

			// Check for OPTIONAL reducible
			if ((reducible == null) || (reducible.equals(""))) {
				allocateString = allocateString;
			} else {
				allocateString += "{reducible=" + reducible + "}";
			}

			if (err != 351) {
				err = DMInterface.processSetValue("ssp.allocate",
						allocateString, true, ioSCOData);
			}*/
			
			/*try {
				// Get some information from the database
				Connection conn = SSP_DBHandler.getConnection();
				PreparedStatement stmtSelectSSP_BucketTbl;
	
				String sqlSelectSSP_BucketTbl = "SELECT * FROM SSP_BucketTbl"
						+ " WHERE CourseID = ? AND ScoID = ?";
				stmtSelectSSP_BucketTbl = conn
						.prepareStatement(sqlSelectSSP_BucketTbl);
	
				if (_Debug) {
					System.out
							.println("about to call SSP_BucketTbl in initSSPData");
					System.out.println("courseID: " + iCourseID);
					System.out.println("scoID: " + iScoID);
				}
	
				ResultSet rsSSP_BucketTbl = null;
	
				synchronized (stmtSelectSSP_BucketTbl) {
					stmtSelectSSP_BucketTbl.setString(1, iCourseID);
					stmtSelectSSP_BucketTbl.setString(2, iScoID);
					rsSSP_BucketTbl = stmtSelectSSP_BucketTbl.executeQuery();
				}
	
				if (_Debug) {
					System.out.println("call to SSP_BucketTbl is complete");
				}
	
				String bucketID = new String();
				String bucketType = new String();
				String persistence = new String();
				String minSize = new String();
				String requestedSize = new String();
				String reducible = new String();
	
				String allocateString = new String();
	
				int errorCode = 0;
	
				// Get the values for the buckets defined in the manifest
				// from the database
				while (rsSSP_BucketTbl.next()) {
					bucketID = rsSSP_BucketTbl.getString("BucketID");
					bucketType = rsSSP_BucketTbl.getString("BucketType");
					persistence = rsSSP_BucketTbl.getString("Persistence");
					minSize = rsSSP_BucketTbl.getString("Min");
					requestedSize = rsSSP_BucketTbl.getString("Requested");
					reducible = rsSSP_BucketTbl.getString("Reducible");
	
					// Allocate for each bucket listed in the Manifest
					// CHeck for MANDATORY BucketID
					if ((bucketID == null) || (bucketID.equals(""))) {
						err = 351;
					} else {
						allocateString = "{bucketID=" + bucketID + "}";
					}
	
					// Check for MANDATORY requested
					if ((requestedSize == null) || (requestedSize.equals(""))) {
						err = 351;
					} else {
						allocateString += "{requested=" + requestedSize + "}";
					}
	
					// Check for OPTIONAL type
					if ((bucketType == null) || (bucketType.equals(""))) {
						allocateString = allocateString;
					} else {
						allocateString += "{type=" + bucketType + "}";
					}
	
					// Check for OPTIONAL persistence
					if ((persistence == null) || (persistence.equals(""))) {
						allocateString = allocateString;
					} else {
						allocateString += "{persistence=" + persistence + "}";
					}
	
					// Check for OPTIONAL min
					if ((minSize == null) || (minSize.equals(""))) {
						allocateString = allocateString;
					} else {
						allocateString += "{minimum=" + minSize + "}";
					}
	
					// Check for OPTIONAL reducibler
					if ((reducible == null) || (reducible.equals(""))) {
						allocateString = allocateString;
					} else {
						allocateString += "{reducible=" + reducible + "}";
					}
	
					if (err != 351) {
						err = DMInterface.processSetValue("ssp.allocate",
								allocateString, true, ioSCOData);
					}
	
				}
	
				stmtSelectSSP_BucketTbl.close();
				conn.close();
	
			} catch (Exception e) {
				e.printStackTrace();
			}*/
	   }
	
	
	
	public SeqActivityTreeDao getSeqActivityTreeDao() {
		return seqActivityTreeDao;
	}
	public void setSeqActivityTreeDao(SeqActivityTreeDao seqActivityTreeDao) {
		this.seqActivityTreeDao = seqActivityTreeDao;
	}
	public DataManagerDao getDataManagerDao() {
		return dataManagerDao;
	}
	public void setDataManagerDao(DataManagerDao dataManagerDao) {
		this.dataManagerDao = dataManagerDao;
	}
	
	
	
	
	
	public User getCurrentUser() {
		return userDirectoryService().getCurrentUser();
	}
	
	
	
	
	
	public IValidatorFactory newValidatorFactory() {
		return new ValidatorFactory();
	}
	
	
	
	
	
	
}
