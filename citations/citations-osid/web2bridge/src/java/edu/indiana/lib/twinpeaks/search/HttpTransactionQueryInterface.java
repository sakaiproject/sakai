/**********************************************************************************
*
 * Copyright (c) 2003, 2004, 2007, 2008 The Sakai Foundation
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
