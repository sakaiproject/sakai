/**
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.login.springframework;

import java.io.Serial;

import lombok.Getter;
import org.opensaml.saml.saml2.core.NameID;
import org.springframework.security.saml2.provider.service.authentication.DefaultSaml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;

/** Retain the validated NameID qualifiers across the HTTP session for single logout. */
@Getter
public class SakaiSamlPrincipal extends DefaultSaml2AuthenticatedPrincipal {
    @Serial private static final long serialVersionUID = 1L;

    private final String nameIdFormat;
    private final String nameQualifier;
    private final String spNameQualifier;

    public SakaiSamlPrincipal(Saml2AuthenticatedPrincipal principal, NameID nameId) {
        super(principal.getName(), principal.getAttributes(), principal.getSessionIndexes());
        setRelyingPartyRegistrationId(principal.getRelyingPartyRegistrationId());
        nameIdFormat = nameId.getFormat();
        nameQualifier = nameId.getNameQualifier();
        spNameQualifier = nameId.getSPNameQualifier();
    }
}
