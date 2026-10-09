/* Improved undo support for Kupu

    This uses the 'diff-match-patch' library from Neil Fraser to make patches
    between edits, and saves those patches in an undo buffer. 

    The diff-match-patch lib is distributed under terms of the LGPL license,
    for more information see doc/LICENSE_DIFFMATCHPATH.txt.

    For more information about the library itself, see
    http://code.google.com/p/google-diff-match-patch/
*/

function KupuUndoTool() {
    if (arguments.length) {
        this._init.apply(this, arguments);
    };
};

KupuUndoTool.prototype = new KupuTool;

KupuUndoTool.prototype._init =
        function _init(undobuttonid, redobuttonid, maxbufsize) {
    this.undobutton = document.getElementById(undobuttonid);
    this.redobutton = document.getElementById(redobuttonid);
    this._content = null;
    this._buffer = [];
    this._rev_buffer = [];
    this._maximum_buffer_size = maxbufsize || 100;
};

KupuUndoTool.prototype.initialize = function initialize(editor) {
    this.editor = editor;
    addEventHandler(this.undobutton, 'click', this.undo, this);
    addEventHandler(this.redobutton, 'click', this.redo, this);
    this.dmp = new diff_match_patch();
    this._content = this._get_content();
};

KupuUndoTool.prototype.updateState = function updateState(selNode, event) {
    var new_content = this._get_content();
    var patch = this.dmp.patch_make(new_content, this._content);
    if (!patch.toString()) {
        return;
    };
    var revpatch = this.dmp.patch_make(this._content, new_content);
    this._buffer.push([patch, revpatch]);
    this._rev_buffer = []; // discard redo information
    this._content = new_content;
    while (this._buffer.length > this._maximum_buffer_size) {
        this._buffer.shift();
    };
};

KupuUndoTool.prototype.undo = function undo() {
    this.updateState();
    var patchset = this._buffer.pop();
    if (!patchset) {
        return;
    };
    this._apply_patch(patchset[0]);
    this._rev_buffer.push(patchset);
};

KupuUndoTool.prototype.redo = function redo() {
    var patchset = this._rev_buffer.pop();
    if (!patchset) {
        return;
    };
    this._apply_patch(patchset[1]);
    this._buffer.push(patchset);
};

KupuUndoTool.prototype._apply_patch = function _apply_patch(patch) {
    var ret = this.dmp.patch_apply(patch, this._content);
    var results = ret[1];
    var failures = false;
    for (var i=0; i < results.length; i++) {
        if (!results[i]) {
            failures = true;
            break;
        };
    };
    if (failures) {
        throw('there were errors applying undo buffer patch! ' +
              'cancelling undo action...');
    };
    var new_content = ret[0];
    this._set_content(new_content);
    this._content = new_content;
};

KupuUndoTool.prototype._get_content = function _get_content() {
    var body = this.editor.document.document.getElementsByTagName('body')[0];
    return body.innerHTML;
};

KupuUndoTool.prototype._set_content = function _set_content(content) {
    var body = this.editor.document.document.getElementsByTagName('body')[0];
    body.innerHTML = content;
};

KupuUndoTool.prototype.debug = function debug(msg) {
    var body = document.getElementsByTagName('body')[0];
    var div = document.createElement('div');
    div.appendChild(document.createTextNode(msg));
    body.appendChild(div);
    div.style.border = '1px solid red';
};
