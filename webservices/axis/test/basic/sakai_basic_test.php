<head>
<title>Sakai Axis Testing</title>
</head>
<body>
<?php
require_once('SOAP/Client.php');

if ( ! $_POST['url'] ) $_POST['url'] = "http://nightly2.sakaiproject.org/sakai-axis/";

if ( $_POST['login'] ) {
  $site_url = $_POST['url'] . 'SakaiLogin.jws?wsdl';
  echo ("Loggging in to Sakai Web Services at ".$site_url);
  $wsdl=new SOAP_WSDL($site_url);

  echo ("<pre>\r\n");
  echo ( $wsdl->generateProxyCode() );
  echo ("</pre>\r\n");

  // Create an object directly from the proxy code
  $myProxy=$wsdl->getProxy();

  $session=$myProxy->login("admin","admin");

  echo ("Session:");
  print_r ($session );
  $_POST['session'] = $session;
}

if ( $_POST['logout'] ) {
  $_POST['session'] = null;
}

if ( $_POST['check'] ) {
  $site_url = $_POST['url'] . 'SakaiSession.jws?wsdl';
  echo("Retrieving Session Information From ".$site_url);
  $wsdl=new SOAP_WSDL($site_url);

  echo ("<pre>\r\n");
  echo ( $wsdl->generateProxyCode() );
  echo ("</pre>\r\n");
  
  // Create an object directly from the proxy code
  $myProxy=$wsdl->getProxy();
  
  $info=$myProxy->checkSession($_POST['session']);
  
  echo("Info:");
  print_r ($info );

}

if ( $_POST['sites'] ) {
  $site_url = $_POST['url'] . 'SakaiSite.jws?wsdl';
  echo("Retrieving Site List From ".$site_url);
  $wsdl=new SOAP_WSDL($site_url);

  echo ("<pre>\r\n");
  echo ( $wsdl->generateProxyCode() );
  echo ("</pre>\r\n");
  
  // Create an object directly from the proxy code
  $myProxy=$wsdl->getProxy();
  
  $sites=$myProxy->getSites($_POST['session'],"",1,999);
  
  echo("Sites:");
  print_r ($sites );
}

if ( $_POST['nightly'] ) {
  echo("<h3>Once 2.0 is released, switch to nightly.sakaiproject.org</h3>");
  $_POST['url'] = "http://nightly2.sakaiproject.org/sakai-axis/";
}

if ( $_POST['local'] ) {
  $_POST['url'] = "http://localhost:8080/sakai-axis/";
}

if ( $_POST['proxy'] ) {
  echo("<h1>Make sure to run the tunnel program from port 8081 to port 8080</h1>");
  $_POST['url'] = "http://localhost:8081/sakai-axis/";
}

?>
<FORM ACTION="<?php echo $_SERVER['PHP_SELF']; ?>" METHOD=post>
Enter the URL for the web services host (must end in a / )
<br>
<INPUT TYPE=TEXT size=60 NAME="url" VALUE="<?php echo $_POST['url']?>">
<BR>
Account / PW 
<INPUT TYPE=TEXT size=10 NAME="id" VALUE="admin">
&nbsp;
<INPUT TYPE=PASSWORD size=10 NAME="pw" VALUE="admin">
<BR>
Sakai Web Services Session Key : <?php echo $_POST['session']; ?>
<INPUT TYPE=Hidden size=20 NAME="session" VALUE="<?php echo $_POST['session']; ?>">
<BR>
<INPUT TYPE=submit NAME=login VALUE="Login">
<?php
if ( $_POST['session'] ) {
  echo("<INPUT TYPE=submit NAME=logout VALUE=Logout>\r\n");
  echo("<INPUT TYPE=submit NAME=check VALUE='Check Session'>\r\n");
  echo("<INPUT TYPE=submit NAME=sites VALUE='Retrieve site list'>\r\n");
} else {
  echo("<INPUT TYPE=submit NAME=nightly VALUE='Sakai Nightly URL'>\r\n");
  echo("<INPUT TYPE=submit NAME=local VALUE='Local Host URL'>\r\n");
  echo("<INPUT TYPE=submit NAME=proxy VALUE='Local URL w/Proxy'>\r\n");

}
?>
</FORM>
</body>
