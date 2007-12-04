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
import org.adl.validator.IValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentEntity;
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
import org.sakaiproject.scorm.service.api.ScormApplicationService;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormPermissionService;
import org.sakaiproject.scorm.service.api.ScormResultService;
import org.sakaiproject.scorm.service.api.ScormSequencingService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
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
	
	protected ScormApplicationService scormApplicationService() { return null; }
	protected ScormContentService scormContentService() { return null; }
	protected ScormPermissionService scormPermissionService() { return null; }
	protected ScormResultService scormResultService() { return null; }
	protected ScormSequencingService scormSequencingService() { return null; }
	
	// Dependency injected properties
	protected ResourceTypeRegistry resourceTypeRegistry;
	protected SeqActivityTreeDao seqActivityTreeDao;
	protected DataManagerDao dataManagerDao;
	
	public void init() {
		//entityManager().registerEntityProducer(this, REFERENCE_ROOT);
	}
	
	
	public ScormApplicationService applicationProgrammingInterface() {
		return scormApplicationService();
	}
	
	public ScormContentService contentPackageInterface() {
		return scormContentService();
	}
	
	public ScormPermissionService permissionInterface() {
		return scormPermissionService();
	}
	
	public ScormResultService resultInterface() {
		return scormResultService();
	}
	
	public ScormSequencingService sequencingInterface() {
		return scormSequencingService();
	}
	
	
	/*public String addContentPackage(File contentPackage, IValidator validator, IValidatorOutcome outcome) throws Exception {
		return scormContentService().addContentPackage(contentPackage, validator, outcome);
	}
	
	public List<ContentEntity> getContentPackages() {		
		return scormContentService().getContentPackages();
	}
		
	public String getContentPackageTitle(Document document) {
		return scormContentService().getContentPackageTitle(document);
	}
	
	public void validate(String resourceId, boolean isManifestOnly, boolean isValidateToSchema) throws Exception {
		scormContentService().validate(resourceId, isManifestOnly, isValidateToSchema);
	}
	
	public IValidator validate(File contentPackage, boolean iManifestOnly, boolean iValidateToSchema) {
		return scormContentService().validate(contentPackage, iManifestOnly, iValidateToSchema);
	}
	
	public IValidatorOutcome validateContentPackage(File contentPackage, boolean onlyValidateManifest) {
		return scormContentService().validateContentPackage(contentPackage, onlyValidateManifest);
	}
	
	//public void removeContentPackage(String contentPackageId) {
	//	scormContentService().removeContentPackage(contentPackageId);
	//}
	
	public ContentResource addManifest(ContentPackageManifest manifest, String id) {
		return scormContentService().addManifest(manifest, id);
	}

	//public ContentPackageManifest getManifest(String contentPackageId) {
	//	return scormContentService().getManifest(contentPackageId);
	//}
	
	public String getContext() {
		return toolManager().getCurrentPlacement().getContext();
	}
	
	public void uploadZipArchive(File zipArchive) throws IdUnusedException, IdUniquenessException, IdLengthException, IdInvalidException, OverQuotaException, ServerOverloadException, PermissionException{
		scormContentService().uploadZipArchive(zipArchive);
	}
	
	public String identifyZipArchive() {
		return scormContentService().identifyZipArchive();
	}*/
	
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
	
	/*public String getUserName() {
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
	
	public ContentEntity getContentEntity() {
		ResourceToolActionPipe pipe = getResourceToolActionPipe();
		
		return pipe.getContentEntity();
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
	
	
	
	
	public String getConfigurationString(String key, String defaultValue) {
		return serverConfigurationService().getString(key, defaultValue);
	}
	
	
	public void setResourceTypeRegistry(ResourceTypeRegistry registry)
	{
		resourceTypeRegistry = registry;
	}
	
	public ResourceTypeRegistry getResourceTypeRegistry()
	{
		return resourceTypeRegistry;
	}
	
	
	
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
	
	public List<IDataManager> getContentPackageDataManagers(String contentPackageId) {
		return dataManagerDao.find(contentPackageId);
	}
	
	public IDataManager getContentPackageDataManger(String contentPackageId, String userId) {
		return dataManagerDao.find(contentPackageId, userId);
	}
	

	public IDataManager initialize(IRunState runState, String numberOfAttempts) {
		log.debug("Service - Initialize");
   
		if (null == runState)
			return null;
		
		String courseId = runState.getCurrentCourseId();
		String scoId = runState.getCurrentSco();
		String userId = runState.getCurrentUserId();
		String title = runState.getTitle();
		
        boolean isNewDataManager = false;
        IDataManager scoDataManager = null;
        
// FIXME : We need to look in the db first -- but currently it doesn't seem that data is getting saved correctly
        // First, check to see if we have a ScoDataManager persisted
        scoDataManager = dataManagerDao.find(courseId, userId);

        if (scoDataManager == null) {
	        // If not, create one
	        scoDataManager = new SCODataManager(courseId, userId, title);
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
	
	private String retrieveValue(String iRequest, String defaultValue, IDataManager scoDataManager) {
		return retrieveValue(iRequest, defaultValue, scoDataManager, false);
	}
		
	private String retrieveValue(String iRequest, String defaultValue, IDataManager scoDataManager, boolean iAdmin) {
		int err = 0;
		DMProcessingInfo dmInfo = new DMProcessingInfo();

		// Get the current completion_status
		err = DMInterface.processGetValue(iRequest, iAdmin, true, scoDataManager, dmInfo);
		
		if (err != DMErrorCodes.NO_ERROR) {
			log.warn("Found an error retrieving value for " + iRequest + " error is " + err);
			if (defaultValue == null)
				return "unknown";
			else
				return defaultValue;
		}
		
		return dmInfo.mValue;
	}
	
	
	private final static String CMI_OBJECTIVES_ROOT = "cmi.objectives.";
	private final static String CMI_COMPLETION_STATUS = "cmi.completion_status";
	private final static String CMI_SUCCESS_STATUS = "cmi.success_status";
	private final static String CMI_ENTRY = "cmi.entry";
	private final static String CMI_SCORE_SCALED = "cmi.score.scaled";
	private final static String CMI_SESSION_TIME = "cmi.session_time";
	
	public class CmiData {
		public boolean setPrimaryObjSuccess = false;
		public boolean setPrimaryObjScore = false;
		public double normalScore = -1.0;
		
		public String completionStatus = null;
		public String SCOEntry = "unknown";

		public String masteryStatus = "unknown";
		public String sessionTime = null;
		public String score = "unknown";
		
		public String activityId;
		public String primaryObjectiveId;
		
		public CmiData() {
			
		}
		
	}
	
	
	private void processObjectives(int numObjs, CmiData cmiData, IDataManager dm, ISequencer sequencer) {
		
		boolean foundPrimaryObj = false;
		
		for (int i = 0; i < numObjs; i++) {
			// Get this objectives id
			String objIdRequest = new StringBuilder(CMI_OBJECTIVES_ROOT).append(i).append(".id").toString();
			String objID = retrieveValue(objIdRequest, "", dm);
			
			
			if (cmiData.primaryObjectiveId != null && objID.equals(cmiData.primaryObjectiveId)) {
				foundPrimaryObj = true;
			} else {
				foundPrimaryObj = false;
			}

			// Get this objectives mastery
			String objMsRequest = new StringBuilder(CMI_OBJECTIVES_ROOT).append(i).append(".success_status").toString();
			String objMS = retrieveValue(objMsRequest, "", dm);
			
			// Report the success status
			if (objMS.equals("passed")) {
				sequencer.setAttemptObjSatisfied(cmiData.activityId, objID, "satisfied");
				
				if (foundPrimaryObj) 
					cmiData.setPrimaryObjSuccess = true;
				
			} else if (objMS.equals("failed")) {
				sequencer.setAttemptObjSatisfied(cmiData.activityId, objID, "notSatisfied");

				if (foundPrimaryObj) 
					cmiData.setPrimaryObjSuccess = true;
			} else 
				sequencer.setAttemptObjSatisfied(cmiData.activityId, objID, "unknown");
			

			// Get this objectives measure
			String objScoreRequest = new StringBuilder(CMI_OBJECTIVES_ROOT).append(i).append(".score.scaled").toString();
			String objScore = retrieveValue(objScoreRequest, "unknown", dm);
			

			// Report the measure
			if (!objScore.equals("") && !objScore.equals("unknown")) {
				try {
					cmiData.normalScore = (new Double(objScore)).doubleValue();
					sequencer.setAttemptObjMeasure(cmiData.activityId, objID, cmiData.normalScore);

					if (foundPrimaryObj) 
						cmiData.setPrimaryObjScore = true;
					
				} catch (Exception e) {
					log.error("Found no valid score for this objective: " + cmiData.normalScore, e);
				}
			} else 
				sequencer.clearAttemptObjMeasure(cmiData.activityId, objID);	
		}
	}
	
	private void processTracking(ISeqActivity act, CmiData cmiData, IDataManager dm, ISequencer sequencer) {
		// Only modify the TM if the activity is tracked
		if (act.getIsTracked()) {

			// Update the activity's status
			if (log.isDebugEnabled()) {
				log.debug(act.getID() + " is TRACKED -- ");
				log.debug("Performing default mapping to TM");
			}
			
			// Find the primary objective ID
			List<SeqObjective> objs = (List<SeqObjective>) act.getObjectives();

			if (objs != null) {
				for (int j = 0; j < objs.size(); j++) {
					SeqObjective obj = objs.get(j);
					if (obj.mContributesToRollup) {
						if (obj.mObjID != null) {
							cmiData.primaryObjectiveId = obj.mObjID;
						}
						break;
					}
				}
			}

			// Get the activities objective list
			// Map the DM to the TM
			String size = retrieveValue("cmi.objectives._count", "none", dm);
			
			if (!size.equals("none")) {
				try {
					Integer s = new Integer(size);
					int numObjs = s.intValue();
	
					// Loop through objectives updating TM
					processObjectives(numObjs, cmiData, dm, sequencer);
					
				} catch (NumberFormatException nfe) {
					log.warn("Caught a number format exception trying to process the string " + size);
				}
				
			}
			
			// Report the completion status
			sequencer.setAttemptProgressStatus(cmiData.activityId, cmiData.completionStatus);

			if (cmiData.SCOEntry.equals("resume")) {
				sequencer.reportSuspension(cmiData.activityId, true);
			} else {

				// TODO: replace with db code.
				// Clean up session level SSP buckets
				// RTEFileHandler fileHandler = new RTEFileHandler();
				// fileHandler.deleteAttemptSSPData(userId, courseId,
				// scoId);

				sequencer.reportSuspension(cmiData.activityId, false);
			}

			// Report the success status
			if (cmiData.masteryStatus.equals("passed")) {
				sequencer.setAttemptObjSatisfied(cmiData.activityId,
						cmiData.primaryObjectiveId, "satisfied");
			} else if (cmiData.masteryStatus.equals("failed")) {
				sequencer.setAttemptObjSatisfied(cmiData.activityId,
						cmiData.primaryObjectiveId, "notSatisfied");
			} else {
				if (!cmiData.setPrimaryObjSuccess) {
					sequencer.setAttemptObjSatisfied(cmiData.activityId,
							cmiData.primaryObjectiveId, "unknown");
				}
			}

			// Report the measure
			if (!cmiData.score.equals("") && !cmiData.score.equals("unknown")) {
				try {
					cmiData.normalScore = (new Double(cmiData.score)).doubleValue();
					sequencer.setAttemptObjMeasure(cmiData.activityId, cmiData.primaryObjectiveId, cmiData.normalScore);
				} catch (Exception e) {
					log.error("  ::--> ERROR: Invalid score");
					log.error("  ::  " + cmiData.normalScore);

					log.error("Exception was: ", e);
				}
			} else {
				if (!cmiData.setPrimaryObjScore) {
					sequencer.clearAttemptObjMeasure(cmiData.activityId,
							cmiData.primaryObjectiveId);
				}
			}
		}
	}
	
	
	private CmiData lookupCmiData(IRunState runState, IDataManager scoDataManager) {

		CmiData status = new CmiData();

		status.primaryObjectiveId = null;
		status.activityId = runState.getCurrentActivityId();
		
		// Get the current completion_status
		status.completionStatus = retrieveValue(CMI_COMPLETION_STATUS, "unknown", scoDataManager);
		
		if (status.completionStatus.equals("not attempted")) {
			status.completionStatus = "incomplete";
		}

		// Get the current success_status
		status.masteryStatus = retrieveValue(CMI_SUCCESS_STATUS, "unknown", scoDataManager);
		
		// Get the current entry
		status.SCOEntry = retrieveValue(CMI_ENTRY, "unknown", scoDataManager, true);
					
		// Get the current scaled score
		status.score = retrieveValue(CMI_SCORE_SCALED, "", scoDataManager);
		
		// Get the current session time
		status.sessionTime = retrieveValue(CMI_SESSION_TIME, "unknown", scoDataManager);
		
		return status;
	}
	
	
	public IValidRequests commit(IRunState runState) {
		log.debug("Service - Commit");
		
		if (null == runState) {
			log.warn("No run state in commit");
			return null;
		}
		
		ADLValidRequests validRequests = null;
		
		try {
			IDataManager dm = runState.getDataManager();

			// Call terminate on the data manager to ensure that we're in the appropriate state
			dm.terminate();

			// Gather information from the data manager
			CmiData cmiData = lookupCmiData(runState, dm);
			
			// Process sequencing changes
			ISequencer sequencer = runState.getSequencer();

			if (sequencer != null) {
				ISeqActivity act = runState.getCurrentActivity();

				processTracking(act, cmiData, dm, sequencer);

				// May need to get the current valid requests
				validRequests = new ADLValidRequests();
				sequencer.getValidRequests(validRequests);

				log.debug("Sequencer is initialized and statuses have been set");

				ISeqActivityTree theTempTree = sequencer.getActivityTree();

				theTempTree.clearSessionState();
			}
				
			//persistActivityTree(theTempTree);
			persistDataManager(runState, dm);

		} catch (Exception e) {
			log.error("Caught an exception:", e);
		}

		return validRequests;
	}
	
	
	public IValidRequests WORKINGcommit(IRunState runState) {
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
			String SCOEntry = "unknown";
			double normalScore = -1.0;
			String masteryStatus = "unknown";
			String sessionTime = null;
			String score = "unknown";

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

			// Added, JLR 11/2/2007
			dmInfo = new DMProcessingInfo();
			
			// Get the current success_status
			err = DMInterface.processGetValue("cmi.success_status", true,
					scoDataManager, dmInfo);
			
			if (err == DMErrorCodes.NO_ERROR)
				masteryStatus = dmInfo.mValue;

			// Added, JLR 11/2/2007
			dmInfo = new DMProcessingInfo();
			
			// Get the current entry
			err = DMInterface.processGetValue("cmi.entry", true, true, scoDataManager,
					dmInfo);
			
			if (err == DMErrorCodes.NO_ERROR)
				SCOEntry = dmInfo.mValue;

			// Added, JLR 11/2/2007
			dmInfo = new DMProcessingInfo();
			
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

			// Added, JLR 11/2/2007
			dmInfo = new DMProcessingInfo();
			
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
					// Added, JLR 11/2/2007
					dmInfo = new DMProcessingInfo();
					err = DMInterface.processGetValue("cmi.objectives._count",
							true, scoDataManager, dmInfo);
					if (err == DMErrorCodes.NO_ERROR) {
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
	
	public User getUser(String userId) throws UserNotDefinedException {
		return userDirectoryService().getUser(userId);
	}
	
	
	
	public IValidatorFactory newValidatorFactory() {
		return new ValidatorFactory();
	}*/
	
	
	
	
	
	
}
