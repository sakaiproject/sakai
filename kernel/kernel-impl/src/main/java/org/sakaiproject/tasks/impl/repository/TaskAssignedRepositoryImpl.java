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
