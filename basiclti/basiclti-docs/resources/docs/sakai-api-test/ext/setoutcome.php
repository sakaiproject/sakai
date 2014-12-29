<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
  <title>Sakai Basic Outcome Service</title>
</head>
<body style="font-family:sans-serif; background-color: pink">
<p><b>Sakai Basic Outcome API (deprecated)</b></p>
<p>
This should not be used if your version of Sakai supports
<a href="http://www.imsglobal.org/developers/LTI/test/v1p1/index.php" target="_new">IMS LTI 1.1</a>.
LTI 1.1 or LTI 2.0 as they include an equivalent outcome service and is a formal standard
and as such will be far more broadly implemented.
This older service is included in Sakai to allow older External Tools to continue to work.
</p>
<p>
This service sets and reads the same grade as the LTI 1.1 and LTI 2.0 service sets.  There is only
one grade.  So if you set the grade using LTI 1.1, and read the grade using tis service, you will 
see the grade that was stored by LTI 1.1.
</p>
<?php
// Load up the LTI 1.0 Support code
require_once '../util/lti_util.php';

error_reporting(E_ALL & ~E_NOTICE);
ini_set("display_errors", 1);

$oauth_consumer_secret = $_REQUEST['secret'];
if (strlen($oauth_consumer_secret) < 1 ) $oauth_consumer_secret = 'secret';
?>
<form method="post" action="">
<p>
Service URL: <input type="text" name="url" size="130" disabled="disabled" value="<?php echo($_REQUEST['url']);?>"/><br/>
lis_result_sourcedid: <input type="text" name="sourcedid" disabled="disabled" size="100" value="<?php echo($_REQUEST['sourcedid']);?>"/><br/>
OAuth Consumer Key: <input type="text" name="key" disabled="disabled" size="80" value="<?php echo($_REQUEST['key']);?>"/><br/>
OAuth Consumer Secret: <input type="text" name="secret" size="80" value="<?php echo($oauth_consumer_secret);?>"/><br/>
<!--
Type: <select name="type">
  <option value=""<?php if ($_REQUEST['type'] == "") echo ' selected="selected"'; ?>>default</option>
  <option value="decimal"<?php if ($_REQUEST['type'] == "decimal") echo 'selected="selected"'; ?>>decimal</option>
  <option value="percentage"<?php if ($_REQUEST['type'] == "percentage") echo 'selected="selected"'; ?>>percentage</option>
  <option value="ratio"<?php if ($_REQUEST['type'] == "ratio") echo 'selected="selected"'; ?>>ratio</option>
  <option value="passfail"<?php if ($_REQUEST['type'] == "passfail") echo 'selected="selected"'; ?>>passfail</option>
  <option value="letteraf"<?php if ($_REQUEST['type'] == "letteraf") echo 'selected="selected"'; ?>>letteraf</option>
  <option value="letterafplus"<?php if ($_REQUEST['type'] == "letterafplus") echo 'selected="selected"'; ?>>letterafplus</option>
  <option value="freetext"<?php if ($_REQUEST['type'] == "freetext") echo 'selected="selected"'; ?>>freetext</option>
</select><br/>
Status: <select name="status">
  <option value=""<?php if ($_REQUEST['status'] == "") echo ' selected="selected"'; ?>>none</option>
  <option value="interim"<?php if ($_REQUEST['status'] == "interim") echo 'selected="selected"'; ?>>interim</option>
  <option value="final"<?php if ($_REQUEST['status'] == "final") echo 'selected="selected"'; ?>>final</option>
  <option value="unmoderated"<?php if ($_REQUEST['status'] == "unmoderated") echo 'selected="selected"'; ?>>unmoderated</option>
  <option value="moderated"<?php if ($_REQUEST['status'] == "moderated") echo 'selected="selected"'; ?>>moderated</option>
</select><br/>
-->
Grade to Send to LMS: <input type="text" name="grade" value="<?php echo($_REQUEST['grade']);?>"/>
(e.g. 0.95)<br/>
<?php  if ( strpos($_REQUEST['accepted'],"text") !== false ) { ?>
Comment to Send to LMS: <input type="text" name="comment" size="60" value="<?php echo($_REQUEST['comment']);?>"/><br/>
<?php } ?>
<input type='submit' name='submit' value="Send Grade"/>
<input type='submit' name='submit' value="Read Grade"/>
<input type='submit' name='submit' value="Delete Grade"/>
</p>
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
address you should be OK.  
You can checkout a copy of the test
tools to run locally at
to test your Sakai instance running on localhost.
(<a href="https://source.sakaiproject.org/svn//basiclti/trunk/basiclti-docs/resources/docs/sakai-api-test/" target="_new">Source Code</a>)
</p>
<?php
}

if ( $_REQUEST['submit'] == "Send Grade" && isset($_REQUEST['grade'] ) ) {
    $message = 'basic-lis-updateresult';
} else if ( $_REQUEST['submit'] == "Read Grade" ) {
    $message = 'basic-lis-readresult';
} else if ( $_REQUEST['submit'] == "Delete Grade" ) {
    $message = 'basic-lis-deleteresult';
} else {
    echo '</body></html>';
    exit();
}

if ( ! isset($_REQUEST['grade']) ) {
    echo '</body></html>';
    exit();
}

// Hack to detect the old form of outcomes
if ( strpos($url,"imsblis/outcomes/") > 0 ) {
    $message = str_replace('basic', 'simple', $message);
}


$data = array(
  'lti_message_type' => $message,
  'sourcedid' => $_REQUEST['sourcedid'],
  'result_resultscore_textstring' => $_REQUEST['grade']);

if ( isset($_REQUEST['comment']) ) {
  $data['result_resultdata_text'] = $_REQUEST['comment'];
}

if (isset($_REQUEST['status']) && (strlen($_REQUEST['status']) > 0)) {
  $data['result_statusofresult'] = $_REQUEST['status'];
}
if (isset($_REQUEST['type']) && (strlen($_REQUEST['type']) > 0)) {
  $data['result_resultvaluesourcedid'] = $_REQUEST['type'];
}

$oauth_consumer_key = $_REQUEST['key'];

$newdata = signParameters($data, $url, 'POST', $oauth_consumer_key, $oauth_consumer_secret);

echo "<pre>\n";
echo "Posting to URL $url \n";

ksort($newdata);
foreach($newdata as $key => $value ) {
    if (get_magic_quotes_gpc()) $value = stripslashes($value);
    print "$key=$value (".mb_detect_encoding($value).")\n";
}

global $LastOAuthBodyBaseString;
echo "\nBase String:\n</pre><p>\n";
echo $LastOAuthBodyBaseString;
echo "\n</p>\n<pre>\n";

$retval = do_body_request($url, "POST", http_build_query($newdata));

$retval = str_replace("<","&lt;",$retval);
$retval = str_replace(">","&gt;",$retval);
echo "Response from server\n";
echo $retval;

?>
</pre>
</body>
</html>
