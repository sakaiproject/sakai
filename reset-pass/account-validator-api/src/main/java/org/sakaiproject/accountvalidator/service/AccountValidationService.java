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
package org.sakaiproject.accountvalidator.service;

import java.util.List;

import org.sakaiproject.accountvalidator.exception.ValidationException;
import org.sakaiproject.accountvalidator.model.ValidationAccount;

/**
 * This service handles the business logic for the account validation tool.
 */
public interface AccountValidationService {

	/**
	 * Get an account by its ID
	 * @param id the account ID
	 * @return the account or null if none found
	 */
	ValidationAccount getVaLidationAcountById(Long id);

	/**
	 * Get a validation account by the token
	 * @param token the validation token (String)
	 * @return the account or null if none found
	 */
	ValidationAccount getVaLidationAcountBytoken(String token);

	/**
	 * Get a validation account for a specific user
	 * @param userId the user ID
	 * @return the account or null if none found
	 */
	ValidationAccount getVaLidationAcountByUserId(String userId);

	/**
	 * Find if an account has been validated
	 * @param userId the user ID
	 * @return true if the account is currently validated
	 */
	boolean isAccountValidated(String userId);

	/**
	 * Determines whether a validation token is expired.
	 * Side effects:
	 * If the ValidationAccount is from reset-pass and the time specified by accountValidator.maxPasswordResetMinutes has elapsed,
	 * this method will set the ValidationAccount's status to expired and the validationReceived time will be set to the current time.
	 * @param va the ValidationAccount whose expired status is to be determined
	 * @return true when
	 * - The ValidationAccount's status is set to expired
	 * - The ValidationAccount is coming from reset-pass and it was emailed longer than x minutes ago where x is configurable by the accountValidator.maxPasswordResetMinutes sakai property.
	 */
	boolean isTokenExpired(ValidationAccount va);

	/**
	 * Create a new validation request for a user
	 * @param userId the user ID
	 * @return the created validation account
	 */
	ValidationAccount createValidationAccount(String userId);

	/**
	 * Create a new validation account
	 * @param userId the user ID
	 * @param newAccount is this a new user
	 * @return the created validation account
	 */
	ValidationAccount createValidationAccount(String userId, boolean newAccount);

	/**
	 * Create a new validation account for userId update
	 * @param userRef existing userId
	 * @param newUserId the new id which the user wants to have as userId
	 * @return the created validation account
	 */
	ValidationAccount createValidationAccount(String userRef, String newUserId);

	/**
	 * Create a validation token for an account of a given status
	 * @param userId the user ID
	 * @param accountStatus the account status
	 * @return the created validation account
	 */
	ValidationAccount createValidationAccount(String userId, Integer accountStatus);

	/**
	 * Merge 2 accounts - the memberships of the first will be moved to the second
	 * @param oldUserReference the old account reference
	 * @param newUserReference the new account reference
	 * @throws ValidationException if merge fails
	 */
	void mergeAccounts(String oldUserReference, String newUserReference) throws ValidationException;

	/**
	 * Delete a validation account
	 * @param toDelete the validation account to delete
	 */
	void deleteValidationAccount(ValidationAccount toDelete);

	/**
	 * Save a validation account
	 * @param toSave the validation account to save
	 */
	void save(ValidationAccount toSave);

	/**
	 * Retrieve a list of accounts by a given status
	 * @param status the status to filter by
	 * @return a List of ValidationAccounts or an empty List if none found
	 */
	List<ValidationAccount> getValidationAccountsByStatus(Integer status);

	/**
	 * Resend the validation to the given user
	 * @param token the validation token
	 */
	void resendValidation(String token);
}
