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
import java.util.Iterator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIMessage;
import javax.faces.context.FacesContext;

import lombok.extern.slf4j.Slf4j;

/**
 * Put the message in a "div" instead of a "span", allowing for much more
 * positioning control.
 */

@Slf4j
public class DivMessageRenderer extends DivMessageRendererBase {

    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
		// Note our complete disregard for class cast and null exceptions....
		UIMessage uiMessage = (UIMessage)component;
		String clientId = uiMessage.findComponent(uiMessage.getFor()).getClientId(context);

		Iterator iter = context.getMessages(clientId);
		if (iter.hasNext()) {
			// Just do the first one.
			FacesMessage message = (FacesMessage)iter.next();
			renderMessage(context, component, message);
		}
	}
}


