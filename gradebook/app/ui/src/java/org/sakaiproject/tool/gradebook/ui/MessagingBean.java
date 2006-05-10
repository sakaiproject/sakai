/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
*
* Licensed under the Educational Community License, Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook.ui;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;

/**
 * A session-scoped bean to handle jsf messages across redirects.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class MessagingBean {
	private List messages = new ArrayList();

    public boolean hasMessages() {
        return messages.size() > 0;
    }

    public List getMessagesAndClear() {
        List list = new ArrayList(messages);
        messages.clear();
        return list;
    }

    public void addMessage(FacesMessage message) {
        messages.add(message);
    }
}



