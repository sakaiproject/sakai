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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A session-scoped bean to handle jsf messages across redirects.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
@SuppressWarnings("unchecked")
public class MessagingBean {
    private List messages;
	
	public MessagingBean() {
		messages = new ArrayList();
	}

    public boolean hasMessages() {
        return messages.size() > 0;
    }

    /**
     * Returns the current list of FacesMessages, then removes them from the local list.
     * @return list of MessageDecorator
     */
    public List getMessagesAndClear() {
        List list = new ArrayList();
        for(Iterator iter = messages.iterator(); iter.hasNext();) {
        	list.add(((MessageDecorator)iter.next()).getMessage());
        }
        messages.clear();
        return list;
    }
    
    /**
     * Adds a unique message.
     * 
     * @param message
     */
    public void addMessage(FacesMessage message) {
    	// Don't add the message twice (in case of double-clicks).  Somewhat related to SAK-3553
    	MessageDecorator decoratedMessage = new MessageDecorator(message);
    	if(!messages.contains(decoratedMessage)) {
            messages.add(decoratedMessage);
    	}
    }
    
    /**
     * Used in place of standard FacesMessage.  Overrides equals so we can add
     * unique messages and avoid duplicates.
     * 
     * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
     *
     */
    private static class MessageDecorator implements Serializable {
		private static final long serialVersionUID = 1L;

		FacesMessage message;

    	public MessageDecorator(FacesMessage message) {
            if (message == null) {
                throw new IllegalArgumentException("Cannot create a message decorator with no message");
            }
    		this.message = message;
    	}

    	public FacesMessage getMessage() {
            if (message == null) {
                throw new IllegalArgumentException("Cannot create a message decorator with no message");
            }
    		return message;
    	}
    	
		public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            MessageDecorator other = (MessageDecorator) obj;
			return new EqualsBuilder()
				.append(getSeverity(), other.getSeverity())
				.append(getDetail(), other.getDetail())
				.append(getSummary(), other.getSummary())
				.isEquals();
		}

		public int hashCode() {
			return new HashCodeBuilder(17, 37)
				.append(getSeverity())
				.append(getDetail())
				.append(getSummary())
				.toHashCode();
		}

        public String getDetail() {
			return message.getDetail();
		}

		public Severity getSeverity() {
			return message.getSeverity();
		}

		public String getSummary() {
			return message.getSummary();
		}

		public void setDetail(String arg0) {
			message.setDetail(arg0);
		}

		public void setSeverity(Severity arg0) {
			message.setSeverity(arg0);
		}

		public void setSummary(String arg0) {
			message.setSummary(arg0);
		}

		public String toString() {
			return message.toString();
		}
    	
    	
    }
}


