/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.lessonbuildertool.cc;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import lombok.extern.slf4j.Slf4j;

import org.xml.sax.SAXException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.util.FormattedText;

@Slf4j
public class QtiImport {
    String title = null;
    int noscore = 0;
    int paras = 0;
    PrintWriter out = null;
    boolean needHeader = true;
    CharArrayWriter charout = null;
    String filebase = null;
    SimplePageBean bean = null;

    boolean feedbackpermitted = true;
    String timelimit = null;
    boolean allowlate = true;
    String maxattempts = "1";
    String siteId = null;
    boolean usesPatternMatch = false;
    boolean usesCurriculum = false;

    class Pair {
	String left;
	String leftident;
	String right;
	String rightident;

	Pair (String left, String leftident, String right, String rightident) {
	    this.left = left;
	    this.leftident = leftident;
	    this.right = right;
	    this.rightident = rightident;
	}
    }

    class Shortans {
	String qident;
	String rident;
	String answer;
	Shortans (String qident, String rident) {
	    this.qident = qident;
	    this.rident = rident;
	    this.answer = null;
	}
    }

    class Mcans {
	String ident;
	String answer;
	List<String> fbident;
	String newident;
	String feedback;
	boolean correct;
	Mcans (String ident, String newident, String answer) {
	    this.ident = ident;
	    this.newident = newident;
	    this.answer = answer;
	    this.fbident = new ArrayList<String>();
	    this.feedback = null;
	    this.correct = false;
	}
    }

    Document document;

    // supporting routines

    public String guessQuestionType (Node itemnode) {

	// explicit type?
	NodeList meta=((Element)itemnode).getElementsByTagName("qmd_itemtype");
	if (meta != null && meta.item(0) != null)
	    return meta.item(0).getTextContent();

	// cc version. look at all metadata items for the profile
	String ccProfile = null;
	meta=((Element)itemnode).getElementsByTagName("qtimetadatafield");
	for (int i = 0; i < meta.getLength(); i++) {
	    Node label = getFirstByName(meta.item(i), "fieldlabel");
	    if ("cc_profile".equals(getText(label))) {
		Node value = getFirstByName(meta.item(i), "fieldentry");
		ccProfile = getText(value);
		break;
	    }
	}

	// response_lid is for multiple choice, true false and matching
	// matching has an explicit type, so just need to see if true false
	NodeList lid=((Element)itemnode).getElementsByTagName("response_lid");
	if (lid != null && lid.item(0) != null) {

	    String cardinality = ((Element)lid.item(0)).getAttribute("rcardinality");
	    if (cardinality != null && cardinality.equalsIgnoreCase("multiple"))
		return "Multiple Correct";

	    NodeList responses=((Element)lid.item(0)).getElementsByTagName("response_label");
	    if (responses == null || responses.getLength() != 2)
		return "Multiple Choice";

	    String answer1 = "";
	    Element response1 = (Element)responses.item(0);
	    if (response1 != null) {
		NodeList text1 = response1.getElementsByTagName("mattext");
		if (text1 != null && text1.item(0) != null)
		    answer1 = text1.item(0).getTextContent();
	    }

	    String answer2 = "";
	    Element response2 = (Element)responses.item(1);
	    if (response2 != null) {
		NodeList text2 = response2.getElementsByTagName("mattext");
		if (text2 != null && text2.item(0) != null)
		    answer2 = text2.item(0).getTextContent();
	    }

	    // there's nothing to say that a true false question
	    // has to use English. However Samigo depends upon it.
	    // It presents localized alternatives, but the database
	    // shows true and false. But if there's some other answer
	    // we have no way of knowing which is true and which false.
	    // so the only safe approach is to treat it as multiple choice.

	    if (answer1.equalsIgnoreCase("True") && 
		answer2.equalsIgnoreCase("False") ||
		answer1.equalsIgnoreCase("False") && 
		answer2.equalsIgnoreCase("True"))
		return "True False";
	    return "Multiple Choice";
	}

	// response_fib is for short ans/essay and fib
	// matching has an explicit type, so just need to see if true false
	NodeList fib=((Element)itemnode).getElementsByTagName("render_fib");
	if (fib != null && fib.item(0) != null) {

	    NodeList varequal=((Element)itemnode).getElementsByTagName("varequal");
	    if (varequal != null && varequal.item(0) != null)
		return "Fill In the Blank";
	    NodeList varsubstring=((Element)itemnode).getElementsByTagName("varsubstring");
	    if (varsubstring != null && varsubstring.item(0) != null)
		return "Fill In the Blank";

	}
	
	return "Short Answers/Essay";

    }

    // from Processing XML with Java, Elliotte Rusty Harold, 
    // http://www.cafeconleche.org/books/xmljava/

    public String escapeText(String s) {
   
	if (s.indexOf('&') != -1 || s.indexOf('<') != -1
	    || s.indexOf('>') != -1 || s.indexOf('"') != -1
	    || s.indexOf('\'') != -1 ) {
	    StringBuffer result = new StringBuffer(s.length() + 6);
	    for (int i = 0; i < s.length(); i++) {
		char c = s.charAt(i);
		if (c == '&') result.append("&amp;");
		else if (c == '<') result.append("&lt;");
		else if (c == '"') result.append("&quot;");
		else if (c == '\'') result.append("&apos;");
		else if (c == '>') result.append("&gt;");
		else result.append(c);
	    }
	    return result.toString();  
	}
	else {
	    return s;   
	}
    }

    String appString(String s1, String s2) {
	if (s1 != null && !s1.equals("")) {
	    if (s2 != null && !s2.equals(""))
		return s1 + "<br/>" + s2;
	    else 
		return s1;
	} else
	    return s2;
    }

    Node getFirstByName(Node node, String name) {
	NodeList nodes = ((Element)node).getElementsByTagName(name);
	int numnodes;

	if (nodes == null)
	    numnodes = 0;
	else
	    numnodes = nodes.getLength();

	if (numnodes == 0)
	    return null;
	else
	    return nodes.item(0);

    }
	    
    Node getNextByName(Node node, String name) {
	Node child = node.getNextSibling();
	while (child != null && !child.getNodeName().equals(name)) {
	    child = child.getNextSibling();
	}
	return child;
    }

    Node getNextElement(Node node) {
	while (node != null && node.getNodeType() != Node.ELEMENT_NODE)
	    node = node.getNextSibling();
	return node;
    }

    // at least in 2.3, Samigo is unable to import MATIMAGE, despite the
    // fact that it produces them. So I stick the image into the HTML
    // of the question
    String getText(Node mattext) {
	String retText = "";

	Element mattextl = null;
	try {
	    mattextl = (Element)mattext;
	} catch (Exception e) {
	    log.debug("mattext is not an element");
	    return null;
	}

	NodeList textNodes = mattext.getChildNodes();
	for (int i = 0; i < textNodes.getLength(); i++) {
	    Node textchild = textNodes.item(i);
	    if (textchild == null)
		continue;

	    String text = textchild.getNodeValue();
	    if (text == null)
		continue;

	    retText = retText + text;
	}

	String texttype = mattextl.getAttribute("texttype");
	if (texttype != null && texttype.equals("text/plain"))
	    retText = FormattedText.convertPlaintextToFormattedText(retText);
	else
	    retText =  retText.replaceAll("\\$IMS-CC-FILEBASE\\$", filebase);

	return retText;
    }

    String getMatText(Node material) {
  
	StringBuilder rettext = new StringBuilder("");
  
  	if (material == null) {
  	    log.debug("<material> is null");
  	    return null;
  	}
  
	Node stuff = material.getFirstChild();
  
	log.debug("start");
	while (stuff != null) {
	    if (stuff.getNodeName().equalsIgnoreCase("mattext")) {
		String thistext = getText(stuff);
		if (thistext != null)
		    rettext.append(thistext);
	    } else if (stuff.getNodeName().equalsIgnoreCase("matimage")) {
		String uri = getAttribute(stuff, "uri");
		if (uri != null) {
		    rettext.append("<p><img src=\"");
		    rettext.append(uri);
		    rettext.append("\" alt=\"");
		    rettext.append(uri);
		    rettext.append("\" />");
		}
	    } else if (stuff.getNodeName().equals("#text")) {
		// apparently the whitespace is reported as #text nodes;
		// ignore them
	    } else {
		log.error("unknown contents in material: {}: {}", stuff.getNodeName(), stuff.getNodeValue());
            }
	    stuff = stuff.getNextSibling();
	}

	return rettext.toString();

    }


    // process question types

    String getAttribute(Node n, String name) {
	Element element;
	try {
	    element = (Element)n;
	} catch (Exception e) {
	    log.error("node is not an element: {}", n.getNodeName());
	    return null;
	}
	return element.getAttribute(name);
    }

    String getNodeText(Node n) {
	Node vartext = n.getFirstChild();
	if (vartext == null)
	    return null;
	return vartext.getNodeValue();
    }


    // matching question
    boolean procmatch(Node item)throws IOException {
	log.debug("match");

	String title = null;
	List<Pair> pairs = new ArrayList<Pair>();
	String feedback = null;
	Double score = 0.0;
	boolean scoreset = false;

	title = getAttribute(item, "title");
	log.debug("title: {}", title);

	Node presentation = getFirstByName(item, "presentation");
	if (presentation == null) {
	    log.debug("can't find <presentation>");
	    return false;
	}

	Node material = getFirstByName(presentation, "material");

	StringBuilder question = new StringBuilder(getMatText(material));

	// optional list of pairs to display before the answers
	material  = getNextByName(material, "material");
	if (material != null) {
	    Node materialr = getNextByName(material, "material");
	    if (materialr != null) {
		question.append("<p><table>");
		// have left and right list
		Node mattextl = null;
		String textl = null;
		Node mattextr = null;
		String textr = null;

		mattextl = getFirstByName(material, "mattext");
		mattextr = getFirstByName(materialr, "mattext");
		
		while (mattextl != null && mattextr != null) {
		    textl = getText(mattextl);
		    textr = getText(mattextr);
		    if (textl == null || textr == null)
			break;

		    question.append("<tr><td>");
		    question.append(textl);
		    question.append("</td><td>");
		    question.append(textr);
		    question.append("</td></tr>");

		    mattextl = getNextByName(mattextl, "mattext");
		    mattextr = getNextByName(mattextr, "mattext");
		}

		question.append("</table>");
	    }
	}

	log.debug("question: {}", question.toString());

	// now build the pairs. we depend upon the specific approach
	// webct uses

	// finding the real pairs is complex. it's easier to start out
	// by looking for the right answers, as that will give us
	// the entries on the right

	List<Pair> rightanswers = new ArrayList<Pair>();

	Node resproc = getFirstByName(item, "resprocessing");
	if (presentation == null) {
	    log.debug("can't find <resprocessing> {}", getAttribute(item, "ident"));
	    return false;
	}

	Node outcomes = getFirstByName(resproc, "outcomes");
	if (outcomes != null) {

	    Node decvar = getFirstByName(outcomes, "decvar");
	    while (decvar != null) {
		try {
		    String varname = getAttribute(decvar, "varname");
		    // log.info("decvar " + varname);
		    String maxval = getAttribute(decvar, "maxvalue");
		    Double numval = Double.parseDouble(maxval);
		    if (varname != null && varname.equals("que_score") && numval > score) {
			score = numval;
			// log.info("scoreset " + numval + " " + maxval);
			scoreset = true;
			break;
		    }
		} catch (Exception ignore) {};
		decvar = getNextByName(decvar, "decvar");
	    }
	}

	Node respcondl = getFirstByName(resproc, "respcondition");
	if (respcondl == null) {
	    log.debug("can't find <respconditionl>");
	    return false;
	}
	
	while (respcondl != null) {
	    Node conditionvar = getFirstByName(respcondl, "conditionvar");
	    if (conditionvar != null) {
		Node varequal = getFirstByName(conditionvar, "varequal");
		if (varequal != null) {
		    String respident = getAttribute(varequal, "respident");
		    String vartext = getNodeText(varequal);
		    if (vartext != null) {
			Node setvar = getNextByName(conditionvar, "setvar");
			if (setvar != null) {
			    String varname = getAttribute(setvar , "varname");
			    if (varname != null && (varname.equals("WebCT_Correct") || (varname.equals("Respondus_Correct")))) {
				String action = getAttribute(setvar , "action");
				if (!scoreset)
				try {
				    String value = getNodeText(setvar);
				    Double numval = Double.parseDouble(value);
				    if (action.equalsIgnoreCase("Set")) {
					if (numval > score)
					    score = numval;
				    } else if (action.equalsIgnoreCase("Add")) {
					if (numval > 0.0) 
					    score = score + numval;
					// log.info("add " + score + " " + numval);
				    }
				} catch (Exception ignore) {};

				rightanswers.add(new Pair(null, respident, null, vartext));
				log.debug("right answer: {}:{}", respident, vartext);
			    }
			}
		    }
		}
	    }
	    respcondl = getNextByName(respcondl, "respcondition");
	}

	Node respgroup = getFirstByName(presentation, "response_grp");
	if (respgroup == null) {
	    respgroup = getFirstByName(presentation, "response_lid");
	    if (respgroup == null) {
		log.debug("can't find <response_grp>");
		return false;
	    }
	}	

	// loop over pairs
	while (respgroup != null) {
	    String lident = getAttribute(respgroup, "ident");
	    if (lident == null) {
		log.debug("Response group has no ident");
		return false;
	    }
	    
	    Node mat = getFirstByName(respgroup, "material");
	    String left = getMatText(mat);

	    // finding the matching text is more complex
	    // we have to look through the responses for the one they
	    // count right

	    Node choice = getFirstByName(respgroup, "render_choice");
	    if (choice == null) {
		log.debug("can't find <render_choice> in response");
		return false;
	    }
	    Node label = getFirstByName(choice, "response_label");
	    if (label == null) {
		log.debug("can't find <response_label> in response");
		return false;
	    }
	    while (label != null) {
		String ident = getAttribute(label, "ident");
		// see if this is a right answer. only process it if so
		if (ident != null) {
		    Iterator rights = rightanswers.iterator();
		    while (rights.hasNext()) {
			Pair rightans = (Pair)rights.next();
			log.debug("allpairs: {}:{}={}:{}", lident, ident, rightans.leftident, rightans.rightident);
			if ((rightans.leftident == null || 
			     rightans.leftident .equals(lident)) &&
			    rightans.rightident.equals(ident)) {
			    // this is the right answer. find the text
			    Node matr = getFirstByName(label, "material");
			    String right = getMatText(matr);

			    log.debug("pair {}:{}:{}:{}", left, lident, right, ident);
			    pairs.add(new Pair(left, lident, right, ident));
			}
		    }
		}

		label = getNextByName(label, "response_label");
	    }

	    Node nextgroup = getNextByName(respgroup, "response_grp");
	    if (nextgroup == null)
		nextgroup = getNextByName(respgroup, "response_lid");
	    respgroup = nextgroup;
	}
	Node feeditem = getFirstByName(item, "itemfeedback");
	if (feeditem != null) {
	    Node matf = getFirstByName(feeditem, "material");
	    feedback = getMatText(matf);
	}
	log.debug("feedback {}", feedback);

	doHeader();
	out.print("<item ident=\""+getAttribute(item, "ident")+"\"");
	if (title != null)
	    out.print(" title=\"" + escapeText(title) + "\"");
	out.println(">");
	out.println("  <itemmetadata>");
	out.println("    <qtimetadata>");
	out.println("      <qtimetadatafield>");
	out.println("        <fieldlabel>qmd_itemtype</fieldlabel>");
	out.println("        <fieldentry>Matching</fieldentry>");
	out.println("      </qtimetadatafield>");
	out.println("    </qtimetadata>");
	out.println("    <qtimetadata>");
	out.println("      <qtimetadatafield>");
	out.println("        <fieldlabel>TEXT_FORMAT</fieldlabel>");
	out.println("        <fieldentry>HTML</fieldentry>");
	out.println("      </qtimetadatafield>");
	out.println("    </qtimetadata>");
	out.println("  </itemmetadata>");
	out.println("  <presentation>");
	out.println("    <flow class=\"Block\">");
	out.println("      <material>");
	out.println("        <mattext charset=\"ascii-us\" texttype=\"text/html\" xml:space=\"default\"><![CDATA[" + question.toString() + "]]></mattext>");
	out.println("      </material>");
	out.println("      <response_grp ident=\"resp_grp\" rcardinality=\"Ordered\" rtiming=\"No\">");
	out.println("        <render_choice shuffle=\"No\">");

	for (Pair leftc: pairs) {
	    out.println("        <response_label ident=\"" + leftc.leftident + "\" rarea=\"Ellipse\" rrange=\"Exact\" rshuffle=\"Yes\"><material><mattext charset=\"ascii-us\" texttype=\"text/html\" xml:space=\"default\"><![CDATA[" + leftc.left + "]]></mattext></material></response_label>");
	}
	for (Pair rightc: pairs) {
	    out.println("        <response_label ident=\"" + rightc.rightident + "\" match_group=\"\" match_max=\"1\" rarea=\"Ellipse\" rrange=\"Exact\" rshuffle=\"Yes\"><material><mattext charset=\"ascii-us\" texttype=\"text/html\" xml:space=\"default\"><![CDATA[" + rightc.right + "]]></mattext></material></response_label>");
	}

	out.println("<response_label rarea=\"Ellipse\" rrange=\"Exact\" rshuffle=\"Yes\"></response_label>");

	out.println("<response_label rarea=\"Ellipse\" rrange=\"Exact\" rshuffle=\"Yes\"></response_label>");
	out.println("</render_choice>");
	out.println("      </response_grp>");
	out.println("    </flow>");
	out.println("  </presentation>");
	out.println("  <resprocessing>");

	if (score <= 0.0) {
	    score = 1.0;
	    noscore++;	
	}

	out.println("    <outcomes>");
	out.println("      <decvar defaultval=\"0\" maxvalue=\"" + score +"\" minvalue=\"0\" varname=\"SCORE\" vartype=\"Integer\"></decvar>");
	out.println("    </outcomes>");
	    
	int numpairs = pairs.size();
	int index = 1;

	// there's a problem here. You're supposed to output every
	// pair, in order to specify feedback for wrong answers.
	// but when I do that, it fails to identify which is right and
	// which is wrong. It appears that it picks up the right and
	// wrong feedback even without any links here, which is wrong
	// but useful.

	for (Pair respl: pairs) {
	    for (Pair respr: pairs) {

		if (respr == respl) {
		    out.println("<respcondition continue=\"No\">");
		    out.println("<conditionvar>");
		    out.println("<varequal case=\"Yes\" index=\""+index+"\" respident=\"" + respl.leftident + "\">" + respr.rightident + "</varequal>");
		    out.println("</conditionvar>");
		    out.println("<setvar action=\"Add\" varname=\"SCORE\">"+score/numpairs+"</setvar>");
		    out.println("</respcondition>");
		    out.println("");
		    index++;
		}
	    }
	}

	out.println("</resprocessing>");

	// WebCT has general feedback only. For this question type
	// samigo doesn't have it, so I implement by setting
	// correct and incorrect to the same thing.

	if (feedback != null && !feedback.trim().equals("")) {

	    out.println("  <itemfeedback ident=\"Correct\" view=\"All\">");
	    out.println("    <flow_mat class=\"Block\">");
	    out.println("      <material>");
	    out.println("        <mattext charset=\"ascii-us\" texttype=\"text/html\" xml:space=\"default\"><![CDATA["+feedback+"]]></mattext>");
	    out.println("      </material>");
	    out.println("    </flow_mat>");
	    out.println("  </itemfeedback>");
	    out.println("  <itemfeedback ident=\"InCorrect\" view=\"All\">");
	    out.println("    <flow_mat class=\"Block\">");
	    out.println("      <material>");
	    out.println("        <mattext charset=\"ascii-us\" texttype=\"text/html\" xml:space=\"default\"><![CDATA["+feedback+"]]></mattext>");
	    out.println("      </material>");
	    out.println("    </flow_mat>");
	    out.println("  </itemfeedback>");

	}

	out.println("</item>");
	return true;

    }

    boolean procpara(Node item) throws IOException{
	log.debug("para");

	String title = null;
	String ident = null;
	String question = null;
	String model = null;
	String feedback = null;
	Double score = 1.0;

	title = getAttribute(item, "title");
	ident = getAttribute(item, "ident");
	log.debug("title: {}", title);

	Node presentation = getFirstByName(item, "presentation");
	if (presentation == null) {
	    log.debug("can't find <presentation>");
	    return false;
	}

	Node material = getFirstByName(presentation, "material");

	question = getMatText(material);

	log.debug("question: {}", question);

	Node feeditem = getFirstByName(item, "itemfeedback");
	boolean haveanswerfeedback = false;
	while (feeditem != null) {
	    String fident = getAttribute(feeditem, "ident");
	    if (fident == null)
		return false;
	    Node matf = getFirstByName(feeditem, "material");
	    if (matf == null)
		return false;
	    String feedtext = getMatText(matf);
	    if (feedtext == null)
		return false;
	    // unlike other types, we're not looking for idents from question processing
	    // feedback can be normal, hint or solution
	    NodeList children = feeditem.getChildNodes();
	    Node child = null;
	    // set child to first real child of feeditem
	    for (int i = 0; i < children.getLength(); i++) {
		if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
		    child = children.item(i);
		    break;
		}
	    }

	    if (child != null) {
		String tagName = child.getNodeName();

		if (tagName.equalsIgnoreCase("flow_mat") || tagName.equalsIgnoreCase("material")) {
		    feedback = appString(feedback, feedtext);
		} else if (tagName.equalsIgnoreCase("solution")) {
		    model = appString(model, feedtext);
		}
	    }

	    feeditem = getNextByName(feeditem, "itemfeedback");
	}

	Node response = getFirstByName(item, "resprocessing");
	if (response != null)
	    response = getFirstByName(response, "outcomes");
	if (response != null)
	    response = getFirstByName(response, "decvar");
	if (response != null)
	    try {
		    String maxval = getAttribute(response, "maxvalue");
		    Double numval = Double.parseDouble(maxval);
		    if (numval > score)
			score = numval;
	    } catch (Exception ignore) {};

	log.debug("feedback {}", feedback);
	log.debug("model {}", model);

	doHeader();
	out.print("<item ident=\""+ident+"\"");
	if (title != null)
	    out.print(" title=\"" + escapeText(title)  + "\"");
	out.println(">");
	out.println("  <itemmetadata>");
	out.println("    <qtimetadata>");
	out.println("      <qtimetadatafield>");
	out.println("        <fieldlabel>qmd_itemtype</fieldlabel>");
	out.println("        <fieldentry>Essay</fieldentry>");
	out.println("      </qtimetadatafield>");
	out.println("    </qtimetadata>");
	out.println("    <qtimetadata>");
	out.println("      <qtimetadatafield>");
	out.println("        <fieldlabel>TEXT_FORMAT</fieldlabel>");
	out.println("        <fieldentry>HTML</fieldentry>");
	out.println("      </qtimetadatafield>");
	out.println("      <qtimetadatafield>");
        out.println("            <fieldlabel>hasRationale</fieldlabel>");
	out.println("            <fieldentry>false</fieldentry>");
	out.println("      </qtimetadatafield>");
	out.println("    </qtimetadata>");
	out.println("  </itemmetadata>");
	out.println("  <presentation label=\"Model Short Answer\">");
	out.println("    <flow class=\"Block\">");
	out.println("      <material>");
	out.println("        <mattext charset=\"ascii-us\" texttype=\"text/html\" xml:space=\"default\"><![CDATA[" + question + "]]></mattext>");
	out.println("      </material>");
	
	out.println("     <response_lid ident=\"LID01\" rcardinality=\"Single\" rtiming=\"No\">");
	out.println("        <render_choice shuffle=\"No\">");
	out.println("          <response_label ident=\"A\" rarea=\"Ellipse\" rrange=\"Exact\" rshuffle=\"Yes\">");
	out.println("            <material>");
	if (model == null)
	    model = "";
	out.println("	  <mattext charset=\"ascii-us\" texttype=\"text/html\" xml:space=\"default\"><![CDATA["+model+"]]></mattext>");
	out.println("            </material>");
	out.println("          </response_label>");
	out.println("</render_choice>");
	out.println("      </response_lid>");
	out.println("    </flow>");
	out.println("  </presentation>");
	out.println("  <resprocessing>");
	out.println("    <outcomes>");
	out.println("      <decvar defaultval=\"0\" maxvalue=\"" + score + "\" minvalue=\"0\" varname=\"SCORE\" vartype=\"Integer\"></decvar>");
	out.println("    </outcomes>");
	out.println("  </resprocessing>");
	paras++;

	if (feedback != null && !feedback.trim().equals("")) {
	    out.println("  <itemfeedback ident=\""+ident+"\" view=\"All\"><flow_mat class=\"Block\"><material><mattext charset=\"ascii-us\" texttype=\"text/plain\" xml:space=\"default\"><![CDATA["+feedback+"]]></mattext>");
	    out.println("</material>");
	    out.println("</flow_mat>");
	    out.println("</itemfeedback>");
	}

	out.println("</item>");

	return true;

    }

    boolean procshort(Node item) throws IOException{
	log.debug("short");

	String title = null;
	String ident = null;
	String question = null;
	List<Shortans> answers = new ArrayList<Shortans>();
	// there are only two possible answers, Samigo only supports correct/incorrect
	// except that essay has only general
	String icfeedback = null;
	String cfeedback = null;
	String gfeedback = null;
	List<String> cident = new ArrayList<String>();
	List<String> icident = new ArrayList<String>();
	List<String> gident = new ArrayList<String>();

	boolean casesens = false; // case sensitive
	Double score = 0.0;
	boolean isPattern = false;

        for (Node meta = getFirstByName(item, "qtimetadatafield");
	     meta != null;
	     meta = getNextByName(meta,"qtimetadatafield")) {

            Node labelNode = getFirstByName(meta, "fieldlabel");
            if (labelNode== null) {
                log.debug("No fieldlabel for qtimetadatafield");
                return false;
            }

            String label = getNodeText(labelNode);
            if (!"cc_profile".equals(label))
                continue;

            Node valueNode = getFirstByName(meta, "fieldentry");
            if (valueNode== null) {
                log.debug("No fieldentry for qtimetadatafield");
                return false;
            }

            String value = getNodeText(valueNode);
            if (value.startsWith("cc.pattern_match")) {
                isPattern = true;
		usesPatternMatch = true;
                break;
            }

        }

	title = getAttribute(item, "title");
	ident = getAttribute(item, "ident");
	log.debug("title: {}", title);

	Node presentation = getFirstByName(item, "presentation");
	if (presentation == null) {
	    log.debug("can't find <presentation>");
	    return false;
	}

	Node material = getFirstByName(presentation, "material");

	question = getMatText(material);

	// flag pattern match questions as needing review.
	// no longer. we actually implement them correctly
	//	if (isPattern)
	//            question = bean.getMessageLocator().getMessage("simplepage.import_cc_pattern") + " " + question;

	log.debug("question: {}", question);

	// the full Qti spec has multiple material and response_str, alternating. 
	// So roses are {} and violets are {} also
	//   is shown as
	// material: roses are
	// response_str
	// material: and violets are
	// response_str
	// also
	// However the CC profile only allows one material and response_str.

	// thus this loop is unnecessary, but for the moment I'm leaving it

	Node response = getFirstByName(presentation, "response_str");
	while (response != null) {
	    Node fib = getFirstByName(response, "render_fib");
	    if (fib == null) {
		log.debug("No render_fib for response_str");
		return false;
	    }
	    String qident = getAttribute(response, "ident");
	    String rident = null;
	    Node label = getFirstByName(fib, "response_label");
	    if (label != null)
		rident = getAttribute(label, "ident");

	    if (qident == null && rident == null) {
		log.debug("No ident for response_label");
		return false;
	    }

	    log.debug("blank: {}:{}", qident, rident);
	    answers.add(new Shortans(qident, rident));
	    response = getNextByName(response,"response_str");
	}

	Node resproc = getFirstByName(item, "resprocessing");
	if (presentation == null) {
	    log.debug("can't find <resprocessing> {}", getAttribute(item, "ident"));
	    return false;
	}

	Node outcomes = getFirstByName(resproc, "outcomes");
	if (outcomes != null) {

	    Node decvar = getFirstByName(outcomes, "decvar");
	    if (decvar != null) {

		try {
		    String maxval = getAttribute(decvar, "maxvalue");
		    Double numval = Double.parseDouble(maxval);
		    if (numval > score)
			score = numval;
		} catch (Exception ignore) {};
	    }
	}

	Node respcondl = getFirstByName(resproc, "respcondition");
	if (respcondl == null) {
	    log.debug("can't find <respconditionl>");
	    return false;
	}
	
	log.debug("point 1");

	while (respcondl != null) {
	    String contin = getAttribute(respcondl, "continue");
	    Node conditionvar = getFirstByName(respcondl, "conditionvar");

	    if (contin.equalsIgnoreCase("Yes") && getFirstByName(conditionvar, "other") != null) { // general feedback
		Node disfeedback = getFirstByName(respcondl, "displayfeedback");
		while (disfeedback != null) {
		    String feedstring = getAttribute(disfeedback, "linkrefid");
		    if (feedstring != null && feedstring.length() > 0) {
			cident.add(feedstring);  // handle general by both correct/incorrect
			icident.add(feedstring);
		    }
		    disfeedback = getNextByName(disfeedback, "displayfeedback");			
		}
	    } else if (getFirstByName(conditionvar, "other") != null) {  // incorrect feedback
		Node disfeedback = getFirstByName(respcondl, "displayfeedback");
		while (disfeedback != null) {
		    String feedstring = getAttribute(disfeedback, "linkrefid");
		    if (feedstring != null && feedstring.length() > 0)
			icident.add(feedstring);
		    disfeedback = getNextByName(disfeedback, "displayfeedback");			
		}
	    } else if (conditionvar != null) {
		// if there's an <or>, use it
		// now check for both varequal and varsubstring
		Node varequal = getFirstByName(conditionvar, "varequal");
		while (varequal != null) {
		    String vtext = getNodeText(varequal);
		    String vident = getAttribute(varequal, "respident");
		    String vcase = getAttribute(varequal, "case");
		    if (vtext != null && vident != null) {
			for (Shortans ans: answers) {
			    if (ans.qident != null && ans.qident.equals(vident)
				|| ans.rident != null && ans.rident.equals(vident)) {
				if (ans.answer == null)
				    ans.answer = vtext;
				else 
				    ans.answer = ans.answer + "|" + vtext;
			        
				log.debug("answer: {}", ans.answer);
			    }
			}
			if (vcase != null && vcase.equalsIgnoreCase("Yes"))
			    casesens = true;
		    }
		    varequal = getNextByName(varequal, "varequal");
		}

		// and varsubset
		Node varsubset = getFirstByName(conditionvar, "varsubstring");
		while (varsubset != null) {
		    String vtext = getNodeText(varsubset);
		    String vident = getAttribute(varsubset, "respident");
		    String vcase = getAttribute(varsubset, "case");
		    if (vtext != null && vident != null) {
			for (Shortans ans: answers) {
			    if (ans.qident != null && ans.qident.equals(vident)
				|| ans.rident != null && ans.rident.equals(vident)) {
				if (ans.answer == null)
				    ans.answer = "*" + vtext + "*";
				else 
				    ans.answer = ans.answer + "|*" + vtext + "*";
			    }
			}
			if (vcase != null && vcase.equalsIgnoreCase("Yes"))
			    casesens = true;
		    }
		    varsubset = getNextByName(varsubset, "varsubset");
		}

		// regex; preserve it to make conversion easier
		Node varregexp = getFirstByName(conditionvar, "var_extension");
		while (varregexp != null) {
		    Node webct = getFirstByName(varregexp, "webct:x_webct_v01_varregex");
		    if (webct == null)
			continue;
		    String vtext = getNodeText(webct);
		    String vident = getAttribute(webct, "respident");
		    String vcase = getAttribute(webct, "case");
		    if (vtext != null && vident != null) {
			for (Shortans ans: answers) {
			    if (ans.qident != null && ans.qident.equals(vident)
				|| ans.rident != null && ans.rident.equals(vident)) {
				if (ans.answer == null)
				    ans.answer = "REGEXP:" + vtext;
				else 
				    ans.answer = ans.answer + "|REGEXP:" + vtext;
			    }
			}
			if (vcase != null && vcase.equalsIgnoreCase("Yes"))
			    casesens = true;
		    }
		    varregexp = getNextByName(varregexp, "var_extension");
		}
		// correct feedback
		Node disfeedback = getFirstByName(respcondl, "displayfeedback");
		while (disfeedback != null) {
		    String feedstring = getAttribute(disfeedback, "linkrefid");
		    if (feedstring != null && feedstring.length() > 0)
			cident.add(feedstring);
		    disfeedback = getNextByName(disfeedback, "displayfeedback");			
		}
	    }
	    respcondl = getNextByName(respcondl, "respcondition");
	}

	log.debug("point 2");

	Node feeditem = getFirstByName(item, "itemfeedback");
	while (feeditem != null) {
	    String fident = getAttribute(feeditem, "ident");
	    if (fident == null)
		return false;
	    Node matf = getFirstByName(feeditem, "material");
	    if (matf == null)
		return false;
	    String feedtext = getMatText(matf);
	    if (feedtext == null)
		return false;
	    if (cident.contains(fident))
		cfeedback = appString(cfeedback, feedtext);
	    if (icident.contains(fident))
		icfeedback = appString(icfeedback, feedtext);

	    feeditem = getNextByName(feeditem, "itemfeedback");
	}

	log.debug("feedback {}", cfeedback);

	doHeader();
	out.print("<item ident=\""+ident+"\"");
	if (title != null)
	    out.print(" title=\"" + escapeText(title)  + "\"");
	out.println(">");
	out.println("  <itemmetadata>");
	out.println("    <qtimetadata>");
	out.println("      <qtimetadatafield>");
	out.println("        <fieldlabel>qmd_itemtype</fieldlabel>");
	out.println("        <fieldentry>Fill In the Blank</fieldentry>");
	out.println("      </qtimetadatafield>");
	out.println("    </qtimetadata>");
	out.println("    <qtimetadata>");
	out.println("      <qtimetadatafield>");
	out.println("        <fieldlabel>TEXT_FORMAT</fieldlabel>");
	out.println("        <fieldentry>HTML</fieldentry>");
	out.println("      </qtimetadatafield>");
	out.println("    </qtimetadata>");
	out.println("    <qtimetadata>");
	out.println("      <qtimetadatafield>");
	out.println("        <fieldlabel>CASE_SENSITIVE</fieldlabel>");
	out.println("        <fieldentry>"+casesens+"</fieldentry>");
	out.println("      </qtimetadatafield>");
	out.println("      <qtimetadatafield>");
        out.println("            <fieldlabel>hasRationale</fieldlabel>");
	out.println("            <fieldentry>false</fieldentry>");
	out.println("      </qtimetadatafield>");
	out.println("    </qtimetadata>");
	out.println("  </itemmetadata>");
	out.println("  <presentation label=\"FIB\">");
	out.println("    <flow class=\"Block\">");
	out.println("    <flow class=\"Block\">");
	out.println("      <material>");

	// there's an issue here. The restricted CC profile is unable to handle a blank in the middle
	// of the question. So the samples all put ___ in the question to show where the real blank is.
	// Samigo will then insert a box at the end of the question. I originally thought it would be
	// nice to put that on a separate line. The probelm is that if we then do a CC export, we've added
	// junk that wasn't in the original, so repeated export and import keeps adding things. It seems
	// like it's safest to leave the text alone.
	
	//out.println("        <mattext charset=\"ascii-us\" texttype=\"text/html\" xml:space=\"default\"><![CDATA[" + question + "<p> 1. ]]></mattext>");
	out.println("        <mattext charset=\"ascii-us\" texttype=\"text/html\" xml:space=\"default\"><![CDATA[" + question + "]]></mattext>");
	out.println("      </material>");
	
// following should not be needed with CC profile
// if it is actually needed we need to save the original text
	for (int i = 2; i <= answers.size(); i++) {
	    out.println("      <material>");
	    out.println("        <mattext charset=\"ascii-us\" texttype=\"text/html\" xml:space=\"default\"><![CDATA[<p> "+i+". ]]></mattext>");
	    out.println("      </material>");
	}

//  this loop should happen only once.
	for (Shortans ans: answers) {
	    String rident = ans.rident;
	    if (ident == null)
		ident = ans.qident;

	    out.println("<response_str ident=\""+rident+"\" rcardinality=\"Ordered\" rtiming=\"No\"><render_fib charset=\"ascii-us\" columns=\"20\" encoding=\"UTF_8\" fibtype=\"String\" prompt=\"Box\" rows=\"1\"></render_fib>");
	    out.println("");
	    out.println("</response_str>");
	    out.println("");
	    out.println("<material><mattext charset=\"ascii-us\" texttype=\"text/html\" xml:space=\"default\"><![CDATA[]]></mattext>");
	    out.println("");
	    out.println("</material>");

	}

	out.println("</flow>");
	out.println("</flow>");
	out.println("</presentation>");

	if (score <= 0.0) {
	    score = 1.0;
	    noscore++;
        }

	out.println("  <resprocessing>");
	out.println("    <outcomes>");
	out.println("      <decvar defaultval=\"0\" maxvalue=\"" + score + "\" minvalue=\"0\" varname=\"SCORE\" vartype=\"Integer\"></decvar>");
	out.println("");
	out.println("");
	out.println("    </outcomes>");

	for (Shortans ans: answers) {
	    String rident = ans.rident;
	    if (rident == null)
		rident = ans.qident;

	    out.println("  <respcondition continue=\"Yes\"><conditionvar><or><varequal case=\"No\" respident=\""+rident+"\"><![CDATA["+ans.answer+"]]></varequal>");
	    out.println("");
	    out.println("</or>");
	    out.println("");
	    out.println("</conditionvar>");
	    out.println("");
	    out.println("<setvar action=\"Add\" varname=\"SCORE\">"+score/answers.size()+"</setvar>");
	    out.println("");
	    out.println("</respcondition>");
	}


	out.println("</resprocessing>");

	if (cfeedback != null && !cfeedback.trim().equals("")) {

	    out.println("  <itemfeedback ident=\"Correct\" view=\"All\">");
	    out.println("    <flow_mat class=\"Block\">");
	    out.println("      <material>");
	    out.println("        <mattext charset=\"ascii-us\" texttype=\"text/html\" xml:space=\"default\"><![CDATA["+cfeedback+"]]></mattext>");
	    out.println("      </material>");
	    out.println("    </flow_mat>");
	    out.println("  </itemfeedback>");

	}

	if (icfeedback != null && !icfeedback.trim().equals("")) {
	    out.println("  <itemfeedback ident=\"InCorrect\" view=\"All\">");
	    out.println("    <flow_mat class=\"Block\">");
	    out.println("      <material>");
	    out.println("        <mattext charset=\"ascii-us\" texttype=\"text/html\" xml:space=\"default\"><![CDATA["+icfeedback+"]]></mattext>");
	    out.println("      </material>");
	    out.println("    </flow_mat>");
	    out.println("  </itemfeedback>");

	}

	out.println("</item>");

	return true;

    }

    boolean procmc(Node item, boolean truefalse) throws IOException{
	log.debug("mc");

	String title = null;
	String ident = null;
	String rlident = null;
	String question = null;
	List<Mcans> answers = new ArrayList<Mcans>();
	String feedback = null;
	String icfeedback = null;
	String cfeedback = null;
	List<String> fbident = new ArrayList<String>();
	List<String> icident = new ArrayList<String>();
	List<String> cident = new ArrayList<String>();
	Double score = 0.0;
	boolean scoreset = false;

	title = getAttribute(item, "title");
	ident = getAttribute(item, "ident");
	log.debug("title: {}", title);

	Node presentation = getFirstByName(item, "presentation");
	if (presentation == null) {
	    log.debug("can't find <presentation>");
	    return false;
	}

	Node material = getFirstByName(presentation, "material");

	question = getMatText(material);

	log.debug("question: {}", question);

	Node response = getFirstByName(presentation, "response_lid");
	if (response == null) {
	    log.debug("No response_lid");
	    return false;
	}
	String rcardinality = getAttribute(response, "rcardinality");
	Node choice = getFirstByName(response, "render_choice");
	if (choice == null) {
	    log.debug("No render_choice");
	    return false;
	}
	Node label = getFirstByName(choice, "response_label");
	if (label == null) {
	    log.debug("No response_label");
	    return false;
	}
	byte [] newident = {65};
	while (label != null) {
	    String lident = getAttribute(label, "ident");
	    Node lmaterial = getFirstByName(label, "material");
	    String ltext = getMatText(lmaterial);
	    
	    if (lident != null && ltext != null) {
		log.debug("answer {} {} {}", lident, newident, ltext);
		answers.add(new Mcans(lident, new String(newident), ltext));
		newident[0]++;
	    }
	    label = getNextByName(label, "response_label");
	}

	Node resproc = getFirstByName(item, "resprocessing");
	if (resproc == null) {
	    log.debug("can't find <resprocessing> {}", getAttribute(item, "ident"));
	    return false;
	}

	Node outcomes = getFirstByName(resproc, "outcomes");
	if (outcomes != null) {

	    Node decvar = getFirstByName(outcomes, "decvar");
	    if (decvar != null) {

		try {
		    String maxval = getAttribute(decvar, "maxvalue");
		    Double numval = Double.parseDouble(maxval);
		    if (numval > score) {
			score = numval;
			scoreset = true;
			log.debug("set score {}", score);
		    }
		} catch (Exception ignore) {};
	    }
	}

	Node respcondl = getFirstByName(resproc, "respcondition");
	if (respcondl == null) {
	    log.debug("can't find <respconditionl>");
	    return false;
	}
	
	int numcorrect = 0;
	while (respcondl != null) {
	    String rctitle = getAttribute(respcondl, "title");
	    String contin = getAttribute(respcondl, "continue");
	    Node conditionvar = getFirstByName(respcondl, "conditionvar");

	    if (contin.equalsIgnoreCase("Yes") && getFirstByName(conditionvar, "other") != null) { // general feedback
		Node disfeedback = getFirstByName(respcondl, "displayfeedback");
		while (disfeedback != null) {
		    String feedstring = getAttribute(disfeedback, "linkrefid");
		    if (feedstring != null && feedstring.length() > 0)
			fbident.add(feedstring);
		    disfeedback = getNextByName(disfeedback, "displayfeedback");			
		}
	    } else if (getFirstByName(conditionvar, "other") != null) {  // incorrect feedback
		Node disfeedback = getFirstByName(respcondl, "displayfeedback");
		while (disfeedback != null) {
		    String feedstring = getAttribute(disfeedback, "linkrefid");
		    if (feedstring != null && feedstring.length() > 0)
			icident.add(feedstring);
		    disfeedback = getNextByName(disfeedback, "displayfeedback");			
		}
	    } else if (conditionvar != null)  { // normal alternative. can't do much if no conditionvar
		// in this profile, the only possibilities are a simple varequal, an <or> of varequals, or an <and> of varequal and <not> varequal.
		Node firsttest = getNextElement(conditionvar.getFirstChild());
		Node varequal = null;
		if (firsttest.getNodeName().equals("varequal"))
		    varequal = firsttest;  // just one varequal. use it
		else if (firsttest.getNodeName().equals("or"))
		    varequal = getNextElement(firsttest.getFirstChild());   // or, use all of its children
		else if (firsttest.getNodeName().equals("and")) {
		    varequal = firsttest.getFirstChild();   // and, has both varequal and not under it. 
		    if (!varequal.getNodeName().equals("varequal"))
			varequal = getNextByName(varequal,"varequal");  // first next must be a <not>, find first varequal
		}
		while (varequal != null) {
		    String vtext = getNodeText(varequal);
		    // don't use the respident because there's only one variable
		    Mcans answer = null;
		    Mcans altanswer = null;
		    for (Mcans ans: answers) {
			if (vtext != null && vtext.equals(ans.ident)) {
			    answer = ans;
			    break;
			}
			// this is absolutely wrong, but some of the validation tests do it for true/false
			if (vtext != null && vtext.equalsIgnoreCase(ans.answer)) {
			    altanswer = ans;
			    break;
			}

		    }
		    if (answer == null) {
			if (altanswer != null) {
			    log.debug("id in respcondition not matching question {} but answer matches ", question);
			    answer = altanswer;
			} else {
			    log.debug("id in respcondition not matching question {}", question);
			    break;
			}
		    }
		    // for this profile correct answer is always
		    // <setvar action="Set" varname="SCORE">100</setvar>
		    Node setvar = getFirstByName(respcondl, "setvar");
		    if (setvar != null && getAttribute(setvar, "action").equalsIgnoreCase("set")
			&& getAttribute(setvar, "varname").equalsIgnoreCase("score")
			&& "100".equals(getNodeText(setvar))) {
			answer.correct = true;
			numcorrect++;
		    }
		    // look for answer specific feedback
		    // yes this is inside the varequal loop. if more than one answer is
		    // being tested need to give this feedback to them all
		    Node disfeedback = getFirstByName(respcondl, "displayfeedback");
		    // correct actually goes in answewr-specific. The only way I see to 
		    // distinguish is the label. Yuck.
		    while (disfeedback != null) {
			String feedstring = getAttribute(disfeedback, "linkrefid");
			if (feedstring != null && feedstring.equals("correct_fb")) {
			    // do this only once
			    if (numcorrect == 1)
				cident.add(feedstring);
			} else if (feedstring != null)
			    answer.fbident.add(feedstring);
			disfeedback = getNextByName(disfeedback, "displayfeedback");			
		    }

		    varequal = getNextByName(varequal, "varequal");		    
		}

	    }
	    respcondl = getNextByName(respcondl, "respcondition");
	}

	// in answers, add actual feedback text
	Node feeditem = getFirstByName(item, "itemfeedback");
	boolean haveanswerfeedback = false;
	while (feeditem != null) {
	    String fident = getAttribute(feeditem, "ident");
	    if (fident == null)
		return false;
	    Node matf = getFirstByName(feeditem, "material");
	    if (matf == null)
		return false;
	    String feedtext = getMatText(matf);
	    if (feedtext == null)
		return false;
	    if (fbident.contains(fident))
		feedback = appString(feedback, feedtext);
	    if (icident.contains(fident))
		icfeedback = appString(icfeedback, feedtext);
	    if (cident.contains(fident))
		cfeedback = appString(icfeedback, feedtext);
	    
	    for (Mcans ans: answers) {
		if (ans.fbident.contains(fident)){
		    if (truefalse) {  // samigo doesn't seem to use item feedback for truefalse
			if (ans.correct)
			    cfeedback = appString(cfeedback, feedtext);
			else
			    icfeedback = appString(icfeedback, feedtext);
		    } else {
			if (!feedtext.trim().equals(""))
			    haveanswerfeedback = true;
			ans.feedback = appString(ans.feedback, feedtext);
		    }
		}
	    }
	    feeditem = getNextByName(feeditem, "itemfeedback");
	}

	log.debug("feedback {}", feedback);

	doHeader();
	out.print("<item ident=\""+ident+"\"");
	if (title != null)
	    out.print(" title=\"" + escapeText(title) + "\"");
	out.println(">");
	out.println("  <itemmetadata>");
	out.println("    <qtimetadata>");
	out.println("      <qtimetadatafield>");
	out.println("        <fieldlabel>qmd_itemtype</fieldlabel>");
	if (truefalse)
	    out.println("        <fieldentry>True False</fieldentry>");
	else if (rcardinality != null && rcardinality.toLowerCase().equals("multiple"))
	    out.println("        <fieldentry>Multiple Correct Answer</fieldentry>");
	else if (numcorrect > 1)
	    out.println("        <fieldentry>Multiple Correct Single Selection</fieldentry>");	    
	else
	    out.println("        <fieldentry>Multiple Choice</fieldentry>");
	out.println("      </qtimetadatafield>");
	out.println("      <qtimetadatafield>");
        out.println("            <fieldlabel>hasRationale</fieldlabel>");
	out.println("            <fieldentry>false</fieldentry>");
	out.println("      </qtimetadatafield>");
	out.println("      <qtimetadatafield>");
	out.println("        <fieldlabel>TEXT_FORMAT</fieldlabel>");
	out.println("        <fieldentry>HTML</fieldentry>");
	out.println("      </qtimetadatafield>");
	out.println("      <qtimetadatafield>");
	out.println("        <fieldlabel>RANDOMIZE</fieldlabel>");
	out.println("        <fieldentry>true</fieldentry>");
	out.println("      </qtimetadatafield>");
	out.println("    </qtimetadata>");
	out.println("  </itemmetadata>");
	out.println("  <presentation>");
	out.println("    <flow class=\"Block\">");
	out.println("      <material>");
	out.println("        <mattext charset=\"ascii-us\" texttype=\"text/html\" xml:space=\"default\"><![CDATA[" + question + "]]></mattext>");
	out.println("      </material>");
	
	if (rcardinality != null)
	    out.println("     <response_lid ident=\"MCSC\" rcardinality=\"" + rcardinality + "\" rtiming=\"No\">");
	else
	    out.println("     <response_lid ident=\"MCSC\" rcardinality=\"Single\" rtiming=\"No\">");
	out.println("        <render_choice shuffle=\"No\">");

	for (Mcans ans:answers) {
	    out.println("          <response_label ident=\""+ans.newident+"\" rarea=\"Ellipse\" rrange=\"Exact\" rshuffle=\"Yes\">");
	    out.println("            <material>");
	    out.println("	  <mattext charset=\"ascii-us\" texttype=\"text/html\" xml:space=\"default\"><![CDATA["+ans.answer+"]]></mattext>");
	    out.println("            </material>");
	    out.println("          </response_label>");
	}

	out.println("</render_choice>");
	out.println("      </response_lid>");
	out.println("    </flow>");
	out.println("  </presentation>");

	out.println("  <resprocessing>");

	if (score <= 0.0) {
	    score = 1.0;
	    noscore++;
        }

	out.println("    <outcomes>");
	out.println("      <decvar defaultval=\"0\" maxvalue=\"" + score + "\" minvalue=\"0\" varname=\"SCORE\" vartype=\"Integer\"></decvar>");
	out.println("    </outcomes>");

	// if we have any answer specific feedback we have to set it for
	// all, or the input parser blows up
	for (Mcans ans:answers) {
	    if (ans.correct)
		out.println("    <respcondition continue=\"No\" title=\"Correct\">");
	    else
		out.println("    <respcondition continue=\"No\">");
	    out.println("      <conditionvar>");
	    out.println("        <varequal case=\"Yes\" respident=\"MCSC\">"+ans.newident+"</varequal>");
	    out.println("      </conditionvar>");
	    if (ans.correct)
		out.println("      <setvar action=\"Add\" varname=\"SCORE\">" + score + "</setvar>");
	    out.println("      <displayfeedback feedbacktype=\"Response\" linkrefid=\""+(ans.correct?"Correct":"Incorrect")+"\"></displayfeedback>");
	    if (haveanswerfeedback) {
		out.println("     <displayfeedback feedbacktype=\"Response\" linkrefid=\"AnswerFeedback\"><![CDATA["+(ans.feedback!=null?ans.feedback:"")+"]]></displayfeedback>");
		// this is what the QTI spec would call for, but above seems to be what samigo wants
		// out.println("     <displayfeedback feedbacktype=\"Response\" linkrefid=\""+ans.newident+"1\"></displayfeedback>");
	    }
	    out.println("    </respcondition>");
	}

	out.println("  </resprocessing>");

	if (feedback != null && !feedback.trim().equals("") ||
	    cfeedback != null && !cfeedback.trim().equals("")) {
	    out.println(" <itemfeedback ident=\"Correct\" view=\"All\">");
	    out.println("    <flow_mat class=\"Block\">");
	    out.println("      <material>");
	    out.println("      <mattext charset=\"ascii-us\" texttype=\"text/html\" xml:space=\"default\"><![CDATA["+appString(cfeedback, feedback)+"]]></mattext>");
	    out.println("      </material>");
	    out.println("    </flow_mat>");
	    out.println("  </itemfeedback>");
	}
	if (feedback != null && !feedback.trim().equals("") ||
	    icfeedback != null && !icfeedback.trim().equals("")) {
	    out.println("  <itemfeedback ident=\"InCorrect\" view=\"All\">");
	    out.println("    <flow_mat class=\"Block\">");
	    out.println("      <material>");
	    out.println("      <mattext charset=\"ascii-us\" texttype=\"text/html\" xml:space=\"default\"><![CDATA["+appString(icfeedback, feedback)+"]]></mattext>");
	    out.println("      </material>");
	    out.println("    </flow_mat>");
	    out.println("  </itemfeedback>");
	}
	if (false) {
	    // this is what we'd expect from QTI, but Samigo doesn't seem to want it
	if (haveanswerfeedback) {
	    for (Mcans ans:answers) {
		out.println("  <itemfeedback ident=\""+ans.newident+"1\" view=\"All\">");
		out.println("    <flow_mat class=\"Block\">");
		out.println("      <material>");
		out.println("      <mattext charset=\"ascii-us\" texttype=\"text/html\" xml:space=\"default\"><![CDATA["+ans.feedback+"]]></mattext>");
		out.println("      </material>");
		out.println("    </flow_mat>");
		out.println("  </itemfeedback>");
	    }
	}
	}

	out.println("</item>");

	return true;

    }

    boolean procitem(Node item)throws IOException {

	Node resproc = getFirstByName(item, "resprocessing");
	if (resproc == null) {
	    log.debug("can't find <resprocessing> {}", getAttribute(item, "ident"));
	}

	if (resproc != null) {
	   Node extension = getFirstByName(resproc, "itemproc_extension");
	   if (extension != null) {
	       log.debug("Item is a WebCT extension, can't process");
	       return false;
  	   }
        }

	Node qticomment = null;
	if (resproc != null)
	    qticomment = getFirstByName(resproc, "qticomment");

	if (qticomment == null) {
	    String type = guessQuestionType(item);
	    log.debug("main we guess type {}", type);

	    // matching not used in CC, not tested
	    if (type.equalsIgnoreCase("Matching"))
		return procmatch(item);
	    else if (type.equalsIgnoreCase("Short Answers/Essay"))
		return procpara(item);
	    // pattern match will be seen as FIB, which is best we can do
	    else if (type.equalsIgnoreCase("Fill In the Blank"))
		return procshort(item);
	    else if (type.equalsIgnoreCase("Multiple Choice"))
		return procmc(item, false);
	    else if (type.equalsIgnoreCase("Multiple Correct"))
		return procmc(item, false);
	    else if (type.equalsIgnoreCase("True False"))
		return procmc(item, true);
	    else {
	        log.debug("type not matched");
		return false;
	    }
	}

	String commenttext = getNodeText(qticomment);
	if (commenttext == null) {
	    log.debug("No text in <qticomment>");
	    return false;
	}

	if (commenttext.startsWith("Match Question"))
	    return procmatch(item);
	else if (commenttext.startsWith("Paragraph Question"))
	    return procpara(item);
	else if (commenttext.startsWith("Short answer Question"))
	    return procshort(item);
	else if (commenttext.startsWith("Multiple Choice Question"))
	    return procmc(item, false);

	return false;
    }

    int fileno = -1;
    int identno = 1;
    int partno = 1;

    public void doHeader() throws IOException {
	if (!needHeader)
	    return;
	fileno++;

	out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
	out.println("<questestinterop>");
	out.println("  <assessment ident=\"samigo" + identno + "\" title=\"" +
		    escapeText(title + (partno == 1 ? "" :
					(" Part " + partno)))
		    + "\">");
	identno++;
	out.println("");                        
	if (timelimit != null && !timelimit.equals(""))
	    out.println("<duration>PT" + timelimit + "M</duration>");
	out.println("<qtimetadata>");
	out.println("  <qtimetadatafield>");
	out.println("    <fieldlabel>FEEDBACK_DELIVERY</fieldlabel>");
	out.println("    <fieldentry>" + (feedbackpermitted ? "ON_SUBMISSION" : "NONE") + "</fieldentry>");
	out.println("  </qtimetadatafield>");
	out.println("  <qtimetadatafield>");
	out.println("    <fieldlabel>ASSESSMENT_RELEASED_TO</fieldlabel>");
	out.println("    <fieldentry>" + escapeText(siteId) + "</fieldentry>");
	out.println("  </qtimetadatafield>");
	out.println("  <qtimetadatafield>");
	out.println("    <fieldlabel>FEEDBACK_COMPONENT_OPTION</fieldlabel>");
	out.println("    <fieldentry>" + (feedbackpermitted ? "SELECT_COMPONENTS" : "SHOW_TOTALSCORE_ONLY") + "</fieldentry>");
	out.println("  </qtimetadatafield>");
	out.println("  <qtimetadatafield>");
	out.println("     <fieldlabel>FEEDBACK_SHOW_ITEM_LEVEL</fieldlabel>");
	out.println("     <fieldentry>" + feedbackpermitted + "</fieldentry>");
	out.println("  </qtimetadatafield>");
	out.println("  <qtimetadatafield>");
	out.println("    <fieldlabel>FEEDBACK_SHOW_STUDENT_SCORE</fieldlabel>");
	out.println("    <fieldentry>" + feedbackpermitted + "</fieldentry>");
	out.println("  </qtimetadatafield>");
	out.println("  <qtimetadatafield>");
	out.println("    <fieldlabel>FEEDBACK_SHOW_QUESTION</fieldlabel>");
	out.println("    <fieldentry>" + feedbackpermitted + "</fieldentry>");
	out.println("  </qtimetadatafield>");
	out.println("  <qtimetadatafield>");
	out.println("    <fieldlabel>FEEDBACK_SHOW_RESPONSE</fieldlabel>");
	out.println("    <fieldentry>" + feedbackpermitted + "</fieldentry>");
	out.println("  </qtimetadatafield>");
	out.println("  <qtimetadatafield>");
	out.println("    <fieldlabel>FEEDBACK_SHOW_GRADER_COMMENT</fieldlabel>");
	out.println("    <fieldentry>" + feedbackpermitted + "</fieldentry>");
	out.println("  </qtimetadatafield>");
	out.println("  <qtimetadatafield>");
	out.println("    <fieldlabel>FEEDBACK_SHOW_STATS</fieldlabel>");
	out.println("    <fieldentry>" + feedbackpermitted + "</fieldentry>");
	out.println("  </qtimetadatafield>");
	out.println("  <qtimetadatafield>");
	out.println("     <fieldlabel>FEEDBACK_SHOW_SELECTION_LEVEL</fieldlabel>");
	out.println("     <fieldentry>" + feedbackpermitted + "</fieldentry>");
	out.println("  </qtimetadatafield>");
	out.println("  <qtimetadatafield>");
	out.println("     <fieldlabel>LATE_HANDLING</fieldlabel>");
	out.println("     <fieldentry>" + allowlate + "</fieldentry>");
	out.println("  </qtimetadatafield>");
	if (maxattempts != null && !maxattempts.equals("")) {
	    if (maxattempts.equalsIgnoreCase("unlimited"))
		maxattempts = "9999";
	}
	out.println("  <qtimetadatafield>");
	out.println("    <fieldlabel>MAX_ATTEMPTS</fieldlabel>");
	out.println("    <fieldentry>" + maxattempts + "</fieldentry>");
	out.println("  </qtimetadatafield>");
	out.println("</qtimetadata>");
	out.println("");

	out.println("  <section ident=\"samigo"+identno+"\" title=\"Default\">");
	identno++;
	needHeader = false;
    }

    public String getChars () {
        if (out != null)
	    return charout.toString();
	else
	    return null;
    }

    public String getTitle () {
	return title;
    }

    public int getNoScore () {
	return noscore;
    }

    public int getParas () {
	return paras;
    }


    public boolean getUsesCurriculum() {
	return usesCurriculum;
    }

    public boolean mainproc (InputStream i, PrintWriter o, boolean isBank, String base, String s, SimplePageBean b, Document d) throws IOException {

        out = o;
	filebase = base;
	siteId = s;
	bean = b;



	try {
	    if (d != null)
		document = d;
	    else {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
		factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		DocumentBuilder builder = factory.newDocumentBuilder();
		document = builder.parse(i);
	    }

	    NodeList sections = null;
	    if (isBank) {
		sections = document.getElementsByTagName("objectbank");
		title = "Imported Pool";
	    } else {
		NodeList assessments = document.getElementsByTagName("assessment");
		Node assessment = assessments.item(0);
		title = getAttribute(assessment, "title");
		sections = document.getElementsByTagName("section");

		Node md = getFirstByName(assessment, "qtimetadata");
		if (md != null) {
		    NodeList mds = ((Element)md).getElementsByTagName("qtimetadatafield");
		    if (mds != null) {
			for (int n = 0; n < mds.getLength(); n++) {
			    Node item = mds.item(n);
			    if (item == null)
				continue;
			    Node l = getFirstByName(item, "fieldlabel");
			    String label = (l == null ? null : getNodeText(l));
			    Node e = getFirstByName(item, "fieldentry");
			    String entry = (e == null ? null : getNodeText(e));			    

			    if ("qmd_feedbackpermitted".equals(label))
				feedbackpermitted = "yes".equalsIgnoreCase(entry);
			    else if ("qmd_timelimit".equals(label))
				timelimit = entry;
			    else if ("cc_allow_late_submissions".equals(label))
				allowlate = "yes".equalsIgnoreCase(entry);
			    else if ("cc_maxattempts".equals(label))
				maxattempts = entry;
			}
		    }
		}
	    }

	    Node section;
	    int numsections;

	    if (sections == null)
		numsections = 0;
	    else
		numsections = sections.getLength();

	    int questionno = 0;

	    for (int sectionno=0; sectionno < numsections; sectionno++) {
		section = sections.item(sectionno);

		log.debug("section");
		
		NodeList items = ((Element)section).getElementsByTagName("item");
		int numitems;
		Node item;

		if (items == null)
		    numitems = 0;
		else
		    numitems = items.getLength();

		for (int itemno=0; itemno < numitems; itemno++) {
		    item = items.item(itemno);

		    String newtitle = getAttribute(section, "title");
		    if (newtitle == null || newtitle.equals(""))
			newtitle = "Respondus Converted Questions";

		    Node md = getFirstByName(item, "itemmetadata");
		    if (md != null && getFirstByName(md, "curriculumStandardsMetadataSet") != null)
			usesCurriculum = true;

		    // for the moment can only write one section
		    if (false) {
		    if (title == null || !title.equals(newtitle)) {

			if (out != null) {
			    out.println("</section>");
			    out.println("</assessment>");
			    out.println("</questestinterop>");
			    out.close();
			}

		        needHeader = true;
			partno = 1;
			questionno = 0;
			title = newtitle;
		    }
		    }

		    // only count question if it is put to the output
		    if (procitem(item))
			questionno++;
			
		    if (false) {
		    if (questionno >= 100) {
			out.println("</section>");
			out.println("</assessment>");
			out.println("</questestinterop>");
			out.close();
			needHeader = true;
			partno++;
			questionno = 0;
		    }
		    }

		    item = item.getNextSibling();
		}

	    }
		
	    // if there are no valid items, still need the header. This will
	    // only output the header if it hasn't been done already
	    doHeader();

	    if (out != null) {
		out.println("</section>");
		out.println("</assessment>");
		out.println("</questestinterop>");
		out.close();
	    }

	} catch (SAXException sxe) {
	    // Error generated during parsing
	    Exception  x = sxe;
	    if (sxe.getException() != null)
		x = sxe.getException();
	    log.error(x.getMessage(), x);
	    
	} catch (ParserConfigurationException pce) {
	    // Parser with specified options can't be built
	    log.error(pce.getMessage(), pce);
	    
	} catch (IOException ioe) {
	    // I/O error
	    log.error(ioe.getMessage(), ioe);
	}

	return usesPatternMatch;

    }

}
