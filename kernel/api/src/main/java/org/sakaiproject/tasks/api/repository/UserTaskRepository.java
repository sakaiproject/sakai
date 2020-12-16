/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.tasks.api.repository;

import java.util.List;
import java.time.Instant;

import org.sakaiproject.tasks.api.Task;
import org.sakaiproject.tasks.api.UserTask;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface UserTaskRepository extends SpringCrudRepository<UserTask, Long> {

    List<UserTask> findByTaskIdAndUserIdIn(Long taskId, List<String> userIds);
    List<UserTask> findByUserId(String userId);
    List<UserTask> findByUserIdAndTask_StartsLessThanEqual(String userId, Instant instant);
    void deleteByTask(Task task);
}
