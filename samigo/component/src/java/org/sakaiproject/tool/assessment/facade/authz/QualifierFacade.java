/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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
package org.sakaiproject.tool.assessment.facade.authz;

import java.util.ArrayList;
import java.util.Set;

import org.osid.shared.Id;
import org.osid.shared.Type;

import org.sakaiproject.tool.assessment.data.dao.authz.QualifierData;
import org.sakaiproject.tool.assessment.data.ifc.authz.QualifierIfc;
import org.sakaiproject.tool.assessment.facade.TypeFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.services.PersistenceService;

public class QualifierFacade implements QualifierIfc {
  private Id id;
  private QualifierIteratorFacade childrenIter;
  private QualifierIteratorFacade parentIter;
  private String referenceName;
  private String description;
  private Type qualifierType;
  private QualifierIfc data;

//  private String qualifierId;
  private long qualifierId;
  private String displayName;
  private String qualifierTypeId;
  private Set childSet;
  private Set parentSet;

  public QualifierFacade() {
    this.data = new QualifierData(); //should do a create method later
  }

  public QualifierFacade(
//      String qualifierId, String referenceName, String displayName, String description, String qualifierTypeId){
    long qualifierId, String referenceName, String displayName, String description, String qualifierTypeId){
    this.data = new QualifierData(); //should do a create method later
    this.referenceName = referenceName;
    setQualifierId(qualifierId);
    setReferenceName(referenceName);
    setDisplayName(displayName);
    setDescription(description);
    setQualifierTypeId(qualifierTypeId);
  }

  public QualifierFacade(QualifierIfc data){
    this.data = data;
    this.id = getId();
/*    this.childrenIter = getChildren();
    this.parentIter = getParents();*/
    this.referenceName= getReferenceName();
    this.description = getDescription();
    this.qualifierType = getQualifierType();
    this.qualifierId = getQualifierId();
    this.qualifierTypeId = getQualifierTypeId();
    this.displayName= getDisplayName();
  }

  public Id getId()  {
    return id;
  }

  public String getReferenceName() {
    return referenceName;
  }

  public void setReferenceName(String referenceName) {
    this.referenceName = referenceName;
    data.setReferenceName(referenceName);
  }

  public String getDescription() {
    return data.getDescription();
  }

  public void setDescription(String description) {
    this.description = description;
    data.setDescription(description);
  }

  public boolean isParent() {
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

  public Type getQualifierType(){
    TypeFacadeQueriesAPI typeFacadeQueries = PersistenceService.getInstance().getTypeFacadeQueries();
    return typeFacadeQueries.getTypeById(new Long(this.data.getQualifierTypeId()));
  }

  public void updateDescription(String description){
    this.description = description;
  }

  public void addParent(Id parm1) {
    ArrayList a = new ArrayList();
    while (parentIter.hasNextQualifier()){
      Id i = (Id)parentIter.nextQualifier();
      a.add(i);
    }
    a.add(parm1);
    this.parentIter = new QualifierIteratorFacade(a);
  }

  public void removeParent(Id parm1) {
    ArrayList a = new ArrayList();
    while (parentIter.hasNextQualifier()){
      Id i = (Id)parentIter.nextQualifier();
      if (!parm1.equals(i))
        a.add(i);
    }
    this.parentIter = new QualifierIteratorFacade(a);
  }

  public void changeParent(Id oldParent, Id newParent) {
    ArrayList a = new ArrayList();
    while (parentIter.hasNextQualifier()){
      Id i = (Id)parentIter.nextQualifier();
      if (!oldParent.equals(i))
        a.add(i);
      else
        a.add(newParent); // replace oldParent with newParent
    }
    this.parentIter = new QualifierIteratorFacade(a);
  }

  public boolean isChildOf(Id parent)  {
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

/*  public QualifierIteratorFacade getChildren() {
    return PersistenceService.getInstance().getAuthorizationFacadeQueries().getQualifierChildren(data.getQualifierId());
  }
  public QualifierIteratorFacade getParents() {
    return PersistenceService.getInstance().getAuthorizationFacadeQueries().getQualifierParents(data.getQualifierId());
  }*/

  public QualifierIfc getData(){
    return this.data;
  }

//  public String getQualifierId() {
  public long getQualifierId() {
    return data.getQualifierId();
  }

//  public void setQualifierId(String id) {
  public void setQualifierId(long id) {
    this.qualifierId = id;
    data.setQualifierId(id);
  }

  public String getDisplayName() {
    return data.getDisplayName();
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
    data.setDisplayName(displayName);
  }

  public String getQualifierTypeId() {
    return data.getQualifierTypeId();
  }

  public void setQualifierTypeId(String qualifierTypeId) {
    this.qualifierTypeId = qualifierTypeId;
    data.setQualifierTypeId(qualifierTypeId);
  }

/*  public Set getChildSet() {
    return data.getChildSet();
  }

  public void setChildSet(Set childSet) {
    this.childSet = childSet;
    data.setChildSet(childSet);
  }

  public Set getParentSet() {
    return data.getParentSet();
  }

  public void setParentSet(Set parentSet) {
    this.parentSet = parentSet;
    data.setParentSet(parentSet);
  }
*/
}
