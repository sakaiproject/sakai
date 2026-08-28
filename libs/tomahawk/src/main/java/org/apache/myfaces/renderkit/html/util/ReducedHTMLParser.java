/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.renderkit.html.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A class which detects the open/close tags in an HTML document and reports
 * them to a listener class.
 * <p>
 * This is unfortunately necessary when using JSF with JSP, as tags in the body
 * of the document can need to output commands into the document at points
 * earlier than the tag occurred (particularly into the document HEAD section).
 * This can only be implemented by buffering the response and post-processing
 * it to find the relevant HTML tags and modifying the buffer as needed.
 * <p>
 * This class tries to do the parsing as quickly as possible; many of the
 * details of HTML are not relevant for the purposes this class is used for.
 * 
 * @version $Revision: 673833 $ $Date: 2008-07-03 16:58:05 -0500 (Thu, 03 Jul 2008) $
 */
public class ReducedHTMLParser
{
    // IMPLEMENTATION NOTE:
    //
    // Many of the methods on this class are package-scope. This is intended
    // solely for the purpose of unit-testing. This class does not expect
    // other classes in this package to access its methods.

    private static final Log log = LogFactory.getLog(ReducedHTMLParser.class);

    public static final int BODY_TAG = 0;
    public static final int HEAD_TAG = 1;
    public static final int SCRIPT_TAG = 2;

    private static final int STATE_READY = 0;
    private static final int STATE_IN_COMMENT = 1;
    private static final int STATE_IN_TAG = 2;
    private static final int STATE_IN_MARKED_SECTION = 3;
    private static final int STATE_EXPECTING_ETAGO = 4;

    private int _offset;
    private int _lineNumber;
    private CharSequence _seq;
    private CallbackListener _listener;

    public static void parse(CharSequence seq, CallbackListener l)
    {
        new ReducedHTMLParser(seq, l).parse();
    }

    /**
     * Constructor, package-scope for unit testing.
     *
     * @param s is the sequence of chars to parse.
     * @param l is the listener to invoke callbacks on.
     */
    ReducedHTMLParser(CharSequence s, CallbackListener l)
    {
        _seq = s;
        _listener = l;
    }

    /**
     * Return true if there are no more characters to parse.
     */
    boolean isFinished()
    {
        return _offset >= _seq.length();
    }

    int getCurrentLineNumber()
    {
         return _lineNumber;
    }

    /**
     * Advance the current parse position over any whitespace characters.
     */
    void consumeWhitespace()
    {
        boolean crSeen = false;

        while (_offset < _seq.length())
        {
            char c = _seq.charAt(_offset);
            if (!Character.isWhitespace(c))
            {
                break;
            }

            // Track line number for error messages.
            if (c == '\r')
            {
                ++_lineNumber;
                crSeen = true;
            }
            else if ((c == '\n') && !crSeen)
            {
                ++_lineNumber;
            }
            else
            {
                crSeen = false;
            }

            ++_offset;
        }
    }

    /**
     * Eat up a sequence of non-whitespace characters and return them.
     */
    String consumeNonWhitespace()
    {
        int wordStart = _offset;
        while (_offset < _seq.length())
        {
            char c = _seq.charAt(_offset);
            if (Character.isWhitespace(c))
            {
                break;
            }
            ++_offset;
        }
        if (wordStart == _offset)
        {
            return null;
        }
        else
        {
            return _seq.subSequence(wordStart, _offset).toString();
        }
    }

    /**
     * If the next chars in the input sequence exactly match the specified
     * string then skip over them and return true.
     * <p>
     * If there is not a match then leave the current parse position
     * unchanged and return false.
     *
     * @param s is the exact string to match.
     * @return true if the input contains exactly the param s
     */
    boolean consumeMatch(String s)
    {
        if (_offset + s.length() > _seq.length())
        {
            // seq isn't long enough to contain the specified string
            return false;
        }

        int i = 0;
        while (i < s.length())
        {
            if (_seq.charAt(_offset+i) == s.charAt(i))
            {
                ++i;
            }
            else
            {
                return false;
            }
        }

        _offset += i;
        return true;
    }

    /**
     * Eat up a sequence of chars which form a valid XML element name.
     * <p>
     * TODO: implement this properly in compliance with spec
     */
    String consumeElementName()
    {
        consumeWhitespace();
        int nameStart = _offset;
        while (!isFinished())
        {
            boolean ok = false;
            char c = _seq.charAt(_offset);
            if (Character.isLetterOrDigit(_seq.charAt(_offset)))
            {
                ok = true;
            }
            else if (c == '_')
            {
                ok = true;
            }
            else if (c == '-')
            {
                ok = true;
            }
            else if (c == ':')
            {
                ok = true;
            }

            if (!ok)
            {
                break;
            }

            ++_offset;
        }

        if (nameStart == _offset)
        {
            return null;
        }
        else
        {
            return _seq.subSequence(nameStart, _offset).toString();
        }
    }

    /**
     * Eat up a sequence of chars which form a valid XML attribute name.
     * <p>
     * TODO: implement this properly in compliance with spec
     */
    String consumeAttrName()
    {
        // for now, assume elements and attributes have same rules
        return consumeElementName();
    }

    /**
     * Eat up a string which is terminated with the specified quote
     * character. This means handling escaped quote chars within the
     * string.
     * <p>
     * This method assumes that the leading quote has already been
     * consumed.
     */
    String consumeString(char quote)
    {
        // TODO: should we consider a string to be terminated by a newline?
        // that would help with runaway strings but I think that multiline
        // strings *are* allowed...
        //
        // TODO: detect newlines within strings and increment lineNumber.
        // This isn't so important, though; they aren't common and being a
        // few lines out in an error message isn't serious either.
        StringBuffer stringBuf = new StringBuffer();
        boolean escaping = false;
        while (!isFinished())
        {
            char c = _seq.charAt(_offset);
            ++_offset;
            if (c == quote)
            {
                if (!escaping)
                {
                    break;
                }
                else
                {
                    stringBuf.append(c);
                    escaping = false;
                }
            }
            else if (c == '\\')
            {
                if (escaping)
                {
                    // append a real backslash
                    stringBuf.append(c);
                    escaping = false;
                }
                else
                {
                    escaping = true;
                }
            }
            else
            {
                if (escaping)
                {
                    stringBuf.append('\\');
                    escaping = false;                    
                }

                stringBuf.append(c);
            }
        }
        return stringBuf.toString();
    }

    /**
     * Assuming we have already encountered "attrname=", consume the
     * value part of the attribute definition. Note that unlike XML,
     * HTML doesn't have to quote its attribute values.
     *
     * @return the attribute value. If the attr-value was quoted,
     * the returned value will not include the quote chars.
     */
    String consumeAttrValue()
    {
        consumeWhitespace();

        if (consumeMatch("'"))
        {
            return consumeString('\'');
        }
        else if (consumeMatch("\""))
        {
            return consumeString('"');
        }
        else
        {
            return consumeNonWhitespace();
        }
    }

    /**
     * Discard all characters in the input until one in the specified
     * string (character-set) is found.
     *
     * @param s is a set of characters that should not be discarded.
     */
    void consumeExcept(String s)
    {
        boolean crSeen = false;

        while (_offset < _seq.length())
        {
            char c = _seq.charAt(_offset);
            if (s.indexOf(c) >= 0)
            {
                // char is in the exception set
                return;
            }

            // Track line number for error messages.
            if (c == '\r')
            {
                ++_lineNumber;
                crSeen = true;
            }
            else if ((c == '\n') && !crSeen)
            {
                ++_lineNumber;
            }
            else
            {
                crSeen = false;
            }

            ++_offset;
        }
    }

    /**
     * Process the entire input buffer, invoking callbacks on the listener
     * object as appropriate.
     */
    void parse()
    {
        int state = STATE_READY;

        int currentTagStart = -1;
        String currentTagName = null;

        _lineNumber = 1;
        _offset = 0;
        int lastOffset = _offset -1;
        while (_offset < _seq.length())
        {
            // Sanity check; each pass through this loop must increase the offset.
            // Failure to do this means a hang situation has occurred.
            if (_offset <= lastOffset)
            {
                // throw new RuntimeException("Infinite loop detected in ReducedHTMLParser");
                log.error("Infinite loop detected in ReducedHTMLParser; parsing skipped."+
                          " Surroundings: '" + getTagSurroundings() +"'.");
                //return;
            }
            lastOffset = _offset;

            if (state == STATE_READY)
            {
                // in this state, nothing but "<" has any significance
                consumeExcept("<");
                if (isFinished())
                {
                    break;
                }

                if (consumeMatch("<!--"))
                {
                    // Note that whitespace is *not* permitted in <!--
                    state = STATE_IN_COMMENT;
                }
                else if (consumeMatch("<!["))
                {
                    // Start of a "marked section", eg "<![CDATA" or
                    // "<![INCLUDE" or "<![IGNORE". These always terminate
                    // with "]]>"
                    log.debug("Marked section found at line " + getCurrentLineNumber()+". "+
                              "Surroundings: '" + getTagSurroundings() +"'.");
                    state = STATE_IN_MARKED_SECTION;
                }
                else if (consumeMatch("<!DOCTYPE"))
                {
                    log.debug("DOCTYPE found at line " + getCurrentLineNumber());
                    // we don't need to actually do anything here; the
                    // tag can't contain a bare "<", so the first "<"
                    // indicates the start of the next real tag.
                    //
                    // TODO: Handle case where the DOCTYPE includes an internal DTD. In
                    // that case there *will* be embedded < chars in the document. However
                    // that's very unlikely to be used in a JSF page, so this is pretty low
                    // priority.
                }
                else if (consumeMatch("<?"))
                {
                    // xml processing instruction or <!DOCTYPE> tag
                    // we don't need to actually do anything here; the
                    // tag can't contain a bare "<", so the first "<"
                    // indicates the start of the next real tag.
                    log.debug("PI found at line " + getCurrentLineNumber());
                }
                else if (consumeMatch("</"))
                {
                    if (!processEndTag())
                    {
                        // message already logged
                        return;
                    }

                    // stay in state READY
                    state = STATE_READY;
                }
                else if (consumeMatch("<"))
                {
                    // We can't tell the user that the tag has closed until after we have
                    // processed any attributes and found the real end of the tag. So save
                    // the current info until the end of this tag.
                    currentTagStart = _offset - 1;
                    currentTagName = consumeElementName();
                    if (currentTagName == null)
                    {
                        log.warn("Invalid HTML; bare lessthan sign found at line "
                                 + getCurrentLineNumber() + ". "+
                                 "Surroundings: '" + getTagSurroundings() +"'.");
                        // remain in STATE_READY; this isn't really the start of
                        // an xml element.
                    }
                    else
                    {
                        state = STATE_IN_TAG;
                    }
                }
                else
                {
                    // should never get here
                    throw new Error("Internal error at line " + getCurrentLineNumber());
                }

                continue;
            }

            if (state == STATE_IN_COMMENT)
            {
                // TODO: handle "--  >", which is a valid way to close a
                // comment according to the specs.

                // in this state, nothing but "--" has any significance
                consumeExcept("-");
                if (isFinished())
                {
                    break;
                }

                if (consumeMatch("-->"))
                {
                    state = STATE_READY;
                }
                else
                {
                    // false call; hyphen is not end of comment
                    consumeMatch("-");
                }

                continue;
            }

            if (state == STATE_IN_TAG)
            {
                consumeWhitespace();

                if (consumeMatch("/>"))
                {
                    // ok, end of element
                    state = STATE_READY;
                    closedTag(currentTagStart, _offset, currentTagName);

                    // and reset vars just in case...
                    currentTagStart = -1;
                    currentTagName = null;
                }
                else if (consumeMatch(">"))
                {
                    if (currentTagName.equalsIgnoreCase("script")
                        || currentTagName.equalsIgnoreCase("style"))
                    {
                        // We've just started a special tag which can contain anything except
                        // the ETAGO marker ("</"). See
                        // http://www.w3.org/TR/REC-html40/appendix/notes.html#notes-specifying-data
                        state = STATE_EXPECTING_ETAGO;
                    }
                    else
                    {
                        state = STATE_READY;
                    }

                    // end of open tag, but not end of element
                    openedTag(currentTagStart, _offset, currentTagName);

                    // and reset vars just in case...
                    currentTagStart = -1;
                    currentTagName = null;
                }
                else
                {
                    // xml attribute
                    String attrName = consumeAttrName();
                    if (attrName == null)
                    {
                        // Oops, we found something quite unexpected in this tag.
                        // The best we can do is probably to drop back to looking
                        // for "/>", though that does risk us misinterpreting the
                        // contents of an attribute's associated string value.
                        log.warn("Invalid tag found: unexpected input while looking for attr name or '/>'"
                                 + " at line " + getCurrentLineNumber()+". "+
                                 "Surroundings: '" + getTagSurroundings() +"'.");
                        state = STATE_EXPECTING_ETAGO;
                        // and consume one character
                        ++_offset;
                    }
                    else
                    {
                        consumeWhitespace();

                        // html can have "stand-alone" attributes with no following equals sign
                        if (consumeMatch("="))
                        {
                            consumeAttrValue();
                        }
                    }
                }

                continue;
            }

            if (state == STATE_IN_MARKED_SECTION)
            {
                // in this state, nothing but "]]>" has any significance
                consumeExcept("]");
                if (isFinished())
                {
                    break;
                }

                if (consumeMatch("]]>"))
                {
                    state = STATE_READY;
                }
                else
                {
                    // false call; ] is not end of cdata section
                    consumeMatch("]");
                }

                continue;
            }

            if (state == STATE_EXPECTING_ETAGO)
            {
                // The term "ETAGO" is the official spec term for "</".
                consumeExcept("<");
                if (isFinished())
                {
                    log.debug("Malformed input page; input terminated while tag not closed.");
                    break;
                }

                if (consumeMatch("</"))
                {
                    if (!processEndTag())
                    {
                        return;
                    }
                    state = STATE_READY;
                }
                else
                {
                    // false call; < does not start an ETAGO
                    consumeMatch("<");
                }

                continue;
            }
        }
    }

    /**
     * Get details about malformed HTML tag.
     *
     * @return Tag surroundings.
     */
    private String getTagSurroundings()
    {
        int maxLength = 30;
        int end = _seq.length();
        if (end - _offset > maxLength) {
            end = _offset + maxLength;
        }
        return _seq.subSequence(_offset, end).toString();
    }

    /**
     * Invoked when "&lt;/" has been seen in the input, this method
     * handles the parsing of the end tag and the invocation of the
     * appropriate callback method.
     *
     * @return true if the tag was successfully parsed, and false
     * if there was a fatal parsing error.
     */
    private boolean processEndTag()
    {
        int tagStart = _offset - 2;
        String tagName = consumeElementName();
        consumeWhitespace();
        if (!consumeMatch(">"))
        {
            // log details about malformed end tag
            log.error("Malformed end tag '" + tagName + "' at line " + getCurrentLineNumber()
                      + "; skipping parsing. Surroundings: '" + getTagSurroundings() +"'.");
            return false;
        }


        // inform user that the tag has been closed
        closedTag(tagStart, _offset, tagName);

        // We can't verify that the tag names balance because this is HTML
        // we are processing, not XML.
        return true;
    }

    /**
     * Invoke a callback method to inform the listener that we have found a start tag.
     *
     * @param startOffset
     * @param endOffset
     * @param tagName
     */
    void openedTag(int startOffset, int endOffset, String tagName)
    {
        //log.debug("Found open tag at " + startOffset + ":" + endOffset + ":" + tagName);

        if ("head".equalsIgnoreCase(tagName))
        {
            _listener.openedStartTag(startOffset, HEAD_TAG);
            _listener.closedStartTag(endOffset, HEAD_TAG);
        }
        else if ("body".equalsIgnoreCase(tagName))
        {
            _listener.openedStartTag(startOffset, BODY_TAG);
            _listener.closedStartTag(endOffset, BODY_TAG);
        }
        else if ("script".equalsIgnoreCase(tagName))
        {
            _listener.openedStartTag(startOffset, SCRIPT_TAG);
            _listener.closedStartTag(endOffset, SCRIPT_TAG);
        }
    }

    void closedTag(int startOffset, int endOffset, String tagName)
    {
        //log.debug("Found close tag at " + startOffset + ":" + endOffset + ":" + tagName);

        if ("head".equalsIgnoreCase(tagName))
        {
            _listener.openedEndTag(startOffset, HEAD_TAG);
            _listener.closedEndTag(endOffset, HEAD_TAG);
        }
        else if ("body".equalsIgnoreCase(tagName))
        {
            _listener.openedEndTag(startOffset, BODY_TAG);
            _listener.closedEndTag(endOffset, BODY_TAG);
        }
        else if ("script".equalsIgnoreCase(tagName))
        {
            _listener.openedEndTag(startOffset, SCRIPT_TAG);
            _listener.closedEndTag(endOffset, SCRIPT_TAG);
        }
    }
}
