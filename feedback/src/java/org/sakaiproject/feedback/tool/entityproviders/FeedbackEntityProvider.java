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

package org.sakaiproject.feedback.tool.entityproviders;

import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import lombok.extern.slf4j.Slf4j;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaFactory;
import net.tanesha.recaptcha.ReCaptchaResponse;

import org.apache.commons.fileupload.FileItem;

import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestAware;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.feedback.db.Database;
import org.sakaiproject.feedback.exception.AttachmentsTooBigException;
import org.sakaiproject.feedback.tool.FeedbackTool;
import org.sakaiproject.feedback.util.Constants;
import org.sakaiproject.feedback.util.SakaiProxy;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.RequestFilter;

@Slf4j
public class FeedbackEntityProvider extends AbstractEntityProvider implements AutoRegisterEntityProvider, Outputable, Describeable, ActionsExecutable, RequestAware {
	
	public final static String ENTITY_PREFIX = "feedback";

    // Error codes start
    private final static String ATTACHMENTS_TOO_BIG = "ATTACHMENTS_TOO_BIG";
    private final static String BAD_DESCRIPTION = "BAD_DESCRIPTION";
    private final static String BAD_RECIPIENT = "BAD_RECIPIENT";
    private final static String BADLY_FORMED_RECIPIENT = "BADLY_FORMED_RECIPIENT";
    private final static String BAD_REQUEST = "BAD_REQUEST";
    private final static String BAD_TITLE = "BAD_TITLE";
    private final static String ERROR = "ERROR";
    private final static String NO_SENDER_ADDRESS = "NO_SENDER_ADDRESS";
    private final static String RECAPTCHA_FAILURE = "RECAPTCHA_FAILURE";
    private final static String SUCCESS = "SUCCESS";
    // Error codes end

    private SakaiProxy sakaiProxy = null;
    public void setSakaiProxy(SakaiProxy sakaiProxy) {
        this.sakaiProxy = sakaiProxy;
    }

    private Database db = null;
    public void setDb(Database db) {
        this.db = db;
    }

    private RequestGetter requestGetter = null;

    private long maxAttachmentsBytes = 0L;

    public void init() {

        maxAttachmentsBytes = sakaiProxy.getAttachmentLimit() * 1024 * 1024;

        if (log.isDebugEnabled()) {
            log.debug("maxAttachmentsBytes = " + maxAttachmentsBytes);
        }
    }

	public Object getSampleEntity() {
		return null;
	}

	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	public String[] getHandledOutputFormats() {
		return new String[] { Formats.JSON, Formats.HTML };
	}

    public void setRequestGetter(RequestGetter rg) {
        this.requestGetter = rg;
    }

	@EntityCustomAction(action = "reportcontent", viewKey = EntityView.VIEW_EDIT)
	public String handleContentReport(EntityView view, Map<String, Object> params) {
        return handleReport(view, params, Constants.CONTENT);
	}

	@EntityCustomAction(action = "reporttechnical", viewKey = EntityView.VIEW_EDIT)
	public String handleTechnicalReport(EntityView view, Map<String, Object> params) {
        return handleReport(view, params, Constants.TECHNICAL);
	}

	@EntityCustomAction(action = "reporthelpdesk", viewKey = EntityView.VIEW_EDIT)
	public String handleHelpReport(EntityView view, Map<String, Object> params) {
		return handleReport(view, params, Constants.HELPDESK);
	}

    @EntityCustomAction(action = "reportsuggestions", viewKey =  EntityView.VIEW_EDIT)
    public String handleSuggestionsReport(EntityView view, Map<String, Object> params) {
        return handleReport(view, params, Constants.SUGGESTIONS);
    }

    @EntityCustomAction(action = "reportsupplementala", viewKey = EntityView.VIEW_EDIT)
    public String handleSupplementalAReport(EntityView view, Map<String, Object> params) {
        return handleReport(view, params, Constants.SUPPLEMENTAL_A);
    }

    @EntityCustomAction(action = "reportsupplementalb", viewKey = EntityView.VIEW_EDIT)
    public String handleSupplementalBReport(EntityView view, Map<String, Object> params) {
        return handleReport(view, params, Constants.SUPPLEMENTAL_B);
    }

	private String handleReport(final EntityView view, final Map<String, Object> params, final String type) {

		final String userId = developerHelperService.getCurrentUserId();

        if (view.getPathSegments().length != 3) {
            return BAD_REQUEST;
        }

        // Because the Feedback Tool EntityProvider parses URLs using forward slashes
        // (see /direct/feedback/describe) we replace forward slashes with a constant
        // and substitute them back here
        // TODO Doesn't this have a possible NPE?
        final String siteId = view.getPathSegment(1).replaceAll(FeedbackTool.FORWARD_SLASH, "/");

        if (log.isDebugEnabled()) log.debug("Site ID: " + siteId);

        final String title = (String) params.get("title");
        final String description = (String) params.get("description");
        final boolean siteExists = new Boolean((String)params.get("siteExists"));

        final String browserNameAndVersion = requestGetter.getRequest().getHeader("User-Agent");
        final String osNameAndVersion = (String) params.get("oscpu");
        final String windowHeight = (String) params.get("windowHeight");
        final String windowWidth = (String) params.get("windowWidth");
        final String browserSize = windowWidth + " x " + windowHeight + " pixels";
        final String screenHeight = (String) params.get("screenHeight");
        final String screenWidth = (String) params.get("screenWidth");
        final String screenSize = screenWidth + " x " + screenHeight + " pixels";
        final String plugins = (String) params.get("plugins");
        final String ip = requestGetter.getRequest().getRemoteAddr();
        int fmt = DateFormat.MEDIUM;
        Locale locale = sakaiProxy.getLocale();
        DateFormat format = DateFormat.getDateTimeInstance(fmt, fmt, locale);
        final String currentTime = format.format(new Date());

        if (title == null || title.isEmpty()) {
			log.debug("Subject incorrect. Returning " + BAD_TITLE + " ...");
            return BAD_TITLE;
        }

        if (description == null || description.isEmpty()) {
			log.debug("No summary. Returning " + BAD_DESCRIPTION + " ...");
            return BAD_DESCRIPTION;
        }

        if (log.isDebugEnabled()) log.debug("title: " + title + ". description: " + description);

        String toAddress = null;

        boolean addNoContactMessage = false;

        // The senderAddress can be either picked up from the current user's
        // account, or manually entered by the user submitting the report.
        String senderAddress = null;

        if (userId != null) {
            senderAddress = sakaiProxy.getUser(userId).getEmail();

            String alternativeRecipientId = (String) params.get("alternativerecipient");

            if (alternativeRecipientId != null && alternativeRecipientId.length() > 0) {
                User alternativeRecipientUser = sakaiProxy.getUser(alternativeRecipientId);

                if (alternativeRecipientUser != null) {
                    toAddress = alternativeRecipientUser.getEmail();
                    addNoContactMessage = true;
                } else {
                    try {
                        //validate site contact email address
                        InternetAddress emailAddr = new InternetAddress(alternativeRecipientId);
                        emailAddr.validate();
                        toAddress = alternativeRecipientId;
                    } catch (AddressException ex) {
                        log.error("Incorrectly formed site contact email address. Returning BADLY_FORMED_RECIPIENT...");
                        return BADLY_FORMED_RECIPIENT;
                    }
                }
            } else {
                toAddress = getToAddress(type, siteId);
            }
        }
        else {
            // Recaptcha
            if (sakaiProxy.getConfigBoolean("user.recaptcha.enabled", false)) {
                String publicKey = sakaiProxy.getConfigString("user.recaptcha.public-key", "");
                String privateKey = sakaiProxy.getConfigString("user.recaptcha.private-key", "");
                ReCaptcha captcha = ReCaptchaFactory.newReCaptcha(publicKey, privateKey, false);
                String challengeField = (String) params.get("recaptcha_challenge_field");
                String responseField = (String) params.get("recaptcha_response_field");
                if (challengeField == null) challengeField = "";
                if (responseField == null) responseField = "";
                String remoteAddress = requestGetter.getRequest().getRemoteAddr();
                ReCaptchaResponse response = captcha.checkAnswer(remoteAddress, challengeField, responseField);
                if (!response.isValid()) {
                    log.warn("Recaptcha failed with this message: " + response.getErrorMessage());
                    return RECAPTCHA_FAILURE;
                }
            }

            senderAddress = (String) params.get("senderaddress");
            if (senderAddress == null || senderAddress.length() == 0) {
                log.error("No sender email address for non logged in user. Returning BAD REQUEST ...");
                return BAD_REQUEST;
            }

            toAddress = getToAddress(type, siteId);
        }

        if (toAddress == null || toAddress.isEmpty()) {
            log.error("No recipient. Returning BAD REQUEST ...");
            return BAD_REQUEST;
        }

        if (senderAddress != null && senderAddress.length() > 0) {
            List<FileItem> attachments = null;
        
            try {
                attachments = getAttachments(params);
                sakaiProxy.sendEmail(userId, senderAddress, toAddress, addNoContactMessage, siteId, type, title, description, attachments, siteExists,
                        browserNameAndVersion, osNameAndVersion, browserSize, screenSize, plugins, ip, currentTime);
                db.logReport(userId, senderAddress, siteId, type, title, description);
                return SUCCESS;
            } catch (AttachmentsTooBigException atbe) {
                log.error("The total size of the attachments exceeded the permitted limit of " + maxAttachmentsBytes + ". '" + ATTACHMENTS_TOO_BIG + "' will be returned to the client.");
                return ATTACHMENTS_TOO_BIG;
            } catch (SQLException sqlException) {
                log.error("Caught exception while generating report. '" + Database.DB_ERROR + "' will be returned to the client.", sqlException);
                return Database.DB_ERROR;
            } catch (Exception e) {
                log.error("Caught exception while sending email or generating report. '" + ERROR + "' will be returned to the client.", e);
                return ERROR;
            }
        } else {
            log.error("Failed to determine a sender address No email or report will be generated. '"  + NO_SENDER_ADDRESS + "' will be returned to the client.");
            return NO_SENDER_ADDRESS;
        }
    }

    private String getToAddress(String type, String siteId) {
        String toAddress;
        if (Constants.CONTENT.equals(type)){
            toAddress = sakaiProxy.getSiteProperty(siteId, Site.PROP_SITE_CONTACT_EMAIL);
            if (toAddress==null){
                toAddress = sakaiProxy.getConfigString(Constants.PROP_TECHNICAL_ADDRESS, null);
            }
        } else if(Constants.HELPDESK.equals(type)){
            toAddress = sakaiProxy.getConfigString(Constants.PROP_HELP_ADDRESS, null);
        } else if(Constants.SUGGESTIONS.equals(type)){
            toAddress = sakaiProxy.getConfigString(Constants.PROP_SUGGESTIONS_ADDRESS, null);
        } else if(Constants.SUPPLEMENTAL_A.equals(type)){
            toAddress = sakaiProxy.getConfigString(Constants.PROP_SUPPLEMENTAL_A_ADDRESS, null);
        } else if(Constants.SUPPLEMENTAL_B.equals(type)){
            toAddress = sakaiProxy.getConfigString(Constants.PROP_SUPPLEMENTAL_B_ADDRESS, null);
        } else {
            toAddress = sakaiProxy.getConfigString(Constants.PROP_TECHNICAL_ADDRESS, null);
        }
        return toAddress;
    }

	private List<FileItem> getAttachments(final Map<String, Object> params) throws Exception {

		final List<FileItem> fileItems = new ArrayList<FileItem>();

		final String uploadsDone = (String) params.get(RequestFilter.ATTR_UPLOADS_DONE);

		if (uploadsDone != null && uploadsDone.equals(RequestFilter.ATTR_UPLOADS_DONE)) {
			log.debug("UPLOAD STATUS: " + params.get("upload.status"));

            FileItem attachment1 = (FileItem) params.get("attachment_0");
            if (attachment1 != null && attachment1.getSize() > 0) {
                fileItems.add(attachment1);
            }
            FileItem attachment2 = (FileItem) params.get("attachment_1");
            if (attachment2 != null && attachment2.getSize() > 0) {
                fileItems.add(attachment2);
            }
            FileItem attachment3 = (FileItem) params.get("attachment_2");
            if (attachment3 != null && attachment3.getSize() > 0) {
                fileItems.add(attachment3);
            }
            FileItem attachment4 = (FileItem) params.get("attachment_3");
            if (attachment4 != null && attachment4.getSize() > 0) {
                fileItems.add(attachment4);
            }
            FileItem attachment5 = (FileItem) params.get("attachment_4");
            if (attachment5 != null && attachment5.getSize() > 0) {
                fileItems.add(attachment5);
            }
		}

        long totalSize = 0L;

        for (FileItem fileItem : fileItems) {
            totalSize += fileItem.getSize();
        }

        if (totalSize > maxAttachmentsBytes) {
            throw new AttachmentsTooBigException();
        }

		return fileItems;
	}
}
