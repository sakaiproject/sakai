/**********************************************************************************
* $HeadURL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.assessment.util;


/**
 * DOCUMENTATION PENDING
 *
 * @author $author$
 * @version $Id$
 */
public class LabelValue
{
  private String label = "";
  private String value = "";

  /**
   * Creates a new LabelValue object.
   *
   * @param label DOCUMENTATION PENDING
   * @param value DOCUMENTATION PENDING
   */
  public LabelValue(String label, String value)
  {
    this.label = label;
    this.value = value;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param label DOCUMENTATION PENDING
   */
  public void setLabel(String label)
  {
    this.label = label;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getLabel()
  {
    return label;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param value DOCUMENTATION PENDING
   */
  public void setValue(String value)
  {
    this.value = value;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getValue()
  {
    return value;
  }
}
