<?php
    session_start();
?>
<html>
<head>
  <title>IMS LTI2 Result Service</title>
</head>
<body style="font-family:sans-serif; background-color:#add8e6">
<p><b>Calling IMS LTI2 Result Service</b></p>
<?php 
// Load up the LTI Support code
require_once("../util/lti_util.php");
require_once("json_indent.php");
require_once("result_json_messages.php");

ini_set("display_errors", 1);

$sourcedid = $_REQUEST['sourcedid'];
if ( strlen($sourcedid) < 1 ) {
    die("Must have a sourcedid");
}

$result_url = isset($_REQUEST['url']) ? $_REQUEST['url'] : $_SESSION['result_url'];
$oauth_consumer_key = isset($_REQUEST['url']) ? $_REQUEST['key'] : $_SESSION['reg_key'];
$oauth_consumer_secret = isset($_REQUEST['secret']) ? $_REQUEST['secret'] : $_SESSION['reg_password'];

if (strlen($oauth_consumer_secret) < 1 || strlen($oauth_consumer_key) < 1 
    || strlen($result_url) < 1 ) {
    var_dump($_SESSION);
    die("Must have result_url, reg_password and reg_key in sesison");
}

if ( isset($_REQUEST['url']) ) {
    $result_url = $_REQUEST['url'];
} else {
    $result_url = str_replace("{sourcedId}",$sourcedid,$result_url);
}

?>
<p>
<form method="post">
Service URL: <input type="text" name="url" size="100" value="<?php echo(htmlentities($result_url));?>"/></br>
OAuth Consumer Key: <input type="text" name="key" size="80" value="<?php echo(htmlentities($oauth_consumer_key));?>"/></br>
OAuth Consumer Secret: <input type="text" name="secret" size="80" value="<?php echo(htmlentities($oauth_consumer_secret));?>"/></br>
</p><p>
Grade to Send to LMS: <input type="text" name="grade" value="<?php echo(htmlent_utf8($_REQUEST['grade']));?>"/>
Comment: <input type="text" name="comment" value="<?php echo(htmlent_utf8($_REQUEST['comment']));?>"/>
(i.e. 0.95)<br/>
</p><p>
<input type='submit' name='get' value="Get Result">
<input type='submit' name='set' value="Set Result">
</form>
<?php 

$postBody = false;
if ( isset($_REQUEST['get']) ){
    $response = sendOAuthGET($result_url, $oauth_consumer_key, $oauth_consumer_secret, 
        "application/vnd.ims.lis.v2.result+json");
} else if ( isset($_REQUEST['set']) ) {
    $addStructureRequest = getResultJSON($_REQUEST['grade'], $_REQUEST['comment']);
    $postBody = indent(json_encode($addStructureRequest));
	$response = sendOAuthBodyPOST("POST", $result_url, $oauth_consumer_key, 
			$oauth_consumer_secret, $content_type, $postBody);
} else {
    exit();
}

global $LastOAuthBodyBaseString;
$lbs = $LastOAuthBodyBaseString;


echo("\n<pre>\n");
echo("Service Url:\n");
echo(htmlentities($result_url)."\n\n");
echo("\n");
echo("------------ POST RETURNS ------------\n");
$response = str_replace("><","&gt;\n&lt;",$response);
$response = str_replace("<","&lt;",$response);
$response = str_replace(">","&gt;",$response);
echo(indent($response));

if ( $postBody !== false ) {
    echo("\n\n------------ WE SENT ------------\n");
    $postBody = str_replace("<","&lt;",$postBody);
    $postBody = str_replace(">","&gt;",$postBody);
    echo($postBody);
}
echo("\nBase String:\n");
echo($lbs);
echo("\n</pre>\n");

?>
