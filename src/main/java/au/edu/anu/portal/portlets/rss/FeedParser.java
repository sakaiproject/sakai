package au.edu.anu.portal.portlets.rss;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 * This class parses a feed via ROME
 * 
 * <p>The output is totally dependent on ROME classes.
 * There could be an abstraction layer but then it wouldn't be as 'simple' which is what this portlet aims to be.</p>
 * 
 * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
 *
 */
public class FeedParser {

    private SyndFeedInput input;
	
	public FeedParser() {
		input  = new SyndFeedInput();
	}
	
	/**
	 * Parse the given feed url and return the raw type
	 * @param feedUrl
	 * @return
	 */
	public SyndFeed parseFeed(String feedUrl) {
		
		URL url;
		XmlReader reader;
		SyndFeed feed;
		
		try {
			
			//load the feed
			url = new URL(feedUrl);
			reader = new XmlReader(url);
			feed = input.build(reader);
			
			return feed;
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (FeedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	
}
