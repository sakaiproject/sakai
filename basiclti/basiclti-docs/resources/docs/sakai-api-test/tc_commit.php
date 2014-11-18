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

    $header = "VND-IMS-CORRELATION-ID: ".$_GET['VND']."\r\n".
              "VND-IMS-DISPOSITION: commit"."\r\n";

    do_get($url,$header);

