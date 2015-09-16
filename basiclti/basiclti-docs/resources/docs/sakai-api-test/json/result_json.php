<?php
    session_start();
// Load up the LTI Support code
require_once("../util/lti_util.php");
require_once("json_indent.php");
require_once("result_json_messages.php");

ini_set("display_errors", 1);
?>
<html>
<head>
  <title>IMS LTI2 Result Service</title>
  <?php echo(ltiUtilToggleHead()); ?>
</head>
<body style="font-family:sans-serif; background-color:#add8e6">
<p><b>Calling IMS LTI2 Result Service</b></p>
<?php 

$result_url = isset($_REQUEST['url']) ? $_REQUEST['url'] : false;
$oauth_consumer_key = isset($_REQUEST['key']) ? $_REQUEST['key'] : $_SESSION['oauth_consumer_key'];
$oauth_consumer_secret = isset($_REQUEST['secret']) ? $_REQUEST['secret'] : 'secret';

$grade = isset($_REQUEST['grade']) ? $_REQUEST['grade'] : '';
$comment = isset($_REQUEST['comment']) ? $_REQUEST['comment'] : '';

if (strlen($oauth_consumer_secret) < 1 || strlen($oauth_consumer_key) < 1 
    || strlen($result_url) < 1 ) {
    var_dump($_SESSION);
    die("Must have url, oauth_consumer_key in sesison");
}

?>
<p>
<form method="post">
Service URL: <input type="text" name="url" size="120" value="<?php echo(htmlentities($result_url));?>"/></br>
OAuth Consumer Key: <input type="text" name="key" size="80" value="<?php echo(htmlentities($oauth_consumer_key));?>"/></br>
OAuth Consumer Secret: <input type="text" name="secret" size="80" value="<?php echo(htmlentities($oauth_consumer_secret));?>"/></br>
</p><p>
Grade to Send to LMS: <input type="text" name="grade" value="<?php echo(htmlent_utf8($grade));?>"/>
Comment: <input type="text" name="comment" value="<?php echo(htmlent_utf8($comment));?>"/>
(i.e. 0.95)<br/>
</p><p>
<input type='submit' name='get' value="Get Result">
<input type='submit' name='set' value="Set Result">
</form>
<?php 

$postBody = false;
$content_type = "application/vnd.ims.lis.v2.result+json";
if ( isset($_REQUEST['get']) ){
    $response = sendOAuthGET($result_url, $oauth_consumer_key, $oauth_consumer_secret, 
        "application/vnd.ims.lis.v2.result+json");
	$debugin = get_get_sent_debug();
	$debugout = get_get_received_debug();
} else if ( isset($_REQUEST['set']) ) {
    $addStructureRequest = getResultJSON($_REQUEST['grade'], $_REQUEST['comment']);
    $postBody = indent(json_encode($addStructureRequest));
	$response = sendOAuthBody("PUT", $result_url, $oauth_consumer_key, 
			$oauth_consumer_secret, $content_type, $postBody);
	$debugin = get_body_sent_debug();
	$debugout = get_body_received_debug();
} else {
    exit();
}

global $LastOAuthBodyBaseString;
$lbs = $LastOAuthBodyBaseString;

ltiUtilTogglePre("Headers sent", $debugin);

if ( $postBody !== false ) {
	ltiUtilTogglePre("Our Body Data", indent($postBody));
}

ltiUtilTogglePre("Our Base String", $lbs);

ltiUtilTogglePre("Results and Headers", $debugout);

if ( strlen($response) < 1 ) {
	echo("<p>HTTP Response Body empty.</p>\n");
} else {
	echo("<p>HTTP Response:</p>\n");
	echo("<pre>\n");
	echo(indent($response));
	echo("\n</pre>\n");
}

?>
