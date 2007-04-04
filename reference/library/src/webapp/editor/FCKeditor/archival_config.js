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

// FCKConfig.Plugins.Add( 'placeholder', 'en,it' ) ;

FCKConfig.AutoDetectLanguage	= true ;
FCKConfig.DefaultLanguage		= 'en' ;
FCKConfig.ContentLangDirection	= 'ltr' ;

FCKConfig.ProcessHTMLEntities	= true ;
FCKConfig.IncludeLatinEntities	= true ;
FCKConfig.IncludeGreekEntities	= true ;

FCKConfig.FillEmptyBlocks	= true ;

FCKConfig.FormatSource		= true ;
FCKConfig.FormatOutput		= true ;
FCKConfig.FormatIndentator	= '    ' ;

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
        ['Source','DocProps','Templates'],
        ['Cut','Copy','Paste','PasteText','PasteWord','SelectAll','RemoveFormat','SpellCheck'],
        ['Undo','Redo','-','Find','Replace'],
        ['Bold','Italic','Underline','StrikeThrough','Subscript','Superscript'],
        ['OrderedList','UnorderedList','Outdent','Indent'],
        ['JustifyLeft','JustifyCenter','JustifyRight','JustifyFull'],
        ['TextColor','BGColor'],
        ['Table','Rule','Smiley','SpecialChar'],['Style'],
        ['FontFormat','FontName','FontSize'],
        ['About']
] ;

FCKConfig.ToolbarSets["Basic"] = [
	['Bold','Italic','-','OrderedList','UnorderedList','-','About']
] ;

FCKConfig.ContextMenu = ['Generic','Link','Anchor','Select','Textarea','Checkbox','Radio','TextField','HiddenField','ImageButton','Button','BulletedList','NumberedList','TableCell','Table','Form'] ;

FCKConfig.FontColors = '000000,993300,333300,003300,003366,000080,333399,333333,800000,FF6600,808000,808080,008080,0000FF,666699,808080,FF0000,FF9900,99CC00,339966,33CCCC,3366FF,800080,999999,FF00FF,FFCC00,FFFF00,00FF00,00FFFF,00CCFF,993366,C0C0C0,FF99CC,FFCC99,FFFF99,CCFFCC,CCFFFF,99CCFF,CC99FF,FFFFFF' ;

FCKConfig.FontNames		= 'Arial;Comic Sans MS;Courier New;Tahoma;Times New Roman;Verdana' ;
FCKConfig.FontSizes		= '1/xx-small;2/x-small;3/small;4/medium;5/large;6/x-large;7/xx-large' ;
FCKConfig.FontFormats	= 'p;div;pre;address;h1;h2;h3;h4;h5;h6' ;

FCKConfig.SpellChecker			= 'ieSpell' ;	// 'ieSpell' | 'SpellerPages'
FCKConfig.IeSpellDownloadUrl	= 'http://www.iespell.com/download.php' ;

FCKConfig.MaxUndoLevels = 15 ;

FCKConfig.DisableImageHandles = true ;
FCKConfig.DisableTableHandles = true ;

FCKConfig.LinkDlgHideTarget		= false ;
FCKConfig.LinkDlgHideAdvanced	= false ;

FCKConfig.ImageDlgHideLink		= false ;
FCKConfig.ImageDlgHideAdvanced	= false ;

FCKConfig.FlashDlgHideAdvanced	= false ;

FCKConfig.LinkBrowser = false ;

FCKConfig.ImageBrowser = false ;

FCKConfig.FlashBrowser = false ;

FCKConfig.LinkUpload = false ;

FCKConfig.ImageUpload = false ;

FCKConfig.FlashUpload = false ;

