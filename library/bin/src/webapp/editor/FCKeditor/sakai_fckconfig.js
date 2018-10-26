
document.write("<script type=\"text/javascript\" src=\"/library/editor/FCKeditor/fckeditor.js\"></script>\n");

/**
 * @deprecated
 */
function chef_setupfcktextarea(textarea_id, widthPx, heightPx, collectionId, tagsFocus, resourceSearch) {
   return chef_setupfcktextarea(textarea_id, widthPx, heightPx, collectionId, resourceSearch);
}

function chef_setupfcktextarea(textarea_id, widthPx, heightPx, collectionId, resourceSearch) {
	var oFCKeditor = new FCKeditor(textarea_id);
	var connector = "/sakai-fck-connector/web/editor/filemanager/browser/default/connectors/jsp/connector";
	oFCKeditor.BasePath = "/library/editor/FCKeditor/";

	if (widthPx < 0) {
   	widthPx = 600;
   }
   if (heightPx < 0) {
      heightPx = 400;
   }
   //FCK's toolset is larger then htmlarea and this prevents tools from ending up with all toolbar
   //and no actual editing area.
   if (heightPx < 200) {
      heightPx = 200;
   }

   oFCKeditor.Width = widthPx;
   oFCKeditor.Height = heightPx;

   var courseId = collectionId;
   oFCKeditor.Config['ImageBrowserURL'] = oFCKeditor.BasePath + 
      "editor/filemanager/browser/default/browser.html?Connector=" + 
      connector + "&Type=Image&CurrentFolder=" + courseId;
   oFCKeditor.Config['LinkBrowserURL'] = oFCKeditor.BasePath +
      "editor/filemanager/browser/default/browser.html?Connector=" + 
      connector + "&Type=Link&CurrentFolder=" + courseId;
   oFCKeditor.Config['FlashBrowserURL'] = oFCKeditor.BasePath +
      "editor/filemanager/browser/default/browser.html?Connector=" + 
      connector + "&Type=Flash&CurrentFolder=" + courseId;
   oFCKeditor.Config['ImageUploadURL'] = oFCKeditor.BasePath +
      connector + "?Type=Image&Command=QuickUpload&Type=Image&CurrentFolder=" + courseId;
   oFCKeditor.Config['FlashUploadURL'] = oFCKeditor.BasePath +
      connector + "?Type=Flash&Command=QuickUpload&Type=Flash&CurrentFolder=" + courseId;
   oFCKeditor.Config['LinkUploadURL'] = oFCKeditor.BasePath +
      connector + "?Type=File&Command=QuickUpload&Type=Link&CurrentFolder=" + courseId;

   oFCKeditor.Config['CurrentFolder'] = courseId;
         
   if(resourceSearch == "true")
   {
      oFCKeditor.Config['CustomConfigurationsPath'] = "/library/editor/FCKeditor/config_rs.js";
   }
   else
   {
       oFCKeditor.Config['CustomConfigurationsPath'] = "/library/editor/FCKeditor/config.js";
   }

   oFCKeditor.ReplaceTextarea() ;
}
