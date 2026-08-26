var cmThemeMiniBlack =
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
	folderLeft: '',
	// HTML code to the right of the folder item
	folderRight: '',
	// HTML code to the left of the regular item
	itemLeft: '',
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
var cmThemeMiniBlackHSplit = [_cmNoAction, '<td colspan="3" style="height: 3px; overflow: hidden"><div class="ThemeMiniBlackMenuSplit"></div></td>'];
// horizontal split, used only in main menu
var cmThemeMiniBlackMainHSplit = [_cmNoAction, '<td colspan="3"><div class="ThemeMiniBlackMenuSplit"></div></td>'];
// vertical split, used only in main menu
var cmThemeMiniBlackMainVSplit = [_cmNoAction, '<div class="ThemeMiniBlackMenuVSplit"></div>'];
