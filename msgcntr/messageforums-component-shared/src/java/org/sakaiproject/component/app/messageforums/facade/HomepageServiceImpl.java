/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.component.app.messageforums.facade;

import org.sakaiproject.api.app.messageforums.MessageForumsUser;
import org.sakaiproject.api.app.messageforums.facade.DiscussionForumsService;
import org.sakaiproject.api.app.messageforums.facade.HomepageService;
import org.sakaiproject.api.app.messageforums.facade.OpenForumsService;
import org.sakaiproject.api.app.messageforums.facade.PrivateMessageService;
import org.sakaiproject.component.app.messageforums.view.DiscussionForums;
import org.sakaiproject.component.app.messageforums.view.OpenForums;
import org.sakaiproject.component.app.messageforums.view.PrivateMessages;

public class HomepageServiceImpl implements HomepageService {

    private PrivateMessageService privateMessageService;
    private DiscussionForumsService discussionForumsService;
    private OpenForumsService openForumsService;

    
    public PrivateMessageService getPrivateMessageService() {
        return privateMessageService;
    }

    public DiscussionForumsService getDiscussionForumsService() {
        return discussionForumsService;
    }

    public OpenForumsService getOpenForumsService() {
        return openForumsService;
    }
    
    // Setters are just used for injection: not in the interface
    public void setDiscussionForumsService(DiscussionForumsService discussionForumsService) {
        this.discussionForumsService = discussionForumsService;
    }

    public void setOpenForumsService(OpenForumsService openForumsService) {
        this.openForumsService = openForumsService;
    }

    public void setPrivateMessageService(PrivateMessageService privateMessageService) {
        this.privateMessageService = privateMessageService;
    }
    
    // Helpers for creating the Homepage view
    public PrivateMessages preparePrivateMessages(MessageForumsUser user) {
        return null;
    }

    public DiscussionForums prepareDiscussionForums(MessageForumsUser user) {
        return null;
    }

    public OpenForums prepareOpenForums(MessageForumsUser user) {
        return null;
    }    

}
