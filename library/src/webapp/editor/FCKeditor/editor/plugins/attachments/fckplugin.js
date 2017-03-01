/*
 * *********************************************************************************
 *  $URL: https://source.sakaiproject.org/svn/content/trunk/content-api/api/src/java/org/sakaiproject/content/api/ContentCollection.java $
 *  $Id: ContentCollection.java 8537 2006-05-01 02:13:28Z jimeng@umich.edu $
 * **********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * *********************************************************************************
 *
 */

var FCKToolbarAttachmentSelectCommand = function ( combo ) {
	this.combo = combo
}

FCKToolbarAttachmentSelectCommand.prototype.Execute = function (itemId, item) {
	this.combo.currentItem = item;
	this.combo.currentItem.itemId = itemId;
}

FCKToolbarAttachmentSelectCommand.prototype.GetState = function()
{
	if(this.combo.currentItem) 
	{
		return this.combo.currentItem.itemId;
	}
	else {
		//nothing selected
		return FCK_TRISTATE_OFF;
	}
}

var FCKToolbarAttachmentsCombo = function( commandName )
{
	this.Label		  = this.GetLabel() ;
	this.Tooltip	  = this.Label ;
	this.Style		  = FCK_TOOLBARITEM_ICONTEXT ;
	this.CommandName = commandName;

	this.NormalLabel = 'Normal' ;

	this.PanelWidth = 260 ;
	this.FieldWidth = 260 ;
}

// Inherit from FCKToolbarSpecialCombo.
FCKToolbarAttachmentsCombo.prototype = new FCKToolbarSpecialCombo ;

FCKToolbarAttachmentsCombo.prototype.GetLabel = function()
{
	return FCKLang['SelectAttchments'];
}

FCKToolbarAttachmentsCombo.prototype.CreateItems = function( targetSpecialCombo )
{
   var resources = window.parent[FCKConfig.AttachmentsVariable];
   for ( var i = 0 ; i < resources.length ; i++ )
   {
      var label = resources[i][0];
      var value = resources[i][1];

      this._Combo.AddItem( value, label, label ) ;
   }
}

FCKToolbarAttachmentsCombo.prototype.GetSelected = function() {
   return this.currentItem;
}

if ( FCKBrowserInfo.IsIE )
{
	FCKToolbarAttachmentsCombo.prototype.RefreshActiveItems = function( combo, value )
	{
//		FCKDebug.Output( 'FCKToolbarFontFormatCombo Value: ' + value ) ;

		// IE returns normal for DIV and P, so to avoid confusion, we will not show it if normal.
		if ( value == this.NormalLabel )
		{
			if ( combo.Label != '&nbsp;' )
				combo.DeselectAll(true) ;
		}
		else
		{
			if ( this._LastValue == value )
				return ;

			combo.SelectItemByLabel( value, true ) ;
		}

		this._LastValue = value ;
	}
}

var SelectAttachmentsCommand = function( combo ) {
   this.combo = combo;
}

function prepareSelection(uri, label) {
	if (FCKBrowserInfo.IsIE) {
		var oSel = FCK.EditorDocument.selection ;
		FCK.Focus();
		var oRange = oSel.createRange();
		if (oRange.text.length == 0) {
			var anchorId = "id" + Math.floor(Math.random()*65535);

			FCK.InsertHtml("<a id='" + anchorId + "'>"+label+"</a>");
			FCK.Focus();
			var oLink = FCK.EditorDocument.getElementById(anchorId);
			if ( oLink ) {
	      FCK.Selection.SelectNode( oLink ) ;
				oLink.setAttribute("href", uri);
				oLink.removeAttribute("id");
			}
		}
	}
	else {
		var oRange = FCK.EditorWindow.getSelection().getRangeAt(0) ;
		if (oRange.collapsed) {
			// Create a fragment with the input HTML.
			var anchorId = "id" + Math.floor(Math.random()*65535);

			FCK.InsertHtml("<a id='" + anchorId + "'>"+label+"</a>");
			FCK.Focus();

			var oLink = FCK.EditorWindow.document.getElementById(anchorId);
			if ( oLink ) {
	      FCK.Selection.SelectNode( oLink ) ;
				oLink.setAttribute("href", uri);
				oLink.removeAttribute("id");
			}
		}
	}
}

SelectAttachmentsCommand.prototype.Execute = function() {
   var item = this.combo.GetSelected();

   var oLink = FCK.Selection.MoveToAncestorNode( 'A' ) ;
   if ( oLink ) {
      FCK.Selection.SelectNode( oLink ) ;
      FCKUndo.SaveUndoStep() ;
			oLink.href = item.itemId ;
      return;
   }
	 else {
	 	  prepareSelection(item.itemId, item.FCKItemLabel);
	 }

	 FCK.CreateLink(item.itemId);
}

SelectAttachmentsCommand.prototype.GetState = function()
{
	return FCK_TRISTATE_OFF ;
}

var attachmentCombo = new FCKToolbarAttachmentsCombo('SelectAttachmentsCombo');
var attachmentComboCommand = new FCKToolbarAttachmentSelectCommand( attachmentCombo );

FCKCommands.RegisterCommand('SelectAttachmentsCombo', attachmentComboCommand);
FCKToolbarItems.RegisterItem('SelectAttachmentsCombo', attachmentCombo);

FCKCommands.RegisterCommand('Select_Attachment', new SelectAttachmentsCommand(attachmentCombo));

var oSelectAttachmentItem = new FCKToolbarButton('Select_Attachment', FCKLang['SelectAttchments']);
oSelectAttachmentItem.IconPath = FCKConfig.PluginsPath + 'attachments/select.gif' ;

FCKToolbarItems.RegisterItem('SelectAttachmentsButton', oSelectAttachmentItem);


