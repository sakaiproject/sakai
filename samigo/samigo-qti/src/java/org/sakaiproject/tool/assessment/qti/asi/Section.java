/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/qti/asi/Section.java $
 * $Id: Section.java 9274 2006-05-10 22:50:48Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/



package org.sakaiproject.tool.assessment.qti.asi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.sakaiproject.tool.assessment.data.dao.assessment.AttachmentData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.qti.constants.QTIConstantStrings;
import org.sakaiproject.tool.assessment.qti.constants.QTIVersion;
import org.sakaiproject.tool.assessment.qti.helper.QTIHelperFactory;
import org.sakaiproject.tool.assessment.qti.helper.item.ItemHelperIfc;

/**
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley esmiley@stanford.edu
 * @author Shastri, Rashmi <rshastri@iupui.edu>
 * @version $Id: Section.java 9274 2006-05-10 22:50:48Z daisyf@stanford.edu $
 */
public class Section extends ASIBaseClass
{
  private static Log log = LogFactory.getLog(Section.class);
  public String basePath;
  private Map items;

  /**
   * Explicitly setting serialVersionUID insures future versions can be
     * successfully restored. It is essential this variable name not be changed
     * to SERIALVERSIONUID, as the default serialization methods expects this
   * exact name.
   */
  private static final long serialVersionUID = 1;
  private int qtiVersion;

  /**
   * Creates a new Section object.
   */
  public Section()
  {
    super();
    this.basePath = QTIConstantStrings.SECTION;
  }

  /**
   * Creates a new Section object.
   *
   * @param document DOCUMENTATION PENDING
   */
  public Section(Document document, int qtiVersion)
  {
    super(document);
    if (!QTIVersion.isValid(qtiVersion))
    {
      throw new IllegalArgumentException("Invalid Section QTI version.");
    }
    this.qtiVersion = qtiVersion;
    this.basePath = QTIConstantStrings.SECTION;
  }

  /**
   * set section ident (id)
   * @param ident
   */
  public void setIdent(String ident)
  {
    String xpath = "section";
    List list = this.selectNodes(xpath);
    if (list.size() > 0)
    {
      Element element = (Element) list.get(0);
      element.setAttribute("ident", ident);
    }
  }
  /**
   * set section title
   * @param title
   */
  public void setTitle(String title)
  {
    String xpath = "section";
    List list = this.selectNodes(xpath);
    if (list.size() > 0)
    {
      Element element = (Element) list.get(0);
      element.setAttribute("title", escapeXml(title));
    }
  }

  /**
   * Update XML from persistence
   * @param section
   */
  public void update(SectionDataIfc section)
  {
    // identity
    setIdent("" + section.getSectionId());
    setTitle(section.getTitle());
    // metadata
    // Where the heck do these come from?  Looks like not being used.
    // If required we could extract keywords by weighting, and
    // set rubrics identical to description, or, we could eliminate these from XML.

    // well, we can add metadata from users' input - lydial 
    
    setFieldentry("SECTION_INFORMATION", section.getDescription());
    setFieldentry("SECTION_OBJECTIVE", section.getSectionMetaDataByLabel(SectionMetaDataIfc.OBJECTIVES));
    setFieldentry("SECTION_KEYWORD", section.getSectionMetaDataByLabel(SectionMetaDataIfc.KEYWORDS));
    setFieldentry("SECTION_RUBRIC", section.getSectionMetaDataByLabel(SectionMetaDataIfc.RUBRICS));
    setFieldentry("ATTACHMENT", getAttachment(section));
    
    // items
    ArrayList items = section.getItemArray();
    addItems(items);
  }

//  /**
//   * @deprecated hardcoded to support only QTIVersion.VERSION_1_2
//   * select and order
//   */
//  public void selectAndOrder()
//  {
//    log.debug("selectAndOrder()");
//    ArrayList selectedList = this.selectItems();
//    this.orderItems(selectedList);
//    ArrayList selectedSections = this.selectSections(basePath);
//    this.orderSections(basePath, selectedSections, QTIVersion.VERSION_1_2);
//  }

  /**
   * select items
   *
   * @return return arraylist of items
   */
  
  /*
  private ArrayList selectItems()
  {
    log.debug("selectItems()");

    ArrayList items = new ArrayList();

    //    try
    //    {
    String xpath = basePath + "/" + QTIConstantStrings.SELECTION_ORDERING +
                   "/";
    String selectionXPath = xpath + QTIConstantStrings.SELECTION;
    String orderingXPath = xpath + QTIConstantStrings.ORDER;

    List selectNodes = this.selectNodes(selectionXPath);
    List orderNodes = this.selectNodes(orderingXPath);

    int selectNodeSize = selectNodes.size();
    for (int i = 0; i < selectNodeSize; i++)
    {
      Element selectElement = (Element) selectNodes.get(i);
  //      items.addAll(processSelectElement(selectElement));
    }

    if (selectNodeSize == 0)
    {
      items.addAll(this.getAllItems());
    }

    //    }
    //    catch(Exception ex)
    //    {
    //      log.error(ex.getMessage(), ex);
    //    }
    removeItems();

    return items;
  }
*/

  /**
   * get all items
   *
   * @return list of items
   */
  private List getAllItems()
  {
    log.debug("getAllItems()");

    String xpath = basePath + "/" + QTIConstantStrings.ITEM;

    return this.selectNodes(xpath);
  }


  /**
   * remove items
   */
  private void removeItems()
  {
    log.debug("removeItems()");

    String xpath = basePath + "/" + QTIConstantStrings.ITEM;
    this.removeElement(xpath);
  }

  /**
   * order items
   *
   * @param items list of items
   */
  /*
  private void orderItems(ArrayList items)
  {
    if (log.isDebugEnabled())
    {
      log.debug("orderItems(ArrayList " + items + ")");
    }

    String xpath = basePath + "/" + QTIConstantStrings.SELECTION_ORDERING +
                   "/";
    String orderingXPath = xpath + QTIConstantStrings.ORDER;
    List orderNodes = this.selectNodes(orderingXPath);
    if ( (orderNodes != null) && (orderNodes.size() > 0))
    {
      Element order = (Element) orderNodes.get(0);
      String orderType = order.getAttribute(QTIConstantStrings.ORDER_TYPE);
      if ("Random".equalsIgnoreCase(orderType))
      {
        //Randomly order items.
        long seed = System.currentTimeMillis();
        Random rand = new Random(seed);
        int size = items.size();
        for (int i = 0; i < size; i++)
        {
          int randomNum = rand.nextInt(size);
          Object temp = items.get(i);
          items.set(i, items.get(randomNum));
          items.set(randomNum, temp);
          log.debug("switch item " + i + " with " + randomNum);
        }
      }
    }

    addItems(items);
  }
*/
  /**
   * Add item list to this section document.
   *
   * @param items list of ItemDataIfc
   */
  private void addItems(ArrayList items)
  {
    if (log.isDebugEnabled())
    {
      log.debug("addItems(ArrayList " + items + ")");
    }
//    ItemHelper itemHelper = new ItemHelper();
    QTIHelperFactory factory = new QTIHelperFactory();
    ItemHelperIfc itemHelper =
        factory.getItemHelperInstance(this.qtiVersion);

    try
    {
      String xpath = basePath;
      for (int i = 0; i < items.size(); i++)
      {
        ItemDataIfc item = (ItemDataIfc) items.get(i);
        //TypeIfc type = item.getType();
        Long type = item.getTypeId();
        Item itemXml;
        if ( (TypeIfc.MULTIPLE_CHOICE_SURVEY).equals(type))
        {
	  // deprecated, keep it for backward compatibility
          String scale = item.getItemMetaDataByLabel(ItemMetaDataIfc.SCALENAME);   
	  // PREDEFINED_SCALE is the new metadata key 
          if (scale==null) {
            scale = item.getItemMetaDataByLabel(ItemMetaDataIfc.PREDEFINED_SCALE);
          }
          itemXml = itemHelper.readTypeSurveyItem(scale);
        }
        else
        {
          itemXml = itemHelper.readTypeXMLItem(type);
        }

        // update item data
        itemXml.setIdent(item.getItemIdString());
        itemXml.update(item);
        Element itemElement = (Element) itemXml.getDocument().
                              getDocumentElement();
        log.debug(
          "Item ident is: " + itemElement.getAttribute("ident"));
        this.addElement(xpath, itemElement);
      }
    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
    }
  }


  /**
   * Method for meta data.
   * @todo use QTIConstantStrings
   * @param fieldlabel field label
   *
   * @return value
   */
  public String getFieldentry(String fieldlabel)
  {
    if (log.isDebugEnabled())
    {
      log.debug("getFieldentry(String " + fieldlabel + ")");
    }

    String xpath =
      "section/qtimetadata/qtimetadatafield/fieldlabel[text()='" +
      fieldlabel +
      "']/following-sibling::fieldentry";

    return super.getFieldentry(xpath);
  }

  /**
   * Method for meta data.
   *
   * @param fieldlabel label
   * @param setValue value
   */
  public void setFieldentry(String fieldlabel, String setValue)
  {
    if (log.isDebugEnabled())
    {
      log.debug(
        "setFieldentry(String " + fieldlabel + ", String " + setValue +
        ")");
    }

    String xpath =
      "section/qtimetadata/qtimetadatafield/fieldlabel[text()='" +
      fieldlabel +
      "']/following-sibling::fieldentry";
    super.setFieldentry(xpath, setValue);
  }

  /**
   * Method for meta data.
   *
   * @param fieldlabel to be added
   */
  public void createFieldentry(String fieldlabel)
  {
    if (log.isDebugEnabled())
    {
      log.debug("createFieldentry(String " + fieldlabel + ")");
    }

    String xpath = "section/qtimetadata";
    super.createFieldentry(xpath, fieldlabel);
  }

  /**
   * ASI OKI implementation
   *
   * @param itemId item id
   */
  public void addItemRef(String itemId)
  {
    if (log.isDebugEnabled())
    {
      log.debug("addItem(String " + itemId + ")");
    }

    try {
    	String xpath = basePath;
    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    	DocumentBuilder db = dbf.newDocumentBuilder();
    	Document document = db.newDocument();
    	Element element = document.createElement(QTIConstantStrings.ITEMREF);
    	element.setAttribute(QTIConstantStrings.LINKREFID, itemId);
    	this.addElement(xpath, element);
    } catch(ParserConfigurationException pce) {
    	log.error("Exception thrown from addItemRef() : " + pce.getMessage());
		pce.printStackTrace();
    }
  }

  /**
   * remove item ref
   *
   * @param itemId igem id
   */
  public void removeItemRef(String itemId)
  {
    if (log.isDebugEnabled())
    {
      log.debug("removeItem(String " + itemId + ")");
    }

    String xpath =
      basePath + "/" + QTIConstantStrings.ITEMREF + "[@" +
      QTIConstantStrings.LINKREFID + "=\"" + itemId + "\"]";
    this.removeElement(xpath);
  }

  /**
   * add section ref
   *
   * @param sectionId section id
   */
  public void addSectionRef(String sectionId)
  {
    if (log.isDebugEnabled())
    {
      log.debug("addSection(String " + sectionId + ")");
    }
    try {
    	String xpath = basePath;
    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    	DocumentBuilder db = dbf.newDocumentBuilder();
    	Document document = db.newDocument();
    	Element element = document.createElement(QTIConstantStrings.SECTIONREF);
    	element.setAttribute(QTIConstantStrings.LINKREFID, sectionId);
    	this.addElement(xpath, element);
    } catch(ParserConfigurationException pce) {
    	log.error("Exception thrown from addSectionRef() : " + pce.getMessage());
		pce.printStackTrace();
    }
  }

  /**
   * remove section ref
   *
   * @param sectionId DOCUMENTATION PENDING
   */
  public void removeSectionRef(String sectionId)
  {
    if (log.isDebugEnabled())
    {
      log.debug("removeSection(String " + sectionId + ")");
    }

    String xpath =
      basePath + "/" + QTIConstantStrings.SECTIONREF + "[@" +
      QTIConstantStrings.LINKREFID + "=" + sectionId + "]";
    this.removeElement(xpath);
  }

  /**
   * Order item refs
   *
   * @param itemRefIds list of ref ids
   */
  public void orderItemRefs(ArrayList itemRefIds)
  {
    if (log.isDebugEnabled())
    {
      log.debug("orderItemRefs(ArrayList " + itemRefIds + ")");
    }

    this.removeItemRefs();
    int size = itemRefIds.size();
    for (int i = 0; i < size; i++)
    {
      this.addItemRef( (String) itemRefIds.get(i));
    }
  }

  /**
   * remove item refs
   */
  private void removeItemRefs()
  {
    log.debug("removeItems()");

    String xpath = basePath + "/" + QTIConstantStrings.ITEMREF;
    this.removeElement(xpath);
  }

  /**
   * get section refs
   *
   * @return list of section refs
   */
  public List getSectionRefs()
  {
    log.debug("getSectionRefs()");
    String xpath = basePath + "/" + QTIConstantStrings.SECTIONREF;

    return this.selectNodes(xpath);
  }

  /**
   * get section ref ids
   *
   * @return list of section ref ids
   */
  public List getSectionRefIds()
  {
    List refs = this.getSectionRefs();
    List ids = new ArrayList();
    int size = refs.size();
    for (int i = 0; i < size; i++)
    {
      Element ref = (Element) refs.get(0);
      String idString = ref.getAttribute(QTIConstantStrings.LINKREFID);
      ids.add(idString);
    }

    return ids;
  }

  /**
   * get Xpath of section
   * @return the Xpath
   */
  public String getBasePath()
  {
    return basePath;
  }

  /**
   * set XPath of section
   * @param basePath
   */
  public void setBasePath(String basePath)
  {
    this.basePath = basePath;
  }
  
  private String getAttachment(SectionDataIfc section) {
	  Set attachmentSet = (Set) section.getSectionAttachmentSet();
   	  if (attachmentSet != null && attachmentSet.size() != 0) { 
   		Iterator iter = attachmentSet.iterator();
   		AttachmentData attachmentData = null;
   		StringBuffer attachment = new StringBuffer();
   		while (iter.hasNext())
   		{
   			attachmentData = (AttachmentData) iter.next();
   			attachment.append(attachmentData.getResourceId().replaceAll(" ", ""));
   			attachment.append("|");
   			attachment.append(attachmentData.getFilename());
   			attachment.append("|");
   			attachment.append(attachmentData.getMimeType());
   			attachment.append("\n");
   		}
   		return attachment.toString();
   	  }
   	  else {
   		return null;
   	  }
  }
}


