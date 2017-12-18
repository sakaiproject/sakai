/*
 * Copyright 2004 Paulo Soares
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 *
 * The Original Code is 'iText, a free JAVA-PDF library'.
 *
 * The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
 * the Initial Developer are Copyright (C) 1999, 2000, 2001, 2002 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000, 2001, 2002 by Paulo Soares. All Rights Reserved.
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * LGPL license (the "GNU LIBRARY GENERAL PUBLIC LICENSE"), in which case the
 * provisions of LGPL are applicable instead of those above.  If you wish to
 * allow use of your version of this file only under the terms of the LGPL
 * License and not to allow others to use your version of this file under
 * the MPL, indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by the LGPL.
 * If you do not delete the provisions above, a recipient may use your version
 * of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the MPL as stated above or under the terms of the GNU
 * Library General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library general Public License for more
 * details.
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * http://www.lowagie.com/iText/
 */

package org.sakaiproject.tool.assessment.pdf.itext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.StringTokenizer;

import lombok.extern.slf4j.Slf4j;
import com.lowagie.text.Chunk;
import com.lowagie.text.DocListener;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.ElementTags;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.FontFactory;
import com.lowagie.text.FontFactoryImp;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.ListItem;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.TextElementArray;
import com.lowagie.text.html.simpleparser.ALink;
import com.lowagie.text.html.simpleparser.ChainedProperties;
import com.lowagie.text.html.simpleparser.FactoryProperties;
import com.lowagie.text.html.simpleparser.Img;
import com.lowagie.text.html.simpleparser.IncCell;
import com.lowagie.text.html.simpleparser.IncTable;
import com.lowagie.text.html.simpleparser.StyleSheet;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.xml.simpleparser.SimpleXMLDocHandler;
import com.lowagie.text.xml.simpleparser.SimpleXMLParser;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.cover.ContentHostingService;

@Slf4j
public class HTMLWorker implements SimpleXMLDocHandler, DocListener {

	protected ArrayList objectList;
	protected DocListener document;
	private Paragraph currentParagraph;
	private ChainedProperties cprops;
	private Stack stack = new Stack();
	private boolean pendingTR = false;
	private boolean pendingTD = false;
	private boolean pendingLI = false;
	private StyleSheet style = new StyleSheet();
	private boolean isPRE = false;
	private boolean inBLOCK = false;
	private Stack tableState = new Stack();
	private boolean skipText = false;
	private HashMap interfaceProps;
	private FactoryProperties factoryProperties = new FactoryProperties();
	private int maxWidth = 450;

	/** Creates a new instance of HTMLWorker */
	public HTMLWorker(DocListener document) {
		this.document = document;
		cprops = new ChainedProperties();
		String fontName = ServerConfigurationService.getString("pdf.default.font");
		if (StringUtils.isNotBlank(fontName)) {
			FontFactory.registerDirectories();
			if (FontFactory.isRegistered(fontName)) {
				HashMap fontProps = new HashMap();
				fontProps.put(ElementTags.FACE, fontName);
				fontProps.put("encoding", BaseFont.IDENTITY_H);
				cprops.addToChain("face", fontProps);
			}
		}
	}

	public void setStyleSheet(StyleSheet style) {
		this.style = style;
	}

	public StyleSheet getStyleSheet() {
		return style;
	}

	public void setInterfaceProps(HashMap interfaceProps) {
		this.interfaceProps = interfaceProps;
		FontFactoryImp ff = null;
		if (interfaceProps != null)
			ff = (FontFactoryImp)interfaceProps.get("font_factory");
		if (ff != null)
			factoryProperties.setFontImp(ff);
	}

	public HashMap getInterfaceProps() {
		return interfaceProps;
	}

	public void parse(Reader reader) throws IOException {
		SimpleXMLParser.parse(this, null, reader, true);
	}

	public static ArrayList parseToList(Reader reader, StyleSheet style) throws IOException {
		return parseToList(reader, style, null);
	}

	public static ArrayList parseToList(Reader reader, StyleSheet style, HashMap interfaceProps) throws IOException {
		HTMLWorker worker = new HTMLWorker(null);
		if (style != null)
			worker.style = style;
		worker.document = worker;
		worker.setInterfaceProps(interfaceProps);
		worker.objectList = new ArrayList();
		worker.parse(reader);
		return worker.objectList;
	}

	public void endDocument() {
		try {
			for (int k = 0; k < stack.size(); ++k)
				document.add((Element)stack.elementAt(k));
			if (currentParagraph != null)
				document.add(currentParagraph);
			currentParagraph = null;
		}
		catch (Exception e) {
			throw new ExceptionConverter(e);
		}
	}

	public void startDocument() {
		HashMap h = new HashMap();
		style.applyStyle("body", h);
		cprops.addToChain("body", h);
	}

	public void startElement(String tag, HashMap h) {
		if (!tagsSupported.containsKey(tag))
			return;
		try {
			style.applyStyle(tag, h);
			String follow = (String)FactoryProperties.followTags.get(tag);
			if (follow != null) {
				HashMap prop = new HashMap();
				prop.put(follow, null);
				cprops.addToChain(follow, prop);
				return;
			}
			FactoryProperties.insertStyle(h);
			if (tag.equals("a")) {
				cprops.addToChain(tag, h);
				if (currentParagraph == null)
					currentParagraph = new Paragraph();
				stack.push(currentParagraph);
				currentParagraph = new Paragraph();
				return;
			}
			if (tag.equals("br")) {
				if (currentParagraph == null)
					currentParagraph = new Paragraph();
				currentParagraph.add(factoryProperties.createChunk("\n", cprops));
				return;
			}
			if (tag.equals("hr")) {

				PdfPTable hr = new PdfPTable(1);  
				hr.setHorizontalAlignment(Element.ALIGN_CENTER);  
				hr.setWidthPercentage(100f);
				hr.setSpacingAfter(0f);
				hr.setSpacingBefore(0f);  
				PdfPCell cell = new PdfPCell();
				cell.setUseVariableBorders(true);  
				cell.setVerticalAlignment(Element.ALIGN_MIDDLE);  
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);  
				cell.setBorder(PdfPCell.BOTTOM);
				cell.setBorderWidth(1f);
				cell.setPadding(0);
				cell.addElement(factoryProperties.createChunk("\n", cprops));

				hr.addCell(cell);
				// paragraphs can't have tables? really? without it hr's may be rendered a bit early..
				//if (currentParagraph != null)
				//    currentParagraph.add(hr);
				//else
				document.add(hr);
				return;

			}
			if (tag.equals("font") || tag.equals("span")) {
				cprops.addToChain(tag, h);
				return;
			}
			if (tag.equals("img")) {
				String src = (String)h.get("src");
				if (src == null)
					return;
				cprops.addToChain(tag, h);
				Image img = null;
				if (interfaceProps != null) {
					HashMap images = (HashMap)interfaceProps.get("img_static");
					if (images != null) {
						Image tim = (Image)images.get(src);
						if (tim != null)
							img = Image.getInstance(tim);
					} else {
						if (!src.startsWith("http")) { // relative src references only
							String baseurl = (String)interfaceProps.get("img_baseurl");
							if (baseurl != null) {
								src = baseurl+src;
								img = Image.getInstance(src);
							}
						}
					}
				}
				if (img == null) {
					if (!src.startsWith("http")) {
						String path = cprops.getProperty("image_path");
						if (path == null)
							path = "";
						src = new File(path, src).getPath();
						img = Image.getInstance(src);
					}
					else {
						img = Image.getInstance(URLDecoder.decode(src));
					}
				}

				String align = (String)h.get("align");
				String width = (String)h.get("width");
				String height = (String)h.get("height");
				String border = (String)h.get("border");
				String hspace = (String)h.get("hspace");
				String vspace = (String)h.get("vspace");
				String before = cprops.getProperty("before");
				String after = cprops.getProperty("after");
				float wp = 0.0f;
				float lp = 0.0f;
				if (maxWidth > 0 && 
						((width != null && Integer.parseInt(width) > maxWidth)
								|| (width == null && (int)img.getWidth() > maxWidth))
				) {
					wp = lengthParse(String.valueOf(maxWidth), (int)img.getWidth());
					lp = wp;
				}
				else {
					wp = lengthParse(width, (int)img.getWidth());
					lp = lengthParse(height, (int)img.getHeight());
				}
				if (wp > 0 && lp > 0)
					img.scalePercent(wp, lp);
				else if (wp > 0)
					img.scalePercent(wp);
				else if (lp > 0)
					img.scalePercent(lp);
				img.setWidthPercentage(0);
				// border
				if (border != null && !"".equals(border)) {
					try {
						img.setBorderWidth(Integer.parseInt(border));
						img.setBorder(Image.BOX);
					}
					catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
				// horizonatal space
				if (hspace != null && !"".equals(hspace)) {
					try {
						img.setSpacingAfter(Float.parseFloat(hspace));
						img.setSpacingBefore(Float.parseFloat(hspace));
					}
					catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}

				// horizontal alignment
				if (align != null && (align.equalsIgnoreCase("left") || align.equalsIgnoreCase("right"))) {
					endElement("p");
					int ralign = Image.LEFT;
					if (align.equalsIgnoreCase("right"))
						ralign = Image.RIGHT;
					img.setAlignment(ralign | Image.TEXTWRAP);
					Img i = null;
					boolean skip = false;
					if (interfaceProps != null) {
						i = (Img)interfaceProps.get("img_interface");
						if (i != null)
							skip = i.process(img, h, cprops, document);
					}
					if (!skip)
						document.add(img);
					cprops.removeChain(tag);
				}
				// vertical alignment (or none)
				else {
					img.setAlignment(Image.TEXTWRAP);

					float bottom = 0.0f;
					float top = img.getTop();
					float prevHeight = 0.0f;
					float prevRise = 0.0f;

					if (currentParagraph != null) {
						ArrayList chunks = currentParagraph.getChunks();
						Chunk sibling = null;

						for (int k = chunks.size() - 1; k >= 0; k--) {
							if (chunks.get(k) != null)
								sibling = (Chunk)chunks.get(k);
						}

						if (sibling != null) {
							if (sibling.hasAttributes())
								prevRise = sibling.getTextRise();
							prevHeight = 0.0f;
							if (sibling.getFont() != null) {
								prevHeight = sibling.getFont().getCalculatedSize();
							}
						}
					}

					if ("absMiddle".equalsIgnoreCase(align)) {
						if (prevHeight > 0)
							bottom += (img.getScaledHeight() / 2.0f) - (prevHeight  / 2.0f);
						else if (img.getScaledHeight() > 0)
							bottom += img.getScaledHeight() / 2.0f;
					}
					else if ("middle".equalsIgnoreCase(align)) {
						if (img.getScaledHeight() > 0)
							bottom += (img.getScaledHeight() / 2.0f);
					}
					else if ("bottom".equalsIgnoreCase(align) || "baseline".equalsIgnoreCase(align)
							|| "absbottom".equalsIgnoreCase(align)) {
						//baseline and absbottom should have some slight tweeking from bottom, but not sure what??
					}
					else if ("top".equalsIgnoreCase(align)) {
						bottom += img.getScaledHeight() - prevHeight;
					}
					else if ("texttop".equalsIgnoreCase(align)) {
						bottom += img.getScaledHeight() - (prevHeight - prevRise);
					}

					cprops.removeChain(tag);
					if (currentParagraph == null) {
						currentParagraph = FactoryProperties.createParagraph(cprops);
						bottom = 0f;
					}
					else if (currentParagraph.isEmpty()) {
						bottom = 0f;
					}

					currentParagraph.setLeading(2f + bottom, 1.00f);
					currentParagraph.add(new Chunk(img, 0, 0 - bottom));
				}
				return;
			}
			if (tag.equals("blockquote")) {
				cprops.addToChain(tag, h);
				inBLOCK = true;
				if (currentParagraph != null)
					endElement("p");
				currentParagraph = FactoryProperties.createParagraph(cprops);
				currentParagraph.add(factoryProperties.createChunk("\n", cprops));
				return;
			}
			endElement("p");
			if (tag.equals("h1") || tag.equals("h2") || tag.equals("h3") || tag.equals("h4") || tag.equals("h5") || tag.equals("h6")) {
				if (!h.containsKey("size")) {
					int v = 8 - Integer.parseInt(tag.substring(1));
					h.put("size", Integer.toString(v));
				}
				cprops.addToChain(tag, h);
				return;
			}
			if (tag.equals("ul")) {
				if (pendingLI)
					endElement("li");
				skipText = true;
				cprops.addToChain(tag, h);
				com.lowagie.text.List list = new com.lowagie.text.List(false, 10);
				list.setListSymbol("\u2022");
				stack.push(list);
				return;
			}
			if (tag.equals("ol")) {
				if (pendingLI)
					endElement("li");
				skipText = true;
				cprops.addToChain(tag, h);
				com.lowagie.text.List list = new com.lowagie.text.List(true, 10);
				stack.push(list);
				return;
			}
			if (tag.equals("li")) {
				if (pendingLI)
					endElement("li");
				skipText = false;
				pendingLI = true;
				cprops.addToChain(tag, h);
				stack.push(FactoryProperties.createListItem(cprops));
				return;
			}
			if (tag.equals("div") || tag.equals("body")) {
				cprops.addToChain(tag, h);
				return;
			}
			if (tag.equals("pre")) {
				if (!h.containsKey("face")) {
					h.put("face", "Courier");
				}
				cprops.addToChain(tag, h);
				isPRE = true;
				return;
			}
			if (tag.equals("p")) {
				cprops.addToChain(tag, h);
				currentParagraph = FactoryProperties.createParagraph(cprops);
				if (inBLOCK) {
					currentParagraph.setIndentationLeft(currentParagraph.getIndentationLeft() + 40.0F);
				}
				return;
			}
			if (tag.equals("tr")) {
				if (pendingTR)
					endElement("tr");
				skipText = true;
				pendingTR = true;
				cprops.addToChain("tr", h);
				return;
			}
			if (tag.equals("td") || tag.equals("th")) {
				if (pendingTD)
					endElement(tag);
				skipText = false;
				pendingTD = true;
				cprops.addToChain("td", h);
				stack.push(new IncCell(tag, cprops));
				return;
			}
			if (tag.equals("table")) {
				cprops.addToChain("table", h);
				IncTable table = new IncTable(h);
				stack.push(table);
				tableState.push(new boolean[]{pendingTR, pendingTD});
				pendingTR = pendingTD = false;
				skipText = true;
				return;
			}
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void endElement(String tag) {
		if (!tagsSupported.containsKey(tag))
			return;
		try {
			String follow = (String)FactoryProperties.followTags.get(tag);
			if (follow != null) {
				cprops.removeChain(follow);
				return;
			}
			if (tag.equals("font") || tag.equals("span")) {
				cprops.removeChain(tag);
				return;
			}
			if (tag.equals("a")) {
				if (currentParagraph == null)
					currentParagraph = new Paragraph();
				ALink i = null;
				boolean skip = false;
				if (interfaceProps != null) {
					i = (ALink)interfaceProps.get("alink_interface");
					if (i != null)
						skip = i.process(currentParagraph, cprops);
				}
				if (!skip) {
					String href = cprops.getProperty("href");
					if (href != null) {
						ArrayList chunks = currentParagraph.getChunks();
						for (int k = 0; k < chunks.size(); ++k) {
							Chunk ck = (Chunk)chunks.get(k);
							ck.setAnchor(href);
						}
					}
				}
				Paragraph tmp = (Paragraph)stack.pop();
				Phrase tmp2 = new Phrase();
				tmp2.add(currentParagraph);
				tmp.add(tmp2);
				currentParagraph = tmp;
				cprops.removeChain("a");
				return;
			}
			if (tag.equals("blockquote")) {
				cprops.removeChain(tag);
				currentParagraph = new Paragraph();
				currentParagraph.add(factoryProperties.createChunk("\n", cprops));
				inBLOCK = false;
				return;
			}
			if (tag.equals("br")) {
				return;
			}
			if (tag.equals("hr")) {
				return;
			}
			if (currentParagraph != null) {
				if (stack.empty())
					document.add(currentParagraph);
				else {
					Object obj = stack.pop();
					if (obj instanceof TextElementArray) {
						TextElementArray current = (TextElementArray)obj;
						current.add(currentParagraph);
					}
					stack.push(obj);
				}
			}
			currentParagraph = null;
			if (tag.equals("ul") || tag.equals("ol")) {
				if (pendingLI)
					endElement("li");
				skipText = false;
				cprops.removeChain(tag);
				if (stack.empty())
					return;
				Object obj = stack.pop();
				if (!(obj instanceof com.lowagie.text.List)) {
					stack.push(obj);
					return;
				}
				if (stack.empty())
					document.add((Element)obj);
				else
					((TextElementArray)stack.peek()).add(obj);
				return;
			}
			if (tag.equals("li")) {
				pendingLI = false;
				skipText = true;
				cprops.removeChain(tag);
				if (stack.empty())
					return;
				Object obj = stack.pop();
				if (!(obj instanceof ListItem)) {
					stack.push(obj);
					return;
				}
				if (stack.empty()) {
					document.add((Element)obj);
					return;
				}
				Object list = stack.pop();
				if (!(list instanceof com.lowagie.text.List)) {
					stack.push(list);
					return;
				}
				ListItem item = (ListItem)obj;
				((com.lowagie.text.List)list).add(item);
				ArrayList cks = item.getChunks();
				if (!cks.isEmpty())
					item.getListSymbol().setFont(((Chunk)cks.get(0)).getFont());
				stack.push(list);
				return;
			}
			if (tag.equals("div") || tag.equals("body")) {
				cprops.removeChain(tag);
				return;
			}
			if (tag.equals("pre")) {
				cprops.removeChain(tag);
				isPRE = false;
				return;
			}
			if (tag.equals("p")) {
				cprops.removeChain(tag);
				return;
			}
			if (tag.equals("h1") || tag.equals("h2") || tag.equals("h3") || tag.equals("h4") || tag.equals("h5") || tag.equals("h6")) {
				cprops.removeChain(tag);
				return;
			}
			if (tag.equals("table")) {
				if (pendingTR)
					endElement("tr");
				cprops.removeChain("table");
				IncTable table = (IncTable) stack.pop();
				if (table.getRows() == null || table.getRows().isEmpty()) {
					// we have an empty table skip it 
					return;
				}
				PdfPTable tb = table.buildTable();
				tb.setSplitRows(true);
				if (stack.empty())
					document.add(tb);
				else
					((TextElementArray)stack.peek()).add(tb);
				boolean state[] = (boolean[])tableState.pop();
				pendingTR = state[0];
				pendingTD = state[1];
				skipText = false;
				return;
			}
			if (tag.equals("tr")) {
				if (pendingTD)
					endElement("td");
				pendingTR = false;
				cprops.removeChain("tr");
				ArrayList cells = new ArrayList();
				IncTable table = null;
				while (true) {
					Object obj = stack.pop();
					if (obj instanceof IncCell) {
						cells.add(((IncCell)obj).getCell());
					}
					if (obj instanceof IncTable) {
						table = (IncTable)obj;
						break;
					}
				}
				if (cells.size() > 0) {
					table.addCols(cells);
					table.endRow();
				}
				stack.push(table);
				skipText = true;
				return;
			}
			if (tag.equals("td") || tag.equals("th")) {
				pendingTD = false;
				cprops.removeChain("td");
				skipText = true;
				return;
			}
		}
		catch (Exception e) {
			throw new ExceptionConverter(e);
		}
	}

	public void text(String str) {
		if (skipText)
			return;
		String content = str;
		if (isPRE) {
			if (currentParagraph == null)
				currentParagraph = new Paragraph();
			currentParagraph.add(factoryProperties.createChunk(content, cprops));
			return;
		}
		if (content.trim().length() == 0 && content.indexOf(' ') < 0) {
			return;
		}

		StringBuffer buf = new StringBuffer();
		int len = content.length();
		char character;
		boolean newline = false;
		for (int i = 0; i < len; i++) {
			switch(character = content.charAt(i)) {
			case ' ':
				if (!newline) {
					buf.append(character);
				}
				break;
			case '\n':
				if (i > 0) {
					newline = true;
					buf.append(' ');
				}
				break;
			case '\r':
				break;
			case '\t':
				break;
			default:
				newline = false;
			buf.append(character);
			}
		}
		if (currentParagraph == null)
			currentParagraph = FactoryProperties.createParagraph(cprops);
		currentParagraph.add(factoryProperties.createChunk(buf.toString(), cprops));
	}

	public boolean add(Element element) throws DocumentException {
		objectList.add(element);
		return true;
	}

	public void clearTextWrap() throws DocumentException {
	}

	public void close() {
	}

	public boolean newPage() {
		return true;
	}

	public void open() {
	}

	public void resetFooter() {
	}

	public void resetHeader() {
	}

	public void resetPageCount() {
	}

	public void setFooter(HeaderFooter footer) {
	}

	public void setHeader(HeaderFooter header) {
	}

	public boolean setMarginMirroring(boolean marginMirroring) {
		return true;
	}
	public boolean setMarginMirroringTopBottom(boolean marginMirroring) {
		return false;
	}

	public boolean setMargins(float marginLeft, float marginRight, float marginTop, float marginBottom) {
		return true;
	}

	public void setPageCount(int pageN) {
	}

	public boolean setPageSize(Rectangle pageSize) {
		return true;
	}

	public static final String tagsSupportedString = "ol ul li a pre font span br p div body table td th tr i b u sub sup em strong"
		+ " h1 h2 h3 h4 h5 h6 img hr blockquote";

	public static final HashMap tagsSupported = new HashMap();

	static {
		StringTokenizer tok = new StringTokenizer(tagsSupportedString);
		while (tok.hasMoreTokens())
			tagsSupported.put(tok.nextToken(), null);
	}

	private static float lengthParse(String txt, int c) {
		if (txt == null)
			return -1;
		if (txt.endsWith("%")) {
			float vf = Float.parseFloat(txt.substring(0, txt.length() - 1));
			return vf;
		}
		int v = Integer.parseInt(txt);
		return (float)v / c * 100f;
	}

	public void setMaxWidth(int width) {
		maxWidth = width;
	}

	private byte[] getImageStream(String src) throws Exception {

		InputStream input = null;
		byte[] buffer;
		try {
			ContentResource resource = ContentHostingService.getResource(src);
			buffer = new byte[(int)resource.getContentLength()];
			input = resource.streamContent();
			input.read(buffer, 0, buffer.length);
			return buffer;

		} catch (Exception e) {
			throw e;
		} finally {
			if(input != null) try { input.close(); } catch (Exception fe) {}
		}
	}
}
