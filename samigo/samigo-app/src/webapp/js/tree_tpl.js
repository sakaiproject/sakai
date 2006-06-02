// Title: Tigra Tree
// Description: See the demo at url
// URL: http://www.softcomplex.com/products/tigra_menu_tree/
// Version: 1.1 (size optimized)
// Date: 11-12-2002 (mm-dd-yyyy)
// Contact: feedback@softcomplex.com (specify product title in the subject)
// Notes: This script is free. Visit official site for further details.
/*
	Feel free to use your custom icons for the tree. Make sure they are all of the same size.
	User icons collections are welcome, we'll publish them giving all regards.
 */

function settree_tpl(root)
{
   var tree_tpl =
   {
      'target' : '_self', // name of the frame links will be opened in
      // other possible values are: _blank, _parent, _search, _self and _top
      'icon_e' : root + 'images/treeMenu/empty.gif', // empty image
      'icon_l' : root + 'images/treeMenu/line.gif', // vertical line
      'icon_32' : root + 'images/treeMenu/page.gif', // root leaf icon normal
      'icon_36' : root + 'images/treeMenu/page.gif', // root leaf icon selected
      'icon_48' : root + 'images/treeMenu/page.gif', // root icon normal
      'icon_52' : root + 'images/treeMenu/page.gif', // root icon selected
      'icon_56' : root + 'images/treeMenu/page.gif', // root icon opened
      'icon_60' : root + 'images/treeMenu/page.gif', // root icon selected
      'icon_16' : root + 'images/treeMenu/page.gif', // node icon normal
      'icon_20' : root + 'images/treeMenu/page.gif', // node icon selected
      'icon_24' : root + 'images/treeMenu/page.gif', // node icon opened
      'icon_28' : root + 'images/treeMenu/page.gif', // node icon selected opened
      'icon_0' : root + 'images/treeMenu/page.gif', // leaf icon normal
      'icon_4' : root + 'images/treeMenu/page.gif', // leaf icon selected
      'icon_2' : root + 'images/treeMenu/joinbottom.gif', // junction for leaf
      'icon_3' : root + 'images/treeMenu/join.gif', // junction for last leaf
      'icon_18' : root + 'images/treeMenu/plusbottom.gif', // junction for closed node
      'icon_19' : root + 'images/treeMenu/plus.gif', // junctioin for last closed node
      'icon_26' : root + 'images/treeMenu/minusbottom.gif', // junction for opened node
      'icon_27' : root + 'images/treeMenu/minus.gif' // junctioin for last opended node
   };
   return tree_tpl;
};
