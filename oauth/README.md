### Sakai OAuth

Sakai OAuth allows any external application to connect to Sakai as any user
thanks to the [OAuth system](http://oauth.net/) 1.0.

### Structure

The project is divided in five modules similar to the structure of many Sakai
projects:

- *api* contains a basic API for OAuth login, such as DAO for accessors and
consumers, and mandatory services.
- *impl* contains the actual code of the default OAuth implementation.
- *pack* is the module defining the [Spring](http://www.springsource.org/)
configuration.
- *tool* is a simple web tool allowing users to remove an authorised *consumer*
and and let administrators add new *consumers* to the system.

### Terminology

The OAuth terminology isn't really strict and may vary depending the libraries
you're working with. Here is what is used in this project:

- **Accessor**, also known as *token (see note)*.  
    Two kinds of accessors are used:
    - *Request*, temporary credentials authorised manually by the *user*.
	This accessor allows a *consumer* to generate one *Access Accessor*.  
        The request accessor goes through three states before being revoked:
        - *new*, the accessor has been created by the OAuth service on
		the request of a *consumer* and is not yet bound to a specific *user*.
        - *authorising*, it's the part during which the *consumer* waits for a
		*user* to grant it an access to the resources.
        - *authorised*, the accessor can be used to generate one (and only one)
		*Access Accessor*.

    - *Access*, used by a *consumer* to directly access some protected resources
	without requiring the *user* to log in.

    *note:* a token is actually the unique identifier of an accessor.  
	*Request accessor* states are specific to Sakai OAuth.
- **Consumer**, also known as *client* or *third party application*.  
It's the application the user will allow to access Sakai on his behalf.
Consumers have to be declared in the OAuth service by an administrator first.
- **Consumer secret**  
Secret password shared only between *one consumer* and the OAuth service.  
It will allow the consumer to *sign* its requests to the OAuth Service to check
the validity of every request.
- [**Access token secret**](http://wiki.oauth.net/w/page/12238553/SignatureMethods)
also known as *signature* or simply *token secret*.  
Secret password generated for each *accessor*, based on the *consumer secret*,
it allows to exchange messages without using directly the actual
*consumer secret* which is really sensitive, while allowing to check the
validity of the request.
- [**Accessor Secret**](http://wiki.oauth.net/w/page/12238502/AccessorSecret).  
Secret independant from the *Consumer secret* and the *Access token secret*, it 
can be defined in advance (the same way a *Consumer secret* is) or during the 
creation of the *request accessor*.  
It is used to replace the *consumer secret* during the generation of the
*Access token secret* for an *access accessor*. This avoids using the
*consumer secret* too much and increase the security.  
*It is not mandatory*
- **Callback URL**  
During the authorisation procedure, a message is sent from the OAuth service to
the *consumer*. This message is initated by the OAuth service and is sent on the
callback URL which has been specified beforehand or during the creation of the
*Request accessor*.
- **User**, also known as *resource owner*. He is the actual Sakai user.

### API

The API relies on three different parts:

- The DAO, used to create new ways to store informations about accessors and consumers
- The service, allowing to specify an implementation respecting the
[OAuth 1.0 workflow](http://hueniverse.com/oauth/guide/workflow/)
- The filters, that will be applied on different parts of Sakai that should be accessible through OAuth
*eg: access and entity-broker*

#### API workflow

This is step by step which method and how they're supposed to be called.

##### [Creation of the *Request accessor*](http://tools.ietf.org/html/rfc5849#section-2.1)

- `OAuthHttpService.handleRequestToken()`, the *customer* sends a direct request
to the OAuthProvider on `/request_token`, asking for a *Request accessor*, the
content of this http request is extracted to obtain a `Consumer`.
- `OAuthService.createRequestAccessor()`, is called by the `OAuthHttpService` in
order to generate a valid *Request accessor*.  
If a *callback URL* or *accessor secret* was defined during the request, they
are included in the *Request accessor* settings.
- A reply containing, the *token*, the *Accessor token secret* is sent back.

The *request accessor* is *new*, isn't linked to any user yet and cannot
generate an *access accessor*.

##### [Authorisation by the *user*](http://tools.ietf.org/html/rfc5849#section-2.2)

- The *consumers* sends the *user* on `/authorize` page of Sakai with the 
*request token* in parameter.
- `AuthorisationServlet` will determine if the *user* is already logged in Sakai
or not, and redirect him to the login screen if needed. The login screen
redirects to `/authorize` with the *request token* in parameter.
- `/authorize` starts the "authorisation procedure" (specific to Sakai OAuth).
    - The *request accessor* is now in the *authorising* step.
    - A `verifier` code is generated and stored with the *request accessor*,
	this verifier allows to check that the result of the authorisation is a
	choice by the *user* (as the *consumer* can't possibly know about this code).
	- A form allowing the *user* to either grant or deny the permission to the
	*consumer* to access the protected resources on his behalf.  
	*The `verifier` is sent with the form*
- Whether the *user* accepts or refuses, `OAuthHttpService.handleRequestAuthorisation()`
is called with the answer of the user, the *token*, the *verifier* and the
current userId.
- If the user accepted, `OAuthHttpService` calls `OAuthService.authoriseAccessor()`
    - The *request accessor* is now in the *authorised* step.
	- The `verifier` is checked and the user ID is verified (**superusers**
	can't use OAuth)
	

### The default implementation

- Any administrator is not permitted authenticate via OAuth.

#### Configuration

The OAuth service can be configured through sakai.properties.

The OAuth service is enabled by default. To disable OAuth on all requests this in the sakai.properties:
`oauth.enabled=false`

### Testing

To test that OAuth is setup correctly you need a tool which will make HTTP requests with OAuth headers.
There is a small Java utility called [oacurl](http://code.google.com/p/oacurl) which can make OAuth requests.

#### Sakai Setup

- Login to your Sakai instance as admin and go to the Administration Workspace.
- Using the Sites tool edit the Administration Workspace (!admin), add a page, on the page put the OAuth
 Administration Tool that allows you to manage OAuth consumers (tool ID: sakai.oauth.admin) and save the site.
- Also using the Sites tool edit the My Workspace template (!user), add a page, on the page put the OAuth Trusted
Applications tool which allows end users to manage their autorized applications (tool ID: sakai.oauth).
- Refresh the browser on the Administration Workspace and you should see the oAuth Admin page appear. 
- In the oAuth Admin tool add a new consumer.
- The consumer key should be unique and meaningful. Eg: oacurl-test.
- The consumer name is displayed to the users when they are allowing it access to their account. Eg oacurl.
- The description should be used to describe what the consumer is doing or used for, again this is shown to
end users.
- The secret is the password for the consumer and should be secure. Eg: ooWaebai2Aep
- Save the new consumer, then enable record mode which will set the permission filter to record permissions
requested and allow them.

#### oacurl Setup

- Download the  oacurl .jar file and put if somewhere sensible.
- Create a properties file (eg oacurl-test.properties) configured with the consumer you have just created.

```
    consumerKey=oacurl-test
    consumerSecret=ooWaebai2Aep
    requestTokenUrl=http://localhost:8080/oauth-tool/request_token
    userAuthorizationUrl=http://localhost:8080/oauth-tool/authorize/
    accessTokenUrl=http://localhost:8080/oauth-tool/access_token
```

- Launch the oacurl login:
    `java -cp oacurl-1.3.0.jar  com.google.oacurl.Login --service-provider=oacurl-test.properties --consumer=oacurl-test.properties`
- Your browser should popup now and ask you to login.
- After logging in as a non-admin user you should be asked to allow the oacurl consumer access to your account.
- After accepting it, a token will be save for later use.
- To test login create a text file in the users My Workspace and save it (eg: test.txt).
- Test the download using oacurl:
    `java -cp oacurl-1.3.0.jar com.google.oacurl.Fetch http://localhost:8080/access/content/user/21096/test.txt`


