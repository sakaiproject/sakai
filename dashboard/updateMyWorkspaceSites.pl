#!/usr/bin/perl -w
 
use SOAP::Lite;
 
my $user = '<admin_userId>';
my $password = '<admin_password>';

# use one single server url instad of the cluster name 
my $loginURI = "https://<server_URL>/sakai-axis/SakaiLogin.jws?wsdl";
my $loginsoap = SOAP::Lite
-> proxy($loginURI)
-> uri($loginURI);

my $scriptURI = "https://<server_url>/sakai-axis/SakaiScript.jws?wsdl";
my $scriptsoap = SOAP::Lite
-> proxy($scriptURI)
-> uri($scriptURI);
 
#START
print "\n";
 
#get session
my $session = $loginsoap->login($user, $password)->result;
print "session is: " . $session . "\n";

# add Dashboard tool to all myworkspace sites
print "begin to add Dashboard tool to all myworkspace sites."."\n";
print $session."\n";
my $r = $scriptsoap->addNewToolToAllWorkspaces($session, "sakai.dashboard", "Dashboard", "Dashboard", 0, 0, false)->result;
print $r;
print "after adding Dashboard tool to all myworkspace sites ".$r."\n";

# logout
my $logout = $loginsoap->logout($session)->result;
print "logging out: " . $logout . "\n";
 
#END
print "\n";
exit;
