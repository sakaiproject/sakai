/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
 * Copyright (c) 2005 University of Cambridge
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

package uk.ac.cam.caret.sakai.rwiki.component.service.impl;

import java.text.MessageFormat;

import org.sakaiproject.service.framework.config.cover.ServerConfigurationService;
import org.sakaiproject.service.legacy.entity.Entity;
import org.sakaiproject.service.legacy.entity.Reference;

import uk.ac.cam.caret.sakai.rwiki.service.api.EntityHandler;

/**
 * 
 * Provides a regex base entity handler where the matching uses MessageFormats
 * and Regex patterns
 * 
 * @author ieb
 * 
 */
public abstract class BaseEntityHandlerImpl implements EntityHandler {

	/**
	 * The start of the URL endign in a /
	 */
	private String accessURLStart = null;

	/**
	 * The sub type of this handler, but be unique in the context of the major
	 * type
	 */
	private String minorType = null;

	/**
	 * if the eh provides a link, this URL will give the link
	 */
	private String feedFormat;

	private boolean experimental = false;

	private boolean setup = false;

	private boolean available = true;

	/**
	 * {@inheritDoc}
	 * 
	 * TODO
	 */
	public void setReference(String majorType, Reference ref, String reference) {
		if (!isAvailable())
			return;
		Decoded decoded = decode(reference);
		if (decoded != null) {
			ref.set(majorType, minorType, decoded.getId(), decoded
					.getContainer(), decoded.getContext());
		} else {
			throw new RuntimeException(this
					+ " Failed to setReference in EntityHelper " + majorType
					+ ":" + minorType
					+ " reference not for this EntityHandler ");
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * TODO
	 */
	public boolean matches(String reference) {
		if (!isAvailable())
			return false;
		return (decode(reference) != null);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * TODO
	 */
	public int getRevision(Reference reference) {
		if (!isAvailable())
			return 0;
		Decoded decode = decode(reference.getReference());
		return Integer.parseInt(decode.getVersion());
	}

	/**
	 * @return Returns the minorType.
	 */
	public String getMinorType() {
		return minorType;
	}

	/**
	 * @param minorType
	 *            The minorType to set.
	 */
	public void setMinorType(String minorType) {
		this.minorType = minorType;
	}

	/**
	 * 
	 * @param s
	 *            the full reference
	 * @return A Decoded object contianing the values, or null if not this
	 *         handler
	 */
	public Decoded decode(String s) {

		String ending = "." + minorType;
		if (isAvailable() && s.startsWith(accessURLStart) && s.endsWith(ending)
				&& s.indexOf("//") == -1 && s.indexOf("/,") == -1
				&& s.indexOf("/.") == -1) {
			Decoded decoded = new Decoded();
			s = s.substring(accessURLStart.length() - 1);
			int lastslash = s.lastIndexOf(Entity.SEPARATOR);
			int firstslash = s.indexOf(Entity.SEPARATOR, 1);
			int nextslash = s.indexOf(Entity.SEPARATOR, firstslash + 1);
			if (nextslash == -1) {
				nextslash = firstslash;
			}
			decoded.setContext(s.substring(0, nextslash));
			if (nextslash == lastslash) {
				decoded.setContainer(Entity.SEPARATOR);
			} else {
				decoded.setContainer(s.substring(nextslash, lastslash));
			}

			String filename = s.substring(lastslash + 1);
			filename = filename.substring(0, filename.length()
					- ending.length());
			int comma = filename.indexOf(",");
			if (comma != -1) {
				decoded.setPage(filename.substring(0, comma));
				decoded.setVersion(filename.substring(comma + 1));
			} else {
				decoded.setPage(filename);
				decoded.setVersion("-1");
			}

			return decoded;
		}
		
		return null;
	}

	/**
	 * @return Returns the accessURLStart.
	 */
	public String getAccessURLStart() {
		return accessURLStart;
	}

	/**
	 * @param accessURLStart
	 *            The accessURLStart to set.
	 */
	public void setAccessURLStart(String accessURLStart) {
		this.accessURLStart = accessURLStart;
	}

	public boolean isAvailable() {
		if (!setup) {
			if (!experimental) {
				available = true;
			} else if (ServerConfigurationService.getBoolean(
					"wiki.experimental", false)) {
				available = true;
			} else {
				available = false;
			}
			setup = true;
		}
		return available;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getHTML(Entity e) {
		if (isAvailable()) {
			if (feedFormat == null)
				return null;
			return MessageFormat
					.format(feedFormat, new Object[] { e.getUrl() });
		}
		return null;
	}

	/**
	 * @return Returns the feedFormat.
	 */
	public String getFeedFormat() {
		return feedFormat;
	}

	/**
	 * @param feedFormat
	 *            The feedFormat to set.
	 */
	public void setFeedFormat(String feedFormat) {
		this.feedFormat = feedFormat;
	}

	/**
	 * @return Returns the experimental.
	 */
	public boolean getExperimental() {
		return experimental;
	}

	/**
	 * @param experimental The experimental to set.
	 */
	public void setExperimental(boolean experimental) {
		this.experimental = experimental;
	}

}
