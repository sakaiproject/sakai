/**********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
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
package edu.indiana.lib.twinpeaks.util;

import java.lang.*;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.*;
import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.w3c.dom.html.*;
import org.xml.sax.*;



public class LogUtils {
  /**
   * Get a Log instance
   * @param logClass Java Class being logged
   */
	public static Log getLog(Class logClass) {
  	return LogFactory.getLog(logClass);
	}

  /**
   * Get a Log instance
   * @param logName Name being logged
   */
	public static Log getLog(String logName) {
  	return LogFactory.getLog(logName);
	}

	/**
	 * Serialize an XML object (Document or Element) to the log
	 * @param log Apache Log object
	 * @param recordElement The XML object to disolay (Document, Element)
	 */
	public static void displayXml(org.apache.commons.logging.Log log,
																Object xmlObject) {
		displayXml(log, null, xmlObject);
	}

	/**
	 * Serialize an XML object (Document or Element) to the log (with an
	 * optional warning header)
	 * @param log Apache Log object
	 * @param errorText Error message (null for none)
	 * @param recordElement The XML object to disolay (Document, Element)
	 */
	public static void displayXml(org.apache.commons.logging.Log log,
																String errorText,
																Object xmlObject) {

  	if (!(xmlObject instanceof Document) && !(xmlObject instanceof Element)) {
     	throw new IllegalArgumentException("Unexpected object for serialzation: "
     																	+ 	xmlObject.toString());
		}

		if (!StringUtils.isNull(errorText)) {
			log.error(errorText);
		}

		log.info("Record Start ----------------------------------------");

		try {
			log.info(DomUtils.serialize(xmlObject));
		} catch (DomException exception) {
			log.error("Failed to serialize element " +	xmlObject.toString(),
									exception);
		}
		log.info("Record End ------------------------------------------");
	}

	/*
	 * Test
	 */
  public static void main(String[] args) {

		Log log = LogUtils.getLog(LogUtils.class);

		log.debug("Debug");
		log.error("Error");
		log.warn("Warn");
		log.info("Info");
	}
}