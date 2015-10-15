<?php
error_reporting(E_ALL & ~E_NOTICE & ~E_DEPRECATED);
ini_set("display_errors", 1);
header('Content-Type: text/html; charset=utf-8');
session_start();
require_once("util/lti_util.php");

$cur_url = str_replace("/tc_profile.php","",curPageURL());
$pos = strpos('?',$cur_url);
if ( $pos !== false ) $cur_url = substr($cur_url,0,$pos-1);

$consumer_key = isset($_GET['key']) ? $_GET['key'] : '106aff6';
$r_key = isset($_GET['r_key']) ? $_GET['r_key'] : 'regkey';
$r_secret = isset($_GET['r_secret']) ? $_GET['r_secret'] : 'regsecret';

// Is guid, the oauth_consumer_key ???

echo <<< EOF
{
  "@context": [
    "http://purl.imsglobal.org/ctx/lti/v2/ToolConsumerProfile",
    {
      "tcp": "$cur_url/$consumer_key#"
    }
  ],
  "@type": "ToolConsumerProfile",
  "@id": "$cur_url/$consumer_key",
  "lti_version": "LTI-2p0",
  "guid": "$consumer_key",
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
          "vendor_name": {
            "default_value": "Sakai",
            "key": "product.vendor.name"
          },
          "description": {
            "default_value": "Sakai is an Open Source Learning Environment",
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
    },
    "service_owner" : {
      "@id" : "http://state.university.edu/",
      "timestamp" : "2012-03-28T09:08:16-04:00",
      "service_owner_name" : {
        "default_value" : "State University",
        "key" : "service_owner.name"
      },
      "description" : {
        "default_value" : "A fictitious university.",
        "key" : "service_owner.description"
      },
      "support" : {
        "email" : "techsupport@university.edu"
      }
    },
    "service_provider" : {
      "@id" : "http://yasp.example.com/ServiceProvider",
      "guid" : "yasp.example.com",
      "timestamp" : "2012-03-28T09:08:16-04:00",
      "service_provider_name" : {
        "default_value" : "Your Application Service Provider",
        "key" : "service_provider.name"
      },
      "description" : {
        "default_value" : "YASP is a fictitious application service provider",
        "key" : "service_provider.description"
      },
      "support" : {
        "email" : "support@yasp.example.com"
      }
    }
  },
  "capability_offered": [
        "basic-lti-launch-request",
        "User.id",
        "User.username",
        "CourseSection.sourcedId",
        "Person.sourcedId",
        "Person.name.full",
        "Membership.role",
        "Person.name.given" ,
        "Person.name.family" ,
        "Person.email.primary" ,
        "User.image" ,
        "Result.sourcedId" ,
        "Result.autocreate",
        "Result.custom.url",
        "OAuth.splitSecret",
        "OAuth.hmac-sha256"
  ],
  "service_offered": [
    {
      "@type": "RestService",
      "@id": "tcp:ToolProxy.collection",
      "endpoint": "$cur_url/tc_register.php?r_key=$r_key&r_secret=$r_secret",
      "format" : ["application/vnd.ims.lti.v2.toolproxy+json"],
      "action": [ "POST" ]
    },
    {
      "@type" : "RestService",
      "@id" : "tcp:ToolProxy.item",
      "endpoint" : "http://lms.example.com/resources/ToolProxy/{tool_proxy_guid}",
      "format" : ["application/vnd.ims.lti.v2.toolproxy+json"],
      "action" : ["GET", "PUT"]
    },
    {
      "@type" : "RestService",
      "@id" : "tcp:Result.item",
      "endpoint" : "http://lms.example.com/resources/Result/{sourcedId}",
      "format" : ["application/vnd.ims.lis.v2.result+json"],
      "action" : ["GET", "PUT"]
    },
    {
      "@type" : "RestService",
      "@id" : "tcp:LtiLinkSettings",
      "endpoint" : "http://lms.example.com/resources/links/{link_id}/custom",
      "format" : ["application/vnd.ims.lti.v2.toolsettings+json", "application/vnd.ims.lti.v2.toolsettings.simple+json"],
      "action" : ["GET", "PUT"]
    },
    {
      "@type" : "RestService",
      "@id" : "tcp:ToolProxyBindingSettings",
      "endpoint" : "http://lms.example.com/resources/lis/{context_type}/{context_id}/bindings/{vendor_code}/{product_code}/custom",
      "format" : ["application/vnd.ims.lti.v2.toolsettings+json", "application/vnd.ims.lti.v2.toolsettings.simple+json"],
      "action" : ["GET", "PUT"]
    },
    {
      "@type":"RestService",
      "@id" : "tcp:ToolProxySettings",
      "endpoint" : "http://lms.example.com/resources/ToolProxy/{tool_proxy_guid}/custom",
      "format" : ["application/vnd.ims.lti.v2.toolsettings+json", "application/vnd.ims.lti.v2.toolsettings.simple+json"],
      "action" : ["GET", "PUT"]
    },
    {
      "@type": "RestService",
      "@id": "tcp:LTI_1_1_ResultService",
      "endpoint": "$cur_url/common/tool_consumer_outcome.php",
      "format": ["application/vnd.ims.lti.v1.outcome+xml"],
      "action": [ "POST" ]
    }
  ]
}
EOF;

