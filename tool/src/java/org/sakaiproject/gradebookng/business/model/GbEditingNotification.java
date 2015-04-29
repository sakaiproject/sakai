package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;
import java.util.Date;

import org.sakaiproject.user.api.User;

import lombok.Getter;
import lombok.Setter;

/**
 * The data packet that is persisted in the cache when someone is editing
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GbEditingNotification implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Getter
	private String editorEid;
	
	@Getter
	private String editorName;
	
	@Getter @Setter
	private Date lastUpdated;
	
	/** 
	 * The gradebook uid is here for redundancy. 
	 * The data is already stored in the cache against the gradebook uid.
	 * TODO I think we might remove this field
	 */
	@Getter @Setter
	private String gradebookUid;
	
	public GbEditingNotification(User u, String gradebookUid){
		this.editorEid = u.getEid();
		this.editorEid = u.getDisplayName();
		this.gradebookUid = gradebookUid;
		this.lastUpdated = new Date();
	}
	
}
