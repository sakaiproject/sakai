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
import org.sakaiproject.util.MergeConfig;
@Slf4j
public class LinkMigrationHelperImpl implements LinkMigrationHelper {
	private static final String ESCAPED_SPACE = "%"+"20";
	private static final String[] ENCODED_IMAGE = {"data:image"};
	private static final String[] SHORTENER_STRINGS = {"/x/", "bit.ly"};

	public static final String ACCESS_CONTENT_GROUP = "/access/content/group/";
	public static final String DIRECT_LINK = "/direct/";

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
				int li = m.indexOf(nextLink);
				String before = m.substring(0, li);
				String after = m.substring(li+nextLink.length());
				if (isAlreadyBracketed(before, after)) {
					continue;
				}
				String replacementForLink = null;
				if(bracketIt){
					replacementForLink = nextLink;
				}else{
					replacementForLink = findLinkContent(nextLink);
				}
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

	private boolean isAlreadyBracketed(String before, String after) {
		return before.endsWith(" [") && after.startsWith("] ");
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
        // Tiny guard: nothing to migrate for null/empty content
        if (StringUtils.isEmpty(msgBody)) {
            return msgBody;
        }
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
        if (StringUtils.isEmpty(msgBody)) {
            return msgBody;
        }
        fromContextRef=fromContextRef.replace(" ",ESCAPED_SPACE);
        targetContextRef = targetContextRef.replace(" ",ESCAPED_SPACE);
        //Expands all the shortened URLs before replacing the context
        String expandedMsgBody = this.expandShortenedUrls(msgBody);
        if(expandedMsgBody.contains(fromContextRef)){
            expandedMsgBody = expandedMsgBody.replace(fromContextRef, targetContextRef);
        }
        return expandedMsgBody;
    }

    /*
     * Do several transformations to migrate the content links present in a zip import of RTE content
     *
     * @param siteId the site id (Must not be null or empty)
     * @param mcx the merge config
     * @param content the content to migrate
     * @return the migrated content
     */
    public String migrateLinksInMergedRTE(String siteId, MergeConfig mcx, String content) {
		String before;
        String after = serverConfigurationService.getServerUrl() + ACCESS_CONTENT_GROUP + siteId + "/";

        // Replace full match of the fromserverUrl and fromContext (ideal)
        if ( StringUtils.isNotBlank(mcx.archiveServerUrl) && StringUtils.isNotBlank(mcx.archiveContext) ) {
            before = mcx.archiveServerUrl + ACCESS_CONTENT_GROUP + mcx.archiveContext + "/";
            content = content.replace(before, after);
        }

        // If we don't know the fromServerUrl, but we know the fromContext, replace athe url prefix to get the links onto this server
        if ( StringUtils.isBlank(mcx.archiveServerUrl) && StringUtils.isNotBlank(mcx.archiveContext) ) {
            before = "https?://[^/]+/" + ACCESS_CONTENT_GROUP + mcx.archiveContext + "/";
            content = content.replaceAll(before, after);
        }

        // If we know the fromContext, we can replace urls that start with "/"
        if ( StringUtils.isNotBlank(mcx.archiveContext) ) {
            before = "\"/" + ACCESS_CONTENT_GROUP + mcx.archiveContext + "/";
            after = "\"/" + ACCESS_CONTENT_GROUP + siteId + "/";
            content = content.replaceAll(before, after);
        }

        // If we know the fromServerUrl and not the fromContext, move old broken urls to this server at a minimum
        if ( StringUtils.isNotBlank(mcx.archiveServerUrl) ) {
            before = mcx.archiveServerUrl + ACCESS_CONTENT_GROUP;
            after = serverConfigurationService.getServerUrl() + ACCESS_CONTENT_GROUP;
            content = content.replace(before, after);
        }

        // [http://localhost:8080/direct/forum_topic/1]
        // Migrate direct links to the new server if we have the name of the server
        if ( StringUtils.isNotBlank(mcx.archiveServerUrl) ) {
            before = mcx.archiveServerUrl + DIRECT_LINK;
            after = serverConfigurationService.getServerUrl() + DIRECT_LINK;
            content = content.replace(before, after);
        }

        try {
            content = this.bracketAndNullifySelectedLinks(content);
        } catch (Exception e) {
            log.debug("Error bracketing and nullifying links: " + e.toString());
        }

        return content;
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
				//If doesn't contain the prefix /x/ or is an encoded image, should ignore the URL.
				if(!referenceContainsEncodedImage(reference) && referenceContainsShortenerDomains(reference)){
					String longUrl = this.expandShortenedUrl(reference);
					replacedBody = StringUtils.replace(replacedBody, reference, longUrl);
				}
			}
		}
		return replacedBody;
	}

	private boolean referenceContainsEncodedImage(String reference) {
		return Arrays.stream(ENCODED_IMAGE).anyMatch(reference::contains);
	}

	private boolean referenceContainsShortenerDomains(String reference) {
		return Arrays.stream(SHORTENER_STRINGS).anyMatch(reference::contains);
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
