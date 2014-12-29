@echo off
echo To run JMeter against an SSL server, you may need to install the 
echo server's certificate as a trusted certificate.
echo For example, this batch file installs the University of Michigan
echo certificate.
echo The certificate is from: http://www.umich.edu/~umweb/umwebCA.pem
echo This batch file installs it using the "keytool" utility that comes 
echo with Java.  Set the password of the keystore to something over 6 
echo characters, anything you want.  (The example jmeter.properties
echo uses "mypassword" as the password).
echo/
echo Importing Michigan certificate into Java's keystore... 

keytool -import -file umwebCA.pem

echo/
echo Now you must edit your jmeter/bin/jmeter.properties.  Uncomment the property
echo "javax.net.ssl.keyStorePassword" and set its value to the keystore
echo password you set just now.




