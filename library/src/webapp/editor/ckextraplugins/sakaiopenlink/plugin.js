/**
 * @fileOverview The "sakaiopenlink" plugin.
 * forked from https://github.com/mlewand/ckeditor-plugin-openlink
 */

( function() {

    function sakaiListener( editor ){

        var target = editor.config.sakaiopenlink_target || '_blank',
            sakaiopenlinkInstance = new sakaiopenlinkPlugin( editor, editor.config );

        // Register sakaiopenlink command.
        editor.addCommand( 'sakaiopenlink', {
            exec: function( editor ) {
                var anchor = getActiveLink( editor ),
                    href;

                if ( anchor ) {
                    href = anchor.getAttribute( 'href' );
                }

                if ( href ) {
                    window.open( href, target );
                }
            }
        } );

        // Register menu items.
        if ( editor.addMenuItems ) {
            editor.addMenuItems( {
                sakaiopenlink: {
                    label: editor.lang.sakaiopenlink.menu,
                    command: 'sakaiopenlink',
                    group: 'link',
                    order: -1
                }
            } );
        }

        // If the "contextmenu" plugin is loaded, register the listeners.
        editor.contextMenu.addListener( function( element, selection ) {
            if ( !element ) {
                return null;
            }

            var anchor = getActiveLink( editor );

            if ( anchor && anchor.getAttribute( 'href' ) ) {
                return {
                    sakaiopenlink: CKEDITOR.TRISTATE_OFF
                };
            }

            return {};
        } );

        // A quick workaround for issue #11842.
        editor.on( 'contentDom', function( evt ) {
            var editable = editor.editable();

            // We want to be able to open links also in read-only mode. This
            // listener will open link in new tab.
            editable.attachListener( editable, 'click', function( evt ) {
                // This feature should be available in:
                // * wysywigmode in read-only
                // * wysywigmode when ctrl key is down
                var target = evt.data.getTarget(),
                    clickedAnchor = ( new CKEDITOR.dom.elementPath( target, editor.editable() ) ).contains( 'a' ),
                    href = clickedAnchor && clickedAnchor.getAttribute( 'href' ),
                    modifierPressed = sakaiopenlinkInstance.properModifierPressed( evt );

                if ( editor.readOnly && !editor.config.sakaiopenlink_enableReadOnly ) {
                    return;
                }

                if ( href && modifierPressed ) {
                    window.open( href, target );

                    // We need to prevent it for Firefox, as it has it's own handling (#8).
                    evt.data.preventDefault();
                }
            } );

            if ( sakaiopenlinkInstance.modifierRequired() ) {
                // Keyboard listeners are needed only if any modifier is required to open clicked link.
                editable.attachListener( editable, 'keydown', sakaiopenlinkInstance.onKeyPress, sakaiopenlinkInstance );
                editable.attachListener( editable, 'keyup', sakaiopenlinkInstance.onKeyPress, sakaiopenlinkInstance );
            } else {
                // If any clicks should trigger link open, then just add the class to the editable.
                editor.editable().addClass( 'sakaiopenlink' );
            }

        } );
    }
    
    CKEDITOR.plugins.add( 'sakaiopenlink', {
        lang: 'bg,en,de,pl,ru,uk', // %REMOVE_LINE_CORE%
        icons: 'sakaiopenlink', // %REMOVE_LINE_CORE%
        hidpi: true, // %REMOVE_LINE_CORE%
        requires: 'link,contextmenu',

        onLoad: function() {
            CKEDITOR.addCss( '.sakaiopenlink a:hover{ cursor: pointer; }' );
        },

        init: function( editor ) {
            if ( !editor.config.sakaiOpenLink )
                return;
            sakaiListener(editor);
            
        },
        afterInit: function( editor ) {
            if ( !editor.config.sakaiOpenLink )
                return;
            editor.ui.addButton( 'sakaiopenlink', {
                command: 'sakaiopenlink',
                toolbar: 'links,50',
                label: `${editor.lang.sakaiopenlink.menu}`
            } );
            editor.balloonToolbars.create( {
                buttons: 'sakaiopenlink,Link,Unlink',
                cssSelector: 'a'
            } );
        }
    } );

    // Returns the element of active (currently focused) link.
    // It has also support for linked image2 instance.
    // @return {CKEDITOR.dom.element}
    function getActiveLink( editor ) {
        var anchor = CKEDITOR.plugins.link.getSelectedLink( editor ),
            // We need to do some special checking against widgets availability.
            activeWidget = editor.widgets && editor.widgets.focused;

        // If default way of getting links didn't return anything useful
        if ( !anchor && activeWidget && activeWidget.name === 'image' && activeWidget.parts.link ) {
            // Since CKEditor 4.4.0 image widgets may be linked.
            anchor = activeWidget.parts.link;
        }

        return anchor;
    }

    /**
     * sakaiopenlink plugin type, groups all the functions related to plugin.
     *
     * @class CKEDITOR.plugins.sakaiopenlink
     * @param {CKEDITOR.editor} editor
     * @param {CKEDITOR.config} config
     */
    function sakaiopenlinkPlugin( editor, config ) {
        this.editor = editor;
        this.modifier = typeof config.sakaiopenlink_modifier != 'undefined' ? config.sakaiopenlink_modifier : CKEDITOR.CTRL;
    }

    /**
     * Whether configuration requires __any__ modifier key to be hold in order to open the link.
     *
     * @returns {Boolean}
     */
    sakaiopenlinkPlugin.prototype.modifierRequired = function() {
        return this.modifier !== 0;
    };

    /**
     * Tells if `evt` has proper modifier keys pressed.
     *
     * **Note:** it will return `true` if modifier is not required.
     *
     * @param {CKEDITOR.dom.event} evt
     * @returns {Boolean}
     */
    sakaiopenlinkPlugin.prototype.properModifierPressed = function( evt ) {
        return !this.modifierRequired() || ( evt.data.getKeystroke() & this.modifier );
    };

    /**
     * Method to be called upon `keydown`, `keyup` events.
     *
     * @param {CKEDITOR.dom.event} evt
     */
    sakaiopenlinkPlugin.prototype.onKeyPress = function( evt ) {
        if ( this.properModifierPressed( evt ) ) {
            this.editor.editable().addClass( 'sakaiopenlink' );
        } else {
            this.editor.editable().removeClass( 'sakaiopenlink' );
        }
    };

    CKEDITOR.plugins.sakaiopenlink = sakaiopenlinkPlugin;
} )();
