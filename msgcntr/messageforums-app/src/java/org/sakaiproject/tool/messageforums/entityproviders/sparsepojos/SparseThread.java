package org.sakaiproject.tool.messageforums.entityproviders.sparsepojos;

import lombok.Getter;
import lombok.Setter;

public class SparseThread extends SparseMessage {
	
	/**
	 * This is only set when this is a top level message, a.k.a. a thread.
	 */
	@Getter @Setter
	private Long threadId;
	
	public SparseThread(SparseMessage sparseMessage) {
		super(sparseMessage);
		this.threadId = sparseMessage.getMessageId();
	}
}