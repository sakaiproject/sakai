var SakaiProject;
if (!SakaiProject) SakaiProject = {};

SakaiProject.fckeditor = function() {
  // Private functions, if any
  
  // Public 
  return {
    initializeEditor: function(textarea_id, collection_id, editor_height, editor_width) {
      var basepath = "/library/editor/FCKeditor/";
      var browsePrefix = basepath + "editor/filemanager/browser/default/browser.html?Connector=/sakai-fck-connector/web/editor/filemanager/browser/default/connectors/jsp/connector&";
      var uploadPrefix = basepath + "/sakai-fck-connector/web/editor/filemanager/browser/default/connectors/jsp/connector?";
      
      var oFCKeditor = new FCKeditor(textarea_id);
      oFCKeditor.BasePath = basepath;
      if (collection_id !== "") {
        oFCKeditor.Config['ImageBrowserURL'] = browsePrefix + "Type=Image&CurrentFolder=" + collection_id;
        oFCKeditor.Config['LinkBrowserURL'] = browsePrefix + "Type=Link&CurrentFolder=" + collection_id;
        oFCKeditor.Config['FlashBrowserURL'] = browsePrefix + "Type=Flash&CurrentFolder=" + collection_id;
        oFCKeditor.Config['ImageUploadURL'] = uploadPrefix + "Type=Image&Command=QuickUpload&Type=Image&CurrentFolder=" + collection_id;
        oFCKeditor.Config['FlashUploadURL'] = uploadPrefix + "Type=Flash&Command=QuickUpload&Type=Flash&CurrentFolder=" + collection_id;
        oFCKeditor.Config['LinkUploadURL'] = uploadPrefix + "Type=File&Command=QuickUpload&Type=Link&CurrentFolder=" + collection_id;
      }
      oFCKeditor.Width  = editor_width ;
      oFCKeditor.Height = editor_height ;
      oFCKeditor.Config['CustomConfigurationsPath'] = "/library/editor/FCKeditor/config.js";
      oFCKeditor.ReplaceTextarea() ;
    }
  };
}();
