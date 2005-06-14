package org.sakaiproject.tool.assessment.util;

import java.util.List;

public interface PagingUtilQueriesAPI
{

  public List getAll(final int pageSize, final int pageNumber,
      final String queryString);

}