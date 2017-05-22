package org.sakaiproject.tool.assessment.facade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

public class PublishedSectionFacadeQueries extends HibernateDaoSupport
		implements PublishedSectionFacadeQueriesAPI {
	private Logger log = LoggerFactory.getLogger(PublishedSectionFacadeQueries.class);

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
