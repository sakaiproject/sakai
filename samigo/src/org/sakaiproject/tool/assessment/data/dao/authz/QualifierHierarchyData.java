package org.sakaiproject.tool.assessment.data.dao.authz;

import java.io.Serializable;
/**
 * DOCUMENTATION PENDING
 *
 * @author $author$
 * @version $Id: QualifierHierarchyData.java,v 1.4 2005/05/31 19:14:28 janderse.umich.edu Exp $
 */
public class QualifierHierarchyData
  implements Serializable
{
  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = 9180085666292824370L;

//  private String childId;
//  private String parentId;
  private long childId;
  private long parentId;
  private QualifierData child;
  private QualifierData parent;
  
  private Long surrogateKey;
  private Integer lockId;


  public QualifierHierarchyData(){
  }

  public QualifierHierarchyData(String childId, String parentId){
//    this.childId = childId;
//    this.parentId = parentId;
    this.childId = (new Long(childId)).longValue();
    this.parentId = (new Long(parentId)).longValue();
  }

  public QualifierHierarchyData(QualifierData child, QualifierData parent){
      this.child = child;
      this.parent = parent;
      this.childId = child.getQualifierId();
      this.parentId = parent.getQualifierId();
  }

  //public String getChildId()
  public long getChildId()
  {
    return childId;
  }

//  public void setChildId(String childId)
  public void setChildId(long childId)
  {
    this.childId = childId;
  }

  //public String getParentId()
  public long getParentId()
  {
    return parentId;
  }

  //public void setParentId(String parentId)
  public void setParentId(long parentId)
  {
    this.parentId = parentId;
  }

  public QualifierData getChild(){
      return this.child;
  }

  public QualifierData getCParent(){
      return this.parent;
  }

  /**
   * @return Returns the lockId.
   */
  public final Integer getLockId()
  {
    return lockId;
  }
  /**
   * @param lockId The lockId to set.
   */
  public final void setLockId(Integer lockId)
  {
    this.lockId = lockId;
  }
  /**
   * @return Returns the surrogateKey.
   */
  public final Long getSurrogateKey()
  {
    return surrogateKey;
  }
  /**
   * @param surrogateKey The surrogateKey to set.
   */
  public final void setSurrogateKey(Long surrogateKey)
  {
    this.surrogateKey = surrogateKey;
  }

  public boolean equals(Object qualifierHierarchy){
    boolean returnValue = false;
    if (this == qualifierHierarchy)
      returnValue = true;
    if (qualifierHierarchy != null && qualifierHierarchy.getClass()==this.getClass()){
      QualifierHierarchyData q = (QualifierHierarchyData)qualifierHierarchy;
//      if ((this.getChildId()).equals(q.getChildId())
//          && (this.getParentId()).equals(q.getParentId()))
      if ((this.getChildId())== (q.getChildId())
        && (this.getParentId()) == (q.getParentId()))
        returnValue = true;
    }
    return returnValue;
  }

  public int hashCode(){
    //String s = this.childId+":"+(this.parentId).toString();
    String s = new Long(this.childId).toString() + ":" + 
    	new Long(this.parentId).toString();
    return (s.hashCode());
  }
}
