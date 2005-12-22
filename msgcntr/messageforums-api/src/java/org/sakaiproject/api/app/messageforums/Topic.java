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

package org.sakaiproject.api.app.messageforums;

import java.util.List;
 
public interface Topic extends MutableEntity {

    public List getAttachments();

    public void setAttachments(List attachments);

    public String getExtendedDescription();

    public void setExtendedDescription(String extendedDescription);

    public Boolean getMutable();

    public void setMutable(Boolean mutable);

    public String getShortDescription();

    public void setShortDescription(String shortDescription);

    public Integer getSortIndex();

    public void setSortIndex(Integer sortIndex);

    public String getTitle();

    public void setTitle(String title);

    public String getTypeUuid();

    public void setTypeUuid(String typeUuid);
    
    public BaseForum getBaseForum();
    
    public void setBaseForum(BaseForum forum);
    
    public List getMessages();
    
    public void setMessages(List messages);

    public void addAttachment(Attachment attachment);
    
    public void removeAttachment(Attachment attachment);
    
    public void addMessage(Message message);
    
    public void removeMessage(Message message);

    public OpenForum getOpenForum();
    
    public void setOpenForum(OpenForum openForum);
    
    public PrivateForum getPrivateForum();
    
    public void setPrivateForum(PrivateForum privateForum);    

}