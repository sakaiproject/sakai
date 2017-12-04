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

package org.sakaiproject.tool.assessment.qti.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.sakaiproject.tool.assessment.qti.util.XmlUtil;

/**
 * Utility class.  Maps  XML elements and attribute under a given node
 * to a Map, or populates bean.
 *
 * Note this now supports deep copy.
 *
 * @author @author Ed Smiley
 * @version $Id$
 */
@Slf4j
public class XmlMapper
{

  public static final String ATTRIBUTE_PREFIX = "attribute_";

  /**
   * Maps each element node and attribute under a given node to a Map.
   * It associates each element's text value with its name, and
   * each attribute value with a key of "attribute_" + the attribute name.
   *
   * If node is a document it processes it as if it were the root node.
   *
   * If there are multiple nodes with the same element name they will be stored
   * in a List.
   *
   * NOTE:
   * This was DESIGNED to ignore elements at more depth than root +1.
   * It has now been modified to deep copy under the nodes, but it
   * WILL NOT recurse and assign key value pairs below,
   * this is by design.  The elements below (e.g. XML snippets, XHTML text)
   * are all put into the value.
   *
   * @param node Node
   * @param indent String
   * @return HashMap
   */
  static public Map map(Document doc)
  {
    return hashNode(doc);
  }

  /**
   * Maps each element node to a bean property.
   * Supports only simple types such as String, int and long, plus Lists.
   *
   * If node is a document it processes it as if it were the root node.
   *
   * If there are multiple nodes with the same element name they will be stored
   * in a List.
   *
   * NOTE:
   * This is DESIGNED to ignore elements at more depth so that simple
   * String key value pairs are used.  This WILL NOT recurse to more depth,
   * by design.  If it did so, you would have to use maps of maps.
   *
   * @param bean Serializable object which has the appropriate setters/getters
   * @param doc the document
   */
  static public void populateBeanFromDoc(Serializable bean, Document doc)
  {
    try
    {
      Map m = map(doc);
      BeanUtils.populate(bean, m);
    }
    catch(Exception e)
    {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  /**
   * utility class, hides the implementation as a HashMap
   * @param node
   * @return HashMap
   */
  private static HashMap hashNode(Node node)
  {
    HashMap hNode = new HashMap();

    int nType = node.getNodeType();
    NodeList nodes = node.getChildNodes();
    NamedNodeMap attributes = node.getAttributes();
    String name = node.getNodeName();

    // node is a document, recurse
    if(nType == Node.DOCUMENT_NODE)
    {
      // find root node
      if(nodes != null)
      {
        for(int i = 0; i < nodes.getLength(); i++)
        {
          // find  and process root node
          Node rnode = nodes.item(i);
          if(rnode.getNodeType() == Node.ELEMENT_NODE)
          {
            hNode = hashNode(rnode);

            break;
          }
        }
      }
    }

    //end if Node.DOCUMENT_NODE
    if(nType == Node.ELEMENT_NODE)
    {
      // add in child elements
      if(nodes != null)
      {
        for(int j = 0; j < nodes.getLength(); j++)
        {
          Node cnode = nodes.item(j);
          if(cnode.getNodeType() == Node.ELEMENT_NODE)
          {
            String cname = cnode.getNodeName();
            String ctext = ""; //textValue(cnode);
            String ctype = getTypeAttribute(cnode);
//            log.debug(cname + "=" + ctype);

            StringBuilder ctextbuf = new StringBuilder();
            // if we have multiple identical entries store them in a List
            if("list".equals(ctype))
            {
              ArrayList list;
              // if this element name already has a list
              if(hNode.get(cname) instanceof ArrayList)
              {
                list = (ArrayList) hNode.get(cname);
              }
              else // put it in a new list
              {
                list = new ArrayList();
              }

              // support for deep copy

//              list.add(ctext);
              NodeList ccnodes = cnode.getChildNodes();
                
              for (int n = 0; n < ccnodes.getLength(); n++) {
                ctextbuf.append(XmlUtil.getDOMString(ccnodes.item(n)));
              }
              ctext = ctextbuf.toString();		 
              list.add(ctext);
              hNode.put(cname, list);
            }
            else // scalar (default)
            {
              // support for deep copy
              NodeList ccnodes = cnode.getChildNodes();
              for (int n = 0; n < ccnodes.getLength(); n++) {
                ctextbuf.append(XmlUtil.getDOMString(ccnodes.item(n)));
              }
              ctext = ctextbuf.toString();		
              hNode.put(cname, ctext);
            }
          }
        }
      }

      // add in attributes
      if(attributes != null)
      {
        for(int i = 0; i < attributes.getLength(); i++)
        {
          Node current = attributes.item(i);
          hNode.put(
            ATTRIBUTE_PREFIX + current.getNodeName(), current.getNodeValue());
        }
      }
    }

    return hNode;
  }

  /**
   * utility method
   *
   * @param nd node
   *
   * @return text value of node
   */
  
  /*
  private static String textValue(Node nd)
  {
    
    NodeList nodes = nd.getChildNodes();
    
    StringBuilder textbuf = new StringBuilder(); 
    
    for(int i = 0; i < nodes.getLength(); i++)
    {
      Node cnode = nodes.item(i);
      if(cnode.getNodeType() == Node.TEXT_NODE)
      {
        //text += cnode.getNodeValue();
        textbuf.append(cnode.getNodeValue());
      }
    }
    String text = textbuf.toString();
    return text;
  }
  
  */

  /**
   * If there is a type attribute for the element node, return its value,
   * otherwise return "scalar".
   * @param node
   * @return
   */
  private static String getTypeAttribute(Node node){
    NamedNodeMap attributes = node.getAttributes();
    if(attributes != null)
    {
      for(int i = 0; i < attributes.getLength(); i++)
      {
        Node current = attributes.item(i);
        if ("type".equals(current.getNodeName())){
          return current.getNodeValue();
        }
      }
    }

    return "scalar";
  }

}




