/**********************************************************************************
*
 * Copyright (c) 2003, 2004, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/
package edu.indiana.lib.twinpeaks.util;

public class DomException extends Exception {

  /**
   * Thrown to indicate a DOM processing issue
   * @param text Explainatory text
   */
  public DomException(String text) {
    super(text);
  }
  /**
   * Thrown to indicate a DOM processing issue
   */
  public DomException() {
    super("");
  }
}
