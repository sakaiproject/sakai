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
package org.sakaiproject.tool.assessment.data.dao.assessment;
import java.util.Date;

public class ItemType {
  private String itemTypeId;
  private String authority;
  private String domain;
  private String keyword;
  private String description;
  private int status;
  private String createdBy;
  private Date createdDate;
  private String lastModifiedBy;
  private Date lastModifiedDate;

  public ItemType(){
  }

  public ItemType(String authority, String domain,
                  String keyword, String description, int status,
                  String createdBy, Date createdDate,
                  String lastModifiedBy, Date lastModifiedDate) {
    this.authority = authority;
    this.domain = domain;
    this.keyword = keyword;
    this.description = description;
    this.status = status;
    this.createdBy = createdBy;
    this.createdDate = createdDate;
    this.lastModifiedBy = lastModifiedBy;
    this.lastModifiedDate = lastModifiedDate;
  }

  /**
   * Return the itemTypeId from ItemType
   */
  public String getItemTypeId(){
      return this.itemTypeId;
  }

  /**
   * Set the itemTypeID for this ItemType.
   */
  public void setItemTypeId(String itemTypeId){
      this.itemTypeId = itemTypeId;
  }

  public String getAuthority(){
      return this.authority;
  }

  public void setAuthority(String authority){
      this.authority = authority;
  }
  public String getDomain(){
      return this.domain;
  }

  public void setDomain(String domain){
      this.domain = domain;
  }

  /**
   * Return the Name for this ItemType.
   */
  public String getKeyword(){
      return this.keyword;
  }

  /**
   * Set the Name for this ItemType.
   */
  public void setKeyword(String keyword){
      this.keyword = keyword;
  }

  public String getDescription(){
      return this.description;
  }

  public void setDescription(String description){
      this.description = description;
  }

  /**
   * Return the status for this ItemType.
   */
  public int getStatus(){
      return this.status;
  }

  /**
   * Set the status for this ItemType.
   */
  public void setStatus(int status){
      this.status = status;
  }

  /**
   * Return the createdBy for this ItemType.
   */
  public String getCreatedBy(){
      return this.createdBy;
  }

  /**
   * Set the createdBy for this ItemType.
   */
  public void setCreatedBy (String createdBy){
      this.createdBy = createdBy;
  }

  /**
   * Return the createdDate for this ItemType.
   */
  public Date getCreatedDate(){
      return this.createdDate;
  }

  /**
   * Set the createdDate for this ItemType.
   */
  public void setCreatedDate (Date createdDate){
      this.createdDate = createdDate;
  }

  /**
   * Return the lastModifiedBy for this ItemType.
   */
  public String getLastModifiedBy(){
      return this.lastModifiedBy;
  }
  /**
   * Set the lastModifiedBy for this ItemType.
   */
  public void setLastModifiedBy (String lastModifiedBy){
      this.lastModifiedBy = lastModifiedBy;
  }

  /**
   * Return the lastModifiedDate for this ItemType.
   */
  public Date getLastModifiedDate(){
      return this.lastModifiedDate;
  }

  /**
   * Set the lastModifiedDate for this ItemType.
   */
  public void setLastModifiedDate (Date lastModifiedDate){
      this.lastModifiedDate = lastModifiedDate;
  }

}
