<?php
    session_start();
// Load up the LTI Support code
require_once("../util/lti_util.php");
require_once("json_indent.php");
require_once("content_json_messages.php");

ini_set("display_errors", 1);

$content_url = isset($_REQUEST['content_url']) ? $_REQUEST['content_url'] : preg_replace("/json.*$/","cc/sakai-export.imscc",curPageUrl());

$result_url = $_REQUEST['content_item_return_url'];
$oauth_consumer_key = $_REQUEST['oauth_consumer_key'];
$oauth_consumer_secret = isset($_REQUEST['secret']) ? $_REQUEST['secret'] : 'secret';
$data = isset($_REQUEST['data']) ? $_REQUEST['data'] : "";

// Note that the opaque data is in $_REQUEST['data'] all the time

if (strlen($oauth_consumer_secret) < 1 || strlen($oauth_consumer_key) < 1 
    || strlen($result_url) < 1 ) {
    var_dump($_SESSION);
    die("Must have url, reg_password and reg_key in sesison or as GET parameters");
}

if ( isset($_REQUEST['send']) ) {
    $parms = array();
    $parms["lti_message_type"] = "ContentItemSelection";
    $parms["lti_version"] = "LTI-1p0";
    if ( isset($_REQUEST['data']) ) {
        $parms["data"] = $_REQUEST['data'];
    }
    $json = getFileItemJSON($content_url);
    $parms["content_items"] = json_encode($json);

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
  <title>IMS Content Item FileItem Service</title>
  <?php echo(ltiUtilToggleHead()); ?>
</head>
<body style="font-family:sans-serif; background-color:#add8e6">
<p><b>Calling IMS LTI Content Item FileItem Service</b></p>
<?php 
?>
<p>
<form method="post">
Service URL: <input type="text" name="content_item_return_url" size="120" value="<?php echo(htmlentities($result_url));?>"/></br>
OAuth Consumer Key: <input type="text" name="oauth_consumer_key" size="80" value="<?php echo(htmlentities($oauth_consumer_key));?>"/></br>
OAuth Consumer Secret: <input type="text" name="secret" size="80" value="<?php echo(htmlentities($oauth_consumer_secret));?>"/></br>
<br/>
Content URL to send: </br>
<input type="text" name="content_url" 
size="80" value="<?php echo(htmlent_utf8($content_url));?>"/>
</p><p>
Opaque Data: </br>
<textarea name="data" rows=5 cols=80>
<?php echo(htmlent_utf8($data));?>
</textarea>
</p><p>
<input type='submit' name='send' value="Send Content Response">
</form>
