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
 *       http://www.osedu.org/licenses/ECL-2.0
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
sakai.editor.editors.fckeditor.launch = function(targetId, config) {
    var oFCKeditor = new FCKeditor(targetId);
    oFCKeditor.BasePath = "/library/editor/FCKeditor/";
    oFCKeditor.Width  = "675" ;
    oFCKeditor.Height = "275" ;

    var folder = "";
    if (sakai.editor.collectionId) {
        folder = '&CurrentFolder=' + sakai.editor.collectionId;
        oFCKeditor.Config['CurrentFolder'] = sakai.editor.collectionId;
    }

    oFCKeditor.Config['ImageBrowserURL'] = oFCKeditor.BasePath + "editor/filemanager/browser/default/browser.html?Connector=/sakai-fck-connector/filemanager/connector&Type=Image" + folder;
    oFCKeditor.Config['LinkBrowserURL'] = oFCKeditor.BasePath + "editor/filemanager/browser/default/browser.html?Connector=/sakai-fck-connector/filemanager/connector&Type=Link" + folder;
    oFCKeditor.Config['FlashBrowserURL'] = oFCKeditor.BasePath + "editor/filemanager/browser/default/browser.html?Connector=/sakai-fck-connector/filemanager/connector&Type=Flash" + folder;
    oFCKeditor.Config['ImageUploadURL'] = oFCKeditor.BasePath + "/sakai-fck-connector/filemanager/connector?Type=Image&Command=QuickUpload&Type=Image" + folder;
    oFCKeditor.Config['FlashUploadURL'] = oFCKeditor.BasePath + "/sakai-fck-connector/filemanager/connector?Type=Flash&Command=QuickUpload&Type=Flash" + folder;
    oFCKeditor.Config['LinkUploadURL'] = oFCKeditor.BasePath + "/sakai-fck-connector/filemanager/connector?Type=File&Command=QuickUpload&Type=Link" + folder;

    var config = sakai.editor.enableResourceSearch ? "config_rs.js" : "config.js";

    oFCKeditor.Config['CustomConfigurationsPath'] = "/library/editor/FCKeditor/" + config;
    oFCKeditor.ReplaceTextarea();
}

sakai.editor.launch = sakai.editor.editors.fckeditor.launch;

