package org.sakaiproject.citation.util.api;

import java.util.Set;

public interface SearchQuery {
	
	public Set<String> getKeywords();
	
	public void addKeywords(String keywords);
	
	public Set<String> getTitles();
	
	public void addTitle(String title);
	
	public Set<String> getYears();
	
	public void addYear(String year);
	
	public void addAuthor( String author );
	
	public Set<String> getAuthors();
	
	public void addSubject( String subject );
	
	public Set<String> getSubjects();
}
