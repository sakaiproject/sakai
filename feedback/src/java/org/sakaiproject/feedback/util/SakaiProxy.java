/********************************************************************************** 
 * $URL$ 
 * $Id$ 
 *********************************************************************************** 
 * 
 * Copyright (c) 2011 The Sakai Foundation 
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.osedu.org/licenses/ECL-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **********************************************************************************/ 

package org.sakaiproject.feedback.util;

import java.util.*;
import java.text.MessageFormat;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.mail.util.ByteArrayDataSource;

import org.sakaiproject.api.privacy.PrivacyManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.Attachment;
import org.sakaiproject.email.api.ContentType;
import org.sakaiproject.email.api.EmailAddress;
import org.sakaiproject.email.api.EmailAddress.RecipientType;
import org.sakaiproject.email.api.EmailMessage;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import lombok.Setter;
import org.sakaiproject.util.ResourceLoader;

public class SakaiProxy {

    public static final int ATTACH_MAX_DEFAULT = 10;

	private static final Log logger = LogFactory.getLog(SakaiProxy.class);

    private static ResourceLoader rb = new ResourceLoader("org.sakaiproject.feedback");

    @Setter
    private ServerConfigurationService serverConfigurationService;

    @Setter
    private SessionManager sessionManager;

    @Setter
    private ToolManager toolManager;

    @Setter
    private UserDirectoryService userDirectoryService;

    @Setter
    private SiteService siteService;

    @Setter
    private EmailService emailService;

    @Setter
    private PrivacyManager privacyManager;


    public boolean getConfigBoolean(String name, boolean defaultValue) {
        return serverConfigurationService.getBoolean(name, defaultValue);
    }

    public String getConfigString(String name, String defaultValue) {
        return serverConfigurationService.getString(name, defaultValue);
    }

    public int getConfigInt(String name, int defaultValue) {
        return serverConfigurationService.getInt(name, defaultValue);
    }

    public String getCurrentUserId() {
        return sessionManager.getCurrentSessionUserId();
    }

    public User getUser(String userId) {

        try {
            return userDirectoryService.getUser(userId);
        } catch (UserNotDefinedException unde) {
            logger.error("No user with id: " + userId + ". Returning null ...");
            return null;
        }
    }

    /**
     * This gets the current Site ID from the placement, if the tool is running outside the portal
     * this will return <code>null</code>.
     * @return The current site ID.
     */
	public String getCurrentSiteId() {
		return toolManager.getCurrentPlacement().getContext();
	}

    private Site getSite(String siteId) {

        try {
            return siteService.getSite(siteId);
        } catch (Exception e) {
            logger.error("Failed to get site for id : " + siteId + ". Returning null ...");
        }

        return null;
    }

    public String getSiteProperty(String siteId, String name) {

        try {
            Site site = siteService.getSite(siteId);
            return site.getProperties().getProperty(name);
        } catch (Exception e) {
            logger.error("Failed to get property '" + name + "' for site : " + siteId + ". Returning null ...");
        }

        return null;
    }

    /**
     *  Returns a map of display name onto user id for each user, in the
     *  specified site, with site.upd. If a user doesn't have an email specced,
     *  they aren't returned.
     *
     *  @param siteId The site to retrieve updaters for
     *  @return A map of display name onto user id
     */
    public Map<String, String> getSiteUpdaters(String siteId) {

        try {
            Site site = siteService.getSite(siteId);
            Map<String, String> map = new HashMap<String, String>();
            Set<String> userIds = site.getUsersIsAllowed(SiteService.SECURE_UPDATE_SITE);
            Set<String> hiddenUserIds = privacyManager.findHidden("/site/" + siteId, userIds);
            userIds.removeAll(hiddenUserIds);
            List<User> users = userDirectoryService.getUsers(userIds);
            for (User user : users) {
                String email = user.getEmail();
                if (email != null && email.length() > 0) {
                    map.put(user.getId(), user.getDisplayName());
                }
            }
            return map;
        } catch (Exception e) {
            logger.error("Failed to get site updaters for site : " + siteId + ". Returning an empty map ...");
            return new HashMap<String, String>();
        }
    }

	public void sendEmail(String fromUserId, String senderAddress, String toAddress, boolean addNoContactEmailMessage, String siteId, String feedbackType
			, String userTitle, String userContent
			, List<FileItem> fileItems, boolean siteExists,
			   String browserNameAndVersion, String osNameAndVersion, String browserSize, String screenSize, String plugins, String ip,
			   String currentTime) {

		final List<Attachment> attachments = new ArrayList<Attachment>();

		if (fileItems.size() > 0) {
			for (FileItem fileItem : fileItems) {
				String name = fileItem.getName();

				if (name.contains("/")) {
					name = name.substring(name.lastIndexOf("/") + 1);
                } else if (name.contains("\\")) {
					name = name.substring(name.lastIndexOf("\\") + 1);
                }

				attachments.add(new Attachment(new ByteArrayDataSource(fileItem.get(), fileItem.getContentType()), name));
			}
		}

        String fromAddress = senderAddress;
        String fromName = "User not logged in";
        String userId = "";
        String userEid = "";

        if (fromUserId != null) {
            User user = getUser(fromUserId);
            fromAddress = user.getEmail();
            fromName = user.getDisplayName();
            userId = user.getId();
            userEid = user.getEid();
        }

        if (fromAddress == null) {
            logger.error("No email for reporter: " + fromUserId + ". No email will be sent.");
            return;
        }

        final String siteLocale = getSiteProperty(siteId, "locale_string");

        Locale locale = null;

        if (siteLocale != null ) {

            String[] localeParts = siteLocale.split("_");

            if (localeParts.length == 1) {
                locale = new Locale(localeParts[0]);
            } else if (localeParts.length == 2) {
                locale = new Locale(localeParts[0], localeParts[1]);
            } else {
                locale = Locale.getDefault();
            }
        } else {
            locale = Locale.getDefault();
        }

        final ResourceLoader rb = new ResourceLoader("org.sakaiproject.feedback");

        String subjectTemplate = null;
        
        if (feedbackType.equals(Constants.CONTENT)) {
            subjectTemplate = rb.getString("content_email_subject_template");
        } else if (feedbackType.equals(Constants.HELPDESK)) {
            subjectTemplate = rb.getString("help_email_subject_template");
        } else {
            subjectTemplate = rb.getString("technical_email_subject_template");
        }

        final String formattedSubject
            = MessageFormat.format(subjectTemplate, new String[] {fromName});

        final Site site = getSite(siteId);

        final String siteTitle = siteExists ? site.getTitle() : "N/A (Site does not exist)";

        final String workerNode = serverConfigurationService.getServerId();
        final String siteUrl = serverConfigurationService.getPortalUrl() + "/site/" + (siteExists ? site.getId() : siteId) ;

        String noContactEmailMessage = "";
        
        final String instance = serverConfigurationService.getServerIdInstance();

        final String bodyTemplate = rb.getString("email_body_template");
        String formattedBody
            = MessageFormat.format(bodyTemplate, new String[]{noContactEmailMessage,
		        userId,
		        userEid,
		        fromName,
		        fromAddress,
		        siteTitle,
		        siteId,
		        siteUrl,
		        instance,
		        userTitle,
		        userContent,
		        "\n",
		        browserNameAndVersion,
		        osNameAndVersion,
		        browserSize,
		        screenSize,
		        plugins,
		        ip,
		        workerNode,
		        currentTime});


        if (feedbackType.equals(Constants.CONTENT)) {
            formattedBody = formattedBody + "\n\n\n" + rb.getString("email_body_template_note");
        }


        if (logger.isDebugEnabled()) {
            logger.debug("fromName: " + fromName);
            logger.debug("fromAddress: " + fromAddress);
            logger.debug("toAddress: " + toAddress);
            logger.debug("userContent: " + userContent);
            logger.debug("userTitle: " + userTitle);
            logger.debug("subjectTemplate: " + subjectTemplate);
            logger.debug("bodyTemplate: " + bodyTemplate);
            logger.debug("formattedSubject: " + formattedSubject);
            logger.debug("formattedBody: " + formattedBody);
        }

		final EmailMessage msg = new EmailMessage();

		msg.setFrom(new EmailAddress(fromAddress, fromName));
		msg.setContentType(ContentType.TEXT_PLAIN);

		msg.setSubject(formattedSubject);
		msg.setBody(formattedBody);

		if (attachments != null) {
			for (Attachment attachment : attachments) {
				msg.addAttachment(attachment);
			}
		}

		// Copy the sender in
		msg.addRecipient(RecipientType.CC, fromName, fromAddress);

		msg.addRecipient(RecipientType.TO, toAddress);

		new Thread(new Runnable() {
			public void run() {
		        try {
			        emailService.send(msg, true);
                } catch (Exception e) {
                    logger.error("Failed to send email.", e);
                }
            }
        }, "Feedback Email Thread").start();
	}

    public int getAttachmentLimit() {

        // set the limit to whichever is the lowest.
        int contentUploadMax = getConfigInt("content.upload.max", ATTACH_MAX_DEFAULT);
        int feedbackAttachMax= getConfigInt("feedback.attach.max", ATTACH_MAX_DEFAULT);
        int mb = (contentUploadMax < feedbackAttachMax) ? contentUploadMax : feedbackAttachMax;

        return mb;
    }

    public Locale getLocale() {
        return rb.getLocale();
    }
}
