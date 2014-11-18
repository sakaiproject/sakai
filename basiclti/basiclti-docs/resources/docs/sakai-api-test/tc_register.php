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
    $json_data = json_decode($body);
    $json = json_encode($json_response);
} catch (Exception $e) {
    header('HTTP/1.1 400 Unauthorized', true, 400);
    echo(json_encode(array("ext_status" => "failure", "ext_detail" => $e->getMessage())));
    exit();
}

try{
    $commit_endpoint = $json_data->tool_profile->service_offered[0]->endpoint;
} catch(Exception $e) {
    $commit_endpoint = false;
}

ob_start();
var_dump(getallheaders());
$result = ob_get_clean();
error_log("tc_register.php");
error_log($result);

$header_key = getOAuthKeyFromHeaders();
if ( $header_key != $oauth_consumer_key ) {
    header('HTTP/1.1 400 Unauthorized', true, 400);
    echo(json_encode(array("ext_status" => "failure", "ext_detail" => "KEY=$oauth_consumer_key HDR=$header_key")));
   exit();
}

// Lets fire up a thread to send the commit message
$headers = getallheaders();
$VND = isset($headers['VND-IMS-CORRELATION-ID']) ? $headers['VND-IMS-CORRELATION-ID'] : false;

if ( $commit_endpoint != false && $VND !== false ) {
    $ch = curl_init();
    error_log("Launching ".$cur_base."tc_commit.php?VND=".$VND);
    curl_setopt($ch, CURLOPT_URL, $cur_base.'tc_commit.php?VND='.$VND.'&url='.$commit_endpoint);
    curl_setopt($ch, CURLOPT_FRESH_CONNECT, true);
    curl_setopt($ch, CURLOPT_TIMEOUT, 1);
    curl_exec($ch);
    curl_close($ch);
}

header('HTTP/1.1 201 Created', true, 201);
print_r($json);

