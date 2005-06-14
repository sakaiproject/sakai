package org.sakaiproject.tool.assessment.osid.authz.impl;

import org.osid.authorization.Function;
import org.osid.shared.Id;
import org.osid.shared.Type;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class FunctionImpl implements Function {
  private Id id;
  private String referenceName;
  private String description;
  private Type functionType;
  private Id qualifierHierarchyId;

  public FunctionImpl() {
  }
  public FunctionImpl(String referenceName, String description,
                      Type functionType, Id qualifierHierarchyId) {
    this.referenceName = referenceName;
    this.description = description;
    this.functionType = functionType;
    this.qualifierHierarchyId = qualifierHierarchyId;
  }
  public Id getId(){
    return id;
  }
  public String getReferenceName() {
    return referenceName;
  }
  public String getDescription(){
    return description;
  }
  public Type getFunctionType(){
    return functionType;
  }
  public Id getQualifierHierarchyId(){
    return qualifierHierarchyId;
  }
  public void updateDescription(String parm1){
    this.description = parm1;
  }

}