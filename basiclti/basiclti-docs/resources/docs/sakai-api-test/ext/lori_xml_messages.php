<?php

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
                <courseId>
                    <language>en-us</language>
                    <textString>CONTEXT_ID</textString>
                </courseId>
                <userId>
                    <language>en-us</language>
                    <textString>USER_ID</textString>
                </userId>
            </params>
            <signature>
                <encrypted_hash>WItAKSt</encrypted_hash>
            </signature>
        </getCourseStructureRequest>
    </imsx_POXBody>
</imsx_POXEnvelopeRequest>';


