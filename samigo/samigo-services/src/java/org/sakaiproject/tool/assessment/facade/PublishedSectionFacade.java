/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.facade;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.osid.assessment.AssessmentException;

import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionMetaData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionMetaDataIfc;
import org.sakaiproject.tool.assessment.services.PersistenceService;

public class PublishedSectionFacade extends SectionFacade implements Serializable, Comparable {

	private static final long serialVersionUID = 5788637014806801101L;

  /**
   * This is a very important constructor. Please make sure that you have
   * set all the properties (declared above as private) of SectionFacade using
   * the "data" supplied. "data" is a org.osid.assessment.Section properties
   * and I use it to store info about an section.
   * @param data
   */
  public PublishedSectionFacade(SectionDataIfc data){
    super(data);
  }

  // the following method's signature has a one to one relationship to
  // org.sakaiproject.tool.assessment.osid.section.SectionImpl
  // which implements org.osid.assessment.Section

  /**
   * Get the Id for this SectionFacade.
   * @return org.osid.shared.Id
   */
  org.osid.shared.Id getId(){
    try {
      this.data = (SectionDataIfc) section.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }

    PublishedSectionFacadeQueriesAPI publishedSectionFacadeQueries = PersistenceService.getInstance().getPublishedSectionFacadeQueries();
    return publishedSectionFacadeQueries.getId(this.data.getSectionId());
  }
  
  /**
   * Add a Meta Data to SectionFacade
   * @param label
   * @param entry
   */
  public void addSectionMetaData(String label, String entry) {
    if (this.metaDataSet == null) {
      setSectionMetaDataSet(new HashSet());
      this.metaDataMap = new HashMap();
    }

    if (this.metaDataMap.get(label)!=null){
      // just update
      Iterator iter = this.metaDataSet.iterator();
      while (iter.hasNext()){
    	  SectionMetaDataIfc metadata = (SectionMetaDataIfc) iter.next();
        if (metadata.getLabel().equals(label))
          metadata.setEntry(entry);
      }
    }
    else {
      this.metaDataMap.put(label, entry);
      this.data.getSectionMetaDataSet().add(new PublishedSectionMetaData((SectionDataIfc)this.data, label, entry));
      this.metaDataSet = this.data.getSectionMetaDataSet();
    }
  }
  
  public Set getItemFacadeSet() throws DataFacadeException {
		this.itemFacadeSet = new HashSet();
		try {
			this.data = (SectionDataIfc) section.getData();
			Set set = this.data.getItemSet();
			Iterator iter = set.iterator();
			while (iter.hasNext()) {
				PublishedItemFacade publishedItemFacade = new PublishedItemFacade((ItemDataIfc) iter
						.next());
				this.itemFacadeSet.add(publishedItemFacade);
			}
			//this.sectionSet = data.getSectionSet();
		} catch (AssessmentException ex) {
			throw new DataFacadeException(ex.getMessage());
		}
		return this.itemFacadeSet;
	}
    
  public AssessmentIfc getAssessment() throws DataFacadeException {
	    try {
	      this.data = (SectionDataIfc) section.getData();
	    }
	    catch (AssessmentException ex) {
	      throw new DataFacadeException(ex.getMessage());
	    }
	    return new PublishedAssessmentFacade(this.data.getAssessment());
  }
  
  public void setAssessment(AssessmentIfc assessment) {
	    this.assessment = (PublishedAssessmentFacade)assessment;
	    PublishedAssessmentData d = (PublishedAssessmentData) ((PublishedAssessmentFacade) this.assessment).getData();
	    this.data.setAssessment(d);
  }
}
