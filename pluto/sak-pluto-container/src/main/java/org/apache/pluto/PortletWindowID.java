/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pluto;

/**
 * The portlet window ID.
 * @version 1.0
 */
public interface PortletWindowID {

	/**
	 * Returns the unique string ID of the portlet window.
	 * <p>
	 * Depending on the implementation of <code>toString()</code> is dangerous,
	 * because the original implementation in <code>Object</code> is not
	 * qualified.
	 * </p>
	 * @return the unique string ID of the portlet window.
	 */
	public String getStringId();

}
