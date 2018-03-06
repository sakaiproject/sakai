/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.calendar.impl;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

import org.sakaiproject.calendar.api.OpaqueUrl;
import org.sakaiproject.calendar.api.OpaqueUrlDao;
import org.sakaiproject.calendar.dao.hbm.OpaqueUrlHbm;

@Slf4j
public class OpaqueUrlDaoHbm extends HibernateDaoSupport implements OpaqueUrlDao {
	
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
