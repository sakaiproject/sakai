package uk.ac.cam.caret.sakai.rwiki.tool.entityproviders;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public class SparseComment {

	@Getter
	private String creatorDisplayName;
	
	@Getter
	private long created;
	
	@Getter
	private String content;
	
	@Getter
	private List<SparseComment> childComments = new ArrayList<SparseComment>();

	public SparseComment(String creatorDisplayName, long created, String content) {
		
		this.creatorDisplayName = creatorDisplayName;
		this.created = created;
		this.content = content;
	}
	
	public void addChildComment(SparseComment childComment) {
		this.childComments.add(childComment);
	}
}
