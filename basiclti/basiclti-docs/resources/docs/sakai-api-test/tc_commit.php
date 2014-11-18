<?php

    require_once("util/lti_util.php");

    if ( isset($_GET['VND']) && isset($_GET['url']) ) {
        // All good
    } else {
        error_log("tc_commit.php missing VND parameter");
        die('tc_commit.php missing parameter');
    }

    error_log("tp_commit.php sleeping 5 seconds");
    sleep(5);
    error_log("tp_commit.php back from sleep");
    $VND = $_GET['VND'];
    $url = $_GET['url'];
    error_log($VND);
    error_log($url);

    $more_headers = array(
        "VND-IMS-CORRELATION-ID: ".$_GET['VND'],
        "VND-IMS-DISPOSITION: commit"
    );

    // TODO: Sign this with OAUTH

    $oauth_consumer_key = '98765';
    $oauth_consumer_secret = 'secret';
    $accept_type = "application/json";
    sendOAuthGET($url, $oauth_consumer_key, $oauth_consumer_secret, $accept_type,$more_headers);

