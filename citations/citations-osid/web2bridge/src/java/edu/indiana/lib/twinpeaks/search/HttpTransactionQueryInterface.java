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
package edu.indiana.lib.twinpeaks.search;

import edu.indiana.lib.twinpeaks.net.HttpTransaction;

public interface HttpTransactionQueryInterface {

	/*
	 * Transaction redirect handling
	 */
	/**
	 * <code>URLConnection</code> handles all redirects
	 */
	final static int			REDIRECT_AUTOMATIC					= 0;
	/**
	 * The <code>QueryBase submit</code> code handles redirects
	 */
	final static int			REDIRECT_MANAGED						= 1;
	/**
	 * The caller will get control at each individual step in the redirect chain
	 */
	final static int			REDIRECT_MANAGED_SINGLESTEP	= 2;

	/**
	 * Set desired redirect behavior for the HTTP transaction code
	 * @param behavior Desired redirect handling. (one of the REDIRECT constants)
	 */
  public void setRedirectBehavior(int behavior);

	/*
	 * Query method
	 */
	final static String		METHOD_GET							= HttpTransaction.METHOD_GET;
	final static String		METHOD_POST							= HttpTransaction.METHOD_POST;
	/**
	 * @param method HTTP submission type. (one of the METHOD constants)
	 */
  public void setQueryMethod(String method);
}
