/**********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2008 The Sakai Foundation
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