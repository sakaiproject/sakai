package uk.ac.cam.caret.sakai.rwiki.tool.entityproviders;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Like a SparsePage but without the html and comments. Intended for return
 * as part of a JSON site graph.
 * 
 * @author Adrian Fish <adrian.r.fish@gmail.com>
 */
public class SparserPage {
	
	@Getter
	private List<SparserPage> childPages = new ArrayList<SparserPage>();
	
	@Setter
	@Getter
	private String name = "";
	
	@Getter
	private String url = "";
	
	@Setter
	@Getter
	private int numberOfComments = 0;
	
	public SparserPage(String name,String siteId) {
		super();
		this.name = name;
		this.url = "/direct/wiki/site/" + siteId + "/page/" + name + ".json";
	}
	
	public void addChildPage(SparserPage childPage) {
		
		this.childPages.add(childPage);
	}
}
