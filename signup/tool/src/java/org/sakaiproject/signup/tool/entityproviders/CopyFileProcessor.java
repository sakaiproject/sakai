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

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.logic.SignupMeetingService;
import org.sakaiproject.signup.model.SignupAttachment;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.tool.jsf.attachment.AttachmentHandler;
import org.sakaiproject.signup.tool.jsf.attachment.RemoveAttachment;

@Slf4j
public class CopyFileProcessor extends AttachmentHandler{

	public CopyFileProcessor(SakaiFacade sakaiFacade, SignupMeetingService signupMeetingService){
		setSakaiFacade(sakaiFacade);
		setSignupMeetingService(signupMeetingService);
	}
	
	public String processAddAttachRedirect(List attachList, SignupMeeting sMeeting,
			boolean isOrganizer) {
		//overwrites original method. unavailable
		return null;
	}
	public SignupAttachment copySignupAttachment(SignupMeeting sMeeting, boolean isOrganizer,
			SignupAttachment attach, String fromContext, String toContext) {
		SignupAttachment newAttach = null;
		ContentResource cr = null;
		ContentResource newCr = null;
		if (attach == null || attach.getResourceId().trim().length() < 1)
			return null;

		String newResourceId = attach.getResourceId();
		if(newResourceId != null){
			newResourceId = newResourceId.replaceAll(fromContext, toContext);
		}
		try {
			cr = getSakaiFacade().getContentHostingService().getResource(attach.getResourceId());
			if (cr != null) {
				String protocol = getSakaiFacade().getServerConfigurationService().getServerUrl();
				newResourceId = getSakaiFacade().getContentHostingService().copy(
						attach.getResourceId(), newResourceId);
				newCr = getSakaiFacade().getContentHostingService().getResource(newResourceId);
				Reference ref = EntityManager.newReference(newCr.getReference());
				if (ref != null) {
					newAttach = createSignupAttachment(ref.getId(), ref.getProperties()
							.getProperty(ref.getProperties().getNamePropDisplayName()), protocol);

					/* Case: for cross-sites, make it to public view */
					determineAndAssignPublicView(sMeeting, newAttach);
				}
			}
		} catch (PermissionException e) {
			log.warn("ContentHostingService.getResource() throws PermissionException="
					+ e.getMessage());
		} catch (IdUnusedException e) {
			log.warn("ContentHostingService.getResource() throws IdUnusedException="
					+ e.getMessage());
			/*
			 * If the attachment somehow get removed from CHS and it's a broken
			 * link
			 */
			RemoveAttachment removeAttach = new RemoveAttachment(getSignupMeetingService(), getSakaiFacade()
					.getCurrentUserId(), getSakaiFacade().getCurrentLocationId(), isOrganizer);
			removeAttach.removeAttachment(sMeeting, attach);
		} catch (TypeException e) {
			log.warn("ContentHostingService.getResource() throws TypeException=" + e.getMessage());
		} catch (Exception e) {
			log.warn("ContentHostingService.getResource() throws Exception=" + e.getMessage());
		}

		return newAttach;
	}
	
	public Element toXml(String element, Document doc, Stack stack) {
        Element ele = doc.createElement(element);

        if (stack.isEmpty()){
            doc.appendChild(ele);
        }
        else{
            ((Element) stack.peek()).appendChild(ele);
        }

        stack.push(ele);
        stack.pop();

        return ele;
    }
	
	public static Element timeslotToXml(SignupTimeslot timeslot, Document doc, Stack<Element> stack) {
        Element element = doc.createElement("timeslot");

        if (stack.isEmpty()){
            doc.appendChild(element);
        }
        else {
            ((Element) stack.peek()).appendChild(element);
        }

        stack.push(element);

        Date startTime = timeslot.getStartTime();
        Date endTime = timeslot.getEndTime();
        Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strStartTime = formatter.format(startTime);
        String strEndTime = formatter.format(endTime);
        
        element.setAttribute("endTime", strEndTime);
        element.setAttribute("startTime", strStartTime);
        
        stack.pop();

        return element;
    }
}
	
