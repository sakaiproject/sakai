
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

/*
 * Created on Aug 1, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.sakaiproject.tool.assessment.ui.bean.delivery;


/**
 * @author casong
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 *
 * Used to be org.navigoproject.ui.web.asi.delivery.DisplayAssetsForm.java
 */
import java.io.Serializable;

import java.util.ArrayList;

/**
 * DOCUMENTATION PENDING
 *
 * @author $author$
 * @version $Id: DisplayAssetsBean.java,v 1.2 2004/10/01 15:56:55 esmiley.stanford.edu Exp $
 */
public class DisplayAssetsBean
  implements Serializable
{
  private ArrayList assets;

  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = -7473000970710480526L;

  /**
   * Creates a new DisplayAssetsBean object.
   */
  public DisplayAssetsBean()
  {
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public ArrayList getAssets()
  {
    return assets;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param assets DOCUMENTATION PENDING
   */
  public void setAssets(ArrayList assets)
  {
    this.assets = assets;
  }
}
