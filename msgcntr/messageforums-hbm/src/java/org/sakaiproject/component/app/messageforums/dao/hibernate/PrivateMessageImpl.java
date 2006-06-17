/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-hbm/src/java/org/sakaiproject/component/app/messageforums/dao/hibernate/PrivateMessageImpl.java $
 * $Id: PrivateMessageImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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
package org.sakaiproject.component.app.messageforums.dao.hibernate;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.MessageForumsUser;
import org.sakaiproject.api.app.messageforums.PrivateMessage;

public class PrivateMessageImpl extends MessageImpl implements PrivateMessage {

    private static final Log LOG = LogFactory.getLog(PrivateMessageImpl.class);
    
    private List recipients = null;//new UniqueArrayList();
    private Boolean externalEmail;
    private String externalEmailAddress;
    private String recipientsAsText;
    
    // indecies for hibernate
    //private int tindex;   

    public Boolean getExternalEmail() {
        return externalEmail;
    }

    public void setExternalEmail(Boolean externalEmail) {
        this.externalEmail = externalEmail;
    }

    public String getExternalEmailAddress() {
        return externalEmailAddress;
    }

    public void setExternalEmailAddress(String externalEmailAddress) {
        this.externalEmailAddress = externalEmailAddress;
    }

    public List getRecipients() {
        return recipients;
    }

    public void setRecipients(List recipients) {
        this.recipients = recipients;
    }
    
    public String getRecipientsAsText() {
			return recipientsAsText;
		}

		public void setRecipientsAsText(String recipientsAsText) {
			this.recipientsAsText = recipientsAsText;
		}
        
//    public int getTindex() {
//        try {
//            return getTopic().getMessages().indexOf(this);
//        } catch (Exception e) {
//            return tindex;
//        }
//    }
//
//    public void setTindex(int tindex) {
//        this.tindex = tindex;        
//    }
    
    ////////////////////////////////////////////////////////////////////////
    // helper methods for collections
    ////////////////////////////////////////////////////////////////////////
    
    public void addRecipient(MessageForumsUser user) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addRecipient(MessageForumsUser " + user + ")");
        }
        
        if (user == null) {
            throw new IllegalArgumentException("user == null");
        }
        
        user.setPrivateMessage(this);
        recipients.add(user);
    }

    public void removeRecipient(MessageForumsUser user) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeRecipient(MessageForumsUser " + user + ")");
        }
        
        if (user == null) {
            throw new IllegalArgumentException("Illegal attachment argument passed!");
        }
        
        user.setPrivateMessage(null);
        recipients.remove(user);
    }		
}
