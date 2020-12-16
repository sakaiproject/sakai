package org.sakaiproject.tasks.api;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TaskService {

    UserTask createSingleUserTask(UserTaskAdapterBean transfer);
    UserTask saveUserTask(UserTaskAdapterBean transfer);
    void removeUserTask(Long userTaskId);
    Task createTask(Task task, Set<String> users, Integer priority);
    Task saveTask(Task task);
    Optional<Task> getTask(String reference);
    List<UserTaskAdapterBean> getAllTasksForCurrentUser();
    List<UserTask> getCurrentUserTasks(String userId);
    void removeTask(Task task);
    void removeTaskByReference(String reference);
    void completeUserTaskByReference(String reference, List<String> userIds);
}
