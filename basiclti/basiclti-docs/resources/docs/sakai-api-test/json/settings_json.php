<?php
    session_start();
// Load up the LTI Support code
require_once("../util/lti_util.php");
require_once("json_indent.php");
// require_once("result_json_messages.php");

ini_set("display_errors", 1);
?>
<html>
<head>
  <title>IMS LTI2 Settings Service</title>
  <?php echo(ltiUtilToggleHead()); ?>
</head>
<body style="font-family:sans-serif; background-color:#add8e6">
<p><b>IMS LTI2 Result Service</b></p>
<?php 

$link_url = isset($_REQUEST['link']) ? $_REQUEST['link'] : false;
$tool_url = isset($_REQUEST['tool']) ? $_REQUEST['tool'] : false;
$proxy_url = isset($_REQUEST['proxy']) ? $_REQUEST['proxy'] : false;
$oauth_consumer_key = isset($_REQUEST['key']) ? $_REQUEST['key'] : $_SESSION['reg_key'];
$oauth_consumer_secret = isset($_REQUEST['secret']) ? $_REQUEST['secret'] : $_SESSION['reg_password'];

if (strlen($oauth_consumer_secret) < 1 || strlen($oauth_consumer_key) < 1 ) {
    var_dump($_SESSION);
    die("Must have reg_password and reg_key in sesison");
}

if ( $link_url || $tool_url || $proxy_url ) {
	// Goodness...
} else {
	die("Must have at least one settings resource url");
}

$settings = isset($_REQUEST['settings']) ? stripslashes($_REQUEST['settings']) : "";
if ( strlen($settings) < 1 ) {
	$settings = "{\n  ".'"pi" : "3.14"'."\n}\n";
}

?>
<p>
<form method="post">
Link URL: <input type="text" name="link_url" size="120" value="<?php echo(htmlentities($link_url));?>"/></br>
Tool URL: <input type="text" name="tool_url" size="120" value="<?php echo(htmlentities($tool_url));?>"/></br>
Proxy URL: <input type="text" name="proxy_url" size="120" value="<?php echo(htmlentities($proxy_url));?>"/></br>
OAuth Consumer Key: <input type="text" name="key" size="80" value="<?php echo(htmlentities($oauth_consumer_key));?>"/></br>
OAuth Consumer Secret: <input type="text" name="secret" size="80" value="<?php echo(htmlentities($oauth_consumer_secret));?>"/></br>
</p><p>
Settings to Send to Sakai: <br/>
<textarea name="settings" cols="60" rows="10">
<?php echo($settings); ?>
</textarea><br/>
<input type='submit' name='get_link' value="Get LtiLink">
<input type='submit' name='get_tool' value="Get ToolProxyBinding">
<input type='submit' name='get_proxy' value="Get ToolProxy"></br>
<input type='submit' name='set_link' value="Set LtiLink">
<input type='submit' name='set_tool' value="Set ToolProxyBinding">
<input type='submit' name='set_proxy' value="Set ToolProxy"></br>
</form>
<?php 

$postBody = false;
$content_type = "application/vnd.ims.lis.v2.result+json";
if ( isset($_REQUEST['get_link']) ){
    $response = sendOAuthGET($link_url, $oauth_consumer_key, $oauth_consumer_secret, 
		$content_type);
	$debugin = get_get_sent_debug();
	$debugout = get_get_received_debug();
} else if ( isset($_REQUEST['get_tool']) ){
    $response = sendOAuthGET($tool_url, $oauth_consumer_key, $oauth_consumer_secret, 
		$content_type);
	$debugin = get_get_sent_debug();
	$debugout = get_get_received_debug();
} else if ( isset($_REQUEST['get_proxy']) ){
    $response = sendOAuthGET($proxy_url, $oauth_consumer_key, $oauth_consumer_secret, 
		$content_type);
	$debugin = get_get_sent_debug();
	$debugout = get_get_received_debug();
} else if ( isset($_REQUEST['set_link']) ) {
	$response = sendOAuthBody("PUT", $link_url, $oauth_consumer_key, 
			$oauth_consumer_secret, $content_type, $settings);
	$debugin = get_body_sent_debug();
	$debugout = get_body_received_debug();
} else if ( isset($_REQUEST['set_tool']) ) {
	$response = sendOAuthBody("PUT", $tool_url, $oauth_consumer_key, 
			$oauth_consumer_secret, $content_type, $settings);
	$debugin = get_body_sent_debug();
	$debugout = get_body_received_debug();
} else if ( isset($_REQUEST['set_proxy']) ) {
	$response = sendOAuthBody("PUT", $proxy_url, $oauth_consumer_key, 
			$oauth_consumer_secret, $content_type, $settings);
	$debugin = get_body_sent_debug();
	$debugout = get_body_received_debug();
} else {
    exit();
}

global $LastOAuthBodyBaseString;
$lbs = $LastOAuthBodyBaseString;

ltiUtilTogglePre("Sent Headers", $debugin);

if ( $postBody !== false ) {
	ltiUtilTogglePre("Our Body Data", indent($postBody));
}

ltiUtilTogglePre("Our Base String", $lbs);

ltiUtilTogglePre("Results and Headers", $debugout);

if ( strlen($response) < 1 ) {
   echo("<p>HTTP Response Body empty.</p>\n");
} else {
	echo("<br/><b>Returned data:</b>\n<pre>\n");
	echo(htmlent_utf8(indent($response)));
	echo("\n</pre>\n");
}

?>
