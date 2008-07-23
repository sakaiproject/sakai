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
package edu.indiana.lib.twinpeaks.search.sru.ss360search;
/**
 * Constants used by the 360 Search components
 */
public interface Constants
{
	/**
	 * 360 Search response - namespace values
	 */
	public static final String NS_CS          = "http://xml.serialssolutions.com/ns/sru/cs/v1.1";
	public static final String NS_SRW         = "http://www.loc.gov/zing/srw/";

	public static final String NS_DC          = "http://purl.org/dc/elements/1.1/";
	public static final String NS_DCTERMS     = "http://purl.org/dc/terms/";

  /**
   * 360 Search parameters
   */
  public static final String CS_CONTINUE    = "continue";

  public static final String CS_ACTION      = "x-cs-action";
  public static final String CS_CATEGORIES  = "x-cs-caregories";
  public static final String CS_DATABASES   = "x-cs-databases";
  public static final String CS_GROUPS      = "x-cs-groups";

  /**
   * 360 Search version details
   */
  public static final String CS_SCHEMA      = "cs1.1";
  public static final String CS_SRU_VERSION = "1.1";
}