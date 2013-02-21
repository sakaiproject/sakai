<?php

$cur_url = curPageURL();
$endpoint = str_replace("ext/lori_xml.php","tool.php",$cur_url);

$getCourseStructureRequest = '<imsx_POXEnvelopeRequest xmlns="http://www.imsglobal.org/lis/oms1p0/pox">
    <imsx_POXHeader>
        <imsx_POXRequestHeaderInfo>
            <imsx_version>V1.0</imsx_version>
            <imsx_messageIdentifier>MESSAGE</imsx_messageIdentifier>
        </imsx_POXRequestHeaderInfo>
    </imsx_POXHeader>
    <imsx_POXBody>
        <getCourseStructureRequest>
            <params>
                <courseId>CONTEXT_ID</courseId>
                <userId>USER_ID</userId>
                <sourcedGUID>
                    <sourcedId>LIS_RESULT_SOURCEDID</sourcedId>
                </sourcedGUID>
            </params>
        </getCourseStructureRequest>
    </imsx_POXBody>
</imsx_POXEnvelopeRequest>';

$addCourseResourcesRequest = '<imsx_POXEnvelopeRequest xmlns="http://www.imsglobal.org/lis/oms1p0/pox">
  <imsx_POXBody>
    <addCourseResourcesRequest>
      <params>
        <courseId>CONTEXT_ID</courseId>
        <folderId>FOLDER_ID</folderId>
        <userId>USER_ID</userId>
        <sourcedGUID>
          <sourcedId>LIS_RESULT_SOURCEDID</sourcedId>
        </sourcedGUID>
        <resources>
          <resource>
            <launchUrl>'.$endpoint.'</launchUrl>
            <tempId>4fe4d1794b65eb4b43000001</tempId>
            <title>Color Test</title>
            <type>lti</type>
          </resource>
        </resources>
      </params>
    </addCourseResourcesRequest>
  </imsx_POXBody>
  <imsx_POXHeader>
    <imsx_POXRequestHeaderInfo>
      <imsx_messageIdentifier>8583988528697</imsx_messageIdentifier>
      <imsx_version>V1.0</imsx_version>
    </imsx_POXRequestHeaderInfo>
  </imsx_POXHeader>
</imsx_POXEnvelopeRequest>';


