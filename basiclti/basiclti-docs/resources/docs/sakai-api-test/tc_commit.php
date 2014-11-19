<?php

    require_once("util/lti_util.php");

    if ( isset($_GET['VND']) && isset($_GET['url']) && isset($_GET['r_key']) && isset($_GET['r_secret']) ) {
        // All good
    } else {
        error_log("tc_commit.php missing VND, url, r_key, or r_secret parameter");
        die('tc_commit.php missing VND, url, r_key, or r_secret parameter');
    }

    error_log("tp_commit.php sleeping 5 seconds");
    sleep(5);
    error_log("tp_commit.php back from sleep");
    $VND = $_GET['VND'];
    $url = $_GET['url'];
    $reg_key = $_GET['r_key'];
    $reg_secret = $_GET['r_secret'];
    error_log($VND);
    error_log($url);

    $more_headers = array();
    $more_oauth = array("oauth_ims_correlation_id" => $VND, "oauth_ims_disposition" => "commit");

    $accept_type = "application/json";
    $body = '{}';
    $response = sendOAuthBody("PUT", $url, $reg_key, $reg_secret, "application/vnd.ims.lti.v2.toolproxy+json", $body, $more_headers, $more_oauth);

