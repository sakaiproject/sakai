package org.sakaiproject.api.app.messageforums;

public interface PermissionLevel extends MutableEntity{
		
	public static final String NEW_FORUM = "newForum"; 
	public static final String NEW_TOPIC = "newTopic";
	public static final String NEW_RESPONSE = "newResponse";
	public static final String NEW_RESPONSE_TO_RESPONSE = "newResponseToResponse";
	public static final String MOVE_POSTING = "movePosting";
	public static final String CHANGE_SETTINGS = "changeSettings";
	public static final String POST_TO_GRADEBOOK = "postToGradebook";
	public static final String READ = "read";
	public static final String MARK_AS_READ = "markAsRead";
	public static final String MODERATE_POSTINGS = "moderatePostings";
	public static final String DELETE_OWN = "deleteOwn";
	public static final String DELETE_ANY = "deleteAny";
	public static final String REVISE_OWN = "reviseOwn";
	public static final String REVISE_ANY = "reviseAny";	
	
	public String getName();

	public void setName(String name);		
	
	public String getTypeUuid();

	public void setTypeUuid(String typeUuid);

	public Boolean getChangeSettings();

	public void setChangeSettings(Boolean changeSettings);

	public Boolean getDeleteAny();

	public void setDeleteAny(Boolean deleteAny);

	public Boolean getDeleteOwn();

	public void setDeleteOwn(Boolean deleteOwn);

	public Boolean getMarkAsRead();

	public void setMarkAsRead(Boolean markAsRead);

	public Boolean getModeratePostings();

	public void setModeratePostings(Boolean moderatePostings);

	public Boolean getMovePosting();

	public void setMovePosting(Boolean movePosting);

	public Boolean getNewForum();

	public void setNewForum(Boolean newForum);

	public Boolean getNewResponse();

	public void setNewResponse(Boolean newResponse);

	public Boolean getNewTopic();

	public void setNewTopic(Boolean newTopic);

	public Boolean getPostToGradebook();

	public void setPostToGradebook(Boolean postToGradebook);

	public Boolean getRead();

	public void setRead(Boolean read);

	public Boolean getNewResponseToResponse();

	public void setNewResponseToResponse(Boolean newResponseToResponse);

	public Boolean getReviseAny();

	public void setReviseAny(Boolean reviseAny);

	public Boolean getReviseOwn();

	public void setReviseOwn(Boolean reviseOwn);

}