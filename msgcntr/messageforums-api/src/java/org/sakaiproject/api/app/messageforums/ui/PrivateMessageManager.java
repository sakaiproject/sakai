package org.sakaiproject.api.app.messageforums.ui;

import java.util.List;

import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.PrivateForum;
import org.sakaiproject.api.app.messageforums.PrivateMessage;
import org.sakaiproject.api.app.messageforums.Topic;

public interface PrivateMessageManager {
    
    public static String SORT_COLUMN_SUBJECT = "title";
    public static String SORT_COLUMN_AUTHOR = "author";
    public static String SORT_COLUMN_DATE = "message.created";
    public static String SORT_COLUMN_LABEL = "label";
    
    public static String SORT_ASC = "asc";
    public static String SORT_DESC = "desc";
          
    
    public void markMessageAsReadForUser(final PrivateMessage message);
    
    public List getMessagesByType(final String typeUuid, final String orderField,
        final String order);
    
    /**
     * check if private message area is enabled for the current user
     * @return boolean
     */
    boolean isPrivateAreaUnabled();

    /**
     * retrieve private message area if it is enabled for the current user
     * @return area
     */
    Area getPrivateMessageArea();
    
    public PrivateForum initializePrivateMessageArea(Area area);        
    
    
    /** 
     * create private message with type
     * @param typeUuid
     * @return private message
     */
    public PrivateMessage createPrivateMessage(String typeUuid);
    
    /**
     * send private message to recipients
     * @param message
     * @param recipients
     */
    public void sendPrivateMessage(PrivateMessage message, List recipients);
    
    
    /**
     * mark message as deleted for user
     * @param message
     */
    public void deletePrivateMessage(Message message);

    /**
     * Save private message
     * @param message
     * @param recipients
     */
    void savePrivateMessage(Message message);
    
    /**
     * find unread message count for topic and type
     * @param topicId
     * @param typeUuid
     * @return count
     */
    public int findUnreadMessageCount(final Long topicId, final String typeUuid);
    
    /**
     * find message count for topic and type
     * @param topicId
     * @param typeUuid
     * @return
     */
    public int findMessageCount(final Long topicId, final String typeUuid);

        

    public Message getMessageById(Long messageId);
    
    public List getReceivedMessages(String orderField, String order);
    public List getSentMessages(String orderField, String order);
    public List getDeletedMessages(String orderField, String order);
    public List getDraftedMessages(String orderField, String order);    
    
    // will the below be helpful for displaying messages related to other mutable topics. 
    public List getMessagesByTopic(String userId, Long topicId);
    
    int getTotalNoMessages(Topic topic);
    int getUnreadNoMessages(String userId, Topic topic);    
    
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
    public Attachment getPvtMsgAttachment(final Long pvtMsgAttachId);
    
    
    public boolean isInstructor(String userId);
}
