/**
 * Copyright (c) 2003-2019 The Apereo Foundation
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
package org.sakaiproject.authz.api;

public class AuthzRealmLockException extends Exception {

    private String reference;
    private AuthzGroup.RealmLockMode lockMode;

    public AuthzRealmLockException(String message) {
        super(message);
    }

    public AuthzRealmLockException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthzRealmLockException(String message, String reference, AuthzGroup.RealmLockMode lockMode, Throwable cause) {
        super(message, cause);
        this.reference = reference;
        this.lockMode = lockMode;
    }

    @Override
    public String getMessage() {
        String lockMessage = "";
        if (reference != null) {
            lockMessage = ", caused by reference: " + reference;
        }
        if (lockMode != null) {
            lockMessage = ", with lock: " + lockMode;
        }
        return super.getMessage() + lockMessage;
    }
}
