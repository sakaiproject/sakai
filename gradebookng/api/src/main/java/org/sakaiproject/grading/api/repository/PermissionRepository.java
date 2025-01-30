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
