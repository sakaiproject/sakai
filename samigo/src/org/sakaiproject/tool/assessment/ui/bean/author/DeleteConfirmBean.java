
/*
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
*/

package org.sakaiproject.tool.assessment.ui.bean.author;

import java.io.Serializable;

/**
 * <p>
 * Title: sakaiproject.org
 * </p>
 *
 * <p>
 * Description: AAM - form for confirming delete media.
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 *
 * Used to be org.navigoproject.ui.web.form.edit.DeleteConfirmForm.java
 *
 * @author Qingru Zhang
 * @version 1.0
 */
public class DeleteConfirmBean
  implements Serializable
{
  private String name;

  /**
   * Creates a new DeleteConfirmBean object.
   */
  public DeleteConfirmBean()
  {
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getName()
  {
    return this.name;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param name DOCUMENTATION PENDING
   */
  public void setName(String name)
  {
    if(name != null)
    {
      this.name = name;
    }
  }
}
