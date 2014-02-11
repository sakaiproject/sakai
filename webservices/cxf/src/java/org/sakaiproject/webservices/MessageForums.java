package org.sakaiproject.webservices;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.util.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.PermissionLevelManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.DBMembershipItem;
import org.sakaiproject.api.app.messageforums.PermissionLevel;
import org.sakaiproject.api.app.messageforums.PermissionsMask;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;


public class MessageForums extends AbstractWebService {

    /** Key in the ThreadLocalManager for binding our current placement. */
    protected final static String CURRENT_PLACEMENT = "sakai:ToolComponent:current.placement";

    /** Key in the ThreadLocalManager for binding our current tool. */
    protected final static String CURRENT_TOOL = "sakai:ToolComponent:current.tool";
	
private static Log LOG = LogFactory.getLog(MessageForums.class);

/**
 * Adds a message to an existing forum or if there are no forums to add, adds a forum
 * and then adds a message.
 *
 * @param sessionid         the session to use
 * @param context           the context to use
 * @param forum             the forum title
 * @param user              the user id that wil be creating the forums / messages
 * @param title             the message title
 * @param body              the message body
 * @return                  the sessionid if active, or "null" if not.
 */
public String addMessage(String sessionid, String context, 
	String forum, String topic, String user, String title, String body )  
{
    Session s = establishSession(sessionid);
    

	// Wrap this in a big try / catch block so we get better feedback 
	// in the logs in the case of an error
	try {
		Site site = siteService.getSite(context);
	
	    ToolConfiguration tool = site.getToolForCommonId("sakai.forums");
	
	    if(tool == null) {
			return "Tool sakai.forums not found in site="+context;
		}
	
		// Lets go down and hack our essense into the thread
	    threadLocalManager.set(CURRENT_PLACEMENT, tool);
	    threadLocalManager.set(CURRENT_TOOL, tool.getTool() ) ;

        List<DiscussionForum> forums = messageForumsForumManager.getForumsForMainPage();

	    Topic selectedTopic = null;
	    Topic anyTopic = null;
	    DiscussionForum selectedForum = null;
	    DiscussionForum anyForum = null;
	    DiscussionTopic dTopic = null;

        for (DiscussionForum dForum: forums) {
		    anyForum = dForum;
		    if ( forum.equals(dForum.getTitle()) ) selectedForum = dForum;
	        LOG.debug("forum = "+dForum+" ID="+dForum.getId());
	    }

	    if ( selectedForum == null ) selectedForum = anyForum;
	    if ( selectedForum == null ) {

            Area area = areaManager.getAreaByContextIdAndTypeId(context, messageForumsTypeManager.getDiscussionForumType());

            if (area == null) {
                area = areaManager.createArea(messageForumsTypeManager.getDiscussionForumType(), context);
                area.setName("AREA 51");
                area.setEnabled(Boolean.TRUE);
                area.setHidden(Boolean.TRUE);
                area.setLocked(Boolean.FALSE);
                area.setModerated(Boolean.FALSE);
                area.setPostFirst(Boolean.FALSE);
                area.setAutoMarkThreadsRead(false);
                area.setSendEmailOut(Boolean.TRUE);
                area.setAvailabilityRestricted(Boolean.FALSE);
                areaManager.saveArea(area);
			    LOG.debug("Created area...");
		    }

            selectedForum = messageForumsForumManager.createDiscussionForum();
            selectedForum.setArea(area);
            selectedForum.setCreatedBy(user);
            selectedForum.setTitle(forum);
            selectedForum.setDraft(false);
            selectedForum.setModerated(false);
            selectedForum.setPostFirst(false);
            messageForumsForumManager.saveDiscussionForum(selectedForum);
		    LOG.debug("Created forum="+forum);
            dTopic = messageForumsForumManager.createDiscussionForumTopic(selectedForum);
            dTopic.setTitle(topic);
            dTopic.setCreatedBy(user);
            messageForumsForumManager.saveDiscussionForumTopic(dTopic, false);
		    LOG.debug("Created topic="+topic);
            forums = messageForumsForumManager.getForumsForMainPage();
		    selectedForum = null;
            for (DiscussionForum dForum: forums) {
		        anyForum = dForum;
		        if ( forum.equals(dForum.getTitle()) ) selectedForum = dForum;
	            LOG.debug("forum = "+dForum+" ID="+dForum.getId());
	        }
	    }

	    if ( selectedForum == null ) selectedForum = anyForum;
	    if ( selectedForum == null ) return "No forums found in site="+context;

        for (Object o: selectedForum.getTopicsSet()) {
            dTopic = (DiscussionTopic)o;
            anyTopic = dTopic;
            if ( topic.equals(dTopic.getTitle()) ) selectedTopic = dTopic;
            if (dTopic.getDraft().equals(Boolean.FALSE)) {
                LOG.debug("Topic ID="+dTopic.getId()+" title="+dTopic.getTitle());
            }
        }

	    if ( selectedTopic == null ) selectedTopic = anyTopic;
	    if ( selectedTopic == null ) return "No topic";

        DiscussionTopic topicWithMsgs = (DiscussionTopic) discussionForumManager.getTopicByIdWithMessages(selectedTopic.getId());
        List tempList = topicWithMsgs.getMessages();
		Message replyMessage = null;
        if(tempList != null && tempList.size() > 0)
        {
            replyMessage = (Message)tempList.get(tempList.size()-1);
		}

       Message aMsg;
       aMsg = messageForumsMessageManager.createDiscussionMessage();
       aMsg.setTitle(title);
       aMsg.setBody(body);
       aMsg.setAuthor(user);
       aMsg.setDraft(Boolean.FALSE);
       aMsg.setDeleted(Boolean.FALSE);
       aMsg.setApproved(Boolean.TRUE);
       aMsg.setTopic(selectedTopic);
       if ( replyMessage != null ) {
          aMsg.setInReplyTo(replyMessage);
       }
       discussionForumManager.saveMessage(aMsg);
       return "Success";
	} catch (Exception e ) {
		e.printStackTrace();
	}

	return "Failure";
}

}
