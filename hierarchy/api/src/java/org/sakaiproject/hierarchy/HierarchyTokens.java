/**
 * Copyright (c) 2007-2014 The Apereo Foundation
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

package org.sakaiproject.hierarchy;

import java.util.Map;
import java.util.Set;


/**
 * This adds in the ability to define permissions key token searching
 *
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface HierarchyTokens {

   /**
    * Find all nodes for a hierarchy associated with a token key
    * 
    * @param hierarchyId a unique id which defines the hierarchy
    * @param permToken a permissions token key
    * @return a set of nodeIds, empty if no nodes found
    */
   public Set<String> getNodesWithToken(String hierarchyId, String permToken);

   /**
    * Find all the nodes for a hierarchy associated with a set of token keys
    * 
    * @param hierarchyId a unique id which defines the hierarchy
    * @param permTokens an array of permissions token keys
    * @return a map of tokenKey -> set of nodeIds, empty if no nodes found
    */
   public Map<String, Set<String>> getNodesWithTokens(String hierarchyId, String[] permTokens);

}
