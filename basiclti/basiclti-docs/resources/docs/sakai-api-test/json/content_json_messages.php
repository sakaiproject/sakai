<?php

function getLtiLinkJSON($url) {

$return = '{
  "@context" : "http://purl.imsglobal.org/ctx/lti/v1/ContentItem", 
  "@graph" : [ 
    { "@type" : "LtiLink",
      "@id" : ":item2",
      "text" : "The mascot for the Sakai Project", 
      "title" : "The fearsome mascot of the Sakai Project",
      "url" : "http://developers.imsglobal.org/images/imscertifiedsm.png",
      "icon" : {
        "@id" : "fa-bullseye",
        "width" : 50,
        "height" : 50
      }
    }
  ]
}';

    $json = json_decode($return);
    $json->{'@graph'}[0]->url = $url;
    return $json;
}

function getFileItemJSON($url) {

$return = '{
  "@context" : "http://purl.imsglobal.org/ctx/lti/v1/ContentItem", 
  "@graph" : [ 
    { "@type" : "FileItem",
      "@id" : ":item3",
      "url" : "http://developers.imsglobal.org/images/imscertifiedsm.png",
      "mediaType" : "application/vnd.ims.imsccv1p3", 
      "copyAdvice" : true
    }
  ]
}';

    $json = json_decode($return);
    $json->{'@graph'}[0]->url = $url;
    return $json;
}
