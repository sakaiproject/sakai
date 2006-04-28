OVERVIEW

Sakai 2.0 shipped with the capability to support web services, but with very few web service end points. Sakai 2.1 adds a number of Web Services end-points (SakaiScript). SakaiScript was led by Steven Githens and a number of developers in the Sakai community.


ENABLING SAKAISCRIPT

Sakai web services and SakaiScript are included in the release and need to be turned on using a property in the Sakai Properties file:

# Indicates whether or not we allow web-service logins
webservices.allowlogin=false

This parameter is set to false so as to make sure that no one can robot-guess Sakai passwords using web services out of the box. If you want to support the login and session-establishment, you must set this proprty to true in your Sakai instance.


SECURITY

Any site enabling web-services should understand the security implications of Sakai web services. Web services should be run over HTTPS in any production environment as IDs, Passwords, and Sakai Session Keys are pased back and forth in plain text using SOAP. This is quite reasonable (and equivalent to how browser-based login and browser cookies work) but depends on HTTPS for protection of the materials contained in the SOAP envelope.