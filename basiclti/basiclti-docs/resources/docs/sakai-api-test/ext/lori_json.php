<html>
<head>
  <title>Sakai Lessons API</title>
</head>
<body style="font-family:sans-serif; background-color:#add8e6">
<p><b>Calling the Sakai Lessons API</b></p>
<?php 
// Load up the LTI Support code
require_once("../util/lti_util.php");
require_once("json_indent.php");
require_once("lori_json_messages.php");

// Note - We avoid using the session in this file to avoid deadlocks
// If we were calling a web service on the same server

if (version_compare(PHP_VERSION, '5.3.0') >= 0) {
 error_reporting(E_ALL & ~E_NOTICE & ~E_DEPRECATED);
} else { 
 error_reporting(E_ALL & ~E_WARNING & ~E_NOTICE);
}

ini_set("display_errors", 1);

$oauth_consumer_secret = $_REQUEST['secret'];
if (strlen($oauth_consumer_secret) < 1 ) $oauth_consumer_secret = 'secret';

$sourcedid = $_REQUEST['context_id'];
if (get_magic_quotes_gpc()) $sourcedid = stripslashes($sourcedid);

$user_id = $_REQUEST['user_id'];
$context_id = $_REQUEST['context_id'];
$resource_link_id = $_REQUEST['resource_link_id'];
$token = $_REQUEST['token'];

?>
<p>
<form method="POST">
Service URL: <input type="text" name="url" size="100" disabled="true" value="<?php echo(htmlentities($_REQUEST['url']));?>"/></br>
user_id: <input type="text" name="user_id" disabled="true" size="100" value="<?php echo(htmlentities($user_id));?>"/></br>
context_id: <input type="text" name="context_id" disabled="true" size="100" value="<?php echo(htmlentities($context_id));?>"/></br>
api_token: <input type="text" name="token" disabled="true" size="100" value="<?php echo(htmlentities($token));?>"/></br>
OAuth Consumer Key: <input type="text" name="key" disabled="true" size="80" value="<?php echo(htmlentities($_REQUEST['key']));?>"/></br>
OAuth Consumer Secret: <input type="text" name="secret" size="80" value="<?php echo(htmlentities($oauth_consumer_secret));?>"/></br>
</p><p>
<input type='submit' name='submit' value="Get Course Structure">
</p><p>
<input type='submit' name='submit' value="Add Course Resources">
resource_link_id: <input type="text" name="resource_link_id" size="100" 
   value="<?php echo(htmlentities($resource_link_id));?>"/></br>
</form>
<?php 
$url = $_REQUEST['url'];

$oauth_consumer_key = $_REQUEST['key'];
$method="POST";
$endpoint = $_REQUEST['url'];
$content_type = "application/json";

if ( substr($endpoint, -1) != '/' ) $endpoint = $endpoint . '/';
if ( $_REQUEST['submit'] == "Get Course Structure" && isset($token) ){
    $courseStructureRequest = array(
      'context_id' => $context_id,
      'lori_api_token' => $token
    );
    $postBody = indent(json_encode($courseStructureRequest));
    $endpoint = $endpoint . "coursestructure";
} else if ( $_REQUEST['submit'] == "Add Course Resources" ) {
    $endpoint = $endpoint . "addcourseresources";
    $addStructureRequest = getAddStructure();
    $addStructureRequest['context_id'] = $context_id;
    $addStructureRequest['lori_api_token'] = $token;
	if ( strlen($resource_link_id) > 0 ) $addStructureRequest['resource_link_id'] = $resource_link_id;
    $postBody = indent(json_encode($addStructureRequest));
} else {
    exit();
}

$response = sendOAuthBodyPOST($method, $endpoint, $oauth_consumer_key, $oauth_consumer_secret, $content_type, $postBody);
global $LastOAuthBodyBaseString;
$lbs = $LastOAuthBodyBaseString;


echo("\n<pre>\n");
echo("Service Url:\n");
echo(htmlentities($endpoint)."\n\n");
echo("\n");
echo("------------ POST RETURNS ------------\n");
$response = str_replace("><","&gt;\n&lt;",$response);
$response = str_replace("<","&lt;",$response);
$response = str_replace(">","&gt;",$response);
echo(indent($response));

echo("\n\n------------ WE SENT ------------\n");
$postBody = str_replace("<","&lt;",$postBody);
$postBody = str_replace(">","&gt;",$postBody);
echo($postBody);
echo("\nBase String:\n");
echo($lbs);
echo("\n</pre>\n");

?>
