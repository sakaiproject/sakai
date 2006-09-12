package org.sakaiproject.importer.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.sakaiproject.importer.api.ImportDataSource;
import org.sakaiproject.importer.api.ImportFileParser;
import org.sakaiproject.importer.impl.importables.FileResource;
import org.sakaiproject.importer.impl.importables.Folder;
import org.sakaiproject.importer.impl.importables.WebLink;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class CommonCartridgeFileParser extends IMSFileParser {
	private static final String CC_SCHEMA_NAME = "IMS Common Cartridge";
	private Document manifest;
	private String pathToData;
	private Map resourceMap = new HashMap();

	public boolean isValidArchive(byte[] fileData) {
		if (super.isValidArchive(fileData)) {
			Document manifest = extractFileAsDOM("/imsmanifest.xml", fileData);
			return CC_SCHEMA_NAME.equals(XPathHelper.getNodeValue("/manifest/metadata/schema", manifest));
		} else return false;
	}

	protected Collection getCategoriesFromArchive(String pathToData) {
		// TODO Auto-generated method stub
		return null;
	}

	protected boolean isCompoundDocument(Node node, Document resourceDescriptor) {
		// TODO Auto-generated method stub
		return false;
	}

}
