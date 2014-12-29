<?php

function getResultJSON($grade, $comment) {

$resultArray = array(
  "@context" => "http://purl.imsglobal.org/ctx/lis/v2/Result",
  "@type" => "Result",
  "comment" => $comment,
  "resultScore" => array(
    "@type" => "decimal",
    "@value" => $grade
  ),
);

  return $resultArray;
}
