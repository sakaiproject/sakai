<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
	<title>Cross-Browser Rich Text Editor</title>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<meta name="PageURL" content="http://www.kevinroth.com/rte/demo.php" />
	<meta name="PageTitle" content="Cross-Browser Rich Text Editor (PHP Demo)" />
	<script language="JavaScript" type="text/javascript" src="html2xhtml.js"></script>
	<!-- To decrease bandwidth, use richtext_compressed.js instead of richtext.js //-->
	<script language="JavaScript" type="text/javascript" src="richtext.js"></script>
</head>
<body>

<h2>Cross-Browser Rich Text Editor</h2>
<p><a href="http://www.planetsourcecode.com/vb/scripts/ShowCode.asp?txtCodeId=3508&amp;lngWId=2" target="_blank"><img src="/images/PscContestWinner.jpg" height="88" width="409" alt="PscContestWinner" border="0"></a></p>
<p>The cross-browser rich-text editor (RTE) is based on the <a href="http://msdn.microsoft.com/archive/default.asp?url=/archive/en-us/samples/internet/ie55/EditRegions/default.asp" target="_blank">designMode()</a> functionality introduced in Internet Explorer 5, and implemented in Mozilla 1.3+ using the <a href="http://www.mozilla.org/editor/midas-spec.html" target="_blank">Mozilla Rich Text Editing API</a>.  The cross-browser rich-text editor now includes <b>table support</b> (as of 2/10/2005) as well as an option to generate <b>xhtml-compliant code</b> (as of 2/24/2005).</p>
<p><b>This code is public domain.</b> Redistribution and use of this code, with or without modification, is permitted.</p>
<p>For frequently asked question and support, please visit <a href="http://www.kevinroth.com/forums/index.php?c=2">http://www.kevinroth.com/forums/index.php?c=2</a></p>
<p><b>Requires:</b> IE5+/<a href="http://www.mozilla.org/products/mozilla1.x/">Mozilla</a> 1.3+/<a href="http://www.mozilla.org/products/firefox/" target="_blank">Mozilla Firebird/Firefox</a> 0.6.1+/<a href="http://channels.netscape.com/ns/browsers/download.jsp" target="_blank">Netscape</a> 7.1+, or any other browser that fully supports designMode() for all rich-text features to function properly.  All other browsers will display a standard textarea box instead.</p>
<p><b>Source:</b> <a href="rte.zip">rte.zip</a>, <a href="rte.tar.gz">rte.tar.gz</a><br>
Included in the zip are <a href="demo.htm">HTML</a>, <a href="demo.asp">ASP</a>, and <a href="demo.php">PHP</a> demos.  Also, here is an html demo showing <a href="multi.htm">multiple RTEs</a> on one page.</p>
<p><b>Change Log:</b> <a href="changelog.txt">changelog.txt</a></p>

<p><b>If you feel that the work I've done has value to you,</b> I would greatly appreciate a paypal donation (click button below).  Another way you can help me out is to <a href="http://www.FreeFlatScreens.com/default.aspx?referer=11055453" target="_blank">sign up for a free flat screen</a>, to help me get mine.  Again, I am very grateful for any and all contributions.</p>
<form action="https://www.paypal.com/cgi-bin/webscr" method="post">
<input type="hidden" name="cmd" value="_xclick">
<input type="hidden" name="business" value="kevin@kevinroth.com">
<input type="hidden" name="no_note" value="1">
<input type="hidden" name="currency_code" value="USD">
<input type="hidden" name="tax" value="0">
<input type="hidden" name="lc" value="US">
<input type="image" src="/images/paypal_donate.gif" border="0" name="submit" alt="Make payments with PayPal - it's fast, free and secure!">
</form>

<!-- START Demo Code -->
<form name="RTEDemo" action="<?=$_SERVER["PHP_SELF"]?>" method="post" onsubmit="return submitForm();">
<script language="JavaScript" type="text/javascript">
<!--
function submitForm() {
	//make sure hidden and iframe values are in sync before submitting form
	//to sync only 1 rte, use updateRTE(rte)
	//to sync all rtes, use updateRTEs
	updateRTE('rte1');
	//updateRTEs();
	
	//change the following line to true to submit form
	return true;
}

//Usage: initRTE(imagesPath, includesPath, cssFile, genXHTML)
initRTE("images/", "", "", true);
//-->
</script>
<noscript><p><b>Javascript must be enabled to use this form.</b></p></noscript>

<script language="JavaScript" type="text/javascript">
<!--
<?php
//format content for preloading
if (!(isset($_POST["rte1"]))) {
	$content = "here's the " . chr(13) . "\"preloaded <b>content</b>\"";
	$content = rteSafe($content);
} else {
	//retrieve posted value
	$content = rteSafe($_POST["rte1"]);
}
?>//Usage: writeRichText(fieldname, html, width, height, buttons, readOnly)
writeRichText('rte1', '<?=$content;?>', 520, 200, true, false);
//-->
</script>

<p>Click submit to post the form and reload with your rte content.</p>
<p><input type="submit" name="submit" value="Submit"></p>
</form>
<?php
function rteSafe($strText) {
	//returns safe code for preloading in the RTE
	$tmpString = $strText;
	
	//convert all types of single quotes
	$tmpString = str_replace(chr(145), chr(39), $tmpString);
	$tmpString = str_replace(chr(146), chr(39), $tmpString);
	$tmpString = str_replace("'", "&#39;", $tmpString);
	
	//convert all types of double quotes
	$tmpString = str_replace(chr(147), chr(34), $tmpString);
	$tmpString = str_replace(chr(148), chr(34), $tmpString);
//	$tmpString = str_replace("\"", "\"", $tmpString);
	
	//replace carriage returns & line feeds
	$tmpString = str_replace(chr(10), " ", $tmpString);
	$tmpString = str_replace(chr(13), " ", $tmpString);
	
	return $tmpString;
}
?>
<!-- END Demo Code -->

</body>
</html>
