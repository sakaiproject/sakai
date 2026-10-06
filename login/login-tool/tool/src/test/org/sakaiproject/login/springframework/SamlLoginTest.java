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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.LogoutResponse;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.Signer;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.login.tool.SkinnableLogin;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.user.api.AuthenticationManager;
import org.sakaiproject.user.api.AuthenticationException;
import org.sakaiproject.user.api.ExternalTrustedEvidence;
import org.sakaiproject.user.api.Evidence;
import org.sakaiproject.util.Xml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.w3c.dom.Document;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Exercise the production XML and filter chain with real signed SAML messages. */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = SamlTestConfiguration.class)
public class SamlLoginTest {
    private static final String ACS = "https://sakai.example.test/sakai-login-tool/container/saml/SSO";
    @Autowired private FilterChainProxy springSecurityFilterChain;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private UsageSessionService usageSessionService;
    @Autowired private SessionManager sessionManager;
    private Session sakaiSession;

    @Before
    public void setUp() throws Exception {
        reset(authenticationManager, usageSessionService, sessionManager);
        SecurityContextHolder.clearContext();
        sakaiSession = mock(Session.class);
        when(sessionManager.getCurrentSession()).thenReturn(sakaiSession);
        org.sakaiproject.user.api.Authentication identity = mock(org.sakaiproject.user.api.Authentication.class);
        when(identity.getUid()).thenReturn("user-id");
        when(identity.getEid()).thenReturn("student@example.test");
        when(authenticationManager.authenticate(any(Evidence.class))).thenReturn(identity);
        when(usageSessionService.login(anyString(), anyString(), anyString(), nullable(String.class), anyString()))
                .thenReturn(true);
    }

    @Test
    public void signedResponseCreatesSakaiSessionAndPreservesNameId() throws Exception {
        when(sakaiSession.getAttribute(Tool.HELPER_DONE_URL)).thenReturn("https://sakai.example.test/portal/site/course");
        MockHttpServletRequest request = callback(response(true, true, "https://sakai.example.test/saml", Instant.now()));
        MockHttpServletResponse response = filter(request);
        assertEquals("https://sakai.example.test/portal/site/course", response.getRedirectedUrl());
        ArgumentCaptor<Evidence> evidence = ArgumentCaptor.forClass(Evidence.class);
        verify(authenticationManager).authenticate(evidence.capture());
        assertEquals("student@example.test", ((ExternalTrustedEvidence) evidence.getValue()).getIdentifier());
        verify(usageSessionService).login(eq("user-id"), eq("student@example.test"), anyString(), nullable(String.class),
                eq(UsageSessionService.EVENT_LOGIN_CONTAINER));
        verify(sakaiSession).setAttribute(SkinnableLogin.ATTR_CONTAINER_SUCCESS, SkinnableLogin.ATTR_CONTAINER_SUCCESS);
        SecurityContext context = (SecurityContext) request.getSession().getAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        Saml2AuthenticatedPrincipal principal = (Saml2AuthenticatedPrincipal) context.getAuthentication().getPrincipal();
        assertEquals("idp-name-id", principal.getName());
        assertEquals("sakai", principal.getRelyingPartyRegistrationId());
    }

    @Test
    public void unsignedResponseCannotLogIntoSakai() throws Exception {
        assertRejected(response(false, true, "https://sakai.example.test/saml", Instant.now()));
    }

    @Test
    public void wrongAudienceCannotLogIntoSakai() throws Exception {
        assertRejected(response(true, true, "https://another.example.test/saml", Instant.now()));
    }

    @Test
    public void oldAuthenticationCannotLogIntoSakai() throws Exception {
        assertRejected(response(true, true, "https://sakai.example.test/saml", Instant.now().minusSeconds(8000)));
    }

    @Test
    public void missingPrincipalAttributeClearsSpringAuthentication() throws Exception {
        MockHttpServletRequest request = callback(response(true, false, "https://sakai.example.test/saml", Instant.now()));
        assertEquals("https://sakai.example.test/portal/xlogin", filter(request).getRedirectedUrl());
        verifyNoInteractions(authenticationManager, usageSessionService);
        assertNull(request.getSession(false));
    }

    @Test
    public void sakaiRejectionClearsSpringAuthentication() throws Exception {
        when(authenticationManager.authenticate(any(Evidence.class))).thenThrow(new AuthenticationException("Rejected test identity"));
        MockHttpServletRequest request = callback(response(true, true, "https://sakai.example.test/saml", Instant.now()));
        assertEquals("https://sakai.example.test/portal/xlogin", filter(request).getRedirectedUrl());
        verifyNoInteractions(usageSessionService);
        assertNull(request.getSession(false));
    }

    @Test
    public void loginEndpointProducesAuthenticationRequest() throws Exception {
        MockHttpServletResponse response = filter(request("GET", "/container/saml/login"));
        assertEquals(200, response.getStatus());
        assertTrue(response.getContentAsString().contains("https://idp.example.test/login"));
        assertTrue(response.getContentAsString().contains("SAMLRequest"));
    }

    @Test
    public void metadataContainsExistingAssertionConsumerUrl() throws Exception {
        MockHttpServletResponse response = filter(request("GET", "/container/saml/metadata"));
        assertEquals(200, response.getStatus());
        assertTrue(response.getContentAsString().contains(ACS));
    }

    @Test
    public void localLogoutTerminatesSakaiSession() throws Exception {
        MockHttpServletResponse response = filter(request("GET", "/container/saml/logout"));
        assertEquals("https://sakai.example.test/portal", response.getRedirectedUrl());
        verify(usageSessionService).logout();
    }

    @Test
    public void relyingPartyLogoutSendsSignedRequestToIdp() throws Exception {
        MockHttpServletRequest login = callback(response(true, true, "https://sakai.example.test/saml", Instant.now()));
        filter(login);
        MockHttpServletRequest logout = request("GET", "/container/saml/logout");
        logout.setSession((MockHttpSession) login.getSession());
        MockHttpServletResponse response = filter(logout);
        assertEquals(200, response.getStatus());
        assertTrue(response.getContentAsString().contains("https://idp.example.test/logout"));
        assertTrue(response.getContentAsString().contains("SAMLRequest"));
        LogoutRequest message = (LogoutRequest) unmarshal(new String(Base64.getDecoder().decode(
                formValue(response, "SAMLRequest")), StandardCharsets.UTF_8));
        assertNotNull(message.getSignature());
        assertEquals("idp-name-id", message.getNameID().getValue());
        assertEquals("urn:oasis:names:tc:SAML:2.0:nameid-format:transient", message.getNameID().getFormat());
        verify(usageSessionService).logout();
    }

    @Test
    public void logoutResponseCompletesAtPortal() throws Exception {
        MockHttpServletRequest login = callback(response(true, true, "https://sakai.example.test/saml", Instant.now()));
        filter(login);
        MockHttpServletRequest logout = request("GET", "/container/saml/logout");
        logout.setSession((MockHttpSession) login.getSession());
        MockHttpServletResponse form = filter(logout);
        LogoutRequest message = (LogoutRequest) unmarshal(new String(Base64.getDecoder().decode(
                formValue(form, "SAMLRequest")), StandardCharsets.UTF_8));
        String xml = """
                <samlp:LogoutResponse xmlns:samlp="urn:oasis:names:tc:SAML:2.0:protocol" xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"
                    ID="_%s" Version="2.0" IssueInstant="%s" InResponseTo="%s"
                    Destination="https://sakai.example.test/sakai-login-tool/container/saml/SingleLogout">
                  <saml:Issuer>https://idp.example.test</saml:Issuer>
                  <samlp:Status><samlp:StatusCode Value="urn:oasis:names:tc:SAML:2.0:status:Success"/></samlp:Status>
                </samlp:LogoutResponse>
                """.formatted(UUID.randomUUID(), Instant.now(), message.getID());
        LogoutResponse saml = (LogoutResponse) unmarshal(xml);
        Signature signature = signature();
        saml.setSignature(signature);
        XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(saml).marshall(saml);
        Signer.signObject(signature);
        MockHttpServletRequest callback = request("POST", "/container/saml/SingleLogout");
        callback.setSession((MockHttpSession) logout.getSession());
        callback.setParameter("RelayState", formValue(form, "RelayState"));
        String signed = net.shibboleth.shared.xml.SerializeSupport.nodeToString(saml.getDOM());
        callback.setParameter("SAMLResponse", Base64.getEncoder().encodeToString(signed.getBytes(StandardCharsets.UTF_8)));
        assertEquals("https://sakai.example.test/portal", filter(callback).getRedirectedUrl());
        verify(usageSessionService).logout();
    }

    @Test
    public void signedIdpLogoutTerminatesSakaiSession() throws Exception {
        MockHttpServletRequest login = callback(response(true, true, "https://sakai.example.test/saml", Instant.now()));
        filter(login);
        MockHttpServletRequest logout = request("POST", "/container/saml/SingleLogout");
        logout.setSession((MockHttpSession) login.getSession());
        logout.setParameter("SAMLRequest", logoutMessage(true));
        MockHttpServletResponse response = filter(logout);
        assertEquals(200, response.getStatus());
        assertTrue(response.getContentAsString().contains("SAMLResponse"));
        verify(usageSessionService).logout();
    }

    @Test
    public void unsignedIdpLogoutCannotTerminateSakaiSession() throws Exception {
        MockHttpServletRequest login = callback(response(true, true, "https://sakai.example.test/saml", Instant.now()));
        filter(login);
        MockHttpServletRequest logout = request("POST", "/container/saml/SingleLogout");
        logout.setSession((MockHttpSession) login.getSession());
        logout.setParameter("SAMLRequest", logoutMessage(false));
        assertEquals(401, filter(logout).getStatus());
        verify(usageSessionService, never()).logout();
    }

    private void assertRejected(String samlResponse) throws Exception {
        assertEquals("https://sakai.example.test/portal/xlogin", filter(callback(samlResponse)).getRedirectedUrl());
        verifyNoInteractions(authenticationManager, usageSessionService);
    }

    private MockHttpServletRequest callback(String samlResponse) {
        MockHttpServletRequest request = request("POST", "/container/saml/SSO");
        request.setParameter("SAMLResponse", Base64.getEncoder().encodeToString(samlResponse.getBytes(StandardCharsets.UTF_8)));
        return request;
    }

    private MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, "/sakai-login-tool" + path);
        request.setContextPath("/sakai-login-tool");
        request.setServletPath("/container");
        request.setPathInfo(path.substring("/container".length()));
        request.setScheme("https");
        request.setSecure(true);
        request.setServerName("sakai.example.test");
        request.setServerPort(443);
        return request;
    }

    private MockHttpServletResponse filter(MockHttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        springSecurityFilterChain.doFilter(request, response, new MockFilterChain());
        return response;
    }

    private String response(boolean signed, boolean attribute, String audience, Instant authenticatedAt) throws Exception {
        Instant now = Instant.now();
        String xml = """
                <samlp:Response xmlns:samlp="urn:oasis:names:tc:SAML:2.0:protocol" xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"
                    ID="_%s" Version="2.0" IssueInstant="%s" Destination="%s">
                  <saml:Issuer>https://idp.example.test</saml:Issuer>
                  <samlp:Status><samlp:StatusCode Value="urn:oasis:names:tc:SAML:2.0:status:Success"/></samlp:Status>
                  <saml:Assertion ID="_%s" Version="2.0" IssueInstant="%s">
                    <saml:Issuer>https://idp.example.test</saml:Issuer>
                    <saml:Subject><saml:NameID Format="urn:oasis:names:tc:SAML:2.0:nameid-format:transient">idp-name-id</saml:NameID>
                      <saml:SubjectConfirmation Method="urn:oasis:names:tc:SAML:2.0:cm:bearer">
                        <saml:SubjectConfirmationData Recipient="%s" NotOnOrAfter="%s"/>
                      </saml:SubjectConfirmation>
                    </saml:Subject>
                    <saml:Conditions NotBefore="%s" NotOnOrAfter="%s">
                      <saml:AudienceRestriction><saml:Audience>%s</saml:Audience></saml:AudienceRestriction>
                    </saml:Conditions>
                    <saml:AuthnStatement AuthnInstant="%s" SessionIndex="test-session">
                      <saml:AuthnContext><saml:AuthnContextClassRef>urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport</saml:AuthnContextClassRef></saml:AuthnContext>
                    </saml:AuthnStatement>
                    %s
                  </saml:Assertion>
                </samlp:Response>
                """.formatted(UUID.randomUUID(), now, ACS, UUID.randomUUID(), now, ACS, now.plusSeconds(300),
                        now.minusSeconds(60), now.plusSeconds(300), audience, authenticatedAt,
                        attribute ? """
                          <saml:AttributeStatement><saml:Attribute Name="urn:oid:1.3.6.1.4.1.5923.1.1.1.6">
                            <saml:AttributeValue xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xs="http://www.w3.org/2001/XMLSchema" xsi:type="xs:string">student@example.test</saml:AttributeValue>
                          </saml:Attribute></saml:AttributeStatement>
                          """ : "");
        Document document = Xml.createSecureDocumentBuilderFactory().newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        Response response = (Response) XMLObjectProviderRegistrySupport.getUnmarshallerFactory()
                .getUnmarshaller(document.getDocumentElement()).unmarshall(document.getDocumentElement());
        if (signed) {
            Signature signature = signature();
            Assertion assertion = response.getAssertions().get(0);
            assertion.setSignature(signature);
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(response).marshall(response);
            Signer.signObject(signature);
        }
        return net.shibboleth.shared.xml.SerializeSupport.nodeToString(
                XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(response).marshall(response));
    }

    private String logoutMessage(boolean signed) throws Exception {
        String xml = """
                <samlp:LogoutRequest xmlns:samlp="urn:oasis:names:tc:SAML:2.0:protocol" xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"
                    ID="_%s" Version="2.0" IssueInstant="%s" Destination="https://sakai.example.test/sakai-login-tool/container/saml/SingleLogout">
                  <saml:Issuer>https://idp.example.test</saml:Issuer>
                  <saml:NameID>idp-name-id</saml:NameID>
                  <samlp:SessionIndex>test-session</samlp:SessionIndex>
                </samlp:LogoutRequest>
                """.formatted(UUID.randomUUID(), Instant.now());
        Document document = Xml.createSecureDocumentBuilderFactory().newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        LogoutRequest logout = (LogoutRequest) XMLObjectProviderRegistrySupport.getUnmarshallerFactory()
                .getUnmarshaller(document.getDocumentElement()).unmarshall(document.getDocumentElement());
        if (signed) {
            Signature signature = signature();
            logout.setSignature(signature);
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(logout).marshall(logout);
            Signer.signObject(signature);
        }
        String message = net.shibboleth.shared.xml.SerializeSupport.nodeToString(
                XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(logout).marshall(logout));
        return Base64.getEncoder().encodeToString(message.getBytes(StandardCharsets.UTF_8));
    }

    private Signature signature() throws Exception {
        PrivateKey key;
        X509Certificate certificate;
        try (InputStream input = new ClassPathResource("saml/idp-key.pem").getInputStream()) {
            key = RsaKeyConverters.pkcs8().convert(input);
        }
        try (InputStream input = new ClassPathResource("saml/idp-cert.pem").getInputStream()) {
            certificate = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(input);
        }
        Signature signature = (Signature) XMLObjectProviderRegistrySupport.getBuilderFactory()
                .getBuilder(Signature.DEFAULT_ELEMENT_NAME).buildObject(Signature.DEFAULT_ELEMENT_NAME);
        signature.setSigningCredential(new BasicX509Credential(certificate, key));
        signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        return signature;
    }

    private org.opensaml.core.xml.XMLObject unmarshal(String xml) throws Exception {
        Document document = Xml.createSecureDocumentBuilderFactory().newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        return XMLObjectProviderRegistrySupport.getUnmarshallerFactory()
                .getUnmarshaller(document.getDocumentElement()).unmarshall(document.getDocumentElement());
    }

    private String formValue(MockHttpServletResponse response, String name) throws Exception {
        Matcher matcher = Pattern.compile("name=\"" + name + "\"[^>]*value=\"([^\"]+)\"")
                .matcher(response.getContentAsString());
        assertTrue("SAML form must contain " + name, matcher.find());
        return matcher.group(1);
    }
}
