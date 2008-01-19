package org.sakaiproject.tool.assessment.facade;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class PublishedSectionFacadeQueries extends HibernateDaoSupport
		implements PublishedSectionFacadeQueriesAPI {
	private static Log log = LogFactory.getLog(PublishedSectionFacadeQueries.class);

	  public IdImpl getId(String id) {
	    return new IdImpl(id);
	  }

	  public IdImpl getId(Long id) {
	    return new IdImpl(id);
	  }

	  public IdImpl getId(long id) {
	    return new IdImpl(id);
	  }

}
