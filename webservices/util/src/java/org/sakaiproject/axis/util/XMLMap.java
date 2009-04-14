/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.axis.util;

/* Author - Charles Severance (csev@umich.edu) */

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.Attr;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

/**
 * a simple utility class for REST style XML
 * kind of lets us act like we are in PHP.
 */
public class XMLMap {

    /*
     * testing:
     * 
     * Map<String, String> tm = XMLMap.getMap("<a><b x=\"X\">B</b><c><d>D</d></c></a>");
     * System.out.println("tm="+tm);
     */

    /* 
     * Used when folks have XML that is a pure tree with all peer child nodes having
     * a different tag (D and E below are peers byt they are different tags.  This
     * will parse but ignore addition identically named peer child nodes.
     *
     * <A>
     *   <B>TEXT</B>
     *   <C>
     *     <D>D-Text</D>
     *     <E>E-Text</E>
     *   </C>
     *   <F X="X-Text" />
     *   <G Y="Y-Text">G-Text</G>
     * </A>
     * 
     * Output:
     * 
     * /A/B     TEXT
     * /A/C/D   D-Text
     * /A/C/E   E-Text
     * /A/F!X   X-Text
     * /A/G     G-Text
     * /A/G!Y   Y-Text
     */

    public static Map<String,String> getMap(String str)
    {
        Map<String,Object> tm =  getObjectMap(str, false);
        if ( tm == null ) return null;

        // Reduce to the first column of elements for the simple return value
        TreeMap<String,String> retval = new TreeMap<String, String> ();
        Iterator<String> iter = tm.keySet().iterator();
        while( iter.hasNext() ) {
                String key = iter.next();
                Object value = tm.get(key);
		// No need to handle String[] - because they will not
		// be stored when doFull == false
		if ( value instanceof String ) {
			String svalue = (String) value;
                	// System.out.println(key+" = " + value);
                        if ( value != null ) retval.put(key,svalue);
                }
        }
        return retval;
    }

    public static Map<String,Object> getFullMap(String str)
    {
	return getObjectMap(str, true);
    }

    private static Map<String,Object> getObjectMap(String str, boolean doFull)
    {
	if ( str == null ) return null;
	Document doc = documentFromString(str);
	if ( doc == null ) return null;
	Map<String,Object> tm = new TreeMap<String,Object>();
	recurse(tm, "", doc, doFull);
	return tm;
    }

    // Some nice utility methods
    private static Document documentFromString(String input)
    {
	try{
            DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = parser.parse(new ByteArrayInputStream(input.getBytes()));
	    return document;
	} catch (Exception e) {
	    return null;
	}
    }

    private static void recurse(Map<String, Object> tm, String path, Node parentNode, boolean doFull) 
    {
	// System.out.println("path="+path+" parentNode="+ nodeToString(parentNode).getNodeName());

	NodeList nl = parentNode.getChildNodes();
	NamedNodeMap nm = parentNode.getAttributes();

	// Insert the text node if we find one
	if ( nl != null ) for (int i = 0; i< nl.getLength(); i++ ) {
		Node node = nl.item(i);
        	if (node.getNodeType() == node.TEXT_NODE) {
			String value = node.getNodeValue();
			if ( value == null ) break;
			// Might want to make this trim test selectable by user
			if ( value.trim().length() < 1 ) break;
			// System.out.println("Adding path="+path+" value="+node.getNodeValue());
			tm.put(path,node.getNodeValue());
			break;  // Only the first one
        	}
	}

	// TODO:  Build support for doFull.
	// Outline: Loop throught the Element and Attribute Nodes
	// to see which ones are single and which are repeated
	// When doFull is true we handle repeated nodes with
	// a different syntax:
	// /a/b/c[0]!atr

	// Now loop through and add the attribute values 
	if ( nm != null ) for (int i = 0; i< nm.getLength(); i++ ) {
		Node node = nm.item(i);
        	if (node.getNodeType() == node.ATTRIBUTE_NODE) {
			String name = node.getNodeName();
			String value = node.getNodeValue();
			// System.out.println("ATTR "+path+"("+name+") = "+node.getNodeValue());
			if ( name == null || name.trim().length() < 1 || 
			     value == null || value.trim().length() < 1 ) continue;  

			String newPath = path+"!"+name;
			tm.put(newPath,value);
		}
	}
	
	// Now descend the tree to the next level deeper !!
	Set <String> done = new HashSet<String>();
	if ( nl != null ) for (int i = 0; i< nl.getLength(); i++ ) {
		Node node = nl.item(i);
        	if (node.getNodeType() == node.ELEMENT_NODE && ( ! done.contains(node.getNodeName())) ) {
			recurse(tm, path+"/"+node.getNodeName(),node,doFull);
			done.add(node.getNodeName());	
        	}
	}
    }

    public static String getXML(Map tm)
    {
	Document document = getXMLDom(tm);
	if ( document == null ) return null;
	return documentToString(document, false);
    }

    public static String getXML(Map tm, boolean pretty)
    {
	Document document = getXMLDom(tm);
	if ( document == null ) return null;
	String retval = documentToString(document, pretty);
	// Since the built in transform seems unable to indent
	// We patch it ourselves to keep from being ugly
	if ( pretty ) {
		retval = prettyString(retval);
	}
	return retval;
    }

    public static String prettyString(String inString)
    {
	StringBuffer sb = new StringBuffer();
	int depth = 0;
	boolean newLine = false;
	for (int i=0; i<inString.length(); i++ ) 
	{
		char ch = inString.charAt(i);
		char nc = ' ';
		if ( (i+1) < inString.length() ) nc = inString.charAt(i+1);
		if ( ch == '\n' ) 
		{ 
			sb.append('\n');
			newLine = true;
			continue;
		}
		// Eat Leading whitespace
		if ( newLine && ( ch == ' ' || ch == '\t' ) ) continue; 

		// Decrement depth if the first non-space is an end-tag
		if ( ch == '<' && nc == '/' ) depth--;
		if ( newLine ) 
		{
			for (int j=0; j<depth && j < 15; j++) sb.append("  ");
			newLine = false;
		}

		// Update depth if necessary
		if ( ch == '<' && ! ( nc == '/' || nc == '?' )) depth++;

		sb.append(ch);
	}
	return sb.toString();
    }

    public static Document getXMLDom(Map tm)
    {
	if ( tm == null ) return null;
        Document document = null;

        try{
            DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            document = parser.newDocument();
        } catch (Exception e) {
            return null;
        }

	iterateMap(document, document.getDocumentElement(), tm);
	return document;
    }

/*  Remember that the map is a linear list of entries
    /a/b B1
    /a/c Map
         /x X1
         /y Y1
         /y!r R1
    /a/c!q Q1
    /a/d D1

    <a>
      <b>B1</b>
      <c q="Q1">
         <x>X1</x>
         <y r="R1">Y1</y>
      </c>
      <d>D1</d>
    </a>
*/
    private static void iterateMap(Document document, Node parentNode, Map tm)
    {
	// System.out.println("> IterateMap parentNode = "+ nodeToString(parentNode));
	Iterator iter = tm.keySet().iterator();
	while( iter.hasNext() ) {
		String key = (String) iter.next();
		if ( ! key.startsWith("/") ) continue;  // Skip
		Object obj = tm.get(key);
		if ( obj instanceof String ) {
			storeInDom(document, parentNode, key, (String) obj);
		} else if ( obj instanceof Map ) {
			Map subMap = (Map) obj;
 			Node startNode = getNodeAtPath(document, parentNode, key);
			// System.out.println("descending into path="+key+" startNode="+ nodeToString(startNode));
			iterateMap(document, startNode, subMap);
			// System.out.println("back from descent path="+key+" startNode="+ nodeToString(startNode));
		}
	}
	// System.out.println("< IterateMap parentNode = "+ nodeToString(parentNode));
    }

    private static void storeInDom(Document document, Node parentNode, String key, String value)
    {
	// System.out.println("> storeInDom"+key+" = " + value + " parent="+ nodeToString(parentNode));
	if ( document == null | key == null || value == null ) return;
	if ( parentNode == null ) parentNode = document;
	// System.out.println("parentNode I="+ nodeToString(parentNode));

	String [] newPath = key.split("/");
	// System.out.println("newPath = "+outStringArray(newPath));
	String nodeAttr = null;
	for ( int i=1; i< newPath.length; i++ )
	{
		String nodeName = newPath[i];
		if ( i == newPath.length-1 ) {
			// System.out.println("Splitting !="+nodeName);
			// check to see if we have a nodename=attributename
			String [] nodeSplit = nodeName.split("!");
			if ( nodeSplit.length > 1 ) {
				nodeName = nodeSplit[0];
				nodeAttr = nodeSplit[1];
				// System.out.println("new nodeName="+nodeName+" nodeAttr="+nodeAttr);
			}
		}
		parentNode = getOrAddChildNode(document, parentNode, nodeName);
	}
	// System.out.println("parentNode after="+ nodeToString(parentNode));
	
	if ( nodeAttr != null )
	{
		if ( value!= null && parentNode instanceof Element ) 
		{
			Element element = (Element) parentNode;
			// System.out.println("Adding an attribute "+nodeAttr);
			element.setAttribute(nodeAttr,value);
		}
	}
	else if ( value != null ) 
	{
		Text newNode = document.createTextNode(value);
		parentNode.appendChild(newNode);
	}
	// System.out.println("xml="+documentToString(document,false));
	// System.out.println("< storeInDom"+key+" = " + value);
    }

    // Note - sadly this does not "return" the attr name - hence we need 
    // to replicate this code in storeInDom :(
    private static Node getNodeAtPath(Document document, Node parentNode, String path)
    {
	if ( parentNode == null ) parentNode = document;
	// System.out.println("> getNodeAtPath path="+path+" parentNode="+ nodeToString(parentNode));

	String [] newPath = path.split("/");
	// System.out.println("newPath = "+outStringArray(newPath));
	for ( int i=1; i< newPath.length; i++ )
	{
		String nodeName = newPath[i];
		if ( i == newPath.length-1 ) {
			// System.out.println("Splitting !="+nodeName);
			// check to see if we have a nodename=attributename
			String [] nodeSplit = nodeName.split("!");
			if ( nodeSplit.length > 1 ) {
				nodeName = nodeSplit[0];
				// System.out.println("new nodeName="+nodeName);
			}
		}
		parentNode = getOrAddChildNode(document, parentNode, nodeName);
	}
	// System.out.println("< getNodeAtPath returning="+ nodeToString(parentNode));
	return parentNode;
    }

    private static Node getOrAddChildNode(Document doc, Node parentNode, String nodeName)
    {
	// System.out.println("> getOrAddChildNode name="+nodeName+" parentNode="+ nodeToString(parentNode));
	if ( nodeName == null || parentNode == null) return null;

	NodeList nl = parentNode.getChildNodes();
        if ( nl != null ) for (int i = 0; i< nl.getLength(); i++ ) {
                Node node = nl.item(i);
		// System.out.println("length= " +nl.getLength()+ " i="+i+" NT="+node.getNodeType());
		// System.out.println("searching nn="+nodeName+" nc="+node.getNodeName());
                if (node.getNodeType() == node.ELEMENT_NODE) {
			if ( nodeName.equals(node.getNodeName()) ) {
				// System.out.println("< getOrAddChildNode found retval="+ nodeToString(node));
				return node;
			}
                }
        }

	Element newNode = doc.createElement(nodeName);
	parentNode.appendChild(newNode);
	// System.out.println("Adding "+nodeName+" at "+ nodeToString(parentNode)+" in "+doc);
	// System.out.println("xml="+documentToString(doc,false));
	// System.out.println("< getOrAddChildNode added retval="+ nodeToString(newNode));
	return newNode;
    }

    public static String outStringArray(String [] arr)
    {
	if ( arr == null ) return null;
	StringBuffer sb = new StringBuffer();
	for (int i = 0; i < arr.length; i++ ) {
		if ( i > 0 ) sb.append(" ");
		sb.append("["+i+"]=");
		sb.append(arr[i]);
	}
	return sb.toString();
    }

    public static String nodeToString(Node node)
    {
	if ( node == null ) return null;
	String retval = node.getNodeName();
        while ( (node = node.getParentNode()) != null ) {
		retval = node.getNodeName() + "/" + retval;
	}
	return "/" + retval;
    }

    // Optionally setup indenting to "pretty print"
    // Note - this is not very pretty at least in my testing - but it is better
    // than all string together
    public static String documentToString(Document document, boolean pretty)
    {
	try {
    	    javax.xml.transform.Transformer tf =
      		javax.xml.transform.TransformerFactory.newInstance().newTransformer();
	    if ( pretty ) {
                tf.setOutputProperty(OutputKeys.INDENT, "yes");
                tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            }
	    ByteArrayOutputStream baStream = new ByteArrayOutputStream();
    	    tf.transform (new javax.xml.transform.dom.DOMSource (document),
      		new javax.xml.transform.stream.StreamResult (baStream));
	    return baStream.toString();
	} catch (Exception e)  {
	    return null;
	}
    }

    // Someone better at generics can yell at me as to how this should have been 
    // done to use the same code for either obects or strings.  Sorry.
    public static Map<String, String> selectSubMap(Map<String, String> sm, String selection)
    {
	if ( sm == null ) return null;
        selection = selection.trim();
        if ( ! selection.endsWith("/") ) selection = selection + "/";
	if ( badSelection(selection) ) return null;
	Map<String, String> retval = new TreeMap<String, String>();
	selectSubMap(sm, retval, null, null, selection);
	return retval;
    }

    public static Map<String, Object> selectFullSubMap(Map<String, Object> om, String selection)
    {
	if ( om == null ) return null;
        selection = selection.trim();
        if ( ! selection.endsWith("/") ) selection = selection + "/";
	if ( badSelection(selection) ) return null;
	Map<String, Object> retval = new TreeMap<String, Object>();
	selectSubMap(null, null, om, retval, selection);
	return retval;
    }
 
    private static boolean badSelection(String selection)
    {
        if ( selection == null ) return true;
        if ( "/".equals(selection) ) return true;
        if ( selection.length() < 2 ) return true;
        if ( ! selection.startsWith("/") ) return true;
	return false;
    }

    private static void selectSubMap(Map<String, String> sm, Map<String, String> sret,
		Map<String, Object> om,  Map<String, Object> oret, String selection)
    {
        Iterator<String> iter = null;
	if ( sm != null ) {
            iter = sm.keySet().iterator();
	} else {
            iter = om.keySet().iterator();
	}

        while( iter.hasNext() ) {
                String key = iter.next();
                if ( key.startsWith(selection) ) {
			String newKey = key.substring(selection.length()-1);
			// System.out.println("newKey = "+newKey);
			if ( sm != null ) {
				String value = sm.get(key);
				if ( value == null ) continue;
				sret.put(newKey,value);
                        	// System.out.println(newKey+" = " + value);
			} else { 
				Object value = om.get(key);
				if ( value == null ) continue;
				oret.put(newKey,value);
                        	// System.out.println(newKey+" = " + value);
			}
                }
        }
    }

    /*
     * Remove a submap.  Depending if the string ends ina slash - there are
     * two behaviors.
     * /x/y/   All of the children are removed but the node is left intact
     * /x/y    All of the children are removed and the node itself and
     *           any attributes are removed as well (typical case)
     */
    public static void removeSubMap(Map tm, String selection)
    {
	if ( tm == null ) return;
        selection = selection.trim();
	if ( badSelection(selection) ) return;

	// If the selection does not end with /, generate the 
	// Attribute and children selections
	selection = selection.trim();
	String childSel = selection;
	String attrSel = selection;
	if ( ! selection.endsWith("/") ) {
		childSel = selection + "/";
		attrSel = selection + "!";
	}

	// Track what we will delete until loop is done
	Set<String> delSet = new HashSet<String>();

        Iterator iter = tm.keySet().iterator();
        while( iter.hasNext() ) {
                Object key = iter.next();
		if ( ! (key instanceof String) ) continue;
		String strKey = (String) key;
                if ( strKey.equals(selection) || strKey.startsWith(childSel) || strKey.startsWith(attrSel)) {
			delSet.add(strKey);
			// System.out.println("Deleting key="+key);
                }
        }

	// Actually remove...
	Iterator<String> setIter = delSet.iterator();
        while( setIter.hasNext() ) {
		String key = setIter.next();
		tm.remove(key);
	}
    }
}
