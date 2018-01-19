/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-hbm/src/java/org/sakaiproject/component/app/messageforums/dao/hibernate/PrivateMessageImpl.java $
 * $Id: PrivateMessageImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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
package org.sakaiproject.component.app.messageforums.dao.hibernate;

import java.util.Comparator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.app.messageforums.MessageForumsUser;
import org.sakaiproject.api.app.messageforums.PrivateMessage;

@Slf4j
public class PrivateMessageImpl extends MessageImpl implements PrivateMessage {

    private List recipients = null;//new UniqueArrayList();  addRecipient(MessageForumsUser user)
    private Boolean externalEmail;
    private String externalEmailAddress;
    private String recipientsAsText;
    private String recipientsAsTextBcc;
    
    public static Comparator RECIPIENT_LIST_COMPARATOR_ASC;
    public static Comparator RECIPIENT_LIST_COMPARATOR_DESC;
    
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
		
	public String getRecipientsAsTextBcc() {
		return recipientsAsTextBcc;
	}

	public void setRecipientsAsTextBcc(String recipientsAsTextBcc) {
		this.recipientsAsTextBcc = recipientsAsTextBcc;
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
        if (log.isDebugEnabled()) {
            log.debug("addRecipient(MessageForumsUser " + user + ")");
        }
        
        if (user == null) {
            throw new IllegalArgumentException("user == null");
        }
        
        user.setPrivateMessage(this);
        recipients.add(user);
    }

    public void removeRecipient(MessageForumsUser user) {
        if (log.isDebugEnabled()) {
            log.debug("removeRecipient(MessageForumsUser " + user + ")");
        }
        
        if (user == null) {
            throw new IllegalArgumentException("Illegal attachment argument passed!");
        }
        
        user.setPrivateMessage(null);
        recipients.remove(user);
    }	
    
    // SORT BY RECIPIENT
    static
    {
    	RECIPIENT_LIST_COMPARATOR_ASC = new Comparator()
    	{
    		public int compare(Object pvtMsg, Object otherPvtMsg)
    		{
    			if (pvtMsg != null && otherPvtMsg != null
    					&& pvtMsg instanceof PrivateMessage && otherPvtMsg instanceof PrivateMessage)
    			{
    				String msg1 = ((PrivateMessage) pvtMsg).getRecipientsAsText().toLowerCase();
    				String msg2 = ((PrivateMessage) otherPvtMsg).getRecipientsAsText().toLowerCase();
    				return msg1.compareTo(msg2);
    			}
    			return -1;

    		}
    	};

    	RECIPIENT_LIST_COMPARATOR_DESC = new Comparator()
    	{
    		public int compare(Object pvtMsg, Object otherPvtMsg)
    		{
    			if (pvtMsg != null && otherPvtMsg != null
    					&& pvtMsg instanceof PrivateMessage && otherPvtMsg instanceof PrivateMessage)
    			{
    				String msg1 = ((PrivateMessage) pvtMsg).getRecipientsAsText().toLowerCase();
    				String msg2 = ((PrivateMessage) otherPvtMsg).getRecipientsAsText().toLowerCase();
    				return msg2.compareTo(msg1);
    			}
    			return -1;

    		}
    	};
    }
}
