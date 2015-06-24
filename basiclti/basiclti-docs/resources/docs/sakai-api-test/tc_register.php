<?php
require_once("util/lti_util.php");

$cur_url = curPageURL();
$cur_base = str_replace("tc_register.php","",$cur_url);

header('Content-Type: application/vnd.ims.lti.v2.toolproxy.id+json; charset=utf-8;');

if ( ! isset($_GET['r_secret']) || ! isset($_GET['r_key']) ) die ("missing r_key or r_secret parameter");
$oauth_consumer_key  = $_GET['r_key'];
$oauth_consumer_secret = $_GET['r_secret'];

$response = '{
  "@context": "http://purl.imsglobal.org/ctx/lti/v2/ToolProxyId",
  "@type": "ToolProxy",
  "@id": "http://localhost:4000/toolproxies/0df4b410-9e38-0130-4f3c-406c8f217861",
  "tool_proxy_guid": "__replace me__"
}';

$json_response = json_decode($response);
$json_response->{'@id'} = $cur_base . uniqid();
$json_response->tool_proxy_guid = $oauth_consumer_key;
$half = bin2hex( openssl_random_pseudo_bytes( 512/8 ) ) ;
$json_response->tc_half_shared_secret = $half;


try {
    $body = handleOAuthBodyPOST($oauth_consumer_key, $oauth_consumer_secret);
    $json_data = json_decode($body);
    $json = json_encode($json_response);
} catch (Exception $e) {
    header('HTTP/1.1 400 Unauthorized', true, 400);
    echo(json_encode(array("ext_status" => "failure", "ext_detail" => $e->getMessage())));
    exit();
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
$commit_endpoint = isset($headers['VND-IMS-CONFIRM-URL']) ? $headers['VND-IMS-CONFIRM-URL'] : false;

if ( $commit_endpoint != false ) {
    $ch = curl_init();
    $launch = $cur_base."tc_commit.php?r_key=".$oauth_consumer_key."&r_secret=".$oauth_consumer_secret.'&url='.urlencode($commit_endpoint);
    error_log("Launching ".$launch);
    curl_setopt($ch, CURLOPT_URL, $launch);
    curl_setopt($ch, CURLOPT_FRESH_CONNECT, true);
    curl_setopt($ch, CURLOPT_TIMEOUT, 1);
    curl_exec($ch);
    curl_close($ch);
}

header('HTTP/1.1 201 Created', true, 201);
print_r($json);

