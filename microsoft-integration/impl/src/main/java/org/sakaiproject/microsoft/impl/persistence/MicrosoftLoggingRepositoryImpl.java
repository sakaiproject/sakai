/**
 * Copyright (c) 2024 The Apereo Foundation
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
package org.sakaiproject.microsoft.impl.persistence;

import org.sakaiproject.microsoft.api.model.MicrosoftLog;
import org.sakaiproject.microsoft.api.persistence.MicrosoftLoggingRepository;
import org.sakaiproject.serialization.BasicSerializableRepository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class MicrosoftLoggingRepositoryImpl extends BasicSerializableRepository<MicrosoftLog, String> implements MicrosoftLoggingRepository {

	@Override
	public List<MicrosoftLog> getLogsFromZonedDateTime(ZonedDateTime zonedDateTime) {
		String query = "SELECT ml FROM MicrosoftLog ml WHERE ml.eventDate >= :zonedDateTime";
		query += " ORDER BY ml.id DESC, ml.eventDate DESC";
		return sessionFactory.getCurrentSession().createQuery(query, MicrosoftLog.class)
				.setParameter("zonedDateTime", zonedDateTime)
				.stream()
				.collect(Collectors.toList());
	}
}
