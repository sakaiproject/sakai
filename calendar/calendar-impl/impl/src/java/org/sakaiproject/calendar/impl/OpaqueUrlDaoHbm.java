package org.sakaiproject.calendar.impl;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.sakaiproject.calendar.api.OpaqueUrl;
import org.sakaiproject.calendar.api.OpaqueUrlDao;
import org.sakaiproject.calendar.dao.hbm.OpaqueUrlHbm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

public class OpaqueUrlDaoHbm extends HibernateDaoSupport implements OpaqueUrlDao {

	private static Logger log = LoggerFactory.getLogger(OpaqueUrlDaoHbm.class);
	
	public OpaqueUrl newOpaqueUrl(String userUUID, String calendarRef) {
		final OpaqueUrlHbm opaqueUrl = new OpaqueUrlHbm(userUUID, calendarRef, UUID.randomUUID().toString());
		getHibernateTemplate().execute(session -> {
            Serializable opaqueUUID = session.save(opaqueUrl);
            // We look for the opaque URL later on in the request so flush.
            session.flush();
            return opaqueUUID;
        });
		return opaqueUrl;
	}

	public OpaqueUrl getOpaqueUrl(String userUUID, String calendarRef) {
		OpaqueUrlHbm example = new OpaqueUrlHbm();
		example.setUserUUID(userUUID);
		example.setCalendarRef(calendarRef);
		List<OpaqueUrl> results = getHibernateTemplate().findByExample(example);
		OpaqueUrl result = null;
		if (results.size() > 0) {
			result = results.get(0);
			if (results.size() > 1) {
				log.warn("More than one result found for search: " + example);
			}
		}
		return result;
	}

	public OpaqueUrl getOpaqueUrl(String opaqueUUID) {
		return (OpaqueUrl) getHibernateTemplate().get(OpaqueUrlHbm.class, opaqueUUID);
	}

	public void deleteOpaqueUrl(String userUUID, String calendarRef) {
		OpaqueUrl opaqueUrl = getOpaqueUrl(userUUID, calendarRef);
		if (opaqueUrl != null) {
			getHibernateTemplate().delete(opaqueUrl);
		} else {
			log.warn("Nothing to delete for userUUID: " + userUUID 
					+ ", calendarRef: " + calendarRef);
		}
	}
}
