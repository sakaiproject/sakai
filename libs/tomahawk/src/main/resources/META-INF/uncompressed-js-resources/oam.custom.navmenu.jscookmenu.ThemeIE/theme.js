
var cmThemeIEBase = 'jscookmenu/ThemeIE/';

if(myThemeIEBase)
    cmThemeIEBase = myThemeIEBase;

var myPrefix = cmThemeIEBase;
var mySuffix="";

// sub menu display attributes
if(cmThemeIEBase.indexOf("/;j")>-1){
	myPrefix=cmThemeIEBase.substring(0,cmThemeIEBase.indexOf("/;j")) + "/";
	mySuffix=cmThemeIEBase.substring(cmThemeIEBase.indexOf("/;j")+3);
}

var myFolderLeft=myPrefix + 'folder.gif' + mySuffix;
var myFolderRight=myPrefix + 'arrow.gif' + mySuffix;
var myItemLeft=myPrefix + 'link.gif' + mySuffix;


var cmThemeIE =
{
  	// main menu display attributes
  	//
  	// Note.  When the menu bar is horizontal,
  	// mainFolderLeft and mainFolderRight are
  	// put in <span></span>.  When the menu
  	// bar is vertical, they would be put in
  	// a separate TD cell.

  	// HTML code to the left of the folder item
  	mainFolderLeft: '',
  	// HTML code to the right of the folder item
  	mainFolderRight: '',
	// HTML code to the left of the regular item
	mainItemLeft: '',
	// HTML code to the right of the regular item
	mainItemRight: '',

	// sub menu display attributes

	// HTML code to the left of the folder item
	folderLeft: '<img alt="" src="' + myFolderLeft + '">',
	// HTML code to the right of the folder item
	folderRight: '<img alt="" src="' + myFolderRight + '">',
	// HTML code to the left of the regular item
	itemLeft: '<img alt="" src="' + myItemLeft + '">',
	// HTML code to the right of the regular item
	itemRight: '',
	// cell spacing for main menu
	mainSpacing: 0,
	// cell spacing for sub menus
	subSpacing: 0,
	// auto dispear time for submenus in milli-seconds
	delay: 100
};

// horizontal split, used only in sub menus
var cmThemeIEHSplit = [_cmNoAction, '<td colspan="3" style="height: 3px; overflow: hidden"><div class="ThemeIEMenuSplit"></div></td>'];
// vertical split, used only in main menu
var cmThemeIEMainVSplit = [_cmNoAction, '<div class="ThemeIEMenuVSplit"></div>'];
// horizontal split, used only in main menu
var cmThemeIEMainHSplit = [_cmNoAction, '<td colspan="3"><div class="ThemeIEMenuSplit"></div></td>'];
