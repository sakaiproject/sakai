<head>
<title>Sakai Axis Testing</title>
</head>
<body>
<?php

if ( ! $_POST['url'] ) $_POST['url'] = "http://localhost:8081/sakai-axis/";

echo("<pre>\n");
if ( $_POST['login'] ) {
  $site_url = $_POST['url'] . 'SakaiLogin.jws?wsdl';
  echo ("Loggging in to Sakai Web Services at ".$site_url."\n");
  $soapClient =new SoapClient($site_url);

  $ap_param = array( 'id' => 'admin', 'pw' => 'admin');
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
echo("</pre>\n");

?>
<form action="<?php echo $_SERVER['PHP_SELF']; ?>" method=post>
Enter the URL for the web services host (must end in a / )
<br>
<input type="text" size=60 name="url" value="<?php echo $_POST['url']?>">
<BR>
Account / PW 
<input type="text" size=10 name="id" value="admin">
&nbsp;
<input type="password" size=10 name="pw" value="admin">
<BR>
Sakai Web Services Session Key : <?php echo $_POST['session']; ?>
<input type="hidden" size=20 name="session" value="<?php echo $_POST['session']; ?>">
<br>
<input type=submit name=login value=Login>
<?php
if ( $_POST['session'] ) {
  echo("<input type=submit name=check value='Check Session'>\r\n");
  echo("<input type=submit name=sites value='Retrieve site list'>\r\n");
  echo("<input type=submit name=logout value=Logout>\r\n");
}
?>
</form>
</body>
