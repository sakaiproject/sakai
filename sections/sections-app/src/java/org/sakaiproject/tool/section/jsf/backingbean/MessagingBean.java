/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California and The Regents of the University of Michigan
*
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

package org.sakaiproject.tool.section.jsf.backingbean;

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
     * @return
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
    class MessageDecorator implements Serializable {
		private static final long serialVersionUID = 1L;

		FacesMessage message;

    	public MessageDecorator(FacesMessage message) {
    		this.message = message;
    	}

    	public FacesMessage getMessage() {
    		return message;
    	}
    	
		public boolean equals(Object o) {
			MessageDecorator other = (MessageDecorator)o;
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


