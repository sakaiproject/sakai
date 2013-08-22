<?php
error_reporting(E_ALL & ~E_NOTICE & ~E_DEPRECATED);
ini_set("display_errors", 1);
header('Content-Type: text/html; charset=utf-8');
session_start();
require_once("util/lti_util.php");

$cur_url = str_replace("/tc_profile.php","",curPageURL());

// Is guid, the oauth_consumer_key ???

echo <<< EOF
{
  "@context": [
    "http://www.imsglobal.org/imspurl/lti/v2/ctx/ToolConsumerProfile",
    {
      "ltitcp": "$cur_url/106aff6"
    }
  ],
  "@type": "ToolConsumerProfile",
  "@id": "$cur_url/106aff6",
  "lti_version": "LTI-2p0",
  "guid": "106aff6",
  "product_instance": {
    "guid": "dcddf9808107-81ea-eaa4-1edf-5d24568c",
    "product_info": {
      "product_name": {
        "default_value": "Sakai LTI 2.0 Test Harness",
        "key": "product.name"
      },
      "product_version": "0.1",
      "description": {
        "default_value": "Dr. Chuck Online",
        "key": "product.version"
      },
      "technical_description": {
        "default_value": "LTI 1, 1.1 and 2.0 compliant",
        "key": "product.technicalDescription"
      },
      "product_family": {
        "code": "SakaiTestOs",
        "vendor": {
          "code": "www.sakaiproject.org",
          "name": {
            "default_value": "Sakai",
            "key": "product.vendor.name"
          },
          "description": {
            "default_value": "Sakai is an Open Source Collaboration and Learning Environment",
            "key": "product.vendor.description"
          },
          "website": "http://www.sakaiproject.org/",
          "timestamp": "2012-07-09T012:08:16-04:00",
          "contact": {
            "email": "support@sakaiproject.org"
          }
        }
      }
    },
    "support": {
      "email": "drchuck@gmail.com"
    }
  },
  "capability_enabled": [
       "Person.name.given" ,
       "Person.name.family" ,
       "Person.email.primary" ,
       "User.image" ,
       "Result.sourcedId" ,
       "basic-lti-launch-request",
       "Result.autocreate",
       "Result.sourcedGUID"
  ],
  "service_offered": [
    {
      "@type": "RestService",
      "@id": "$cur_url/tc_register.php",
      "endpoint": "$cur_url/tc_register.php",
      "format": "application/vnd.ims.lti.v2.ToolProxy+json",
      "action": [ "POST" ]
    },
    {
      "@type": "RestService",
      "@id": "$cur_url/common/tool_consumer_outcome.php",
      "endpoint": "$cur_url/common/tool_consumer_outcome.php",
      "format": "application/vnd.ims.lti.v1.Outcome+xml",
      "action": [ "POST" ]
    }
  ]
}
EOF;

