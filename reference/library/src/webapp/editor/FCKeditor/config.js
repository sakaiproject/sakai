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

FCKConfig.EditorAreaCSS = FCKConfig.BasePath + 'css/fck_editorarea.css' ;

FCKConfig.DocType = '<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">' ;

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

FCKConfig.EnableXHTML		= true ;	// Unsupported: Do not change.
FCKConfig.EnableSourceXHTML	= true ;	// Unsupported: Do not change.

FCKConfig.ProcessHTMLEntities	= true ;
FCKConfig.IncludeLatinEntities	= true ;
FCKConfig.IncludeGreekEntities	= true ;

FCKConfig.FillEmptyBlocks	= true ;

FCKConfig.FormatSource		= true ;
FCKConfig.FormatOutput		= true ;
FCKConfig.FormatIndentator	= '    ' ;

FCKConfig.ToolbarSets["Default"] = [
        ['Source','DocProps','Templates'],
        ['Cut','Copy','Paste','PasteText','PasteWord','SelectAll','RemoveFormat','SpellCheck'],
        ['Undo','Redo','-','Find','Replace'],
        ['Bold','Italic','Underline','StrikeThrough','Subscript','Superscript'],
        ['OrderedList','UnorderedList','Outdent','Indent'],
        ['JustifyLeft','JustifyCenter','JustifyRight','JustifyFull','TextColor','BGColor'],
        ['Link','Unlink','Anchor'],
        ['Image','Flash','Table','Rule','Smiley','SpecialChar','UniversalKey'],['Style'],
        ['FontFormat','FontName','FontSize'],
        ['About']
] ;

FCKConfig.ToolbarSets["Attachments"] = [
   ['Source','DocProps','Templates'],
   ['Cut','Copy','Paste','PasteText','PasteWord','SelectAll','RemoveFormat','SpellCheck'],
   ['Undo','Redo','-','Find','Replace'],
   ['Bold','Italic','Underline','StrikeThrough','Subscript','Superscript'],
   ['OrderedList','UnorderedList','Outdent','Indent'],
   ['JustifyLeft','JustifyCenter','JustifyRight','JustifyFull','TextColor','BGColor'],
   ['Link','Unlink','Anchor'],
   ['Image','Flash','Table','Rule','Smiley','SpecialChar','UniversalKey'],['Style'],
   ['FontFormat','FontName','FontSize'],
   ['About'],
   '/',
   ['SelectAttachmentsCombo','SelectAttachmentsButton']
] ;

FCKConfig.ToolbarSets["Basic"] = [
	['Bold','Italic','-','OrderedList','UnorderedList','-','Link','Unlink','-','About']
] ;

FCKConfig.ContextMenu = ['Generic','Link','Anchor','Image','Flash','Select','Textarea','Checkbox','Radio','TextField','HiddenField','ImageButton','Button','BulletedList','NumberedList','TableCell','Table','Form'] ;

FCKConfig.FontColors = '000000,993300,333300,003300,003366,000080,333399,333333,800000,FF6600,808000,808080,008080,0000FF,666699,808080,FF0000,FF9900,99CC00,339966,33CCCC,3366FF,800080,999999,FF00FF,FFCC00,FFFF00,00FF00,00FFFF,00CCFF,993366,C0C0C0,FF99CC,FFCC99,FFFF99,CCFFCC,CCFFFF,99CCFF,CC99FF,FFFFFF' ;

FCKConfig.FontNames		= 'Arial;Comic Sans MS;Courier New;Tahoma;Times New Roman;Verdana' ;
FCKConfig.FontSizes		= '1/xx-small;2/x-small;3/small;4/medium;5/large;6/x-large;7/xx-large' ;
FCKConfig.FontFormats	= 'p;div;pre;address;h1;h2;h3;h4;h5;h6' ;

FCKConfig.SpellChecker			= 'ieSpell' ;	// 'ieSpell' | 'SpellerPages'
FCKConfig.IeSpellDownloadUrl	= 'http://www.iespell.com/rel/ieSpellSetup211325.exe' ;

FCKConfig.MaxUndoLevels = 15 ;

FCKConfig.DisableImageHandles = true ;
FCKConfig.DisableTableHandles = true ;

FCKConfig.LinkDlgHideTarget	= false ;
FCKConfig.LinkDlgHideAdvanced	= false ;

FCKConfig.ImageDlgHideLink	= true ;
FCKConfig.ImageDlgHideAdvanced	= false ;

FCKConfig.FlashDlgHideAdvanced	= false ;

FCKConfig.LinkBrowser = true ;

FCKConfig.ImageBrowser = true ;

FCKConfig.FlashBrowser = true ;

FCKConfig.LinkUpload = false ;

FCKConfig.ImageUpload = false ;

FCKConfig.FlashUpload = false ;

if( window.console ) window.console.log( 'Config is loaded!' ) ;	// @Packager.Compactor.RemoveLine
