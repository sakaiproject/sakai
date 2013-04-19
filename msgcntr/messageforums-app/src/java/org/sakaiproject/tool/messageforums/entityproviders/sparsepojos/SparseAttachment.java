package org.sakaiproject.tool.messageforums.entityproviders.sparsepojos;

import lombok.Getter;

public class SparseAttachment {
	
	@Getter
	private String name;
	
	@Getter
	private String url;
	
	public SparseAttachment(String name, String url) {
		this.name = name;
		this.url = url;
	}

}
