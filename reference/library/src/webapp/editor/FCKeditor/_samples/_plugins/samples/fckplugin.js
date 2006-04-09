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
 * File Name: fckplugin.js
 * 	This is a sample plugin definition file.
 * 
 * File Authors:
 * 		Frederico Caldeira Knabben (fredck@fckeditor.net)
 */

// Here we define our custom Style combo, with a custom widths.
var oMyBigStyleCombo = new FCKToolbarStyleCombo() ;
oMyBigStyleCombo.FieldWidth = 250 ;
oMyBigStyleCombo.PanelWidth = 300 ;
FCKToolbarItems.RegisterItem( 'My_BigStyle', oMyBigStyleCombo ) ;