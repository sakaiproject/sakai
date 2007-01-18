/*
 * FCKeditor - The text editor for internet
 * Copyright (C) 2003-2006 Frederico Caldeira Knabben
 * 
 * Licensed under the terms of the GNU Lesser General Public License:
 * 		http://www.opensource.org/licenses/lgpl-license.php
 * 
 * For further information visit:
 * 		http://www.fckeditor.net/
 * 
 * "Support Open Source software. What about a donation today?"
 * 
 * File Name: fckregexlib.js
 * 	These are some Regular Expresions used by the editor.
 * 
 * File Authors:
 * 		Frederico Caldeira Knabben (fredck@fckeditor.net)
 */

var FCKRegexLib = {

// This is the Regular expression used by the SetHTML method for the "&apos;" entity.
AposEntity		: /&apos;/gi ,

// Used by the Styles combo to identify styles that can't be applied to text.
ObjectElements	: /^(?:IMG|TABLE|TR|TD|TH|INPUT|SELECT|TEXTAREA|HR|OBJECT|A|UL|OL|LI)$/i ,

BlockElements	: /^(?:P|DIV|H1|H2|H3|H4|H5|H6|ADDRESS|PRE|OL|UL|LI|TD|TH)$/i ,

// Elements marked as empty "Empty" in the XHTML DTD.
EmptyElements	: /^(?:BASE|META|LINK|HR|BR|PARAM|IMG|AREA|INPUT)$/i ,

// List all named commands (commands that can be interpreted by the browser "execCommand" method.
NamedCommands	: /^(?:Cut|Copy|Paste|Print|SelectAll|RemoveFormat|Unlink|Undo|Redo|Bold|Italic|Underline|StrikeThrough|Subscript|Superscript|JustifyLeft|JustifyCenter|JustifyRight|JustifyFull|Outdent|Indent|InsertOrderedList|InsertUnorderedList|InsertHorizontalRule)$/i ,

BodyContents	: /([\s\S]*\<body[^\>]*\>)([\s\S]*)(\<\/body\>[\s\S]*)/i ,

// Temporary text used to solve some browser specific limitations.
ToReplace		: /___fcktoreplace:([\w]+)/ig ,

// Get the META http-equiv attribute from the tag.
MetaHttpEquiv	: /http-equiv\s*=\s*["']?([^"' ]+)/i ,

HasBaseTag		: /<base /i ,

HtmlOpener		: /<html\s?[^>]*>/i ,
HeadOpener		: /<head\s?[^>]*>/i ,
HeadCloser		: /<\/head\s*>/i ,

TableBorderClass : /\s*FCK__ShowTableBorders\s*/ ,

// Validate element names.
ElementName		: /(^[A-Za-z_:][\w.\-:]*\w$)|(^[A-Za-z_]$)/ ,

// Used in conjuction with the FCKConfig.ForceSimpleAmpersand configuration option.
ForceSimpleAmpersand : /___FCKAmp___/g ,

// Get the closing parts of the tags with no closing tags, like <br/>... gets the "/>" part.
SpaceNoClose	: /\/>/g ,

EmptyParagraph	: /^<(p|div)>\s*<\/\1>$/i ,

TagBody			: /></ ,

StrongOpener	: /<STRONG([ \>])/gi ,
StrongCloser	: /<\/STRONG>/gi ,
EmOpener		: /<EM([ \>])/gi ,
EmCloser		: /<\/EM>/gi ,
AbbrOpener		: /<ABBR([ \>])/gi ,
AbbrCloser		: /<\/ABBR>/gi ,

GeckoEntitiesMarker : /#\?-\:/g ,

// We look for the "src" and href attribute with the " or ' or whithout one of
// them. We have to do all in one, otherwhise we will have problems with URLs
// like "thumbnail.php?src=someimage.jpg" (SF-BUG 1554141).
ProtectUrlsImg	: /(?:(<img(?=\s).*?\ssrc=)("|')(.*?)\2)|(?:(<img\s.*?src=)([^"'][^ >]+))/gi ,
ProtectUrlsA	: /(?:(<a(?=\s).*?\shref=)("|')(.*?)\2)|(?:(<a\s.*?href=)([^"'][^ >]+))/gi ,

Html4DocType	: /HTML 4\.0 Transitional/i ,
DocTypeTag		: /<!DOCTYPE[^>]*>/i ,

// These regex are used to save the original event attributes in the HTML.
TagsWithEvent	: /<[^\>]+ on\w+[\s\r\n]*=[\s\r\n]*?('|")[\s\S]+?\>/g ,
EventAttributes	: /\s(on\w+)[\s\r\n]*=[\s\r\n]*?('|")([\s\S]*?)\2/g,
ProtectedEvents : /\s\w+_fckprotectedatt="([^"]+)"/g
}