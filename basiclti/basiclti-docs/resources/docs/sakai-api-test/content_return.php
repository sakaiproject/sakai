<?php 
error_reporting(E_ALL & ~E_NOTICE);
ini_set("display_errors", 1);

// Load up the LTI Support code
require_once 'util/lti_util.php';

session_start();
header('Content-Type: text/html; charset=utf-8'); 

// Initialize, all secrets are 'secret', do not set session, and do not redirect
$key = isset($_POST['oauth_consumer_key']) ? $_POST['oauth_consumer_key'] : false;
$secret = "secret";
$context = new BLTI($secret, false, false);
?>
<html>
<head>
  <title>Sakai Test Content Return EndPoint</title>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
</head>
<body style="font-family:sans-serif; background-color:#add8e6">
<?php
echo("<p><b>Sakai Content Return Test</b></p>\n");

if ( $context->valid ) {
   print "<p style=\"color:green\">Launch Validated.<p>\n";
} else {
    print "<p style=\"color:red\">Could not establish context: ".$context->message."<p>\n";
}
print "<p>Base String:<br/>\n";
print htmlent_utf8($context->basestring);
print "<br/></p>\n";

echo('<a href="basecheck.php?b='.urlencode($context->basestring).'" target="_blank">Compare This Base String</a><br/>');
print "<br/></p>\n";

print "<pre>\n";
print "Raw POST Parameters:\n\n";
ksort($_POST);
foreach($_POST as $key => $value ) {
    if (get_magic_quotes_gpc()) $value = stripslashes($value);
    print htmlent_utf8($key) . "=" . htmlent_utf8($value) . " (".mb_detect_encoding($value).")\n";
}

print "\nRaw GET Parameters:\n\n";
ksort($_GET);
foreach($_GET as $key => $value ) {
    if (get_magic_quotes_gpc()) $value = stripslashes($value);
    print htmlent_utf8($key) . "=" . htmlent_utf8($value) . " (".mb_detect_encoding($value).")\n";
}
print "</pre>";

?>
