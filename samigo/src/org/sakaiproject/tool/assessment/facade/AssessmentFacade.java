/**********************************************************************************
* $HeadURL$
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
package org.sakaiproject.tool.assessment.facade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.osid.assessment.AssessmentException;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.SectionData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class AssessmentFacade extends AssessmentBaseFacade
    implements AssessmentIfc
{
  private AssessmentIfc data;
  private Long assessmentTemplateId;
  private Long assessmentId;
  private Set sectionSet;

  public AssessmentFacade() {
    //super();
    this.data = new AssessmentData();
    try {
      // assessment(org.osid.assessment.Assessment) is a protected properties
      // in AssessmentBaseFacade
      assessment.updateData(this.data);
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
  }

  /**
   * IMPORTANT: this constructor do not have "data", this constructor is
   * merely used for holding assessmentBaseId (which is the assessmentId), Title
   * & lastModifiedDate for displaying purpose.
   * This constructor does not persist data (which it has none) to DB
   * @param id
   * @param title
   * @param lastModifiedDate
   */
  public AssessmentFacade(Long id, String title, Date lastModifiedDate) {
    // in the case of template assessmentBaseId is the assessmentTemplateId
    super.setAssessmentBaseId(id);
    super.setTitle(title);
    super.setLastModifiedDate(lastModifiedDate);
  }

  public AssessmentFacade(AssessmentIfc data, Boolean loadSection) {
    try {
      // assessment(org.osid.assessment.Assessment) is a protected properties
      // in AssessmentBaseFacade
      //assessment.updateData(this.data);
      super.setData(data);
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    // super class does not have assessmentTemplateId nor sectionSet,
    // so we need to set it here
    this.assessmentTemplateId = data.getAssessmentTemplateId();
    // sectionSet is a set of SectionFacade
    this.sectionSet = new HashSet();

    // check if we need to load section
    if (loadSection.equals(Boolean.TRUE)){
      Set dataSet = data.getSectionSet();
      Iterator iter = dataSet.iterator();
      while (iter.hasNext()) {
        SectionData s = (SectionData) iter.next();
        this.sectionSet.add(new SectionFacade(s));
      }
    }
  }

  public AssessmentFacade(AssessmentIfc data) {
    try {
      // assessment(org.osid.assessment.Assessment) is a protected properties
      // in AssessmentBaseFacade
      //assessment.updateData(this.data);
      super.setData(data);
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    // super class does not have assessmentTemplateId nor sectionSet,
    // so we need to set it here
    this.assessmentTemplateId = data.getAssessmentTemplateId();
    // sectionSet is a set of SectionFacade
    this.sectionSet = new HashSet();
    Set dataSet = data.getSectionSet();
    Iterator iter = dataSet.iterator();
    while (iter.hasNext()){
      SectionData s = (SectionData)iter.next();
      this.sectionSet.add(new SectionFacade(s));
    }
  }

  public Long getAssessmentId(){
    try {
      this.data = (AssessmentIfc) assessment.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getAssessmentId();
  }

  public Long getAssessmentTemplateId() {
    try {
      this.data = (AssessmentIfc) assessment.getData();
    }
    catch (AssessmentException ex) {
      ex.printStackTrace();
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getAssessmentTemplateId();
 }

  public void setAssessmentTemplateId(Long assessmentTemplateId) {
    this.assessmentTemplateId = assessmentTemplateId;
    this.data.setAssessmentTemplateId(assessmentTemplateId);
  }

  public Set getSectionSet() {
    try {
      this.data = (AssessmentIfc) assessment.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    this.sectionSet = new HashSet();
    Set dataSet = this.data.getSectionSet();
    Iterator iter = dataSet.iterator();
    while (iter.hasNext()){
      SectionData s = (SectionData)iter.next();
      this.sectionSet.add(new SectionFacade(s));
    }
    return this.sectionSet;
  }

  // sectionSet must be a set of SectionFacade
  public void setSectionSet(Set sectionSet) {
    this.sectionSet = sectionSet;
    HashSet set = new HashSet();
    Iterator iter = sectionSet.iterator();
    while (iter.hasNext()){
      SectionFacade sf = (SectionFacade)iter.next();
      set.add(sf.getData());
    }
    this.data.setSectionSet(set);
  }

  public ArrayList getSectionArray() {
    ArrayList list = new ArrayList();
    if (this.sectionSet != null){
      Iterator iter = this.sectionSet.iterator();
      while (iter.hasNext()) {
        SectionFacade s = (SectionFacade)iter.next();
        list.add(s);
      }
    }
    return list;
  }

  public ArrayList getSectionArraySorted() {
    ArrayList list = getSectionArray();
    Collections.sort(list);
    return list;
  }

  public SectionDataIfc getSection(Long sequence){
    ArrayList list = getSectionArraySorted();
    if (list == null)
      return null;
    else
      return (SectionDataIfc) list.get(sequence.intValue()-1);
  }

  public SectionDataIfc getDefaultSection(){
    ArrayList list = getSectionArraySorted();
    if (list == null)
      return null;
    else
      return (SectionDataIfc) list.get(0);
  }

}