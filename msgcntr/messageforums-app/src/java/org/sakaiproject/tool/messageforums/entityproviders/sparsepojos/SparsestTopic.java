package org.sakaiproject.tool.messageforums.entityproviders.sparsepojos;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.sakaiproject.api.app.messageforums.Topic;

public class SparsestTopic {
	
	@Getter
	private Long id;
	
	@Getter
	private String title;
	
	@Getter
	private Long createdDate;
	
	@Getter
	private String creator;
	
	@Getter
	private Long modifiedDate;
	
	@Getter
	private String modifier;
	
	@Getter
	private Boolean isAutoMarkThreadsRead;
	
	@Getter @Setter
	private Integer totalMessages = 0;
	
	@Getter @Setter
	private Integer readMessages = 0;
	
	@Getter @Setter
	private List<SparseAttachment> attachments = new ArrayList<SparseAttachment>();
	
	public SparsestTopic(Topic fatTopic) {
		
		this.id = fatTopic.getId();
		this.title = fatTopic.getTitle();
		this.createdDate = fatTopic.getCreated().getTime()/1000;
		this.creator = fatTopic.getCreatedBy();
		this.modifiedDate = fatTopic.getModified().getTime()/1000;
		this.modifier = fatTopic.getModifiedBy();
		this.isAutoMarkThreadsRead = fatTopic.getAutoMarkThreadsRead();
	}
}
