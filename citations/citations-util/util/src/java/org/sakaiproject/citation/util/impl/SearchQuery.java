/**
 * Copyright (c) 2003-2007 The Apereo Foundation
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
package org.sakaiproject.citation.util.impl;

import java.util.Set;

public class SearchQuery
implements org.sakaiproject.citation.util.api.SearchQuery {
	private InputStringParser isp = new InputStringParser();
	private Set<String> keywords;
	private Set<String> title;
	private Set<String> authors;
	private Set<String> subjects;
	private Set<String> year;
	
	public Set<String> getKeywords() {
		return keywords;
	}
	
	public void addKeywords(String keywords) {
		if( this.keywords == null ) {
			this.keywords = new java.util.HashSet<String>();
		}
		
		if( keywords != null && !keywords.trim().equals("") ) {
			Set<String> keywordSet = isp.parseInputString( keywords );
			for( String keyword : keywordSet ) {
				this.keywords.add( keyword );
			}
		}
	}
	
	public Set<String> getTitles() {
		return title;
	}
	
	public void addTitle(String title) {
		if( this.title == null ) {
			this.title = new java.util.HashSet<String>();
		}
		
		if( title != null && !title.trim().equals("") ) {
			this.title.add( title );
		}
	}
	
	public Set<String> getYears() {
		return year;
	}
	
	public void addYear(String year) {
		if( this.year == null ) {
			this.year = new java.util.HashSet<String>();
		}
		
		if( year != null && !year.trim().equals("") ) {
			this.year.add( year );
		}
	}
	
	public void addAuthor( String author ) {
		if( authors == null ) {
			authors = new java.util.HashSet<String>();
		}
		
		if( author != null && !author.trim().equals( "" ) ) {
			authors.add( author );
		}
	}
	
	public Set<String> getAuthors() {
		return authors;
	}
	
	public void addSubject( String subject ) {
		if( subjects == null ) {
			subjects = new java.util.HashSet<String>();
		}
		
		if( subject != null && !subject.trim().equals( "" ) ) {
			Set<String> subjectSet = isp.parseInputString( subject );
			for( String subj : subjectSet ) {
				subjects.add( subj );
			}
		}
	}
	
	public Set<String> getSubjects() {
		return subjects;
	}
}
