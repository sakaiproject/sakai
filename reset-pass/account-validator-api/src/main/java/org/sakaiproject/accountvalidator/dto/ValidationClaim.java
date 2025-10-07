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
package org.sakaiproject.accountvalidator.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for account validation claim submissions.
 * 
 * <p>This class represents the data submitted by users when claiming an existing account
 * during the account validation workflow. It is used when a user who already has an account
 * wants to link their existing credentials to a validation token, allowing account merging
 * or membership transfer operations.</p>
 * 
 * <p>The typical workflow involves:</p>
 * <ol>
 *   <li>User receives a validation token via email</li>
 *   <li>User accesses the validation page with the token</li>
 *   <li>User chooses to claim the account using existing credentials</li>
 *   <li>This object captures the submitted form data</li>
 *   <li>System validates credentials and merges accounts if valid</li>
 * </ol>
 * 
 * <p>This class differs from {@link org.sakaiproject.accountvalidator.model.ValidationAccount}
 * which represents the persisted validation record in the database, while ValidationClaim
 * represents transient form submission data.</p>
 * 
 * @see org.sakaiproject.accountvalidator.model.ValidationAccount
 * @see org.sakaiproject.accountvalidator.service.AccountValidationService
 */
@Data
public class ValidationClaim {

    /**
     * The internal user ID from the existing account that the user is claiming.
     * This is the Sakai internal UUID for the user, not the user's login ID (EID).
     * 
     * <p>This field is typically populated during the claim process after successful
     * authentication of the user's credentials.</p>
     */
    private String userId;

    /**
     * The unique validation token associated with this claim request.
     * This token is generated when a validation account is created and sent to the user via email.
     * 
     * <p>The token serves as a secure, time-limited identifier that links the validation
     * request to a specific {@link org.sakaiproject.accountvalidator.model.ValidationAccount}
     * record in the database.</p>
     * 
     * <p>Format: Typically a randomly generated alphanumeric string</p>
     * 
     * @see org.sakaiproject.accountvalidator.model.ValidationAccount#getValidationToken()
     */
    private String validationToken;

    /**
     * The password submitted by the user for authentication.
     * This is the user's existing account password used to verify their identity
     * before allowing the account claim operation.
     * 
     * <p>This field is transient and should never be persisted to the database.
     * It is only used during the authentication process within a single request lifecycle.</p>
     * 
     * <p><strong>Security Note:</strong> This value should be handled securely and cleared
     * from memory after authentication is complete.</p>
     */
    private String password1;

    /**
     * Confirmation password field (currently unused in claim workflow).
     * Reserved for potential future use where password confirmation might be required
     * during the claim process.
     * 
     * <p>In the current implementation, only {@link #password1} is used for authentication
     * during account claiming. This field is included for consistency with password validation
     * workflows where password confirmation is required.</p>
     */
    private String password2;

    /**
     * The enterprise ID (login username) of the existing account being claimed.
     * This is the user-facing login identifier, as opposed to the internal {@link #userId}.
     * 
     * <p>Example: For a user with email login, this might be "john.doe@university.edu"
     * or for a username-based system, it might be "jdoe".</p>
     * 
     * <p>This value is used in conjunction with {@link #password1} to authenticate
     * the user via {@link org.sakaiproject.user.api.UserDirectoryService#authenticate(String, String)}</p>
     * 
     * @see org.sakaiproject.user.api.User#getEid()
     */
    private String userEid;
}
