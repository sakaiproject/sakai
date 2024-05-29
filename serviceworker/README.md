Making Service Worker Run in your Tomcat
----------------------------------------

In order for the Service Worker to work for all of Sakai, it needs to
be served at the root.

    /sakai-service-worker.js

However it is bad idea to overwrite the `ROOT.war` webapp as many
Sakai installations tweak the ROOT webapp area with content, redirect
code, or even a favicon.  

So the solution is to put this content into a webapp called `sakai-root`
and include instructions on how to edit `server.xml` and setup a `Catalina`
configuration to setup URL rewriting.

Once this is setup, it is pretty easy to add URL rewrite entries.

The first change is to add a `org.apache.catalina.valves.rewrite.RewriteValve`
entry to the `Host` entry in the `server.xml` as shown below:

     <Host name="localhost"  appBase="webapps"
            unpackWARs="true" autoDeploy="true">

        <!-- SingleSignOn valve, share authentication between web applications
             Documentation at: /docs/config/valve.html -->
        <!--
        <Valve className="org.apache.catalina.authenticator.SingleSignOn" />
        -->

        <!-- Access log processes all example.
             Documentation at: /docs/config/valve.html
             Note: The pattern used is equivalent to using pattern="common" -->
        <Valve className="org.apache.catalina.valves.AccessLogValve" directory="logs"
               prefix="localhost_access_log" suffix=".txt"
               pattern="%h %l %u %t &quot;%r&quot; %s %b" />

        <Valve className="org.apache.catalina.valves.rewrite.RewriteValve" />

      </Host>

Note the `name` attribute in the `Host` entry.  Then create the
following directories and file, changing `localhost` in the path
to the name of your `Host` entry.

    tomcat-dir/conf/Catalina/localhost/rewrite.config 

In this file put entries like the following:

    RewriteCond %{SERVLET_PATH} !-f
    RewriteRule ^/sakai-service-worker.js$ /sakai-root/sakai-service-worker.js [L]
    RewriteRule ^/favicon.ico$ /sakai-root/favicon.ico [L]
    RewriteRule ^/$ /sakai-root/index.html [L]

This works mostly like Apache HTTPd's `mod_rewrite`.  This makes it so Tomcat
serves the resources at the root path as desired.


References
----------

https://tomcat.apache.org/tomcat-9.0-doc/rewrite.html

