package org.sakaiproject.util;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



public class LinkMigrationHelper {
	private final static String ESCAPED_SPACE= "%"+"20";
	private final static String ASSIGNMENT_LINK_SIG = "assignment";
	private final static String FORUM_LINK_SIG = "forum";
	
	private static final Log LOG = LogFactory.getLog(LinkMigrationHelper.class);

	
	public static String editLinks(String m, String linksToEdit) throws Exception {
//		String m = msg.toLowerCase();
		List existingLinks = findLinks(m);
		Iterator l = existingLinks.iterator();
		while(l.hasNext()){
			String nextLink = (String) l.next();
			if(nextLink.indexOf(linksToEdit)>=0){
				String replacementForLink = findLinkContent(nextLink);
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
	
	public static String bracketLinks(String m, String linksToEdit) throws Exception {
//		String m = msg.toLowerCase();
		List existingLinks = findLinks(m);
		Iterator l = existingLinks.iterator();
		while(l.hasNext()){
			String nextLink = (String) l.next();
			if(nextLink.indexOf(linksToEdit)>=0){
				String replacementForLink = nextLink;
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
	
	public static String miagrateAllLinks(Set entrySet, String msgBody){
		Iterator<Entry<String, String>> entryItr = entrySet.iterator();
		while(entryItr.hasNext()) {
			Entry<String, String> entry = (Entry<String, String>) entryItr.next();
//			String fromContextRef = entry.getKey().toLowerCase();
//			String targetContextRef = entry.getValue().toLowerCase();
			String fromContextRef = entry.getKey();
			String targetContextRef = entry.getValue();
/*			
			fromContextRef=fromContextRef.replace(" ",ESCAPED_SPACE);
			targetContextRef = targetContextRef.replace(" ",ESCAPED_SPACE);
//			logger.debug("fromContextRef:"+fromContextRef+"="+entry.getValue());
//			logger.debug("entry.getValue="+entry.getValue());
			if(msgBody.contains(fromContextRef)){
//				logger.debug("found a match");
				msgBody = msgBody.replace(fromContextRef, targetContextRef);
			}
*/
			msgBody = LinkMigrationHelper.miagrateOneLink(fromContextRef, targetContextRef, msgBody);
		}
		try {
			msgBody = bracketLinks(msgBody, "assignment");
			msgBody = bracketLinks(msgBody, "forum");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.debug ("Forums LinkMigrationHelper.editLinks failed" + e);
		}
		
		
		return msgBody;
	}
	
	public static String miagrateOneLink(String fromContextRef, String targetContextRef, String msgBody){

		
		fromContextRef=fromContextRef.replace(" ",ESCAPED_SPACE);
		targetContextRef = targetContextRef.replace(" ",ESCAPED_SPACE);
//		logger.debug("fromContextRef:"+fromContextRef+"="+entry.getValue());
//		logger.debug("entry.getValue="+entry.getValue());
		if(msgBody.contains(fromContextRef)){
//			logger.debug("found a match");
			msgBody = msgBody.replace(fromContextRef, targetContextRef);
		}								
		return msgBody;
	}
	
	public static List findLinks(String msgBody) throws Exception {
		
		Vector links = new Vector();
		int nextLinkAt = 0;
		nextLinkAt = msgBody.indexOf("<a", nextLinkAt);
		boolean done = false;
		if(nextLinkAt<0){
			done=true;
		}
		while(!done){
			
			int closingTagLocation = msgBody.indexOf("</a>", nextLinkAt);
			if(closingTagLocation<0){
				throw new Exception("unbalanced anchor tag");
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
	
	public static String findLinkContent(String link) throws Exception {
		int contentStart = link.indexOf(">");
		int contentEnd = link.indexOf("</a>", contentStart);
		return link.substring(contentStart+1, contentEnd);
	}
	
	public static String findLinkEntire(String link) throws Exception {
		int contentStart = link.indexOf("<");
		int contentEnd = link.indexOf("</a>", contentStart);
		return link.substring(contentStart+1, contentEnd);

	}

}
