<?php

function getContentJSON($url) {

$return = '{
  "@context" : "http://purl.imsglobal.org/ctx/lti/v1/ContentItem", 
  "@graph" : [ 
    { "@type" : "FileItem",
      "url" : "http://developers.imsglobal.org/images/imscertifiedsm.png",
      "mediaType" : "image/png",
      "text" : "The mascot for the Sakai Project", 
      "title" : "The fearsome mascot of the Sakai Project",
      "placementAdvice" : {
        "displayWidth" : 150,
        "displayHeight" : 154,
        "presentationDocumentTarget" : "embed"
      }
    }
  ]
}';

    $json = json_decode($return);
    $json->{'@graph'}[0]->url = $url;
    return $json;
}
