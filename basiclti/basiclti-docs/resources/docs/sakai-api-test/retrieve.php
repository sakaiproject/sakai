<?php
if ( !isset($_REQUEST['session']) ) die("Must have session");

echo("<h1>Encrypted Session Unit Test</h1>\n");

$page = false;
$url = false;
$session = $_REQUEST['session'];

if ( isset($_REQUEST['server']) ) {
    $url = $_REQUEST['server'] . '/direct/session/current.json';
}
if ( isset($_POST['url']) ) $url = $_POST['url'];

if (isset($_REQUEST['session']) && isset($_POST["url"]) ) {

    echo("<pre>\nRetrieving ".$_POST["url"]."\n");
    $c = curl_init();
    curl_setopt($c, CURLOPT_URL , $url);
    curl_setopt($c, CURLOPT_VERBOSE, 1);
    curl_setopt($c, CURLOPT_SSL_VERIFYPEER, false);
    curl_setopt($c, CURLOPT_SSL_VERIFYHOST, false);
    $cookie = 'JSESSIONID='.$session;
    curl_setopt($c, CURLOPT_COOKIE, $cookie);
    curl_setopt($c, CURLOPT_RETURNTRANSFER, 1);
    $page = curl_exec($c);
    if ( $page === false ) {
	echo("CURL Error: ".curl_error($c));
    } else {
        echo("GET Returned ".strlen($page)." bytes.\n");
        echo("First few bytes\n".htmlentities(substr($page,0,20))."\n</pre>\n");
    }
    curl_close($c);
}
?>
<form method="POST">
<p>URL:<br/>
<input type="string" name="url" value="<?= htmlentities($url) ?>" size="100"><br/>
<p>Session:<br/>
<input type="text" name="session" value="<?= $_REQUEST['session'] ?>" size="100"><br/>
<input type="submit">
</form>
<?php
if ( $page !== false ) {
    echo("\n<pre>\n");
    echo(htmlentities($page));
    echo("\n</pre>\n");
}

/*
$key = "this is the key";
$plain = "I am plain text";
$crypttext = mcrypt_encrypt(MCRYPT_BLOWFISH, $key, $plain, MCRYPT_MODE_ECB);
echo(bin2hex($crypttext));
echo "\n";

$plainback = mcrypt_decrypt(MCRYPT_BLOWFISH, $key, $crypttext, MCRYPT_MODE_ECB);
echo($plainback);
echo "\n";
*/
