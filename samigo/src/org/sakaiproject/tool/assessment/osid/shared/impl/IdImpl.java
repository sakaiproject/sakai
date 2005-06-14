package org.sakaiproject.tool.assessment.osid.shared.impl;

import org.osid.shared.Id;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class IdImpl implements Id {
  private String id;

  public IdImpl(String id){
    this.id = id;
  }

  public IdImpl(Long internalId){
    this.id = internalId.toString();
  }

  public IdImpl(long internalId) {
    this.id = (new Long(internalId)).toString();
  }

  public String getIdString() throws org.osid.shared.SharedException{
    return id;
  }
  public boolean isEqual(Id pId) throws org.osid.shared.SharedException {
    if (pId.getIdString().equals(getIdString()))
      return true;
    else
      return false;
  }

}