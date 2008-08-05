/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/business/entity/assessment/model/IPMaskData.java $
 * $Id: IPMaskData.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright 2004, 2005, 2006 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/


package org.sakaiproject.tool.assessment.business.entity.assessment.model;

import java.io.Serializable;
import java.net.InetAddress;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This holds the ip access model, and a list of allowed or blocked ip
 * addresses, based on the model.  Do we need a choice that combines allowed
 * and blocked in some way?  (i.e. everything on the allowed list except the
 * blocked ones, or nothing on the blocked list except the allowed ones?)
 *
 * @author Rachel Gollub
 * @author Ed Smiley
 */
public class IPMaskData implements Serializable
{
	/** Use serialVersionUID for interoperability. */
	private final static long serialVersionUID = -1090852048737428722L;

  private String ipAccessType;
  private Collection allowedAddresses;
  private Collection blockedAddresses;

  /**
   * Creates a new IPMaskData object.
   */
  public IPMaskData()
  {
    allowedAddresses = new ArrayList();
    blockedAddresses = new ArrayList();
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getIPAccessType()
  {
    return ipAccessType;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pipAccessType DOCUMENTATION PENDING
   */
  public void setIPAccessType(String pipAccessType)
  {
    ipAccessType = pipAccessType;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Collection getAllowedAddresses()
  {
    return allowedAddresses;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pallowedAddresses DOCUMENTATION PENDING
   */
  public void setAllowedAddresses(Collection pallowedAddresses)
  {
    allowedAddresses = pallowedAddresses;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param paddress DOCUMENTATION PENDING
   */
  public void addAllowedAddress(InetAddress paddress)
  {
    allowedAddresses.add(paddress);
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param paddress DOCUMENTATION PENDING
   */
  public void removeAllowedAddress(InetAddress paddress)
  {
    allowedAddresses.remove(paddress);
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Collection getBlockedAddresses()
  {
    return blockedAddresses;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param pblockedAddresses DOCUMENTATION PENDING
   */
  public void setBlockedAddresses(Collection pblockedAddresses)
  {
    blockedAddresses = pblockedAddresses;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param paddress DOCUMENTATION PENDING
   */
  public void addBlockedAddress(InetAddress paddress)
  {
    blockedAddresses.add(paddress);
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param paddress DOCUMENTATION PENDING
   */
  public void removeBlockedAddress(InetAddress paddress)
  {
    blockedAddresses.remove(paddress);
  }
}
