/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.james;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.mailet.GenericMailet;
import org.apache.mailet.Mail;
import org.apache.mailet.MailAddress;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.mailarchive.api.MailArchiveChannel;
import org.sakaiproject.mailarchive.cover.MailArchiveService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.Web;

/**
 * <p>
 * SakaiMailet watches incoming mail (via James) and sends mail to the appropriate mail archive channel in Sakai.
 * </p>
 */
public class SakaiMailet extends GenericMailet
{
	/** Resource bundle using current language locale */
	private static ResourceLoader rb = new ResourceLoader("sakaimailet");

	/** Our logger. */
	private static Logger M_log = LoggerFactory.getLogger(SakaiMailet.class);

	/** The user name of the postmaster user - the one who posts incoming mail. */
	public static final String POSTMASTER = "postmaster";
	
	// used when parsing email header parts
	private static final String NAME_PREFIX = "name=";

    private static String PROCESSOR_ROOT = "root";
    private static String PROCESSOR_ERROR = Mail.ERROR;
    private static String PROCESSOR_TRANSPORT = Mail.TRANSPORT;
	/* 
	 * root processor is first (redirects to transport mostly), then error, then transport (which is where the SakaiMailet is engaged)
	 * then the processors in the order listed below
	 * 
	 * From: http://james.apache.org/server/2.3.0/spoolmanager_configuration.html
	 * The James SpoolManager creates a correspondence between processor names and the "state" of a mail as defined in the Mailet API. 
	 * Specifically, after each mailet processes a mail, the state of the message is examined. 
	 * If the state has been changed, the message does not continue in the current processor. 
	 * If the new state is "ghost" then processing of that message terminates completely. 
	 * If the new state is anything else, the message is re-routed to the processor with the name matching the new state.
	 */
    private static String PROCESSOR_SPAM = "spam";
    private static String PROCESSOR_VIRUS = "virus";
    private static String PROCESSOR_LOCAL_ADDRESS_ERROR = "local-address-error";
    private static String PROCESSOR_RELAY_DENIED = "relay-denied";
    private static String PROCESSOR_BOUNCES = "bounces";
    // indicates no more processing should occur
    private static String PROCESSOR_GHOST = Mail.GHOST;
    /**
     * The james processor order - NOTE: this MUST match with the config in:
     * /sakai-mailarchive-james/src/webapp/apps/james/SAR-INF/config.xml
     */
    private static String[] PROCESSORS = {
        PROCESSOR_ROOT,
        PROCESSOR_ERROR,
        PROCESSOR_TRANSPORT,
        PROCESSOR_SPAM,
        PROCESSOR_VIRUS,
        PROCESSOR_LOCAL_ADDRESS_ERROR,
        PROCESSOR_RELAY_DENIED,
        PROCESSOR_BOUNCES,
        PROCESSOR_GHOST
    };

	private AliasService aliasService = null;
	private ContentHostingService contentHostingService = null;
	private EntityManager entityManager = null;
	private SiteService siteService = null;
	private ThreadLocalManager threadLocalManager = null;
	private TimeService timeService = null;
	private SessionManager sessionManager = null;
	private UserDirectoryService userDirectoryService = null;
	private ServerConfigurationService serverConfigurationService = null;

    private String courseMailArchiveDisabledProcessor = null;
	private String courseMailArchiveNotExistsProcessor = null;
    private String userNotAllowedToPostProcessor = null;

    /**
	 * Called when created.
	 */
	public void init() throws MessagingException
	{
		M_log.info("init()");
		// load the services
		aliasService = requireService(AliasService.class);
		contentHostingService = requireService(ContentHostingService.class);
		entityManager = requireService(EntityManager.class);
		siteService = requireService(SiteService.class);
		threadLocalManager = requireService(ThreadLocalManager.class);
		timeService = requireService(TimeService.class);
		sessionManager = requireService(SessionManager.class);
		userDirectoryService = requireService(UserDirectoryService.class);
		serverConfigurationService = requireService(ServerConfigurationService.class);

		// load up the configuration options for the sakai mailet processor
        courseMailArchiveDisabledProcessor = serverConfigurationService.getString("smtp.archive.disabled.processor", PROCESSOR_GHOST);
        if (courseMailArchiveDisabledProcessor == null 
                || "none".equalsIgnoreCase(courseMailArchiveDisabledProcessor)
                || !Arrays.asList(PROCESSORS).contains(courseMailArchiveDisabledProcessor)) {
            courseMailArchiveDisabledProcessor = PROCESSOR_GHOST; // DEFAULT
        }
		courseMailArchiveNotExistsProcessor = serverConfigurationService.getString("smtp.archive.address.invalid.processor", PROCESSOR_GHOST);
        if (courseMailArchiveNotExistsProcessor == null 
                || "none".equalsIgnoreCase(courseMailArchiveNotExistsProcessor)
                || !Arrays.asList(PROCESSORS).contains(courseMailArchiveNotExistsProcessor)) {
            courseMailArchiveNotExistsProcessor = PROCESSOR_GHOST; // DEFAULT
        }
		userNotAllowedToPostProcessor = serverConfigurationService.getString("smtp.user.not.allowed.processor", PROCESSOR_GHOST);
        if (userNotAllowedToPostProcessor == null 
                || "none".equalsIgnoreCase(userNotAllowedToPostProcessor)
                || !Arrays.asList(PROCESSORS).contains(userNotAllowedToPostProcessor)) {
            userNotAllowedToPostProcessor = PROCESSOR_GHOST; // DEFAULT
        }
        M_log.info("MailArchiveDisabledProcessor="+courseMailArchiveDisabledProcessor+", MailArchiveNotExistsProcessor="+courseMailArchiveNotExistsProcessor+", UserNotAllowedToPostProcessor"+userNotAllowedToPostProcessor);
	}

	/**
	 * Get the service for a class or die
	 * @param serviceClass
	 * @return the service class
	 * @throws IllegalStateException if the service cannot be found
	 */
	@SuppressWarnings({"unchecked"})
    private <T> T requireService(Class<T> serviceClass) {
	    Object service = ComponentManager.get(serviceClass);
	    if (service == null) {
	        throw new IllegalStateException("Unable to get service ("+serviceClass+") from Sakai ComponentManager");
	    }
	    return (T) service;
	}

	/**
	 * Called when leaving.
	 */
	public void destroy()
	{
		M_log.info("destroy()");

		super.destroy();

	} // destroy

	/**
	 * Process incoming mail.
	 * 
	 * @param mail
	 *        ...
	 */
	public void service(Mail mail) throws MessagingException
	{
		// get the postmaster user
		User postmaster = null;
		try
		{
			postmaster = userDirectoryService.getUser(POSTMASTER);
		}
		catch (UserNotDefinedException e)
		{
			M_log.warn("service(): no postmaster, incoming mail will not be processed until a postmaster user (id="+POSTMASTER+") exists in this Sakai instance");
			mail.setState(Mail.GHOST);
			return;
		}

		try
		{
			// set the current user to postmaster
			Session s = sessionManager.getCurrentSession();
			if (s != null)
			{
				s.setUserId(postmaster.getId());
			}
			else
			{
				M_log.warn("service - no SessionManager.getCurrentSession, cannot set to postmaser user, attempting to use the current user ("
				        +sessionManager.getCurrentSessionUserId()+") and session ("+sessionManager.getCurrentSession().getId()+")");
			}

			MimeMessage msg = mail.getMessage();
			String id = msg.getMessageID();

			Address[] fromAddresses = msg.getFrom();
			String from = null;
			String fromAddr = null;
			if ((fromAddresses != null) && (fromAddresses.length == 1))
			{
				from = fromAddresses[0].toString();
				if (fromAddresses[0] instanceof InternetAddress)
				{
					fromAddr = ((InternetAddress) (fromAddresses[0])).getAddress();
				}
			}
			else
			{
				from = mail.getSender().toString();
				fromAddr = mail.getSender().toInternetAddress().getAddress();
			}

			Collection<MailAddress> to = mail.getRecipients();

			Date sent = msg.getSentDate();

			String subject = StringUtils.trimToNull(msg.getSubject());

			Enumeration<String> headers = msg.getAllHeaderLines();
			List<String> mailHeaders = new Vector<String>();
			while (headers.hasMoreElements())
			{
				String line = (String) headers.nextElement();
				// check if string starts with "Content-Type", ignoring case
				if (line.regionMatches(true, 0, MailArchiveService.HEADER_CONTENT_TYPE, 0, MailArchiveService.HEADER_CONTENT_TYPE.length()))
				{
					String contentType = line.substring(0, MailArchiveService.HEADER_CONTENT_TYPE.length() );
					mailHeaders.add(line.replaceAll(contentType, MailArchiveService.HEADER_OUTER_CONTENT_TYPE));
				}
				// don't copy null subject lines. we'll add a real one below
				if (!(line.regionMatches(true, 0, MailArchiveService.HEADER_SUBJECT, 0, MailArchiveService.HEADER_SUBJECT.length()) &&
				      subject == null))
				    mailHeaders.add(line);

			}

           //Add headers for a null subject, keep null in DB
			if (subject == null) {
               mailHeaders.add(MailArchiveService.HEADER_SUBJECT + ": <"+ rb.getString("err_no_subject") +">");
			}

            if (M_log.isDebugEnabled()) {
                M_log.debug(id + " : mail: from:" + from + " sent: " + timeService.newTime(sent.getTime()).toStringLocalFull() 
                        + " subject: " + subject);
            }

			// process for each recipient
			Iterator<MailAddress> it = to.iterator();
			while (it.hasNext())
			{
				String mailId = null;
				try
				{
					MailAddress recipient = (MailAddress) it.next();
                    if (M_log.isDebugEnabled()) {
                        M_log.debug(id + " : checking to: " + recipient);
                    }

					// the recipient's mail id
					mailId = recipient.getUser();

					// eat the no-reply
					if ("no-reply".equalsIgnoreCase(mailId))
					{
						mail.setState(Mail.GHOST);
                        if (M_log.isInfoEnabled()) {
                            M_log.info("Incoming message mailId ("+mailId+") set to no-reply, mail processing cancelled");
                        }
                        /* NOTE: this doesn't make a lot of sense to me, once the mail is ghosted 
                         * then it won't be processed anymore so continuing is kind of a waste of time,
                         * shouldn't this just break instead?
                         */
						continue;
					}

					// find the channel (mailbox) that this is addressed to
					// for now, check only for it being a site or alias to a site.
					// %%% - add user and other later -ggolden
					MailArchiveChannel channel = null;

					// first, assume the mailId is a site id
					String channelRef = MailArchiveService.channelReference(mailId, SiteService.MAIN_CONTAINER);
					try
					{
						channel = MailArchiveService.getMailArchiveChannel(channelRef);
                        if (M_log.isDebugEnabled()) {
                            M_log.debug("Incoming message mailId ("+mailId+") IS a valid site channel reference");
                        }
					}
					catch (IdUnusedException goOn) {
					    // INDICATES the incoming message is NOT for a currently valid site
					    if (M_log.isDebugEnabled()) {
					        M_log.debug("Incoming message mailId ("+mailId+") is NOT a valid site channel reference, will attempt more matches");
					    }
					} catch (PermissionException e) {
					    // INDICATES the channel is valid but the user has no permission to access it
	                    // This generally should not happen because the current user should be the postmaster
	                    M_log.warn("mailarchive failure: message processing cancelled: PermissionException with channelRef ("+channelRef
	                            +") - user not allowed to get this mail archive channel: (id="+id+") (mailId="+mailId+") (user="
                                +sessionManager.getCurrentSessionUserId()+") (session="+sessionManager.getCurrentSession().getId()+"): " + e, e);
	                    // BOUNCE REPLY - send a message back to the user to let them know their email failed
                        String errMsg = rb.getString("err_not_member") + "\n\n";
                        String mailSupport = StringUtils.trimToNull( serverConfigurationService.getString("mail.support") );
                        if ( mailSupport != null ) {
                            errMsg +=(String) rb.getFormattedMessage("err_questions",  new Object[]{mailSupport})+"\n";
                        }
                        mail.setErrorMessage(errMsg);
                        mail.setState(userNotAllowedToPostProcessor);
                        continue;
                    }

					// next, if not a site, see if it's an alias to a site or channel
					if (channel == null) {
						// if not an alias, it will throw the IdUnusedException caught below
						Reference ref = entityManager.newReference(aliasService.getTarget(mailId));

						if (ref.getType().equals(SiteService.APPLICATION_ID)) {
						    // ref is a site
							// now we have a site reference, try for it's channel
							channelRef = MailArchiveService.channelReference(ref.getId(), SiteService.MAIN_CONTAINER);
                            if (M_log.isDebugEnabled()) {
                                M_log.debug("Incoming message mailId ("+mailId+") IS a valid site reference ("+ref.getId()+")");
                            }
						} else if (ref.getType().equals(MailArchiveService.APPLICATION_ID)) {
							// ref is a channel
							channelRef = ref.getReference();
                            if (M_log.isDebugEnabled()) {
                                M_log.debug("Incoming message mailId ("+mailId+") IS a valid channel reference ("+ref.getId()+")");
                            }
						} else {
						    // ref cannot be be matched
	                        if (M_log.isInfoEnabled()) {
	                            M_log.info(id + " : mail rejected: unknown address: " + mailId + " : mailId ("+mailId+") does NOT match site, alias, or other current channel");
	                        }
	                        if (M_log.isDebugEnabled()) {
	                            M_log.debug("Incoming message mailId ("+mailId+") is NOT a valid does NOT match site, alias, or other current channel reference ("+ref.getId()+"), message rejected");
	                        }
							throw new IdUnusedException(mailId);
						}

						// if there's no channel for this site, it will throw the IdUnusedException caught below
						try {
                            channel = MailArchiveService.getMailArchiveChannel(channelRef);
                        } catch (PermissionException e) {
                            // INDICATES the channel is valid but the user has no permission to access it
                            // This generally should not happen because the current user should be the postmaster
                            M_log.warn("mailarchive failure: message processing cancelled: PermissionException with channelRef ("+channelRef
                                    +") - user not allowed to get this mail archive channel: (id="+id+") (mailId="+mailId+") (user="
                                    +sessionManager.getCurrentSessionUserId()+") (session="+sessionManager.getCurrentSession().getId()+"): " + e, e);
                            // BOUNCE REPLY - send a message back to the user to let them know their email failed
                            String errMsg = rb.getString("err_not_member") + "\n\n";
                            String mailSupport = StringUtils.trimToNull( serverConfigurationService.getString("mail.support") );
                            if ( mailSupport != null ) {
                                errMsg +=(String) rb.getFormattedMessage("err_questions",  new Object[]{mailSupport})+"\n";
                            }
                            mail.setErrorMessage(errMsg);
                            mail.setState(userNotAllowedToPostProcessor);
                            continue;
                        }
                        if (M_log.isDebugEnabled()) {
                            M_log.debug("Incoming message mailId ("+mailId+") IS a valid channel ("+channelRef+"), found channel: "+channel);
                        }
					}
                    if (channel == null) {
                        if (M_log.isDebugEnabled()) {
                            M_log.debug("Incoming message mailId ("+mailId+"), channelRef ("+channelRef+") could not be resolved and is null: "+channel);
                        }
                        // this should never happen but it is here just in case
                        throw new IdUnusedException(mailId);
                    }

                    // skip disabled channels
					if (!channel.getEnabled())
					{
                        // INDICATES that the channel is NOT currently enabled so no messages can be received
						if (from.startsWith(POSTMASTER)) {
							mail.setState(Mail.GHOST);
						} else {
		                    // BOUNCE REPLY - send a message back to the user to let them know their email failed
							String errMsg = rb.getString("err_email_off") + "\n\n";
							String mailSupport = StringUtils.trimToNull( serverConfigurationService.getString("mail.support") );
							if ( mailSupport != null ) {
								errMsg +=(String) rb.getFormattedMessage("err_questions",  new Object[]{mailSupport})+"\n";
							}
							mail.setErrorMessage(errMsg);
                            mail.setState(courseMailArchiveDisabledProcessor);
						}

                        if (M_log.isInfoEnabled()) {
                            M_log.info(id + " : mail rejected: channel ("+channelRef+") not enabled: " + mailId);
                        }
						continue;
					}

					// for non-open channels, make sure the from is a member
					if (!channel.getOpen())
					{
						// see if our fromAddr is the email address of any of the users who are permitted to add messages to the channel.
						if (!fromValidUser(fromAddr, channel))
						{
						    // INDICATES user is not allowed to send messages to this group
		                    if (M_log.isInfoEnabled()) {
		                        M_log.info(id + " : mail rejected: from: " + fromAddr + " not authorized for site: " + mailId + " and channel ("+channelRef+")");
		                    }
		                    // BOUNCE REPLY - send a message back to the user to let them know their email failed
							String errMsg = rb.getString("err_not_member") + "\n\n";
							String mailSupport = StringUtils.trimToNull( serverConfigurationService.getString("mail.support") );
							if ( mailSupport != null ) {
								errMsg +=(String) rb.getFormattedMessage("err_questions",  new Object[]{mailSupport})+"\n";
							}
							mail.setErrorMessage(errMsg);
		                    mail.setState(userNotAllowedToPostProcessor);
							continue;
						}
					}

					// prepare the message 
					StringBuilder bodyBuf[] = new StringBuilder[2];
					bodyBuf[0] = new StringBuilder();
					bodyBuf[1] = new StringBuilder();
					List<Reference> attachments = entityManager.newReferenceList();
					String siteId = null;
					if (siteService.siteExists(channel.getContext())) {
						siteId = channel.getContext();
					}
					
					try
					{
						StringBuilder bodyContentType = new StringBuilder();
						parseParts(siteId, msg, id, bodyBuf, bodyContentType, attachments, Integer.valueOf(-1));
						
						if (bodyContentType.length() > 0)
						{
							// save the content type of the message body - which may be different from the
							// overall MIME type of the message (multipart, etc)
							mailHeaders.add(MailArchiveService.HEADER_INNER_CONTENT_TYPE + ": " + bodyContentType);
						}
					}
					catch (MessagingException e)
					{
					    // NOTE: if this happens it just means we don't get the extra header, not the end of the world
					    //e.printStackTrace();
						M_log.warn("MessagingException: service(): msg.getContent() threw: " + e, e);
					}
					catch (IOException e)
					{
                        // NOTE: if this happens it just means we don't get the extra header, not the end of the world
					    //e.printStackTrace();
						M_log.warn("IOException: service(): msg.getContent() threw: " + e, e);
					}

					mailHeaders.add("List-Id: <"+ channel.getId()+ "."+ channel.getContext()
							+ "."+ serverConfigurationService.getServerName()+ ">");

					// post the message to the group's channel
					String body[] = new String[2];
					body[0] = bodyBuf[0].toString(); // plain/text
					body[1] = bodyBuf[1].toString(); // html/text
					
					try {
                        if (channel.getReplyToList())
                        {
                        	List<String> modifiedHeaders = new Vector<String>();
                        	for (String header: (List<String>)mailHeaders) 
                        	{
                        		if (header != null && !header.startsWith("Reply-To:"))
                        		{
                        			modifiedHeaders.add(header);
                        		}
                        	}
                        	// Note: can't use recipient, since it's host may be configured as mailId@myhost.james
                        	String mailHost = serverConfigurationService.getServerName();
                        	if ( mailHost == null || mailHost.trim().equals("") )
                        		mailHost = mail.getRemoteHost();
              
                        	MailAddress replyTo = new MailAddress( mailId, mailHost );
                            if (M_log.isDebugEnabled()) {
                                M_log.debug("Set Reply-To address to "+ replyTo.toString());
                            }
                        	modifiedHeaders.add("Reply-To: "+ replyTo.toString());
  
                        	// post the message to the group's channel
                        	channel.addMailArchiveMessage(subject, from.toString(), timeService.newTime(sent.getTime()), modifiedHeaders,
                        			attachments, body);
                        }
                        else
                        {
                        	// post the message to the group's channel
                        	channel.addMailArchiveMessage(subject, from.toString(), timeService.newTime(sent.getTime()), mailHeaders,
                        			attachments, body);
                        }
                    } catch (PermissionException pe) {
                        // INDICATES that the current user does not have permission to add or get the mail archive message from the current channel
                        // This generally should not happen because the current user should be the postmaster
                        M_log.warn("mailarchive PermissionException message service failure: (id="+id+") (mailId="+mailId+") : " + pe, pe);
                        mail.setState(Mail.GHOST); // ghost out the message because this should not happen
                    }

                    if (M_log.isDebugEnabled()) {
                        M_log.debug(id + " : delivered to:" + mailId);
                    }

					// all is happy - ghost the message to stop further processing
					mail.setState(Mail.GHOST);
				}
				catch (IdUnusedException goOn)
				{
					// INDICATES that the channelReference found above was actually invalid OR that no channel reference could be identified

				    // if this is to the postmaster, and there's no site, channel or alias for the postmaster, then quietly eat the message
					if (POSTMASTER.equals(mailId) || from.startsWith(POSTMASTER + "@"))
					{
						mail.setState(Mail.GHOST);
						continue;
					}

					// BOUNCE REPLY - send a message back to the user to let them know their email failed
					if (M_log.isInfoEnabled()) {
					    M_log.info("mailarchive invalid or unusable channel reference ("+mailId+"): "+id + " : mail rejected: " + goOn.toString());
					}
					String errMsg = rb.getString("err_addr_unknown") + "\n\n";
					String mailSupport = StringUtils.trimToNull( serverConfigurationService.getString("mail.support") );
					if ( mailSupport != null ) {
						errMsg +=(String) rb.getFormattedMessage("err_questions",  new Object[]{mailSupport})+"\n";
					}
					mail.setErrorMessage(errMsg);
                    mail.setState(courseMailArchiveNotExistsProcessor);
				}
				catch (Exception ex)
				{
                    // INDICATES that some general exception has occurred which we did not expect
			        // This definitely should NOT happen
					M_log.error("mailarchive General message service exception: (id="+id+") (mailId="+mailId+") : " + ex, ex);
                    mail.setState(Mail.GHOST); // ghost the message to stop it from being further processed
				}
			}
		}
		finally
		{
			// clear out any current current bindings
			threadLocalManager.clear();
		}
	}

	/**
	 * Check if the fromAddr email address is recognized as belonging to a user who has permission to add to the channel.
	 * 
	 * @param fromAddr
	 *        The email address to check.
	 * @param channel
	 *        The mail archive channel.
	 * @return True if the email address is from a user who is authorized to add mail, false if not.
	 */
	protected boolean fromValidUser(String fromAddr, MailArchiveChannel channel)
	{
		if ((fromAddr == null) || (fromAddr.length() == 0)) return false;

		// find the users with this email address
		Collection<User> users = userDirectoryService.findUsersByEmail(fromAddr);

		// if none found
		if ((users == null) || (users.isEmpty())) return false;

		// see if any of them are allowed to add
		for (Iterator<User> i = users.iterator(); i.hasNext();)
		{
			User u = (User) i.next();
			if (channel.allowAddMessage(u)) return true;
		}

		return false;
	}

	/**
	 * Create an attachment, adding it to the list of attachments.
	 */
	protected Reference createAttachment(String siteId, List attachments, String type, String fileName, byte[] body, String id)
	{
		// we just want the file name part - strip off any drive and path stuff
		String name = FilenameUtils.getName(fileName);  //Validator.getFileName(fileName);
		String resourceName = Validator.escapeResourceName(fileName);

		// make a set of properties to add for the new resource
		ResourcePropertiesEdit props = contentHostingService.newResourceProperties();
		props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
		props.addProperty(ResourceProperties.PROP_DESCRIPTION, fileName);

		// make an attachment resource for this URL
		try
		{
			ContentResource attachment;
			if (siteId == null) {
				attachment = contentHostingService.addAttachmentResource(resourceName, type, body, props);
			} else {
				attachment = contentHostingService.addAttachmentResource(
						resourceName, siteId, null, type, body, props);
			}

			// add a dereferencer for this to the attachments
			Reference ref = entityManager.newReference(attachment.getReference());
			attachments.add(ref);

			M_log.debug(id + " : attachment: " + ref.getReference() + " size: " + body.length);

			return ref;
		}
		catch (Exception any)
		{
			M_log.warn(id + " : exception adding attachment resource: " + name + " : " + any.toString());
			return null;
		}
	}

	/**
	 * Read in a stream from the mime body into a byte array
	 */
	protected byte[] readBody(int approxSize, InputStream stream)
	{
		// the size is APPROXIMATE, and is sometimes wrong -
		// so read the body into a ByteArrayOutputStream
		// that will grow if necessary
		if (approxSize <= 0) return null;

		ByteArrayOutputStream baos = new ByteArrayOutputStream(approxSize);
		byte[] buff = new byte[10000];
		try
		{
			//int lenRead = 0;
			int count = 0;
			while (count >= 0)
			{
				count = stream.read(buff, 0, buff.length);
				if (count <= 0) break;
				baos.write(buff, 0, count);
				//lenRead += count;
			}
		}
		catch (IOException e)
		{
			M_log.warn("readBody(): " + e);
		}

		return baos.toByteArray();
	}

	/**
	 * Breaks email messages into parts which can be saved as files (saves as attachments) or viewed as plain text (added to body of message).
	 * 
	 * @param siteId
	 *        Site associated with attachments, if any
	 * @param p
	 *        The message-part embedded in a message..
	 * @param id
	 *        The string containing the message's id.
	 * @param bodyBuf
	 *        The string-buffers in which the plain/text and/or html/text message body is being built.
	 * @param bodyContentType
	 *        The value of the Content-Type header for the mesage body.
	 * @param attachments
	 *        The ReferenceVector in which references to attachments are collected.
	 * @param embedCount
	 *        An Integer that counts embedded messages (outer message is zero).
	 * @return Value of embedCount (updated if this call processed any embedded messages).
	 */
	protected Integer parseParts(String siteId, Part p, String id, StringBuilder bodyBuf[], 
										  StringBuilder bodyContentType, List attachments,	Integer embedCount) 
			throws MessagingException, IOException
	{
		// increment embedded message counter
		if (p instanceof Message)
		{
			embedCount = Integer.valueOf( embedCount.intValue() + 1 );
		}
		
		String type = p.getContentType();

		// discard if content-type is unknown
		if (type == null || "".equals(type))
		{
			M_log.warn(this+" message with unknown content-type discarded");
		}
		
		// add plain text to bodyBuf[0]
		else if (p.isMimeType("text/plain") && p.getFileName() == null)
		{
		        Object o = null;
			// let them convert to text if possible
			// but if bad encaps get the stream and do it ourselves
			try {
			    o = p.getContent();
			} catch (java.io.UnsupportedEncodingException ignore) {
			    o = p.getInputStream();
			}

			String txt = null;
			String innerContentType = p.getContentType();

			if (o instanceof String)
			{
				txt = (String) p.getContent();
				if (bodyContentType != null && bodyContentType.length() == 0) 
					bodyContentType.append(innerContentType);
			}

			else if (o instanceof InputStream)
			{
				InputStream in = (InputStream) o;
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte[] buf = new byte[in.available()];
				for (int len = in.read(buf); len != -1; len = in.read(buf))
					out.write(buf, 0, len);
				String charset = (new ContentType(innerContentType)).getParameter("charset");
				// RFC 2045 says if no char set specified use US-ASCII.
				// If specified but illegal that's less clear. The common case is X-UNKNOWN.
				// my sense is that UTF-8 is most likely these days but the sample we got
				// was actually ISO 8859-1. Could also justify using US-ASCII. Duh...
				if (charset == null)
				    charset = "us-ascii";
				try {
				    txt = out.toString(MimeUtility.javaCharset(charset));
				} catch (java.io.UnsupportedEncodingException ignore) {
				    txt = out.toString("UTF-8");
				}
				if (bodyContentType != null && bodyContentType.length() == 0) 
					bodyContentType.append(innerContentType);
			}
			
 			// remove extra line breaks added by mac Mail, perhaps others
			// characterized by a space followed by a line break
			if (txt != null)
			{
				txt = txt.replaceAll(" \n", " ");
			}

			// make sure previous message parts ended with newline
			if (bodyBuf[0].length() > 0 && bodyBuf[0].charAt(bodyBuf[0].length() - 1) != '\n')
				bodyBuf[0].append("\n");
			
			bodyBuf[0].append(txt);
		}

		// add html text to bodyBuf[1]
		else if (p.isMimeType("text/html") && p.getFileName() == null)
		{
		        Object o = null;
			// let them convert to text if possible
			// but if bad encaps get the stream and do it ourselves
			try {
			    o = p.getContent();
			} catch (java.io.UnsupportedEncodingException ignore) {
			    o = p.getInputStream();
			}
			String txt = null;
			String innerContentType = p.getContentType();

			if (o instanceof String)
			{
				txt = (String) p.getContent();
				if (bodyContentType != null && bodyContentType.length() == 0) 
					bodyContentType.append(innerContentType);
			}

			else if (o instanceof InputStream)
			{
				InputStream in = (InputStream) o;
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte[] buf = new byte[in.available()];
				for (int len = in.read(buf); len != -1; len = in.read(buf))
					out.write(buf, 0, len);
				String charset = (new ContentType(innerContentType)).getParameter("charset");
				if (charset == null)
				    charset = "us-ascii";
				try {
				    txt = out.toString(MimeUtility.javaCharset(charset));
				} catch (java.io.UnsupportedEncodingException ignore) {
				    txt = out.toString("UTF-8");
				}
				if (bodyContentType != null && bodyContentType.length() == 0) 
					bodyContentType.append(innerContentType);
			}

			// remove bad image tags and naughty javascript
			if (txt !=null)
			{
				txt = Web.cleanHtml(txt);
			}
			
			bodyBuf[1].append(txt);
		}
				
		// process subparts of multiparts
		else if (p.isMimeType("multipart/*"))
		{
			Multipart mp = (Multipart) p.getContent();
			int count = mp.getCount();
			for (int i = 0; i < count; i++)
			{
				embedCount = parseParts(siteId, mp.getBodyPart(i), id, bodyBuf, bodyContentType, attachments, embedCount);
			}
		}
		
		// Discard parts with mime-type application/applefile. If an e-mail message contains an attachment is sent from
		// a macintosh, you may get two parts, one for the data fork and one for the resource fork. The part that
		// corresponds to the resource fork confuses users, this has mime-type application/applefile. The best thing
		// is to discard it.
		else if (p.isMimeType("application/applefile"))
		{
			M_log.warn(this+" message with application/applefile discarded");
		}
		
		// discard enriched text version of the message.
		// Sakai only uses the plain/text or html/text version of the message.
		else if (p.isMimeType("text/enriched") && p.getFileName() == null)
		{
			M_log.warn(this+" message with text/enriched discarded");
		}
		
		// everything else gets treated as an attachment
		else
		{
			String name = p.getFileName();
			
			// look for filenames not parsed by getFileName() 
			if ( name == null && type.indexOf(NAME_PREFIX) != -1 )
			{
				name = type.substring( type.indexOf(NAME_PREFIX)+NAME_PREFIX.length() );
			}
			// ContentType can't handle filenames with spaces or UTF8 characters
			if ( name != null )
			{
				String decodedName = MimeUtility.decodeText( name ); // first decode RFC 2047
				type = type.replace( name, URLEncoder.encode(decodedName, "UTF-8") );
				name = decodedName;
			}
			
			ContentType cType = new ContentType(type);
			String disposition = p.getDisposition();
			int approxSize = p.getSize();

			if (name == null)
			{
				name = "unknown";
				// if file's parent is multipart/alternative,
				// provide a better name for the file
				if (p instanceof BodyPart)
				{
					Multipart parent = ((BodyPart) p).getParent();
					if (parent != null)
					{
						String pType = parent.getContentType();
						ContentType pcType = new ContentType(pType);
						if (pcType.getBaseType().equalsIgnoreCase("multipart/alternative"))
						{
							name = "message" + embedCount;
						}
					}
				}
				if (p.isMimeType("text/html"))
				{
					name += ".html";
				}
				else if (p.isMimeType("text/richtext"))
				{
					name += ".rtx";
				}
				else if (p.isMimeType("text/rtf"))
				{
					name += ".rtf";
				}
				else if (p.isMimeType("text/enriched"))
				{
					name += ".etf";
				}
				else if (p.isMimeType("text/plain"))
				{
					name += ".txt";
				}
				else if (p.isMimeType("text/xml"))
				{
					name += ".xml";
				}
				else if (p.isMimeType("message/rfc822"))
				{
					name += ".txt"; 
				}
			}

			// read the attachments bytes, and create it as an attachment in content hosting
			byte[] bodyBytes = readBody(approxSize, p.getInputStream());
			if ((bodyBytes != null) && (bodyBytes.length > 0))
			{
				// can we ignore the attachment it it's just whitespace chars??
				Reference attachment = createAttachment(siteId, attachments, cType.getBaseType(), name, bodyBytes, id);

				// add plain/text attachment reference (if plain/text message)
				if (attachment != null && bodyBuf[0].length() > 0)
					bodyBuf[0].append("[see attachment: \"" + name + "\", size: " + bodyBytes.length + " bytes]\n\n");
					
				// add html/text attachment reference (if html/text message)
				if (attachment != null && bodyBuf[1].length() > 0)
					bodyBuf[1].append("<p>[see attachment: \"" + name + "\", size: " + bodyBytes.length + " bytes]</p>");
					
				// add plain/text attachment reference (if no plain/text and no html/text)
				if (attachment != null && bodyBuf[0].length() == 0 && bodyBuf[1].length() == 0)
					bodyBuf[0].append("[see attachment: \"" + name + "\", size: " + bodyBytes.length + " bytes]\n\n");
			}
		}
		
		return embedCount;
	}

}
