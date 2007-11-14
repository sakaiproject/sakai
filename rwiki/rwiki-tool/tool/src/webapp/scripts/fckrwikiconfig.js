/*
 * FCKeditor - The text editor for internet
 * Copyright (C) 2003-2005 Frederico Caldeira Knabben
 *
 * Licensed under the terms of the GNU Lesser General Public License:
 * 		http://www.opensource.org/licenses/lgpl-license.php
 *
 * For further information visit:
 * 		http://www.fckeditor.net/
 *
 * "Support Open Source software. What about a donation today?"
 *
 * File Name: config.js
 * 	Editor configuration settings.
 * 	See the documentation for more info.
 *
 * File Authors:
 * 		Frederico Caldeira Knabben (fredck@fckeditor.net)
 */

FCKConfig.ProtectedSource.Add( /<script[\s\S]*?\/script>/gi ) ;       // <SCRIPT> tags.

FCKConfig.ProcessNumericEntities = false ;
FCKConfig.AdditionalNumericEntities = ''  ;             // Single Quote: "'"

FCKConfig.TemplateReplaceAll = true ;
FCKConfig.TemplateReplaceCheckbox = true ;

FCKConfig.BaseHref = '' ;

FCKConfig.FullPage = false ;

FCKConfig.Debug = false ;

FCKConfig.SkinPath = FCKConfig.BasePath + 'skins/default/' ;

FCKConfig.PluginsPath = FCKConfig.BasePath + 'plugins/' ;

FCKConfig.Plugins.Add( 'attachments', 'en' ) ;

// FCKConfig.Plugins.Add( 'placeholder', 'en,it' ) ;

FCKConfig.AutoDetectLanguage	= true ;
FCKConfig.DefaultLanguage		= 'en' ;
FCKConfig.ContentLangDirection	= 'ltr' ;

//FCKConfig.ProcessHTMLEntities	= true ;
FCKConfig.ProcessHTMLEntities	= false ; // Won't replace special chars with HTML Entities
FCKConfig.IncludeLatinEntities	= true ;
FCKConfig.IncludeGreekEntities	= true ;

FCKConfig.FillEmptyBlocks	= true ;

FCKConfig.FormatSource		= true ;
FCKConfig.FormatOutput		= true ;
FCKConfig.FormatIndentator	= '' ;

FCKConfig.ToolbarLocation = 'In' ;
FCKConfig.EnterMode = 'p' ;                     // p | div | br
FCKConfig.ShiftEnterMode = 'br' ;       // p | div | br

FCKConfig.Keystrokes = [
        [ CTRL + 65 /*A*/, true ],
        [ CTRL + 67 /*C*/, true ],
        [ CTRL + 70 /*F*/, true ],
        [ CTRL + 83 /*S*/, true ],
        [ CTRL + 88 /*X*/, true ],
        [ CTRL + 86 /*V*/, 'Paste' ],
        [ SHIFT + 45 /*INS*/, 'Paste' ],
        [ CTRL + 90 /*Z*/, 'Undo' ],
        [ CTRL + 89 /*Y*/, 'Redo' ],
        [ CTRL + SHIFT + 90 /*Z*/, 'Redo' ],
        [ CTRL + 76 /*L*/, 'Link' ],
        [ CTRL + 66 /*B*/, 'Bold' ],
        [ CTRL + 73 /*I*/, 'Italic' ],
        [ CTRL + 85 /*U*/, 'Underline' ],
        [ CTRL + SHIFT + 83 /*S*/, 'Save' ],
        [ CTRL + ALT + 13 /*ENTER*/, 'FitWindow' ],
        [ CTRL + 9 /*TAB*/, 'Source' ]
] ;


FCKConfig.ToolbarSets["Default"] = [
        ['Undo','Redo','-','Find','Replace'],
        ['Bold','Italic','StrikeThrough','Subscript','Superscript'],
        ['OrderedList','UnorderedList'],
        ['Link','Unlink'],
        ['Image','Table','FontFormat','SpecialChar'],
        ['About']
] ;

FCKConfig.ContextMenu = ['Generic','Link','Image','ImageButton','BulletedList','NumberedList','TableCell','Table'] ;

FCKConfig.FontFormats	= 'h1;h3;h6' ;

FCKConfig.SpellChecker			= 'ieSpell' ;	// 'ieSpell' | 'SpellerPages'
FCKConfig.IeSpellDownloadUrl	= 'http://www.iespell.com/download.php' ;

FCKConfig.MaxUndoLevels = 15 ;

/*FCKConfig.DisableImageHandles = true ;
FCKConfig.DisableTableHandles = true ;

FCKConfig.LinkDlgHideTarget	= false ;
FCKConfig.LinkDlgHideAdvanced	= false ;

FCKConfig.ImageDlgHideLink	= true ;
FCKConfig.ImageDlgHideAdvanced	= false ;

FCKConfig.FlashDlgHideAdvanced	= false ;

FCKConfig.LinkBrowser = true ;
//FCKConfig.LinkBrowser = false ;
FCKConfig.ImageBrowser = true ;
//FCKConfig.ImageBrowser = false ;

FCKConfig.FlashBrowser = true ;

FCKConfig.LinkUpload = false ;

FCKConfig.ImageUpload = false ;

FCKConfig.FlashUpload = false ;*/

FCKConfig.DisableObjectResizing = false ;
FCKConfig.DisableFFTableHandles = true ;

FCKConfig.LinkDlgHideTarget		= false ;
FCKConfig.LinkDlgHideAdvanced	= false ;

FCKConfig.ImageDlgHideLink		= false ;
FCKConfig.ImageDlgHideAdvanced	= false ;

FCKConfig.FlashDlgHideAdvanced	= false ;

FCKConfig.ProtectedTags = '' ;

// This will be applied to the body element of the editor
FCKConfig.BodyId = '' ;
FCKConfig.BodyClass = '' ;

// The option switches between trying to keep the html structure or do the changes so the content looks like it was in Word
FCKConfig.CleanWordKeepsStructure = false ;

// The following value defines which File Browser connector and Quick Upload
// "uploader" to use. It is valid for the default implementaion and it is here
// just to make this configuration file cleaner.
// It is not possible to change this value using an external file or even
// inline when creating the editor instance. In that cases you must set the
// values of LinkBrowserURL, ImageBrowserURL and so on.
// Custom implementations should just ignore it.
var _FileBrowserLanguage	= 'asp' ;	// asp | aspx | cfm | lasso | perl | php | py
var _QuickUploadLanguage	= 'asp' ;	// asp | aspx | cfm | lasso | php


// Don't care about the following line. It just calculates the correct connector
// extension to use for the default File Browser (Perl uses "cgi").
var _FileBrowserExtension = _FileBrowserLanguage == 'perl' ? 'cgi' : _FileBrowserLanguage ;

FCKConfig.LinkBrowser = true ;
FCKConfig.LinkBrowserURL = FCKConfig.BasePath + 'filemanager/browser/default/browser.html?Connector=connectors/' + _FileBrowserLanguage + '/connector.' + _FileBrowserExtension ;
FCKConfig.LinkBrowserWindowWidth	= FCKConfig.ScreenWidth * 0.7 ;		// 70%
FCKConfig.LinkBrowserWindowHeight	= FCKConfig.ScreenHeight * 0.7 ;	// 70%

FCKConfig.ImageBrowser = true ;
FCKConfig.ImageBrowserURL = FCKConfig.BasePath + 'filemanager/browser/default/browser.html?Type=Image&Connector=connectors/' + _FileBrowserLanguage + '/connector.' + _FileBrowserExtension ;
FCKConfig.ImageBrowserWindowWidth  = FCKConfig.ScreenWidth * 0.7 ;	// 70% ;
FCKConfig.ImageBrowserWindowHeight = FCKConfig.ScreenHeight * 0.7 ;	// 70% ;

FCKConfig.LinkUpload = true ;
FCKConfig.LinkUploadURL = FCKConfig.BasePath + 'filemanager/upload/' + _QuickUploadLanguage + '/upload.' + _QuickUploadLanguage ;
FCKConfig.LinkUploadAllowedExtensions	= "" ;			// empty for all
FCKConfig.LinkUploadDeniedExtensions	= ".(html|htm|php|php2|php3|php4|php5|phtml|pwml|inc|asp|aspx|ascx|jsp|cfm|cfc|pl|bat|exe|com|dll|vbs|js|reg|cgi|htaccess|asis)$" ;	// empty for no one

FCKConfig.ImageUpload = true ;
FCKConfig.ImageUploadURL = FCKConfig.BasePath + 'filemanager/upload/' + _QuickUploadLanguage + '/upload.' + _QuickUploadLanguage + '?Type=Image' ;
FCKConfig.ImageUploadAllowedExtensions	= ".(jpg|gif|jpeg|png|bmp)$" ;		// empty for all
FCKConfig.ImageUploadDeniedExtensions	= "" ;							// empty for no one

FCKConfig.FlashUpload = true ;
FCKConfig.FlashUploadURL = FCKConfig.BasePath + 'filemanager/upload/' + _QuickUploadLanguage + '/upload.' + _QuickUploadLanguage + '?Type=Flash' ;
FCKConfig.FlashUploadAllowedExtensions	= ".(swf|fla)$" ;		// empty for all
FCKConfig.FlashUploadDeniedExtensions	= "" ;					// empty for no one

