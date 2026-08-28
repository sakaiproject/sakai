/*****************************************************************************
 *
 * Copyright (c) 2003-2005 Kupu Contributors. All rights reserved.
 *
 * This software is distributed under the terms of the Kupu
 * License. See LICENSE.txt for license text. For a list of Kupu
 * Contributors see CREDITS.txt.
 *
 *****************************************************************************/
// $Id: kupusaveonpart.js 35854 2006-12-18 15:37:15Z duncan $

function saveOnPart() {
    /* ask the user if (s)he wants to save the document before leaving */
    if (kupu.content_changed && confirm(
            _('You are leaving the editor. Do you want to save your changes?')
            )) {
        kupu.config.reload_src = 0;
        kupu.saveDocument(false, true);
    };
}
