<?php 
error_reporting(E_ALL & ~E_NOTICE);
ini_set("display_errors", 1);

// Load up the LTI Support code
require_once 'util/lti_util.php';
require_once 'tp_messages.php';

session_start();
header('Content-Type: text/html; charset=utf-8'); 

// Initialize, all secrets are 'secret', do not set session, and do not redirect
$context = new BLTI("secret", false, false);

?>
<html>
<head>
  <title>Sakai External Tool API Test Harness 2.0</title>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
</head>
<body style="font-family:sans-serif; background-color:#add8e6">
<?php
echo("<p><b>Sakai External Tool API Test Harness 2.0</b></p>\n");



/* 
launch_presentation_document_target=window (ASCII)
launch_presentation_return_url=http://localhost:4000/admin/tool_actions (ASCII)
lti_message_type=ToolProxyRegistrationRequest (ASCII)
reg_key=fdb322c0-9d7b-0130-4f3a-406c8f217861 (ASCII)
reg_password=f26e3b5d90d1b0026b6eead7857bba31 (ASCII)
roles=urn:lti:sysrole:ims/lti/SysAdmin (ASCII)
tc_profile_url=http://localhost:4000/tool_consumer_profiles/fdb32840-9d7b-0130-4f3a-406c8f217861 (ASCII)
user_id=2 (ASCII)
*/

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

$launch_presentation_return_url = $_POST['launch_presentation_return_url'];

$tc_profile_url = $_POST['tc_profile_url'];
if ( strlen($tc_profile_url) > 1 ) {
	echo("Retrieving profile from ".$tc_profile_url."\n");
    $tc_profile_json = do_get($tc_profile_url);
	echo("Retrieved ".strlen($tc_profile_json)." characters.\n");
    // echo($tc_profile_json);
	echo("Parsing JSON..\n");
    $tc_profile = json_decode($tc_profile_json);
    // print_r($tc_profile);echo("\n<hr>\n");
	// TODO: Handle error here...
}

// Find the registration URL

$tc_services = $tc_profile->service_offered;
echo("Found ".count($tc_services)." services profile..\n");
// var_dump($tc_services);
$endpoint = false;
foreach ($tc_services as $tc_service) {
   $id = $tc_service->{'@id'};
    echo("Service: ".$id."\n");
   if ( $id != "ltitcp:ToolProxy.collection" ) continue;
   // var_dump($tc_service);
   $endpoint = $tc_service->endpoint;
}
$cur_url = curPageURL();
$cur_base = str_replace("tp.php","",$cur_url);

$reg_key = $_POST['reg_key'];
$reg_password = $_POST['reg_password'];

$tp_profile = json_decode($tool_proxy);

// Tweak the stock profile
$tp_profile->tool_proxy_guid = $reg_key;
$tp_profile->{'@id'} = $cur_base . uniqid();
$tp_profile->tool_consumer_profile = $tc_profile_url;

// Re-register
$tp_profile->tool_profile->message[0]->path = $cur_url;

// Launch Request
$tp_profile->tool_profile->resource_handler[0]->message[0]->path = "tool.php";

$tp_profile->tool_profile->base_url_choice[0]->secure_base_url = $cur_base;
$tp_profile->tool_profile->base_url_choice[0]->default_base_url = $cur_base;

$tp_profile->security_contract->shared_secret = 'secret';
// print_r($tp_profile);

$reg_key = $_POST['reg_key'];
$reg_password = $_POST['reg_password'];
$body = json_encode($tp_profile);
print "\n<hr/>\n";
echo("Registering....\n");
echo("Endpoint=".$endpoint."\n");
echo($reg_key."\n");
echo($reg_password."\n");
echo(htmlent_utf8($body));
print "\n<hr/>\n";

$response = sendOAuthBodyPOST("POST", $endpoint, $reg_key, $reg_password, "application/vnd.ims.lti.v2.ToolProxy+json", $body);

global $last_base_string;
echo($last_base_strig);

print "\n<hr/>\n";
echo("Html Response:\n");
echo(htmlent_utf8($response));

print "</pre>";

echo('<p><a href="'.$launch_presentation_return_url.'">Continue to launch_presentation_url</a></p>'."\n");

?>
