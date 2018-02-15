<?php 
error_reporting(E_ALL & ~E_NOTICE);
ini_set("display_errors", 1);

// Load up the LTI Support code
require_once 'util/lti_util.php';
require_once 'util/mimeparse.php';

session_start();
header('Content-Type: text/html; charset=utf-8'); 

// Initialize, all secrets are 'secret', do not set session, and do not redirect
$key = isset($_POST['oauth_consumer_key']) ? $_POST['oauth_consumer_key'] : false;
$secret = "secret";
$_SESSION['oauth_consumer_key'] = $_POST['oauth_consumer_key'];
$_SESSION['secret'] = "secret";
$context = new BLTI($secret, false, false);
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
        print '&secret=secret';
        print '&url='.urlencode($_POST['lis_outcome_service_url']);
        if ( isset($_POST['oauth_signature_method']) && $_POST['oauth_signature_method'] != 'HMAC-SHA1' ) {
            print '&oauth_signature_method='.urlencode($_POST['oauth_signature_method']).'">';
        }
        print '&accepted='.urlencode($_POST['ext_outcome_data_values_accepted']).'">';
        print 'Test LTI 1.1 Outcome Service</a>.</p>'."\n";
		$found = true;
    }

    if ( isset($_POST['custom_result_url']) ) {
        print "<p>\n";
        print '<a href="json/result_json.php?url='.urlencode($_POST['custom_result_url']).'">';
        print 'Test LTI 2.0 Outcome Service</a>.</p>'."\n";
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
		$found = true;
    }

    if ( isset($_POST['ext_sakai_encrypted_session']) && isset($_POST['ext_sakai_serverid']) &&
	  isset($_POST['ext_sakai_server']) ) {
	// In the future support key lengths beyond 128 bits
	$keylength = isset($_POST['ext_sakai_blowfish_length']) ? $_POST['ext_sakai_blowfish_length'] / 8 : 16;
	if ( $keylength < 1 ) $keylength = 16;
	// hash is returning binary - not hex encoded so we get the full 160 bits
	$sha1Secret = hash('sha1',$secret, true);
	if ( strlen($sha1Secret) > $keylength ) $sha1Secret = substr($sha1Secret,0,$keylength);
	$encrypted_session=hex2bin($_POST['ext_sakai_encrypted_session']);
	$session = mcrypt_decrypt(MCRYPT_BLOWFISH, $sha1Secret, $encrypted_session, MCRYPT_MODE_ECB);

	// The encryption pads out the input string to a full block with non-printing characters
	// so we must remove them here.  Since the pre-encrypted sesison only includes non-printing
	// characters it is fafe to rtrim the non-printing characters up to \32 - initial testing
	// of Sakai indicates that the padding used by this versio of Java is 0x04 - but that could change
	// so we are playing it safe and right-trimming all non-printing characters.

	// http://stackoverflow.com/questions/1061765/should-i-trim-the-decrypted-string-after-mcrypt-decrypt
	$session = rtrim($session,"\0..\32");

	$session .= '.' . $_POST['ext_sakai_serverid'];
        print "<p>\n";
        print '<a href="retrieve.php?session='.urlencode($session);
	print '&server='.urlencode($_POST['ext_sakai_server']).'">';
        print 'Test Encrypted Session Extension</a>.</p>'."\n";
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

    $ltilink_allowed = false;
    if ( isset($_POST['accept_media_types']) ) {
        $ltilink_mimetype = 'application/vnd.ims.lti.v1.ltilink';
        $m = new Mimeparse;
        $ltilink_allowed = $m->best_match(array($ltilink_mimetype), $_POST['accept_media_types']);
    }

    if ( $ltilink_allowed && $_POST['content_item_return_url'] ) {
        print '<p><form action="json/content_json.php" method="post">'."\n";
        foreach ( $_POST as $k => $v ) {
            print '<input type="hidden" name="'.$k.'" ';
            print 'value="'.htmlentities($v).'"/>';
        }
        print '<input type="submit" value="Test LtiLinkItem Content Item"/>';
        print "</form></p>\n";
        $found = true;
    }

    $fileitem_allowed = false;
    if ( isset($_POST['accept_media_types']) ) {
        $fileitem_mimetype = 'application/vnd.ims.imsccv1p3';
        $m = new Mimeparse;
        $fileitem_allowed = $m->best_match(array($fileitem_mimetype), $_POST['accept_media_types']);
    }

    if ( $fileitem_allowed && $_POST['content_item_return_url'] ) {
        print '<p><form action="json/fileitem_json.php" method="post">'."\n";
        foreach ( $_POST as $k => $v ) {
            print '<input type="hidden" name="'.$k.'" ';
            print 'value="'.htmlentities($v).'"/>';
        }
        print '<input type="submit" value="Test FileItem Content Item"/>';
        print "</form></p>\n";
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
