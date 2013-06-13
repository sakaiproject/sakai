<?php
error_reporting(E_ALL & ~E_NOTICE & ~E_DEPRECATED);
ini_set("display_errors", 1);
header('Content-Type: text/html; charset=utf-8');
session_start();
?>
<html>
<head>
  <title>Sakai LTI Unit Tests</title>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
</head>
<body style="font-family:sans-serif;">
<a href="http://www.sakaiger.com" target="_new">
<img src="http://www.sakaiger.com/images/Sakaiger.png" align="right"></a>
<h1>Sakai LTI Unit Tests</h1>
<p>
This code is sample code and unit test code for IMS LTI 1.1 
that is maintained in the Sakai project.  It not only
supports the IMS LTI 1.1 code - it also supports the Sakai
LTI extensions.  
</p>
<p>
The latest Sakai LTI documentation is here:
<a href="https://confluence.sakaiproject.org/display/LTI/Home" target="_blank">
https://confluence.sakaiproject.org/display/LTI/Home</a>.
</p>
<p>If you want to test your LMS with LTI 1.1 or Sakai's extensions, use
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
</p>
<p>
If you want to test your tool, you can use this fake LMS test harness:
<pre>
<?php
require_once("util/lti_util.php");

  $cur_url = curPageURL();
  $cur_url = str_replace("index.php","lms.php",$cur_url);
echo("URL: ".$cur_url."\n");
?>
</pre>
<h1>Using This Code</h1>
<p>
You are welcome to grab a copy of this code at<br/>
<a href="https://source.sakaiproject.org/svn/basiclti/trunk/basiclti-docs/resources/docs/sakai-api-test/" target="_blank">
https://source.sakaiproject.org/svn/basiclti/trunk/basiclti-docs/resources/docs/sakai-api-test/</a>.
</p>
<p>
I host a copy of this code at:<br/>
<a href="https://online.dr-chuck.com/sakai-api-test/" target="_blank">
https://online.dr-chuck.com/sakai-api-test/</a>
- it is good to test with https as much as you can to avoid surprises when you swtich 
from http to https.
</p>
<p>
<b>Note:</b> I recently found a problem in the www.dr-chuck.com hosted version of this code because 
1and1 (my hosting provider) seems to eat Authorization headers so making grade callbacks to 
www.dr-chuck.com won't work.   Hence the move to online.dr-chuck.com - with the added benefit of 
https testing as well.
</p>
<p>
If you have questions, contact Dr. Chuck.
