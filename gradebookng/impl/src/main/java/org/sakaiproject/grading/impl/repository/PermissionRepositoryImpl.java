package org.sakaiproject.grading.impl.repository;

import java.util.List;

import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.transaction.annotation.Transactional;

import org.sakaiproject.grading.api.model.Permission;
import org.sakaiproject.grading.api.repository.PermissionRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

public class PermissionRepositoryImpl extends SpringCrudRepositoryImpl<Permission, Long>  implements PermissionRepository {

    @Transactional(readOnly = true)
    public List<Permission> findByGradebookId(Long gradebookId) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Permission> query = cb.createQuery(Permission.class);
        Root<Permission> p = query.from(Permission.class);
        query.where(cb.equal(p.get("gradebookId"), gradebookId));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<Permission> findByGradebookIdAndUserId(Long gradebookId, String userId) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Permission> query = cb.createQuery(Permission.class);
        Root<Permission> p = query.from(Permission.class);
        query.where(cb.and(cb.equal(p.get("gradebookId"), gradebookId),
                            cb.equal(p.get("userId"), userId)));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<Permission> findByGradebookIdAndUserIdAndCategoryIdIn(Long gradebookId, String userId, List<Long> categoryIds) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Permission> query = cb.createQuery(Permission.class);
        Root<Permission> p = query.from(Permission.class);
        query.where(cb.and(cb.equal(p.get("gradebookId"), gradebookId),
                            cb.equal(p.get("userId"), userId),
                            p.get("categoryId").in(categoryIds)));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<Permission> findByGradebookIdAndUserIdAndCategoryIdIsNullAndFunctionNameIn(Long gradebookId, String userId, List<String> functionNames) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Permission> query = cb.createQuery(Permission.class);
        Root<Permission> p = query.from(Permission.class);
        query.where(cb.and(cb.equal(p.get("gradebookId"), gradebookId),
                            cb.equal(p.get("userId"), userId),
                            cb.isNull(p.get("categoryId")),
                            p.get("functionName").in(functionNames)));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<Permission> findByGradebookIdAndUserIdAndGroupIdIsNullAndFunctionNameIn(Long gradebookId, String userId, List<String> functionNames) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Permission> query = cb.createQuery(Permission.class);
        Root<Permission> p = query.from(Permission.class);
        query.where(cb.and(cb.equal(p.get("gradebookId"), gradebookId),
                            cb.equal(p.get("userId"), userId),
                            cb.isNull(p.get("groupId")),
                            p.get("functionName").in(functionNames)));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<Permission> findByGradebookIdAndUserIdAndGroupIdIsNullAndCategoryIdIn(Long gradebookId, String userId, List<Long> categoryIds) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Permission> query = cb.createQuery(Permission.class);
        Root<Permission> p = query.from(Permission.class);
        query.where(cb.and(cb.equal(p.get("gradebookId"), gradebookId),
                            cb.equal(p.get("userId"), userId),
                            cb.isNull(p.get("groupId")),
                            p.get("categoryId").in(categoryIds)));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<Permission> findByGradebookIdAndCategoryIdIn(Long gradebookId, List<Long> categoryIds) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Permission> query = cb.createQuery(Permission.class);
        Root<Permission> p = query.from(Permission.class);
        query.where(cb.and(cb.equal(p.get("gradebookId"), gradebookId), p.get("categoryId").in(categoryIds)));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<Permission> findByGradebookIdAndUserIdAndCategoryIdIsNullAndGroupIdIsNull(Long gradebookId, String userId) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Permission> query = cb.createQuery(Permission.class);
        Root<Permission> p = query.from(Permission.class);
        query.where(cb.and(cb.equal(p.get("gradebookId"), gradebookId),
                            cb.equal(p.get("userId"), userId),
                            cb.isNull(p.get("categoryId")),
                            cb.isNull(p.get("groupId"))));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<Permission> findByGradebookIdAndUserIdAndCategoryIdIsNullAndGroupIdIn(Long gradebookId, String userId, List<String> groupIds) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Permission> query = cb.createQuery(Permission.class);
        Root<Permission> p = query.from(Permission.class);
        query.where(cb.and(cb.equal(p.get("gradebookId"), gradebookId),
                            cb.equal(p.get("userId"), userId),
                            cb.isNull(p.get("categoryId")),
                            p.get("groupId").in(groupIds)));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<Permission> findByGradebookIdAndUserIdAndGroupIdIn(Long gradebookId, String userId, List<String> groupIds) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Permission> query = cb.createQuery(Permission.class);
        Root<Permission> p = query.from(Permission.class);
        query.where(cb.and(cb.equal(p.get("gradebookId"), gradebookId),
                            cb.equal(p.get("userId"), userId),
                            p.get("groupId").in(groupIds)));
        return session.createQuery(query).list();
    }
}
