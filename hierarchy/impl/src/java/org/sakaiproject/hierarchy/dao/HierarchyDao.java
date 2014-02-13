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

import org.sakaiproject.genericdao.api.GeneralGenericDao;

/**
 * DAO for access to the database for entity broker internal writes
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface HierarchyDao extends GeneralGenericDao {

    /**
     * This will apply migration type fixes to the database as needed
     */
    public void fixupDatabase();

}
