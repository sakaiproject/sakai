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

import java.util.List;

import org.apache.velocity.app.VelocityEngine;
import org.opensaml.common.binding.security.SAMLProtocolMessageXMLSignatureSecurityPolicyRule;
import org.opensaml.ws.security.SecurityPolicyRule;
import org.opensaml.xml.parse.ParserPool;
import org.opensaml.xml.signature.SignatureTrustEngine;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.processor.HTTPPostBinding;

public class XmlSignatureOnlyHTTPPostBinding extends HTTPPostBinding {

    public XmlSignatureOnlyHTTPPostBinding(ParserPool parserPool, VelocityEngine velocityEngine) {
        super(parserPool, velocityEngine);
    }

    @Override
    public void getSecurityPolicy(List<SecurityPolicyRule> securityPolicy, SAMLMessageContext samlContext) {
        SignatureTrustEngine engine = samlContext.getLocalTrustEngine();
        securityPolicy.add(new SAMLProtocolMessageXMLSignatureSecurityPolicyRule(engine));
    }
}
