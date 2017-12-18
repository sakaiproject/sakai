/**
 * Copyright 2011-2013 The Australian National University
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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import au.edu.anu.portal.portlets.rss.model.Attachment;
import au.edu.anu.portal.portlets.rss.utils.Messages;

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
@Slf4j
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
			log.error(e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			log.error(e.getMessage(), e);
		} catch (FeedException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Parses the entries contained in an RSS feed, extracts the enclosures, converts them to an {@link Attachment}
	 * adds them to the map with the entry uri as key.
	 * <p>The RSS spec says there is only one enclosure per item so this is what we work with. We don't actually check this so it's possible
	 * that if you have more than one enclosure attached to an item that only the latest one will be presented in the end.
	 *
	 * @param feed
	 * @return
	 */
	public static Map<String, Attachment> parseFeedEnclosures(SyndFeed feed) {
		
		Map<String,Attachment> attachments = new HashMap<String,Attachment>();
		
		// image mime types that are ok to be rendered as an image
		List<String> imageTypes = new ArrayList<String>();
		imageTypes.add("image/jpeg");
		imageTypes.add("image/gif");
		imageTypes.add("image/png");
		imageTypes.add("image/jpg");
		
		List<SyndEntry> entries = feed.getEntries();
		for(SyndEntry entry: entries) {
			
			//get entry uri, but it could be blank so if so, skip this item
			if(StringUtils.isBlank(entry.getUri())) {
				continue;
			}
			
			//for each enclosure attached to an entry get the first one and use that.			
			List<SyndEnclosure> enclosures = entry.getEnclosures();
			for(SyndEnclosure e: enclosures) {
				
				//convert to an Attachment
				Attachment a = new Attachment();
				a.setUrl(e.getUrl());
				a.setDisplayLength(formatLength(e.getLength()));
				a.setType(e.getType());
				
				//process the url into a displayname (get just the filename from the full URL)
				String displayName = StringUtils.substringAfterLast(e.getUrl(), "/");
				if(StringUtils.isNotBlank(displayName)){
					a.setDisplayName(displayName);
				} else {
					a.setDisplayName(Messages.getString("view.attachment.default"));
				}
				
				//check if its an iamge we are able to display as the thumbnail for the entry
				if(imageTypes.contains(e.getType())){
					a.setImage(true);
				} 
				
				attachments.put(entry.getUri(), a);
			}
		}
		
		return attachments;
	}

	/**
	 * Helper to format the length from bytes into a human readable format eg 126 kB
	 * @param length
	 * @return
	 */
	private static String formatLength(long length){
		return FileUtils.byteCountToDisplaySize(length);
	}

}
