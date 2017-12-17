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

package org.sakaiproject.signup.tool.jsf.attachment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.Role;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.logic.SignupMeetingService;
import org.sakaiproject.signup.model.SignupAttachment;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupSite;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;

/*
 * <p> This class will provides add/remove attachment functionality for sign-up tool.
 * </P>
 */
@Slf4j
public class AttachmentHandler implements Serializable {

	private List<SignupAttachment> attachments;

	private SakaiFacade sakaiFacade;

	private SignupMeetingService signupMeetingService;

	/**
	 * default constructor
	 */
	public AttachmentHandler() {
	}

	/**
	 * Constructor
	 * 
	 * @param sakaiFacade
	 *            -SakaiFacade object
	 * @param signupMeetingService
	 *            -SignupMeetingService object
	 */
	public AttachmentHandler(SakaiFacade sakaiFacade, SignupMeetingService signupMeetingService) {
		this.sakaiFacade = sakaiFacade;
		this.signupMeetingService = signupMeetingService;
	}

	public void clear() {
		this.attachments = null;
	}

	/**
	 * Redirect the add/remove attachment to Sakai's help page.
	 * 
	 * @param attachList -
	 *            a list of attachment objects
	 * @param sMeeting -
	 *            SignupMeeting object
	 * @param isOrganizer -
	 *            a boolean value
	 * @return null value
	 */
	public String processAddAttachRedirect(List attachList, SignupMeeting sMeeting,
			boolean isOrganizer) {
		this.attachments = attachList;
		try {
			List filePickerList = prepareReferenceList(attachments, sMeeting, isOrganizer);
			ToolSession currentToolSession = SessionManager.getCurrentToolSession();
			currentToolSession.setAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS,
					filePickerList);
			ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
			context.redirect("sakai.filepicker.helper/tool");
		} catch (Exception e) {
			log.error("fail to redirect to attachment page: " + e.getMessage());
		}
		return null;
	}

	private List prepareReferenceList(List attachmentList, SignupMeeting sMeeting,
			boolean isOrganizer) {
		List list = new ArrayList();
		if (attachmentList == null) {
			return list;
		}
		for (int i = 0; i < attachmentList.size(); i++) {
			ContentResource cr = null;
			SignupAttachment attach = (SignupAttachment) attachmentList.get(i);
			try {
				cr = getSakaiFacade().getContentHostingService()
						.getResource(attach.getResourceId());
			} catch (PermissionException e) {
				log.warn("ContentHostingService.getResource() throws PermissionException="
						+ e.getMessage());
			} catch (IdUnusedException e) {
				log.warn("ContentHostingService.getResource() throws IdUnusedException="
						+ e.getMessage());
				/*
				 * If the attachment somehow get removed from CHS and it's a
				 * broken link
				 */
				RemoveAttachment removeAttach = new RemoveAttachment(signupMeetingService,
						sakaiFacade.getCurrentUserId(), sakaiFacade.getCurrentLocationId(),
						isOrganizer);
				removeAttach.removeAttachment(sMeeting, attach);
			} catch (TypeException e) {
				log.warn("ContentHostingService.getResource() throws TypeException="
						+ e.getMessage());
			} catch (Exception e) {
				log.warn("Exception: " + e.getMessage());
			}
			if (cr != null) {
				Reference ref = EntityManager.newReference(cr.getReference());
				if (ref != null)
					list.add(ref);
			}
		}
		return list;
	}

	/**
	 * Called by SamigoJsfTool.java on exit from file picker
	 */
	public void setAttachmentItems() {
		/*
		 * they share the same attachments pointer with JSF bean and via this to
		 * pass updated list
		 */
		processItemAttachment();
	}

	private void processItemAttachment() {
		ToolSession session = SessionManager.getCurrentToolSession();
		if (session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS) != null) {

			HashMap map = getResourceIdHash(this.attachments);
			ArrayList newAttachmentList = new ArrayList();

			String protocol = getSakaiFacade().getServerConfigurationService().getServerUrl();

			List refs = (List) session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
			if (refs != null && refs.size() > 0) {
				Reference ref;

				for (int i = 0; i < refs.size(); i++) {
					ref = (Reference) refs.get(i);
					String resourceId = ref.getId();
					if (map.get(resourceId) == null) {
						// new attachment, add
						SignupAttachment newAttach = createSignupAttachment(ref.getId(), ref
								.getProperties().getProperty(
										ref.getProperties().getNamePropDisplayName()), protocol);
						newAttachmentList.add(newAttach);
					} else {
						// attachment already exist, let's add it to new list
						// and
						// check it off from map
						newAttachmentList.add((SignupAttachment) map.get(resourceId));
						map.remove(resourceId);
					}
				}
			}

			session.removeAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
			session.removeAttribute(FilePickerHelper.FILE_PICKER_CANCEL);
			this.attachments.clear();
			this.attachments.addAll(newAttachmentList);
		}

	}

	private HashMap<String, SignupAttachment> getResourceIdHash(List<SignupAttachment> attachList) {
		HashMap<String, SignupAttachment> map = new HashMap<String, SignupAttachment>();
		if (attachList != null) {
			for (SignupAttachment attach : attachList) {
				map.put(attach.getResourceId(), attach);
			}
		}
		return map;
	}

	/**
	 * Create a new copy of the attachment
	 * 
	 * @param sMeeting
	 *            -SignupMeeting object
	 * @param isOrganizer -
	 *            a boolean value
	 * @param attach
	 *            -SignupAttachment object
	 * @param folderId -
	 *            a foldId string object
	 * @return - a SignupAttachment object
	 */
	public SignupAttachment copySignupAttachment(SignupMeeting sMeeting, boolean isOrganizer,
			SignupAttachment attach, String folderId) {
		SignupAttachment newAttach = null;
		ContentResource cr = null;
		ContentResource newCr = null;
		if (attach == null || attach.getResourceId().trim().length() < 1)
			return null;

		String newResourceId = attach.getResourceId();
		int index = attach.getResourceId().lastIndexOf("/");
		if (index > -1) {
			newResourceId = newResourceId.substring(0, index + 1) + folderId + "/"
					+ newResourceId.substring(index + 1, newResourceId.length());
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
			RemoveAttachment removeAttach = new RemoveAttachment(signupMeetingService, sakaiFacade
					.getCurrentUserId(), sakaiFacade.getCurrentLocationId(), isOrganizer);
			removeAttach.removeAttachment(sMeeting, attach);
		} catch (TypeException e) {
			log.warn("ContentHostingService.getResource() throws TypeException=" + e.getMessage());
		} catch (Exception e) {
			log.warn("ContentHostingService.getResource() throws Exception=" + e.getMessage());
		}

		return newAttach;
	}

	public void removeAttachmentInContentHost(SignupAttachment attach) {
		if (attach == null || attach.getResourceId() == null)
			return;

		try {
			getSakaiFacade().getContentHostingService().removeResource(attach.getResourceId());
		} catch (PermissionException e) {
			log.warn("ContentHostingService.getResource() throws PermissionException="
					+ e.getMessage());
		} catch (IdUnusedException e) {
			log.warn("ContentHostingService.getResource() throws IdUnusedException="
					+ e.getMessage());
		} catch (TypeException e) {
			log.warn("ContentHostingService.getResource() throws TypeException=" + e.getMessage());
		} catch (InUseException e) {
			log.warn("ContentHostingService.getResource() throws InUseException=" + e.getMessage());
		} catch (Exception e) {
			log.warn("ContentHostingService.getResource() throws Exception=" + e.getMessage());
		}
	}

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

	public List<SignupAttachment> getAttachments() {
		return this.attachments;
	}

	public void setAttachments(List<SignupAttachment> attachments) {
		this.attachments = attachments;
	}

	protected SignupAttachment createSignupAttachment(String resourceId, String filename,
			String protocol) {
		SignupAttachment attach = new SignupAttachment();

		Boolean isLink = Boolean.FALSE;
		try {
			ContentResource cr = getSakaiFacade().getContentHostingService()
					.getResource(resourceId);
			if (cr != null) {
				ResourceProperties p = cr.getProperties();

				attach = new SignupAttachment();

				attach.setResourceId(resourceId);
				attach.setMimeType(cr.getContentType());
				// we want to display kb, so divide by 1000 and round the result
				attach.setFileSize(new Long("" + fileSizeInKB((int)cr.getContentLength())));//2Gb??
				if (cr.getContentType().lastIndexOf("url") > -1) {
					isLink = Boolean.TRUE;
					if (!filename.toLowerCase().startsWith("http")) {
						String adjustedFilename = "http://" + filename;
						attach.setFilename(adjustedFilename);
					} else {
						attach.setFilename(filename);
					}
				} else {
					attach.setFilename(filename);
				}
				attach.setIsLink(isLink);
				attach.setCreatedBy(p.getProperty(p.getNamePropCreator()));
				attach.setCreatedDate(new Date());
				attach.setLastModifiedBy(p.getProperty(p.getNamePropModifiedBy()));
				attach.setLastModifiedDate(new Date());
				attach.setLocation(getRelativePath(cr.getUrl(), protocol));
			}
		} catch (PermissionException pe) {
			log.warn("PermissionException: " + pe.getMessage());
		} catch (IdUnusedException ie) {
			log.warn("IdUnusedException: " + ie.getMessage());
		} catch (TypeException te) {
			log.warn("TypeException: " + te.getMessage());
		}

		return attach;

	}

	private String fileSizeInKB(int fileSize) {
		String fileSizeString = "1";
		int size = Math.round((float) fileSize / 1024.0f);
		if (size > 0) {
			fileSizeString = size + "";
		}
		return fileSizeString;
	}

	public String getRelativePath(String url, String protocol) {
		// replace whitespace with %20
		url = replaceSpace(url);
		int index = url.lastIndexOf(protocol);
		if (index == 0) {
			url = url.substring(protocol.length());
		}
		return url;
	}

	public void setPublicView(String resourceId, boolean pubview) {
		getSakaiFacade().getContentHostingService().setPubView(resourceId, pubview);
	}

	private String replaceSpace(String tempString) {
		String newString = "";
		char[] oneChar = new char[1];
		for (int i = 0; i < tempString.length(); i++) {
			if (tempString.charAt(i) != ' ') {
				oneChar[0] = tempString.charAt(i);
				String concatString = new String(oneChar);
				newString = newString.concat(concatString);
			} else {
				newString = newString.concat("%20");
			}
		}
		return newString;
	}

	/**
	 * If it's a cross sites or for other site, the attachment will be set to
	 * public view in ContentHostingService
	 * 
	 * @param sMeeting -
	 *            a SignupMeeting object
	 * @param attach -
	 *            a SignupAttachment object
	 */
	public void determineAndAssignPublicView(SignupMeeting sMeeting, SignupAttachment attach) {
		if (attach == null)
			return;

		/* Case 1: multiple sites - set to public view */
		if (sMeeting.getSignupSites() != null && sMeeting.getSignupSites().size() > 1) {
			getSakaiFacade().getContentHostingService().setPubView(attach.getResourceId(), true);
			return;
		}

		Site site = null;
		if (sMeeting.getSignupSites() == null || sMeeting.getSignupSites().isEmpty())
			return;

		SignupSite signupSite = sMeeting.getSignupSites().get(0);
		try {
			site = getSakaiFacade().getSiteService().getSite(signupSite.getSiteId());
		} catch (IdUnusedException e) {
			log.error(e.getMessage(), e);
		}

		if (site == null)
			return;

		/* Case 2: publish the event for other site only - set to public view */
		if (!site.getId().equals(sakaiFacade.getCurrentLocationId())) {
			getSakaiFacade().getContentHostingService().setPubView(attach.getResourceId(), true);
			return;
		}

		/*
		 * case 3: If site has roleId '.auth', any logged-in user should see it -
		 * set to public view
		 */
		Set siteRoles = site.getRoles();
		if (siteRoles != null) {
			for (Iterator iter = siteRoles.iterator(); iter.hasNext();) {
				Role role = (Role) iter.next();
				/* '.auth' roleId */
				if (SakaiFacade.REALM_ID_FOR_LOGIN_REQUIRED_ONLY.equals(role.getId())) {
					getSakaiFacade().getContentHostingService().setPubView(attach.getResourceId(),
							true);
					break;
				}
			}
		}
	}

}
