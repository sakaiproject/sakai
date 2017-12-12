/**
 * Copyright (c) 2007-2016 The Apereo Foundation
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
/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational
* Community License, Version 2.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of the
* License at:
*
* http://opensource.org/licenses/ecl2.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.hierarchy.dao;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;
import org.sakaiproject.genericdao.hibernate.HibernateGeneralGenericDao;
import org.sakaiproject.hierarchy.dao.model.HierarchyNodeMetaData;

/**
 * Implementation of DAO
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
@Slf4j
public class HierarchyDaoImpl extends HibernateGeneralGenericDao implements HierarchyDao {

    public void fixupDatabase() {
        // fix up some of the null fields
        long count = 0;
        count = countBySearch(HierarchyNodeMetaData.class, new Search("isDisabled","", Restriction.NULL) );
        if (count > 0) {
            int counter = 0;
            counter += getHibernateTemplate().bulkUpdate("update HierarchyNodeMetaData nm set nm.isDisabled = false where nm.isDisabled is null");
            log.info("Updated " + counter + " HierarchyNodeMetaData.isDisabled fields from null to boolean false");
        }
    }
}
