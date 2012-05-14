<head>
<title>Sakai Axis Testing</title>
</head>
<body>
<?php

ini_set("soap.wsdl_cache_enabled", "0");

// The Site ID to play with
$siteid = $_POST['siteid'];
if ( strlen($siteid) < 1 ) $siteid = 'mercury';
$id = $_POST['id'];
if ( strlen($id) < 1 ) $id = 'admin';
$pw = $_POST['pw'];
if ( strlen($pw) < 1 ) $pw = 'admin';

if ( ! $_POST['url'] ) $_POST['url'] = "http://localhost:8081/sakai-axis/";

echo("<pre>\n");
if ( $_POST['login'] ) {
  $site_url = $_POST['url'] . 'SakaiLogin.jws?wsdl';
  echo ("Loggging in to Sakai Web Services at ".$site_url."\n");
  $soapClient =new SoapClient($site_url);

  $ap_param = array( 'id' => $id, 'pw' => $pw);
  $session = $soapClient->__soapCall("login", $ap_param); 

  echo ("Session:");
  print_r ($session );
  $_POST['session'] = $session;
}

if ( $_POST['logout'] ) {
  $_POST['session'] = null;
}

if ( $_POST['check'] ) {
  $site_url = $_POST['url'] . 'SakaiScript.jws?wsdl';
  echo("Retrieving Session Information From ".$site_url."\n");

  $soapClient =new SoapClient($site_url);

  $ap_param = array( 'sessionid' => $_POST['session']);
  $info = $soapClient->__soapCall("checkSession", $ap_param);

  echo("Info:");
  print_r ($info );

}

if ( $_POST['sites'] ) {
  $site_url = $_POST['url'] . 'SakaiScript.jws?wsdl';
  echo("Retrieving Site List From ".$site_url."\n");

  $soapClient =new SoapClient($site_url);

  $ap_param = array( 'sessionid' => $_POST['session']);
  $sites = $soapClient->__soapCall("getSitesUserCanAccess", $ap_param);

  echo("Sites:");
  $sites = str_replace("<","&lt;",$sites);
  $sites = str_replace(">","&gt;\n",$sites);
  print_r ($sites );
}

// public String getAssignmentsForContext(String sessionid, String context) throws AxisFault{

if ( $_POST['getassign'] ) {
  $site_url = $_POST['url'] . 'Assignments.jws?wsdl';
  echo("Retrieving Assignment List From ".$site_url."\n");

  $soapClient =new SoapClient($site_url);

  $ap_param = array( 'sessionid' => $_POST['session'], "context"=> $siteid);
  $assignment = $soapClient->__soapCall("getAssignmentsForContext", $ap_param);

  echo("Sites:");
  $assignment = str_replace("<","&lt;",$assignment);
  $assignment = str_replace(">","&gt;\n",$assignment);
  print_r ($assignment );
}

// public String createAssignment(String sessionId, String context, String title, long dueTime, long openTime, long closeTime, int maxPoints, int gradeType, String instructions, int subType)

if ( $_POST['addassign'] ) {
  $site_url = $_POST['url'] . 'Assignments.jws?wsdl';
  echo("Adding an assignment via ".$site_url."\n");

  $soapClient =new SoapClient($site_url);

  $ap_param = array( 'sessionid' => $_POST['session'], "context"=> $siteid,
    "title" => "My Title", "dueTime" => 0, "openTime" => 0, "closeTime" => 0, 
    "maxPoints" => 100, "gradeType" => 0, 
    "instructions" => "Have fun", "subType" => 0
  );

  print_r($ap_param);

  $assignment = $soapClient->__soapCall("createAssignment", $ap_param);

  echo("Result:");
  $assignment = str_replace("<","&lt;",$assignment);
  $assignment = str_replace(">","&gt;\n",$assignment);
  print_r ($assignment );
}

// public String addMessage(String sessionid, String context, String forum, String topic, String user, String title, String body )

if ( $_POST['addmessage'] ) {
  $site_url = $_POST['url'] . 'MessageForums.jws?wsdl';
  echo("Retrieving Message Forums From ".$site_url."\n");

  $soapClient =new SoapClient($site_url);

  $ap_param = array( 
	 'sessionid' => $_POST['session'], 'context' => $siteid, 
     'forum' => 'Forum from PHP', 'topic' => 'Topic from PHP', 
     'user' => $id,
     'title' => 'PHP Message Title', 'body' => 'PHP Message Body');
  $session = $soapClient->__soapCall("addMessage", $ap_param);

  echo("Session:");
  print_r ($session );
}

echo("</pre>\n");

?>
<form action="<?php echo $_SERVER['PHP_SELF']; ?>" method=post>
Enter the URL for the web services host (must end in a / )
<br>
<input type="text" size=70 name="url" value="<?php echo $_POST['url']?>">
<BR>
Account / PW 
<input type="text" size=10 name="id" value="<?php echo $id; ?>">
&nbsp;
<input type="text" size=10 name="pw" value="<?php echo $pw; ?>">
<br/>
Site ID: 
<input type="text" size=50 name="siteid" value="<?php echo $siteid; ?>">
<br/>
Sakai Web Services Session Key : <?php echo $_POST['session']; ?>
<input type="hidden" size=20 name="session" value="<?php echo $_POST['session']; ?>">
<br>
<input type=submit name=login value=Login>
<?php
if ( $_POST['session'] ) {
  echo("<input type=submit name=check value='Check Session'>\r\n");
  echo("<input type=submit name=sites value='Retrieve site list'>\r\n");
  echo("<input type=submit name=addassign value='Add an assignment'>\r\n");
  echo("<input type=submit name=getassign value='Get assignment list'>\r\n");
  echo("<input type=submit name=addmessage value='Add a discussion message'>\r\n");
  echo("<input type=submit name=logout value=Logout>\r\n");
}
?>
</form>
</body>
