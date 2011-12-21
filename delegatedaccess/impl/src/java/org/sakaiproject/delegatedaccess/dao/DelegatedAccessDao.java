package org.sakaiproject.delegatedaccess.dao;

import java.util.List;

public interface DelegatedAccessDao {

	public List<String> getDistinctSiteTerms(String termField);
}
