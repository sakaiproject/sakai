package org.sakaiproject.api.app.messageforums.ui;

import java.util.Set;

import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.PrivateMessage;
import org.sakaiproject.api.app.messageforums.Topic;

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
   
    int getTotalNoMessages(Topic topic);
    int getUnreadNoMessages(String userId, Topic topic);
    
    //create new instance of Privae Message --required before saving 
    public PrivateMessage createPrivateMessage() ;
    
    
    public void saveAreaSetting() ;
    
    //Topic Folder Setting
    //TODO - As per UI pg 37 -39 user is asked to manipulate revise/delete/rename topic title 
    // so method signature is as below.. But question is can't the user have same name for different private topics.
    // As I understand thet I believe there are some unmutable topics like Received/Deleted/Sent etc ..    
    public boolean isMutableTopicFolder();
    public String createTopicFolderInForum(String parentForumId, String name);
    public String createTopicFolderInTopic(String parentTopicId, String name);
    public void deleteTopicFolder(String topicId);

    
///Attachment
    
    public void addAttachToPvtMsg(final PrivateMessage pvtMsgData, final Attachment pvtMsgAttach);
    public Attachment createPvtMsgAttachment(String attachId, String name);      
    public void savePvtMsgAttachment(Attachment attach);
    public void removePvtMsgAttachment(Attachment o);
    public Attachment getPvtMsgAttachment(final String pvtMsgAttachId);
    
    

    public void removePvtMsgAttachPvtMsgData(final PrivateMessage pvtMsgData, final Attachment pvtMsgAttach);

    public Set getPvtMsgAttachmentsForPvtMsgData(final PrivateMessage pvtMsgData);

    
///
}
