This text (sakai_basic_test) probes sakai's simplest web services.

You need to have an Apache running port 80 and put the php file into its
web space.

You must get PEAR and Pear's SOAP into your PHP (see PEAR readme) before the script 
will work (http://pear.php.net/)..


The URLS in the php are hard-coded to localhost Sakai - maybe a later version 
will use forms to make things more dynamic.

Notice that the URLs are 8081 because it expects a Sakai running on port 8080 and 
the TCP Monitor running on 8081 mapping to 8080 and snooping the packets.

$ sh tunnel.sh &

The TCP Monitor is really cool and lets you watch the web-serves GETS, POSTs, 
and responses and is an invaluable debugging tool.

If you don't want to use the monitor just change the URLs to 8080.

So to run the test, 

(1) Have Sakai2 running on 8080 with admin/admin as valid account
    opptionaly tail catalina.out
(2) Have properly configured Apache with PHP and SOAP on 80
(3) Have TCP Monitor mapping 8081 to 8080
(4) Go to the php url: http://localhost/~csev/sakai_basic_test.php

Your output should look like sakai_basic_test.html and your
TCPMonitor should have lots of fun stuff to explore.

/Chuck
Fri Apr 15 14:30:20 EDT 2005
