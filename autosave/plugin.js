/**
 * @license Copyright (c) CKSource - Frederico Knabben. All rights reserved.
 * For licensing, see LICENSE.html or http://ckeditor.com/license
 */

(function() {
    if (!supportsLocalStorage()) {
        CKEDITOR.plugins.add("autosave", {}); //register a dummy plugin to pass CKEditor plugin initialization process
        return;
    }

    CKEDITOR.plugins.add("autosave", {
        lang: 'de,en,jp,pl,pt-br,sv,zh,zh-cn', // %REMOVE_LINE_CORE%
        version: 0.10,
        init: function(editor) {
            CKEDITOR.document.appendStyleSheet(CKEDITOR.getUrl(CKEDITOR.plugins.getPath('autosave') + 'css/autosave.min.css'));

            if (typeof (jQuery) === 'undefined') {
                CKEDITOR.scriptLoader.load('//ajax.googleapis.com/ajax/libs/jquery/1/jquery.min.js', function() {
                    jQuery.noConflict();

                    loadPlugin(editor);
                });

            } else {
                CKEDITOR.scriptLoader.load(CKEDITOR.getUrl(CKEDITOR.plugins.getPath('autosave') + 'js/extensions.min.js'), function() {
                    loadPlugin(editor);
                });
            }
        }
    });

    function loadPlugin(editorInstance) {
        var autoSaveKey = editorInstance.config.autosave_SaveKey != null ? editorInstance.config.autosave_SaveKey : 'autosave_' + window.location;
        var notOlderThan = editorInstance.config.autosave_NotOlderThan != null ? editorInstance.config.autosave_NotOlderThan : 1440;
        var saveOnDestroy = editorInstance.config.autosave_saveOnDestroy != null ? editorInstance.config.autosave_saveOnDestroy : false;
        var saveDetectionSelectors =
            editorInstance.config.autosave_saveDetectionSelectors != null ? editorInstance.config.autosave_saveDetectionSelectors : "a[href^='javascript:__doPostBack'][id*='Save'],a[id*='Cancel']";


        CKEDITOR.scriptLoader.load(CKEDITOR.getUrl(CKEDITOR.plugins.getPath('autosave') + 'js/extensions.min.js'), function() {
            GenerateAutoSaveDialog(editorInstance, autoSaveKey);

            CheckForAutoSavedContent(editorInstance, autoSaveKey, notOlderThan);
        });

        jQuery(saveDetectionSelectors).click(function() {
            RemoveStorage(autoSaveKey);
        });

        editorInstance.on('change', startTimer);

        editorInstance.on('destroy', function() {
            if (saveOnDestroy) {
                SaveData(autoSaveKey, editorInstance);
            }
        });

        editorInstance.on('uiSpace', function(event) {
            if (event.data.space == 'bottom') {
                event.data.html += '<div class="autoSaveMessage" unselectable="on"><div unselectable="on" id="'
                    + autoSaveMessageId(event.editor)
                    + '"class="hidden">'
                    + event.editor.lang.autosave.autoSaveMessage
                    + '</div></div>';
            }
        }, editorInstance, null, 100);
    }

    function autoSaveMessageId(editorInstance) {
        return 'cke_autoSaveMessage_' + editorInstance.name;
    }

    var timeOutId = 0,
        savingActive = false;

    var startTimer = function(event) {
        if (timeOutId) {
            clearTimeout(timeOutId);
        }
        var delay = CKEDITOR.config.autosave_delay != null ? CKEDITOR.config.autosave_delay : 10;
        timeOutId = setTimeout(onTimer, delay * 1000, event);
    };
    var onTimer = function(event) {
        if (savingActive) {
            startTimer(event);
        } else if (event.editor.checkDirty() || event.editor.plugins.bbcode) {
            savingActive = true;
            var editor = event.editor,
                autoSaveKey = editor.config.autosave_SaveKey != null ? editor.config.autosave_SaveKey : 'autosave_' + window.location;

            SaveData(autoSaveKey, editor);

            savingActive = false;
        }
    };

    // localStorage detection
    function supportsLocalStorage() {
        if (typeof (Storage) === 'undefined') {
            return false;
        }

        try {
            localStorage.getItem("___test_key");
            return true;
        } catch (e) {
            return false;
        }
    }

    function GenerateAutoSaveDialog(editorInstance, autoSaveKey) {
        CKEDITOR.dialog.add('autosaveDialog', function() {
            return {
                title: editorInstance.lang.autosave.title,
                minHeight: 155,
                height: 300,
                width: 750,
                onShow: function() {
                    RenderDiff(this, editorInstance, autoSaveKey);
                },
                onOk: function() {
                    var jsonSavedContent = LoadData(autoSaveKey);

                    editorInstance.setData(jsonSavedContent.data);

                    RemoveStorage(autoSaveKey);
                },
                onCancel: function() {
                    RemoveStorage(autoSaveKey);
                },
                contents: [
                    {
                        label: '',
                        id: 'general',
                        elements: [
                            {
                                type: 'radio',
                                id: 'diffType',
                                label: editorInstance.lang.autosave.diffType,
                                items: [[editorInstance.lang.autosave.sideBySide, 'sideBySide'], [editorInstance.lang.autosave.inline, 'inline']],
                                'default': 'sideBySide',
                                onClick: function() {
                                    RenderDiff(this._.dialog, editorInstance, autoSaveKey);
                                }
                            }, {
                                type: 'html',
                                id: 'diffContent',
                                html: ''
                            }
                        ]
                    }
                ],
                buttons: [
                    {
                        id: 'ok',
                        type: 'button',
                        label: editorInstance.lang.autosave.ok,
                        'class': 'cke_dialog_ui_button_ok cke_dialog_autosave_ok',
                        onClick: function(evt) {
                            var dialog = evt.data.dialog;
                            if (dialog.fire('ok', { hide: true }).hide !== false)
                                dialog.hide();
                        }
                    },
                    {
                        id: 'cancel',
                        type: 'button',
                        label: editorInstance.lang.autosave.no,
                        'class': 'cke_dialog_ui_button_cancel',
                        onClick: function(evt) {
                            var dialog = evt.data.dialog;
                            if (dialog.fire('cancel', { hide: true }).hide !== false)
                                dialog.hide();
                        }
                    }
                ]
            };
        });
    }

    function CheckForAutoSavedContent(editorInstance, autoSaveKey, notOlderThan) {
        // Checks If there is data available and load it
        if (localStorage.getItem(autoSaveKey)) {
            var jsonSavedContent = LoadData(autoSaveKey);

            var autoSavedContent = jsonSavedContent.data;
            var autoSavedContentDate = jsonSavedContent.saveTime;

            var editorLoadedContent = editorInstance.getData();

            // check if the loaded editor content is the same as the autosaved content
            if (editorLoadedContent == autoSavedContent) {
                localStorage.removeItem(autoSaveKey);
                return;
            }

            // Ignore if autosaved content is older then x minutes
            if (moment(new Date()).diff(autoSavedContentDate, 'minutes') > notOlderThan) {
                RemoveStorage(autoSaveKey);

                return;
            }

            var confirmMessage = editorInstance.lang.autosave.loadSavedContent.replace("{0}", moment(autoSavedContentDate).lang(editorInstance.config.language).format(editorInstance.lang.autosave.dateFormat));
            if (confirm(confirmMessage)) {
                // Open DIFF Dialog
                editorInstance.openDialog('autosaveDialog');
            } else {
                RemoveStorage(autoSaveKey);
            }
        }
    }

    function LoadData(autoSaveKey) {
        var compressedJSON = LZString.decompressFromUTF16(localStorage.getItem(autoSaveKey));
        return JSON.parse(compressedJSON);
    }

    function SaveData(autoSaveKey, editorInstance) {
        var compressedJSON = LZString.compressToUTF16(JSON.stringify({ data: editorInstance.getData(), saveTime: new Date() }));
        localStorage.setItem(autoSaveKey, compressedJSON);

        var autoSaveMessage = document.getElementById(autoSaveMessageId(editorInstance));

        if (autoSaveMessage) {
            autoSaveMessage.className = "show";

            setTimeout(function() {
                autoSaveMessage.className = "hidden";
            }, 2000);
        }
    }

    function RemoveStorage(autoSaveKey) {
        if (timeOutId) {
            clearTimeout(timeOutId);
        }

        localStorage.removeItem(autoSaveKey);
    }

    function RenderDiff(dialog, editorInstance, autoSaveKey) {
        var jsonSavedContent = LoadData(autoSaveKey);

        var base = difflib.stringAsLines(editorInstance.getData());
        var newtxt = difflib.stringAsLines(jsonSavedContent.data);
        var sm = new difflib.SequenceMatcher(base, newtxt);
        var opcodes = sm.get_opcodes();

        dialog.getContentElement('general', 'diffContent').getElement().setHtml('<div class="diffContent">' + diffview.buildView({
            baseTextLines: base,
            newTextLines: newtxt,
            opcodes: opcodes,
            baseTextName: editorInstance.lang.autosave.loadedContent,
            newTextName: editorInstance.lang.autosave.autoSavedContent + (moment(jsonSavedContent.saveTime).lang(editorInstance.config.language).format(editorInstance.lang.autosave.dateFormat)) + '\'',
            contextSize: 3,
            viewType: dialog.getContentElement('general', 'diffType').getValue() == "inline" ? 1 : 0
        }).outerHTML + '</div>');
    }
})();
