/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.util.impl;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.util.api.LinkMigrationHelper;

@Slf4j
public class LinkMigrationHelperImpl implements LinkMigrationHelper {
	private static final String ESCAPED_SPACE= "%"+"20";
	private static final String[] shortenerDomainsToExpand = {"/x/", "bit.ly"};

	private ServerConfigurationService serverConfigurationService;

	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	public void init(){
		
	}

	/**
	 * {@inheritDoc}
	 */
	public String bracketAndNullifySelectedLinks(String m) throws Exception {
		
		String lbTmp = serverConfigurationService.getString("LinkMigrationHelper.linksToBracket","assignment,forum");
		String[] linksToBracket = lbTmp.split(",");
		String lnTmp = serverConfigurationService.getString("LinkMigrationHelper.linksToNullify","sam_pub,/posts/");
		String[] linksToNullify = lnTmp.split(",");
		List<String> existingLinks = findLinks(m);
		Iterator<String> l = existingLinks.iterator();
		while(l.hasNext()){
			
			String nextLink = l.next();
			boolean bracketIt = matchLink(nextLink, linksToBracket);
			boolean nullIt = matchLink(nextLink, linksToNullify);
			if(bracketIt | nullIt){
				String replacementForLink = null;
				if(bracketIt){
					replacementForLink = nextLink;
				}else{
					replacementForLink = findLinkContent(nextLink);
				}
				int li = m.indexOf(nextLink);
				String before = m.substring(0, li);
				String after = m.substring(li+nextLink.length());
				StringBuffer replacementBuffer = new StringBuffer();
				replacementBuffer.append(before);
				replacementBuffer.append(" [");
				replacementBuffer.append(replacementForLink);
				replacementBuffer.append("] ");
				replacementBuffer.append(after);
				m=replacementBuffer.toString();
			}
		}
		return m;
	}

	private boolean matchLink(String link, String[] matches){
		
		for(int s = 0; s< matches.length; s++){
			String nextKey = matches[s];
			if(link.indexOf(nextKey)>0) return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public String migrateAllLinks(Set<Entry<String,String>> entrySet, String msgBody){
		Iterator<Entry<String, String>> entryItr = entrySet.iterator();
		while(entryItr.hasNext()) {
			Entry<String, String> entry = entryItr.next();
			String fromContextRef = entry.getKey();
			String targetContextRef = entry.getValue();
			msgBody =migrateOneLink(fromContextRef, targetContextRef, msgBody);
		}
		try {
			msgBody = bracketAndNullifySelectedLinks(msgBody);
		} catch (Exception e) {
			log.debug ("Forums LinkMigrationHelper.editLinks failed" + e);
		}
		return msgBody;
	}

	/**
	 * {@inheritDoc}
	 */
	public String migrateOneLink(String fromContextRef, String targetContextRef, String msgBody){
		fromContextRef=fromContextRef.replace(" ",ESCAPED_SPACE);
		targetContextRef = targetContextRef.replace(" ",ESCAPED_SPACE);
		//Expands all the shortened URLs before replacing the context
		String expandedMsgBody = this.expandShortenedUrls(msgBody);
		if(expandedMsgBody.contains(fromContextRef)){
			expandedMsgBody = expandedMsgBody.replace(fromContextRef, targetContextRef);
		}
		return expandedMsgBody;
	}

	private List<String> findLinks(String msgBody) throws Exception {
		
		List<String> links = new ArrayList<>();
		int nextLinkAt = 0;
		nextLinkAt = msgBody.indexOf("<a", nextLinkAt);
		boolean done = false;
		if(nextLinkAt<0){
			done=true;
		}
		while(!done){
			
			int closingTagLocation = msgBody.indexOf("</a>", nextLinkAt);
			if(closingTagLocation<0){
				throw new IllegalArgumentException("unbalanced anchor tag");
			}else{
				String thisAnchor = msgBody.substring(nextLinkAt, closingTagLocation+4);
				links.add(thisAnchor);
			}
			nextLinkAt = msgBody.indexOf("<a", closingTagLocation+4);
			if(nextLinkAt<0){
				done=true;
			}
		}
		return links;
	}
	
	private String findLinkContent(String link) throws Exception {
		int contentStart = link.indexOf(">");
		int contentEnd = link.indexOf("</a>", contentStart);
		return link.substring(contentStart+1, contentEnd);
	}

	/**
	 * Parses an HTML content, extracts the URLs and expands the shortened ones.
	 * This method is used mostly when importing content from other sites
	 * @param msgBody
	 * @return String the msgBody with the expanded URLs
	 */
	private String expandShortenedUrls(String msgBody){
		String replacedBody = msgBody;
		if(StringUtils.isNotEmpty(msgBody)){
			Document doc = Jsoup.parse(msgBody);

			Elements links = doc.select("a[href]");
			Elements media = doc.select("[src]");
			Elements imports = doc.select("link[href]");
			List<String> references = new ArrayList<String>();
			// href ...
			for (Element link : links) {
				references.add(link.attr("abs:href"));
			}

			// img ...
			for (Element src : media) {
				references.add(src.attr("abs:src"));
			}

			// js, css, ...
			for (Element link : imports) {
				references.add(link.attr("abs:href"));
			}

			for(String reference : references){
				//If doesn't contain the prefix /x/, should ignore the URL.
				if(referenceContainsShortenerDomains(reference)){
					String longUrl = this.expandShortenedUrl(reference);
					replacedBody = StringUtils.replace(replacedBody, reference, longUrl);
				}
			}
		}
		return replacedBody;
	}

	private boolean referenceContainsShortenerDomains(String reference) {
		return Arrays.stream(shortenerDomainsToExpand).anyMatch(reference::contains);
	}

	private String expandShortenedUrl(String shortenedUrl){
		String expandedURL = StringUtils.EMPTY;
		try{
			URL url = new URL(shortenedUrl);
			// open connection
			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			// stop following browser redirect
			httpURLConnection.setInstanceFollowRedirects(false);
			// extract location header containing the actual destination URL
			expandedURL = httpURLConnection.getHeaderField("Location");
			httpURLConnection.disconnect();
		}catch(Exception ex){
			log.warn("LinkMigrationHelper: Unable to expand URL {}.", shortenedUrl);
		}
		return expandedURL;
	}

}
