package org.sakaiproject.tool.assessment.facade;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.osid.assessment.AssessmentException;
import org.osid.assessment.Item;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemText;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.osid.assessment.impl.ItemImpl;

public class PublishedItemFacade extends ItemFacade implements Serializable, ItemDataIfc, Comparable {

	private static final long serialVersionUID = -1711478342512505707L;
	private PublishedSectionFacade section;
	
	  /** ItemFacade is the class that is exposed to developer
	   *  It contains some of the useful methods specified in
	   *  org.osid.assessment.Item and it implements
	   *  org.sakaiproject.tool.assessment.ifc.
	   *  When new methods is added to osid api, this code is still workable.
	   *  If signature in any of the osid methods that we mirrored changes,
	   *  we only need to modify those particular methods.
	   *  - daisyf
	   */
	  public PublishedItemFacade(){
	  // need to hook PublishedItemFacade.data to ItemData, our POJO for Hibernate
	  // persistence
	   this.data = new PublishedItemData();
	   ItemImpl itemImpl = new ItemImpl(); //<-- place holder
	   item = (Item)itemImpl;
	   try {
	     item.updateData(this.data);
	   }
	   catch (AssessmentException ex) {
	     throw new DataFacadeException(ex.getMessage());
	   }
	  }

	  /**
	   * This is a very important constructor. Please make sure that you have
	   * set all the properties (declared above as private) of ItemFacade using
	   * the "data" supplied. "data" is a org.osid.assessment.Item properties
	   * and I use it to store info about an item.
	   * @param data
	   */
	  public PublishedItemFacade(ItemDataIfc data){
	    this.data = data;
	    ItemImpl itemImpl = new ItemImpl(); // place holder
	    item = (Item)itemImpl;
	    try {
	      item.updateData(this.data);
	    }
	    catch (AssessmentException ex) {
	      throw new DataFacadeException(ex.getMessage());
	    }
	    this.id = getId();
	    this.description = getDescription();
	    this.itemType = getItemType();
	    this.itemTextSet = getItemTextSet();
	    this.itemMetaDataSet = getItemMetaDataSet();
	    this.itemMetaDataMap = getItemMetaDataMap(this.itemMetaDataSet);
	    this.itemFeedbackSet = getItemFeedbackSet();
	    this.itemFeedbackMap = getItemFeedbackMap(this.itemFeedbackSet);
	    this.hasRationale= data.getHasRationale();//rshastri :SAK-1824
	    this.itemAttachmentSet = getItemAttachmentSet();
	  }

	  // the following method's signature has a one to one relationship to
	  // org.sakaiproject.tool.assessment.osid.item.ItemImpl
	  // which implements org.osid.assessment.Item

	  /**
	   * Get the Id for this ItemFacade.
	   * @return org.osid.shared.Id
	   */
	  org.osid.shared.Id getId(){
	    try {
	      this.data = (ItemDataIfc) item.getData();
	    }
	    catch (AssessmentException ex) {
	      throw new DataFacadeException(ex.getMessage());
	    }
	    PublishedItemFacadeQueries publishedItemFacadeQueries = new PublishedItemFacadeQueries();
	    return publishedItemFacadeQueries.getItemId(this.data.getItemId());
	  }

	  /**
	   * Set itemId for ItemFacade
	   * @param itemId
	   */
	  public void setItemIdString(String itemIdString) {
	    this.itemIdString = itemIdString;
	    this.data.setItemIdString(itemIdString);
	  }

	  // expect a return of SectionFacade from this method
	  public SectionDataIfc getSection() throws DataFacadeException {
	    try {
	      this.data = (ItemDataIfc) item.getData();
	    }
	    catch (AssessmentException ex) {
	      throw new DataFacadeException(ex.getMessage());
	    }
	    if (this.data.getSection()!= null) {
	      return new PublishedSectionFacade(this.data.getSection());
	    }
	    else {
	      return null;
	    }
	  }

	  // section is SectionFacade not SectionData
	  public void setSection(SectionDataIfc section) {
	    this.section = (PublishedSectionFacade) section;
	    if (this.section != null) {
	      this.data.setSection(this.section.getData());
	    }
	    else {
	      this.data.setSection(null);
	    }
	  }
	  
	  /**
	   * Add item text (question text) to ItemFacade (question). For multiple
	   * choice, multiple correct, survey, matching & fill in the blank, you can
	   * specify a set of acceptable answers. Usually, the purpose for this is
	   * to facilitate auto-grading.
	   * @param text
	   * @param answerSet
	   */
	  public void addItemText(String text, Set answerSet) {
	    if (this.data.getItemTextSet() == null) {
	      this.data.setItemTextSet(new HashSet());
	    }
	    Long sequence =  Long.valueOf(this.data.getItemTextSet().size()+1);
	    PublishedItemText itemText = new PublishedItemText((PublishedItemData)this.data, sequence,
	                                     text, answerSet);
	    this.data.getItemTextSet().add(itemText);
	    this.itemTextSet = this.data.getItemTextSet();
	  }

	  /**
	   * Add a Meta Data to ItemFacade
	   * @param label
	   * @param entry
	   */
	  public void addItemMetaData(String label, String entry) {
	    if (this.itemMetaDataSet == null) {
	      setItemMetaDataSet(new HashSet());
	      this.itemMetaDataMap = new HashMap();
	    }
	    this.itemMetaDataMap.put(label, entry);
	    this.data.getItemMetaDataSet().add(new PublishedItemMetaData((PublishedItemData)this.data, label, entry));
	    this.itemMetaDataSet = this.data.getItemMetaDataSet();
	  }

	  /**
	   * Add feedback of a specified feedback type (e.g. CORRECT, INCORRECT)
	   * to ItemFacade
	   * @param feedbackTypeId
	   * @param text
	   */
	  public void addItemFeedback(String feedbackTypeId, String text) {
	    if (this.itemFeedbackSet == null) {
	      setItemFeedbackSet(new HashSet());
	      this.itemFeedbackMap = new HashMap();
	    }
	    this.itemFeedbackMap.put(feedbackTypeId, text);
	    this.data.getItemFeedbackSet().add(new PublishedItemFeedback((PublishedItemData)this.data, feedbackTypeId, text));
	    this.itemFeedbackSet = this.data.getItemFeedbackSet();
	  }
}
