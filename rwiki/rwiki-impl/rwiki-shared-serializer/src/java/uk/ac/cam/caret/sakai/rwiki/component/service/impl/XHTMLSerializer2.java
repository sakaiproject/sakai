

/**********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 Sakai Foundation
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

package uk.ac.cam.caret.sakai.rwiki.component.service.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import javax.xml.transform.SourceLocator;
import javax.xml.transform.Transformer;

import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.apache.xml.serializer.DOMSerializer;
import org.apache.xml.serializer.NamespaceMappings;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xml.serializer.ToXMLStream; 

public class XHTMLSerializer2 implements SerializationHandler
{
	private static final String XHTML_NAMESPACE = "http://www.w3.org/1999/xhtml";
	
	private ToXMLStream xmlStream = null;

	private static HashMap emptyTag = new HashMap();

	static
	{
		// inclusion els
		emptyTag.put("img", "img");
		emptyTag.put("area", "area");
		emptyTag.put("frame", "frame");
		// non-standard inclusion els
		emptyTag.put("layer", "layer");
		emptyTag.put("embed", "embed");
		// form el
		emptyTag.put("input", "input");
		// default els
		emptyTag.put("base", "base");
		// styling els
		emptyTag.put("col", "col");
		emptyTag.put("basefont", "basefont");
		// hidden els
		emptyTag.put("link", "link");
		emptyTag.put("meta", "meta");
		// separator els
		emptyTag.put("br", "br");
		emptyTag.put("hr", "hr");

	}
	
	public XHTMLSerializer2()
	{
		xmlStream = new ToXMLStream();
	}

	public void endElement(String namespaceURI, String localName, String name)
			throws SAXException
	{
		if ((namespaceURI != null && !"".equals(namespaceURI) && !namespaceURI
				.equals(XHTML_NAMESPACE))
				|| emptyTag.containsKey(localName.toLowerCase()))
		{
			xmlStream.endElement(namespaceURI, localName, name);
			return;
		}

		xmlStream.characters("");

		xmlStream.endElement(namespaceURI, localName, name);

	}	
	
	public void characters(char[] arg0, int arg1, int arg2) throws SAXException
	{
		xmlStream.characters(arg0, arg1, arg2);
	}

	public void endDocument() throws SAXException
	{
		xmlStream.endDocument();
	}

	public void endPrefixMapping(String arg0) throws SAXException
	{
		xmlStream.endPrefixMapping(arg0);
	}

	public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
			throws SAXException
	{
		xmlStream.ignorableWhitespace(arg0, arg1, arg2);
	}

	public void processingInstruction(String arg0, String arg1)
			throws SAXException
	{
		xmlStream.processingInstruction(arg0, arg1);
	}

	public void setDocumentLocator(Locator arg0)
	{
		xmlStream.setDocumentLocator(arg0);
	}

	public void skippedEntity(String arg0) throws SAXException
	{
		xmlStream.skippedEntity(arg0);
	}

	public void startDocument() throws SAXException
	{
		xmlStream.startDocument();
	}

	public void startElement(String arg0, String arg1, String arg2,
			Attributes arg3) throws SAXException
	{
		xmlStream.startElement(arg0, arg1, arg2, arg3);
	}

	public void startPrefixMapping(String arg0, String arg1)
			throws SAXException
	{
		xmlStream.startPrefixMapping(arg0, arg1);
	}

	public void close() {
		xmlStream.close();
	}

	public void flushPending() throws SAXException {
		xmlStream.flushPending();
	}

	public Transformer getTransformer() {
		return xmlStream.getTransformer();
	}

	public void serialize(Node arg0) throws IOException {
		xmlStream.serialize(arg0);	
	}

	public void setContentHandler(ContentHandler arg0) {
		xmlStream.setContentHandler(arg0);
	}

	public void setDTDEntityExpansion(boolean arg0) {
		// This method does not exist in the xalan 2.6.0 version
		//xmlStream.setDTDEntityExpansion(arg0);
	}

	public boolean setEscaping(boolean arg0) throws SAXException {
		return xmlStream.setEscaping(arg0);
	}

	public void setIndentAmount(int arg0) {
		xmlStream.setIndentAmount(arg0);
	}

	public void setNamespaceMappings(NamespaceMappings arg0) {
		xmlStream.setNamespaceMappings(arg0);
	}

	public void setNewLine(char[] arg0) {
		// This method does not exist in the xalan 2.6.0 version
		// xmlStream.setNewLine(arg0);
	}

	public void setTransformer(Transformer arg0) {
		xmlStream.setTransformer(arg0);
	}

	public void addAttribute(String arg0, String arg1) {
		// This method does not exist in the xalan 2.6.0 version
		// xmlStream.addAttribute(arg0, arg1);
	}

	public void addAttribute(String arg0, String arg1, String arg2, String arg3, String arg4) throws SAXException {
		// This method does not exist in the xalan 2.6.0 version
		// xmlStream.addAttribute(arg0, arg1, arg2, arg3, arg4);
	}

	public void addAttribute(String arg0, String arg1, String arg2, String arg3, String arg4, boolean arg5) throws SAXException {
		// This method does not exist in the xalan 2.6.0 version
		// xmlStream.addAttribute(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	public void addAttributes(Attributes arg0) throws SAXException {
		xmlStream.addAttributes(arg0);
	}

	public void addUniqueAttribute(String arg0, String arg1, int arg2) throws SAXException {
		xmlStream.addUniqueAttribute(arg0, arg1, arg2);
	}

	public void addXSLAttribute(String arg0, String arg1, String arg2) {
		// This method does not exist in the xalan 2.6.0 version
		// xmlStream.addXSLAttribute(arg0, arg1, arg2);
	}

	public void characters(String arg0) throws SAXException {
		xmlStream.characters(arg0);
	}

	public void characters(Node arg0) throws SAXException {
		xmlStream.characters(arg0);
	}

	public void endElement(String arg0) throws SAXException {
		xmlStream.endElement(arg0);
	}

	public void entityReference(String arg0) throws SAXException {
		xmlStream.entityReference(arg0);
	}

	public NamespaceMappings getNamespaceMappings() {
		return xmlStream.getNamespaceMappings();
	}

	public String getNamespaceURI(String arg0, boolean arg1) {
		return xmlStream.getNamespaceURI(arg0, arg1);
	}

	public String getNamespaceURIFromPrefix(String arg0) {
		return xmlStream.getNamespaceURIFromPrefix(arg0);
	}

	public String getPrefix(String arg0) {
		return xmlStream.getPrefix(arg0);
	}

	public void namespaceAfterStartElement(String arg0, String arg1) throws SAXException {
		xmlStream.namespaceAfterStartElement(arg0, arg1);		
	}

	public void setSourceLocator(SourceLocator arg0) {
		xmlStream.setSourceLocator(arg0);		
	}

	public void startElement(String arg0) throws SAXException {
		xmlStream.startElement(arg0);		
	}

	public void startElement(String arg0, String arg1, String arg2) throws SAXException {
		xmlStream.startElement(arg0, arg1, arg2);		
	}

	public boolean startPrefixMapping(String arg0, String arg1, boolean arg2) throws SAXException {
		return xmlStream.startPrefixMapping(arg0, arg1, arg2);
	}

	public void comment(String arg0) throws SAXException {
		xmlStream.comment(arg0);
	}

	public void comment(char[] ch, int start, int length) throws SAXException {
		xmlStream.comment(ch, start, length);
	}

	public void endCDATA() throws SAXException {
		xmlStream.endCDATA();
		
	}

	public void endDTD() throws SAXException {
		xmlStream.endDTD();
		
	}

	public void endEntity(String name) throws SAXException {
		xmlStream.endEntity(name);
	}

	public void startCDATA() throws SAXException {
		xmlStream.startCDATA();		
	}

	public void startDTD(String name, String publicId, String systemId) throws SAXException {
		xmlStream.startDTD(name, publicId, systemId);
	}

	public void startEntity(String name) throws SAXException {
		xmlStream.startEntity(name);		
	}

	public String getDoctypePublic() {
		return xmlStream.getDoctypePublic();
	}

	public String getDoctypeSystem() {
		return xmlStream.getDoctypeSystem();
	}

	public String getEncoding() {
		return xmlStream.getEncoding();
	}

	public boolean getIndent() {
		return xmlStream.getIndent();
	}

	public int getIndentAmount() {
		return xmlStream.getIndentAmount();
	}

	public String getMediaType() {
		return xmlStream.getMediaType();
	}

	public boolean getOmitXMLDeclaration() {
		return xmlStream.getOmitXMLDeclaration();
	}

	public String getStandalone() {
		return xmlStream.getStandalone();
	}

	public String getVersion() {
		return xmlStream.getVersion();
	}

	public void setCdataSectionElements(Vector arg0) {
		xmlStream.setCdataSectionElements(arg0);
	}

	public void setDoctype(String arg0, String arg1) {
		xmlStream.setDoctype(arg0, arg1);		
	}

	public void setDoctypePublic(String arg0) {
		xmlStream.setDoctypePublic(arg0);		
	}

	public void setDoctypeSystem(String arg0) {
		xmlStream.setDoctypeSystem(arg0);
	}

	public void setEncoding(String arg0) {
		xmlStream.setEncoding(arg0);
	}

	public void setIndent(boolean arg0) {
		xmlStream.setIndent(arg0);
	}

	public void setMediaType(String arg0) {
		xmlStream.setMediaType(arg0);
	}

	public void setOmitXMLDeclaration(boolean arg0) {
		xmlStream.setOmitXMLDeclaration(arg0);		
	}

	public void setStandalone(String arg0) {
		xmlStream.setStandalone(arg0);		
	}

	public void setVersion(String arg0) {
		xmlStream.setVersion(arg0);		
	}

	public void attributeDecl(String eName, String aName, String type, String mode, String value) throws SAXException {
		xmlStream.attributeDecl(eName, aName, type, mode, value);		
	}

	public void elementDecl(String name, String model) throws SAXException {
		xmlStream.elementDecl(name, model);
	}

	public void externalEntityDecl(String name, String publicId, String systemId) throws SAXException {
		xmlStream.externalEntityDecl(name, publicId, systemId);		
	}

	public void internalEntityDecl(String name, String value) throws SAXException {
		xmlStream.internalEntityDecl(name, value);		
	}

	public void notationDecl(String name, String publicId, String systemId) throws SAXException {
		// This method does not exist in the xalan 2.6.0 version
		// xmlStream.notationDecl(name, publicId, systemId);		
	}

	public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws SAXException {
		// This method does not exist in the xalan 2.6.0 version
		// xmlStream.unparsedEntityDecl(name, publicId, systemId, notationName);		
	}

	public void error(SAXParseException exception) throws SAXException {
		xmlStream.error(exception);		
	}

	public void fatalError(SAXParseException exception) throws SAXException {
		xmlStream.fatalError(exception);		
	}

	public void warning(SAXParseException exception) throws SAXException {
		xmlStream.warning(exception);		
	}

	public ContentHandler asContentHandler() throws IOException {
		return xmlStream.asContentHandler();
	}

	public Object asDOM3Serializer() throws IOException {
		// This method does not exist in the xalan 2.6.0 version
 		// return xmlStream.asDOM3Serializer();
		return null;
	}

	public DOMSerializer asDOMSerializer() throws IOException {
		return xmlStream.asDOMSerializer();
	}

	public Properties getOutputFormat() {
		return xmlStream.getOutputFormat();
	}

	public OutputStream getOutputStream() {
		return xmlStream.getOutputStream();
	}

	public Writer getWriter() {
		return xmlStream.getWriter();
	}

	public boolean reset() {
		return xmlStream.reset();
	}

	public void setOutputFormat(Properties arg0) {
		xmlStream.setOutputFormat(arg0);
	}

	public void setOutputStream(OutputStream arg0) {
		xmlStream.setOutputStream(arg0);		
	}

	public void setWriter(Writer arg0) {
		xmlStream.setWriter(arg0);
	}

}