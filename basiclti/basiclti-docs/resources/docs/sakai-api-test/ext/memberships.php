<html>
<head>
  <title>LTI 1.0 Extensions</title>
</head>
<body style="font-family:sans-serif; background-color: pink">
<p><b>PHP Roster Retrieval (Memberships) Extensions</b></p>
<p><a href="../index.php" target="_new">About these extensions</a>.
</p>
<p>
This is one of the more popular extensions as it allows the tol to retrieve an entire roster
for the course.   If the <b>resource_link_id</b> supports outcomes, then 
the memberships output may include <b>lis_result_sourcedid</b> values for each course member.
</p>
<?php
// Load up the LTI 1.0 Support code
require_once '../util/lti_util.php';

error_reporting(E_ALL & ~E_NOTICE);
ini_set("display_errors", 1);

$oauth_consumer_secret = $_REQUEST['secret'];
if (strlen($oauth_consumer_secret) < 1 ) $oauth_consumer_secret = 'secret';
?>
<p>
<form method="POST">
Service URL: <input type="text" name="url" size="80" disabled="true" value="<?php echo($_REQUEST['url']);?>"/></br>
lis_result_sourcedid: <input type="text" name="id" disabled="true" size="100" value="<?php echo($_REQUEST['id']);?>"/></br>
OAuth Consumer Key: <input type="text" name="key" disabled="true" size="80" value="<?php echo($_REQUEST['key']);?>"/></br>
OAuth Consumer Secret: <input type="text" name="secret" size="80" value="<?php echo($oauth_consumer_secret);?>"/></br>
</p>
<input type='submit' name='submit' value="Read Roster"></br>
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
address you should be OK.  You can download a copy of the test
tools to run locally at
to test your LMS instance running on localhost.
(<a href="../dist.zip" target="_new">Download</a>)
</p>
<?php
}
$message = 'basic-lis-readmembershipsforcontext';

if ( ! isset($_REQUEST['submit']) ) exit;

$url = 'http://localhost:8080/imsblis/service/';
$url = $_REQUEST['url'];

$data = array(
  'lti_message_type' => $message,
  'id' => $_REQUEST['id']);

$oauth_consumer_key = $_REQUEST['key'];

$newdata = signParameters($data, $url, 'POST', $oauth_consumer_key, $oauth_consumer_secret);

echo "<pre>\n";
echo "Posting to URL $url \n";

ksort($newdata);
foreach($newdata as $key => $value ) {
    if (get_magic_quotes_gpc()) $value = stripslashes($value);
    print "$key=$value (".mb_detect_encoding($value).")\n";
}

global $last_base_string;
echo "\nBase String:\n</pre><p>\n";
echo $last_base_string;
echo "\n</p>\n<pre>\n";

$retval = do_post_request($url, http_build_query($newdata));

$retval = str_replace("<","&lt;",$retval);
$retval = str_replace(">","&gt;",$retval);
echo "Response from server\n";
echo $retval;

?>
