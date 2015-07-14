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
$oauth_consumer_key = isset($_REQUEST['key']) ? $_REQUEST['key'] : $_SESSION['oauth_consumer_key'];
$oauth_consumer_secret = isset($_REQUEST['secret']) ? $_REQUEST['secret'] : 'secret';

if (strlen($oauth_consumer_secret) < 1 || strlen($oauth_consumer_key) < 1 ) {
    var_dump($_SESSION);
    die("Must have oauth_consumer_key in session");
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

$format = isset($_REQUEST['format']) ? $_REQUEST['format']+0 : 1;
$bubble = isset($_REQUEST['bubble']) ? $_REQUEST['bubble']+0 : 0;

?>
<p>
<form method="post">
Link URL: <input type="text" name="link_url" size="120" value="<?php echo(htmlentities($link_url));?>"/></br>
Tool URL: <input type="text" name="tool_url" size="120" value="<?php echo(htmlentities($tool_url));?>"/></br>
Proxy URL: <input type="text" name="proxy_url" size="120" value="<?php echo(htmlentities($proxy_url));?>"/></br>
OAuth Consumer Key: <input type="text" name="key" size="80" value="<?php echo(htmlentities($oauth_consumer_key));?>"/></br>
OAuth Consumer Secret: <input type="text" name="secret" size="80" value="<?php echo(htmlentities($oauth_consumer_secret));?>"/></br>
Settings format:
<select name="format">
  <option value="1" <?php if ( $format == 1 ) echo("selected");?> >application/vnd.ims.lti.v2.toolsettings.simple+json</option>
  <option value="2" <?php if ( $format == 2 ) echo("selected");?> >application/vnd.ims.lti.v2.toolsettings+json</option>
</select>
<br/>
Bubble option:
<select name="bubble">
  <option value="0" <?php if ( $bubble == 0 ) echo("selected");?> >None</option>
  <option value="1" <?php if ( $bubble == 1 ) echo("selected");?> >All</option>
  <option value="2" <?php if ( $bubble == 2 ) echo("selected");?> >Distinct</option>
</select>
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
$content_type = "application/vnd.ims.lti.v2.toolsettings.simple+json";
if ( $format == 2 ) {
	$content_type = "application/vnd.ims.lti.v2.toolsettings+json";

	$type = "";
    $endpoint = "";
	if ( isset($_REQUEST['set_link']) ) {
		$type = "LtiLink";
		$endpoint = $link_url;
	} else if ( isset($_REQUEST['set_tool']) ) {
		$type = "ToolProxyBinding";
		$endpoint = $tool_url;
	} else if ( isset($_REQUEST['set_proxy']) ) {
		$type = "ToolProxy";
		$endpoint = $proxy_url;
	}

	if ( $type != "" ) {
		$settings = '{
  "@context" : "http://purl.imsglobal.org/ctx/lti/v2/ToolSettings",
  "@graph" : [ 
    { "@type" : "'.$type.'",
      "@id" : "'.$endpoint.'",
      "custom" : '.$settings.'
    }
  ]
}
';
	}
}

function doBubble($url) {
    if ( !isset($_POST['bubble']) || $_POST['bubble'] == 0 ) return $url;
	if ( strpos($url,'?') > 0 ) {
		$url .= '&';
	} else {
		$url .= '?';
	}
	if ( $_POST['bubble'] == 1 ) {
		$url .= "bubble=all";
	} else {
		$url .= "bubble=distinct";
	}
	return $url;
}

if ( isset($_REQUEST['get_link']) ){
    $response = sendOAuthGET(doBubble($link_url), $oauth_consumer_key, $oauth_consumer_secret, 
		$content_type);
	$debugin = get_get_sent_debug();
	$debugout = get_get_received_debug();
} else if ( isset($_REQUEST['get_tool']) ){
    $response = sendOAuthGET(doBubble($tool_url), $oauth_consumer_key, $oauth_consumer_secret, 
		$content_type);
	$debugin = get_get_sent_debug();
	$debugout = get_get_received_debug();
} else if ( isset($_REQUEST['get_proxy']) ){
    $response = sendOAuthGET(doBubble($proxy_url), $oauth_consumer_key, $oauth_consumer_secret, 
		$content_type);
	$debugin = get_get_sent_debug();
	$debugout = get_get_received_debug();
} else if ( isset($_REQUEST['set_link']) ) {
	$postBody = true;
	$response = sendOAuthBody("PUT", $link_url, $oauth_consumer_key, 
			$oauth_consumer_secret, $content_type, $settings);
	$debugin = get_body_sent_debug();
	$debugout = get_body_received_debug();
} else if ( isset($_REQUEST['set_tool']) ) {
	$postBody = true;
	$response = sendOAuthBody("PUT", $tool_url, $oauth_consumer_key, 
			$oauth_consumer_secret, $content_type, $settings);
	$debugin = get_body_sent_debug();
	$debugout = get_body_received_debug();
} else if ( isset($_REQUEST['set_proxy']) ) {
	$postBody = true;
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
	ltiUtilTogglePre("Our Body Data", indent($settings));
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
