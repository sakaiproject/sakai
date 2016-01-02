<?php 
error_reporting(E_ALL & ~E_NOTICE);
ini_set("display_errors", 1);

// Load up the LTI Support code
require_once 'util/lti_util.php';
require_once 'util/json_indent.php';  // Until all PHP's are > 5.4

session_start();
header('Content-Type: text/html; charset=utf-8'); 

$lti_message_type = $_POST["lti_message_type"];

global $div_id;
$div_id = 1;

function togglePre($title, $content) {
    global $div_id;
    echo('<h4>'.$title);
    echo(' (<a href="#" onclick="dataToggle('."'".$div_id."'".');return false;">Toggle</a>)</h4>'."\n");
    echo('<pre id="'.$div_id.'" style="display:none; border: solid 1px">'."\n");
    echo($content);
    echo("</pre>\n");
    $div_id = $div_id + 1;
}

function die_with_return_url($message) { 
    error_log($message);
    echo('<p style="color:red;">Error: '.htmlentities($message)."</p>\n");
    if ( isset($_POST['launch_presentation_return_url']) ) {
        $launch_presentation_return_url = $_POST['launch_presentation_return_url'];
        if ( strpos($launch_presentation_return_url,'?') > 0 ) {
            $launch_presentation_return_url .= '&';
        } else {
            $launch_presentation_return_url .= '?';
        }
        $launch_presentation_return_url .= "status=failure";
        $launch_presentation_return_url .= "&lti_errormsg=" . urlencode($message);
        echo('<p><a href="'.$launch_presentation_return_url.'">Continue to launch_presentation_url</a></p>'."\n");
    }
    die();
}

?>
<html>
<head>
  <title>Sakai External Tool API Test Harness 2.0</title>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<script language="javascript"> 
function dataToggle(divName) {
    var ele = document.getElementById(divName);
    if(ele.style.display == "block") {
        ele.style.display = "none";
    }
    else {
        ele.style.display = "block";
    }
} 
  //]]> 
</script>
</head>
<body style="font-family:sans-serif; background-color:#add8e6">
<?php
echo("<p><b>Sakai LTI 2.0 Test Harness</b></p>\n");

ksort($_POST);
$output = "";
foreach($_POST as $key => $value ) {
    if (get_magic_quotes_gpc()) $value = stripslashes($value);
    $output = $output . htmlent_utf8($key) . "=" . htmlent_utf8($value) . " (".mb_detect_encoding($value).")\n";
}
togglePre("Raw POST Parameters", $output);


$output = "";
ksort($_GET);
foreach($_GET as $key => $value ) {
    if (get_magic_quotes_gpc()) $value = stripslashes($value);
    $output = $output . htmlent_utf8($key) . "=" . htmlent_utf8($value) . " (".mb_detect_encoding($value).")\n";
}
if ( strlen($output) > 0 ) togglePre("Raw GET Parameters", $output);

echo("<pre>\n");

$secret = isset($_SESSION['split_secret']) ? $_SESSION['split_secret'] : 'secret';
$re_register = false;
$tool_proxy_guid = false;
if ( $lti_message_type == "ToolProxyReregistrationRequest" ) {
    $reg_key = $_POST['oauth_consumer_key'];
    $tool_proxy_guid = isset($_POST['tool_proxy_guid']) ? $_POST['tool_proxy_guid'] : false;
    $reg_password = "secret";
    $re_register = false;
    $context = new BLTI($secret, false, false);
    if ( $context->valid ) {
        print "<p style=\"color:green\">Launch Validated.<p>\n";
    } else {
        print "<p style=\"color:red\">Could not establish context: ".$context->message."<p>\n";
        print "<p>Base String:<br/>\n";
        print htmlent_utf8($context->basestring);
        print "<br/></p>\n";

        echo('<a href="basecheck.php?b='.urlencode($context->basestring).'" target="_blank">Compare This Base String</a><br/>');
        print "<br/></p>\n";
        echo ("<p>Continuing re-registration...</p>\n");
    }
} else if ( $lti_message_type == "ToolProxyRegistrationRequest" ) {
    $reg_key = $_POST['reg_key'];
    $reg_password = $_POST['reg_password'];
} else {
    echo("</pre>");
    die_with_return_url("lti_message_type not supported ".$lti_message_type);
}

$cur_url = curPageURL();
$cur_base = str_replace("tp.php","",$cur_url);
require_once 'tp_messages.php';

$launch_presentation_return_url = $_POST['launch_presentation_return_url'];

$tc_profile_url = $_POST['tc_profile_url'];
if ( strlen($tc_profile_url) > 1 ) {
    echo("Retrieving profile from ".$tc_profile_url."\n");
    $tc_profile_json = do_get($tc_profile_url);
    echo("Retrieved ".strlen($tc_profile_json)." characters.\n");
    echo("</pre>\n");
    togglePre("Retrieved Consumer Profile",$tc_profile_json);
    $tc_profile = json_decode($tc_profile_json);
    if ( $tc_profile == null ) {
        die_with_return_url("Unable to parse tc_profile error=".json_last_error());
    }
} else {
    die_with_return_url("We must have a tc_profile_url to continue...");
}

// Find the registration URL

echo("<pre>\n");
$tc_guid = $tc_profile->guid;
echo("Tool Consumer guid: ".$tc_guid."\n");
$tc_services = $tc_profile->service_offered;
echo("Found ".count($tc_services)." services profile..\n");
if ( count($tc_services) < 1 ) die_with_return_url("At a minimum, we need the service to register ourself - doh!\n");

// var_dump($tc_services);
$register_url = false;
$result_url = false;
foreach ($tc_services as $tc_service) {
    // var_dump($tc_service);
    $formats = $tc_service->{'format'};
    $actions = $tc_service->{'action'};
    $type = $tc_service->{'@type'};
    $id = $tc_service->{'@id'};
    echo("Service id=".$id."\n");
    foreach($formats as $format) {
        // Canvas includes two entries - only one with POST
        // The POST entry is the one with a real URL
        if ( ! in_array("POST",$actions) ) continue;
        if ( $format != "application/vnd.ims.lti.v2.toolproxy+json" ) continue;
        $register_url = $tc_service->endpoint;
    }
}

if ( $register_url == false ) die_with_return_url("Must have an application/vnd.ims.lti.v2.toolproxy+json service available in order to do tool_registration.");

// unset($_SESSION['result_url']);
// if ( $result_url !== false ) $_SESSION['result_url'] = $result_url;

echo("\nFound an application/vnd.ims.lti.v2.toolproxy+json service - nice for us...\n");

// Check for capabilities
$tc_capabilities = $tc_profile->capability_offered;
echo("Found ".count($tc_capabilities)." capabilities..\n");
if ( count($tc_capabilities) < 1 ) die_with_return_url("No capabilities found!\n");
echo("Optional money collection phase complete...\n");
echo("<hr/>");

$tp_profile = json_decode($tool_proxy);
if ( $tp_profile == null ) {
    togglePre("Tool Proxy Raw",htmlent_utf8($tool_proxy));
    $body = json_encode($tp_profile);
    $body = json_indent($body);
    togglePre("Tool Proxy Parsed",htmlent_utf8($body));
    die_with_return_url("Unable to parse our own internal Tool Proxy (DOH!) error=".json_last_error()."\n");
}

// Tweak the stock profile
$tp_profile->tool_consumer_profile = $tc_profile_url;

// Copy over the context
$tp_profile->{'@context'} = $tc_profile->{'@context'};
for($i=0; $i < count($tp_profile->{'@context'}); $i++ ) {
    $ctx = $tp_profile->{'@context'}[$i];
    if ( is_string($ctx) && strpos($ctx,"http://purl.imsglobal.org/ctx/lti/v2/ToolConsumerProfile") !== false ) {
	$tp_profile->{'@context'}[$i] = "http://purl.imsglobal.org/ctx/lti/v2/ToolProxy";
    }
}

// Re-register
$tp_profile->tool_profile->message[0]->path = $cur_url;
$tp_profile->tool_profile->product_instance->product_info->product_family->vendor->website = $cur_base;
$tp_profile->tool_profile->product_instance->product_info->product_family->vendor->timestamp = "2013-07-13T09:08:16-04:00";

// I want this *not* to be unique per instance
$tp_profile->tool_profile->product_instance->guid = "urn:sakaiproject:unit-test";

$tp_profile->tool_profile->product_instance->service_provider->guid = "http://www.sakaiproject.org/";

// Launch Request
$tp_profile->tool_profile->resource_handler[0]->message[0]->path = "tool.php";
$tp_profile->tool_profile->resource_handler[0]->resource_type->code = "sakai-api-test-01";

// Ask for all the parameter mappings we are interested in
// Canvas rejects us if  we ask for a custom parameter that they did 
// not offer as capability
$newparms = array();
foreach($desired_parameters as $parameter) {
    if ( ! in_array($parameter, $tc_capabilities) ) continue;
    $np = new stdClass();
    $np->variable = $parameter;
    $np->name = strtolower(str_replace(".","_",$parameter));
    $newparms[] = $np;
}
// var_dump($newparms);
$tp_profile->tool_profile->resource_handler[0]->message[0]->parameter = $newparms;

// Cause an error on registration
// $tp_profile->tool_profile->resource_handler[0]->message[0]->enabled_capability[] = "Give.me.the.database.password";

$tp_profile->tool_profile->base_url_choice[0]->secure_base_url = $cur_base;
$tp_profile->tool_profile->base_url_choice[0]->default_base_url = $cur_base;

// Make a split-secret if desired
$oauth_splitsecret = in_array('OAuth.splitSecret', $tc_capabilities);

// We don't do oauth_split secret here because we have no storage
// You can test split secret with this harness but launches will fail.
// Comment out the line below to make it so this registers with split secret
// But then expect LTI 2.x launches to fail with a bad signature.
$oauth_splitsecret = false;

$tp_half_shared_secret = false;
if ( $oauth_splitsecret ) {
    $tp_half_shared_secret = bin2hex( openssl_random_pseudo_bytes( 512/8 ) ) ;
    if ( strlen($tp_half_shared_secret) != 128 ) {
        echo('<p style="color: red">Warning secret length of '.strlen($tp_half_shared_secret)." should be 128</p>\n");
    }
    $tp_profile->security_contract->tp_half_shared_secret = $tp_half_shared_secret;
    echo("Provider Half Secret:\n".$tp_half_shared_secret."\n");
} else {
    $tp_profile->security_contract->shared_secret = $secret;
}

// Ask for the kitchen sink...
$hmac256 = false;
foreach($tc_capabilities as $capability) {
    if ( "basic-lti-launch-request" == $capability ) continue;

    if ( $oauth_splitsecret === false && "OAuth.splitSecret" == $capability ) continue;

    if ( "OAuth.hmac-sha256" == $capability ) {
	// This is not fully supported beyond registration so we never accept this
        // $hmac256 = 'HMAC-SHA256';
    }

    // promote these up to the top level capabilities
    if ( "OAuth.splitSecret" == $capability || "OAuth.hmac-sha256" == $capability ) {
        $tp_profile->enabled_capability[] = $capability;
    }

    if ( in_array($capability, $tp_profile->tool_profile->resource_handler[0]->message[0]->enabled_capability) ) continue;
    $tp_profile->tool_profile->resource_handler[0]->message[0]->enabled_capability[] = $capability;
}


$tp_services = array();
foreach($tc_services as $tc_service) {
    // var_dump($tc_service);
    $tp_service = new stdClass;
    $tp_service->{'@type'} = 'RestServiceProfile';
    $tp_service->action = $tc_service->action;
    $tp_service->service = $tc_service->{'@id'};
    $tp_services[] = $tp_service;
}
// var_dump($tp_services);
$tp_profile->security_contract->tool_service = $tp_services;
// print_r($tp_profile);

$body = json_encode($tp_profile);
$body = json_indent($body);

echo("Registering....\n");
echo("Register Endpoint=".$register_url."\n");
echo("Result Endpoint=".$result_url."\n");
echo("reg_key=".$reg_key."\n");
echo("reg_password=".$reg_password."\n");
echo("</pre>\n");

if ( strlen($register_url) < 1 || strlen($reg_key) < 1 || strlen($reg_password) < 1 ) die_with_return_url("Cannot call register_url - insufficient data...\n");

togglePre("Registration Request",htmlent_utf8($body));

$more_headers = array();
if ( $lti_message_type == "ToolProxyReregistrationRequest" ) {
    $more_headers[] = 'VND-IMS-CONFIRM-URL: '.$cur_base.'tp_commit.php?correlation=49201-48842';
}

$response = sendOAuthBody("POST", $register_url, $reg_key, $reg_password, "application/vnd.ims.lti.v2.toolproxy+json", $body, $more_headers, $hmac256);

togglePre("Registration Request Headers",htmlent_utf8(get_body_sent_debug()));

global $LastOAuthBodyBaseString;
togglePre("Registration Request Base String",$LastOAuthBodyBaseString);

togglePre("Registration Response Headers",htmlent_utf8(get_body_received_debug()));

togglePre("Registration Response",htmlent_utf8(json_indent($response)));

$tc_half_shared_secret = false;
if ( $last_http_response == 201 || $last_http_response == 200 ) {

    $responseObject = json_decode($response);

    $tc_tool_proxy_guid = $responseObject->tool_proxy_guid;
    if ( $tc_tool_proxy_guid ) {
        echo('<p>Tool consumer returned tool_proxy_guid='.$tc_tool_proxy_guid."</p>\n");
        if ( $tool_proxy_guid && $tool_proxy_guid != $tc_tool_proxy_guid ) {
            echo('<p style="color: red;">Error: Returned tool_proxy_guid did not match launch tool_proxy_guid='.$tool_proxy_guid."</p>\n");
        }
    } else {
        echo('<p style="color: red;">Error: Tool Consumer did not include tool_proxy_guid in its response.</p>'."\n");
    }

    if ( $oauth_splitsecret && $tp_half_shared_secret ) {
        if ( isset($responseObject->tc_half_shared_secret) ) {
            $tc_half_shared_secret = $responseObject->tc_half_shared_secret;
            echo("<p>tc_half_shared_secret: ".$tc_half_shared_secret."</p>\n");
            if ( strlen($tc_half_shared_secret) != 128 ) {
                echo('<p style="color: red">Warning secret length of '.strlen($tc_half_shared_secret)." should be 128</p>\n");
            }
            $split_secret = $tc_half_shared_secret . $tp_half_shared_secret;
            $_SESSION['split_secret'] = $split_secret;
            echo("<p>Split Secret: ".$split_secret."</p>\n");
        } else {
            die_with_return_url("<p>Error: Tool Consumer did not provide oauth_splitsecret</p>\n");
        }
    }

    if ( strpos($launch_presentation_return_url,'?') > 0 ) {
        $launch_presentation_return_url .= '&';
    } else {
        $launch_presentation_return_url .= '?';
    }
    $launch_presentation_return_url .= "status=success";
    $launch_presentation_return_url .= "&tool_proxy_guid=" . urlencode($tc_tool_proxy_guid);

  echo('<p><a href="'.$launch_presentation_return_url.'">Continue to launch_presentation_url</a></p>'."\n");
  exit();
}

die_with_return_url("Registration failed, http code=".$last_http_response."\n");

// Check to see if they slid us the base string...
$responseObject = json_decode($response);
if ( $responseObject != null ) {
    $base_string = $responseObject->base_string;
    if ( strlen($base_string) > 0 && strlen($LastOAuthBodyBaseString) > 0 && $base_string != $LastOAuthBodyBaseString ) {
        $compare = compare_base_strings($LastOAuthBodyBaseString, $base_string);
        togglePre("Compare Base Strings (ours first)",htmlent_utf8($compare));
    }
}

?>
