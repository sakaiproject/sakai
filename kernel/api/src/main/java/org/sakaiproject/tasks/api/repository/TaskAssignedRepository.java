package org.sakaiproject.tasks.api.repository;

import java.util.List;

import org.sakaiproject.springframework.data.SpringCrudRepository;
import org.sakaiproject.tasks.api.Task;
import org.sakaiproject.tasks.api.TaskAssigned;

public interface TaskAssignedRepository extends SpringCrudRepository<TaskAssigned, Long>{
	
	List<TaskAssigned> findByTaskId(Long taskId);
	void deleteByTask(Task task);

}
