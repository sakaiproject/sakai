var sakai = sakai || {};
sakai.editor = sakai.editor || {};
sakai.editor.editors = sakai.editor.editors || {};

sakai.editor.editors.ckeditor = {};
sakai.editor.editors.ckeditor.launch = function(targetId, config) {
    var folder = "";
    if (sakai.editor.collectionId) {
        folder = "&CurrentFolder=" + sakai.editor.collectionId;
    }
    CKEDITOR.replace(targetId, {
        skin: 'v2',
        height: 460,
        filebrowserBrowseUrl :'/library/editor/FCKeditor/editor/filemanager/browser/default/browser.html?Connector=/sakai-fck-connector/web/editor/filemanager/browser/default/connectors/jsp/connector' + folder,
        filebrowserImageBrowseUrl : '/library/editor/FCKeditor/editor/filemanager/browser/default/browser.html?Type=Image&Connector=/sakai-fck-connector/web/editor/filemanager/browser/default/connectors/jsp/connector' + folder,
        filebrowserFlashBrowseUrl :'/library/editor/FCKeditor/editor/filemanager/browser/default/browser.html?Type=Flash&Connector=/sakai-fck-connector/web/editor/filemanager/browser/default/connectors/jsp/connector' + folder
    });
}

sakai.editor.launch = sakai.editor.editors.ckeditor.launch;

