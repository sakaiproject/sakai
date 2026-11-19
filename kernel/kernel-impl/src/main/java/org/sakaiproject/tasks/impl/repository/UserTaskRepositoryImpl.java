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

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;

import org.sakaiproject.tasks.api.Task;
import org.sakaiproject.tasks.api.UserTask;
import org.sakaiproject.tasks.api.repository.UserTaskRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

public class UserTaskRepositoryImpl extends SpringCrudRepositoryImpl<UserTask, Long> implements UserTaskRepository {

    public List<UserTask> findByTaskIdAndUserIdIn(Long taskId, List<String> userIds) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<UserTask> query = cb.createQuery(UserTask.class);
        Root<UserTask> root = query.from(UserTask.class);
        query.where(cb.equal(root.get("task").get("id"), taskId), root.get("userId").in(userIds));

        return session.createQuery(query).list();
    }

    public List<UserTask> findByUserIdAndStartsAfter(String userId, Instant from) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<UserTask> query = cb.createQuery(UserTask.class);
        Root<UserTask> userTask = query.from(UserTask.class);
        query.where(cb.equal(userTask.get("userId"), userId), cb.lessThanOrEqualTo(userTask.get("task").get("starts"), from));

        return session.createQuery(query).list();
    }
    
    public List<UserTask> findByUserIdAndSiteId(String userId, String siteId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<UserTask> query = cb.createQuery(UserTask.class);
        Root<UserTask> root = query.from(UserTask.class);
        query.where(cb.equal(root.get("userId"), userId), cb.equal(root.get("task").get("siteId"), siteId));

        return session.createQuery(query).list();
    }

    public List<UserTask> findByUserIdAndTask_StartsLessThanEqual(String userId, Instant instant) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<UserTask> query = cb.createQuery(UserTask.class);
        Root<UserTask> root = query.from(UserTask.class);
        query.where(cb.equal(root.get("userId"), userId), cb.lessThanOrEqualTo(root.get("task").get("starts"), instant));

        return session.createQuery(query).list();
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

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<UserTask> query = cb.createQuery(UserTask.class);
        Root<UserTask> root = query.from(UserTask.class);
        query.where(cb.equal(root.get("task"), task));

        // Deleted via the session (not a bulk delete) so any UserTask instances already managed
        // in the persistence context are removed from it too, rather than left stale and later
        // tripping a transient-reference check when the deleted Task is flushed.
        session.createQuery(query).list().forEach(session::delete);
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
