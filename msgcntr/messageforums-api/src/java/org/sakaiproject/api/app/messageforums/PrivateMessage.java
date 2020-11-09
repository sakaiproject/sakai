/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-api/src/java/org/sakaiproject/api/app/messageforums/PrivateMessage.java $
 * $Id: PrivateMessage.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.api.app.messageforums;

import java.util.List;
 
public interface PrivateMessage extends Message {
    
    /**
     * In the recipientsAsText field, this indicates the start of the
     * "hidden" recipients (ie those with privacy restrictions in the site)
     */
    public static final String HIDDEN_RECIPIENTS_START = "[";
    /**
     * In the recipientsAsText field, this indicates the end of the 
     * "hidden" recipients (ie those with privacy restrictions in the site)
     */
    public static final String HIDDEN_RECIPIENTS_END = "]";

    public Boolean getExternalEmail();

    public void setExternalEmail(Boolean externalEmail);

    public String getExternalEmailAddress();

    public void setExternalEmailAddress(String externalEmailAddress);

    public List<PrivateMessageRecipient> getRecipients();

    public void setRecipients(List<PrivateMessageRecipient> recipients);
    
    public String getRecipientsAsText() ;
    
    public void setRecipientsAsText(String recipientsAsText) ;
    
    public String getRecipientsAsTextBcc();
    
    public void setRecipientsAsTextBcc(String recipientsAsTextBcc) ;
        
        

}