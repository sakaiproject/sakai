<?php
header('Content-Type: text/html; charset=utf-8');
session_start();
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

    $consumer_key = isset($_REQUEST['consumer_key']) ? $_REQUEST['consumer_key'] : '106fab23';

    $lmsdata = array(
      "lti_message_type" => "ToolProxyRegistrationRequest",
      "lti_version" => "LTI-2p0",
      "reg_key" => "98765",
      "reg_password" => "dontpanic",
      "tc_profile_url" => str_replace("tc.php", "tc_profile.php", $cur_url),
      "launch_presentation_return_url" => str_replace("tc.php", "tc_continue.php", $cur_url)
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
  echo("Consumer Key: <input size=\"60\" type=\"text\" size=\"60\" name=\"consumer_key\" value=\"$consumer_key\">\n");
  echo("</fieldset><p>");
  echo("<fieldset><legend>Launch Data</legend>\n");
  foreach ($lmsdata as $k => $val ) {
      echo($k.": <input type=\"text\" size=\"60\" name=\"".$k."\" value=\"");
      echo(htmlspecialchars($val));
      echo("\"><br/>\n");
  }
  echo("</fieldset>\n");
  echo("</form>\n");
  echo("</div>\n");
  echo("<hr>");

  $parms = $lmsdata;
  // Cleanup parms before we sign
  foreach( $parms as $k => $val ) {
    if (strlen(trim($parms[$k]) ) < 1 ) {
       unset($parms[$k]);
    }
  }

  // Add oauth_callback to be compliant with the 1.0A spec
  $parms["oauth_callback"] = "about:blank";
    
  // $parms['launch_presentation_css_url'] = $cssurl;

  if ( isset($_POST['launch']) || isset($_POST['debug']) ) {

  if ( isset($parms['tc_profile_url']) ) {
        $parms['tc_profile_url'] .= '?key=' . $consumer_key;
    }
    $content = postLaunchHTML($parms, $endpoint, isset($_POST['debug']), 
        "_blank");
        // "width=\"100%\" height=\"900\" scrolling=\"auto\" frameborder=\"1\" transparency");
    print($content);
}

?>
