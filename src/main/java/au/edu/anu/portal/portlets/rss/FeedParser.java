/**
 * Copyright 2011-2011 The Australian National University
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package au.edu.anu.portal.portlets.rss;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.sun.syndication.feed.synd.SyndEnclosure;
import com.sun.syndication.feed.synd.SyndEntry;
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
	
	
	/**
	 * Parses the enclosures contained in an RSS feed and extracts the <b>first</b> image that matches the allowed types
	 * then adds it to the map with the Entry URI as key and the Enclosure URL as value.
	 * @param feed
	 * @return
	 */
	public static Map<String, String> parseEntryImages(SyndFeed feed) {
		
		Map<String,String> images = new HashMap<String,String>();
		
		List<String> imageTypes = new ArrayList<String>();
		imageTypes.add("image/jpeg");
		imageTypes.add("image/gif");
		imageTypes.add("image/png");
		imageTypes.add("image/jpg");
		//add more types as required
		
		
		List<SyndEntry> entries = feed.getEntries();
		for(SyndEntry entry: entries) {
			
			//get entry uri, but it could be blank so if so, skip this item
			if(StringUtils.isBlank(entry.getUri())) {
				continue;
			}
			
			//for each enclosure attached to an entry get the first image and use that.
			List<SyndEnclosure> enclosures = entry.getEnclosures();
			for(SyndEnclosure enclosure: enclosures) {
				String type = enclosure.getType();
				if(StringUtils.isNotBlank(type)){
					if(imageTypes.contains(type)){
						images.put(entry.getUri(), enclosure.getUrl());
					}
				}
			}
		}
		
		return images;
	}
	
}
