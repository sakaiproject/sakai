<html>
<head>
  <title>IMS Learning Tools Interoperability</title>
</head>
<body style="font-family:sans-serif; background-color:#add8e6">
<p><b>Tool Provider Calling the IMS LIS/LTI Outcome Service</b></p>
<p>This is a simple implementation of the Simple Outcomes Service.</p>
<?php 
// Load up the LTI Support code
require_once("../util/lti_util.php");

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

$sourcedid = $_REQUEST['sourcedid'];
if (get_magic_quotes_gpc()) $sourcedid = stripslashes($sourcedid);

$signature = false;
if ( isset($_REQUEST['oauth_signature_method']) ) $signature = $_REQUEST['oauth_signature_method'];

?>
<p>
<form method="POST">
Service URL: <input type="text" name="url" size="100" disabled="true" value="<?php echo(htmlent_utf8($_REQUEST['url']));?>"/></br>
lis_result_sourcedid: <input type="text" name="sourcedid" disabled="true" size="100" value="<?php echo(htmlent_utf8($sourcedid));?>"/></br>
OAuth Consumer Key: <input type="text" name="key" disabled="true" size="80" value="<?php echo(htmlent_utf8($_REQUEST['key']));?>"/></br>
OAuth Consumer Secret: <input type="text" name="secret" size="80" value="<?php echo(htmlent_utf8($oauth_consumer_secret));?>"/></br>
OAuth Signature Method: <input type="text" name="oauth_signature_method" value="<?php echo(htmlent_utf8($_REQUEST['oauth_signature_method']));?>"/></br>
</p><p>
Grade to Send to LMS: <input type="text" name="grade" value="<?php echo(htmlent_utf8($_REQUEST['grade']));?>"/>
(i.e. 0.95)<br/>
<?php  if ( strpos($_REQUEST['accepted'],"text") !== false ) { ?>
Comment to Send to LMS: <input type="text" name="comment" size="60" value="<?php echo($_REQUEST['comment']);?>"/>(extension)<br/>
<?php } ?>
<input type="hidden" name="accepted" value="<?php echo(htmlent_utf8($_REQUEST['accepted']));?>"/></br>
<input type='submit' name='submit' value="Send Grade">
<input type='submit' name='submit' value="Read Grade">
<input type='submit' name='submit' value="Delete Grade"></br>
</form>
<?php 
$url = $_REQUEST['url'];
if(!in_array($_SERVER['HTTP_HOST'],array('localhost','127.0.0.1')) && strpos($url,'localhost') > 0){ ?>
<p>
<b>Note</b> This service call may not work.  It appears as though you are 
calling a service running on <b>localhost</b> from a tool that
is not running on localhost.
Because these services are server-to-server calls if you are 
running your LMS on "localhost", you must also run this script
on localhost as well.  If your LMS has a real Internet
address you should be OK.  You can get a copy of the test
tools to run locally at 
to test your LMS instance running on localhost.
(<a href="../lti.zip" target="_new">Download</a>) 
</p>
<?php
}

$oauth_consumer_key = $_REQUEST['key'];
$method="POST";
$endpoint = $_REQUEST['url'];
$content_type = "application/xml";

$sourcedid = htmlspec_utf8($sourcedid);

if ( $_REQUEST['submit'] == "Send Grade" && isset($_REQUEST['grade'] ) ) {
    $operation = 'replaceResultRequest';
    $postBody = str_replace(
	array('SOURCEDID', 'GRADE', 'OPERATION','MESSAGE'), 
	array($sourcedid, $_REQUEST['grade'], $operation, uniqid()), 
	getPOXGradeRequest());
    if ( strpos($_REQUEST['accepted'],"text") !== false && strlen($_REQUEST['comment']) > 0 ) {
        $postBody = str_replace("</resultScore>",
        "</resultScore>\n<resultData>\n<text>\n".$_REQUEST['comment'].
        "\n</text>\n</resultData>", $postBody);
    }
} else if ( $_REQUEST['submit'] == "Read Grade" ) {
    $operation = 'readResultRequest';
    $postBody = str_replace(
	array('SOURCEDID', 'OPERATION','MESSAGE'), 
	array($sourcedid, $operation, uniqid()), 
	getPOXRequest());
} else if ( $_REQUEST['submit'] == "Delete Grade" ) {
    $operation = 'deleteResultRequest';
    $postBody = str_replace(
	array('SOURCEDID', 'OPERATION','MESSAGE'), 
	array($sourcedid, $operation, uniqid()), 
	getPOXRequest());
} else {
    exit();
}

$more_headers = false;
$response = sendOAuthBody($method, $endpoint, $oauth_consumer_key, $oauth_consumer_secret, $content_type, $postBody, $more_headers, $signature);
global $LastOAuthBodyBaseString;
$lbs = $LastOAuthBodyBaseString;

global $LastPOSTHeader;
$lph = $LastPOSTHeader;

try { 
    $retval = parseResponse($response);
} catch(Exception $e) {
    $retval = $e->getMessage();
}

echo("\n<pre>\n");
echo("Service Url:\n");
echo(htmlent_utf8($endpoint)."\n\n");
echo(get_body_sent_debug());
print_r($retval);
echo("\n");
echo("------------ POST RETURNS ------------\n");
$response = str_replace("><","&gt;\n&lt;",$response);
$response = str_replace("<","&lt;",$response);
$response = str_replace(">","&gt;",$response);
echo($response);

echo("\n\n------------ WE SENT ------------\n");
echo(get_body_received_debug());
echo("\n");
$postBody = str_replace("<","&lt;",$postBody);
$postBody = str_replace(">","&gt;",$postBody);
echo($postBody);
echo("\n\nBase String:\n");
echo($lbs);
echo("\n</pre>\n");

?>
