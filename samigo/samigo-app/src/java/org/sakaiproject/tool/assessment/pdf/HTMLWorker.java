/**
 * Copyright (c) 2005-2015 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.pdf;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;

import com.lowagie.text.DocListener;
import com.lowagie.text.html.simpleparser.StyleSheet;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.cover.ContentHostingService;

/**
 * 
 * @author Joshua Ryan <a href="mailto:joshua.ryan@asu.edu">joshua.ryan@asu.edu</a>
 *
 * Utility classs that extends itext's HTMLWorker to replace all images in
 * Content hosting with references to temp files, as urls through access 
 * won't work for items not publicly available when requested from the 
 * server to the server
 *  
 */
@Slf4j
 public class HTMLWorker extends org.sakaiproject.tool.assessment.pdf.itext.HTMLWorker {

	//http://yourhost/access + /content at the time of this writting
	private static String ACCESSBASE = ServerConfigurationService.getAccessUrl() +
	ContentHostingService.REFERENCE_ROOT;

	// /access/content at the time of this writting
	private String RELATIVEBASE = ACCESSBASE.replace(ServerConfigurationService.getServerUrl(), "");

	//to keep track of temp files created for ContentHosting Images
	private ArrayList tempFiles = new ArrayList();

	/**
	 * {@inheritDoc}
	 * 
	 */
	public HTMLWorker(DocListener doc) {
		super(doc);
	}

	//duplicated here only because static reference to this was creating a base class instance
	//is there a better way to do this with spring?
	public static ArrayList parseToList(Reader reader, StyleSheet style, HashMap interfaceProps) throws IOException {
		HTMLWorker worker = new HTMLWorker(null);
		if (style != null)
			worker.setStyleSheet(style);
		worker.document = worker;
		worker.setInterfaceProps(interfaceProps);
		worker.objectList = new ArrayList();
		worker.parse(reader);
		return worker.objectList;
	}

	/**
	 * Adds Sakai's ConentHostingService awareness to img references
	 * 
	 * This is needed due to Sakai's security model for content with in
	 * ContentHostingService, which would not allow requests from the server
	 * to the server to get protected images
	 * 
	 * {@inheritDoc}
	 */
	public void startElement(String tag, HashMap h) {

		if (tag.equals("img")) {
			String src = (String)h.get("src");
			if (src == null)
				return;

			String imgId = "";
			if ((src.startsWith(ACCESSBASE)) || (src.startsWith(RELATIVEBASE)) || src.startsWith("/samigo/")) {
				FileOutputStream fos = null;
				DataOutputStream dos = null;
				if ((src.startsWith(ACCESSBASE)) || (src.startsWith(RELATIVEBASE))) {
					imgId = src.replaceFirst(ACCESSBASE, "").replaceFirst(RELATIVEBASE, "");
				}
				else if (src.startsWith("/samigo/")) {
					imgId = src.replaceFirst("/samigo", "");
				}

				try {
					imgId = URLDecoder.decode(imgId); 
					ContentResource img = ContentHostingService.getResource(imgId);

					//creates a temp file in the default temp file location..
					String ext = imgId.substring(imgId.lastIndexOf("."));
					File temp = File.createTempFile("temp" + img.hashCode(), ext);
					fos = new FileOutputStream(temp);
					dos = new DataOutputStream(fos);
					dos.write(img.getContent(), 0, (int)img.getContentLength());
					dos.close();
					fos.close();

					//keep track of the new temp file for later cleanup
					tempFiles.add(temp);

					//change the src ref to point to the new local temp file
					h.put("src", temp.getCanonicalPath());

					//Spoof the interface props so that it won't try anything weird with urls
					HashMap props = this.getInterfaceProps();
					HashMap tempProps = new HashMap();
					this.setInterfaceProps(tempProps);

					super.startElement(tag, h);

					this.setInterfaceProps(props);

				}
				catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				finally {
					if ( dos != null ) {
						try {
							dos.close();
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}
					}
					if ( fos!= null ) {
						try {
							fos.close();
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}
					}
        			}

			}
			else if (src.startsWith("temp://")) {
				try {
					File temp = new File(src.replaceFirst("temp://", ""));
					
					//keep track of the new temp file for later cleanup
					tempFiles.add(temp);

					//change the src ref to point to the new local temp file
					h.put("src", temp.getCanonicalPath());

					//Spoof the interface props so that it won't try anything weird with urls
					HashMap props = this.getInterfaceProps();
					HashMap tempProps = new HashMap();
					this.setInterfaceProps(tempProps);

					super.startElement(tag, h);

					this.setInterfaceProps(props);
				}
				catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
			//nothing fancy for normal images
			else {
				super.startElement(tag, h);
			}

			//This may not be the best way to clean up the temp files... 
			//The temp files need to exist until after the pdf has actually been created
			//or weird errors show up.
			for (int i = 0; i < tempFiles.size(); i++) {
				File trash = (File)tempFiles.get(i);
				trash.deleteOnExit();
			}
		}	
		else {
			super.startElement(tag, h);
		}		
	}
}
