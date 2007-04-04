<cfsetting enablecfoutputonly="true" showdebugoutput="false">
<!---
 * FCKeditor - The text editor for Internet - http://www.fckeditor.net
 * Copyright (C) 2003-2007 Frederico Caldeira Knabben
 *
 * == BEGIN LICENSE ==
 *
 * Licensed under the terms of any of the following licenses at your
 * choice:
 *
 *  - GNU General Public License Version 2 or later (the "GPL")
 *    http://www.gnu.org/licenses/gpl.html
 *
 *  - GNU Lesser General Public License Version 2.1 or later (the "LGPL")
 *    http://www.gnu.org/licenses/lgpl.html
 *
 *  - Mozilla Public License Version 1.1 or later (the "MPL")
 *    http://www.mozilla.org/MPL/MPL-1.1.html
 *
 * == END LICENSE ==
 *
 * Sample page for ColdFusion.
--->

<cfoutput>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
	<title>FCKeditor - Sample</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta name="robots" content="noindex, nofollow">
	<link href="../sample.css" rel="stylesheet" type="text/css" />
</head>

<body>

<h1>FCKeditor - ColdFusion - Sample 1</h1>

This sample displays a normal HTML form with a FCKeditor with full features enabled; invoked by a ColdFusion Custom Tag / Module.
<hr>
<form method="POST" action="#cgi.script_name#">
</cfoutput>

<cfmodule
	template="../../fckeditor.cfm"
	basePath="#Left(cgi.script_name, FindNoCase('_samples', cgi.script_name)-1)#"
	instanceName="myEditor"
	value='This is some sample text. You are using <a href="http://fckeditor.net/" target="_blank">FCKeditor</a>.'
	width="100%"
	height="200"
>
<cfoutput>
<br />
<input type="submit" value="Submit">
<br />
</form>
</cfoutput>

<cfif isDefined( 'FORM.fieldnames' )>
	<cfoutput>
	<hr />
	<style>
	<!--
		td, th { font: 11px Verdana, Arial, Helv, Helvetica, sans-serif; }
	-->
	</style>
	<table border="1" cellspacing="0" cellpadding="2" bordercolor="darkblue" bordercolordark="darkblue" bordercolorlight="darkblue">
	<tr>
		<th colspan="2" bgcolor="darkblue"><font color="white"><strong>Dump of FORM Variables</strong></font></th>
	</tr>
	<tr>
		<td bgcolor="lightskyblue">FieldNames</td>
		<td>#FORM.fieldNames#</td>
	</tr>
	<cfloop list="#FORM.fieldnames#" index="key">
	<tr>
		<td valign="top" bgcolor="lightskyblue">#key#</td>
		<td>#HTMLEditFormat(evaluate("FORM.#key#"))#</td>
	</tr>
	</cfloop>
	</table>
	</cfoutput>
</cfif>

</body>
</html>
<cfsetting enablecfoutputonly="false">