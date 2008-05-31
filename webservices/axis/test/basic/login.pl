#!/usr/bin/perl
use SOAP::Lite;
use strict;

# Change these variables as appropriate
my $server = "http://localhost:8080";
my $loginURI = $server . "/sakai-axis/SakaiLogin.jws?wsdl";

my $user = "admin";
my $password = "admin";

### Start a Sakai session

my $soap = SOAP::Lite
    -> proxy($loginURI)
    -> uri($loginURI);

my $session_server =  $soap->loginToServer($user,$password)->result;
(my $session, my $server) = split(",", $session_server);

print "session: $session  server: $server\n";

sleep(10);

my $result = $soap->logout($session);

