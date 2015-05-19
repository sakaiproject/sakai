<?php
    session_start();
// Load up the LTI Support code
require_once("../util/lti_util.php");
require_once("json_indent.php");
require_once("content_json_messages.php");

ini_set("display_errors", 1);

$content_url = isset($_REQUEST['content_url']) ? isset($_REQUEST['content_url']) : preg_replace("/json.*$/","images/Sakaiger.png",curPageUrl());

$result_url = isset($_REQUEST['url']) ? $_REQUEST['url'] : false;
$oauth_consumer_key = isset($_REQUEST['key']) ? $_REQUEST['key'] : $_SESSION['reg_key'];
$oauth_consumer_secret = isset($_REQUEST['secret']) ? $_REQUEST['secret'] : $_SESSION['reg_password'];

$grade = isset($_REQUEST['grade']) ? $_REQUEST['grade'] : '';
$comment = isset($_REQUEST['comment']) ? $_REQUEST['comment'] : '';

if (strlen($oauth_consumer_secret) < 1 || strlen($oauth_consumer_key) < 1 
    || strlen($result_url) < 1 ) {
    var_dump($_SESSION);
    die("Must have url, reg_password and reg_key in sesison or as GET parameters");
}

if ( isset($_REQUEST['send']) ) {
    $parms = array();
    $parms["lti_message_type"] = "ContentItemSelection";
    $parms["lti_version"] = "LTI-1p0";
    $parms["data"] = "Yo Yo";
    $retval = json_encode(getContentJSON($content_url));
    $parms["content_items"] = $retval;

    $parms = signParameters($parms, $result_url, "POST", 
        $oauth_consumer_key, $oauth_consumer_secret,
        "Finish Content Return");

    $content = postLaunchHTML($parms, $result_url, true);
    echo($content);
    return;
}

?>
<html>
<head>
  <title>IMS Content Item Service</title>
  <?php echo(ltiUtilToggleHead()); ?>
</head>
<body style="font-family:sans-serif; background-color:#add8e6">
<p><b>Calling IMS LTI Content Item Service</b></p>
<?php 
?>
<p>
<form method="post">
Service URL: <input type="text" name="url" size="120" value="<?php echo(htmlentities($result_url));?>"/></br>
OAuth Consumer Key: <input type="text" name="key" size="80" value="<?php echo(htmlentities($oauth_consumer_key));?>"/></br>
OAuth Consumer Secret: <input type="text" name="secret" size="80" value="<?php echo(htmlentities($oauth_consumer_secret));?>"/></br>
</p><p>
Content URL to send: </br>
<input type="text" name="content_url" 
size="80" value="<?php echo(htmlent_utf8($content_url));?>"/>
</p><p>
<input type='submit' name='send' value="Send Content Response">
</form>
