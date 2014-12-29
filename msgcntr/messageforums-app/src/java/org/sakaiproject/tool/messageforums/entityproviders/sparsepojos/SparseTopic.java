package org.sakaiproject.tool.messageforums.entityproviders.sparsepojos;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.sakaiproject.api.app.messageforums.Topic;

public class SparseTopic extends SparsestTopic {
	
	@Getter @Setter
	private List<SparseThread> threads;
	
	public SparseTopic(Topic fatTopic) {
		
		super(fatTopic);
	}
}
