# SAML login on the Jakarta branch

The login tool now uses `spring-security-saml2-service-provider` at the same
version as Spring Security, with OpenSAML 5. The old
`spring-security-saml2-core` extension and its OpenSAML 2 helpers are removed.
Existing extension XML files in `sakai.home` must be replaced before enabling
SAML on Tomcat 10.

## Configure the service provider

Enable exactly one sample in the login tool's `applicationContext.xml`:
`xlogin-context.saml.xml`, or `xlogin-context.saml.adfs-prod.xml`. The ADFS
sample retains the `saml-adfs-prod` Spring profile.

Set these values in `sakai.properties`:

```properties
container.login=true
saml.idp.metadata=file:/opt/tomcat/sakai/idp-metadata.xml
saml.entity-id=https://sakai.example.edu/existing-sp-entity-id
```

Use the SP entity ID already registered with the institution's IdP. The old
SP metadata file is replaced by these settings and generated metadata at
`/sakai-login-tool/container/saml/metadata`. Endpoint URLs use Sakai's configured
`serverUrl`, including when Sakai is behind a reverse proxy.

If the metadata includes multiple IdPs, set `saml.idp.entity-id` to select the
trusted issuer. Metadata may be a local resource or HTTPS URL. It is loaded at
startup; remote metadata must be available then. The removed HTTP backup helper
is not automatically replaced with a refresh or backup mechanism. Institutions
that used it should maintain a trusted local metadata file and restart the login
tool after metadata changes, including certificate rotations.

The public authentication endpoints remain:

| Purpose | Path under `/sakai-login-tool` |
| --- | --- |
| Start login | `/container/saml/login` |
| Receive SAML response (HTTP POST) | `/container/saml/SSO` |
| Start logout | `/container/saml/logout` |
| Receive single logout messages | `/container/saml/SingleLogout` |

## Map the Sakai username

The standard sample uses NameID. The ADFS sample uses the UPN claim
`http://schemas.xmlsoap.org/ws/2005/05/identity/claims/upn`.

To replace the old `EppnSamlFilter`, set:

```properties
saml.principal.attribute=urn:oid:1.3.6.1.4.1.5923.1.1.1.6
```

A configured attribute must contain exactly one nonempty string. Missing or
ambiguous attributes fail login. The mapped identity goes through Sakai's
`AuthenticationManager` and `UsageSessionService`; an IdP-authenticated identity
does not establish a Sakai login unless Sakai accepts it. Spring retains the
original NameID and session indexes for SAML logout.

`saml.max-authentication-age` retains the authentication age limit in seconds:
7200 for the standard sample, 86400 for ADFS. Spring's signature, issuer,
destination, audience, subject confirmation, and time validation remain enabled.

## Configure signing and single logout

The existing JKS keystore can be reused:

```properties
saml.keystore.path=file:/opt/tomcat/sakai/samlKeystore.jks
saml.keystore.type=JKS
saml.keystore.alias=samlprodkey
saml.keystore.password=your-store-password
saml.keystore.key-password=your-key-password
login.container.logout.url=https://sakai.example.edu/sakai-login-tool/container/saml/logout
```

These SP credentials sign authentication and logout requests and decrypt
assertions. Single logout requires SP signing credentials and a logout service
in IdP metadata. Without SP credentials, logout is local. The existing GET logout
trigger is retained for Sakai's container logout redirect; inbound SAML logout
messages are validated by Spring Security.

Custom extension configurations, bindings, or user details services must be
migrated to the corresponding Spring Security service-provider APIs. Consult
[Spring's SAML documentation](https://docs.spring.io/spring-security/reference/6.5/servlet/saml2/index.html).

## Validation

`SamlLoginTest` loads the production Spring XML and filter chain and sends real
signed messages. It covers Sakai session creation, attribute mapping, signature
and audience rejection, authentication age, metadata, and local and single logout.

A Playwright SAML flow is not practical in the current local E2E setup: it has no
configured test IdP or SP trust relationship. Before deployment, run browser login
and logout against the institution's IdP, including its signing, encrypted
assertion, and reverse-proxy settings.
