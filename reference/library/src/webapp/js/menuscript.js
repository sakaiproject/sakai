/**********************************************************************************
*
* $Header: /cvs/sakai2/reference/library/src/webapp/js/menuscript.js,v 1.1 2005/04/12 17:44:21 ggolden.umich.edu Exp $
*
***********************************************************************************
@license@
**********************************************************************************/

function showMainMenu(menu, bar)
{
	m_menuString = new String('' + menu);
	var menuItem;
	var barItem;
	var parentLeft;
	var parentTop;
	var parentWidth;
	var parentHeight;

	menuItem = document.getElementById(menu);
	barItem = document.getElementById(bar);

	parentLeft = barItem.offsetLeft;
	parentTop = barItem.offsetTop;
	parentWidth = barItem.offsetWidth;
	parentHeight = barItem.offsetHeight;

	if (menuItem.style.display != '')
	{
		menuItem.style.pixelLeft = parentLeft;
		menuItem.style.pixelTop = parentTop + parentHeight;
		menuItem.style.display = '';
		m_mainMenuActivated = true;
	}

}//showMainMenu


function processMenuEntryMouseOut(menu, bar)
{
	var overEle = document.elementFromPoint(window.event.clientX, window.event.clientY);
	var name = overEle.className;

	if((name != 'menu-bar') && (name != 'menu-bar-last') && ((name != 'menu-hidden') && (name != 'menu-entry') && (name != 'menu-image')))
	{
		hideAllMenus();
	}
}

function processMenuBarMouseOut(menu, bar)
{
	var overEle = document.elementFromPoint(window.event.clientX, window.event.clientY);
	var name = overEle.className;
	
	if((name != 'menu-hidden') && (name != 'menu-entry') && (name != 'menu-image') && (name != 'menu-divider'))
	{
		if((name == 'menu-bar') || (name == 'menu-bar-last'))
			hideMenu(menu, bar);
		else
			hideAllMenus();
	}
}

function processMenuBarMouseOver(menu, bar)
{
	if(m_mainMenuActivated)
	{
		showMainMenu(menu, bar);
	}
}

function processMenuBarClick(menu, bar)
{
	var menuItem = document.getElementById(menu);
	var barItem = document.getElementById(bar);

	if(menuItem.style.display == '')
	{
		menuItem.style.display = 'none';
		m_mainMenuActivated = false;
	}
	else
	{
		m_menuString = new String(menu);
		m_parentString = new String(bar);
		showMainMenu(m_menuString, m_parentString);
	}

}//processMenuBarClick

/**********************************************************************************
*
* $Header: /cvs/sakai2/reference/library/src/webapp/js/menuscript.js,v 1.1 2005/04/12 17:44:21 ggolden.umich.edu Exp $
*
**********************************************************************************/
