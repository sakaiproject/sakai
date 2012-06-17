<?php
error_reporting(E_ALL & ~E_NOTICE & ~E_DEPRECATED);
ini_set("display_errors", 1);
header('Content-Type: text/html; charset=utf-8');
session_start();
?>
<html>
<head>
  <title>IMS Learning Tools Interoperability Extensions</title>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
</head>
<body style="font-family:sans-serif;">
<h1>IMS Learning Tools Interoperability Extensions</h1>
<p>
This is the documentation for a set of IMS Learning Tools Interoperability 
<a href="http://www.imsglobal.org/developers/BLTI/" target="_new">1.0</a> / 
<a href="http://www.imsglobal.org/developers/LTI/test/v1p1/index.php" target="_new">1.1</a>
extensions.  These extensions have been implemented in a number of LMS systems / 
extensions:
<ul>
<li><a href="http://www.sakaiproject.org" target="_new">Sakai 2.8</a> and later</li>
<li><a href="http://code.google.com/p/basiclti4moodle/" target="_new">Basic LTI for Moodle (basiclti4moodle)</a> that supports Moodle 1.9 and later.   Note that the built-in support in Moodle 2.2 does not support these
extensions but the basiclti4moodle modue can be added to Moodle 2.2 and later as well.</li>
<li><a href="http://www.spvsoftwareproducts.com/bb/basiclti/" target="_new">SPV Software Basic LTI Blackboard Building Block</a> </li>
<li><a href="http://www.spvsoftwareproducts.com/powerlinks/basiclti/" target="_new">SPV Software Basic LTI PowerLink for WebCT</a></li>
<li><a href="http://www.atutor.ca" target="_new">ATutor</a> version 2.0.2 and later</li>
</ul>
<h1>Test Harness</h1>
<p>If you have an LMS that supports the extensions, you can use
the following test harness:
<pre>
<?php
require_once("util/lti_util.php");

  $cur_url = curPageURL();
  $cur_url = str_replace("index.php","tool.php",$cur_url);
echo("URL: ".$cur_url."\n");
?>
Key: 12345
Secret: secret
</pre>
<h1>Documentation</h1>
<p>
The IMS LTI specifications allows the definition of extensions as long as those extensions
are prefixed with <b>"ext_"</b>.   A Tool Provider can make use of the extensions of the Tool Consumer
adds the appropriate values to the launch.
</p>
<p>
Each of these extension services takes HTML form encoded POST parameters signed by OAuth using the 
same key/secret for the launch for the particular <b>resource_link_id</b>.
</p>
<h2>Roster (Memberships) Retrieval Service</h2>
<p>
This is the most useful of the extensions - it allows the Tool Provider to retrieve
the entire roster of the <b>context_id</b> (i.e. course).   If the TC is willing to 
provide the roster it adds the following values to the launch:
<pre>
ext_ims_lis_memberships_url=http://localhost:8080/imsblis/service/
ext_ims_lis_memberships_id=7d65a1b397c0d4b1e86b6
</pre>
<p>
To retrieve the roster, the Tool Provider sends the following POST data (form encoded)
and signed using OAuth to the <b>ext_ims_lis_memberships_url</b>.
<pre>
POST http://localhost:8080/imsblis/service/

id=7d65a1b397c0d4b1e86b6
lti_message_type=basic-lis-readmembershipsforcontext
lti_version=LTI-1p0 
oauth_callback=about:blank
oauth_consumer_key=12345
oauth_nonce=0c9a78b4fef3a8c06852af669b682ac4
oauth_signature=lyd/UAIyf/82kyw/bljrxILcGhM=
oauth_signature_method=HMAC-SHA1
oauth_timestamp=1338867958
oauth_version=1.0
</pre>
The <b>id</b> is the one provided on the launch as <b>ext_ims_lis_memberships_id</b>.  This value
is opaque and likely signed by the Tool Consumer to insure that it is not modified by the Tool Provider.
</p>
<b>Returned Data</b>
<pre>
&lt;?xml version="1.0" encoding="UTF-8" standalone="no"?&gt;
&lt;message_response&gt;
  &lt;lti_message_type&gt;basic-lis-readmembershipsforcontext&lt;/lti_message_type&gt;
  &lt;members&gt;
    &lt;member&gt;
      &lt;lis_result_sourcedid&gt;7d65a1b397&lt;/lis_result_sourcedid&gt;
      &lt;person_contact_email_primary&gt;csev@ppp.co&lt;/person_contact_email_primary&gt;
      &lt;person_name_family&gt;Severance&lt;/person_name_family&gt;
      &lt;person_name_full&gt;Charles Severance&lt;/person_name_full&gt;
      &lt;person_name_given&gt;Charles&lt;/person_name_given&gt;
      &lt;person_sourcedid&gt;csev&lt;/person_sourcedid&gt;
      &lt;role&gt;Instructor&lt;/role&gt;
      &lt;user_id&gt;422e09b8-b53a-45dc-a4e5-196d3f749782&lt;/user_id&gt;
    &lt;/member&gt;
  &lt;/members&gt;
  &lt;statusinfo&gt;
    &lt;codemajor&gt;Success&lt;/codemajor&gt;
    &lt;codeminor&gt;fullsuccess&lt;/codeminor&gt;
    &lt;severity&gt;Status&lt;/severity&gt;
  &lt;/statusinfo&gt;
&lt;/message_response&gt;
</pre>

<h2>Outcome (Grade) Setting Service</h2>
<p>
<b>Note:</b> While this might be the only way to set an outcome in some situations, 
IMS LTI 1.1 now includes as part of the standard an outcome service.  Tools should
use the LTI 1.1 outcome service whenever possible.
</p>
<p>
This is the most useful of the extensions - it allows the Tool Provider to retrieve
the entire roster of the <b>context_id</b> (i.e. course).   If the TC is willing to 
provide the roster it adds the following values to the launch:
<pre>
ext_ims_lis_basic_outcome_url=http://localhost:8080/imsblis/service/
ext_ims_lis_resultvalue_sourcedids=decimal
lis_result_sourcedid=7d65a1b397
</pre>
<p>
The <b>lis_result_sourcedid</b> is not prefixed with "ext_" because it is a valid launch parameter
in LTI 1.0 and 1.1.  The <b>ext_ims_lis_resultvalue_sourcedids</b> is optional and if present is 
a comma-separated list of the kinds of outcomes this LMS is capable of supporting taken from the list:
decimal, percentage, ratio, passfail, letteraf, letterafplus, or freetext.
The tool can always assume "decimal" is possible even if the <b>ext_ims_lis_resultvalue_sourcedids</b>
value is not present.
</p>
<p>
To retrieve the roster, the Tool Provider sends the following POST data (form encoded)
and signed using OAuth to the <b>ext_ims_lis_basic_outcome_url</b>.
<pre>
POST http://localhost:8080/imsblis/service/

lti_message_type=basic-lis-updateresult
lti_version=LTI-1p0
oauth_callback=about:blank
oauth_consumer_key=12345
oauth_nonce=bdcb2d645ba41255f0bce60bb31e3ace
oauth_signature=0G0SXTS1eLMh4FCZ9q4JYlLjSyE=
oauth_signature_method=HMAC-SHA1
oauth_timestamp=1338868573
oauth_version=1.0
result_resultscore_textstring=0.10
result_resultvaluesourcedid=decimal
result_statusofresult=interim
sourcedid=7d65a1b397
</pre>
<p>
<b>sourcedid</b><br>
This is the value from the launch <b>lis_result_sourcedid</b> for the user/resource/course combination.  
This value is opaque and likely signed by the Tool Consumer to insure that it is not modified by 
the Tool Provider.
</p>
<p>
<b>result_resultvaluesourcedid</b><br/>
This can be one of decimal, percentage, ratio, passfail, letteraf, letterafplus, or freetext.
<p>
<b>result_statusofresult</b><br/>
This can be interm, final, unmoderated, or moderated.   The Tool Consumer may not support
this and may ignore this value.
</p>
<p>
<b>result_resultscore_textstring</b><br/>
This value depends on the <b>result_resultvaluesourcedid</b>.  Remember that if the 
Tool Comnsumer supports outcomes, it must accept "decimal" type outcomes.
<pre>
decimal        A floating point number between 0.0 and 1.0.  (This must be supported)
percentage     An integer number from 0 to 100 including an optional percent sign
ratio          Two positive integer numbers separated by a slash to be treated as a 
			   rational number.  An  example of this would be 18/20
letteraf       The letters A through F
letterafplus   The letters A through F with an optional plus sign or minus sign
passfail       Either "pass" or "fail"
freetext       Up to 1024 characters of text
</pre>
</p>

<p><b>Returned Data</b></p>

<pre>
&lt;?xml version="1.0" encoding="UTF-8" standalone="no"?&gt;
&lt;message_response&gt;
  &lt;lti_message_type&gt;basic-lis-updateresult&lt;/lti_message_type&gt;
  &lt;statusinfo&gt;
    &lt;codemajor&gt;Success&lt;/codemajor&gt;
    &lt;codeminor&gt;fullsuccess&lt;/codeminor&gt;
    &lt;severity&gt;Status&lt;/severity&gt;
  &lt;/statusinfo&gt;
&lt;/message_response&gt;
</pre>

<h2>Launch Setting Service</h2>
<p>
This setting extension is not widely used.   It allows a Tool Provider to
store up to 4K of data to be included on every launch.
There is one setting per <b>resource_link_id</b> across all values for <b>user_id</b>.
The idea was to allow a service to be built that required no storage
on a per <b>resource_link_id</b> basis.
</p>
<p>
If the TC is willing to store launch data, it adds the following values to the launch:
<pre>
ext_ims_lti_tool_setting_id=832823923899238
ext_ims_lti_tool_setting_url=http://localhost:8080/imsblis/service/
</pre>
<p>
If there is a setting associate with the launch, the TC adds this parameter to every 
launch from the <b>resource_link_id</b>:
<pre>
ext_ims_lti_tool_setting=previous launch data
</pre>
</p>
<p>
To set the launch data, the Tool Provider does the following:
<pre>
POST http://localhost:8080/imsblis/service/ 

id=832823923899238
lti_message_type=basic-lti-savesetting
lti_version=LTI-1p0
oauth_callback=about:blank
oauth_consumer_key=12345
oauth_nonce=14c6211cc66d9525304f085551d50b1d
oauth_signature=IGy7KlO9DZ1qfShYBYE+BhCfuec=
oauth_signature_method=HMAC-SHA1
oauth_timestamp=1338872426
oauth_version=1.0
setting=Some Setting Data
</pre>
The <b>id</b> value is the value from <b>ext_ims_lti_tool_setting_id</b> on the launch.
The <b>setting</b> is the data to be included in the setting.  It is a maximum of 4K.

<p><b>Returned Data</b></p>

<pre>
Response from server
&lt;?xml version="1.0" encoding="UTF-8" standalone="no"?&gt;
&lt;message_response&gt;
  &lt;lti_message_type&gt;basic-lti-savesetting&lt;/lti_message_type&gt;
  &lt;statusinfo&gt;
    &lt;codemajor&gt;Success&lt;/codemajor&gt;
    &lt;codeminor&gt;fullsuccess&lt;/codeminor&gt;
    &lt;severity&gt;Status&lt;/severity&gt;
  &lt;/statusinfo&gt;
&lt;/message_response&gt;
</pre>


