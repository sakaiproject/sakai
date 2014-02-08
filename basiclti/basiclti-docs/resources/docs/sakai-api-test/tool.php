<?php 
error_reporting(E_ALL & ~E_NOTICE);
ini_set("display_errors", 1);

// Load up the LTI Support code
require_once 'util/lti_util.php';

session_start();
header('Content-Type: text/html; charset=utf-8'); 

// Initialize, all secrets are 'secret', do not set session, and do not redirect
$context = new BLTI("secret", false, false);
?>
<html>
<head>
  <title>Sakai External Tool API Test Harness</title>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
</head>
<body style="font-family:sans-serif; background-color:#add8e6">
<?php
echo("<p><b>Sakai External Tool API Test Harness</b></p>\n");

$sourcedid = $_REQUEST['lis_result_sourcedid'];
if (get_magic_quotes_gpc()) $sourcedid = stripslashes($sourcedid);

if ( $context->valid ) {
   print "<p style=\"color:green\">Launch Validated.<p>\n";
   if ( $_POST['launch_presentation_return_url']) {
     $msg = 'A%20message%20from%20the%20tool%20provider.';
     $error_msg = 'An%20error%20message%20from%20the%20tool%20provider.';
     $sep = (strpos($_POST['launch_presentation_return_url'], '?') === FALSE) ? '?' : '&amp;';
     print "<p><a href=\"{$_POST['launch_presentation_return_url']}\">Return to tool consumer</a> (";
     print "<a href=\"{$_POST['launch_presentation_return_url']}{$sep}lti_msg={$msg}&amp;lti_log=LTI%20log%20entry:%20{$msg}\">with a message</a> or ";
     print "<a href=\"{$_POST['launch_presentation_return_url']}{$sep}lti_errormsg={$error_msg}&amp;lti_errorlog=LTI%20error%20log%20entry:%20{$error_msg}\">with an error</a>";
     print ")</p>\n";
   }

   $found = false;
    if ( $_POST['lis_result_sourcedid'] && $_POST['lis_outcome_service_url'] ) {
        print "<p>\n";
        print '<a href="common/tool_provider_outcome.php?sourcedid='.urlencode($sourcedid);
        print '&key='.urlencode($_POST['oauth_consumer_key']);
        print '&seret=secret';
        print '&url='.urlencode($_POST['lis_outcome_service_url']);
        print '&accepted='.urlencode($_POST['ext_outcome_data_values_accepted']).'">';
        print 'Test LTI 1.1 Outcome Service</a>.</p>'."\n";
		$found = true;
    }

    if ( isset($_POST['custom_result_url']) ) {
        print "<p>\n";
        print '<a href="json/result_json.php?url='.urlencode($_POST['custom_result_url']).'">';
        print 'Test LTI 2.0 Outcome Service</a>.</p>'."\n";
		$_SESSION['reg_key'] = $_POST['oauth_consumer_key'];
		$_SESSION['reg_password'] = "secret";
		$found = true;
    }

    if ( isset($_POST['custom_ltilink_custom_url']) || isset($_POST['custom_toolproxy_custom_url']) ||
		isset($_POST['custom_toolproxybinding_custom_url']) ) {
        print "<p>\n";
        print '<a href="json/settings_json.php?';
		if ( isset($_POST['custom_ltilink_custom_url']) ) { 
			print 'link='.urlencode($_POST['custom_ltilink_custom_url'])."&";
		}
		if ( isset($_POST['custom_toolproxy_custom_url']) ) { 
			print 'proxy='.urlencode($_POST['custom_toolproxy_custom_url'])."&";
		}
		if ( isset($_POST['custom_toolproxybinding_custom_url']) ) { 
			print 'tool='.urlencode($_POST['custom_toolproxybinding_custom_url'])."&";
		}
		print 'x=24">';
        print 'Test LTI 2.0 Settings Service</a>.</p>'."\n";
		$_SESSION['reg_key'] = $_POST['oauth_consumer_key'];
		$_SESSION['reg_password'] = "secret";
		$found = true;
    }


    if ( $_POST['context_id'] && $_POST['ext_lori_api_url_xml'] && $_POST['lis_result_sourcedid'] ) {
        print "<p>\n";
        print '<a href="ext/lori_xml.php?context_id='.htmlent_utf8($_POST['context_id']);
        print '&lis_result_sourcedid='.urlencode($_POST['lis_result_sourcedid']);
        print '&user_id='.urlencode($_POST['user_id']);
        print '&key='.urlencode($_POST['oauth_consumer_key']);
        print '&url='.urlencode($_POST['ext_lori_api_url_xml']).'">';
        print 'Test Sakai LORI XML API</a>.</p>'."\n";
        $found = true;
    }

    if ( $_POST['ext_ims_lis_memberships_id'] && $_POST['ext_ims_lis_memberships_url'] ) {
        print "<p>\n";
        print '<a href="ext/memberships.php?id='.htmlent_utf8($_POST['ext_ims_lis_memberships_id']);
        print '&key='.urlencode($_POST['oauth_consumer_key']);
        print '&url='.urlencode($_POST['ext_ims_lis_memberships_url']).'">';
        print 'Test Sakai Roster API</a>.</p>'."\n";
		$found = true;
    }

    if ( $_POST['lis_result_sourcedid'] && $_POST['ext_ims_lis_basic_outcome_url'] ) {
        print "<p>\n";
        print '<a href="ext/setoutcome.php?sourcedid='.$sourcedid;
        print '&key='.urlencode($_POST['oauth_consumer_key']);
        print '&accepted='.urlencode($_POST['ext_outcome_data_values_accepted']);
        print '&url='.urlencode($_POST['ext_ims_lis_basic_outcome_url']).'">';
        print 'Test Sakai Outcome API</a>.</p>'."\n";
		$found = true;
    } 
    if ( $_POST['ext_ims_lti_tool_setting_id'] && $_POST['ext_ims_lti_tool_setting_url'] ) {
        print "<p>\n";
        print '<a href="ext/setting.php?id='.htmlent_utf8($_POST['ext_ims_lti_tool_setting_id']);
        print '&key='.urlencode($_POST['oauth_consumer_key']);
        print '&url='.urlencode($_POST['ext_ims_lti_tool_setting_url']).'">';
        print 'Test Sakai Settings API</a>.</p>'."\n";
		$found = true;
    }

    if ( ! $found ) {
		echo("<p>No Services are available for this launch.</p>\n");
	}
    print "<pre>\n";
    print "Context Information:\n\n";
    print htmlent_utf8($context->dump());
    print "</pre>\n";
} else {
    print "<p style=\"color:red\">Could not establish context: ".$context->message."<p>\n";
}
print "<p>Base String:<br/>\n";
print htmlent_utf8($context->basestring);
print "<br/></p>\n";

echo('<a href="basecheck.php?b='.urlencode($context->basestring).'" target="_blank">Compare This Base String</a><br/>');
print "<br/></p>\n";

print "<pre>\n";
print "Raw POST Parameters:\n\n";
ksort($_POST);
foreach($_POST as $key => $value ) {
    if (get_magic_quotes_gpc()) $value = stripslashes($value);
    print htmlent_utf8($key) . "=" . htmlent_utf8($value) . " (".mb_detect_encoding($value).")\n";
}

print "\nRaw GET Parameters:\n\n";
ksort($_GET);
foreach($_GET as $key => $value ) {
    if (get_magic_quotes_gpc()) $value = stripslashes($value);
    print htmlent_utf8($key) . "=" . htmlent_utf8($value) . " (".mb_detect_encoding($value).")\n";
}
print "</pre>";

?>
