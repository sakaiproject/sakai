/**********************************************************************************
*
* $Header$
*
***********************************************************************************
*
* Copyright (c) 2005 University of Cambridge
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
package uk.ac.cam.caret.sakai.rwiki.utils;

/**
 * @author andrew
 *
 */
//FIXME: Tool

public class XmlEscaper {

    public static final char HIGHEST_CHARACTER = '>';
    public static final char[][] specialChars = new char[HIGHEST_CHARACTER + 1][];
    static {
        specialChars['>'] = "&gt;".toCharArray();
        specialChars['<'] = "&lt;".toCharArray();
        specialChars['&'] = "&amp;".toCharArray();
        specialChars['"'] = "&#034;".toCharArray();
        specialChars['\''] = "&#039;".toCharArray();
    }
    
    public static String xmlEscape(String toEscape) {
        char[] chars = toEscape.toCharArray();
        int lastEscapedBefore = 0;
        StringBuffer escapedString = null;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] <= HIGHEST_CHARACTER) {
                char[] escapedPortion = specialChars[chars[i]];
                if (escapedPortion != null) {
                    if (lastEscapedBefore == 0) {
                        escapedString = new StringBuffer(chars.length + 5);
                    }
                    if (lastEscapedBefore < i ) {
                        escapedString.append(chars, lastEscapedBefore, i - lastEscapedBefore);
                    }
                    lastEscapedBefore = i + 1;
                    escapedString.append(escapedPortion);                  
                }
            }
        }
        
        if (lastEscapedBefore == 0) {
            return toEscape;
        }
        
        if (lastEscapedBefore < chars.length) {
            escapedString.append(chars, lastEscapedBefore, chars.length - lastEscapedBefore);
        }
        
        return escapedString.toString();
    }
    
}
