<?php
// After the Deadline Proxy Script
// *phear*

// You get the option of hardcoding your API key here.  Do this if you don't want people seeing
// your key when they do View -> Source.  
$API_KEY = "";

if ( $_SERVER['REQUEST_METHOD'] === 'POST' )
{
   $postText = trim(file_get_contents('php://input'));
}

if (strcmp($API_KEY, "") != 0)
{
   $postText .= '&key=' . $API_KEY;
}

// I am a vampire
// I have lost my fangs

$url = $_GET['url'];

/* this function directly from akismet.php by Matt Mullenweg.  *props* */
function AtD_http_post($request, $host, $path, $port = 80) 
{
   $http_request  = "POST $path HTTP/1.0\r\n";
   $http_request .= "Host: $host\r\n";
   $http_request .= "Content-Type: application/x-www-form-urlencoded\r\n";
   $http_request .= "Content-Length: " . strlen($request) . "\r\n";
   $http_request .= "User-Agent: AtD/0.1\r\n";
   $http_request .= "\r\n";
   $http_request .= $request;            

   $response = '';                 
   if( false != ( $fs = @fsockopen($host, $port, $errno, $errstr, 10) ) ) 
   {                 
      fwrite($fs, $http_request);

      while ( !feof($fs) )
      {
          $response .= fgets($fs);
      }
      fclose($fs);
      $response = explode("\r\n\r\n", $response, 2);
   }
   return $response;
}

// So I'm sad and I feel lonely
// So I cry and I'm very angry
// And I hate some garlic
// So I'm so no more sad and
// Ache yeah yeah

$data = AtD_http_post($postText, "service.afterthedeadline.com", $url);

// I am a vampire and I am looking in the city
// Pretty girls don't look at me
// Don't look at me
// Cause I don't have my fangs
// But I have lost my fangs

header("Content-Type: text/xml");
echo $data[1];

// -- Antsy Pants, Vampire
?>
