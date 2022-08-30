package org.sakaiproject.grading.api.repository;

import java.util.List;

import org.sakaiproject.grading.api.model.Permission;

import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface PermissionRepository extends SpringCrudRepository<Permission, Long> {

    List<Permission> findByGradebookId(Long gradebookId);
    List<Permission> findByGradebookIdAndUserId(Long gradebookId, String userId);
    List<Permission> findByGradebookIdAndUserIdAndCategoryIdIn(Long gradebookId, String userId, List<Long> categoryIds);
    List<Permission> findByGradebookIdAndUserIdAndCategoryIdIsNullAndFunctionNameIn(Long gradebookId, String userId, List<String> functionNames);
    List<Permission> findByGradebookIdAndUserIdAndGroupIdIsNullAndFunctionNameIn(Long gradebookId, String userId, List<String> functionNames);
    List<Permission> findByGradebookIdAndUserIdAndGroupIdIsNullAndCategoryIdIn(Long gradebookId, String userId, List<Long> categoryIds);
    List<Permission> findByGradebookIdAndCategoryIdIn(Long gradebookId, List<Long> categoryIds);
    List<Permission> findByGradebookIdAndUserIdAndCategoryIdIsNullAndGroupIdIsNull(Long gradebookId, String userId);
    List<Permission> findByGradebookIdAndUserIdAndCategoryIdIsNullAndGroupIdIn(Long gradebookId, String userId, List<String> groupIds);
    List<Permission> findByGradebookIdAndUserIdAndGroupIdIn(Long gradebookId, String userId, List<String> groupIds);
}
