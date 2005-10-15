/**********************************************************************************
* $URL$
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
  private QualifierIterator parentIter;
  private String referenceName;
  private String description;
  private Type qualifierType;

  public QualifierImpl() {
  }

  public Id getId() throws org.osid.authorization.AuthorizationException {
    return id;
  }

  public String getReferenceName() throws org.osid.authorization.AuthorizationException {
    return referenceName;
  }

  public String getDescription() throws org.osid.authorization.AuthorizationException {
    return description;
  }

  public boolean isParent() throws org.osid.authorization.AuthorizationException {
    boolean returnValue = false;
    ArrayList a = new ArrayList();
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

  public void updateDescription(String parm1) throws org.osid.authorization.AuthorizationException {
    this.description = description;
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
    ArrayList a = new ArrayList();
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