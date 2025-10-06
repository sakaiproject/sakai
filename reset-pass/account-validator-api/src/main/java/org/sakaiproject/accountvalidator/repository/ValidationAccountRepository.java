/**
 * $Id$
 * $URL$
 *
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
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
 */
package org.sakaiproject.accountvalidator.repository;

import java.util.List;
import java.util.Optional;

import org.sakaiproject.accountvalidator.model.ValidationAccount;
import org.sakaiproject.springframework.data.SpringCrudRepository;

/**
 * Repository interface for ValidationAccount persistence operations.
 * Extends SpringCrudRepository which provides standard CRUD operations.
 */
public interface ValidationAccountRepository extends SpringCrudRepository<ValidationAccount, Long> {

	/**
	 * Find a validation account by its validation token.
	 *
	 * @param validationToken the unique validation token
	 * @return Optional containing the validation account if found, empty otherwise
	 */
	Optional<ValidationAccount> findByValidationToken(String validationToken);

	/**
	 * Find a validation account by user ID.
	 *
	 * @param userId the user ID
	 * @return Optional containing the validation account if found, empty otherwise
	 */
	Optional<ValidationAccount> findByUserId(String userId);

	/**
	 * Find all validation accounts with a specific status.
	 *
	 * @param status the validation status
	 * @return List of validation accounts with the given status, empty list if none found
	 */
	List<ValidationAccount> findByStatus(Integer status);
}
