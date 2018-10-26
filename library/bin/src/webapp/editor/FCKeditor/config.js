/*
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
 * Editor configuration settings.
 *
 * Follow this link for more information:
 * http://wiki.fckeditor.net/Developer%27s_Guide/Configuration/Configurations_Settings
 */

FCKConfig.CustomConfigurationsPath = '' ;

FCKConfig.EditorAreaCSS = FCKConfig.BasePath + 'css/fck_editorarea.css' ;
FCKConfig.EditorAreaStyles = '' ;
FCKConfig.ToolbarComboPreviewCSS = '' ;

FCKConfig.DocType = '' ;

FCKConfig.BaseHref = '' ;

FCKConfig.FullPage = false ;

// The following option determines whether the "Show Blocks" feature is enabled or not at startup.
FCKConfig.StartupShowBlocks = false ;

FCKConfig.Debug = false ;
FCKConfig.AllowQueryStringDebug = true ;

FCKConfig.SkinPath = FCKConfig.BasePath + 'skins/default/' ;
FCKConfig.PreloadImages = [ FCKConfig.SkinPath + 'images/toolbar.start.gif', FCKConfig.SkinPath + 'images/toolbar.buttonarrow.gif' ] ;

FCKConfig.PluginsPath = FCKConfig.BasePath + 'plugins/' ;

FCKConfig.Plugins.Add( 'attachments' ) ;
FCKConfig.Plugins.Add( 'movieplayer', 'en,pt' ) ;

// FCKConfig.Plugins.Add( 'autogrow' ) ;
// FCKConfig.Plugins.Add( 'dragresizetable' );
FCKConfig.AutoGrowMax = 400 ;

// FCKConfig.ProtectedSource.Add( /<%[\s\S]*?%>/g ) ;	// ASP style server side code <%...%>
// FCKConfig.ProtectedSource.Add( /<\?[\s\S]*?\?>/g ) ;	// PHP style server side code
// FCKConfig.ProtectedSource.Add( /(<asp:[^\>]+>[\s|\S]*?<\/asp:[^\>]+>)|(<asp:[^\>]+\/>)/gi ) ;	// ASP.Net style tags <asp:control>

FCKConfig.AutoDetectLanguage	= true ;
FCKConfig.DefaultLanguage		= 'en' ;
FCKConfig.ContentLangDirection	= 'ltr' ;

FCKConfig.ProcessHTMLEntities	= true ;
FCKConfig.IncludeLatinEntities	= true ;
FCKConfig.IncludeGreekEntities	= true ;

FCKConfig.ProcessNumericEntities = false ;

FCKConfig.AdditionalNumericEntities = ''  ;		// Single Quote: "'"

FCKConfig.FillEmptyBlocks	= true ;

FCKConfig.FormatSource		= true ;
FCKConfig.FormatOutput		= true ;
FCKConfig.FormatIndentator	= '    ' ;

FCKConfig.StartupFocus	= false ;
FCKConfig.ForcePasteAsPlainText	= false ;
FCKConfig.AutoDetectPasteFromWord = true ;	// IE only.
FCKConfig.ShowDropDialog = true ;
FCKConfig.ForceSimpleAmpersand	= false ;
// SAK-1735: By setting TabSpaces=0 in the config.js files used for the FCKeditor, 
// then the FCKeditor is no longer a keyboard trap. The user can tab in and tab out of it.
FCKConfig.TabSpaces		= 5 ;
FCKConfig.ShowBorders	= true ;
FCKConfig.SourcePopup	= false ;
FCKConfig.ToolbarStartExpanded	= true ;
FCKConfig.ToolbarCanCollapse	= true ;
FCKConfig.IgnoreEmptyParagraphValue = true ;
FCKConfig.PreserveSessionOnFileBrowser = false ;
FCKConfig.FloatingPanelsZIndex = 10000 ;
FCKConfig.HtmlEncodeOutput = false ;

FCKConfig.TemplateReplaceAll = true ;
FCKConfig.TemplateReplaceCheckbox = true ;

FCKConfig.ToolbarLocation = 'In' ;

FCKConfig.ToolbarSets["Default"] = [
        ['Source','DocProps','Templates'],
        ['Cut','Copy','Paste','PasteText','PasteWord','SelectAll','RemoveFormat','SpellCheck'],
        ['Undo','Redo','-','Find','Replace'],
        ['Bold','Italic','Underline','StrikeThrough','Subscript','Superscript'],
        ['OrderedList','UnorderedList','Outdent','Indent'],
        ['JustifyLeft','JustifyCenter','JustifyRight','JustifyFull','TextColor','BGColor'],
        ['Link','Unlink','Anchor'],
        ['Image','Movie','Flash','Table','Rule','Smiley','SpecialChar'],['Style'],
        ['FontFormat','FontName','FontSize'],
        ['About']
] ;

FCKConfig.ToolbarSets["large"] = [
        ['Source','DocProps','Templates'],
        ['Cut','Copy','Paste','PasteText','PasteWord','SelectAll','RemoveFormat','SpellCheck'],
        ['Undo','Redo','-','Find','Replace'],
        ['Bold','Italic','Underline','StrikeThrough','Subscript','Superscript'],
        ['OrderedList','UnorderedList','Outdent','Indent'],
        ['JustifyLeft','JustifyCenter','JustifyRight','JustifyFull','TextColor','BGColor'],
        ['Link','Unlink','Anchor'],
        ['Image','Movie','Flash','Table','Rule','Smiley','SpecialChar'],['Style'],
        ['FontFormat','FontName','FontSize'],
] ;

FCKConfig.ToolbarSets["largecompressed"] = [
        ['Source','DocProps','Templates'],
        ['Cut','Copy','Paste','PasteText','PasteWord','SelectAll','RemoveFormat','SpellCheck','Undo','Redo'],
        ['Bold','Italic','Underline','StrikeThrough','Subscript','Superscript'],
        ['OrderedList','UnorderedList','Outdent','Indent','JustifyLeft','JustifyCenter','JustifyRight','JustifyFull'],
        ['TextColor','BGColor','Link','Unlink','Anchor'],
        ['Image','Movie','Flash','Table','Rule','Smiley','SpecialChar'],
        ['FontFormat','FontName'],['Style','FontSize'],
] ;

FCKConfig.ToolbarSets["medium"] = [
        ['Source'],
	      ['Cut','Copy','Paste','PasteText','PasteWord'],
        ['Undo','Redo','-','Find','Replace'],
        ['Bold','Italic','Underline','TextColor','BGColor','OrderedList','UnorderedList','Outdent','Indent','JustifyLeft','JustifyCenter','JustifyRight','JustifyFull'],
        ['Image','Table','Link','Unlink','-','FontFormat','Style']        
] ;

FCKConfig.ToolbarSets["small"] = [
        ['Bold','Italic','TextColor','BGColor','-','Outdent','Indent','JustifyLeft','JustifyCenter','JustifyRight','-','OrderedList','UnorderedList','-','Link','Unlink']
] ;


FCKConfig.ToolbarSets["Attachments"] = [
   ['Source','DocProps','Templates'],
   ['Cut','Copy','Paste','PasteText','PasteWord','SelectAll','RemoveFormat','SpellCheck'],
   ['Undo','Redo','-','Find','Replace'],
   ['Bold','Italic','Underline','StrikeThrough','Subscript','Superscript'],
   ['OrderedList','UnorderedList','Outdent','Indent'],
   ['JustifyLeft','JustifyCenter','JustifyRight','JustifyFull','TextColor','BGColor'],
   ['Link','Unlink','Anchor'],
   ['Image','Movie','Flash','Table','Rule','Smiley','SpecialChar'],['Style'],
   ['FontFormat','FontName','FontSize'],
   ['About'],
   '/',
   ['SelectAttachmentsCombo','SelectAttachmentsButton']
] ;

FCKConfig.ToolbarSets["Basic"] = [
        ['Bold','Italic','-','OrderedList','UnorderedList','-','Link','Unlink','-','About']
] ;

FCKConfig.EnterMode = 'br' ;			// p | div | br
FCKConfig.ShiftEnterMode = 'p' ;	// p | div | br

FCKConfig.Keystrokes = [
	[ CTRL + 65 /*A*/, true ],
	[ CTRL + 67 /*C*/, true ],
	[ CTRL + 70 /*F*/, true ],
	[ CTRL + 83 /*S*/, true ],
	[ CTRL + 88 /*X*/, true ],
	[ CTRL + 86 /*V*/, 'Paste' ],
	[ SHIFT + 45 /*INS*/, 'Paste' ],
	[ CTRL + 88 /*X*/, 'Cut' ],
	[ SHIFT + 46 /*DEL*/, 'Cut' ],
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

FCKConfig.ContextMenu = ['Generic','Link','Anchor','Image','Movie','Flash','Select','Textarea','Checkbox','Radio','TextField','HiddenField','ImageButton','Button','BulletedList','NumberedList','Table','Form'] ;
FCKConfig.BrowserContextMenuOnCtrl = false ;

FCKConfig.EnableMoreFontColors = true ;
FCKConfig.FontColors = '000000,993300,333300,003300,003366,000080,333399,333333,800000,FF6600,808000,808080,008080,0000FF,666699,808080,FF0000,FF9900,99CC00,339966,33CCCC,3366FF,800080,999999,FF00FF,FFCC00,FFFF00,00FF00,00FFFF,00CCFF,993366,C0C0C0,FF99CC,FFCC99,FFFF99,CCFFCC,CCFFFF,99CCFF,CC99FF,FFFFFF' ;

FCKConfig.FontFormats	= 'p;h1;h2;h3;h4;h5;h6;pre;address;div' ;
FCKConfig.FontNames		= 'Arial;Comic Sans MS;Courier New;Tahoma;Times New Roman;Verdana' ;
FCKConfig.FontSizes		= 'smaller;larger;xx-small;x-small;small;medium;large;x-large;xx-large' ;

FCKConfig.StylesXmlPath		= FCKConfig.EditorPath + 'fckstyles.xml' ;
FCKConfig.TemplatesXmlPath	= FCKConfig.EditorPath + 'fcktemplates.xml' ;

FCKConfig.SpellChecker		= 'ieSpell' ;	// 'ieSpell' | 'SpellerPages'
FCKConfig.IeSpellDownloadUrl	= 'http://www.iespell.com/download.php' ;
FCKConfig.SpellerPagesServerScript = 'server-scripts/spellchecker.php' ;	// Available extension: .php .cfm .pl
FCKConfig.FirefoxSpellChecker	= false ;

FCKConfig.MaxUndoLevels = 15 ;

FCKConfig.DisableObjectResizing = false ;
FCKConfig.DisableFFTableHandles = true ;

FCKConfig.LinkDlgHideTarget	= false ;
FCKConfig.LinkDlgHideAdvanced	= false ;

FCKConfig.ImageDlgHideLink	= true ;
FCKConfig.ImageDlgHideAdvanced	= false ;

FCKConfig.FlashDlgHideAdvanced	= false ;

FCKConfig.ProtectedTags = '' ;

// This will be applied to the body element of the editor
FCKConfig.BodyId = '' ;
FCKConfig.BodyClass = '' ;

FCKConfig.DefaultStyleLabel = '' ;
FCKConfig.DefaultFontFormatLabel = '' ;
FCKConfig.DefaultFontLabel = '' ;
FCKConfig.DefaultFontSizeLabel = '' ;

FCKConfig.DefaultLinkTarget = '' ;

// The option switches between trying to keep the html structure or do the changes so the content looks like it was in Word
FCKConfig.CleanWordKeepsStructure = false ;

// Only inline elements are valid.
FCKConfig.RemoveFormatTags = 'b,big,code,del,dfn,em,font,i,ins,kbd,q,samp,small,span,strike,strong,sub,sup,tt,u,var' ;

FCKConfig.CustomStyles = 
{
	'Red Title'	: { Element : 'h3', Styles : { 'color' : 'Red' } }
};

// Do not add, rename or remove styles here. Only apply definition changes.
FCKConfig.CoreStyles = 
{
	// Basic Inline Styles.
	'Bold'			: { Element : 'b', Overrides : 'strong' },
	'Italic'		: { Element : 'i', Overrides : 'em' },
	'Underline'		: { Element : 'u' },
	'StrikeThrough'	: { Element : 'strike' },
	'Subscript'		: { Element : 'sub' },
	'Superscript'	: { Element : 'sup' },
	
	// Basic Block Styles (Font Format Combo).
	'p'				: { Element : 'p' },
	'div'			: { Element : 'div' },
	'pre'			: { Element : 'pre' },
	'address'		: { Element : 'address' },
	'h1'			: { Element : 'h1' },
	'h2'			: { Element : 'h2' },
	'h3'			: { Element : 'h3' },
	'h4'			: { Element : 'h4' },
	'h5'			: { Element : 'h5' },
	'h6'			: { Element : 'h6' },
	
	// Other formatting features.
	'FontFace' : 
	{ 
		Element		: 'span', 
		Styles		: { 'font-family' : '#("Font")' }, 
		Overrides	: [ { Element : 'font', Attributes : { 'face' : null } } ]
	},
	
	'Size' :
	{ 
		Element		: 'span', 
		Styles		: { 'font-size' : '#("Size","fontSize")' }, 
		Overrides	: [ { Element : 'font', Attributes : { 'size' : null } } ]
	},
	
	'Color' :
	{ 
		Element		: 'span', 
		Styles		: { 'color' : '#("Color","color")' }, 
		Overrides	: [ { Element : 'font', Attributes : { 'color' : null } } ]
	},
	
	'BackColor'		: { Element : 'span', Styles : { 'background-color' : '#("Color","color")' } },

	'SelectionHighlight' : { Element : 'span', Styles : { 'background-color' : 'navy', 'color' : 'white' } }
};

// The distance of an indentation step.
FCKConfig.IndentLength = 40 ;
FCKConfig.IndentUnit = 'px' ;

// Alternatively, FCKeditor allows the use of CSS classes for block indentation.
// This overrides the IndentLength/IndentUnit settings.
FCKConfig.IndentClasses = [] ;

// [ Left, Center, Right, Justified ]
FCKConfig.JustifyClasses = [] ;

FCKConfig.LinkBrowser = true ;
FCKConfig.LinkBrowserWindowWidth	= FCKConfig.ScreenWidth * 0.7 ;		// 70%
FCKConfig.LinkBrowserWindowHeight	= FCKConfig.ScreenHeight * 0.7 ;	// 70%

FCKConfig.ImageBrowser = true ;
FCKConfig.ImageBrowserWindowWidth  = FCKConfig.ScreenWidth * 0.7 ;	// 70% ;
FCKConfig.ImageBrowserWindowHeight = FCKConfig.ScreenHeight * 0.7 ;	// 70% ;

FCKConfig.FlashBrowser = true ;
FCKConfig.FlashBrowserWindowWidth  = FCKConfig.ScreenWidth * 0.7 ;	//70% ;
FCKConfig.FlashBrowserWindowHeight = FCKConfig.ScreenHeight * 0.7 ;	//70% ;

FCKConfig.LinkUpload = true ;
FCKConfig.LinkUploadAllowedExtensions	= ".(7z|aiff|asf|avi|bmp|csv|doc|fla|flv|gif|gz|gzip|jpeg|jpg|mid|mov|mp3|mp4|mpc|mpeg|mpg|ods|odt|pdf|png|ppt|pxd|qt|ram|rar|rm|rmi|rmvb|rtf|sdc|sitd|swf|sxc|sxw|tar|tgz|tif|tiff|txt|vsd|wav|wma|wmv|xls|xml|zip)$" ;			// empty for all
FCKConfig.LinkUploadDeniedExtensions	= "" ;	// empty for no one

FCKConfig.ImageUpload = true ;
FCKConfig.ImageUploadAllowedExtensions	= ".(jpg|gif|jpeg|png|bmp)$" ;		// empty for all
FCKConfig.ImageUploadDeniedExtensions	= "" ;							// empty for no one

FCKConfig.FlashUpload = true ;
FCKConfig.FlashUploadAllowedExtensions	= ".(swf|flv)$" ;		// empty for all
FCKConfig.FlashUploadDeniedExtensions	= "" ;					// empty for no one

FCKConfig.SmileyPath	= FCKConfig.BasePath + 'images/smiley/msn/' ;
FCKConfig.SmileyImages	= ['regular_smile.gif','sad_smile.gif','wink_smile.gif','teeth_smile.gif','confused_smile.gif','tounge_smile.gif','embaressed_smile.gif','omg_smile.gif','whatchutalkingabout_smile.gif','angry_smile.gif','angel_smile.gif','shades_smile.gif','devil_smile.gif','cry_smile.gif','lightbulb.gif','thumbs_down.gif','thumbs_up.gif','heart.gif','broken_heart.gif','kiss.gif','envelope.gif'] ;
FCKConfig.SmileyColumns = 8 ;
FCKConfig.SmileyWindowWidth		= 320 ;
FCKConfig.SmileyWindowHeight	= 240 ;

