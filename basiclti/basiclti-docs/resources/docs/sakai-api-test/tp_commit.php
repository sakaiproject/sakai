<?php

require_once "util/lti_util.php";

if ( isset($_GET['r_key']) && isset($_GET['r_secret']) ) {
    $retval = validateOAuth($_GET['r_key'], $_GET['r_secret']);
    if ( $retval === true ) {
        error_log("tp_commit.php Validated");
    } else {
        error_log("tp_commit.php Failed validation: ".$retval);
        echo("tp_commit.php Failed validation: ".$retval."\n");
    }
}

header("Content-type: application/vnd.ims.lti.v2.toolproxy.id+json; charset=utf-8");

$headers = getallheaders();
ob_start();
var_dump($headers);
$result = ob_get_clean();
error_log("tp_commit.php");
error_log($result);
ob_start();
var_dump($_POST);
$result = ob_get_clean();
error_log($result);

$correlation = $headers["VND-IMS-CORRELATION-ID"];

echo <<< EOF
{
  "@context": "http://purl.imsglobal.org/ctx/lti/v2/ToolProxyId",
  "@type": "ToolProxy",
  "@id": "http://rails.kinexis.com:4000/tools/f7aaee90-474c-0132-0bbb-406c8f40a599",
  "tool_proxy_guid": "f7aaee90-474c-0132-0bbb-406c8f40a599",
  "disposition": "commit"
}
EOF;

