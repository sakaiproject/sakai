/**
 * Copyright (c) 2007-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational 
* Community License, Version 2.0 (the "License"); you may not use this file 
* except in compliance with the License. You may obtain a copy of the 
* License at:
*
* http://opensource.org/licenses/ecl2.txt
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.signup.tool.entityproviders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.logic.SignupMeetingService;
import org.sakaiproject.signup.model.MeetingTypes;
import org.sakaiproject.signup.model.SignupAttachment;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupGroup;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupSite;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.tool.jsf.organizer.CopyMeetingSignupMBean;
import org.sakaiproject.signup.tool.jsf.organizer.action.CreateMeetings;
import org.sakaiproject.signup.tool.util.Utilities;
import org.sakaiproject.site.api.SiteService;

@Slf4j
public class SignupEntityProducer implements MeetingTypes, EntityProducer, EntityTransferrer,
		ApplicationContextAware {

	public static final String SIGNUP = "signup";
	public static final String REFERENCE_ROOT = Entity.SEPARATOR + SIGNUP;

	private EntityManager entityManager;
	private SakaiFacade sakaiFacade;
	ApplicationContext applicationContext;
	private CopyFileProcessor copyFileProcessor;
	private static boolean DEFAULT_ARCHIVE_SUPPORT = "true".equalsIgnoreCase(Utilities.getSignupConfigParamVal("signup.isarchive.support", "false")) ? true : false;
	
    public void init() {
        if (log.isDebugEnabled()) log.debug("signup EP.init()");
        try {
            entityManager.registerEntityProducer(this, REFERENCE_ROOT);
            log.info("Registered kaltura entity producer as: "+ REFERENCE_ROOT);

            // get the main sakai AC (it will be the parent of our AC)
            ApplicationContext sakaiAC = applicationContext.getParent();
            if (sakaiAC != null && sakaiAC instanceof ConfigurableApplicationContext) {
                // only ConfigurableApplicationContext - or higher - can register singletons
                Object currentKEP = ComponentManager.get(SignupEntityProducer.class.getName());
                // check if something is already registered
                if (currentKEP != null) {
                    log.info("Found existing "+SignupEntityProducer.class.getName()+" in the ComponentManager: "+currentKEP);
                    // attempt to unregister the existing bean (otherwise the register call will fail)
                    try {
                        // only DefaultListableBeanFactory - or higher - can unregister singletons
                        DefaultListableBeanFactory dlbf = (DefaultListableBeanFactory) sakaiAC.getAutowireCapableBeanFactory();
                        dlbf.destroySingleton(SignupEntityProducer.class.getName());
                        log.info("Removed existing "+SignupEntityProducer.class.getName()+" from the ComponentManager");
                    } catch (Exception e) {
                        log.warn("FAILED attempted removal of signup bean: "+e);
                    }
                }
                // register this EP with the sakai AC
                ((ConfigurableApplicationContext)sakaiAC).getBeanFactory().registerSingleton(SignupEntityProducer.class.getName(), this);
            }
            // now verify if we are good to go
            if (ComponentManager.get(SignupEntityProducer.class.getName()) != null) {
                log.info("Found "+SignupEntityProducer.class.getName()+" in the ComponentManager");
            } else {
                log.warn("FAILED to insert and lookup "+SignupEntityProducer.class.getName()+" in the Sakai ComponentManager, archive imports for signup will not work");
            }
        } catch (Exception ex) {
            log.warn("signup EP.init(): "+ex, ex);
        }
        this.copyFileProcessor = new CopyFileProcessor(getSakaiFacade(), getSignupMeetingService());
    }
	
	//get current user
	public SakaiFacade getSakaiFacade() {
		return sakaiFacade;
	}

	public void setSakaiFacade(SakaiFacade sakaiFacade) {
		this.sakaiFacade = sakaiFacade;
	}

	public SignupMeetingService getSignupMeetingService() {
		return signupMeetingService;
	}

	public void setSignupMeetingService(SignupMeetingService signupMeetingService) {
		this.signupMeetingService = signupMeetingService;
	}

	private SignupMeetingService signupMeetingService;
	private CreateMeetings createMeetings;
	private CopyMeetingSignupMBean copyMeetingSignupMBean;
	private SignupMeeting signupMeeting;
	
	private String currentUserID = null;
	private boolean sendEmail = true;
	private boolean assignParticatpantsToFirstOne = false;
	private boolean assignParticitpantsToAllEvents = false;
	
	CreateMeetings createMeeting = new CreateMeetings(signupMeeting, sendEmail, assignParticatpantsToFirstOne, assignParticitpantsToAllEvents, sakaiFacade, signupMeetingService, currentUserID );

	public CopyMeetingSignupMBean getCopyMeetingSignupMBean() {
		return copyMeetingSignupMBean;
	}
	
	public void setCopyMeetingSignupMBean(CopyMeetingSignupMBean copyMeetingSignupMBean) {
		this.copyMeetingSignupMBean = copyMeetingSignupMBean;
	}

	public CreateMeetings getCreateMeetings() {
		return createMeetings;
	}
	
	public void setCreateMeetings(CreateMeetings createMeetings) {
		this.createMeetings = createMeetings;
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	@Override
	//this method is for merge data
	public void transferCopyEntities(String fromContext, String toContext, List ids) {
		
		String currentUserId = getSakaiFacade().getCurrentUserId();
		List<SignupMeeting> allMeetings = getSignupMeetingService().getAllSignupMeetings(fromContext, currentUserId);
		List<SignupMeeting> newMeetings = new ArrayList<SignupMeeting>();
		SignupMeeting copiedMeeting = new SignupMeeting();
		SignupMeeting meetingTemp = new SignupMeeting();
		ArrayList<Long> recurIDs = new ArrayList<Long>();
		int skip = 0; //for when recurring meetings are removed
		
		if (!allMeetings.isEmpty()){
			for(int i = 0; i < allMeetings.size(); i++){
				meetingTemp = allMeetings.get(i); //holds each meeting
				
				if(meetingTemp.getRecurrenceId() != null && !recurIDs.contains(meetingTemp.getRecurrenceId())){
					recurIDs.add(meetingTemp.getRecurrenceId()); //add recurID to the list
					copiedMeeting = createMeeting.prepareDeepCopy(meetingTemp, 0); //copies each meeting (meetingTemp)
					copiedMeeting.setCoordinatorIds(currentUserId); //changes organizer to currentUserId
					copiedMeeting.getSignupSites().get(0).setSiteId(toContext);
					copiedMeeting.setRepeatType(ONCE_ONLY);
					
					//copy attachments
					List<SignupAttachment> newOnes = new ArrayList<SignupAttachment>();
					List<SignupAttachment> olds = meetingTemp.getSignupAttachments(); 
					if (olds != null){
						for (SignupAttachment old : olds) {
							SignupAttachment newOne = this.copyFileProcessor.copySignupAttachment(meetingTemp, true, old,
									fromContext, toContext);
							newOnes.add(newOne);
						}
					}
					copiedMeeting.setSignupAttachments(newOnes);
					newMeetings.add((i - skip), copiedMeeting); //a list of copied signup meetings
				}
				else if(recurIDs.contains(meetingTemp.getRecurrenceId())){
					skip++;
				}
				else {
					copiedMeeting = createMeeting.prepareDeepCopy(meetingTemp, 0); //copies each meeting (meetingTemp)
					copiedMeeting.setCoordinatorIds(currentUserId); //changes organizer to currentUserId
					copiedMeeting.getSignupSites().get(0).setSiteId(toContext);
					copiedMeeting.setRepeatType(ONCE_ONLY);
					
					List<SignupAttachment> newOnes = new ArrayList<SignupAttachment>();
					List<SignupAttachment> olds = meetingTemp.getSignupAttachments(); 
					if (olds != null){ //copied attachments with correct siteId
						for (SignupAttachment old : olds) {
							SignupAttachment newOne = this.copyFileProcessor.copySignupAttachment(meetingTemp, true, old,
									fromContext, toContext);
							newOnes.add(newOne);
						}
					}
					copiedMeeting.setSignupAttachments(newOnes);
					newMeetings.add((i - skip), copiedMeeting); //a list of copied signup meetings
				}
			} //for end
			try {
				getSignupMeetingService().saveMeetings(newMeetings, currentUserId);
			} catch (PermissionException e) {
				log.warn("permission issue:" + e.getMessage());
			}
		} //if end
	}

	@Override
	public String[] myToolIds() {
		 return new String[] {"sakai.signup"};
	}

	@Override
	//this method is for replace data
	public void transferCopyEntities(String fromContext, String toContext, List ids, boolean cleanup) {
		// TODO Auto-generated method stub
		String currentUserId = getSakaiFacade().getCurrentUserId();
		List<SignupMeeting> oldMeetings = getSignupMeetingService().getAllSignupMeetings(toContext, currentUserId);

		//removes meetings before adding new ones
		try {
			getSignupMeetingService().removeMeetings(oldMeetings);
		} catch (Exception e1) {
			log.warn("remove oldmeeting error:" + e1.getMessage());
		}
		transferCopyEntities(fromContext, toContext, ids);
	}

	@Override
	public String getLabel() {
		return SIGNUP;
	}

	@Override
	public boolean willArchiveMerge() {
		return DEFAULT_ARCHIVE_SUPPORT;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments) {
		
		String currentUserId = getSakaiFacade().getCurrentUserId();
        StringBuilder results = new StringBuilder();
 
        results.append("archiving " + getLabel() + Entity.SEPARATOR + siteId
                + Entity.SEPARATOR + SiteService.MAIN_CONTAINER + ".\n");

        Element rootElement = doc.createElement(SignupMeetingService.class.getName());
        ((Element) stack.peek()).appendChild(rootElement); //<org.sakaiproject>
        stack.push(rootElement);
        
        List<SignupMeeting> allMeetings = getSignupMeetingService().getAllSignupMeetings(siteId, currentUserId);
        
        if(allMeetings.size() > 0){
	        Element meetingListElement = this.copyFileProcessor.toXml("meetingList", doc, stack); //<meetingList>
	        
	        //adds meetings
	        for(int i = 0; allMeetings.size() > i; i++) {
	            try {
	                SignupMeeting meeting = allMeetings.get(i);
	                
	                Element meetingElement = this.copyFileProcessor.toXml("meeting", doc, stack); //<meeting>
	                Element titleElement = this.copyFileProcessor.toXml("title", doc, stack); //<title>
	                Element locElement = this.copyFileProcessor.toXml("location", doc, stack); //<location>
	                Element descElement = this.copyFileProcessor.toXml("description", doc, stack); //<description>
	                Element meetingTypeElement = this.copyFileProcessor.toXml("meetingType", doc, stack); //<meetingType>
	                Element creatorIdElement = this.copyFileProcessor.toXml("creatorId", doc, stack); //<creatorId>
	                
	                titleElement.appendChild(doc.createTextNode(meeting.getTitle())); //title
	                locElement.appendChild(doc.createTextNode(meeting.getLocation())); //location
	                descElement.appendChild(doc.createTextNode(meeting.getDescription())); //description
	                meetingTypeElement.appendChild(doc.createTextNode(meeting.getMeetingType())); //meetingType
	                creatorIdElement.appendChild(doc.createTextNode(meeting.getCreatorUserId())); //creatorId
	                
	                meetingElement.appendChild(titleElement);
	                meetingElement.appendChild(locElement);
	                meetingElement.appendChild(descElement);
	                meetingElement.appendChild(meetingTypeElement);
	                meetingElement.appendChild(creatorIdElement);
	                
	                if(meeting.isRecurredMeeting()){
	                	Element recurElement = this.copyFileProcessor.toXml("recurrenceType", doc, stack); //<recurrenceType>
	                	recurElement.appendChild(doc.createTextNode(meeting.getRepeatType())); //recurrence
	                	meetingElement.appendChild(recurElement);
	                }
	                
	                Element timeslotListElement = this.copyFileProcessor.toXml("timeslotList", doc, stack); //<timeslotList>
	                meetingElement.appendChild(timeslotListElement);
	                
	                List<SignupTimeslot> timeslots = meeting.getSignupTimeSlots(); //get the timeslots
	                
	                //adds timeslots to timeslotList
	                for (int j = 0; j < timeslots.size(); j++) {
	                    SignupTimeslot timeslot = timeslots.get(j);
	                    List<SignupAttendee> attendees = timeslot.getAttendees();
	                    
	                    Element timeslotElement = CopyFileProcessor.timeslotToXml(timeslot, doc, stack); //<timeslot>
	                    timeslotListElement.appendChild(timeslotElement);
	                    
	                    if(attendees.size() > 0){
	                    	Element attendeeListElement = this.copyFileProcessor.toXml("attendeeList", doc, stack); //<attendeeList>
	                    	timeslotElement.appendChild(attendeeListElement);
	                    
	                    	//adds attendees and attendeeIds
		                    for (int q = 0; q < attendees.size(); q++){
		                    	SignupAttendee attendee = (SignupAttendee) attendees.get(q);
		                    	Element attendeeElement = this.copyFileProcessor.toXml("attendee", doc, stack); //<attendee>
		                    	Element attendeeIdElement = this.copyFileProcessor.toXml("attendeeId", doc, stack); //<attendeeId>
		                        Element attendeeSiteIdElement = this.copyFileProcessor.toXml("attendeeSiteId", doc, stack); //<attendeeSiteId>
		                        
		                        attendeeIdElement.appendChild(doc.createTextNode(attendee.getAttendeeUserId()));
		                        attendeeSiteIdElement.appendChild(doc.createTextNode(attendee.getSignupSiteId()));
		                        attendeeElement.appendChild(attendeeIdElement);
		                        attendeeElement.appendChild(attendeeSiteIdElement);
		                        attendeeListElement.appendChild(attendeeElement);
		                    } //attendee loop end
	                    } //if any attendee end
	                } //timeslot loop end
	                
	                //if there are any attachments
	                if(meeting.hasSignupAttachments()){
	                	Element attachmentListElement = this.copyFileProcessor.toXml("attachmentList", doc, stack); //<attachmentList>
	                	List<SignupAttachment> allAttachments = meeting.getSignupAttachments();
	                	meetingElement.appendChild(attachmentListElement);
	                	
	                	//adds attachments
	                	for(int m = 0; m < allAttachments.size(); m++){
	                		SignupAttachment attachment = allAttachments.get(m);
	                		
	                		Element attachmentElement = this.copyFileProcessor.toXml("attachment", doc, stack); //<attachment>
		                	Element attachmentUrlElement = this.copyFileProcessor.toXml("attachmentUrl", doc, stack); //<attachmentUrl>
		                	Element attachmentName = this.copyFileProcessor.toXml("attachmentName", doc, stack); //<attachmentName>
		                	attachmentUrlElement.appendChild(doc.createTextNode(attachment.getResourceId()));
		                	attachmentName.appendChild(doc.createTextNode(attachment.getFilename()));
		                	attachmentElement.appendChild(attachmentUrlElement);
		                	attachmentElement.appendChild(attachmentName);
		                	attachmentListElement.appendChild(attachmentElement);
	                	}
	                }
	                
	                List<SignupSite> allSitesInMeeting = meeting.getSignupSites();
	                
	                Element availableToElement = this.copyFileProcessor.toXml("availableTo", doc, stack); //<availableTo>
	                Element siteListElement = this.copyFileProcessor.toXml("siteList", doc, stack); //<siteList>
	                availableToElement.appendChild(siteListElement);
	                meetingElement.appendChild(availableToElement);
	                
	                for(int n = 0; n < allSitesInMeeting.size(); n++){
	                	SignupSite site = allSitesInMeeting.get(n);
	                	
	                	Element siteElement = this.copyFileProcessor.toXml("site", doc, stack); //<site>
	                	Element siteIdElement = this.copyFileProcessor.toXml("siteId", doc, stack); //<siteId>
	                	siteIdElement.appendChild(doc.createTextNode(site.getSiteId()));
	                	siteElement.appendChild(siteIdElement);
	                	siteListElement.appendChild(siteElement);
	                	
	                	//if there are groups
	                	if(site.getSignupGroups().size() > 0){
	                		List<SignupGroup> allGroupsInSite = site.getSignupGroups();
	                		Element groupListElement = this.copyFileProcessor.toXml("groupList", doc, stack); //<groupList>
	                		siteElement.appendChild(groupListElement);
	                		
	                		//adds groups
	                		for(int g = 0; g < allGroupsInSite.size(); g++){
	                			SignupGroup group = allGroupsInSite.get(g);
	                			
	                			Element groupElement = this.copyFileProcessor.toXml("group", doc, stack); //<group>
	                        	Element groupIdElement = this.copyFileProcessor.toXml("groupId", doc, stack); //<groupId>
	                        	
	                        	groupIdElement.appendChild(doc.createTextNode(group.getGroupId()));
	                        	groupElement.appendChild(groupIdElement);
	                        	groupListElement.appendChild(groupElement);
	                		}
	                	} //signupGroups if end
	                } //allSites for-loop end
	                
	                //add meetings to root
	                meetingListElement.appendChild(meetingElement);
	                rootElement.appendChild(meetingListElement);
	                
	            } catch (Exception e) {
	                log.warn(e.getMessage());
	            }
	        } //main for-loop end
        }
        stack.pop();
        return results.toString();
    } //archive end

	@Override
	public String merge(String siteId, Element root, String archivePath,
			String fromSiteId, Map attachmentNames, Map userIdTrans,
			Set userListAllowImport) {
		log.warn("Currently, Sign-up tool site archive will not support merging functionality from archive data into a site");
		return null;
	}

	@Override
	public boolean parseEntityReference(String reference, Reference ref) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getEntityDescription(Reference ref) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResourceProperties getEntityResourceProperties(Reference ref) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entity getEntity(Reference ref) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getEntityUrl(Reference ref) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection getEntityAuthzGroups(Reference ref, String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpAccess getHttpAccess() {
		// TODO Auto-generated method stub
		return null;
	}

}
