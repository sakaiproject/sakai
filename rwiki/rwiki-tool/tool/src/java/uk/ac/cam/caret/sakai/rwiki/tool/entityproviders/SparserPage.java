package uk.ac.cam.caret.sakai.rwiki.tool.entityproviders;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.Setter;

/**
 * Like a SparsePage but without the html and comments. Intended for return
 * as part of a JSON site graph.
 * 
 * @author Adrian Fish <adrian.r.fish@gmail.com>
 */
@Slf4j
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
	
	public SparserPage(String name,String siteId,String format) {
		super();
		this.name = name;
		try {
			this.url = "/direct/" + RWikiEntityProvider.ENTITY_PREFIX + "/" + siteId + "/page/" + URLEncoder.encode(name,"UTF-8").replaceAll("\\+","%20") + "." + format;
		} catch(UnsupportedEncodingException e) {
			log.error("UTF-8 is unsupported in the the encoding of URLs. The url was not set.");
		}
	}
	
	public void addChildPage(SparserPage childPage) {
		
		this.childPages.add(childPage);
	}
}
