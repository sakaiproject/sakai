/*****************************************************************************
 *
 * Copyright (c) 2003-2004 Kupu Contributors. All rights reserved.
 *
 * This software is distributed under the terms of the Kupu
 * License. See LICENSE.txt for license text. For a list of Kupu
 * Contributors see CREDITS.txt.
 *
 *****************************************************************************/
// $Id: kupucleanupexpressions.js 928511 2010-03-28 22:53:14Z lu4242 $

// WARNING: this file can contain non-ascii characters, *always* make sure your
// text-editor uses 'UTF-8' as the character encoding!!

function CleanupExpressionsTool(actionselectid, performactionbuttonid) {
    /* tool that allows global replace actions on the text contents

        the actions will be presented to the user as a list of some
        sort (e.g. select) of which the user can choose one, when (s)he
        does the system will use a set of regular expressions and 
        replacements on the code, when a match of the expression is
        encountered the match will be replaced with the replacement

        matches and replacements can be configured from the XML, the
        format is:

        <config>
          <cleanup_expressions>
            <set>
              <name>Convert single quotes to curly ones</name>
              <expression>
                <reg>
                  (\W)'
                </reg>
                <replacement>
                  \1‘
                </replacement>
              </expression>
              <expression>
                <reg>
                  '
                </reg>
                <replacement>
                  ’
                </replacement>
              </expression>
            </set>
            <set>
              <name>Reduce whitespace</name>
              <expression>
                <reg>
                  [ ]{2}
                </reg>
                <replacement>
                  &#x20;
                </replacement>
              </expression>
            </set>
          </cleanup_expressions>
        </config>
        
    */
    this.actionselect = document.getElementById(actionselectid);
    this.performactionbutton = document.getElementById(performactionbuttonid);
};

CleanupExpressionsTool.prototype = new KupuTool;

CleanupExpressionsTool.prototype.initialize = function(editor) {
    /* initialize the tool, read the regexp sets into a mapping */
    this.editor = editor;
    // mapping name -> [[regexp, replacement], ...]
    this.expressions = this.generateExpressionsMapping();
    // populate action select
    this.populateActionSelect(this.expressions);
    // add the event handler to the button
    addEventHandler(this.performactionbutton, 'click', this.performAction, 
                    this);
};

CleanupExpressionsTool.prototype.generateExpressionsMapping = function() {
    /* convert the config struct to a somewhat simpler mapping */
    var ret = {};
    var expressions = this.editor.config.cleanup_expressions;
    if (!expressions) {
        // no expressions in the XML config, bail out
        alert('no cleanup expressions configured');
        return ret;
    };
    var sets = expressions.set;
    for (var i=0; i < sets.length; i++) {
        var set = sets[i];
        var name = set.name;
        ret[name] = [];
        var exprs = set.expression;
        // may be list type as well as object
        if (exprs.length) {
          for (var j=0; j < exprs.length; j++) {
              var expr = exprs[j];
              var regexp = expr.reg.strip();
              var replacement = this._prepareReplacement(expr.replacement);
              ret[name].push([regexp, replacement]);
          };
        } else {
            var regexp = exprs.reg.strip();
            var replacement = this._prepareReplacement(exprs.replacement);
            ret[name].push([regexp, replacement]);
        };
    };
    return ret;
};

CleanupExpressionsTool.prototype._prepareReplacement = function(data) {
    /* replace \x([0-9a-f]{2}) escapes with the unicode value of \1 */
    data = data.strip();
    var reg = /\\x([0-9a-f]{2})/g;
    while (true) {
        var match = reg.exec(data);
        if (!match || !match.length) {
            return data;
        };
        data = data.replace(match[0], String.fromCharCode(parseInt(match[1], 16)));
    };
};

CleanupExpressionsTool.prototype.populateActionSelect = function(mapping) {
    /* populate the select with which the user can choose actions */
    for (var name in mapping) {
        var option = document.createElement('option');
        option.value = name;
        option.appendChild(document.createTextNode(name));
        this.actionselect.appendChild(option);
    };
    this.actionselect.style.width = '100%';
};

CleanupExpressionsTool.prototype.performAction = function() {
    /* perform a single action (set of regexps/replacements) to the whole body */
    var action = this.actionselect.options[
                  this.actionselect.selectedIndex].value;
    var sets = this.expressions[action];
    for (var i=0; i < sets.length; i++) {
        var body = this.editor.getInnerDocument().getElementsByTagName('body')[0];
        var nodeiterator = new NodeIterator(body);
        while (true) {
            var current = nodeiterator.next();
            if (!current) {
                break;
            };
            if (current.nodeType == 3) {
                var value = current.nodeValue;
                if (value.strip()) {
                    this.performReplaceOnNode(current, sets[i][0], sets[i][1]);
                };
            };
        };
    };
    alert('Cleanup done');
};

CleanupExpressionsTool.prototype.performReplaceOnNode = function(node, regexp, replacement) {
    /* perform the replacement to the contents of a single node */
    var value = node.nodeValue;
    while (true) {
        var reg = new RegExp(regexp, 'g');
        var match = reg.exec(value);
        if (!match || !match.length) {
            node.nodeValue = value;
            return;
        };
        value = value.replace(match[0], this.createReplacement(replacement, match));
    };
};

CleanupExpressionsTool.prototype.createReplacement = function(pattern, interpolations) {
    /* interpolate '\[0-9]' escapes, they will be replaced with interpolations[n] where
        n is the number behind the backslash */
    var reg = /\\([0-9])/g;
    while (true) {
        var match = reg.exec(pattern);
        if (!match || !match.length) {
            return pattern;
        };
        pattern = pattern.replace(match[0], interpolations[parseInt(match[1])]);
    };
};
