/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.osid.authz.impl;

import org.osid.authorization.Qualifier;
import org.osid.shared.Id;
import org.osid.authorization.AuthorizationException;
import org.osid.shared.Type;
import org.osid.authorization.QualifierIterator;
import java.util.ArrayList;

public class QualifierImpl implements Qualifier {
  private Id id;

private QualifierIterator childrenIter;
  public QualifierIterator getChildrenIter() {
	return childrenIter;
}

public void setChildrenIter(QualifierIterator childrenIter) {
	this.childrenIter = childrenIter;
}
private QualifierIterator parentIter;
  private String referenceName;

private String description;
  private Type qualifierType;



public QualifierImpl() {
  }

  public Id getId() throws org.osid.authorization.AuthorizationException {
    return id;
  }

  public void setId(Id id) {
		this.id = id;
  }
  
  public String getReferenceName() throws org.osid.authorization.AuthorizationException {
    return referenceName;
  }
  
  public void setReferenceName(String referenceName) {
		this.referenceName = referenceName;
	}

  public String getDescription() throws org.osid.authorization.AuthorizationException {
    return description;
  }

  public boolean isParent() throws org.osid.authorization.AuthorizationException {
    boolean returnValue = false;
    //ArrayList a = new ArrayList();
    while (childrenIter.hasNextQualifier()){
      Id i = (Id)childrenIter.nextQualifier();
      if (i.equals(this.id)){
        returnValue = true;
        break;
      }
    }
    return returnValue;
  }

  public Type getQualifierType() throws org.osid.authorization.AuthorizationException {
    return qualifierType;
  }

  public void setQualifierType(Type qualifierType) {
		this.qualifierType = qualifierType;
	}
  
  public void updateDescription(String parm1) throws org.osid.authorization.AuthorizationException {
    this.description = parm1;
  }

  public void addParent(Id parm1) throws org.osid.authorization.AuthorizationException {
    ArrayList a = new ArrayList();
    while (parentIter.hasNextQualifier()){
      Id i = (Id)parentIter.nextQualifier();
      a.add(i);
    }
    a.add(parm1);
    this.parentIter = new QualifierIteratorImpl(a);
  }

  public void removeParent(Id parm1) throws org.osid.authorization.AuthorizationException {
    ArrayList a = new ArrayList();
    while (parentIter.hasNextQualifier()){
      Id i = (Id)parentIter.nextQualifier();
      if (!parm1.equals(i))
        a.add(i);
    }
    this.parentIter = new QualifierIteratorImpl(a);
  }

  public void changeParent(Id oldParent, Id newParent) throws org.osid.authorization.AuthorizationException {
    ArrayList a = new ArrayList();
    while (parentIter.hasNextQualifier()){
      Id i = (Id)parentIter.nextQualifier();
      if (!oldParent.equals(i))
        a.add(i);
      else
        a.add(newParent); // replace oldParent with newParent
    }
    this.parentIter = new QualifierIteratorImpl(a);
  }

  public boolean isChildOf(Id parent) throws org.osid.authorization.AuthorizationException {
    boolean returnValue = false;
    //ArrayList a = new ArrayList();
    while (parentIter.hasNextQualifier()){
      Id i = (Id)parentIter.nextQualifier();
      if (parent.equals(i)){
        returnValue = true;
        break;
      }
    }
    return returnValue;
  }

  public boolean isDescendantOf(Id parent) throws org.osid.authorization.AuthorizationException {
    throw new AuthorizationException(AuthorizationException.UNIMPLEMENTED);
  }

  public QualifierIterator getChildren() throws org.osid.authorization.AuthorizationException {
    return childrenIter;
  }
  public QualifierIterator getParents() throws org.osid.authorization.AuthorizationException {
    return parentIter;
  }

}
