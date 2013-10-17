<?php
if (version_compare(PHP_VERSION, '5.3.0') >= 0) {
 error_reporting(E_ALL & ~E_NOTICE & ~E_DEPRECATED);
} else { 
 error_reporting(E_ALL & ~E_WARNING & ~E_NOTICE);
}

$old_error_handler = set_error_handler("myErrorHandler");

function myErrorHandler($errno, $errstr, $errfile, $errline)
{
    // echo("YO ". $errorno . $errstr . "\n");
    if ( strpos($errstr, 'deprecated') !== false ) return true;
    return false;
}

ini_set("display_errors", 1);

/*
if ( !isset ( $_REQUEST['b64'] ) ) {
   die("Missing b64 parameter");
}


$b64 = $_REQUEST['b64'];
session_id(md5($b64));
*/
session_start();

require_once("util/lti_util.php");

$cur_url = curPageURL();
$cur_base = str_replace("tp.php","",$cur_url);

// For my application, We only allow application/xml
$request_headers = OAuthUtil::get_headers();
$hct = $request_headers['Content-Type'];
if ( ! isset($hct) ) $hct = $request_headers['Content-type'];
/*
if ($hct != 'application/xml' ) {
   header('Content-Type: text/plain');
   print_r($request_headers);
   die("Must be content type xml, found ".$hct);
}
*/

header('Content-Type: application/vnd.ims.lti.v2.ToolProxy.id+json; charset=utf-8;');

/*
$b64 = base64_decode($b64);
$b64 = explode(":::", $b64);
*/

$oauth_consumer_key = $b64[0];
$oauth_consumer_secret = $b64[1];

$oauth_consumer_key = '98765';
$oauth_consumer_secret = 'secret';

$response = '{
  "@context": "http://purl.imsglobal.org/ctx/lti/v2/ToolProxyId",
  "@type": "ToolProxy",
  "@id": "http://localhost:4000/toolproxies/0df4b410-9e38-0130-4f3c-406c8f217861",
  "tool_proxy_guid": "0df4b410-9e38-0130-4f3c-406c8f217861"
}';

$json_response = json_decode($response);
$json_response->{'@id'} = $cur_base . uniqid();
$json_response->tool_proxy_guid = uniqid();

try {
    $body = handleOAuthBodyPOST($oauth_consumer_key, $oauth_consumer_secret);
    $json = json_decode($body);
    // $json_response->ext_debug = "This is awesome!";
    $json = json_encode($json_response);
} catch (Exception $e) {
    http_response_code(400);
    echo(json_encode(array("ext_status" => "failure", "ext_detail" => $e->getMessage())));
    exit();
}

$header_key = getOAuthKeyFromHeaders();
if ( $header_key != $oauth_consumer_key ) {
    header("HTTP/1.1 403 Failure");
    echo(json_encode(array("ext_status" => "failure", "ext_detail" => "KEY=$oauth_consumer_key HDR=$header_key")));
   exit();
}

header("HTTP/1.1 201 Created");
print_r($json);

?>
