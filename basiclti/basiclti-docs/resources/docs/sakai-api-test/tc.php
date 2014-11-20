<?php
header('Content-Type: text/html; charset=utf-8');
session_start();
require_once 'cert.php';
?>
<html>
<head>
  <title>IMS Learning Tools Interoperability 2.0</title>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
</head>
<body style="font-family:sans-serif;">
<?php
echo("<p><b>IMS LTI 2.0 Consumer</b></p>\n");
echo("<p>This is a very simple reference implementaton of the LMS admin UI (i.e. consumer) for IMS LTI 2.0.</p>\n");

require_once("util/lti_util.php");

    $cur_url = curPageURL();

    $lmsdata = array(
      "lti_message_type" => "ToolProxyRegistrationRequest",
      "lti_version" => "LTI-2p0",
      "reg_key" => "98765",
      "reg_password" => "dontpanic",
      "tc_profile_url" => str_replace("tc.php", "tc_profile.php", $cur_url),
      "launch_presentation_return_url" => str_replace("tc.php", "tc_continue.php", $cur_url),
      "secret" => "secret"
      );

  foreach ($lmsdata as $k => $val ) {
      if ( isset($_POST[$k]) && strlen($_POST[$k]) > 0 ) {
          $lmsdata[$k] = $_POST[$k];
      }
  }

  $endpoint = str_replace("tc.php","tp.php",$cur_url);
  if ( isset($_REQUEST["endpoint"]) ) $endpoint = trim($_REQUEST["endpoint"]);

?>
<script language="javascript"> 
  //<![CDATA[ 
function lmsdataToggle() {
    var ele = document.getElementById("lmsDataForm");
    if(ele.style.display == "block") {
        ele.style.display = "none";
    }
    else {
        ele.style.display = "block";
    }
} 
  //]]> 
</script>
<?php
  echo("<div style=\"display:block\">\n");
  echo("<form method=\"post\">\n");
  echo("<input type=\"submit\" name=\"launch\" value=\"Launch\">\n");
  echo("<input type=\"submit\" name=\"debug\" value=\"Debug Launch\">\n");
  echo('<input type="submit" onclick="javascript:lmsdataToggle();return false;" value="Toggle Input Data">');
  if ( isset($_POST['launch']) || isset($_POST['debug']) ) {
    echo("<div id=\"lmsDataForm\" style=\"display:none\">\n");
  } else {
    echo("<div id=\"lmsDataForm\" style=\"display:block\">\n");
  }
  echo("<fieldset id=\"lmsDataForm\"><legend>LTI 2</legend>\n");
  $disabled = '';
  echo("Launch URL: <input size=\"60\" type=\"text\" size=\"60\" name=\"endpoint\" value=\"$endpoint\"><br/>\n");
  echo("</fieldset><p>");
  echo("<fieldset><legend>Launch Data</legend>\n");
  $re_register = isset($lmsdata) && $lmsdata['lti_message_type'] == 'ToolProxyReregistrationRequest';
?>
Message Type: 
<select name="lti_message_type">
<option value="ToolProxyRegistrationRequest">Registration</option>
<option value="ToolProxyReregistrationRequest"<?php if($re_register) echo(' selected');?>>Re-Registration</option>
</select>
<br/>
<?php
  foreach ($lmsdata as $k => $val ) {
      if ( $k == "lti_message_type" ) continue;
      if ( $k == "secret" ) {
        echo("<br/>Used for Re-Registration only:<br/>");
      }
      echo($k.": <input type=\"text\" size=\"60\" name=\"".$k."\" value=\"");
      echo(htmlspecialchars($val));
      echo("\"><br/>\n");
  }
  echo("<p>Note that reg_key will become the oauth_consumer_key</p>\n");
  echo("</fieldset>\n");
  echo("</form>\n");
  echo("</div>\n");
  echo("<hr>");

  if ( ! isset($_POST['reg_key']) ) exit();

  $parms = $lmsdata;
  // Cleanup parms before we sign
  foreach( $parms as $k => $val ) {
    if (strlen(trim($parms[$k]) ) < 1 ) {
       unset($parms[$k]);
    }
  }

    $reg_key = isset($_POST['reg_key']) ? $_POST['reg_key'] : false;

    // Add oauth_callback to be compliant with the 1.0A spec
    // $parms['launch_presentation_css_url'] = $cssurl;
    if ( isset($parms['tc_profile_url']) ) {
        $parms['tc_profile_url'] .= '?key=' . $reg_key;
        if ( $re_register ) {
            $parms['tc_profile_url'] .= '&r_key=' . $reg_key . '&r_secret='. $parms['secret'];
        } else {
            $parms['tc_profile_url'] .= '&r_key=' . $reg_key . '&r_secret='. $parms['reg_password'];
        }
    }

    if ( $re_register ) {
        $parms["oauth_callback"] = "about:blank";
        $key = $_POST['reg_key'];
        $secret = $parms['secret'];
        unset($parms['key']);
        unset($parms['secret']);
        unset($parms['reg_key']);
        unset($parms['reg_password']);
        $tool_consumer_instance_guid = $lmsdata_common['tool_consumer_instance_guid'];
        $tool_consumer_instance_description = $lmsdata_common['tool_consumer_instance_description'];
        $parms = signParameters($parms, $endpoint, "POST", $key, $secret,
            "Finish Launch", $tool_consumer_instance_guid, $tool_consumer_instance_description);
    } else {
        unset($parms['secret']);
    }

    if ( isset($_POST['launch']) || isset($_POST['debug']) ) {

    $content = postLaunchHTML($parms, $endpoint, isset($_POST['debug']), "_blank");
        // "width=\"100%\" height=\"900\" scrolling=\"auto\" frameborder=\"1\" transparency");
    print($content);
}

?>
