/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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
package org.sakaiproject.antivirus.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.antivirus.api.VirusFoundException;
import org.sakaiproject.antivirus.api.VirusScanIncompleteException;
import org.sakaiproject.antivirus.api.VirusScanner;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;

/**
 * Provide virus scanning to email msgs & byte arrays using ClamAV software
 * <br>Creation Date: Mar 22, 2005
 *
 * <p>Provide the following properties in your sakai.properties to config and enable the scanner:</p>
 *
 * <ul>
 * <li>virusScan.host=localhost
 * <li>virusScan.port=3310
 * <li>virusScan.enabled=true
 * </ul>
 *
 * @author Mike DeSimone, mike.[at].rsmart.com
 * @author John Bush
 * @version $Revision$
 */
@Slf4j
public class ClamAVScanner implements VirusScanner {
	private final String STREAM_PORT_STRING = "PORT ";
	private final String FOUND_STRING = "FOUND";
	private final String SCAN_INCOMPLETE_MSG = "Virus scan could not finish due to an internal error";
	private final int DEFAULT_STREAM_BUFFER_SIZE = 8192;
	private int port = 3310;
	private String host = "localhost";

	/**
	 * true if virus scanning is enabled (i.e., we should scan the data)
	 */
	private boolean enabled = false;

	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	public void init(){
		log.info("init()");
		port = serverConfigurationService.getInt("virusScan.port", 3310);
		host = serverConfigurationService.getString("virusScan.host", "localhost");
		enabled = serverConfigurationService.getBoolean("virusScan.enabled", false);
	}

	public void scan(byte[] bytes) throws VirusScanIncompleteException, VirusFoundException {
		if(!enabled) {
			log.debug("Virus scanning not enabled.  Skipping scan");
			return;
		}

		if(bytes != null) {
			doScan(bytes);
		}
	}

	public void scan(InputStream inputStream) throws VirusFoundException, VirusScanIncompleteException {
		if(!enabled) {
			log.debug("Virus scanning not enabled.  Skipping scan");
			return;
		}

		doScan(inputStream);
	}

	protected void doScan(InputStream in) throws VirusScanIncompleteException, VirusFoundException {
		log.debug("doingScan!");
		Socket socket = null;
		String virus = null;
		long start = System.currentTimeMillis();
		//this could be a null or zero lenght stream
		if (in == null) {
			return;
		}

		try {
			socket = getClamdSocket();
		} catch (UnknownHostException e) {
			log.error("could not connect to host for virus check: " + e);
			throw new VirusScanIncompleteException(SCAN_INCOMPLETE_MSG);
		}
		if(socket == null || !socket.isConnected()) {
			log.warn("scan is inclomplete!");
			throw new VirusScanIncompleteException(SCAN_INCOMPLETE_MSG);
		}
		BufferedReader reader = null;
		PrintWriter writer = null;
		Socket streamSocket = null;
		boolean virusFound = false;
		try {

			// prepare the reader and writer for the commands
			boolean autoFlush = true;
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "ASCII"));
			writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), autoFlush);
			// write a request for a port to use for streaming out the data to scan
			writer.println("STREAM");

			// parse and get the "stream" port#
			int streamPort = getStreamPortFromAnswer(reader.readLine());

			// get the "stream" socket and the related (buffered) output stream
			streamSocket = new Socket(socket.getInetAddress(), streamPort);
			OutputStream os = streamSocket.getOutputStream();

			// stream out the message to the scanner
			int data;
			// -1 signals the end of the data stream
			while((data = in.read()) > -1) {
				os.write(data);
			}
			os.flush();
			os.close();
			streamSocket.close();



			String logMessage = "";
			String answer = null;
			for(int i=0; i < 100; ++i) {
				answer = reader.readLine();
				if(answer != null) {
					answer = answer.trim();

					// if a virus is found the answer will be '... FOUND'
					if(answer.substring(answer.length() - FOUND_STRING.length()).equals(FOUND_STRING)) {
						virusFound = true;
						logMessage = answer + " (by virus scanner)";
						//virus = answer.substring(answer.indexOf(":" + 1));
						virus = answer.substring(0, answer.indexOf(FOUND_STRING)).trim();
						log.debug(logMessage);
					} else {
						log.debug("no virus found: " + answer);
					}
				} else {
					break;
				}
			}
			long finish = System.currentTimeMillis();
			log.debug("Content scanned in " + (finish - start));
		} catch (UnsupportedEncodingException e) {
			log.error("Exception caught calling CLAMD on " + socket.getInetAddress() + ": " + e.getMessage());
			throw new VirusScanIncompleteException(SCAN_INCOMPLETE_MSG, e);
		} catch (IOException e) {
			//we expect a connection reset if we tried to send too much data to clamd
			if ("Connection reset".equals(e.getMessage())) {
				log.warn("Clamd reset the connection maybe due to the file being too large");
				return;
			}
			log.error("Exception caught calling CLAMD on " + socket.getInetAddress() + ": " + e.getMessage());
			throw new VirusScanIncompleteException(SCAN_INCOMPLETE_MSG, e);
			
		} 
		finally {
			
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
			if(writer != null) {
				writer.close();
			}
			if(streamSocket != null) {
				try {
					streamSocket.close();
				} catch (IOException e) {

				}
			}
			if(socket != null) {
				try {
					socket.close();
				} catch (IOException e) {

				}
			}

		}
		if(virusFound) {
			log.info("Virus detected!: " + virus);
			throw new VirusFoundException(virus);
		}

	}

	protected void doScan(byte[] bytesIn) throws VirusScanIncompleteException, VirusFoundException {
		if (bytesIn == null) {
			return;
		}
		doScan(new ByteArrayInputStream(bytesIn));
	}

	protected Socket getClamdSocket() throws UnknownHostException {

		InetAddress address = InetAddress.getByName(getHost());
		Socket socket = null;
		try {
			socket = new Socket();
			SocketAddress socketAddress = new InetSocketAddress(address, getPort());
			socket.connect(socketAddress, 5000);
			if(!socket.isConnected()) {
				return null;
			}
			return socket;
		} catch (IOException ioe) {
			log.error("Exception caught acquiring main socket to CLAMD on "
					+ address + " on port " + getPort() + ": " + ioe.getMessage());
		}
		return socket;
	}

	public void scanContent(String resourceReference) throws VirusFoundException, VirusScanIncompleteException {
		log.debug("scanContent(" + resourceReference + ")");
		if (contentHostingService.isCollection(resourceReference)) {
			log.debug("this is a folder no need to scan");
			return;
		}

		try {
			ContentResource resource = contentHostingService.getResource(resourceReference);
			if (resource.getContentLength() > 0) {
				scan(resource.streamContent());
			}
		} catch (PermissionException e) {
			log.warn("no permission to read: " + resourceReference);
			if (log.isDebugEnabled()) {
				log.warn("PermissionException", e);
			}
			
		} catch (IdUnusedException e) {
			log.warn("no such resource: " + resourceReference);
		} catch (TypeException e) {
			log.warn("TypeException: " + resourceReference);
			if (log.isDebugEnabled()) {
				log.warn("TypeException", e);
			}
		} catch (ServerOverloadException e) {
			log.warn("ServerOverloadException: " + resourceReference);
			if (log.isDebugEnabled()) {
				log.warn("ServerOverloadException", e);
			}
		} catch (VirusFoundException e) {
			//we should log an event for this is we likely have CHS events before and after this
			eventTrackingService.post(eventTrackingService.newEvent("antivirus.virusfound", contentHostingService.getReference(resourceReference), false));
			throw e;
		}
	}

	protected int getStreamPortFromAnswer(String answer) throws ConnectException {
		int port = -1;
		if(answer != null && answer.startsWith(STREAM_PORT_STRING)) {
			try {
				port = Integer.parseInt(answer.substring(STREAM_PORT_STRING.length()));
			} catch (NumberFormatException nfe) {  }
		}
		if(port <= 0) {
			throw new ConnectException("\"PORT nn\" expected - unable to parse: " + "\"" + answer + "\"");
		}

		return port;
	}


	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	private ContentHostingService contentHostingService;
	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}
	
	private EventTrackingService eventTrackingService;
	public void setEventTrackingService(EventTrackingService eventTrackingService) {
		this.eventTrackingService = eventTrackingService;
	}

	
}
