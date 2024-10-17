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

    public List<UserTask> findBySiteId(String siteId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<UserTask> query = cb.createQuery(UserTask.class);
        Root<UserTask> userTask = query.from(UserTask.class);
        query.where(cb.equal(userTask.get("task").get("siteId"), siteId));

        return session.createQuery(query).list();
    }

    public List<UserTask> findByTaskIdAndUserIdIn(Long taskId, List<String> userIds) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<UserTask> query = cb.createQuery(UserTask.class);
        Root<UserTask> userTask = query.from(UserTask.class);
        query.where(cb.equal(userTask.get("task").get("id"), taskId), userTask.get("userId").in(userIds));

        return session.createQuery(query).list();
    }

    public List<UserTask> findByUserIdAndStartsAfterAndSoftDeleted(String userId, Instant from, Boolean softDeleted) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<UserTask> query = cb.createQuery(UserTask.class);
        Root<UserTask> userTask = query.from(UserTask.class);
        query.where(cb.and(cb.equal(userTask.get("userId"), userId) , cb.lessThanOrEqualTo(userTask.get("task").get("starts"), from))
                        , cb.or(cb.isNull(userTask.get("softDeleted")), cb.equal(userTask.get("softDeleted"), softDeleted)));

        return session.createQuery(query).list();
    }
    
    public List<UserTask> findByUserIdAndSiteId(String userId, String siteId) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<UserTask> query = cb.createQuery(UserTask.class);
        Root<UserTask> userTask = query.from(UserTask.class);
        query.where(cb.equal(userTask.get("userId"), userId), cb.equal(userTask.get("task").get("siteId"), siteId));

        return session.createQuery(query).list();
    }

    public List<UserTask> findByUserIdAndTask_StartsLessThanEqual(String userId, Instant earlierThan) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<UserTask> query = cb.createQuery(UserTask.class);
        Root<UserTask> userTask = query.from(UserTask.class);
        query.where(cb.equal(userTask.get("userId"), userId), cb.lessThanOrEqualTo(userTask.get("task").get("starts"), earlierThan));

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

    public int deleteByTask(Task task) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<UserTask> cd = cb.createCriteriaDelete(UserTask.class);
        Root<UserTask> userTask = cd.from(UserTask.class);
        cd.where(cb.equal(userTask.get("task"), task));

        return session.createQuery(cd).executeUpdate();
    }

    public int deleteByTaskAndUserIdNotIn(Task task, Set<String> users) {

        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<UserTask> cd = cb.createCriteriaDelete(UserTask.class);
        Root<UserTask> root = cd.from(UserTask.class);
        cd.where(cb.equal(root.get("task"), task), cb.not(root.get("userId").in(users)));

        return session.createQuery(cd).executeUpdate();
    }
}
