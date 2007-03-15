/**********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
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
/*

Copyright (c) 2000-2003 Board of Trustees of Leland Stanford Jr. University,
all rights reserved.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
STANFORD UNIVERSITY BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Except as contained in this notice, the name of Stanford University shall not
be used in advertising or otherwise to promote the sale, use or other dealings
in this Software without prior written authorization from Stanford University.

*/

package edu.indiana.lib.twinpeaks.util;

import java.io.*;
import java.util.*;

import sun.misc.BASE64Encoder;
import sun.misc.BASE64Decoder;
/**
 * Base 64 encoding/decoding
 */
public class Base64Utils
{
private static org.apache.commons.logging.Log	_log = LogUtils.getLog(Base64Utils.class);

  private Base64Utils() {
  }

  /**
   * Decode Base 64 text
   *
   * @param
   *     data for decoding
   * @return
   *     Decoded bytes
   */
  public static byte decodeToBytes(String data)[] throws IOException {
      return new BASE64Decoder().decodeBuffer(data);
  }

  /**
   * Decode Base 64 text
   *
   * @param
   *    data for decoding
   * @return
   *    Decoded string
   */
  public static String decodeToString(String data) throws IOException {
    return new String(decodeToBytes(data));
  }

  /**
   * Encode provided data
   *
   * @param
   *    data for encoding
   * @return
   *    Base 64 encoded string
   */
  public static String encode(byte data[]) {
    return filter(new BASE64Encoder().encodeBuffer(data));
  }

  /**
   * Encode provided data
   *
   * @param
   *    data for encoding
   * @return
   *    Base 64 encoded string
   */
  public static String encode(String data) {
    return encode(data.getBytes());
  }

  /*
   * Remove carriage return/linefeed
   */
  private static String filter(String encoded) {

   StringBuffer filtered = new StringBuffer(encoded.length());

   for (int i = 0; i < encoded.length(); i++) {

     char ch = encoded.charAt(i);

       if ((ch == '\r') || (ch =='\n')) {
            continue;
       }
       filtered.append(encoded.substring(i, i + 1));
    }
    return filtered.toString();
  }
}
