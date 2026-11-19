//
// Overrides the original JSCookMenu function to work with MyFaces
// action should be taken for mouse button up at a menu item
//
function cmItemMouseUp (obj, isMain, idSub, menuID, index)
{
	if (_cmItemList[index].isDisabled)
		return;

    var item = _cmItemList[index];

    var link = null, target = '_self';

    if (item.length > 2)
        link = item[2];
    if (item.length > 3 && item[3])
        target = item[3];

    if (link != null)
    {
        // changes by Richard J. Barbalace
        if (link.match(/^\w*:A\]\w*:\/\//) != null ) {
            // Link is a URL
            link = link.replace(/^\w*:A\]/, "");  // Remove JSF ID
            window.open (link, target);
        } else if (link.match(/^\w*:A\]\w*:/) != null ) {
            // Link is a script method
            link = link.replace(/^\w*:A\]\w*:/, "");  // Remove JSF ID
            eval(link);
        } else {
            // Link is a JSF action
            var dummyForm = document.forms[target];
            dummyForm.elements['jscook_action'].value = link;
            if (typeof submitForm == "undefined") {
                //No Trinidad
                dummyForm.submit();
            } else {
                //Trinidad style
                var mysource = dummyForm.name+':jscook_action';
                submitForm(dummyForm.name,1,{source:mysource});    
            }
        }
    }

    var menuInfo = _cmMenuList[menuID];
    var prefix = menuInfo.prefix;
    var thisMenu = cmGetThisMenu (obj, prefix);

    var hasChild = (item.length > 5);
    if (!hasChild)
    {
        if (cmIsDefaultItem (item))
        {
            if (obj.cmIsMain)
                obj.className = prefix + 'MainItem';
            else
                obj.className = prefix + 'MenuItem';
        }
        cmHideMenu (thisMenu, null, menuInfo);
    }
    else
    {
        if (cmIsDefaultItem (item))
        {
            if (obj.cmIsMain)
                obj.className = prefix + 'MainItemHover';
            else
                obj.className = prefix + 'MenuItemHover';
        }
    }
}
