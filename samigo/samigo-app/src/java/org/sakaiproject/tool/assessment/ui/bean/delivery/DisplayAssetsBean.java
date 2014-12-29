/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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
 * @version $Id$
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
