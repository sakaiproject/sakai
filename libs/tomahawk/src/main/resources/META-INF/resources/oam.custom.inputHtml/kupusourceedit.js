/*****************************************************************************
 *
 * Copyright (c) 2003-2005 Kupu Contributors. All rights reserved.
 *
 * This software is distributed under the terms of the Kupu
 * License. See LICENSE.txt for license text. For a list of Kupu
 * Contributors see CREDITS.txt.
 *
 *****************************************************************************/
// $Id: kupusourceedit.js 928511 2010-03-28 22:53:14Z lu4242 $


function SourceEditTool(sourcebuttonid, sourceareaid) {
    /* Source edit tool to edit document's html source */
    this.sourceButton = getFromSelector(sourcebuttonid);
    this.sourcemode = false;
    this._currently_editing = null;

    // method defined inline to support closure
    // XXX would be nice to have this defined on the prototype too, because
    // of subclassing issues?
    this.getSourceArea = function() {
        return getFromSelector(sourceareaid);
    };
};

SourceEditTool.prototype = new KupuTool;

SourceEditTool.prototype.cancelSourceMode = function() {
    if (this._currently_editing) {
        this.switchSourceEdit(null, true);
    }
};

SourceEditTool.prototype.updateState = 
        SourceEditTool.prototype.cancelSourceMode;

SourceEditTool.prototype.initialize = function(editor) {
    /* attach the event handlers */
    this.editor = editor;
    if (!this.sourceButton) return;
    addEventHandler(this.sourceButton, "click", this.switchSourceEdit, this);
    this.editor.logMessage(_('Source edit tool initialized'));
};

SourceEditTool.prototype.switchSourceEdit = function(event, nograb) {
    var kupu = this.editor;
    var docobj = this._currently_editing||kupu.getDocument();
    var editorframe = docobj.getEditable();
    var sourcearea = this.getSourceArea();
    var kupudoc = docobj.getDocument();
    var sourceClass = 'kupu-sourcemode';

    if (!this.sourcemode) {
        if (window.drawertool) {
            window.drawertool.closeDrawer();
        }
        if (/on/i.test(kupudoc.designMode)) {
            kupudoc.designMode = 'Off';
        };
        kupu._initialized = false;

        var data='';
        if(kupu.config.filtersourceedit) {
            window.status = _('Cleaning up HTML...');
            var transform = kupu._filterContent(kupu.getInnerDocument().documentElement);
            data = kupu.getXMLBody(transform);
            data = kupu._fixupSingletons(data).replace(/<\/?body[^>]*>/g, "");
            if (kupu._getBase && kupu.makeLinksRelative) {
                var base = kupu._getBase(transform);
                data = kupu.makeLinksRelative(data, base).replace(/<\/?body[^>]*>/g, "");
            };
            window.status = '';
        } else {
            data = kupu.getHTMLBody();
        }
        sourcearea.value = data.strip();
        kupu.setClass(sourceClass);
        editorframe.style.display = 'none';
        sourcearea.style.display = 'block';
        if (!nograb) {
            sourcearea.focus();
        };
        this._currently_editing = docobj;
      } else {
        kupu.setHTMLBody(sourcearea.value);
        kupu.clearClass(sourceClass);
        sourcearea.style.display = 'none';
        editorframe.style.display = 'block';
        if (/off/i.test(kupudoc.designMode)) {
            kupudoc.designMode = 'On';
        };
        if (!nograb) {
            docobj.getWindow().focus();
            var selection = this.editor.getSelection();
            selection.collapse();
        };

        kupu._initialized = true;
        this._currently_editing = null;
        this.editor.updateState();
    };
    this.sourcemode = !this.sourcemode;
};

SourceEditTool.prototype.enable = function() {
    kupuButtonEnable(this.sourceButton);
};

SourceEditTool.prototype.disable = function() {
    kupuButtonDisable(this.sourceButton);
};

function MultiSourceEditTool(sourcebuttonid, textareaprefix) {
    /* Source edit tool to edit document's html source */
    this.sourceButton = getFromSelector(sourcebuttonid);
    this.textareaprefix = textareaprefix;

    this._currently_editing = null;
};

MultiSourceEditTool.prototype = new SourceEditTool;

MultiSourceEditTool.prototype.getSourceArea = function() {
    var docobj = this._currently_editing||kupu.getDocument();
    var sourceareaid = this.textareaprefix + docobj.getEditable().id;
    return getFromSelector(sourceareaid);
};
