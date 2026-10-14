/*****************************************************************************
 *
 * Copyright (c) 2003-2005 Kupu Contributors. All rights reserved.
 *
 * This software is distributed under the terms of the Kupu
 * License. See LICENSE.txt for license text. For a list of Kupu
 * Contributors see CREDITS.txt.
 *
 *****************************************************************************/
// $Id: kupuloggers.js 928511 2010-03-28 22:53:14Z lu4242 $


//----------------------------------------------------------------------------
// Loggers
//
//  Loggers are pretty simple classes, that should have 1 method, called 
//  'log'. This is called with 2 arguments, the first one is the message to
//  log and the second is the severity, which can be 0 or some other false
//  value for debug messages, 1 for warnings and 2 for errors (the loggers
//  are allowed to raise an exception if that happens).
//
//----------------------------------------------------------------------------

function DebugLogger() {
    /* Alert all messages */
    
    this.log = function(message, severity) {
        /* log a message */
        if (severity > 1) {
            alert("Error: " + message);
        } else if (severity == 1) {
            alert("Warning: " + message);
        } else {
            alert("Log message: " + message);
        }
    };
}

function PlainLogger(debugelid, maxlength) {
    /* writes messages to a debug tool and throws errors */

    this.debugel = getFromSelector(debugelid);
    this.maxlength = maxlength;
    
    this.log = function(message, severity) {
        /* log a message */
        if (severity > 1) {
            throw message;
        } else {
            if (this.maxlength) {
                if (this.debugel.childNodes.length > this.maxlength - 1) {
                    this.debugel.removeChild(this.debugel.childNodes[0]);
                }
            }
            var now = new Date();
            var time = now.getHours() + ':' + now.getMinutes() + ':' + now.getSeconds();
            
            var div = document.createElement('div');
            var text = document.createTextNode(time + ' - ' + message);
            div.appendChild(text);
            this.debugel.appendChild(div);
        }
    };
}

function DummyLogger() {
    this.log = function(message, severity) {
        if (severity > 1) {
            throw message;
        }
    };
}
