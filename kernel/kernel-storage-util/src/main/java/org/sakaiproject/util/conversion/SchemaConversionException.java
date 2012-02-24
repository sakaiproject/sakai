/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-util/src/main/java/org/sakaiproject/util/conversion/SchemaConversionException.java $
 * $Id: SchemaConversionException.java 101634 2011-12-12 16:44:33Z aaronz@vt.edu $
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

package org.sakaiproject.util.conversion;

/**
 * A major problem has been encountered with the conversion, rollback has been
 * attempted and the conversion should now be aborted.
 * 
 * @author ieb
 */
public class SchemaConversionException extends Exception {

    private static final long serialVersionUID = 1L;

	public SchemaConversionException() { }

	public SchemaConversionException(String arg0) {
		super(arg0);
	}

	public SchemaConversionException(Throwable arg0) {
		super(arg0);
	}

	public SchemaConversionException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
