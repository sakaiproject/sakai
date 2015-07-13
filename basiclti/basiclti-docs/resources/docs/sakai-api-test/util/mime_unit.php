<?php

/**
 * Note from Chuck: This was downloaded from 
 *
 * https://code.google.com/p/mimeparse/
 * 
 * on July 13, 2015 - it has an MIT license.
 *
 * I split it into two files to pull out the unit tests
 */


// Unit tests //////////////////////////////////////////////////////////////////////////////////////////////////////////


$m = new Mimeparse;
if ($m->parse_media_range("application/xml;q=1") === array(0 => "application", 1=> "xml", 2=> array("q" => "1") ))
    echo "application/xml;q=1 - OK<br>";
else 
    echo "application/xml;q=1 - FAIL<br>";
    
if ($m->parse_media_range("application/xml") === array(0 => "application", 1=> "xml", 2=> array("q" => "1") ))
    echo "application/xml - OK<br>";
else 
    echo "application/xml - FAIL<br>";
    
if ($m->parse_media_range("application/xml;q=") === array(0 => "application", 1=> "xml", 2=> array("q" => "1") ))
    echo "application/xml;q= - OK<br>";
else 
    echo "application/xml;q= - FAIL<br>";
    

if ($m->parse_media_range("application/xml ; q=1;b=other") === array(0 => "application", 1=> "xml", 2=> array("q" => "1", "b" => "other") ))
    echo "application/xml ; q=1;b=other - OK<br>";
else 
    echo "application/xml ; q=1;b=other - FAIL<br>";
    
if ($m->parse_media_range("application/xml ; q=2;b=other") === array(0 => "application", 1=> "xml", 2=> array("q" => "1", "b" => "other") ))
    echo "application/xml ; q=2;b=other - OK<br>";
else 
    echo "application/xml ; q=2;b=other - FAIL<br>";
    
/* Java URLConnection class sends an Accept header that includes a single "*" */
if ($m->parse_media_range(" *; q=.2") === array(0 => "*", 1=> "*", 2=> array("q" => ".2") ))
    echo " *; q=.2 - OK<br>";
else 
    echo " *; q=.2 - FAIL<br>";
    
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

$accept = "text/*;q=0.3, text/html;q=0.7, text/html;level=1, text/html;level=2;q=0.4, */*;q=0.5";

echo (1 == $m->quality("text/html;level=1", $accept) ? 'text/html;level=1 - OK<br>' : 'text/html;level=1 - FAIL<br>');
echo (0.7 == $m->quality("text/html", $accept) ? 'text/html - OK<br>' : 'text/html - FAIL<br>');
echo (0.3 == $m->quality("text/plain", $accept) ? 'text/plain - OK<br>' : 'text/plain - FAIL<br>');
echo (0.5 == $m->quality("image/jpeg", $accept) ? 'image/jpeg - OK<br>' : 'image/jpeg - FAIL<br>');
echo (0.4 == $m->quality("text/html;level=2", $accept) ? 'text/html;level=2 - OK<br>' : 'text/html;level=2 - FAIL<br>');
echo (0.7 == $m->quality("text/html;level=3", $accept) ? 'text/html;level=3 - OK<br>' : 'text/html;level=3 - FAIL<br>');
 
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

$supported_mime_types = array("application/xbel+xml", "application/xml");

# direct match
assert_best_match ("application/xbel+xml", "application/xbel+xml");
# direct match with a q parameter
assert_best_match ("application/xbel+xml", "application/xbel+xml; q=1");
# direct match of our second choice with a q parameter
assert_best_match ("application/xml", "application/xml; q=1");
# match using a subtype wildcard
assert_best_match ("application/xml", "application/*; q=1");
# match using a type wildcard
assert_best_match ("application/xml", "* / *");

$supported_mime_types = array( "application/xbel+xml", "text/xml" );
# match using a type versus a lower weighted subtype
assert_best_match ("text/xml", "text/ *;q=0.5,* / *;q=0.1");
# fail to match anything
assert_best_match (null, "text/html,application/atom+xml; q=0.9" );
# common AJAX scenario
$supported_mime_types = array( "application/json", "text/html" );
assert_best_match("application/json", "application/json, text/javascript, */*");
# verify fitness sorting
$supported_mime_types = array( "application/json", "text/html" );
assert_best_match("application/json", "application/json, text/html;q=0.9");


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
$supported_mime_types = array('image/*', 'application/xml');
# match using a type wildcard
assert_best_match ('image/*', 'image/png');
# match using a wildcard for both requested and supported
assert_best_match ('image/*', 'image/*');

function assert_best_match($expected, $header) {
    global $m, $supported_mime_types;

    if ($expected == $m->best_match($supported_mime_types, $header)) 
        echo "$header - OK<br>";
    else 
        echo "$header - FAIL<br>";
}

