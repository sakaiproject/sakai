package org.sakaiproject.search.api;

public interface TermFrequency
{

	/**
	 * get the terms, ordered by frequency 
	 * @return
	 */
	String[] getTerms();

	/**
	 * get the frequencies
	 * @return
	 */
	int[] getFrequencies();
	

}
