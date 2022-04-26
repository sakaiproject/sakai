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

package org.sakaiproject.tasks.impl.repository;

import java.util.List;
import java.util.Set;
import java.time.Instant;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import org.sakaiproject.tasks.api.Task;
import org.sakaiproject.tasks.api.UserTask;
import org.sakaiproject.tasks.api.repository.UserTaskRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

public class UserTaskRepositoryImpl extends SpringCrudRepositoryImpl<UserTask, Long> implements UserTaskRepository {

    public List<UserTask> findByTaskIdAndUserIdIn(Long taskId, List<String> userIds) {

        Session session = sessionFactory.getCurrentSession();

        return (List<UserTask>) session.createCriteria(UserTask.class)
            .add(Restrictions.eq("task.id", taskId))
            .add(Restrictions.in("userId", userIds)).list();
    }

    public List<UserTask> findByUserId(String userId) {

        Session session = sessionFactory.getCurrentSession();

        return (List<UserTask>) session.createCriteria(UserTask.class)
            .add(Restrictions.eq("userId", userId)).list();
    }
    
    public List<UserTask> findByUserIdAndSiteId(String userId, String siteId) {

        Session session = sessionFactory.getCurrentSession();

        return session.createQuery("select u from UserTask u where userId = :userId and task.siteId = :siteId")
        	.setParameter("userId", userId)
        	.setParameter("siteId", siteId).list();
    }

    public List<UserTask> findByUserIdAndTask_StartsLessThanEqual(String userId, Instant instant) {

        Session session = sessionFactory.getCurrentSession();

        return (List<UserTask>) session.createCriteria(UserTask.class)
            .add(Restrictions.eq("userId", userId))
            .add(Restrictions.le("task.starts", instant)).list();
    }

    public List<UserTask> findByTask_SiteId(String siteId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<UserTask> cq = cb.createQuery(UserTask.class);
        Root<UserTask> root = cq.from(UserTask.class);
        Join<UserTask, Task> taskJoin = root.join("task");
        cq.select(root);
        cq.where(cb.equal(taskJoin.get("siteId"), siteId));

        return session.createQuery(cq).list();

    }

    public void deleteByTask(Task task) {

        Session session = sessionFactory.getCurrentSession();

        session.createQuery("delete from UserTask where task = :task")
            .setParameter("task", task).executeUpdate();
        session.delete(task);
    }

    public void deleteByTaskAndUserIdNotIn(Task task, Set<String> users) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<UserTask> cd = cb.createCriteriaDelete(UserTask.class);
        Root<UserTask> root = cd.from(UserTask.class);
        cd.where(cb.equal(root.get("task"), task), cb.not(root.get("userId").in(users)));

        session.createQuery(cd).executeUpdate();
    }
}
