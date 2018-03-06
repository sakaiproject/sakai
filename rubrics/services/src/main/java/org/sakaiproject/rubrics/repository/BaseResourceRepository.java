/**********************************************************************************
 *
 * Copyright (c) 2017 The Sakai Foundation
 *
 * Original developers:
 *
 *   Unicon
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.rubrics.repository;

import org.sakaiproject.rubrics.model.BaseResource;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.Serializable;

@NoRepositoryBean
public interface BaseResourceRepository<T extends BaseResource, ID extends Serializable>
        extends PagingAndSortingRepository<T, ID> {

    static final String QUERY_CONTEXT_CONSTRAINT = "(resource.metadata.ownerId = ?#{principal.contextId} " +
            "or 1 = ?#{principal.isSuperUser() ? 1 : 0})";

    @Override
    @PreAuthorize("canWrite(#resource)")
    <S extends T> S save(@Param("resource") S resource);

    /**
     * Updating all or part of the resource collection is not supported. Always results in a
     * {@link HttpStatus#METHOD_NOT_ALLOWED} response via {@code RestResource(exported = false)}.
     * <p>Individual resource {@link #save} operations must be used instead.</p>
     *
     * @param iterable set of rubrics
     */
    @Override
    @RestResource(exported = false)
    <S extends T> Iterable<S> save(Iterable<S> iterable);

    @Override
    @PreAuthorize("canWrite(#resource)")
    void delete(@Param("resource") T resource);

    /**
     * Deleting all the resource collection is not supported. Always results in a
     * {@link HttpStatus#METHOD_NOT_ALLOWED} response via {@code RestResource(exported = false)}.
     * <p>Individual resource {@link #delete} operations must be used instead.</p>
     */
    @Override
    @RestResource(exported = false)
    void deleteAll();

    /**
     * Deleting all or part of the resource collection is not supported. Always results in a
     * {@link HttpStatus#METHOD_NOT_ALLOWED} response via {@code RestResource(exported = false)}.
     * <p>Individual resource {@link #delete} operations must be used instead.</p>
     *
     * @param iterable set of rubrics
     */
    @Override
    @RestResource(exported = false)
    void delete(Iterable<? extends T> iterable);
}
