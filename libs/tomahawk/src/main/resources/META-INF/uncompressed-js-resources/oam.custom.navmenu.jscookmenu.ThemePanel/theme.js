
// directory of where all the images are
var cmThemePanelBase = 'jscookmenu/ThemePanel/';

if(myThemePanelBase)
    cmThemePanelBase = myThemePanelBase;

var myPrefix = cmThemePanelBase;
var mySuffix="";

// sub menu display attributes
if(cmThemePanelBase.indexOf("/;j")>-1){
	myPrefix=cmThemePanelBase.substring(0,cmThemePanelBase.indexOf("/;j")) + "/";
	mySuffix=cmThemePanelBase.substring(cmThemePanelBase.indexOf("/;j")+3);
}

var myFolderLeft=myPrefix + 'blank.gif' + mySuffix;
var myFolderRight=myPrefix + 'arrow.gif' + mySuffix;
var myItemLeft=myPrefix + 'blank.gif' + mySuffix;
var myItemRight=myPrefix + 'blank.gif' + mySuffix;	

var cmThemePanel =
{
  	// main menu display attributes
  	//
  	// Note.  When the menu bar is horizontal,
  	// mainFolderLeft and mainFolderRight are
  	// put in <span></span>.  When the menu
  	// bar is vertical, they would be put in
  	// a separate TD cell.

  	// HTML code to the left of the folder item
  	mainFolderLeft: '<img alt="" src="' + myFolderLeft + '">',
  	// HTML code to the right of the folder item
  	mainFolderRight: '<img alt="" src="' + myFolderRight + '">',
	// HTML code to the left of the regular item
	mainItemLeft: '<img alt="" src="' + myItemLeft + '">',
	// HTML code to the right of the regular item
	mainItemRight: '<img alt="" src="' + myItemRight + '">',

	// sub menu display attributes

	// HTML code to the left of the folder item
	folderLeft: '<img alt="" src="' + myFolderLeft + '">',
	// HTML code to the right of the folder item
	folderRight: '<img alt="" src="' + myFolderRight + '">',
	// HTML code to the left of the regular item
	itemLeft: '<img alt="" src="' + myItemLeft + '">',
	// HTML code to the right of the regular item
	itemRight: '<img alt="" src="' + myItemRight + '">',
	// cell spacing for main menu
	mainSpacing: 0,
	// cell spacing for sub menus
	subSpacing: 0,
	// auto dispear time for submenus in milli-seconds
	delay: 500
};

// for sub menu horizontal split
var cmThemePanelHSplit = [_cmNoAction, '<td colspan="3" style="height: 5px; overflow: hidden"><div class="ThemePanelMenuSplit"></div></td>'];
// for vertical main menu horizontal split
var cmThemePanelMainHSplit = [_cmNoAction, '<td colspan="3" style="height: 5px; overflow: hidden"><div class="ThemePanelMenuSplit"></div></td>'];
// for horizontal main menu vertical split
var cmThemePanelMainVSplit = [_cmNoAction, '|'];
