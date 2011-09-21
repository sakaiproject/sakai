package org.sakaiproject.component.app.messageforums;

import java.util.Date;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.scheduler.DelayedInvocation;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.ForumScheduleNotification;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.SynopticMsgcntrManager;
import org.sakaiproject.api.app.messageforums.cover.SynopticMsgcntrManagerCover;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;

public class ForumScheduleNotificationImpl implements ForumScheduleNotification {
	
	private static final Log LOG = LogFactory.getLog(ForumScheduleNotificationImpl.class);
    
    private static final String AREA_PREFIX = "area-";
    private static final String FORUM_PREFIX = "forum-";
    private static final String TOPIC_PREFIX = "topic-";
	
    private MessageForumsTypeManager typeManager;
    public void setTypeManager(MessageForumsTypeManager typeManager){
    	this.typeManager = typeManager;
    }
	private AreaManager areaManager;
	public void setAreaManager(AreaManager areaManager){
		this.areaManager = areaManager;
	}
	
	private DiscussionForumManager forumManager;
	public void setForumManager(DiscussionForumManager forumManager){
		this.forumManager = forumManager;
	}

	private TimeService timeService;
    public void setTimeService(TimeService timeService)
    {
        this.timeService = timeService;
    }

    private ScheduledInvocationManager scheduledInvocationManager;

    public void setScheduledInvocationManager(
            ScheduledInvocationManager scheduledInvocationManager)
    {
        this.scheduledInvocationManager = scheduledInvocationManager;
    }

    public void init() {
		LOG.info("init()");
	}
    
    public void scheduleAvailability(Area area)
    {
    	scheduleAvailability(AREA_PREFIX + area.getContextId(), area.getAvailabilityRestricted(), area.getOpenDate(), area.getCloseDate());
    }
    
    public void scheduleAvailability(DiscussionForum forum)
    {
    	scheduleAvailability(FORUM_PREFIX + forum.getId().toString(), forum.getAvailabilityRestricted(), forum.getOpenDate(), forum.getCloseDate());
    }
    
    public void scheduleAvailability(DiscussionTopic topic)
    {
    	scheduleAvailability(TOPIC_PREFIX + topic.getId().toString(), topic.getAvailabilityRestricted(), topic.getOpenDate(), topic.getCloseDate());
    }
    
    private void scheduleAvailability(String id, boolean availabilityRestricted, Date openDate, Date closeDate){
    	// Remove any existing notifications for this area
    	DelayedInvocation[] fdi = scheduledInvocationManager.findDelayedInvocations("org.sakaiproject.api.app.messageforums.ForumScheduleNotification",
    			id);
    	if (fdi != null && fdi.length > 0)
    	{
    		for (DelayedInvocation d : fdi)
    		{
    			scheduledInvocationManager.deleteDelayedInvocation(d.uuid);
    		}
    	}
    	
    	if (availabilityRestricted)
    	{
    		Time openTime = null;
    		Time closeTime = null;
    		if(openDate != null){
    			openTime = timeService.newTime(openDate.getTime());
    		}
    		if(closeDate != null){
    			closeTime = timeService.newTime(closeDate.getTime());
    		}
    		// Schedule the new notification
    		if (openTime != null && openTime.after(timeService.newTime()))
    		{
    			scheduledInvocationManager.createDelayedInvocation(openTime,
    					"org.sakaiproject.api.app.messageforums.ForumScheduleNotification",
    					id);
    		}
    		if(closeTime != null && closeTime.after(timeService.newTime())){
    			scheduledInvocationManager.createDelayedInvocation(closeTime,
    					"org.sakaiproject.api.app.messageforums.ForumScheduleNotification",
    					id);
    		}
    	}  	
    }
    
    
    public void execute(String opaqueContext){
    	LOG.info("ForumScheduleNotificationImpl.execute(): " + opaqueContext);
    	if(opaqueContext.startsWith(AREA_PREFIX)){
    		String siteId = opaqueContext.substring(AREA_PREFIX.length());
    		Area area = areaManager.getAreaByContextIdAndTypeId(siteId, typeManager.getDiscussionForumType());
    		boolean makeAvailable = makeAvailableHelper(area.getAvailabilityRestricted(), area.getOpenDate(), area.getCloseDate());
    		
    		boolean madeChange = false;
    		if(area.getAvailability()){
    			if(!makeAvailable){
    				//make area unavailable:
    				area.setAvailability(makeAvailable);
    				madeChange = true;
    			}
    		}else{
    			if(makeAvailable){
    				//make area available:
    				area.setAvailability(makeAvailable);
    				madeChange = true;
    			}    			
    		}
    		if(madeChange){
    			//save area and update synoptic counts
    			areaManager.saveArea(area);    			
    			SynopticMsgcntrManagerCover.resetAllUsersSynopticInfoInSite(siteId);
    		}
    	}else if(opaqueContext.startsWith(FORUM_PREFIX)){
    		Long forumId = Long.parseLong(opaqueContext.substring(FORUM_PREFIX.length()));
    		DiscussionForum forum = forumManager.getForumById(forumId);
    		boolean makeAvailable = makeAvailableHelper(forum.getAvailabilityRestricted(), forum.getOpenDate(), forum.getCloseDate());
    		boolean madeChange = false;
    		if(forum.getAvailability()){
    			if(!makeAvailable){
    				//make area unavailable:
    				forum.setAvailability(makeAvailable);
    				madeChange = true;
    			}
    		}else{
    			if(makeAvailable){
    				//make area available:
    				forum.setAvailability(makeAvailable);
    				madeChange = true;
    			}    			
    		}
    		if(madeChange){
    			//save forum and update synoptic counts
    			String siteId = forumManager.getContextForForumById(forumId);
    			HashMap<String, Integer> beforeChangeHM = SynopticMsgcntrManagerCover.getUserToNewMessagesForForumMap(siteId, forum.getId(), null);
    			forumManager.saveForum(forum, forum.getDraft(), siteId, false, "-forumScheduler-");
    			updateSynopticMessagesForForumComparingOldMessagesCount(siteId, forum.getId(), null, beforeChangeHM, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
    		}

    	}else if(opaqueContext.startsWith(TOPIC_PREFIX)){
    		Long topicId = Long.parseLong(opaqueContext.substring(TOPIC_PREFIX.length()));
    		DiscussionTopic topic = forumManager.getTopicById(topicId);
    		
    		boolean makeAvailable = makeAvailableHelper(topic.getAvailabilityRestricted(), topic.getOpenDate(), topic.getCloseDate());
    		boolean madeChange = false;
    		if(topic.getAvailability()){
    			if(!makeAvailable){
    				//make area unavailable:
    				topic.setAvailability(makeAvailable);
    				madeChange = true;
    			}
    		}else{
    			if(makeAvailable){
    				//make area available:
    				topic.setAvailability(makeAvailable);
    				madeChange = true;
    			}    			
    		}
    		if(madeChange){
    			//save forum and update synoptic counts
    			String siteId = forumManager.getContextForTopicById(topicId);
    			HashMap<String, Integer> beforeChangeHM = SynopticMsgcntrManagerCover.getUserToNewMessagesForForumMap(siteId, topic.getBaseForum().getId(), topic.getId());
    			
    			forumManager.saveTopic(topic, topic.getDraft(), false, "-forumScheduler-");
    			updateSynopticMessagesForForumComparingOldMessagesCount(siteId, topic.getBaseForum().getId(), topic.getId(), beforeChangeHM, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
    		}
    	}
    }
    
    public boolean makeAvailableHelper(boolean availabilityRestricted, Date openDate, Date closeDate){
    	boolean makeAvailable = true;
		if(availabilityRestricted){
			//availability is being restricted:
			makeAvailable = false;
			
			boolean afterOpen = false;
			boolean beforeClose = false;
			Time openTime = null;
    		Time closeTime = null;
    		if(openDate != null){
    			openTime = timeService.newTime(openDate.getTime());
    		}
    		if(closeDate != null){
    			closeTime = timeService.newTime(closeDate.getTime());
    		}
    		if(closeDate == null && openDate == null){
    			//user didn't specify either, so open topic
    			makeAvailable = true;
    		}
    		
    		
			if(openTime != null && openTime.before(timeService.newTime())){
				afterOpen = true;
			}else if(openTime == null){
				afterOpen = true;
			}
			if(closeTime != null && closeTime.after(timeService.newTime())){
				beforeClose = true;
			}else if(closeTime == null){
				beforeClose = true;
			}

			if(afterOpen && beforeClose){
				makeAvailable = true;    				
			}
		}
		return makeAvailable;
    }
    
    public void updateSynopticMessagesForForumComparingOldMessagesCount(String siteId, Long forumId, Long topicId, HashMap<String, Integer> beforeChangeHM, int numOfAttempts) {
  	  try {
  		  // update synotpic info for forums only:
  		  SynopticMsgcntrManagerCover
  		  .updateSynopticMessagesForForumComparingOldMessagesCount(
  				  siteId, forumId, topicId, beforeChangeHM);
  	  } catch (HibernateOptimisticLockingFailureException holfe) {

  		  // failed, so wait and try again
  		  try {
  			  Thread.sleep(SynopticMsgcntrManager.OPT_LOCK_WAIT);
  		  } catch (InterruptedException e) {
  			  e.printStackTrace();
  		  }

  		  numOfAttempts--;

  		  if (numOfAttempts <= 0) {
  			  System.out
  			  .println("ForumScheduleNotificationImpl: HibernateOptimisticLockingFailureException no more retries left");
  			  holfe.printStackTrace();
  		  } else {
  			  System.out
  			  .println("ForumScheduleNotificationImpl: HibernateOptimisticLockingFailureException: attempts left: "
  					  + numOfAttempts);
  			  updateSynopticMessagesForForumComparingOldMessagesCount(siteId,
  					  forumId, topicId, beforeChangeHM, numOfAttempts);
  		  }
  	  }
    }
}
