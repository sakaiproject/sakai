/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.search.component.adapter.contenthosting;

import java.util.Iterator;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author ieb
 */
public class RegexParser implements Iterator<String>
{
	private static Log log = LogFactory.getLog(RegexParser.class);
	private static final String IGNORE_TAGS = "::script::head::style::";
	
	private Stack<String> elementStack = new Stack<String>();
	private Pattern tags = Pattern.compile("(.*?)<(.*?)>",Pattern.MULTILINE);
	private Matcher matcher;
	private int ignore = 100;

	private boolean notxml = true;
	
	
	public RegexParser(String content)
	{
		    matcher = tags.matcher(content);
	}
    
    
    public String getTagName (String tag, int start) {
     tag = tag.substring(start);
	 char[] c= tag.toCharArray();
	 String[] words = tag.split("\\s",2);
	 if  ( words != null && words.length != 0 ) {
		 return words[0];
	 } else {
		 return tag;
	 }
    }


	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext()
	{
		return matcher.find();
	}


	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public String next()
	{
		String before = matcher.group(1);
		String tag = matcher.group(2);
		if ( tag.endsWith("/") ) {
		} else if ( tag.startsWith("!--") ) {
		} else if ( tag.startsWith("/") ) {
			tag = getTagName(tag,1);
			if ( elementStack.size() > 0 && tag.equals(elementStack.peek()) ) {
				elementStack.pop();
			} else {
				notxml = true;
			}
		} else {
			tag = getTagName(tag,0);
			elementStack.push(tag);
			if ( ignore >= elementStack.size() ) {
				if ( IGNORE_TAGS.indexOf(":"+tag+":") != -1 ) {
			        ignore = elementStack.size();
				} else {
			        ignore = elementStack.size()+1;
				}
			} 
		}
		
		if ( notxml || ignore > elementStack.size()  )  {
		    return before.trim();
		} 
		return "";
	}


	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	public void remove()
	{
	}
       
}
