/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/

package org.sakaiproject.jsf.app;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

/**
 * <p>
 * MessageSaver has utility methods to save FacesMessage objects from one request to the next request (the messages are saved attributes of the current HttpSession).
 * </p>
 * 
 * @author University of Michigan, Sakai Software Development Team
 * @version $Revision$
 */
public class MessageSaver
{
	/** The session attribute name used to store the latest batch of messages. */
	public static final String ATTR_MSGS = "org.sakaiproject.jsf.messages";

	/**
	 * Restore saved messages.
	 * 
	 * @param context
	 *        The current faces context.
	 */
	public static void restoreMessages(FacesContext context)
	{
		if (context == null) return;

		// look in the session
		HttpSession s = (HttpSession) context.getExternalContext().getSession(false);
		if (s == null) return;

		// get messages
		List msgs = (List) s.getAttribute(ATTR_MSGS);
		if (msgs != null)
		{
			// process each one - add it to this context's message set
			for (Iterator iMessages = msgs.iterator(); iMessages.hasNext();)
			{
				FacesMessage msg = (FacesMessage) iMessages.next();
				// Note: attributed to no specific tree element
				context.addMessage(null, msg);
			}

			s.removeAttribute(ATTR_MSGS);
		}
	}

	/**
	 * Save current messages for later restoration.
	 * 
	 * @param context
	 *        The current faces context.
	 */
	public static void saveMessages(FacesContext context)
	{
		if (context == null) return;

		// look in the session
		HttpSession s = (HttpSession) context.getExternalContext().getSession(false);
		if (s == null) return;

		// collect the messages from the context for restoration on the next rendering
		List msgs = new Vector();
		for (Iterator iMessages = context.getMessages(); iMessages.hasNext();)
		{
			FacesMessage msg = (FacesMessage) iMessages.next();
			msgs.add(msg);
		}

		// store the messages for this mode to find
		s.setAttribute(ATTR_MSGS, msgs);
	}
}



