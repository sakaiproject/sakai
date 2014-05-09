<?php
require_once("util/lti_util.php");

$cur_url = curPageURL();
$cur_base = str_replace("tc_register.php","",$cur_url);

header('Content-Type: application/vnd.ims.lti.v2.toolproxy.id+json; charset=utf-8;');

$oauth_consumer_key = '98765';
$oauth_consumer_secret = 'dontpanic';

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
    $json = json_encode($json_response);
} catch (Exception $e) {
    header('HTTP/1.1 400 Unauthorized', true, 400);
    echo(json_encode(array("ext_status" => "failure", "ext_detail" => $e->getMessage())));
    exit();
}

$header_key = getOAuthKeyFromHeaders();
if ( $header_key != $oauth_consumer_key ) {
    header('HTTP/1.1 400 Unauthorized', true, 400);
    echo(json_encode(array("ext_status" => "failure", "ext_detail" => "KEY=$oauth_consumer_key HDR=$header_key")));
   exit();
}

header('HTTP/1.1 201 Created', true, 201);
print_r($json);

