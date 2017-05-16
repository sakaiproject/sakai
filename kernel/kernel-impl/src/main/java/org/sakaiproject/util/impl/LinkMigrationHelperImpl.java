package org.sakaiproject.util.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.util.api.LinkMigrationHelper;



public class LinkMigrationHelperImpl implements LinkMigrationHelper {

	private static final Logger LOG = LoggerFactory.getLogger(LinkMigrationHelperImpl.class);
	private static final String ESCAPED_SPACE= "%"+"20";

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
			LOG.debug ("Forums LinkMigrationHelper.editLinks failed" + e);
		}
		return msgBody;
	}

	/**
	 * {@inheritDoc}
	 */
	public String migrateOneLink(String fromContextRef, String targetContextRef, String msgBody){
		fromContextRef=fromContextRef.replace(" ",ESCAPED_SPACE);
		targetContextRef = targetContextRef.replace(" ",ESCAPED_SPACE);
		if(msgBody.contains(fromContextRef)){
			msgBody = msgBody.replace(fromContextRef, targetContextRef);
		}
		return msgBody;
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

}
