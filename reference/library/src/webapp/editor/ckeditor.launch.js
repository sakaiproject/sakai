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
        filebrowserFlashBrowseUrl :'/library/editor/FCKeditor/editor/filemanager/browser/default/browser.html?Type=Flash&Connector=/sakai-fck-connector/web/editor/filemanager/browser/default/connectors/jsp/connector' + folder,
        extraPlugins: (sakai.editor.enableResourceSearch ? 'resourcesearch' : ''),

        toolbar_Full:
        [
            ['Source','-','Templates'],
            ['Cut','Copy','Paste','PasteText','PasteFromWord','-','Print', 'SpellChecker', 'Scayt'],
            ['Undo','Redo','-','Find','Replace','-','SelectAll','RemoveFormat'],
            ['NumberedList','BulletedList','-','Outdent','Indent','Blockquote','CreateDiv'],
            '/',
            ['Bold','Italic','Underline','Strike','-','Subscript','Superscript'],
            ['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],
            ['BidiLtr', 'BidiRtl' ],
            ['Link','Unlink','Anchor'],
            (sakai.editor.enableResourceSearch
                ? ['ResourceSearch', 'Image','Flash','Table','HorizontalRule','Smiley','SpecialChar','PageBreak']
                : ['Image','Flash','Table','HorizontalRule','Smiley','SpecialChar','PageBreak']),
            '/',
            ['Styles','Format','Font','FontSize'],
            ['TextColor','BGColor'],
            ['Maximize', 'ShowBlocks','-','About']
        ],
        resize_dir: 'vertical'
    });
}

sakai.editor.launch = sakai.editor.editors.ckeditor.launch;

