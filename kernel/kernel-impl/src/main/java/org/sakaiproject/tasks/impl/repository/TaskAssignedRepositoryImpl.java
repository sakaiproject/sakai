/*
 * Copyright (c) 2003-2022 The Apereo Foundation
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
package org.sakaiproject.tasks.impl.repository;

import java.util.List;

import org.hibernate.Session;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.sakaiproject.tasks.api.Task;
import org.sakaiproject.tasks.api.TaskAssigned;
import org.sakaiproject.tasks.api.repository.TaskAssignedRepository;

public class TaskAssignedRepositoryImpl extends SpringCrudRepositoryImpl<TaskAssigned, Long> implements TaskAssignedRepository {

	@Override
	public List<TaskAssigned> findByTaskId(Long taskId) {
		Session session = sessionFactory.getCurrentSession();
        return session.createQuery("select u from TaskAssigned u where task.id = :taskId")
        	.setParameter("taskId", taskId).list();
	}

	@Override
	public void deleteByTask(Task task) {
		Session session = sessionFactory.getCurrentSession();
        session.createQuery("delete from TaskAssigned where task = :task")
            .setParameter("task", task).executeUpdate();		
	}

}
