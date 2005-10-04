/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/trunk/sakai/sam/tool/src/java/org/sakaiproject/tool/assessment/integration/helper/standalone/AuthzHelperImpl.java $
 * $Id: AuthzHelperImpl.java 2008 2005-09-23 20:01:57Z esmiley@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.assessment.integration.helper.standalone;

import java.util.ListResourceBundle;

/**
 *
 * <p>Description: SQL String resources for standalone AuthZ.</p>
 * <p>Sakai Project Copyright (c) 2005</p>
 * <p> </p>
 * @author Ed Smiley <esmiley@stanford.edu>
 *
 */
public class AuthzResource extends java.util.ListResourceBundle
{
  private static final Object[][] contents = new String[][]{
  { "view_pub", "VIEW_PUBLISHED_ASSESSMENT" },
	{ "select_authdata_w_qual", "select a from AuthorizationData a where a.qualifierId=\'" },
  { "select_authdata_w_fun", "select a from AuthorizationData a where a.functionId=\'" },
  { "select_authdata_w_agent", "select a from AuthorizationData a where a.agentIdString=\'" },
	{ "and_funid", "\' and a.functionId=\'" },
	{ "and_qid", "\' and a.qualifierId=\'" }};
  public Object[][] getContents()
  {
    return contents;
  }
}