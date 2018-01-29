/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.email.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.Digest;
import org.sakaiproject.email.api.DigestEdit;
import org.sakaiproject.email.api.DigestMessage;
import org.sakaiproject.email.api.DigestService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeBreakdown;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.SessionBindingEvent;
import org.sakaiproject.tool.api.SessionBindingListener;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.sakaiproject.util.Resource;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.SingleStorageUser;
import org.sakaiproject.util.Xml;

/**
 * <p>
 * BaseDigestService is the base service for DigestService.
 * </p>
 */
@Slf4j
public abstract class BaseDigestService implements DigestService, SingleStorageUser
{
	/** localized tool properties **/
	private static final String DEFAULT_RESOURCECLASS = "org.sakaiproject.localization.util.EmailImplProperties";
	private static final String DEFAULT_RESOURCEBUNDLE = "org.sakaiproject.localization.bundle.emailimpl.email-impl";
	private static final String RESOURCECLASS = "resource.class.emailimpl";
	private static final String RESOURCEBUNDLE = "resource.bundle.emailimpl";	
	private ResourceLoader rb = null;
	// private ResourceLoader rb = new ResourceLoader("email-impl");

	/** Storage manager for this service. */
	protected Storage m_storage = null;

	/** The initial portion of a relative access point URL. */
	protected String m_relativeAccessPoint = null;

	/** The queue of digests waiting to be added (DigestMessage). */
	protected List m_digestQueue = new Vector();

	/** The thread I run my periodic clean and report on. */
	//	protected Thread m_thread = null;

	/** My thread's quit flag. */
	//	protected boolean m_threadStop = false;

	/** How long to wait between digest checks (seconds) */
	private int DIGEST_PERIOD = 3600;
	/** How long to wait between digest checks (seconds) */
	public void setDIGEST_PERIOD(int digest_period) {
		DIGEST_PERIOD = digest_period;
	}

	/** How long to wait before the first digest check (seconds) */
	private int DIGEST_DELAY = 300;
	/** How long to wait before the first digest check (seconds) */
	public void setDIGEST_DELAY(int digest_delay) {
		DIGEST_DELAY = digest_delay;
	}

	protected boolean m_debugBypass = false;

	/** True if we are in the mode of sending out digests, false if we are waiting. */
	protected boolean m_sendDigests = true;

	/** The time period last time the sendDigests() was called. */
	protected String m_lastSendPeriod = null;

	/**
	 * Use a timer for repeating actions
	 */
	private Timer digestTimer = new Timer(true);

	/**
	 * This is the name of the sakai.properties property for the DIGEST_PERIOD,
	 * this is how long (in seconds) the digest service will wait between checking to see if there
	 * are digests that need to be sent (they are always only sent once per day), default=3600
	 */
	public static final String EMAIL_DIGEST_CHECK_PERIOD_PROPERTY = "email.digest.check.period";

	/**
	 * This is the name of the sakai.properties property for the DIGEST_DELAY,
	 * this is how long (in seconds) the digest service will wait after starting up
	 * before it does the first check for sending out digests, default=300
	 */
	public static final String EMAIL_DIGEST_START_DELAY_PROPERTY = "email.digest.start.delay";

	public static final String BY_PASS_FOR_DEBUG = "digest.email.bypass.for.debug";

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Runnable
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Start the clean and report thread.
	 */
	//	protected void start()
	//	{
	//		m_threadStop = false;
	//
	//		m_thread = new Thread(this, getClass().getName());
	//		m_thread.start();
	//	}

	/**
	 * Stop the clean and report thread.
	 */
	//	protected void stop()
	//	{
	//		if (m_thread == null) return;
	//
	//		// signal the thread to stop
	//		m_threadStop = true;
	//
	//		// wake up the thread
	//		m_thread.interrupt();
	//
	//		m_thread = null;
	//	}

	/**
	 * Run the clean and report thread.
	 */
	//	public void run()
	//	{
	//		// since we might be running while the component manager is still being created and populated, such as at server
	//		// startup, wait here for a complete component manager
	//		ComponentManager.waitTillConfigured();
	//
	//		// loop till told to stop
	//		while ((!m_threadStop) && (!Thread.currentThread().isInterrupted()))
	//		{
	//			try
	//			{
	//				// process the queue of digest requests
	//				processQueue();
	//
	//				// check for a digest mailing time
	//				sendDigests();
	//			}
	//			catch (Exception e)
	//			{
	//				log.warn(": exception: ", e);
	//			}
	//
	//			// take a small nap
	//			try
	//			{
	//				Thread.sleep(PERIOD);
	//			}
	//			catch (Exception ignore)
	//			{
	//			}
	//		}
	//	}

	/**
	 * Attempt to process all the queued digest requests. Ones that cannot be processed now will be returned to the queue.
	 */
	protected void processQueue()
	{
		log.debug("Processing mail digest queue...");

		// setup a re-try queue
		List retry = new Vector();

		// grab the queue - any new stuff will be processed next time
		List queue = new Vector();
		synchronized (m_digestQueue)
		{
			queue.addAll(m_digestQueue);
			m_digestQueue.clear();
		}

		for (Iterator iQueue = queue.iterator(); iQueue.hasNext();)
		{
			DigestMessage message = (DigestMessage) iQueue.next();
			try
			{
				DigestEdit edit = edit(message.getTo());
				edit.add(message);
				commit(edit);
				// %%% could do this by pulling all for id from the queue in one commit -ggolden
			}
			catch (InUseException e)
			{
				log.warn("digest in use, will try send again at next digest attempt: " + e.getMessage());
				// retry next time
				retry.add(message);
			}
		}

		// requeue the retrys
		if (retry.size() > 0)
		{
			synchronized (m_digestQueue)
			{
				m_digestQueue.addAll(retry);
			}
		}
	}

	/**
	 * If it's time, send out any digested messages. Send once daily, after a certiain time of day (local time).
	 */
	protected void sendDigests()
	{
		if (log.isDebugEnabled()) log.debug("checking for sending digests");

		// compute the current period
		String curPeriod = computeRange(timeService.newTime()).toString();

		// if we are in a new period, start sending again
		if (!curPeriod.equals(m_lastSendPeriod) || m_debugBypass)
		{
			m_sendDigests = true;

			// remember this period for next check
			m_lastSendPeriod = curPeriod;
		}

		// if we are not sending, early out
		if (!m_sendDigests) return;

		log.info("Preparing to send the mail digests for "+curPeriod);

		// count send candidate digests
		int count = 0;

		// process each digest
		List digests = getDigests();
		for (Iterator iDigests = digests.iterator(); iDigests.hasNext();)
		{
			Digest digest = (Digest) iDigests.next();

			// see if this one has any prior periods
			List periods = digest.getPeriods();
			if (periods.size() == 0) continue;

			boolean found = false;
			for (Iterator iPeriods = periods.iterator(); iPeriods.hasNext();)
			{
				String period = (String) iPeriods.next();
				if (!curPeriod.equals(period) || m_debugBypass)
				{
					found = true;
					break;
				}
			}
			if (!found) {
				continue;
			}

			// this digest is a send candidate
			count++;

			// get a lock
			DigestEdit edit = null;
			try
			{
				boolean changed = false;
				edit = edit(digest.getId());

				// process each non-current period
				for (Iterator iPeriods = edit.getPeriods().iterator(); iPeriods.hasNext();)
				{
					String period = (String) iPeriods.next();

					// process if it's not the current period
					if (!curPeriod.equals(period) || m_debugBypass)
					{
						TimeRange periodRange = timeService.newTimeRange(period);
						Time timeInPeriod = periodRange.firstTime();

						// any messages?
						List msgs = edit.getMessages(timeInPeriod);
						if (msgs.size() > 0)
						{
							// send this one
							send(edit.getId(), msgs, periodRange);
						}

						// clear this period
						edit.clear(timeInPeriod);

						changed = true;
					}
				}

				// commit, release the lock
				if (changed)
				{
					// delete it if empty
					if (edit.getPeriods().size() == 0)
					{
						remove(edit);
					}
					else
					{
						commit(edit);
					}
					edit = null;
				}
				else
				{
					cancel(edit);
					edit = null;
				}
			}
			// if in use, missing, whatever, skip on
			catch (Exception any)
			{
			}
			finally
			{
				if (edit != null)
				{
					cancel(edit);
					edit = null;
				}
			}

		} // for (Iterator iDigests = digests.iterator(); iDigests.hasNext();)

		// if we didn't see any send candidates, we will stop sending till next period
		if (count == 0)
		{
			m_sendDigests = false;
		}
	}

	/**
	 * Send a single digest message
	 * 
	 * @param id
	 *        The use id to send the message to.
	 * @param msgs
	 *        The List (DigestMessage) of message to digest.
	 * @param period
	 *        The time period of the digested messages.
	 */
	protected void send(String id, List msgs, TimeRange period)
	{
		// sanity check
		if (msgs.size() == 0) return;

		try
		{
			String to = userDirectoryService.getUser(id).getEmail();

			// if use has no email address we can't send it
			if ((to == null) || (to.length() == 0)) return;

			String from = "postmaster@" + serverConfigurationService.getServerName();
			String subject = serverConfigurationService.getString("ui.service", "Sakai") + " " + rb.getString("notif") + " "
			+ period.firstTime().toStringLocalDate();

			StringBuilder body = new StringBuilder();
			body.append(subject);
			body.append("\n\n");

			// toc
			int count = 1;
			for (Iterator iMsgs = msgs.iterator(); iMsgs.hasNext();)
			{
				DigestMessage msg = (DigestMessage) iMsgs.next();

				body.append(Integer.toString(count));
				body.append(".  ");
				body.append(msg.getSubject());
				body.append("\n");
				count++;
			}
			body.append("\n----------------------\n\n");

			// for each msg
			count = 1;
			for (Iterator iMsgs = msgs.iterator(); iMsgs.hasNext();)
			{
				DigestMessage msg = (DigestMessage) iMsgs.next();

				// repeate toc entry
				body.append(Integer.toString(count));
				body.append(".  ");
				body.append(msg.getSubject());
				body.append("\n\n");

				// message body
				body.append(msg.getBody());

				body.append("\n----------------------\n\n");
				count++;
			}

			// tag
			body.append(rb.getString("thiaut") + " " + serverConfigurationService.getString("ui.service", "Sakai") + " " + "("
					+ serverConfigurationService.getServerUrl() + ")" + "\n" + rb.getString("youcan") + "\n");

			if (log.isDebugEnabled()) log.debug(this + " sending digest email to: " + to);

			emailService.send(from, to, subject, body.toString(), to, null, null);
		}
		catch (Exception any)
		{
			log.warn(".send: digest to: " + id + " not sent: " + any.toString());
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Abstractions, etc.
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Construct storage for this service.
	 */
	protected abstract Storage newStorage();

	/**
	 * Access the partial URL that forms the root of resource URLs.
	 * 
	 * @param relative
	 *        if true, form within the access path only (i.e. starting with /content)
	 * @return the partial URL that forms the root of resource URLs.
	 */
	protected String getAccessPoint(boolean relative)
	{
		return (relative ? "" : serverConfigurationService.getAccessUrl()) + m_relativeAccessPoint;
	}

	/**
	 * Access the internal reference which can be used to access the resource from within the system.
	 * 
	 * @param id
	 *        The digest id string.
	 * @return The the internal reference which can be used to access the resource from within the system.
	 */
	public String digestReference(String id)
	{
		return getAccessPoint(true) + Entity.SEPARATOR + id;
	}

	/**
	 * Access the digest id extracted from a digest reference.
	 * 
	 * @param ref
	 *        The digest reference string.
	 * @return The the digest id extracted from a digest reference.
	 */
	protected String digestId(String ref)
	{
		String start = getAccessPoint(true) + Entity.SEPARATOR;
		int i = ref.indexOf(start);
		if (i == -1) return ref;
		String id = ref.substring(i + start.length());
		return id;
	}

	/**
	 * Check security permission.
	 * 
	 * @param lock
	 *        The lock id string.
	 * @param resource
	 *        The resource reference string, or null if no resource is involved.
	 * @return true if allowd, false if not
	 */
	protected boolean unlockCheck(String lock, String resource)
	{
		if (!securityService.unlock(lock, resource))
		{
			return false;
		}

		return true;
	}

	/**
	 * Check security permission.
	 * 
	 * @param lock
	 *        The lock id string.
	 * @param resource
	 *        The resource reference string, or null if no resource is involved.
	 * @exception PermissionException
	 *            Thrown if the user does not have access
	 */
	protected void unlock(String lock, String resource) throws PermissionException
	{
		if (!unlockCheck(lock, resource))
		{
			throw new PermissionException(sessionManager.getCurrentSessionUserId(), lock, resource);
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies
	 *********************************************************************************************************************************************************************************************************************************************************/

	protected TimeService timeService;
	protected ServerConfigurationService serverConfigurationService;
	protected EmailService emailService;
	protected EventTrackingService eventTrackingService;
	protected SecurityService securityService;
	protected UserDirectoryService userDirectoryService;
	protected SessionManager sessionManager;

	/**
	 * @return the TimeService collaborator.
	 */
	public void setTimeService(TimeService timeService)
	{
		this.timeService = timeService;
	}

	/**
	 * @return the ServerConfigurationService collaborator.
	 */
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService)
	{
		this.serverConfigurationService = serverConfigurationService;
	}

	/**
	 * @return the EmailService collaborator.
	 */
	public void setEmailService(EmailService emailService)
	{
		this.emailService = emailService;
	}

	/**
	 * @return the EventTrackingService collaborator.
	 */
	public void setEventTrackingService(EventTrackingService eventTrackingService)
	{
		this.eventTrackingService = eventTrackingService;
	}

	/**
	 * @return the MemoryServiSecurityServicece collaborator.
	 */
	public void setSecurityService(SecurityService securityService)
	{
		this.securityService = securityService;
	}

	/**
	 * @return the UserDirectoryService collaborator.
	 */
	public void setUserDirectoryService(UserDirectoryService userDirectoryService)
	{
		this.userDirectoryService = userDirectoryService;
	}

	/**
	 * @return the SessionManager collaborator.
	 */
	public void setSessionManager(SessionManager sessionManager)
	{
		this.sessionManager = sessionManager;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		m_relativeAccessPoint = REFERENCE_ROOT;

		// construct storage and read
		m_storage = newStorage();
		m_storage.open();

		// setup the queue
		m_digestQueue.clear();

		// Resource Bundle
		String resourceClass = serverConfigurationService.getString(RESOURCECLASS, DEFAULT_RESOURCECLASS);
		String resourceBundle = serverConfigurationService.getString(RESOURCEBUNDLE, DEFAULT_RESOURCEBUNDLE);
		rb = new Resource().getLoader(resourceClass, resourceBundle);

		// USE A TIMER INSTEAD OF CREATING A NEW THREAD -AZ
		// start();
		int digestPeriod = serverConfigurationService.getInt(EMAIL_DIGEST_CHECK_PERIOD_PROPERTY, DIGEST_PERIOD);
		int digestDelay = serverConfigurationService.getInt(EMAIL_DIGEST_START_DELAY_PROPERTY, DIGEST_DELAY);
		m_debugBypass = serverConfigurationService.getBoolean(BY_PASS_FOR_DEBUG, false);
		digestDelay += new Random().nextInt(60); // add some random delay to get the servers out of sync
		digestTimer.schedule(new DigestTimerTask(), (digestDelay * 1000), (digestPeriod * 1000) );

		log.info("init(): email digests will be checked in " + digestDelay + " seconds and then every " 
				+ digestPeriod + " seconds while the server is running" );
	}

	/**
	 * This timer task is run by the timer thread based on the period set above
	 * 
	 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
	 */
	private class DigestTimerTask extends TimerTask {
		@Override
		public void run() {
			try {
				log.debug("running timer task");
				// process the queue of digest requests
				processQueue();
				// check for a digest mailing time
				sendDigests();
			} catch (Exception e) {
				log.error("Digest failure: " + e.getMessage(), e);
			}
		}
	}

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		//		stop();
		digestTimer.cancel();

		m_storage.close();
		m_storage = null;

		if (m_digestQueue.size() > 0)
		{
			log.warn(".shutdown: with items in digest queue"); // %%%
		}
		m_digestQueue.clear();

		log.info("destroy()");
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * DigestService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @inheritDoc
	 */
	public Digest getDigest(String id) throws IdUnusedException
	{
		Digest digest = findDigest(id);
		if (digest == null) throw new IdUnusedException(id);

		return digest;
	}

	/**
	 * @inheritDoc
	 */
	public List getDigests()
	{
		List digests = m_storage.getAll();

		return digests;
	}

	/**
	 * @inheritDoc
	 */
	public void digest(String to, String subject, String body)
	{
		DigestMessage message = new org.sakaiproject.email.impl.DigestMessage(to, subject, body);

		// queue this for digesting
		synchronized (m_digestQueue)
		{
			m_digestQueue.add(message);
		}
	}

	/**
	 * @inheritDoc
	 */
	public DigestEdit edit(String id) throws InUseException
	{
		// security
		// unlock(SECURE_EDIT_DIGEST, digestReference(id));

		// one add/edit at a time, please, to make sync. only one digest per user
		// TODO: I don't link sync... could just do the add and let it fail if it already exists -ggolden
		synchronized (m_storage)
		{
			// check for existance
			if (!m_storage.check(id))
			{
				try
				{
					return add(id);
				}
				catch (IdUsedException e)
				{
					log.warn(".edit: from the add: " + e);
				}
			}

			// ignore the cache - get the user with a lock from the info store
			DigestEdit edit = m_storage.edit(id);
			if (edit == null) throw new InUseException(id);

			((BaseDigest) edit).setEvent(SECURE_EDIT_DIGEST);

			return edit;
		}
	}

	/**
	 * @inheritDoc
	 */
	public void commit(DigestEdit edit)
	{
		// check for closed edit
		if (!edit.isActiveEdit())
		{
			try
			{
				throw new Exception();
			}
			catch (Exception e)
			{
				log.warn(".commit(): closed DigestEdit", e);
			}
			return;
		}

		// update the properties
		// addLiveUpdateProperties(user.getPropertiesEdit());

		// complete the edit
		m_storage.commit(edit);

		// track it
		eventTrackingService.post(eventTrackingService.newEvent(((BaseDigest) edit).getEvent(), edit.getReference(), true));

		// close the edit object
		((BaseDigest) edit).closeEdit();
	}

	/**
	 * @inheritDoc
	 */
	public void cancel(DigestEdit edit)
	{
		// check for closed edit
		if (!edit.isActiveEdit())
		{
			try
			{
				throw new Exception();
			}
			catch (Exception e)
			{
				log.warn(".cancel(): closed DigestEdit", e);
			}
			return;
		}

		// release the edit lock
		m_storage.cancel(edit);

		// close the edit object
		((BaseDigest) edit).closeEdit();
	}

	/**
	 * @inheritDoc
	 */
	public void remove(DigestEdit edit)
	{
		// check for closed edit
		if (!edit.isActiveEdit())
		{
			try
			{
				throw new Exception();
			}
			catch (Exception e)
			{
				log.warn(".remove(): closed DigestEdit", e);
			}
			return;
		}

		// complete the edit
		m_storage.remove(edit);

		// track it
		eventTrackingService.post(eventTrackingService.newEvent(SECURE_REMOVE_DIGEST, edit.getReference(), true));

		// close the edit object
		((BaseDigest) edit).closeEdit();
	}

	/**
	 * @inheritDoc
	 */
	protected BaseDigest findDigest(String id)
	{
		BaseDigest digest = (BaseDigest) m_storage.get(id);

		return digest;
	}

	/**
	 * @inheritDoc
	 */
	public DigestEdit add(String id) throws IdUsedException
	{
		// check security (throws if not permitted)
		// unlock(SECURE_ADD_DIGEST, digestReference(id));

		// one add/edit at a time, please, to make sync. only one digest per user
		synchronized (m_storage)
		{
			// reserve a user with this id from the info store - if it's in use, this will return null
			DigestEdit edit = m_storage.put(id);
			if (edit == null)
			{
				throw new IdUsedException(id);
			}

			return edit;
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Digest implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class BaseDigest implements DigestEdit, SessionBindingListener
	{
		/** The user id. */
		protected String m_id = null;

		/** The properties. */
		protected ResourcePropertiesEdit m_properties = null;

		/** The digest time ranges (Map TimeRange string to List of DigestMessage). */
		protected Map m_ranges = null;

		/**
		 * Construct.
		 * 
		 * @param id
		 *        The user id.
		 */
		public BaseDigest(String id)
		{
			m_id = id;

			// setup for properties
			ResourcePropertiesEdit props = new BaseResourcePropertiesEdit();
			m_properties = props;

			// setup for ranges
			m_ranges = new Hashtable();

			// if the id is not null (a new user, rather than a reconstruction)
			// and not the anon (id == "") user,
			// add the automatic (live) properties
			// %%% if ((m_id != null) && (m_id.length() > 0)) addLiveProperties(props);
		}

		/**
		 * Construct from another Digest object.
		 * 
		 * @param user
		 *        The user object to use for values.
		 */
		public BaseDigest(Digest digest)
		{
			setAll(digest);
		}

		/**
		 * Construct from information in XML.
		 * 
		 * @param el
		 *        The XML DOM Element definining the user.
		 */
		public BaseDigest(Element el)
		{
			// setup for properties
			m_properties = new BaseResourcePropertiesEdit();

			// setup for ranges
			m_ranges = new Hashtable();

			m_id = el.getAttribute("id");

			// the children (properties, messages)
			NodeList children = el.getChildNodes();
			final int length = children.getLength();
			for (int i = 0; i < length; i++)
			{
				Node child = children.item(i);
				if (child.getNodeType() != Node.ELEMENT_NODE) continue;
				Element element = (Element) child;

				// look for properties
				if (element.getTagName().equals("properties"))
				{
					// re-create properties
					m_properties = new BaseResourcePropertiesEdit(element);
				}

				// look for a messages
				else if (element.getTagName().equals("messages"))
				{
					String period = element.getAttribute("period");

					// find the range
					List msgs = (List) m_ranges.get(period);
					if (msgs == null)
					{
						msgs = new Vector();
						m_ranges.put(period, msgs);
					}

					// do these children for messages
					NodeList msgChildren = element.getChildNodes();
					final int msgChildrenLen = msgChildren.getLength();
					for (int m = 0; m < msgChildrenLen; m++)
					{
						Node msgChild = msgChildren.item(m);
						if (msgChild.getNodeType() != Node.ELEMENT_NODE) continue;
						Element msgChildEl = (Element) msgChild;

						if (msgChildEl.getTagName().equals("message"))
						{
							String subject = Xml.decodeAttribute(msgChildEl, "subject");
							String body = Xml.decodeAttribute(msgChildEl, "body");
							msgs.add(new org.sakaiproject.email.impl.DigestMessage(m_id, subject, body));
						}
					}
				}
			}
		}

		/**
		 * Take all values from this object.
		 * 
		 * @param user
		 *        The user object to take values from.
		 */
		protected void setAll(Digest digest)
		{
			m_id = digest.getId();

			m_properties = new BaseResourcePropertiesEdit();
			m_properties.addAll(digest.getProperties());

			m_ranges = new Hashtable();
			// %%% deep enough? -ggolden
			m_ranges.putAll(((BaseDigest) digest).m_ranges);
		}

		/**
		 * @inheritDoc
		 */
		public Element toXml(Document doc, Stack stack)
		{
			Element digest = doc.createElement("digest");

			if (stack.isEmpty())
			{
				doc.appendChild(digest);
			}
			else
			{
				((Element) stack.peek()).appendChild(digest);
			}

			stack.push(digest);

			digest.setAttribute("id", getId());

			// properties
			m_properties.toXml(doc, stack);

			// for each message range
			for (Iterator it = m_ranges.entrySet().iterator(); it.hasNext();)
			{
				Map.Entry entry = (Map.Entry) it.next();

				Element messages = doc.createElement("messages");
				digest.appendChild(messages);
				messages.setAttribute("period", (String) entry.getKey());

				// for each message
				for (Iterator iMsgs = ((List) entry.getValue()).iterator(); iMsgs.hasNext();)
				{
					DigestMessage msg = (DigestMessage) iMsgs.next();

					Element message = doc.createElement("message");
					messages.appendChild(message);
					Xml.encodeAttribute(message, "subject", msg.getSubject());
					Xml.encodeAttribute(message, "body", msg.getBody());
				}
			}

			stack.pop();

			return digest;
		}

		/**
		 * @inheritDoc
		 */
		public String getId()
		{
			if (m_id == null) return "";
			return m_id;
		}

		/**
		 * @inheritDoc
		 */
		public String getUrl()
		{
			return getAccessPoint(false) + m_id;
		}

		/**
		 * @inheritDoc
		 */
		public String getReference()
		{
			return digestReference(m_id);
		}

		/**
		 * @inheritDoc
		 */
		public String getReference(String rootProperty)
		{
			return getReference();
		}

		/**
		 * @inheritDoc
		 */
		public String getUrl(String rootProperty)
		{
			return getUrl();
		}

		/**
		 * @inheritDoc
		 */
		public ResourceProperties getProperties()
		{
			return m_properties;
		}

		/**
		 * @inheritDoc
		 */
		public List getMessages(Time period)
		{
			synchronized (m_ranges)
			{
				// find the range
				String range = computeRange(period).toString();
				/* 
				 * http://jira.sakaiproject.org/jira/browse/SAK-11841
				 * If the current date/time gets out of sync with the stored date/time periods then
				 * messages will sit in the queue forever and cause looping in the code which will
				 * never resolve itself, to keep this from happening I am adding in an extra
				 * check as a stopgap which will do the reverse check in the case that nothing
				 * is retrieved, this really ugly and needs to be done a better way
				 * (which means a way that is not so fragile) -AZ
				 */
				List msgs = (List) m_ranges.get(range);
				if (msgs == null) {
					// nothing found so go through all ranges and hack the range strings
					SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
					for (Iterator<String> iterator = m_ranges.keySet().iterator(); iterator.hasNext();) {
						String rangeKey = (String) iterator.next();
						Date startDate;
						try {
							startDate = formatter.parse( range.substring(0, 12) );
							long difference = Math.abs( period.getTime() - startDate.getTime() );
							if (difference < (12 * 60 * 60 * 1000)) {
								// within 12 hours of the correct period so use this one
								msgs = (List) m_ranges.get(rangeKey);
								break;
							}
						} catch (ParseException e) {
							log.warn("Failed to parse first 12 chars from '"+rangeKey+"' into a date, aborting the attempt to find close data matches", e);
						}
					}
				}

				List rv = new Vector();
				if (msgs != null) {
					rv.addAll(msgs);
				}

				return rv;
			}
		}

		/**
		 * @inheritDoc
		 */
		public List getPeriods()
		{
			synchronized (m_ranges)
			{
				List rv = new Vector();
				rv.addAll(m_ranges.keySet());

				return rv;
			}
		}

		/**
		 * @inheritDoc
		 */
		public boolean equals(Object obj)
		{
			if (!(obj instanceof Digest)) return false;
			return ((Digest) obj).getId().equals(getId());
		}

		/**
		 * @inheritDoc
		 */
		public int hashCode()
		{
			return getId().hashCode();
		}

		/**
		 * @inheritDoc
		 */
		public int compareTo(Object obj)
		{
			if (!(obj instanceof Digest)) throw new ClassCastException();

			// if the object are the same, say so
			if (obj == this) return 0;

			// sort based on (unique) id
			int compare = getId().compareTo(((Digest) obj).getId());

			return compare;
		}

		/******************************************************************************************************************************************************************************************************************************************************
		 * Edit implementation
		 *****************************************************************************************************************************************************************************************************************************************************/

		/** The event code for this edit. */
		protected String m_event = null;

		/** Active flag. */
		protected boolean m_active = false;

		/**
		 * @inheritDoc
		 */
		public void add(DigestMessage msg)
		{
			synchronized (m_ranges)
			{
				// find the current range
				String range = computeRange(timeService.newTime()).toString();
				List msgs = (List) m_ranges.get(range);
				if (msgs == null)
				{
					msgs = new Vector();
					m_ranges.put(range, msgs);
				}
				msgs.add(msg);
			}
		}

		/**
		 * @inheritDoc
		 */
		public void add(String to, String subject, String body)
		{
			DigestMessage msg = new org.sakaiproject.email.impl.DigestMessage(to, subject, body);

			synchronized (m_ranges)
			{
				// find the current range
				String range = computeRange(timeService.newTime()).toString();
				List msgs = (List) m_ranges.get(range);
				if (msgs == null)
				{
					msgs = new Vector();
					m_ranges.put(range, msgs);
				}
				msgs.add(msg);
			}
		}

		/**
		 * @inheritDoc
		 */
		public void clear(Time period)
		{
			synchronized (m_ranges)
			{
				// find the range
				String range = computeRange(period).toString();
				List msgs = (List) m_ranges.get(range);
				if (msgs != null)
				{
					m_ranges.remove(range);
				}
			}
		}

		/**
		 * Clean up.
		 */
		protected void finalize()
		{
			// catch the case where an edit was made but never resolved
			if (m_active)
			{
				cancel(this);
			}
		}

		/**
		 * Take all values from this object.
		 * 
		 * @param user
		 *        The user object to take values from.
		 */
		protected void set(Digest digest)
		{
			setAll(digest);
		}

		/**
		 * Access the event code for this edit.
		 * 
		 * @return The event code for this edit.
		 */
		protected String getEvent()
		{
			return m_event;
		}

		/**
		 * Set the event code for this edit.
		 * 
		 * @param event
		 *        The event code for this edit.
		 */
		protected void setEvent(String event)
		{
			m_event = event;
		}

		/**
		 * @inheritDoc
		 */
		public ResourcePropertiesEdit getPropertiesEdit()
		{
			return m_properties;
		}

		/**
		 * Enable editing.
		 */
		protected void activate()
		{
			m_active = true;
		}

		/**
		 * @inheritDoc
		 */
		public boolean isActiveEdit()
		{
			return m_active;
		}

		/**
		 * Close the edit object - it cannot be used after this.
		 */
		protected void closeEdit()
		{
			m_active = false;
		}

		/******************************************************************************************************************************************************************************************************************************************************
		 * SessionBindingListener implementation
		 *****************************************************************************************************************************************************************************************************************************************************/

		/**
		 * @inheritDoc
		 */
		public void valueBound(SessionBindingEvent event)
		{
		}

		/**
		 * @inheritDoc
		 */
		public void valueUnbound(SessionBindingEvent event)
		{
			if (log.isDebugEnabled()) log.debug(this + ".valueUnbound()");

			// catch the case where an edit was made but never resolved
			if (m_active)
			{
				cancel(this);
			}
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Storage
	 *********************************************************************************************************************************************************************************************************************************************************/

	protected interface Storage
	{
		/**
		 * Open.
		 */
		public void open();

		/**
		 * Close.
		 */
		public void close();

		/**
		 * Check if a digest by this id exists.
		 * 
		 * @param id
		 *        The user id.
		 * @return true if a digest for this id exists, false if not.
		 */
		public boolean check(String id);

		/**
		 * Get the digest with this id, or null if not found.
		 * 
		 * @param id
		 *        The digest id.
		 * @return The digest with this id, or null if not found.
		 */
		public Digest get(String id);

		/**
		 * Get all digests.
		 * 
		 * @return The list of all digests.
		 */
		public List getAll();

		/**
		 * Add a new digest with this id.
		 * 
		 * @param id
		 *        The digest id.
		 * @return The locked Digest object with this id, or null if the id is in use.
		 */
		public DigestEdit put(String id);

		/**
		 * Get a lock on the digest with this id, or null if a lock cannot be gotten.
		 * 
		 * @param id
		 *        The digest id.
		 * @return The locked Digest with this id, or null if this records cannot be locked.
		 */
		public DigestEdit edit(String id);

		/**
		 * Commit the changes and release the lock.
		 * 
		 * @param user
		 *        The edit to commit.
		 */
		public void commit(DigestEdit edit);

		/**
		 * Cancel the changes and release the lock.
		 * 
		 * @param user
		 *        The edit to commit.
		 */
		public void cancel(DigestEdit edit);

		/**
		 * Remove this edit and release the lock.
		 * 
		 * @param user
		 *        The edit to remove.
		 */
		public void remove(DigestEdit edit);
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * StorageUser implementation (no container)
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @inheritDoc
	 */
	public Entity newResource(Entity container, String id, Object[] others)
	{
		return new BaseDigest(id);
	}

	/**
	 * @inheritDoc
	 */
	public Entity newResource(Entity container, Element element)
	{
		return new BaseDigest(element);
	}

	/**
	 * @inheritDoc
	 */
	public Entity newResource(Entity container, Entity other)
	{
		return new BaseDigest((Digest) other);
	}

	/**
	 * @inheritDoc
	 */
	public Edit newResourceEdit(Entity container, String id, Object[] others)
	{
		BaseDigest e = new BaseDigest(id);
		e.activate();
		return e;
	}

	/**
	 * @inheritDoc
	 */
	public Edit newResourceEdit(Entity container, Element element)
	{
		BaseDigest e = new BaseDigest(element);
		e.activate();
		return e;
	}

	/**
	 * @inheritDoc
	 */
	public Edit newResourceEdit(Entity container, Entity other)
	{
		BaseDigest e = new BaseDigest((Digest) other);
		e.activate();
		return e;
	}

	/**
	 * @inheritDoc
	 */
	public Object[] storageFields(Entity r)
	{
		return null;
	}

	/**
	 * Compute a time range based on a specific time.
	 * 
	 * @return The time range that encloses the specific time.
	 */
	protected TimeRange computeRange(Time time)
	{
		// set the period to "today" (local!) from day start to next day start, not end inclusive
		TimeBreakdown brk = time.breakdownLocal();
		brk.setMs(0);
		brk.setSec(0);
		brk.setMin(0);
		brk.setHour(0);
		Time start = timeService.newTimeLocal(brk);
		Time end = timeService.newTime(start.getTime() + 24 * 60 * 60 * 1000);
		return timeService.newTimeRange(start, end, true, false);
	}

}
