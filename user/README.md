# Users/New Account tool

This is the source code for the Users and New Account tools in Sakai.

## Tool Properties

The following tool properties are available for configuration on a per instance basis:

* create-blurb
  * text to be displayed in the UI (default is empty)
  * ex. Please enter your account details below
* force-eid-equals-email
  * true/false (default is false)
  * if true, removes the field for the user to type their own ID; the eid will be the user's email address
* validate-through-email
  * true/false (default is false)
  * if true, integrates with account-validator:
    1. user creates/requests inactive account
    2. email is sent containing confirmation link
    3. user navigates to link
    4. user fills out the form to complete/activate their account
