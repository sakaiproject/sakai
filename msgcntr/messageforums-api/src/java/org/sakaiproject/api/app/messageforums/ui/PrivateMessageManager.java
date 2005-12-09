package org.sakaiproject.api.app.messageforums.ui;

import java.util.List;

import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.PrivateMessage;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.exception.IdUnusedException;

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
    
    /**
     * Send private message to recipients
     * @param message
     * @param recipients
     */
    public void sendPrivateMessage(PrivateMessage message, List recipients) throws IdUnusedException;

    /**
     * Save private message
     * @param message
     * @param recipients
     */
    void savePrivateMessage(Message message);

    
    
    void deletePrivateMessage(Message message);

    public Message getMessageById(String messageId);
    
    public List getReceivedMessages(String userId);
    public List getSentMessages(String userId);
    public List getDeletedMessages(String userId);
    public List getDraftedMessages(String userId);
    // will the below be helpful for displaying messages related to other mutable topics. 
    public List getMessagesByTopic(String userId, String topicId);
    
    int getTotalNoMessages(Topic topic);
    int getUnreadNoMessages(String userId, Topic topic);
    
    //create new instance of Privae Message --required before saving 
    public PrivateMessage createPrivateMessage() ;
    
    //Topic Setting
    public void saveAreaSetting() ;
    
    //Topic Folder Setting
    public boolean isMutableTopicFolder(String parentTopicId);
    public String createTopicFolderInForum(String parentForumId, String userId, String name);
    public String createTopicFolderInTopic(String parentTopicId, String userId, String name);
    //I don't know why I include userId, may be for uniformity purpose or may be to keep track who made changes
    public String renameTopicFolder(String parentTopicId, String userId, String newName);
    public void deleteTopicFolder(String topicId);

    
    //Attachment
    public void addAttachToPvtMsg(final PrivateMessage pvtMsgData, final Attachment pvtMsgAttach);
    public Attachment createPvtMsgAttachment(String attachId, String name);
    public void removePvtMsgAttachment(Attachment o);
    public Attachment getPvtMsgAttachment(final String pvtMsgAttachId);
    
}
