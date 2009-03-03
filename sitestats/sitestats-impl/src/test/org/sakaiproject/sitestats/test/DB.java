package org.sakaiproject.sitestats.test;

import java.util.List;

public interface DB {
	public List getResultsForClass(final Class classz);
	
	public void deleteAllForClass(final Class classz);
	public void deleteAll();
}
