/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.tool.section.jsf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIMessages;
import javax.faces.context.FacesContext;

import lombok.extern.slf4j.Slf4j;

/**
 * Work around JSF 1.1's inadequate Messages renderer, which has the following problems:
 *
 *   - Contrary to the documentation, an HTML list is not used to render a list. Instead,
 *     the message texts are written out one after another.
 *
 *   - There's no way to define styles or classes for the containing HTML element.
 *
 *   - Each message is put into a "span" element rather than a "div". This means that
 *     important formatting options are not available to page designers.
 *
 * Of these, the worst problem seems the use of "span" instead of "div". Not many friendly
 * UIs will present messages to the user in a bulleted or numbered list, and since
 * each row only has one cell, the "table" capabilities aren't much different than one
 * would get from "div"s. So this overriding renderer uses "div" instead.
 *
 * TODO Implement "table" and "list", and the trimmings ("detail", "title", etc.) to
 * make this a full replacement.
 *
 * TODO Possibly add a TLD to make styling the container possible.
 *
 * To replace the JSF renderer with no further fuss, just paste this into faces-config.xml :
 *
 *	<render-kit>
 *		<renderer>
 *			<component-family>javax.faces.Messages</component-family>
 *			<renderer-type>javax.faces.Messages</renderer-type>
 *			<renderer-class>org.sakaiproject.tool.section.jsf.DivMessagesRenderer</renderer-class>
 *		</renderer>
 *	</render-kit>
 *
 * Notes added by JH below:
 *
 * This messages renderer will check in two places for faces messages:  in the
 * faces context, and in the session-scoped messagesBean.
 *
 */
@Slf4j
public class DivMessagesRenderer extends DivMessageRendererBase {

	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        if (!component.isRendered()) {
			return;
		}

        List allMessages = combineMessages(context, component);
		if (allMessages.size() == 0) {
			return;
		}

        for(Iterator messages = allMessages.iterator(); messages.hasNext();) {
			FacesMessage message = (FacesMessage)messages.next();
			renderMessage(context, component, message);
		}
	}

	/**
     * Combine the messages from the redirect-safe messages bean and those
     * stored in the faces context.  If there are messages associated with
     * individual components, but no global messages, then add a global message
     * pointing the user to check for component messages ('See messages below'
     * for instance).
	 *
     * @param context
	 * @param component
	 * @return
	 */
    private List combineMessages(FacesContext context, UIComponent component) {
        List redirectSafeMessages = ((MessagingBean)JsfUtil.resolveVariable("messagingBean")).getMessagesAndClear();
        List allMessages = new ArrayList(redirectSafeMessages);
        boolean globalOnly = ((UIMessages)component).isGlobalOnly();

        Iterator allFacesMessages = context.getMessages();
        Iterator globalFacesMessages = context.getMessages(null);

        Collection componentBoundMessages = getComponentBoundMessages(allFacesMessages, globalFacesMessages);
        Iterator facesMessages;
        if(globalOnly) {
            facesMessages = globalFacesMessages;
        } else {
            facesMessages = allFacesMessages;
        }

        // If this is a global only component, and there are no global messages,
        // and there are no redirect-safe messages (which are always global), and
        // there are component-bound messages, then add a global message telling
        // the user to look for the component-bound messages.
        if(globalOnly && redirectSafeMessages.size() == 0 && !globalFacesMessages.hasNext() && componentBoundMessages.size() != 0) {
            FacesMessage seeBelowMessage = new FacesMessage(FacesMessage.SEVERITY_WARN, JsfUtil.getLocalizedMessage("validation_messages_present"), null);
            allMessages.add(seeBelowMessage);
        }

        // We've already iterated over the facesMessage iterator, so we need to get them again so we can iterate again.
        // This is ugly... is there a better way to do this?
        if(globalOnly) {
            facesMessages = context.getMessages(null);
        } else {
            facesMessages = context.getMessages();
        }

        for(Iterator msgs = facesMessages; facesMessages.hasNext();) {
            allMessages.add(facesMessages.next());
        }
		return allMessages;
	}

	/**
     * Finds the non-global messages
     *
	 * @param allFacesMessages
	 * @param globalFacesMessages
	 * @return
	 */
	private Collection getComponentBoundMessages(Iterator allFacesMessages, Iterator globalFacesMessages) {
        List allFacesMessagesList = new ArrayList();
        for(Iterator msgs = allFacesMessages; msgs.hasNext();) {
            allFacesMessagesList.add(msgs.next());
        }

        List globalFacesMessagesList = new ArrayList();
        for(Iterator msgs = globalFacesMessages; msgs.hasNext();) {
            globalFacesMessagesList.add(msgs.next());
        }

        allFacesMessagesList.removeAll(globalFacesMessagesList);
        if(log.isDebugEnabled())log.debug(allFacesMessagesList.size() + " component bound messages");
        return allFacesMessagesList;
	}
}
