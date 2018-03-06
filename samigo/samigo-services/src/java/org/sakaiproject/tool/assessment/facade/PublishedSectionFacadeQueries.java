/**
 * Copyright (c) 2005-2017 The Apereo Foundation
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

package org.sakaiproject.tool.assessment.facade;

import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

public class PublishedSectionFacadeQueries extends HibernateDaoSupport
		implements PublishedSectionFacadeQueriesAPI {

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
