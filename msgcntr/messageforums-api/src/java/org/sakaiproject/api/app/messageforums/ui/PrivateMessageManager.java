package org.sakaiproject.api.app.messageforums.ui;

import java.util.Set;

import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.PrivateMessage;

public interface PrivateMessageManager {
    /**
     * Check if private message area is enabled for the current user
     * 
     * @return boolean
     * 
     */
    boolean isPrivateAreaUnabled();

    /**
     * Retrieve private message area if it is enabled for the current user
     * 
     * @return
     */
    Area getPrivateArea();

    void savePrivateMessage(Message message);

    void deletePrivateMessage(Message message);

    Message getMessageById(String id);
    
///
    public Attachment createPvtMsgAttachmentObject(String attachId, String name);      

    public void savePvtMsgAttachment(Attachment attach);

    public void addPvtMsgAttachToPvtMsgData(final PrivateMessage pvtMsgData, final Attachment pvtMsgAttach);

    public void removePvtMsgAttachmentObject(Attachment o);

    public void removePvtMsgAttachPvtMsgData(final PrivateMessage pvtMsgData, final Attachment pvtMsgAttach);

    public Set getPvtMsgAttachmentsForPvtMsgData(final PrivateMessage pvtMsgData);

    public Attachment getPvtMsgAttachment(final String pvtMsgAttachId);
///
}
