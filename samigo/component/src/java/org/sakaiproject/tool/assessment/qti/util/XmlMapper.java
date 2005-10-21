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

package org.sakaiproject.tool.assessment.qti.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
public class XmlMapper
{
  private static Log log = LogFactory.getLog(XmlMapper.class);

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
      log.error(e); throw new Error(e);
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
                ctext += XmlUtil.getDOMString(ccnodes.item(n));
              }
              list.add(ctext);
              hNode.put(cname, list);
            }
            else // scalar (default)
            {
              // support for deep copy
              NodeList ccnodes = cnode.getChildNodes();
              for (int n = 0; n < ccnodes.getLength(); n++) {
                ctext += XmlUtil.getDOMString(ccnodes.item(n));
              }
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
  private static String textValue(Node nd)
  {
    String text = "";
    NodeList nodes = nd.getChildNodes();
    for(int i = 0; i < nodes.getLength(); i++)
    {
      Node cnode = nodes.item(i);
      if(cnode.getNodeType() == Node.TEXT_NODE)
      {
        text += cnode.getNodeValue();
      }
    }

    return text;
  }

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




