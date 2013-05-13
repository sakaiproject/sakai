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
$tc_profile_url = $_POST['tc_profile_url'];
if ( strlen($tc_profile_url) > 1 ) {
    $tc_profile_json = do_get($tc_profile_url);
    // echo($tc_profile_json);
    $tc_profile = json_decode($tc_profile_json);
    // print_r($tc_profile);
}

$cur_url = curPageURL();
echo($cur_url."\n");
$cur_base = str_replace("tp.php","",$cur_url);

$reg_key = $_POST['reg_key'];
$reg_password = $_POST['reg_password'];

$tp_profile = json_decode($tool_proxy);
$tp_profile->tool_proxy_guid = $reg_key;
// Re-register
$tp_profile->tool_profile->message[0]->path = $cur_url;

// Launch Request
$tp_profile->tool_profile->resource_handler[0]->message[0]->path = "tool.php";

$tp_profile->tool_profile->base_url_choice[0]->secure_base_url = $cur_base;
$tp_profile->tool_profile->base_url_choice[0]->default_base_url = $cur_base;

$tp_profile->security_contract->shared_secret = 'secret';
print_r($tp_profile);


print "Raw POST Parameters:\n\n";
ksort($_POST);
foreach($_POST as $key => $value ) {
    if (get_magic_quotes_gpc()) $value = stripslashes($value);
    print htmlentities($key) . "=" . htmlentities($value) . " (".mb_detect_encoding($value).")\n";
}

print "\nRaw GET Parameters:\n\n";
ksort($_GET);
foreach($_GET as $key => $value ) {
    if (get_magic_quotes_gpc()) $value = stripslashes($value);
    print htmlentities($key) . "=" . htmlentities($value) . " (".mb_detect_encoding($value).")\n";
}
print "</pre>";

?>
