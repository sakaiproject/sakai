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

// What is the response type here?
header("Content-type: application/vnd.ims.lti.v2.toolproxy.id+json; charset=utf-8");

$method = $_SERVER['REQUEST_METHOD'];
error_log("Commmit Method: $method");
$VND = isset($_GET['correlation']) ? $_GET['correlation'] : '** MISSING **';
error_log("tp_commit saw correlation_id of ".$VND);

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

if ( $method == "PUT" || $method == "DELETE" ) {
	error_log("Returning 200");
	http_response_code(200);
} else {
	error_log("Returning 400");
	http_response_code(400);
}

