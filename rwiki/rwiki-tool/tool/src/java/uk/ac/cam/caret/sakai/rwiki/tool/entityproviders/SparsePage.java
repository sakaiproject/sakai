package uk.ac.cam.caret.sakai.rwiki.tool.entityproviders;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * A more fully fleshed page with html and comments. Intended for retrieval
 * by single page requests.
 * 
 * @author Adrian Fish <adrian.r.fish@gmail.com>
 */
public class SparsePage extends SparserPage {
	
	@Getter
	private List<SparseComment> comments = new ArrayList<SparseComment>();
	
	@Setter
	@Getter
	private String html = "";
	
	public SparsePage(String name, String siteId, String format) {
		super(name,siteId, format);
	}
	
	public void addComment(SparseComment comment) {
		this.comments.add(comment);
	}
}
