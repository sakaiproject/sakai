/*******************************************************************************
 * $URL:  $
 * $Id:  $
 * **********************************************************************************
 *
 * Copyright (c) 2010 The Sakai Foundation
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
 ******************************************************************************/

var sakai = sakai || {};
sakai.editor = sakai.editor || {};
sakai.editor.editors = sakai.editor.editors || {};


sakai.editor.editors.fckeditor = {};
sakai.editor.editors.fckeditor.launch = function(targetId, config, w, h) {
    var oFCKeditor = new FCKeditor(targetId);
    oFCKeditor.BasePath = "/library/editor/FCKeditor/";
    if(config != null && config.width && config.width != ''){
	w = config.width;
    }else if (w == null || w == '') {
	w = "675";
    }
    if(config != null && config.height && config.height != ''){
	h = config.height;
    }else if (h == null || h == '') {
	h = "275";
    }
    oFCKeditor.Width  = w;
    oFCKeditor.Height = h;

    var folder = "";

    var collectionId = "";
    if (config != null && config.collectionId) {
        collectionId=config.collectionId;
    }
    else if (sakai.editor.collectionId) {
        collectionId=sakai.editor.collectionId
    }

    if (collectionId) {
        folder = '&CurrentFolder=' + collectionId;
        oFCKeditor.Config['CurrentFolder'] = collectionId 
    }
		
    oFCKeditor.Config['ImageBrowserURL'] = oFCKeditor.BasePath + "editor/filemanager/browser/default/browser.html?Connector=/sakai-fck-connector/filemanager/connector"+collectionId+"&Type=Image" + folder;
    oFCKeditor.Config['LinkBrowserURL'] = oFCKeditor.BasePath + "editor/filemanager/browser/default/browser.html?Connector=/sakai-fck-connector/filemanager/connector"+collectionId+"&Type=Link" + folder;
    oFCKeditor.Config['FlashBrowserURL'] = oFCKeditor.BasePath + "editor/filemanager/browser/default/browser.html?Connector=/sakai-fck-connector/filemanager/connector"+collectionId+"&Type=Flash" + folder;
    oFCKeditor.Config['ImageUploadURL'] = "/sakai-fck-connector/filemanager/connector"+collectionId+"?Type=Image&Command=QuickUpload&Type=Image" + folder;
    oFCKeditor.Config['FlashUploadURL'] = "/sakai-fck-connector/filemanager/connector"+collectionId+"?Type=Flash&Command=QuickUpload&Type=Flash" + folder;
    oFCKeditor.Config['LinkUploadURL'] = "/sakai-fck-connector/filemanager/connector"+collectionId+"?Type=File&Command=QuickUpload&Type=Link" + folder;

    var config = sakai.editor.enableResourceSearch ? "config_rs.js" : "config.js";

    oFCKeditor.Config['CustomConfigurationsPath'] = "/library/editor/FCKeditor/" + config;
    oFCKeditor.ReplaceTextarea();
}

sakai.editor.launch = sakai.editor.editors.fckeditor.launch;

