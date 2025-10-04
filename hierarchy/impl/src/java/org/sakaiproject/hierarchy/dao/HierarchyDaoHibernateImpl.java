package org.sakaiproject.hierarchy.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import org.sakaiproject.hierarchy.dao.model.HierarchyNodeMetaData;
import org.sakaiproject.hierarchy.dao.model.HierarchyNodePermission;
import org.sakaiproject.hierarchy.dao.model.HierarchyPersistentNode;

@Slf4j
public class HierarchyDaoHibernateImpl implements HierarchyDao {

    private SessionFactory sessionFactory;
    public void setSessionFactory(SessionFactory sessionFactory) { this.sessionFactory = sessionFactory; }
    private Session s() { return sessionFactory.getCurrentSession(); }

    // No runtime maintenance / fixup here; schema migration should handle data normalization

    // Persistence helpers
    @Override
    public void save(Object entity) {
        Object managed = s().merge(entity);
        if (entity != managed) {
            if (entity instanceof HierarchyNodeMetaData && managed instanceof HierarchyNodeMetaData) {
                ((HierarchyNodeMetaData) entity).setId(((HierarchyNodeMetaData) managed).getId());
            } else if (entity instanceof HierarchyPersistentNode && managed instanceof HierarchyPersistentNode) {
                ((HierarchyPersistentNode) entity).setId(((HierarchyPersistentNode) managed).getId());
            } else if (entity instanceof HierarchyNodePermission && managed instanceof HierarchyNodePermission) {
                ((HierarchyNodePermission) entity).setId(((HierarchyNodePermission) managed).getId());
            }
        }
    }

    @Override
    public void saveSet(Collection<?> entities) {
        if (entities == null) return;
        for (Object e : entities) { save(e); }
    }

    @Override
    public void delete(Object entity) {
        if (entity == null) return;
        if (entity instanceof HierarchyNodeMetaData) {
            Long id = ((HierarchyNodeMetaData) entity).getId();
            HierarchyNodeMetaData managed = id == null ? null : s().get(HierarchyNodeMetaData.class, id);
            if (managed != null) { s().delete(managed); }
            else if (s().contains(entity)) { s().delete(entity); }
        } else if (entity instanceof HierarchyPersistentNode) {
            Long id = ((HierarchyPersistentNode) entity).getId();
            HierarchyPersistentNode managed = id == null ? null : s().get(HierarchyPersistentNode.class, id);
            if (managed != null) { s().delete(managed); }
            else if (s().contains(entity)) { s().delete(entity); }
        } else if (entity instanceof HierarchyNodePermission) {
            Long id = ((HierarchyNodePermission) entity).getId();
            HierarchyNodePermission managed = id == null ? null : s().get(HierarchyNodePermission.class, id);
            if (managed != null) { s().delete(managed); }
            else if (s().contains(entity)) { s().delete(entity); }
        } else {
            if (s().contains(entity)) s().delete(entity);
        }
    }

    @Override
    public void deleteSet(Collection<?> entities) {
        if (entities == null) return;
        for (Object e : entities) { delete(e); }
    }

    @Override
    public void deleteMixedSet(Set<?>[] entitySets) {
        if (entitySets == null) return;
        for (Set<?> set : entitySets) { deleteSet(set); }
    }

    @Override
    public void saveMixedSet(Set<?>[] entitySets) {
        if (entitySets == null) return;
        for (Set<?> set : entitySets) { saveSet(set); }
    }

    @Override
    public <T> T findById(Class<T> type, Long id) {
        return s().get(type, id);
    }

    // Node meta data
    @Override
    public long countNodeMetaByHierarchyId(String hierarchyId) {
        Long c = (Long) s().createQuery(
                "select count(m.id) from HierarchyNodeMetaData m where m.hierarchyId = :hid")
            .setParameter("hid", hierarchyId)
            .uniqueResult();
        return c == null ? 0L : c;
    }

    @Override
    public List<HierarchyNodeMetaData> findNodeMetaByHierarchyId(String hierarchyId) {
        return s().createQuery(
                "from HierarchyNodeMetaData m where m.hierarchyId = :hid", HierarchyNodeMetaData.class)
            .setParameter("hid", hierarchyId)
            .list();
    }

    @Override
    public HierarchyNodeMetaData findRootNodeMetaByHierarchy(String hierarchyId) {
        return s().createQuery(
                "from HierarchyNodeMetaData m where m.hierarchyId = :hid and m.isRootNode = true",
                HierarchyNodeMetaData.class)
            .setParameter("hid", hierarchyId)
            .uniqueResult();
    }

    @Override
    public HierarchyNodeMetaData findNodeMetaByNodeId(Long nodeId) {
        return s().createQuery(
                "from HierarchyNodeMetaData m where m.node.id = :nid", HierarchyNodeMetaData.class)
            .setParameter("nid", nodeId)
            .uniqueResult();
    }

    @Override
    public List<HierarchyNodeMetaData> findNodeMetaByNodeIds(List<Long> nodeIds) {
        if (nodeIds == null || nodeIds.isEmpty()) return List.of();
        return s().createQuery(
                "from HierarchyNodeMetaData m where m.node.id in (:ids)", HierarchyNodeMetaData.class)
            .setParameterList("ids", nodeIds)
            .list();
    }

    @Override
    public List<HierarchyNodeMetaData> findNodeMetaByHierarchyAndPermTokenOrdered(String hierarchyId, String permToken) {
        return s().createQuery(
                "from HierarchyNodeMetaData m where m.hierarchyId = :hid and m.permToken = :pt order by m.node.id",
                HierarchyNodeMetaData.class)
            .setParameter("hid", hierarchyId)
            .setParameter("pt", permToken)
            .list();
    }

    // Nodes
    @Override
    public List<HierarchyPersistentNode> findNodesByIds(List<Long> nodeIds) {
        if (nodeIds == null || nodeIds.isEmpty()) return List.of();
        return s().createQuery(
                "from HierarchyPersistentNode n where n.id in (:ids)", HierarchyPersistentNode.class)
            .setParameterList("ids", nodeIds)
            .list();
    }

    // Permissions
    @Override
    public HierarchyNodePermission findNodePerm(String userId, String nodeId, String permission) {
        return s().createQuery(
                "from HierarchyNodePermission p where p.userId = :uid and p.nodeId = :nid and p.permission = :perm",
                HierarchyNodePermission.class)
            .setParameter("uid", userId)
            .setParameter("nid", nodeId)
            .setParameter("perm", permission)
            .uniqueResult();
    }

    @Override
    public List<HierarchyNodePermission> findNodePerms(String userId, String permission) {
        return s().createQuery(
                "from HierarchyNodePermission p where p.userId = :uid and p.permission = :perm",
                HierarchyNodePermission.class)
            .setParameter("uid", userId)
            .setParameter("perm", permission)
            .list();
    }

    @Override
    public List<HierarchyNodePermission> findNodePerms(String userId, String permission, List<String> nodeIds) {
        if (nodeIds == null || nodeIds.isEmpty()) return new ArrayList<>();
        StringBuilder hql = new StringBuilder("from HierarchyNodePermission p where p.nodeId in (:nids)");
        if (userId != null) hql.append(" and p.userId = :uid");
        if (permission != null) hql.append(" and p.permission = :perm");
        Query<HierarchyNodePermission> q = s().createQuery(hql.toString(), HierarchyNodePermission.class)
            .setParameterList("nids", nodeIds);
        if (userId != null) q.setParameter("uid", userId);
        if (permission != null) q.setParameter("perm", permission);
        return q.list();
    }

    @Override
    public List<HierarchyNodePermission> findNodePermsByNodeIds(List<String> nodeIds) {
        if (nodeIds == null || nodeIds.isEmpty()) return List.of();
        return s().createQuery(
                "from HierarchyNodePermission p where p.nodeId in (:nids)", HierarchyNodePermission.class)
            .setParameterList("nids", nodeIds)
            .list();
    }

    @Override
    public List<HierarchyNodePermission> findNodePermsByUserIds(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) return List.of();
        return s().createQuery(
                "from HierarchyNodePermission p where p.userId in (:uids)", HierarchyNodePermission.class)
            .setParameterList("uids", userIds)
            .list();
    }
}
