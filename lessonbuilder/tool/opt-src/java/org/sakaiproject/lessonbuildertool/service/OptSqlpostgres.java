/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Charles Hedrick, hedrick@rutgers.edu
 *
 * Copyright (c) 2010 Rutgers, the State University of New Jersey
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");                                                                
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.lessonbuildertool.service;

/*
 *
 * @author Charles Hedrick <hedrick@rutgers.edu>
 * 
 */

public class OptSqlpostgres implements OptSql {

    public String Assignment2InsertGroupSql () {
	return "insert into A2_ASSIGN_GROUP_T (assignment_group_id, version, assignment_id, group_ref) values (A2_ASSIGN_GROUP_S.nextval, 0, ?, ?)";
    }

}
