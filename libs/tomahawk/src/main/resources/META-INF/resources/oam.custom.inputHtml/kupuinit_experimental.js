/*****************************************************************************
 *
 * Copyright (c) 2003-2005 Kupu Contributors. All rights reserved.
 *
 * This software is distributed under the terms of the Kupu
 * License. See LICENSE.txt for license text. For a list of Kupu
 * Contributors see CREDITS.txt.
 *
 *****************************************************************************/
// $Id: kupuinit_experimental.js 39345 2007-02-23 18:29:27Z yuppie $


//----------------------------------------------------------------------------
// Sample initialization function
//----------------------------------------------------------------------------

function initKupu(iframe) {
    /* Although this is meant to be a sample implementation, it can
        be used out-of-the box to run the sample pagetemplate or for simple
        implementations that just don't use some elements. When you want
        to do some customization, this should probably be overridden. For
        larger customization actions you will have to subclass or roll your
        own UI object.
    */

    // first we create a logger
    var l = new PlainLogger('kupu-toolbox-debuglog', 5);

    // now some config values
    var conf = loadDictFromXML(document, 'kupuconfig');

    // the we create the document, hand it over the id of the iframe
    var doc = new KupuDocument(iframe);

    // now we can create the controller
    var kupu = new KupuEditor(doc, conf, l);

    var contextmenu = new ContextMenu();
    kupu.setContextMenu(contextmenu);

    // now we can create a UI object which we can use from the UI
    var ui = new KupuUI('kupu-tb-styles');

    // the ui must be registered to the editor like a tool so it can be notified
    // of state changes
    kupu.registerTool('ui', ui); // XXX Should this be a different method?

    // add the buttons to the toolbar
    var savebuttonfunc = function(button, editor) {editor.saveDocument();};
    var savebutton = new KupuButton('kupu-save-button', savebuttonfunc);
    kupu.registerTool('savebutton', savebutton);

    // function that returns a function to execute a button command
    var execCommand = function(cmd) {
        return function(button, editor) {
            editor.execCommand(cmd);
        };
    };

    var boldchecker = parentWithStyleChecker(['b', 'strong'],
                                             'fontWeight', 'bold', 'bold');
    var boldbutton = new KupuStateButton('kupu-bold-button',
                                         execCommand('bold'),
                                         boldchecker,
                                         'kupu-bold',
                                         'kupu-bold-pressed');
    kupu.registerTool('boldbutton', boldbutton);

    var italicschecker = parentWithStyleChecker(['i', 'em'],
                                              'fontStyle', 'italic', 'italic');
    var italicsbutton = new KupuStateButton('kupu-italic-button',
                                            execCommand('italic'),
                                            italicschecker,
                                            'kupu-italic',
                                            'kupu-italic-pressed');
    kupu.registerTool('italicsbutton', italicsbutton);

    var underlinechecker = parentWithStyleChecker(['u'],
                                   'textDecoration', 'underline', 'underline');
    var underlinebutton = new KupuStateButton('kupu-underline-button',
                                              execCommand('underline'),
                                              underlinechecker,
                                              'kupu-underline',
                                              'kupu-underline-pressed');
    kupu.registerTool('underlinebutton', underlinebutton);

    var subscriptchecker = parentWithStyleChecker(['sub'],
                                                  null, null, 'subscript');
    var subscriptbutton = new KupuStateButton('kupu-subscript-button',
                                              execCommand('subscript'),
                                              subscriptchecker,
                                              'kupu-subscript',
                                              'kupu-subscript-pressed');
    kupu.registerTool('subscriptbutton', subscriptbutton);

    var superscriptchecker = parentWithStyleChecker(['super', 'sup'],
                                                    null, null, 'superscript');
    var superscriptbutton = new KupuStateButton('kupu-superscript-button',
                                                execCommand('superscript'),
                                                superscriptchecker,
                                                'kupu-superscript',
                                                'kupu-superscript-pressed');
    kupu.registerTool('superscriptbutton', superscriptbutton);

    var justifyleftbutton = new KupuButton('kupu-justifyleft-button',
                                           execCommand('justifyleft'));
    kupu.registerTool('justifyleftbutton', justifyleftbutton);

    var justifycenterbutton = new KupuButton('kupu-justifycenter-button',
                                             execCommand('justifycenter'));
    kupu.registerTool('justifycenterbutton', justifycenterbutton);

    var justifyrightbutton = new KupuButton('kupu-justifyright-button',
                                            execCommand('justifyright'));
    kupu.registerTool('justifyrightbutton', justifyrightbutton);

    var outdentbutton = new KupuButton('kupu-outdent-button', execCommand('outdent'));
    kupu.registerTool('outdentbutton', outdentbutton);

    var indentbutton = new KupuButton('kupu-indent-button', execCommand('indent'));
    kupu.registerTool('indentbutton', indentbutton);

    var undobutton = new KupuButton('kupu-undo-button', execCommand('undo'));
    kupu.registerTool('undobutton', undobutton);

    var redobutton = new KupuButton('kupu-redo-button', execCommand('redo'));
    kupu.registerTool('redobutton', redobutton);

    var removeimagebutton = new KupuRemoveElementButton('kupu-removeimage-button',
                                                        'img',
                                                        'kupu-removeimage');
    kupu.registerTool('removeimagebutton', removeimagebutton);

    var removelinkbutton = new KupuRemoveElementButton('kupu-removelink-button',
                                                       'a',
                                                       'kupu-removelink');
    kupu.registerTool('removelinkbutton', removelinkbutton);

    // add some tools
    var colorchoosertool = new ColorchooserTool('kupu-forecolor-button',
                                                'kupu-hilitecolor-button',
                                                'kupu-colorchooser');
    kupu.registerTool('colorchooser', colorchoosertool);

    var listtool = new ListTool('kupu-list-ul-addbutton',
                                'kupu-list-ol-addbutton',
                                'kupu-ulstyles',
                                'kupu-olstyles');
    kupu.registerTool('listtool', listtool);

    // since we use the inspector we don't need much else ;)
    var inspector = new KupuInspector('kupu-inspector-form');
    kupu.registerTool('inspector', inspector);

    var linktool = new LinkTool();
    kupu.registerTool('linktool', linktool);

    var imagetool = new ImageTool();
    kupu.registerTool('imagetool', imagetool);

    var tabletool = new TableTool();
    kupu.registerTool('tabletool', tabletool);

    var showpathtool = new ShowPathTool();
    kupu.registerTool('showpathtool', showpathtool);

    var viewsourcetool = new ViewSourceTool();
    kupu.registerTool('viewsourcetool', viewsourcetool);

    // Drawers...

    // Function that returns function to open a drawer
    var opendrawer = function(drawerid) {
        return function(button, editor) {
            drawertool.openDrawer(drawerid);
        };
    };

    var imagelibdrawerbutton = new KupuButton('kupu-imagelibdrawer-button',
                                              opendrawer('imagelibdrawer'));
    kupu.registerTool('imagelibdrawerbutton', imagelibdrawerbutton);

    var linklibdrawerbutton = new KupuButton('kupu-linklibdrawer-button',
                                             opendrawer('linklibdrawer'));
    kupu.registerTool('linklibdrawerbutton', linklibdrawerbutton);

    var linkdrawerbutton = new KupuButton('kupu-linkdrawer-button',
                                          opendrawer('linkdrawer'));
    kupu.registerTool('linkdrawerbutton', linkdrawerbutton);

    var tabledrawerbutton = new KupuButton('kupu-tabledrawer-button',
                                           opendrawer('tabledrawer'));
    kupu.registerTool('tabledrawerbutton', tabledrawerbutton);

    // create some drawers, drawers are some sort of popups that appear when a
    // toolbar button is clicked
    var drawertool = new DrawerTool();
    kupu.registerTool('drawertool', drawertool);

    var linklibdrawer = new LinkLibraryDrawer(linktool,
                                              conf.link_xsl_uri,
                                              conf.link_libraries_uri,
                                              conf.search_links_uri);
    drawertool.registerDrawer('linklibdrawer', linklibdrawer);

    var imagelibdrawer = new ImageLibraryDrawer(imagetool,
                                                conf.image_xsl_uri,
                                                conf.image_libraries_uri,
                                                conf.search_images_uri);
    drawertool.registerDrawer('imagelibdrawer', imagelibdrawer);

    var linkdrawer = new LinkDrawer('kupu-linkdrawer', linktool);
    drawertool.registerDrawer('linkdrawer', linkdrawer);

    var tabledrawer = new TableDrawer('kupu-tabledrawer', tabletool);
    drawertool.registerDrawer('tabledrawer', tabledrawer);

    // register some cleanup filter
    // remove tags that aren't in the XHTML DTD
    var nonxhtmltagfilter = new NonXHTMLTagFilter();
    kupu.registerFilter(nonxhtmltagfilter);

    return kupu;
}
