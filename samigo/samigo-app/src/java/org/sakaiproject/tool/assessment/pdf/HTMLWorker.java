package org.sakaiproject.tool.assessment.pdf;

import java.util.ArrayList;
import java.util.HashMap;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.net.URLDecoder;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.cover.ContentHostingService;

import com.lowagie.text.DocListener;
import com.lowagie.text.html.simpleparser.StyleSheet;

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
			if ((src.startsWith(ACCESSBASE)) || (src.startsWith(RELATIVEBASE))) {

				try {
					String imgId = src.replaceFirst(ACCESSBASE, "").replaceFirst(RELATIVEBASE, "");
					imgId = URLDecoder.decode(imgId); 
					ContentResource img = ContentHostingService.getResource(imgId);

					//creates a temp file in the default temp file location..
					String ext = imgId.substring(imgId.lastIndexOf("."));
					File temp = File.createTempFile("temp" + img.hashCode(), ext);
					FileOutputStream fos = new FileOutputStream(temp);
					DataOutputStream dos = new DataOutputStream(fos);
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
					System.out.println("Something went very wrong " + e);
					e.printStackTrace();
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