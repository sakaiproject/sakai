/**
 * Copyright (c) 2003-2025 The Apereo Foundation
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
package org.sakaiproject.login.saml;

/**
 * A SAML authentication provider that uses the UPN (User Principal Name) attribute for username extraction
 */
public class UpnSamlAuthenticationConverter extends SakaiSamlAuthenticationConverter {
    
    /**
     * Creates a provider that uses the UPN attribute for username extraction
     */
    public UpnSamlAuthenticationConverter() {
        setUsernameAttributeName("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/upn");
    }
}